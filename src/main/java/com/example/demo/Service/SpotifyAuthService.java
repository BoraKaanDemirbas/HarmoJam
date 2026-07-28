package com.example.demo.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.util.Base64;

@Service
public class SpotifyAuthService {

    @Value("${spotify.client.id}")
    private String CLIENT_ID;//spoti api id

    @Value("${spotify.client.secret}")
    private String CLIENT_SECRET; //spoti api secret

    private final String SPOTIFY_TOKEN_URL = "https://accounts.spotify.com/api/token";//spoti api urlsi

    // Token'ı ve son geçerlilik zamanını hafızada tutuyoruz, her aramada
    // Spotify'a yeniden istek atmamak için (token normalde ~1 saat geçerli).
    private String cachedToken;
    private long tokenExpiryEpochMillis = 0;

    public synchronized String getAccessToken() {//token alma
        // Elimizdeki token hâlâ geçerliyse (30 saniyelik güvenlik payı ile) onu döndür
        if (cachedToken != null && System.currentTimeMillis() < tokenExpiryEpochMillis - 30_000) {
            return cachedToken;
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            String authString = CLIENT_ID + ":" + CLIENT_SECRET;//id ile secret birleştirme çünkü spoti api öyle istiyor
            String base64AuthString = Base64.getEncoder().encodeToString(authString.getBytes());//base64 tabanında encoder

            HttpHeaders headers = new HttpHeaders();//HTTP header zarf oluşturma işlemi
            headers.add("Authorization", "Basic " + base64AuthString);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "client_credentials");//body için gerekli özellikleri belirliyoruz

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);//body ve header birleştiriyoruz

            // isteği atıyoruz url+body+header+istedğimiz cevap(string)
            ResponseEntity<String> response = restTemplate.postForEntity(SPOTIFY_TOKEN_URL, requestEntity, String.class);

            //Terminal kontrol
            System.out.println("AUTH SERVİS: Spotify'dan cevap geldi: " + response.getStatusCode());

            // gelen JSON cevabı istediğimiz hale getiriyoruz
            JsonNode root = new ObjectMapper().readTree(response.getBody());
            String temizToken = root.path("access_token").asText();
            int expiresInSeconds = root.path("expires_in").asInt(3600); // Spotify vermezse 1 saat varsay

            cachedToken = temizToken;
            tokenExpiryEpochMillis = System.currentTimeMillis() + (expiresInSeconds * 1000L);

            //Terminal Kontrol
            System.out.println("AUTH SERVİS: Yeni token alındı (" + expiresInSeconds + " sn geçerli): " + temizToken.substring(0, 12) + "...");
            return cachedToken;

        } catch (Exception e) {//hata kontrol
            System.out.println("TOKEN ALMA HATASI: " + e.getMessage());//terminal kontrol
            e.printStackTrace();
            return null;
        }
    }
}