package com.example.geoindoor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class RegisterForm_2 extends AppCompatActivity {
    TextInputLayout u_email, u_password, u_mobile;
    String full_name = "";
    String last_name = "";
    String d_of_birth = "";
    String profile_picture = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_form_2);
        getSupportActionBar().setTitle(R.string.register);

        u_email = findViewById(R.id.email);
        u_password = findViewById(R.id.password);
        u_mobile = findViewById(R.id.mobile_number);

        Intent intent = getIntent();
        full_name = intent.getStringExtra("fullName");
        last_name = intent.getStringExtra("lastName");
        d_of_birth = intent.getStringExtra("date_of_birth");
        profile_picture = intent.getStringExtra("profilePicture");
    }


    public void btn_details_registration(View view) {
        if (validateEmail() && validatePassword() && validateMobile()) {
            Query checkUserMobile = FirebaseDatabase.getInstance().getReference("users").orderByChild("mobile").equalTo(String.valueOf(u_mobile.getEditText().getText().toString().trim()));
            checkUserMobile.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        toastMsg(getString(R.string.mobile_failure));
                    } else {
                        //startActivity(new Intent(getApplicationContext(), RegisterForm_3.class));
                        Intent intent = new Intent(getApplicationContext(), RegisterForm_3.class);
                        intent.putExtra("fullName", full_name);
                        intent.putExtra("lastName", last_name);
                        intent.putExtra("date_of_birth", d_of_birth);
                        intent.putExtra("userEmail", u_email.getEditText().getText().toString().trim());
                        intent.putExtra("userPassword", u_password.getEditText().getText().toString().trim());
                        intent.putExtra("userMobile", u_mobile.getEditText().getText().toString().trim());
                        intent.putExtra("profilePicture", profile_picture.trim());
                        startActivity(intent);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    toastMsg(error.getMessage());
                }
            });


        }
    }

    private boolean validateEmail() {
        String email = u_email.getEditText().getText().toString().trim();
        String emailRegex = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        if (email.isEmpty()) {
            u_email.setError(getString(R.string.please_email));
            return false;
        } else if (!email.matches(emailRegex)) {
            u_email.setError(getString(R.string.please_valid_email));
            return false;
        } else {
            u_email.setError(null);
            u_email.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validatePassword() {
        String password = u_password.getEditText().getText().toString().trim();
        if (password.isEmpty()) {
            u_password.setError(getString(R.string.please_enter_password));
            return false;
        } else if (password.length() < 8) {
            u_password.setError(getString(R.string.please_enter_pass_8));
            return false;
        } else {
            u_password.setError(null);
            u_password.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validateMobile() {
        String mobile = u_mobile.getEditText().getText().toString().trim();
        String regex = "[0-9]+";

        if (mobile.isEmpty()) {
            u_mobile.setError(getString(R.string.please_mobile));
            return false;
        } else if (mobile.length() != 10 || !mobile.matches(regex)) {
            u_mobile.setError(getString(R.string.please_valid_mobile));
            return false;
        } else {
            u_mobile.setError(null);
            u_mobile.setErrorEnabled(false);
            return true;
        }
    }


    public void toastMsg(String msg) {
        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        toast.show();
    }
}