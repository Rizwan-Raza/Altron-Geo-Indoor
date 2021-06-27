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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class ForgotPassword extends AppCompatActivity {
    DatabaseReference reference;
    TextInputLayout c_password, user_password, uname;
    String password = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        getSupportActionBar().setTitle(R.string.reset_password);


        user_password = findViewById(R.id.new_password);
        c_password = findViewById(R.id.confirm_new_pass);
        uname = findViewById(R.id.userId);

    }

    public void btn_login(View view) {
        startActivity(new Intent(getApplicationContext(), LoginForm.class));
    }

    public void btn_reset(View view) {
        if (validatePassword() && validateConPassword()) {
            Query checkUserMobile = FirebaseDatabase.getInstance().getReference("users").orderByChild("mobile").equalTo(uname.getEditText().getText().toString());
            checkUserMobile.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        reference = FirebaseDatabase.getInstance().getReference("users");
                        reference.child(uname.getEditText().getText().toString()).child("password").setValue(c_password.getEditText().getText().toString());
                        toastMsg(getString(R.string.reset_password_success));
                        startActivity(new Intent(getApplicationContext(), LoginForm.class));
                    } else {
                        toastMsg(getString(R.string.reset_password_failure));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    toastMsg(error.getMessage());
                }
            });
        }
    }


    private boolean validatePassword() {
        password = user_password.getEditText().getText().toString().trim();
        if (password.isEmpty()) {
            user_password.setError(getString(R.string.please_enter_password));
            return false;
        } else if (password.length() < 8) {
            user_password.setError(getString(R.string.please_enter_pass_8));
            return false;
        } else {
            user_password.setError(null);
            user_password.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validateConPassword() {
        String cpassword = c_password.getEditText().getText().toString().trim();
        if (cpassword.isEmpty()) {
            c_password.setError(getString(R.string.please_confirm_password));
            return false;
        } else if (!password.equals(cpassword)) {
            c_password.setError(getString(R.string.password_match_failure));
            return false;
        } else {
            c_password.setError(null);
            c_password.setErrorEnabled(false);
            return true;
        }
    }

    public void toastMsg(String msg) {
        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        toast.show();
    }
}
