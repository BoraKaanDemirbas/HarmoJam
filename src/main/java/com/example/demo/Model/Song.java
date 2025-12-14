package com.example.demo;

public class Song {//değişkenler
    private String id;
    private String isim;
    private String sarkici;
    private String resimUrl;
    private String muzikUrl;


    public Song(String id, String isim, String sarkici, String resimUrl, String muzikUrl) {
        this.id = id;
        this.isim = isim;
        this.sarkici = sarkici;
        this.resimUrl = resimUrl;
        this.muzikUrl = muzikUrl;
        //Constructor
    }

    //erişim metotları Getters
    //değişkenler private olduğundan frontendin verileri erişmesi için açık kapı bırakıyoruz getter
    public String getId() { return id; }
    public String getIsim() { return isim; }
    public String getSarkici() { return sarkici; }
    public String getResimUrl() { return resimUrl; }
    public String getMuzikUrl() { return muzikUrl; }

}
