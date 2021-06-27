package com.example.geoindoor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.geoindoor.models.Users;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class viewProfile extends AppCompatActivity {

    private static final String TAG = "DetailAgendaActivity";
    TextInputLayout firstName, lastName, dob, mobile, emailAdd, disabilty, mobility;
    ImageView imageView;
    String firstName1, lastName1, dob1, mobile1, emailAdd1, disability1, mobility1, image;
    FirebaseDatabase geoIndoor = FirebaseDatabase.getInstance();
    DatabaseReference reference = geoIndoor.getReference("users");

    Spinner s;
    String str;
    String[] mobilityTypes;

    String mobileNumber;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Intent intent = getIntent();
        mobileNumber = intent.getStringExtra("userMobileNumber");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);
        getSupportActionBar().setTitle(R.string.user_profile);

        firstName = findViewById(R.id.firstName);
        lastName = findViewById(R.id.lastName);
        dob = findViewById(R.id.dob);
        mobile = findViewById(R.id.mobile);
        mobility = findViewById(R.id.mobility);
        emailAdd = findViewById(R.id.emailAdd);
        disabilty = findViewById(R.id.disability);
        imageView = findViewById(R.id.imageView2);

        mobilityTypes = getResources().getStringArray(R.array.mobility_types);//        String[] arraySpinner = new String[] {
//                "Select your mobility","Walker", "Wheelchair", "None"
//        };

        s = findViewById(R.id.spinner2);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, mobilityTypes) {
            @Override
            public boolean isEnabled(int position) {
                // Disable the first item from Spinner
                // First item will be use for hint
                return position != 0;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(adapter);
        s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                str = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        reference.orderByChild("mobile").equalTo(mobileNumber).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot dataSnapshot1 : snapshot.getChildren()) {
                    Users users = dataSnapshot1.getValue(Users.class);

                    firstName1 = users.getFirstname();
                    System.out.println("what is this : " + firstName1);

                    lastName1 = users.getLastname();
                    dob1 = users.getDateOfBirth();
                    mobile1 = users.getMobile();
                    emailAdd1 = users.getEmail();
                    disability1 = users.getDisability();
                    image = users.getProfilePic();
                    // System.out.println("what is the mobility 22 " +str);


                    firstName.getEditText().setText(firstName1);
                    lastName.getEditText().setText(lastName1);
                    dob.getEditText().setText(dob1);
                    mobile.getEditText().setText(mobile1);
                    emailAdd.getEditText().setText(emailAdd1);
                    disabilty.getEditText().setText(disability1);
                    Picasso.get().load(image).into(imageView);
                }
                mobility1 = snapshot.child(mobileNumber).child("mobility").getValue(String.class);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }


    public void update(View view) {

        if (firstNameChanged() || lastNameChanged() || dobChanged() ||
                emailChanged() || disabilityChanged() || mobilityChanged()) {
            Toast.makeText(this, R.string.user_update_success, Toast.LENGTH_LONG).show();

//            Intent intent = new Intent(viewProfile.this, HomePage.class);
//            intent.putExtra("userMobileNumber", getIntent().getStringExtra("userMobileNumber"));
//            startActivity(intent);
            finish();
        } else
            Toast.makeText(this, R.string.user_update_failure, Toast.LENGTH_LONG).show();

    }

    private boolean mobilityChanged() {
        if (!mobility1.equals(str)) {
            reference.child(mobileNumber).child("mobility").setValue(str);
            return true;
        } else {
            return false;
        }

    }

    private boolean disabilityChanged() {
        if (!disability1.equals(disabilty.getEditText().getText().toString())) {
            reference.child(mobileNumber).child("disability").setValue(disabilty.getEditText().getText().toString());
            return true;
        } else {
            return false;
        }
    }

    private boolean emailChanged() {
        if (!emailAdd1.equals(emailAdd.getEditText().getText().toString())) {
            reference.child(mobileNumber).child("email").setValue(emailAdd.getEditText().getText().toString());
            return true;
        } else {
            return false;
        }
    }

    private boolean dobChanged() {
        if (!dob1.equals(dob.getEditText().getText().toString())) {
            reference.child(mobileNumber).child("dateOfBirth").setValue(dob.getEditText().getText().toString());
            return true;
        } else {
            return false;
        }
    }

    private boolean lastNameChanged() {
        if (!lastName1.equals(lastName.getEditText().getText().toString())) {
            reference.child(mobileNumber).child("lastname").setValue(lastName.getEditText().getText().toString());
            return true;
        } else {
            return false;
        }
    }

    private boolean firstNameChanged() {

        if (!firstName1.equals(firstName.getEditText().getText().toString())) {
            reference.child(mobileNumber).child("firstname").setValue(firstName.getEditText().getText().toString());
            return true;
        } else {
            return false;
        }
    }


}