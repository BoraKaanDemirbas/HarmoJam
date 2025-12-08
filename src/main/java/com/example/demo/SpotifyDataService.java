package com.example.demo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import java.util.ArrayList;
import java.util.List;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class SpotifyDataService {

    @Autowired
    private SpotifyAuthService authService;//auth servise ile bağlantı kuruyoruz

    //Sportify arama url'si
    private final String SEARCH_URL = "https://api.spotify.com/v1/search";



    @Value("${lastfm.api.key}")//Last.fm API
    private String LASTFM_API_KEY;

    // Arama Metodu
    public List<Song> searchSong(String query) {
        System.out.println("SPOTIFY: Arama yapılıyor -> " + query);//terminal kontrol
        String url = SEARCH_URL + "?q=" + query + "&type=track&limit=12";//spoti api arama urlsini üzerine sorgu işlemi için gerekenler girilir
        return fetchFromSpotify(url);//arama sonucu ekrana yazdırılacak olan sonuçları filtrelediğimiz metodu çağırıyoruz
    }

    //spotify arama metodu
    private List<Song> fetchFromSpotify(String url) {
        RestTemplate restTemplate = new RestTemplate();
        // Token alma işlemi OAuth
        String token = authService.getAccessToken();
        if (token == null) return new ArrayList<>();//token yoksa diye kontrol

        //HTTP header işlemi. göndereceğimiz zarfı hazırlama
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        List<Song> sarkiListesi = new ArrayList<>();
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);//header zarfı paketleyip sunucuya gönderme işlemi
            ObjectMapper mapper = new ObjectMapper();
            JsonNode items = mapper.readTree(response.getBody()).path("tracks").path("items");//gelen cevabı dönüştüryoruz istediğimiz şeyleri alıyoruz Ayıklama

            for (JsonNode item : items) {//filtrelemek istediğimiz özellikleri filtreleme
                String id = item.path("id").asText();
                String isim = item.path("name").asText();
                String sarkici = item.path("artists").get(0).path("name").asText();
                String resimUrl = item.path("album").path("images").get(0).path("url").asText();
                /*String resimUrl = "https://via.placeholder.com/300"; // Eğer resim yoksa bu gri kutuyu göster
                JsonNode imageNode = item.path("album").path("images");
                if (imageNode.size() > 0) {
                    resimUrl = imageNode.get(0).path("url").asText();
                }*/

                String muzikUrl = item.path("preview_url").asText();
                if (muzikUrl == null || muzikUrl.equals("null") || muzikUrl.isEmpty()) {
                    muzikUrl = findPreviewOnDeezer(isim, sarkici);//30 saniye müzik spotify APIda olmadığından deezerdan alıyoruz
                }

                if (muzikUrl == null || muzikUrl.equals("null")) muzikUrl = "Müzik Yok";

                sarkiListesi.add(new Song(id, isim, sarkici, resimUrl, muzikUrl));
            }
        } catch (Exception e) {
            e.printStackTrace();//hata olması durumunda try catch
        }
        return sarkiListesi;
    }

    // Öneri alma metodu Last.fm //30sn önizleme,album vs. Deezer
    public List<Song> getRecommendation(String trackName, String artistName) {
        RestTemplate restTemplate = new RestTemplate();
        List<Song> sarkiListesi = new ArrayList<>();

        try {
            System.out.println("LAST.FM: Benzerler soruluyor -> " + trackName);//kontrol

            // URL Encoding boşluklu isimlerde hata çıkmaması sebebi ile
            String encodedTrack = URLEncoder.encode(trackName, StandardCharsets.UTF_8);
            String encodedArtist = URLEncoder.encode(artistName, StandardCharsets.UTF_8);


            //last.fm sarkici ve sarki arama urlsi olusturma sınır 12
            String lastFmUrl = "http://ws.audioscrobbler.com/2.0/?method=track.getsimilar&artist=" + encodedArtist + "&track=" + encodedTrack + "&api_key=" + LASTFM_API_KEY + "&format=json&limit=12";

            String lastFmResponse = restTemplate.getForObject(lastFmUrl, String.class);//cevabı alma
            JsonNode similarTracks = new ObjectMapper().readTree(lastFmResponse).path("similartracks").path("track");//cevabı dönüştürme istediklerimizi alma paketi açma

            if (similarTracks.isEmpty()) {
                System.out.println("LAST.FM: Benzer bulamadı.");//Kontrol
                return sarkiListesi;
            }

            //Resim ve Müzik için gelen isimleri Deezer'da Aratma işlemi
            int sayac = 0;
            for (JsonNode track : similarTracks) {//üstte aldığımız sonuçları track içine koyduk deezera göndereceğimiz için
                if (sayac >= 12) break;//hepsi bitene kadar for işlemine girdik toplam 12tane

                String similarName = track.path("name").asText();//isimler
                String similarArtist = track.path("artist").path("name").asText();//sarkici isimler

                // Deezer url işlemi gerekenleri koyarak urlyi hazırlıyoruz
                // &order=RANKING ile en popüler (orijinal) versiyonu istiyoruz
                String deezerUrl = "https://api.deezer.com/search?q=artist:\"" + similarArtist + "\" track:\"" + similarName + "\"&order=RANKING&limit=1";

                try {
                    String deezerRes = restTemplate.getForObject(deezerUrl, String.class);//string cevap istiyoruz
                    // deezer api açık olduğundan exchange header token ile uğraşmıyoruz zarflamıyoruz direkt istek atıyoruz
                    JsonNode deezerRoot = new ObjectMapper().readTree(deezerRes);//alınan sonucu paketi aç oku//haritalama

                    if (!deezerRoot.path("data").isEmpty()) {//şarkı var ise yani boş değilse devam boşsa catch
                        JsonNode songData = deezerRoot.path("data").get(0);

                        // ID olarak Deezer ID'sini veriyoruz (Tekrar tıklanırsa Deezer'dan devam eder)
                        //istenilen verileri alıyoruz
                        String id = String.valueOf(songData.path("id").asLong());
                        String isim = songData.path("title").asText();
                        String sarkici = songData.path("artist").path("name").asText();
                        String resimUrl = songData.path("album").path("cover_medium").asText();
                        String muzikUrl = songData.path("preview").asText();

                        sarkiListesi.add(new Song(id, isim, sarkici, resimUrl, muzikUrl));//istenilen verileri listeleme
                        sayac++;
                        System.out.println("EKLENDİ: " + isim + " - " + sarkici);//terminal Kontrol
                    }
                } catch (Exception e) {
                    //şarkı Deezer'da yoksa sonrakine geç
                    continue;
                }
            }

        } catch (Exception e) {
            System.out.println("ÖNERİ HATASI: " + e.getMessage());//hata Kontrol
            e.printStackTrace();
        }
        return sarkiListesi;//listeyi döndür
    }

    private String findPreviewOnDeezer(String trackName, String artistName) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            // Deezer'da sanatçı ve şarkı ismiyle arama yap
            String q = "artist:\"" + artistName + "\" track:\"" + trackName + "\"";
            String deezerUrl = "https://api.deezer.com/search?q=" + URLEncoder.encode(q, StandardCharsets.UTF_8) + "&limit=1";

            String deezerRes = restTemplate.getForObject(deezerUrl, String.class);
            JsonNode root = new ObjectMapper().readTree(deezerRes);
            JsonNode data = root.path("data");

            if (data.isArray() && data.size() > 0) {
                // Eğer Deezer bulduysa, müzik linkini geri gönder
                return data.get(0).path("preview").asText();
            }
        } catch (Exception e) {
            // Hata olursa null dön
        }
        return "Müzik Yok";
    }
}