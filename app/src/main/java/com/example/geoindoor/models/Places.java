package com.example.geoindoor.models;

import com.mapbox.mapboxsdk.geometry.LatLng;

public class Places {
    LatLng coordinate;
    String place_name;

    public Places(String name, LatLng coordinate){
        this.place_name = name;
        this.coordinate = coordinate;
    }

    public LatLng getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(LatLng coordinate) {
        this.coordinate = coordinate;
    }

    public String getPlace_name() {
        return place_name;
    }

    public void setPlace_name(String place_name) {
        this.place_name = place_name;
    }
}
