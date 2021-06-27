package com.example.geoindoor.models;

import com.mapbox.mapboxsdk.geometry.LatLng;

import java.io.Serializable;

public class Node implements Serializable {
    public LatLng coordinate;
    double bearing;     // Difference in bearing to the next node
    boolean isConnector = false;
    Connector connector =  null;
    boolean directionUp = false;
    boolean directionDown = false;


    public Node(LatLng coordinate, double bearing){
        this.coordinate = coordinate;
        this.bearing = bearing;
    }

    public void setConnector(Connector connector, boolean directionUp) {
        this.isConnector = true;
        this.connector = connector;
        if (directionUp) {
            goingUp();
        } else {
            goingDown();
        }
    }

    public void goingUp() {
        this.directionUp = true;
    }

    public void goingDown() {
        this.directionDown = true;
    }

    @Override
    public String toString() {
        if (isConnector) {
            String direction = this.directionUp ? "up": "down";
            return connector.toString() + " going " + direction;
        } else {
            return coordinate.toString() + ", " + bearing;
        }
    }
}
