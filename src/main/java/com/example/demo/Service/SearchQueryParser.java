package com.example.demo.Service;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Component
public class SearchQueryParser {

    // Tanınan tür (genre) kelimeleri -> Spotify genre etiketi.
    // LinkedHashMap "hip hop" gibi çok kelimeli ifadeler için
    private static final Map<String, String> GENRE_MAP = new LinkedHashMap<>();
    static {
        GENRE_MAP.put("hip hop", "hip-hop");
        GENRE_MAP.put("hiphop", "hip-hop");
        GENRE_MAP.put("rap", "hip-hop");
        GENRE_MAP.put("r&b", "r-n-b");
        GENRE_MAP.put("rnb", "r-n-b");
        GENRE_MAP.put("k-pop", "k-pop");
        GENRE_MAP.put("kpop", "k-pop");
        GENRE_MAP.put("rock", "rock");
        GENRE_MAP.put("pop", "pop");
        GENRE_MAP.put("alternatif", "alternative");
        GENRE_MAP.put("alternative", "alternative");
        GENRE_MAP.put("elektronik", "electronic");
        GENRE_MAP.put("electronic", "electronic");
        GENRE_MAP.put("caz", "jazz");
        GENRE_MAP.put("jazz", "jazz");
        GENRE_MAP.put("klasik", "classical");
        GENRE_MAP.put("classical", "classical");
        GENRE_MAP.put("metal", "metal");
        GENRE_MAP.put("punk", "punk");
        GENRE_MAP.put("indie", "indie");
        GENRE_MAP.put("blues", "blues");
        GENRE_MAP.put("reggae", "reggae");
        GENRE_MAP.put("country", "country");
        GENRE_MAP.put("folk", "folk");
        GENRE_MAP.put("dans", "dance");
        GENRE_MAP.put("dance", "dance");
        GENRE_MAP.put("soul", "soul");
        GENRE_MAP.put("funk", "funk");
        GENRE_MAP.put("disco", "disco");
        GENRE_MAP.put("grunge", "grunge");
        GENRE_MAP.put("latin", "latin");
    }

    // "müzik/şarkı/parça" gibi tür ya da yıl belirtmeyen, arama isabetini
    // bozan genel/dolgu kelimeler. Serbest metinden ayıklanırlar.
    private static final String[] FILLER_WORDS = {
            "müzikleri", "müzikler", "müziği", "müzik",
            "şarkıları", "şarkılar", "şarkısı", "şarkı",
            "parçaları", "parçalar", "parça",
            "tracks", "track", "songs", "song", "music", "musics",
            "en iyi", "en güzel", "en sevilen"
    };

    // "1980'lerin", "80'ler", "2000ler", "90'ların" gibi onyıl (decade) ifadeleri.
    // Türkçe karakterler için UNICODE_CHARACTER_CLASS
    private static final int FLAGS = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.UNICODE_CHARACTER_CLASS;

    private static final Pattern YEAR_PATTERN = Pattern.compile(
            "\\byear\\s*:\\s*" +
                    "(\\d{2,4})" +                                   // grup1: ilk sayı
                    "('?s|'?(?:lar|ler)\\w*)?" +                      // grup2: decade eki (varsa)
                    "(?:\\s*[-\\s]\\s*(\\d{2,4}))?" +                 // grup3: ikinci sayı (aralık için)
                    "(?:\\s+(başı|başları|erken|early|ortası|orta|mid|sonu|geç|late))?", // grup4: parça kelimesi
            FLAGS);

    // Onyılın hangi bölümünden bahsedildiğini belirten kelimeler
    private static final Pattern EARLY_WORD = Pattern.compile("başı|başları|erken|early", FLAGS);
    private static final Pattern MID_WORD   = Pattern.compile("ortası|orta|mid", FLAGS);
    private static final Pattern LATE_WORD  = Pattern.compile("sonu|geç|late", FLAGS);

    /* Ayrıştırma sonucu: hem Spotify'a gidecek nihai sorgu hem de ayrıştırılan parçalar. */
    public static class ParsedQuery {
        public final String spotifyQuery;
        public final Integer yearFrom;
        public final Integer yearTo;
        public final String genre;
        public final String freeText;

        ParsedQuery(String spotifyQuery, Integer yearFrom, Integer yearTo, String genre, String freeText) {
            this.spotifyQuery = spotifyQuery;
            this.yearFrom = yearFrom;
            this.yearTo = yearTo;
            this.genre = genre;
            this.freeText = freeText;
        }

        public boolean hasFilters() {
            return yearFrom != null || genre != null;
        }
    }

    public ParsedQuery parse(String rawQuery) {
        String working = rawQuery == null ? "" : rawQuery;

        Integer yearFrom = null;
        Integer yearTo = null;

        // 1) Onyıl (decade) tespiti
        // "year:" ifadesi tespiti
        Matcher yearMatcher = YEAR_PATTERN.matcher(working);
        if (yearMatcher.find()) {
            String firstNum = yearMatcher.group(1);
            String decadeSuffix = yearMatcher.group(2);   // s / lar / ler eki
            String secondNum = yearMatcher.group(3);      // aralık ikinci sayı
            String partWord = yearMatcher.group(4);       // başı/ortası/sonu vb.

            if (decadeSuffix != null) {
                // "year:1990s", "year:90lar", "year:80s early" -> onyıl (+ parça)
                int base = resolveDecadeBase(firstNum);
                if (base > 0) {
                    if (partWord != null && EARLY_WORD.matcher(partWord).matches()) {
                        yearFrom = base; yearTo = base + 3;
                    } else if (partWord != null && MID_WORD.matcher(partWord).matches()) {
                        yearFrom = base + 3; yearTo = base + 6;
                    } else if (partWord != null && LATE_WORD.matcher(partWord).matches()) {
                        yearFrom = base + 6; yearTo = base + 9;
                    } else {
                        yearFrom = base; yearTo = base + 9;
                    }
                }
            } else if (secondNum != null) {
                // "year:1990-2000" / "year:1990 2000" -> açık aralık
                yearFrom = resolveYearFull(firstNum);
                yearTo = resolveYearFull(secondNum);
            } else {
                // "year:2015" -> tekil yıl
                yearFrom = resolveYearFull(firstNum);
                yearTo = yearFrom;
            }

            // Eşleşen "year:..." ifadesini metinden çıkar
            working = working.substring(0, yearMatcher.start()) + " " + working.substring(yearMatcher.end());

        }

        // 2) Tür (genre) tespiti - şimdilik ilk eşleşen tür alınır
        String genreTag = null;
        for (Map.Entry<String, String> entry : GENRE_MAP.entrySet()) {
            Pattern p = Pattern.compile("\\bgenre\\s*:\\s*\"?" + Pattern.quote(entry.getKey()) + "\"?\\b", FLAGS);
            Matcher m = p.matcher(working);
            if (m.find()) {
                genreTag = entry.getValue();
                working = working.substring(0, m.start()) + " " + working.substring(m.end());
                break;
            }
        }

        // 3) Anlam taşımayan dolgu kelimelerini temizle
        for (String filler : FILLER_WORDS) {
            Pattern p = Pattern.compile("\\b" + Pattern.quote(filler) + "\\b", FLAGS);
            working = p.matcher(working).replaceAll(" ");
        }

        String freeText = working.replaceAll("\\s+", " ").trim();

        // 4) Spotify'a gidecek nihai sorguyu birleştir
        StringBuilder sb = new StringBuilder();
        if (!freeText.isEmpty()) {
            sb.append(freeText);
        }
        if (genreTag != null) {
            if (sb.length() > 0) sb.append(' ');
            sb.append("genre:\"").append(genreTag).append('"');
        }
        if (yearFrom != null) {
            if (sb.length() > 0) sb.append(' ');
            sb.append("year:").append(yearFrom).append('-').append(yearTo);
        }

        // Hiçbir özel filtre bulunamadıysa orijinal sorguyu aynen kullan
        String finalQuery = sb.length() > 0 ? sb.toString() : rawQuery;

        return new ParsedQuery(finalQuery, yearFrom, yearTo, genreTag, freeText);
    }

    // 2 ya da 4 haneli onyıl ifadesini gerçek başlangıç yılına çevirir
    // "80" -> 1980, "20" -> 2020, "2000" -> 2000, "1990" -> 1990
    private int resolveDecadeBase(String digits) {
        try {
            int n = Integer.parseInt(digits);
            if (digits.length() == 4) {
                return (n / 10) * 10;
            }
            if (digits.length() == 2) {
                // 30-99 -> 1900'ler, 00-29 -> 2000'ler
                return n >= 30 ? 1900 + n : 2000 + n;
            }
        } catch (NumberFormatException ignored) {
            // rakam ayrıştırılamadıysa yok say
        }
        return -1;
    }
    private int resolveYearFull(String digits) {
        try {
            int n = Integer.parseInt(digits);
            if (digits.length() == 4) {
                return n;
            }
            if (digits.length() == 2) {
                // 30-99 -> 1900'ler, 00-29 -> 2000'ler
                return n >= 30 ? 1900 + n : 2000 + n;
            }
        } catch (NumberFormatException ignored) {
            // rakam ayrıştırılamadıysa yok say
        }
        return -1;
    }
}
