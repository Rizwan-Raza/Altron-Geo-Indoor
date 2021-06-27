package com.example.geoindoor;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.geoindoor.models.Users;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterForm_4 extends AppCompatActivity {

    String full_name, last_name, d_of_birth, email, password, mobile, mobility, profile_picture;
    //TextInputLayout disability;
    CheckBox visual, speech, hearing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_form_4);
        getSupportActionBar().setTitle(R.string.register);

        //disability = findViewById(R.id.disability);
        visual = (CheckBox) findViewById(R.id.checkBox);
        hearing = (CheckBox) findViewById(R.id.checkBox2);
        speech = (CheckBox) findViewById(R.id.checkBox3);

        Intent intent = getIntent();
        full_name = intent.getStringExtra("fullName");
        last_name = intent.getStringExtra("lastName");
        d_of_birth = intent.getStringExtra("date_of_birth");
        email = intent.getStringExtra("userEmail");
        password = intent.getStringExtra("userPassword");
        mobile = intent.getStringExtra("userMobile");
        mobility = intent.getStringExtra("radio_btn_txt");
        profile_picture = intent.getStringExtra("profilePicture");


    }

    public void btn_login(View view) {
        FirebaseDatabase geoIndoor = FirebaseDatabase.getInstance();
        DatabaseReference reference = geoIndoor.getReference("users");
        String r = "";
        if (visual.isChecked()) {
            r = r + "," + visual.getText();
        }
        if (hearing.isChecked()) {
            r = r + "," + hearing.getText();
        }
        if (speech.isChecked()) {
            r = r + "," + speech.getText();
        } else {
            r = getString(R.string.none);
        }

        Users newUser = new Users(full_name, last_name, d_of_birth, password, email, mobile, mobility, r.trim(), profile_picture.trim());

        reference.child(mobile).setValue(newUser, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if (error == null) {
                    toastMsg(getString(R.string.register_success));
                } else {
                    toastMsg(getString(R.string.register_failure));
                }
            }
        });
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(getApplicationContext(), LoginForm.class));
            }
        }, 4000);
    }

    public void toastMsg(String msg) {
        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        toast.show();
    }
}