package com.example.geoindoor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class LoginForm extends AppCompatActivity {

    public static final String PREFS_NAME = "MyPrefsFile";
    public Button button;
    public String PREFS_PHONE = "prefsPhone";
    public String PREFS_PASSWORD = "prefsPassword";
    TextInputLayout mobileNum, usrPassword;
    CheckBox rememberMe;
    GestureDetector gestureDetector;
    TextInputEditText mobileEditText, passEditText;
    ScrollView scrollView;
    private float mScale = 1f;
    private ScaleGestureDetector mScaleDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_form);
        getSupportActionBar().setTitle(R.string.login);
        mobileNum = findViewById(R.id.mobileNumber);
        usrPassword = findViewById(R.id.pass);
        rememberMe = findViewById(R.id.remember);

        gestureDetector = new GestureDetector(this, new GestureListener());
        mScaleDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float scale = 1 - detector.getScaleFactor();

                float prevScale = mScale;
                mScale += scale;

                if (mScale < 0.1f)
                    mScale = 0.1f;

                if (mScale > 10f)
                    mScale = 10f;

                ScaleAnimation scaleAnimation = new ScaleAnimation(1f / prevScale, 1f / mScale, 1f / prevScale, 1f / mScale, detector.getFocusX(), detector.getFocusY());
                scaleAnimation.setDuration(0);
                scaleAnimation.setFillAfter(true);
                scrollView = (ScrollView) findViewById(R.id.scrollView);
                scrollView.startAnimation(scaleAnimation);

                return true;
            }
        });

        SharedPreferences pref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String uphone = pref.getString(PREFS_PHONE, "");
        String upassWord = pref.getString(PREFS_PASSWORD, "");
        System.out.println(uphone);
        System.out.println(upassWord);

        if (uphone != null && upassWord != null) {
            mobileNum.getEditText().setText(uphone);
            usrPassword.getEditText().setText(upassWord);
        }


    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        super.dispatchTouchEvent(ev);
        mScaleDetector.onTouchEvent(ev);
        gestureDetector.onTouchEvent(ev);
        return gestureDetector.onTouchEvent(ev);
    }

    public void btn_signupForm(View view) {
        startActivity(new Intent(getApplicationContext(), RegisterForm.class));
    }

    public void btn_resetForm(View view) {
        startActivity(new Intent(getApplicationContext(), ForgotPassword.class));
    }

    public void btn_homePage(View view) {

        Query checkUserMobile = FirebaseDatabase.getInstance().getReference("users").orderByChild("mobile").equalTo(String.valueOf(mobileNum.getEditText().getText().toString().trim()));
        checkUserMobile.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String password = snapshot.child(mobileNum.getEditText().getText().toString().trim()).child("password").getValue(String.class);
                    if (password.equals(usrPassword.getEditText().getText().toString().trim())) {

                        Intent intent = new Intent(getApplicationContext(), HomePage.class);
                        intent.putExtra("userMobileNumber", String.valueOf(mobileNum.getEditText().getText().toString().trim()));
                        startActivity(intent);
                        if (rememberMe.isChecked()) {
                            SharedPreferences pref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                                    .edit()
                                    .putString(PREFS_PHONE, mobileNum.getEditText().getText().toString().trim())
                                    .putString(PREFS_PASSWORD, usrPassword.getEditText().getText().toString().trim())
                                    .commit();
                        }
                    } else {
                        toastMsg(getString(R.string.invalid_creds));
                    }
                } else {
                    toastMsg(getString(R.string.invalid_creds));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                toastMsg(error.getMessage());
            }
        });


    }

    public void toastMsg(String msg) {
        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        toast.show();
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return true;
        }
    }
}