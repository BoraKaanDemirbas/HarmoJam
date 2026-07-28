package com.example.demo.Model;

import java.io.Serializable;
import java.util.Objects;

public class SongId implements Serializable {

    private String id;
    private String ownerId;

    public SongId() {
    }

    public SongId(String id, String ownerId) {
        this.id = id;
        this.ownerId = ownerId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SongId)) return false;
        SongId songId = (SongId) o;
        return Objects.equals(id, songId.id) && Objects.equals(ownerId, songId.ownerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, ownerId);
    }
}
