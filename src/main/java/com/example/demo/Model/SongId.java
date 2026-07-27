package com.example.demo.Model;

import java.io.Serializable;
import java.util.Objects;

public class SongId implements Serializable {

    private String id;
    private String deviceId;

    public SongId() {
    }

    public SongId(String id, String deviceId) {
        this.id = id;
        this.deviceId = deviceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SongId)) return false;
        SongId songId = (SongId) o;
        return Objects.equals(id, songId.id) && Objects.equals(deviceId, songId.deviceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, deviceId);
    }
}
