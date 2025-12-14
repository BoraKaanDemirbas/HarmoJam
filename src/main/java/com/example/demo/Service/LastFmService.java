package com.example.demo.Service;

//import com.example.demo.Model.Song;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
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

            String encodedParca = URLEncoder.encode(parcaIsmi, StandardCharsets.UTF_8);//sorguda boşluklar hata çıkarmasın diye encode
            String encodedSarkici = URLEncoder.encode(sarkiciISmi, StandardCharsets.UTF_8);

            //gerekli urlyi hazırlama
            String lastFmUrl = "http://ws.audioscrobbler.com/2.0/?method=track.getsimilar&artist=" + encodedSarkici + "&track=" + encodedParca + "&api_key=" + LASTFM_API_KEY + "&format=json&limit=12";

            String lastFmResponse = restTemplate.getForObject(lastFmUrl, String.class);//gelen cevap
            JsonNode similarTracks = new ObjectMapper().readTree(lastFmResponse).path("similartracks").path("track");//cevabı dönüştürme istediklerimizi alma paketi açma

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
