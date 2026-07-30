package com.example.demo.Service;

//import com.example.demo.Model.Song;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class LastFmService {

    @Value("${lastfm.api.key}")//Last.fm API
    private String LASTFM_API_KEY;

    private final RestTemplate restTemplate = new RestTemplate();
    //private final ObjectMapper mapper = new ObjectMapper();

    public List<String[]> getRecommendation(String parcaIsmi, String sarkiciISmi){//öneri metodu
        List<String[]> recommendationNames = new ArrayList<>();
        try {
            System.out.println("LAST.FM: Benzerler soruluyor -> " + parcaIsmi);//kontrol

            // UriComponentsBuilder.encode() ile tek seferde doğru encode edip URI olarak veriyoruz.
            URI lastFmUri = UriComponentsBuilder.fromHttpUrl("http://ws.audioscrobbler.com/2.0/")
                    .queryParam("method", "track.getsimilar")
                    .queryParam("artist", sarkiciISmi)
                    .queryParam("track", parcaIsmi)
                    .queryParam("api_key", LASTFM_API_KEY)
                    .queryParam("format", "json")
                    .queryParam("limit", 12)
                    .build()
                    .encode(StandardCharsets.UTF_8)
                    .toUri();

            String lastFmResponse = restTemplate.getForObject(lastFmUri, String.class);//gelen cevap
            JsonNode similarTracks = new ObjectMapper().readTree(lastFmResponse).path("similartracks")
                    .path("track");//cevabı dönüştürme istediklerimizi alma paketi açma

            if (similarTracks.isEmpty()) {
                System.out.println("LAST.FM: Benzer bulamadı.");//Kontrol
                return recommendationNames;
            }

            for (JsonNode track : similarTracks) {
                String name = track.path("name").asText();
                String artist = track.path("artist").path("name").asText();

                // [0] -> Şarkı Adı, [1] -> Sanatçı Adı olarak listeye atıyoruz
                recommendationNames.add(new String[]{name, artist});
            }


        }catch (Exception e){
            System.out.println("LAST.FM KRİTİK HATA: " + e.getMessage());//terminal konrtol
            e.printStackTrace();
        }


    return recommendationNames;
    }

}
