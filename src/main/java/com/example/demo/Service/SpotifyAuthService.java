package com.example.demo.Service;

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