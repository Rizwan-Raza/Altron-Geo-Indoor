package com.example.geoindoor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterForm_3 extends AppCompatActivity {

    String full_name = "";
    String last_name = "";
    String d_of_birth = "";

    String email_user = "";
    String pass_user = "";
    String mobile_user = "";
    String radio_text = "";
    String profile_picture = "";
    RadioGroup radioGroup;
    RadioButton radioButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_form_3);
        getSupportActionBar().setTitle(R.string.register);

        Intent intent = getIntent();
        full_name = intent.getStringExtra("fullName");
        last_name = intent.getStringExtra("lastName");
        d_of_birth = intent.getStringExtra("date_of_birth");
        email_user = intent.getStringExtra("userEmail");
        pass_user = intent.getStringExtra("userPassword");
        mobile_user = intent.getStringExtra("userMobile");
        profile_picture = intent.getStringExtra("profilePicture");
    }

    public void btn_disability_registration(View view) {
        //startActivity(new Intent(getApplicationContext(),RegisterForm_4.class));
        if (validateMobilityOption()) {

            Intent intent = new Intent(getApplicationContext(), RegisterForm_4.class);
            intent.putExtra("fullName", full_name);
            intent.putExtra("lastName", last_name);
            intent.putExtra("date_of_birth", d_of_birth);
            intent.putExtra("userEmail", email_user);
            intent.putExtra("userPassword", pass_user);
            intent.putExtra("userMobile", mobile_user);
            intent.putExtra("radio_btn_txt", radio_text);
            intent.putExtra("profilePicture", profile_picture.trim());
            startActivity(intent);
        } else {
            toastMsg(getString(R.string.please_select_option));
        }
    }


    private boolean validateMobilityOption() {
        radioGroup = (RadioGroup) findViewById(R.id.radio_group_mobility);
        int selectedRadioId = radioGroup.getCheckedRadioButtonId();
        if (selectedRadioId == -1) {
            return false;
        } else {
            radioButton = (RadioButton) findViewById(selectedRadioId);
            radio_text = radioButton.getText().toString();
            return true;
        }
    }

    public void toastMsg(String msg) {
        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        toast.show();
    }
}