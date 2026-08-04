package com.example.demo.Service;

import com.example.demo.Model.Song;
import com.example.demo.Model.SpotifyConnection;
import com.example.demo.Repository.SpotifyConnectionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SpotifyExportService {

    // Not: Bu property adlarının SpotifyAuthService'te kullandığın client id/secret
    // property adlarıyla AYNI olduğunu varsayıyorum. Eğer orada farklı bir isim
    // kullanıyorsan (örn. spotify.clientId), buradaki @Value satırlarını ona göre eşitle.
    @Value("${spotify.client.id}")
    private String clientId;

    @Value("${spotify.client.secret}")
    private String clientSecret;

    @Value("${SPOTIFY_REDIRECT_URI}")
    private String redirectUri;

    @Autowired
    private SpotifyConnectionRepository connectionRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    public boolean isConnected(String ownerId) {
        return connectionRepository.existsById(ownerId);
    }

    public void disconnect(String ownerId) {
        connectionRepository.deleteById(ownerId);
    }

    // Kullanıcıyı Spotify'ın izin ekranına yönlendirecek URL'i oluşturur.
    public String buildAuthorizeUrl(String ownerId) {
        String state = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(ownerId.getBytes(StandardCharsets.UTF_8));

        return "https://accounts.spotify.com/authorize"
                + "?client_id=" + clientId
                + "&response_type=code"
                + "&redirect_uri=" + java.net.URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&scope=" + java.net.URLEncoder.encode("playlist-modify-public playlist-modify-private", StandardCharsets.UTF_8)
                + "&state=" + state;
    }

    // Spotify'dan dönen state parametresinden hangi HarmoJam kullanıcısı olduğunu çözer.
    public String decodeState(String state) {
        return new String(Base64.getUrlDecoder().decode(state), StandardCharsets.UTF_8);
    }

    // Callback'te gelen "code"u gerçek access/refresh token'lara çevirip kaydeder.
    public void handleCallback(String code, String ownerId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(clientId, clientSecret);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("redirect_uri", redirectUri);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://accounts.spotify.com/api/token", entity, String.class);

        saveTokens(ownerId, response.getBody());
    }

    private void saveTokens(String ownerId, String tokenResponseJson) {
        try {
            JsonNode root = mapper.readTree(tokenResponseJson);
            String accessToken = root.path("access_token").asText();
            // Not: Spotify her zaman yeni bir refresh_token döndürmeyebilir (özellikle refresh
            // sırasında); o durumda eski refresh_token'ı koruyoruz.
            String refreshToken = root.path("refresh_token").asText(null);
            int expiresIn = root.path("expires_in").asInt(3600);

            SpotifyConnection conn = connectionRepository.findById(ownerId)
                    .orElse(new SpotifyConnection());
            conn.setOwnerId(ownerId);
            conn.setAccessToken(accessToken);
            if (refreshToken != null) {
                conn.setRefreshToken(refreshToken);
            }
            conn.setExpiresAt(LocalDateTime.now().plusSeconds(expiresIn));
            connectionRepository.save(conn);
        } catch (Exception e) {
            throw new RuntimeException("Spotify token kaydedilemedi: " + e.getMessage(), e);
        }
    }

    private SpotifyConnection getValidConnection(String ownerId) {
        SpotifyConnection conn = connectionRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalStateException("Spotify hesabı bağlı değil."));

        if (conn.getExpiresAt() == null || conn.getExpiresAt().isBefore(LocalDateTime.now().plusMinutes(1))) {
            refreshAccessToken(conn);
        }
        return conn;
    }

    private void refreshAccessToken(SpotifyConnection conn) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(clientId, clientSecret);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("refresh_token", conn.getRefreshToken());

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://accounts.spotify.com/api/token", entity, String.class);

        saveTokens(conn.getOwnerId(), response.getBody());
    }

    // Verilen şarkı listesini yeni bir Spotify playlist'ine aktarır.
    public Map<String, Object> exportSongs(String ownerId, String playlistName, List<Song> songs) {
        SpotifyConnection conn = getValidConnection(ownerId);

        String spotifyUserId = getCurrentSpotifyUserId(conn.getAccessToken());
        String newPlaylistId = createSpotifyPlaylist(conn.getAccessToken(), spotifyUserId, playlistName);

        List<String> uris = songs.stream()
                .map(this::spotifyTrackUriFromSong)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        int skipped = songs.size() - uris.size();

        // Spotify tek istekte en fazla 100 track kabul ediyor, o yüzden 100'lük gruplar hâlinde gönderiyoruz.
        for (int i = 0; i < uris.size(); i += 100) {
            addTracksToPlaylist(conn.getAccessToken(), newPlaylistId, uris.subList(i, Math.min(i + 100, uris.size())));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("exported", uris.size());
        result.put("skipped", skipped);
        result.put("spotifyPlaylistUrl", "https://open.spotify.com/playlist/" + newPlaylistId);
        return result;
    }

    private String spotifyTrackUriFromSong(Song s) {
        if (s.getSpotifyUrl() == null || s.getSpotifyUrl().isBlank()) return null;
        String url = s.getSpotifyUrl();
        int idx = url.lastIndexOf('/');
        if (idx == -1) return null;
        String trackId = url.substring(idx + 1);
        int qIdx = trackId.indexOf('?');
        if (qIdx != -1) trackId = trackId.substring(0, qIdx);
        return "spotify:track:" + trackId;
    }

    private String getCurrentSpotifyUserId(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "https://api.spotify.com/v1/me", HttpMethod.GET, entity, String.class);
        try {
            return mapper.readTree(response.getBody()).path("id").asText();
        } catch (Exception e) {
            throw new RuntimeException("Spotify kullanıcı bilgisi alınamadı.", e);
        }
    }

    private String createSpotifyPlaylist(String accessToken, String spotifyUserId, String playlistName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("name", playlistName);
        body.put("public", false);
        body.put("description", "Exported from HarmoJam");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://api.spotify.com/v1/users/" + spotifyUserId + "/playlists", entity, String.class);

        try {
            return mapper.readTree(response.getBody()).path("id").asText();
        } catch (Exception e) {
            throw new RuntimeException("Spotify playlist oluşturulamadı.", e);
        }
    }

    private void addTracksToPlaylist(String accessToken, String spotifyPlaylistId, List<String> uris) {
        if (uris.isEmpty()) return;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("uris", uris);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        restTemplate.postForEntity(
                "https://api.spotify.com/v1/playlists/" + spotifyPlaylistId + "/tracks", entity, String.class);
    }
}