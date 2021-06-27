package com.example.geoindoor.models;

public class MarkerWrapper {

    String markerAddress;
    String agendaTye;

    public MarkerWrapper(String markerAddress, String agendaTye) {
        this.markerAddress = markerAddress;
        this.agendaTye = agendaTye;
    }

    public String getMarkerAddress() {
        return markerAddress;
    }

    public void setMarkerAddress(String markerAddress) {
        this.markerAddress = markerAddress;
    }

    public String getAgendaTye() {
        return agendaTye;
    }

    public void setAgendaTye(String agendaTye) {
        this.agendaTye = agendaTye;
    }
}
