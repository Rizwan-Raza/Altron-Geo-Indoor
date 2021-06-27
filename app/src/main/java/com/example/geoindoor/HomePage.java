package com.example.geoindoor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class HomePage extends AppCompatActivity {

    Button profileButton, agendaButton, navigationButton, zoomButton, textSpeechButton;
    TextView greetingMessage;

    String userId = "";

    private float mScale = 1f;
    private ScaleGestureDetector mScaleDetector;
    GestureDetector gestureDetector;

    ConstraintLayout constraintLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        getSupportActionBar().setTitle(R.string.home_page);

        agendaButton = (Button) findViewById(R.id.agendaButton);

        Intent intent = getIntent();
        userId = intent.getStringExtra("userMobileNumber");

        Query fetchUserDetails = FirebaseDatabase.getInstance().getReference("users").orderByChild("mobile").equalTo(userId);

        fetchUserDetails.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    greetingMessage = findViewById(R.id.firstName);
                    String message = snapshot.child(userId).child("firstname").getValue(String.class);
                    System.out.println(message);
                    greetingMessage.setText(message);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                toastMsg(error.getMessage());
            }
        });

        greetingMessage = findViewById(R.id.hello);
        greetingMessage.setText(R.string.hello_sp);

        agendaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomePage.this, agendaActivity.class);
                intent.putExtra("userMobileNumber", userId);
                startActivity(intent);
            }
        });

        gestureDetector = new GestureDetector(this, new GestureListener());
        mScaleDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener(){
            @Override
            public boolean onScale(ScaleGestureDetector detector){
                float scale = 1-detector.getScaleFactor();

                float prevScale = mScale;
                mScale += scale;

                if(mScale<0.1f)
                    mScale = 0.1f;

                if(mScale>10f)
                    mScale=10f;

                ScaleAnimation scaleAnimation = new ScaleAnimation(1f/prevScale,1f/mScale,1f/prevScale,1f/mScale, detector.getFocusX(),detector.getFocusY());
                scaleAnimation.setDuration(0);
                scaleAnimation.setFillAfter(true);
                constraintLayout = (ConstraintLayout) findViewById(R.id.constrainLayout);
                constraintLayout.startAnimation(scaleAnimation);

                return true;
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev){
        super.dispatchTouchEvent(ev);
        mScaleDetector.onTouchEvent(ev);
        gestureDetector.onTouchEvent(ev);
        return gestureDetector.onTouchEvent(ev);
    }

    public void btn_profile(View view) {
        Intent intent = new Intent(getApplicationContext(), viewProfile.class);
        intent.putExtra("userMobileNumber",userId);
        startActivity(intent);
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onDown(MotionEvent e){
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e){
            return true;
        }
    }

    public void btn_navigation(View view) {
        Intent intent = new Intent(getApplicationContext(), Navigation.class);
        intent.putExtra("userMobileNumber",userId);
        startActivity(intent);
    }

    public void toastMsg(String msg) {
        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        toast.show();
    }
}