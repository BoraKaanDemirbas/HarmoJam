package com.example.demo.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import jakarta.persistence.*;

@Entity
@Table(name = "favoriler")
public class Song {//değişkenler

    @Id
    @Column(length = 600)
    private String id;
    @Column(length = 600)
    private String isim;
    @Column(length = 600)
    private String sarkici;
    @Column(columnDefinition = "TEXT")
    private String resimUrl;
    @Column(length = 2048)
    private String muzikUrl;

    public Song() {
    }

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
    public void setId(String id) { this.id = id; }

    public String getIsim() { return isim; }
    public void setIsim(String isim) { this.isim = isim; }

    public String getSarkici() { return sarkici; }
    public void setSarkici(String sarkici) { this.sarkici = sarkici; }

    public String getResimUrl() { return resimUrl; }
    public void setResimUrl(String resimUrl) { this.resimUrl = resimUrl; }

    public String getMuzikUrl() { return muzikUrl; }
    public void setMuzikUrl(String muzikUrl) { this.muzikUrl = muzikUrl; }

}
