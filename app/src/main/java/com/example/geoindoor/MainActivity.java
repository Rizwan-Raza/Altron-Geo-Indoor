package com.example.geoindoor;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle(R.string.main_form);
       /* setContentView(R.layout.activity_login_form);
        getSupportActionBar().setTitle("Login Form");*/
    }
}