package com.example.demo;

import com.fasterxml.jackson.databind.JsonNode;//önemli importlar
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Service;

import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.util.Base64;

@Service
public class SpotifyAuthService {

    private final String CLIENT_ID = "6470e4c138d8483d9d9bd152bf0cb2ed"; // spoti id
    private final String CLIENT_SECRET = "4b262a58a76f4ba587b1534f8cadf2fa"; // spoti Secret
    private final String SPOTIFY_TOKEN_URL = "https://accounts.spotify.com/api/token";//spoti api urlsi

    public String getAccessToken() {//token alma
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
            String temizToken = new ObjectMapper().readTree(response.getBody()).path("access_token").asText();

            /*
            //ObjectMapper mapper = new ObjectMapper();
            //JsonNode tokenNode = mapper.readTree(response.getBody()).path("access_token");
            //String temizToken = tokenNode.asText();*/

            //Terminal Kontrol
            System.out.println("AUTH SERVİS: Temiz Token oluşturuldu: " + temizToken.substring(0, 12) + "...");
            return temizToken;

        } catch (Exception e) {//hata kontrol
            System.out.println("TOKEN ALMA HATASI: " + e.getMessage());//terminal kontrol
            e.printStackTrace();
            return null;
        }
    }
}