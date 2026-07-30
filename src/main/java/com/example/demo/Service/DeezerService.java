package com.example.demo.Service;

import com.example.demo.Model.Song;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;

@Service
public class DeezerService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    public Song findTrack(String parcaIsmi, String sarkiciISmi){
        try{
            String query = "artist:\"" + sarkiciISmi + "\" track:\"" + parcaIsmi + "\"";//sarkici ve sarki adları
            // ile sorgu yapacağız


            // UriComponentsBuilder.encode() ile tek seferde doğru encode edip URI olarak veriyoruz.
            URI deezerUri = UriComponentsBuilder.fromHttpUrl("https://api.deezer.com/search")
                    .queryParam("q", query)
                    .queryParam("order", "RANKING")
                    .queryParam("limit", 1)
                    .build()
                    .encode(StandardCharsets.UTF_8)
                    .toUri();

            String deezerRes = restTemplate.getForObject(deezerUri, String.class);
            JsonNode deezerRoot = mapper.readTree(deezerRes);

            if (!deezerRoot.path("data").isEmpty()) {//şarkı var ise yani boş değilse devam boşsa catch
                JsonNode songData = deezerRoot.path("data").get(0);

                // ID olarak Deezer ID'sini veriyoruz (Tekrar tıklanırsa Deezer'dan devam eder)
                //istenilen verileri alıyoruz
                String id = String.valueOf(songData.path("id").asLong());
                String isim = songData.path("title").asText();
                String sarkici = songData.path("artist").path("name").asText();
                String resimUrl = songData.path("album").path("cover_medium").asText();
                String muzikUrl = songData.path("preview").asText();

                System.out.println("EKLENDİ: " + isim + " - " + sarkici);//terminal Kontrol
                return new Song(id, isim, sarkici, resimUrl, muzikUrl);//istenilen verileri listeleyerek döndürüyoruz
            }



        }catch (Exception e){
            System.out.println("Deezer'da bulunamadı: " + parcaIsmi);//terminal Kontrol
        }


    return null;
    }

}
