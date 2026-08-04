package com.example.demo.Service;

import com.example.demo.Model.Song;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class SpotifyService {
    @Autowired
    private SpotifyAuthService authService;

    private final ObjectMapper mapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    private final String SEARCH_URL = "https://api.spotify.com/v1/search";//gerekli url

    public List<Song> searchSong(String query, int offset) {
        System.out.println("SPOTIFY: Arama yapılıyor -> " + query + " (offset=" + offset + ")");//terminal kontrol

        String token = authService.getAccessToken();//token alma işlemi
        if (token == null) return new ArrayList<>();

        List<Song> sarkiListesi = new ArrayList<>();
        try {
            URI uri = UriComponentsBuilder.fromHttpUrl(SEARCH_URL)
                    .queryParam("q", query)
                    .queryParam("type", "track")
                    .queryParam("limit", 12)
                    .queryParam("offset", offset)
                    .queryParam("market", "TR")
                    .build()
                    .encode(StandardCharsets.UTF_8)
                    .toUri();

            //HTTP header işlemi. göndereceğimiz zarfı hazırlama
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + token);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);//header
            // zarfı paketleyip sunucuya gönderme işlemi

            JsonNode items = mapper.readTree(response.getBody()).path("tracks").path("items");//gelen cevabı
            // dönüştüryoruz istediğimiz şeyleri alıyoruz Ayıklama

            for (JsonNode item : items){//istediğimiz veriler
                String id = item.path("id").asText();
                String isim = item.path("name").asText();
                String sarkici = item.path("artists").get(0).path("name").asText();
                String resimUrl = item.path("album").path("images").get(0).path("url").asText();
                String muzikUrl = item.path("preview_url").asText();
                String spotifyUrl = item.path("external_urls").path("spotify").asText(null);

                sarkiListesi.add(new Song(id, isim, sarkici, resimUrl, muzikUrl,spotifyUrl));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return sarkiListesi;
    }
}