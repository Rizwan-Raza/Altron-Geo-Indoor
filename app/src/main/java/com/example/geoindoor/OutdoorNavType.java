package com.example.geoindoor;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class OutdoorNavType extends AppCompatActivity {
    Double marker_lat;
    Double marker_lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outdoor_navigation_type);
        getSupportActionBar().setTitle(R.string.nav_mode);

        Intent intent = getIntent();
        marker_lat = Double.parseDouble(intent.getStringExtra("Marker_lat"));
        marker_lng = Double.parseDouble(intent.getStringExtra("Marker_lng"));

        Button carButton = findViewById(R.id.carBtn);
        carButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("google.navigation:q=" + marker_lat + "," + marker_lng);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setPackage("com.google.android.apps.maps");
                startActivity(intent);
            }
        });

        Button bikeButton = findViewById(R.id.bikeBtn);
        bikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("google.navigation:q=" + marker_lat + "," + marker_lng + "&mode=b");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setPackage("com.google.android.apps.maps");
                startActivity(intent);
            }
        });

        Button walkButton = findViewById(R.id.walkBtn);
        walkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("google.navigation:q=" + marker_lat + "," + marker_lng + "&mode=w");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setPackage("com.google.android.apps.maps");
                startActivity(intent);
            }
        });


    }
}
