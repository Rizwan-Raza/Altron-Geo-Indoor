package com.example.geoindoor;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import com.example.geoindoor.models.Connector;
import com.example.geoindoor.models.Node;
import com.example.geoindoor.models.Places;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Routing {
    private Double inf = Double.POSITIVE_INFINITY;
    List<List<Point>> boundingList;
    private LatLng start, end;
    private int floor = -1;
    private HashMap<Integer, LatLng> junctions;
    private HashMap<Integer, HashMap<Integer, LatLng>> levelJunctions;
    private HashMap<Integer, Integer> backtracking;
    private HashMap<Integer, List<List<Pair<Integer, Double>>>> floorAdjacencyList;
    private List<Double> distances;
    private Context mContext;
    private HashMap<Integer, HashMap<Integer, Pair<Connector, LatLng>>> connectorLevelsCoords;
    String buildingName;


    public Routing(Context context, String buildingName, List<Places> perimeterList){
        mContext = context;
        this.buildingName = buildingName;
        levelJunctions = new HashMap<>();
        connectorLevelsCoords = new HashMap<>();
        floorAdjacencyList = new HashMap<>();
        filterFeatures(0,buildingName);
        addToAdjacencyList(0,buildingName);
        List<Point> perimeter = null;
        boundingList = new ArrayList<>();
        for(int i = 0; i < perimeterList.size(); i ++) {
            perimeter = Arrays.asList(Point.fromLngLat(perimeterList.get(i).getCoordinate().getLongitude(),
                                                  perimeterList.get(i).getCoordinate().getLatitude()));
        }
        boundingList.add(perimeter);
    }

    private void filterFeatures(int level,String buildingName){
        HashMap<Integer, LatLng> junctions = new HashMap<>();
        HashMap<Integer, Pair<Connector, LatLng>> connectors = new HashMap<>();
        FeatureCollection featureCollection;
        try {
            String filename = buildingName + ".geojson";
            featureCollection = FeatureCollection.fromJson(loadJsonFromAsset(filename));
        } catch (Exception e) {
            return;
        }
        List<Feature> featureList = featureCollection.features();

        for (int i = 0; i < featureList.size(); i++) {
            Feature singleLocation = featureList.get(i);
            if (singleLocation.hasProperty("connector") && singleLocation.hasProperty("type")) {
                Double stringLng = ((Point)singleLocation.geometry()).coordinates().get(0);
                Double stringLat = ((Point)singleLocation.geometry()).coordinates().get(1);
                LatLng locationLatLng = new LatLng(stringLat, stringLng);
                locationLatLng.setAltitude(0);
                Integer type = singleLocation.getNumberProperty("type").intValue();
            }
            if(singleLocation.hasProperty("junction")){
                Double stringLng = ((Point)singleLocation.geometry()).coordinates().get(0);
                Double stringLat = ((Point)singleLocation.geometry()).coordinates().get(1);
                System.out.println(stringLat);
                System.out.println(stringLng);
                LatLng locationLatLng = new LatLng(stringLat, stringLng);
                locationLatLng.setAltitude(0);
                Integer vertex = singleLocation.getNumberProperty("junction").intValue();
                junctions.put(vertex, locationLatLng);
            }
        }
        levelJunctions.put(level, junctions);
        connectorLevelsCoords.put(level, connectors);
    }

    private String loadJsonFromAsset(String filename){
        try{
            InputStream is = mContext.getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            return new String(buffer, "UTF-8");
        } catch(IOException e){
            e.printStackTrace();
            return null;
        }
    }


    private double calculate_distance(LatLng current, LatLng destination){
        int earthRadiusKm = 6371;
        double dLat = degreeToRadians(destination.getLatitude()-current.getLatitude());
        double dLng = degreeToRadians(destination.getLongitude()-current.getLongitude());

        double lat1 = degreeToRadians(current.getLatitude());
        double lat2 = degreeToRadians(destination.getLatitude());

        double a = Math.sin(dLat/2)*Math.sin(dLat/2)+
                Math.sin(dLng/2) * Math.sin(dLng/2) *Math.cos(lat1)*Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        return earthRadiusKm*c;
    }

    private void addToAdjacencyList(int level,String buildingName) {
        List<List<Pair<Integer, Double>>> adjacencyList = new ArrayList<>();
        try{
            DataInputStream textFileStream = new DataInputStream(mContext.getAssets().open(String.format(buildingName + ".txt")));
            Scanner scanner = new Scanner(textFileStream);
            int i=0;
            while(scanner.hasNextLine()){
                String line = scanner.nextLine();
                String[] vertices = line.split(" ");
                List<Pair<Integer,Double>> neighbors = new ArrayList<>();
                for(String s: vertices){
                    Integer vertex = Integer.valueOf(s);
                    neighbors.add(Pair.create(vertex, calculate_distance(levelJunctions.get(level).get(i),
                            levelJunctions.get(level).get(vertex))));
                }
                adjacencyList.add(neighbors);
                i++;
            }
            scanner.close();
            floorAdjacencyList.put(level, adjacencyList);
        } catch (IOException e){
            e.printStackTrace();
        }
    }


    private double degreeToRadians(double degrees){
        return degrees*Math.PI/180;
    }

    public HashMap<Integer, List<Node>> getRoute(LatLng start, LatLng end) {
        HashMap<Integer, List<Node>> route = new HashMap<>();
        int startFloor = (int)start.getAltitude();
        route.put(startFloor, getRouteWithinLevel(start, end));
        return route;
    }

    public List<Node> getRouteWithinLevel(LatLng startingPoint, LatLng endingPoint){
        start = startingPoint;
        end = endingPoint;
        floor = (int)startingPoint.getAltitude();
        junctions = levelJunctions.get(floor);
        backtracking = new HashMap<>();
        List<List<Pair<Integer, Double>>> adjacencyList = floorAdjacencyList.get(floor);
        distances = IntStream.range(0, adjacencyList.size())
                .boxed().map(x -> inf).collect(Collectors.toList());

        List<Integer> unvisitedJunctions = IntStream.range(0, adjacencyList.size())
                .boxed().collect(Collectors.toList());

        Integer nextJunction = nearestJunction(startingPoint);
        distances.set(nextJunction, calculate_distance(startingPoint, junctions.get(nextJunction)));
        boolean destinationReached = false;
        while(!destinationReached){
            unvisitedJunctions.remove(nextJunction);
            if(!unvisitedJunctions.contains(nearestJunction(endingPoint))){
                destinationReached = true;
                break;
            }
            for(Pair<Integer,Double> neighborDist: adjacencyList.get(nextJunction)){
                Integer neighbor = neighborDist.first;
                Double toDistance = neighborDist.second;
                // if unvisited
                if(unvisitedJunctions.contains(neighbor) &&
                        toDistance + distances.get(nextJunction) < distances.get(neighbor)){

                    distances.set(neighbor, toDistance + distances.get(nextJunction));
                    // going from NextJunction to neighbor
                    backtracking.put(neighbor, nextJunction);
                }
            }
            // Select unvisited node with the least distance
            Integer potentialNJ = null;
            double minDist = inf;

            for(Integer v: unvisitedJunctions){
                if (distances.get(v) < minDist){
                    minDist = distances.get(v);
                    potentialNJ = v;
                }
            }
            if(potentialNJ != null){         // no possible route found
                nextJunction = potentialNJ;
            } else{
                Log.d("Routing", "returning null");
                return null;
            }
        }


        List<Node> route = new ArrayList<>(); // start, junctions/waypoints, end

        // Backtracking the waypoints, starting from EndPoint
        route.add(0, new Node(endingPoint, 0.0));

        // IF nearest junction involves backtracking:
        Integer current = nearestJunction(endingPoint);
        Integer parentNode = backtracking.get(current);

        // No waypoint needed

        if(getStartDist(nearestJunction(startingPoint)) > getStartToEndDist() ||
                getEndDist(nearestJunction(endingPoint)) > getStartToEndDist()){
            Log.d("Routing", "No waypoint - start to end: "+ getStartToEndDist());
            route.add(0, new Node(startingPoint, getStartToEndBearing()));
            return route;
        }
        // Omit the last waypoint -> from 2nd last waypoint to endPoint
        else if(parentNode != null && getDistance(parentNode, current) > getEndDist(parentNode)) {
            Log.d("Routing", "Omitting the last waypoint");
            current = parentNode;
            parentNode = backtracking.get(current);
        }

        route.add(0, new Node(junctions.get(current), getEndBearing(current)));

        while(true){
            if(parentNode == null || (backtracking.get(parentNode) == null && getStartDist(current) < getDistance(parentNode, current))){
                route.add(0, new Node(startingPoint, getStartBearing(current)));
                Log.d("Routing", "add starting");
                return route;
            }
            else{
                route.add(0, new Node(junctions.get(parentNode), getBearing(parentNode, current)));
            }
            current = parentNode;
            parentNode = backtracking.get(current);
        }
    }

    private Integer nearestJunction(LatLng startingPoint){
        double shortestDist = 1000;
        Integer nearest = null;
        for(Integer junction: junctions.keySet()){
            if(calculate_distance(startingPoint, junctions.get(junction)) < shortestDist){
                shortestDist = calculate_distance(startingPoint, junctions.get(junction));
                nearest = junction;
            }
        }
        return nearest;
    }

    private Double calculate_bearing(LatLng point1, LatLng point2){
        // In radians
        Double a1 = Math.toRadians(point1.getLatitude());
        Double a2 = Math.toRadians(point2.getLatitude());
        Double b1 = Math.toRadians(point1.getLongitude());
        Double b2 = Math.toRadians(point2.getLongitude());

        Double y = Math.sin(b2-b1) * Math.cos(a2);
        Double x = Math.cos(a1)* Math.sin(a2) -
                Math.sin(a1) * Math.cos(a2) * Math.cos(b2-b1);
        return Math.toDegrees(Math.atan2(y,x));
    }

    private double getStartDist(Integer point){
        return calculate_distance(start, junctions.get(point));
    }

    private double getStartToEndDist(){
        return calculate_distance(start, end);
    }

    private double getEndDist(Integer point){
        return calculate_distance(junctions.get(point), end);
    }

    private double getBearing(Integer parent, Integer child){
        return calculate_bearing(junctions.get(parent), junctions.get(child));
    }
    private double getStartBearing(Integer point){
        return calculate_bearing(start, junctions.get(point));
    }
    private double getEndBearing(Integer point){
        return calculate_bearing(junctions.get(point), end);
    }
    private double getStartToEndBearing(){
        return calculate_bearing(start, end);
    }
    private double getDistance(Integer parent, Integer child){
        return calculate_distance(junctions.get(parent), junctions.get(child));
    }
}
