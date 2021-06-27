package com.example.geoindoor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;


public class Navigation extends AppCompatActivity {
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        getSupportActionBar().setTitle(R.string.navigation);

        Intent intent = getIntent();
        userId = intent.getStringExtra("userMobileNumber");
    }

    public void btn_whereAmI(View view) {
        startActivity(new Intent(getApplicationContext(),CurrentLocation.class));
    }

    public void btn_viewMyAgendasOnMap(View view) {
        Intent intent = new Intent(getApplicationContext(), ViewMyAgendasOnMap.class);
        intent.putExtra("userMobileNumber",userId);
        startActivity(intent);
    }

    public void btn_indoorNavigation(View view) {
        startActivity(new Intent(getApplicationContext(), SelectBuilding.class));
    }
}