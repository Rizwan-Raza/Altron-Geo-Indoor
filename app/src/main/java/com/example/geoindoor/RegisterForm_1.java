package com.example.geoindoor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.geoindoor.models.Agenda;
import com.google.android.material.textfield.TextInputLayout;
import com.niwattep.materialslidedatepicker.SlideDatePickerDialog;
import com.niwattep.materialslidedatepicker.SlideDatePickerDialogCallback;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class RegisterForm_1 extends AppCompatActivity implements SlideDatePickerDialogCallback {
    private final Agenda agendaModel = new Agenda();
    TextInputLayout dob;
    String full_name = "";
    String last_name = "";
    String profile_picture = "";
    Button button;
    TextView textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_form_1);
        getSupportActionBar().setTitle(R.string.register);


        dob = findViewById(R.id.date_of_birth);
        textView = findViewById(R.id.date_of_birth_edit);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar endDate = Calendar.getInstance();
                int year = Calendar.getInstance().get(Calendar.YEAR);
                endDate.set(Calendar.YEAR, year);
                SlideDatePickerDialog.Builder builder = new SlideDatePickerDialog.Builder();
                builder.setEndDate(endDate);
                SlideDatePickerDialog dialog = builder.build();
                dialog.show(getSupportFragmentManager(), "Dialog");
            }
        });

        Intent intent = getIntent();
        full_name = intent.getStringExtra("fullName");
        last_name = intent.getStringExtra("lastName");
        profile_picture = intent.getStringExtra("profilePicture");
    }

    // date picker
    @Override
    public void onPositiveClick(int date, int month, int year, Calendar calendar) {
        SimpleDateFormat format = new SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault());
        textView.setText(format.format(calendar.getTime()));
    }

    public void btn_mobileNumber_registration(View view) {
        if (validateDOB()) {
            // startActivity(new Intent(getApplicationContext(), RegisterForm_2.class));
            Intent intent = new Intent(getApplicationContext(), RegisterForm_2.class);
            intent.putExtra("fullName", full_name);
            intent.putExtra("lastName", last_name);
            intent.putExtra("profilePicture", profile_picture.trim());
            intent.putExtra("date_of_birth", dob.getEditText().getText().toString().trim());
            startActivity(intent);
        }
    }


    private boolean validateDOB() {
        String dateOfBirth = dob.getEditText().getText().toString().trim();

        if (dateOfBirth.isEmpty()) {
            dob.setError(getString(R.string.please_birth));
            return false;
        } else {
            dob.setError(null);
            dob.setErrorEnabled(false);
            return true;
        }
    }

}