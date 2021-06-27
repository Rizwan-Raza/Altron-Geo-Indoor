package com.example.geoindoor;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.geoindoor.models.Node;
import com.example.geoindoor.models.Places;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.mapbox.mapboxsdk.style.expressions.Expression.exponential;
import static com.mapbox.mapboxsdk.style.expressions.Expression.interpolate;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.expressions.Expression.zoom;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

public class IndoorNavigation extends AppCompatActivity implements OnMapReadyCallback {
    //options for spinner
    List<String> rooms = new ArrayList<>();
    //List for Dynamic Building Mapping
    List<Places> nameList;
    List<Places> markerList;
    List<Places> perimeterList;
    private Routing mapRouting;
    private MapView mapView;
    private MapboxMap mapbox;
    private GeoJsonSource building217Source;
    private GeoJsonSource building213Source;
    private GeoJsonSource building80Source;
    private LatLng startCoord, destinationCoord;
    private Icon currentLocation;
    private HashMap<Integer, List<LatLng>> routePolylines;
    private HashMap<Integer, Polyline> polylines;
    private final int floor = 0;
    private String buildingName;
    private String str;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        buildingName = intent.getStringExtra("BuildingName");
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        if (buildingName.equals("building213")) {
            setContentView(R.layout.activity_indoor_navigation213);
        } else if (buildingName.equals("building217")) {
            setContentView(R.layout.activity_indoor_navigation217);
        } else
            setContentView(R.layout.activity_indoor_navigation80);

        getSupportActionBar().setTitle(R.string.indoor_navigation);

        nameList = loadNamesFromGeoJson(buildingName);
        markerList = loadMarkersFromGeoJson(buildingName);
        perimeterList = loadPerimeterFromGeoJson(buildingName);
        loadRoomsList();

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        mapRouting = new Routing(this, buildingName, perimeterList);
        //spinner
        Spinner spin = findViewById(R.id.spinner1);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, rooms);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(adapter);
        spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                for (int i = 0; i < rooms.size(); i++) {
                    ((TextView) parent.getChildAt(0)).setTextColor(Color.parseColor("#FF03A9F4"));
                    ((TextView) parent.getChildAt(0)).setTextSize(14);
                }
                str = spin.getSelectedItem().toString();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        //


        Button route_button = findViewById(R.id.start_route_button);
        route_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDestinationCoord();
                if (destinationCoord != null) {
                    HashMap<Integer, List<Node>> drawNodes = mapRouting.getRoute(startCoord, destinationCoord);
                    buildRoute(drawNodes);
                }
            }
        });
    }

    private void setDestinationCoord() {
        for (int i = 0; i < nameList.size(); i++) {
            if (nameList.get(i).getPlace_name().toLowerCase().equals(str.toLowerCase())) {
                destinationCoord = nameList.get(i).getCoordinate();
            }
        }
    }

    private void removeRoute(int level) {
        if (mapbox.getPolylines().size() > 0 && polylines.get(level) != null) {
            mapbox.removePolyline(polylines.get(level));
        }
    }


    private void removeAllRoutes() {
        if (polylines == null || polylines.isEmpty()) {
            return;
        }
        for (Integer level : polylines.keySet()) {
            removeRoute(level);
        }
    }


    private void buildRoute(HashMap<Integer, List<Node>> waypoints) {
        if (waypoints == null || waypoints.size() <= 0) return;
        removeAllRoutes();
        routePolylines = new HashMap<>();
        polylines = new HashMap<>();
        for (Integer level : waypoints.keySet()) {
            List<LatLng> routePolyline = new ArrayList<>();
            for (Node node : waypoints.get(level)) {
                routePolyline.add(node.coordinate);
            }
            routePolylines.put(level, routePolyline);
        }
        refreshLevel();
    }

    private void refreshLevel() {
        //refreshMarkers();
        for (Polyline line : mapbox.getPolylines()) {
            mapbox.removePolyline(line);
        }
        if (routePolylines != null) {
            for (Integer level : routePolylines.keySet()) {
                if (floor == level) {
                    drawRoute(level);
                }
            }
        }
    }

    private void drawRoute(int level) {
        List<LatLng> routePolyline = routePolylines.get(level);
        PolylineOptions polylineOptions = new PolylineOptions()
                .addAll(routePolyline)
                .color(Color.GREEN)
                .width(3f);
        Polyline polyline = mapbox.addPolyline(polylineOptions);
        polylines.put(level, polyline);
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {

        mapbox = mapboxMap;
        if (buildingName.equals("building217")) {
            building217Source = new GeoJsonSource("building217", loadJsonFromAsset(buildingName + ".geojson"));
            mapboxMap.addSource(building217Source);
            loadBuildingLayer(buildingName);
        } else if (buildingName.equals("building213")) {
            building213Source = new GeoJsonSource("building213", loadJsonFromAsset(buildingName + ".geojson"));
            mapboxMap.addSource(building213Source);
            loadBuildingLayer(buildingName);
        } else {
            building80Source = new GeoJsonSource("building80", loadJsonFromAsset(buildingName + ".geojson"));
            mapboxMap.addSource(building80Source);
            loadBuildingLayer(buildingName);
        }
        setMarkers();
    }

    private void loadRoomsList() {
        for (int i = 0; i < nameList.size(); i++) {
            if (nameList.get(i).getPlace_name().toLowerCase().equals("entrance")) {
                startCoord = nameList.get(i).getCoordinate();
            } else {
                rooms.add(nameList.get(i).getPlace_name());
            }
        }
    }

    private void setMarkers() {
        for (int i = 0; i < markerList.size(); i++) {
            mapbox.addMarker(new MarkerOptions().position(markerList.get(i).getCoordinate()));
        }
        currentLocation = IconFactory.getInstance(IndoorNavigation.this).fromResource(R.drawable.current_location);
        mapbox.addMarker(new MarkerOptions().position(startCoord).setTitle(getString(R.string.current_location)).snippet(getString(R.string.youAreHere)).setIcon(currentLocation));
    }

    private List<Places> loadPerimeterFromGeoJson(String buildingName) {
        FeatureCollection featureCollection;
        try {
            String filename = buildingName + ".geojson";
            featureCollection = FeatureCollection.fromJson(loadJsonFromAsset(filename));
        } catch (Exception e) {
            return null;
        }
        List<Feature> featureList = featureCollection.features();
        List<Places> placesList = new ArrayList<>();
        for (int i = 0; i < featureList.size(); i++) {
            Feature singleLocation = featureList.get(i);
            if (singleLocation.hasProperty("perimeter")) {
                String name = singleLocation.getStringProperty("perimeter");
                Double stringLng = ((Point) singleLocation.geometry()).coordinates().get(0);
                Double stringLat = ((Point) singleLocation.geometry()).coordinates().get(1);
                LatLng locationLatLng = new LatLng(stringLat, stringLng);

                placesList.add(new Places(name, locationLatLng));
            }
        }
        return placesList;
    }

    private List<Places> loadMarkersFromGeoJson(String buildingName) {
        FeatureCollection featureCollection;
        try {
            String filename = buildingName + ".geojson";
            featureCollection = FeatureCollection.fromJson(loadJsonFromAsset(filename));
        } catch (Exception e) {
            return null;
        }
        List<Feature> featureList = featureCollection.features();
        List<Places> placesList = new ArrayList<>();
        for (int i = 0; i < featureList.size(); i++) {
            Feature singleLocation = featureList.get(i);
            if (singleLocation.hasProperty("marker")) {
                String name = singleLocation.getStringProperty("marker");
                Double stringLng = ((Point) singleLocation.geometry()).coordinates().get(0);
                Double stringLat = ((Point) singleLocation.geometry()).coordinates().get(1);
                LatLng locationLatLng = new LatLng(stringLat, stringLng);

                placesList.add(new Places(name, locationLatLng));
            }
        }
        return placesList;
    }

    public List<Places> loadNamesFromGeoJson(String buildingName) {
        FeatureCollection featureCollection;
        try {
            String filename = buildingName + ".geojson";
            featureCollection = FeatureCollection.fromJson(loadJsonFromAsset(filename));
        } catch (Exception e) {
            return null;
        }
        List<Feature> featureList = featureCollection.features();
        List<Places> placesList = new ArrayList<>();
        for (int i = 0; i < featureList.size(); i++) {
            Feature singleLocation = featureList.get(i);
            if (singleLocation.hasProperty("name")) {
                String name = singleLocation.getStringProperty("name");
                Double stringLng = ((Point) singleLocation.geometry()).coordinates().get(0);
                Double stringLat = ((Point) singleLocation.geometry()).coordinates().get(1);
                LatLng locationLatLng = new LatLng(stringLat, stringLng);
                placesList.add(new Places(name, locationLatLng));
            }
        }
        return placesList;
    }

    private String loadJsonFromAsset(String filename) {
        try {
            InputStream is = getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            return new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void loadBuildingLayer(String buildingName) {
        FillLayer indoorBuildingLayer = new FillLayer("building -fill", buildingName).withProperties(
                fillColor(Color.parseColor("#eeeeee")), fillOpacity(interpolate(exponential(1f), zoom(),
                        stop(17f, 1f),
                        stop(16.5f, 0.5f),
                        stop(16f, 0f))));

        mapbox.addLayer(indoorBuildingLayer);

        LineLayer indoorBuildingLineLayer = new LineLayer("indoor-building-line", buildingName)
                .withProperties(lineColor(Color.parseColor("#50667f")),
                        lineWidth(0.5f),
                        lineOpacity(interpolate(exponential(1f), zoom(),
                                stop(17f, 1f),
                                stop(16.5f, 0.5f),
                                stop(16f, 0f))));
        mapbox.addLayer(indoorBuildingLineLayer);
    }
}