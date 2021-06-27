package com.example.geoindoor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class agendaActivity extends AppCompatActivity {

    Button viewButton, createButton, zoomButton, textSpeechButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agenda);
        getSupportActionBar().setTitle(R.string.agenda);


//view agenda page
        viewButton = (Button) findViewById(R.id.viewButton);

        viewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(agendaActivity.this, viewAgendaActivity.class);
                intent.putExtra("userMobileNumber", getIntent().getStringExtra("userMobileNumber"));
                startActivity(intent);            }
        });


//create agenda page
        createButton = (Button) findViewById(R.id.createButton);

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(agendaActivity.this, CreateAgendaActivity.class);
                intent.putExtra("userMobileNumber", getIntent().getStringExtra("userMobileNumber"));
                startActivity(intent);
            }
        });

    }
}