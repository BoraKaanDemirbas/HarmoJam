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

import java.net.URLEncoder;
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

    public List<Song> searchSong(String query) {
        System.out.println("SPOTIFY: Arama yapılıyor -> " + query);//terminal kontrol

        String token = authService.getAccessToken();//token alma işlemi
        if (token == null) return new ArrayList<>();

        List<Song> sarkiListesi = new ArrayList<>();
        try {
            String Query = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = SEARCH_URL + "?q=" + Query + "&type=track&limit=12&market=TR";//url yapacağımız sorgu ve istediğimiz değişkenle birleştirme

            //HTTP header işlemi. göndereceğimiz zarfı hazırlama
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + token);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);//header zarfı paketleyip sunucuya gönderme işlemi
            JsonNode items = mapper.readTree(response.getBody()).path("tracks").path("items");//gelen cevabı dönüştüryoruz istediğimiz şeyleri alıyoruz Ayıklama

            for (JsonNode item : items){//istediğimiz veriler
                String id = item.path("id").asText();
                String isim = item.path("name").asText();
                String sarkici = item.path("artists").get(0).path("name").asText();
                String resimUrl = item.path("album").path("images").get(0).path("url").asText();
                String muzikUrl = item.path("preview_url").asText();

                sarkiListesi.add(new Song(id, isim, sarkici, resimUrl, muzikUrl));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return sarkiListesi;
    }
}