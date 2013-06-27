package com.precisionag.lib;

public class Dem {
    int id;
    float sw_lat;
    float sw_long;
    float ne_lat;
    float ne_long;
    String filename;
    String timestamp;

    public Dem(int id, float sw_lat, float sw_long, float ne_lat, float ne_long, String filename, String timestamp) {
        this.id = id;
        this.sw_lat = sw_lat;
        this.sw_long = sw_long;
        this.ne_lat = ne_lat;
        this.ne_long = ne_long;
        this.filename = filename;
        this.timestamp = timestamp;
    }

    public Dem(float sw_lat, float sw_long, float ne_lat, float ne_long, String filename, String timestamp) {
        this.sw_lat = sw_lat;
        this.sw_long = sw_long;
        this.ne_lat = ne_lat;
        this.ne_long = ne_long;
        this.filename = filename;
        this.timestamp = timestamp;
    }

    public Dem() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getSw_lat() {
        return sw_lat;
    }

    public void setSw_lat(float sw_lat) {
        this.sw_lat = sw_lat;
    }

    public float getSw_long() {
        return sw_long;
    }

    public void setSw_long(float sw_long) {
        this.sw_long = sw_long;
    }

    public float getNe_lat() {
        return ne_lat;
    }

    public void setNe_lat(float ne_lat) {
        this.ne_lat = ne_lat;
    }

    public float getNe_long() {
        return ne_long;
    }

    public void setNe_long(float ne_long) {
        this.ne_long = ne_long;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

}