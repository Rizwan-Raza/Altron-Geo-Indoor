package com.example.geoindoor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class SelectBuilding extends AppCompatActivity {
    Double marker_lat;
    Double marker_lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selcect_building);
        getSupportActionBar().setTitle(R.string.select_building);
    }

    public void building80(View view) {
        Intent intent = new Intent(getApplicationContext(), IndoorNavigation.class);
        intent.putExtra("BuildingName", "building80");
        startActivity(intent);
    }

    public void building217(View view) {
        Intent intent = new Intent(getApplicationContext(), IndoorNavigation.class);
        intent.putExtra("BuildingName", "building217");
        startActivity(intent);
    }

    public void building213(View view) {
        Intent intent = new Intent(getApplicationContext(), IndoorNavigation.class);
        intent.putExtra("BuildingName", "building213");
        startActivity(intent);
    }
}

