package com.example.geoindoor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.geoindoor.models.Agenda;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class detailActivity extends AppCompatActivity {

    private static final String TAG = "DetailAgendaActivity";
    TextInputLayout name2, type2, address2, person2, contact2, notes2, date2, time2;
    String name1, address1, person1, contact1, notes1, date1, time1;
    int type1, count;
    String agendaID, mobile;
    Button viewButton;
    FirebaseDatabase geoIndoor = FirebaseDatabase.getInstance();
    DatabaseReference reference = geoIndoor.getReference("agendas");

    Map<String, Integer> agendaTypes = new HashMap<String, Integer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Intent intent = getIntent();
        count = intent.getIntExtra("count", 0);
        mobile = intent.getStringExtra("mobileNumber");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        getSupportActionBar().setTitle(R.string.detail_agenda);

        agendaTypes.put("Appointment", R.string.appointment);
        agendaTypes.put("Bakery", R.string.bakery);
        agendaTypes.put("Get Together", R.string.get_together);
        agendaTypes.put("Hospital", R.string.hospital);
        agendaTypes.put("Meeting", R.string.meeting);
        agendaTypes.put("Social Event", R.string.social_event);
        agendaTypes.put("Walk", R.string.walk);
        agendaTypes.put("Other", R.string.other);

        name2 = findViewById(R.id.firstName);
        type2 = findViewById(R.id.lastName);
        address2 = findViewById(R.id.dob);
        person2 = findViewById(R.id.mobile);
        contact2 = findViewById(R.id.mobility);
        notes2 = findViewById(R.id.notes);
        date2 = findViewById(R.id.emailAdd);
        time2 = findViewById(R.id.disability);


        ArrayList<Agenda> agendas = this.getIntent().getParcelableArrayListExtra("key");
        name1 = agendas.get(count).getName();
        type1 = getTranslated(agendas.get(count).getType());
        address1 = agendas.get(count).getAddress();
        person1 = agendas.get(count).getPersonToMeet();
        contact1 = agendas.get(count).getContactNumber();
        notes1 = agendas.get(count).getNotes();
        date1 = agendas.get(count).getDate();
        time1 = agendas.get(count).getTime();

        agendaID = agendas.get(count).get_agendaId();

        name2.getEditText().setText(name1);
        type2.getEditText().setText(type1);
        address2.getEditText().setText(address1);
        person2.getEditText().setText(person1);
        contact2.getEditText().setText(contact1);
        notes2.getEditText().setText(notes1);
        date2.getEditText().setText(date1);
        time2.getEditText().setText(time1);

        //change
        viewButton = findViewById(R.id.completeButton);

        viewButton.setOnClickListener(view -> reference.child(agendaID).child("status").setValue(true).addOnSuccessListener(aVoid -> {
            toastMsg(getString(R.string.agenda_complete_success));
            Intent data = new Intent();
            data.putExtra("delete", true);
            data.putExtra("key", count);
            setResult(RESULT_OK, data);
            finish();

        }).addOnFailureListener(e -> {
            toastMsg(getString(R.string.server_error));
        }));

    }

    private int getTranslated(String type) {
        return agendaTypes.get(type);
    }


//    public void update(View view) {
//        if(agendaNameChanged() || agendaTypeChanged() || addressChanged() ||
//                personToMeetChanged() || dateChanged()|| timeChanged() || contactNumberChanged() || notesChanged()){
//            Toast.makeText(this, "Agenda Details have been updated", Toast.LENGTH_LONG).show();
//        }else
//            Toast.makeText(this, "Agenda Details are same and cannot be updated", Toast.LENGTH_LONG).show();
//
//
//    }
//
//    private boolean notesChanged() {
//        if(!notes1.equals(notes2.getEditText().getText().toString())){
//            reference.child(agendaID).child("notes").setValue(notes2.getEditText().getText().toString());
//            return true;
//        }else{
//            return false;
//        }
//    }
//
//    private boolean contactNumberChanged() {
//        if(!contact1.equals(contact2.getEditText().getText().toString())){
//            reference.child(agendaID).child("contactNumber").setValue(contact2.getEditText().getText().toString());
//            return true;
//        }else{
//            return false;
//        }
//    }
//
//    private boolean timeChanged() {
//        if(!time1.equals(time2.getEditText().getText().toString())){
//            reference.child(agendaID).child("time").setValue(time2.getEditText().getText().toString());
//            return true;
//        }else{
//            return false;
//        }
//    }
//
//    private boolean dateChanged() {
//        if(!date1.equals(date2.getEditText().getText().toString())){
//            reference.child(agendaID).child("date").setValue(date2.getEditText().getText().toString());
//            return true;
//        }else{
//            return false;
//        }
//    }
//
//    private boolean personToMeetChanged() {
//        if(!person1.equals(person2.getEditText().getText().toString())){
//            reference.child(agendaID).child("personToMeet").setValue(person2.getEditText().getText().toString());
//            return true;
//        }else{
//            return false;
//        }
//    }
//
//    private boolean addressChanged() {
//        if(!address1.equals(address2.getEditText().getText().toString())){
//            reference.child(agendaID).child("address").setValue(address2.getEditText().getText().toString());
//            return true;
//        }else{
//            return false;
//        }
//    }
//
//    private boolean agendaTypeChanged() {
//        if(!notes1.equals(notes2.getEditText().getText().toString())){
//            reference.child(agendaID).child("notes").setValue(notes2.getEditText().getText().toString());
//            return true;
//        }else{
//            return false;
//        }
//    }
//
//    private boolean agendaNameChanged() {
//        if(!notes1.equals(notes2.getEditText().getText().toString())){
//            reference.child(agendaID).child("notes").setValue(notes2.getEditText().getText().toString());
//            return true;
//        }else{
//            return false;
//        }
//    }

    public void delete(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(detailActivity.this)
                .setTitle(R.string.delete_agenda)
                .setMessage(R.string.delete_agenda_warn).setPositiveButton(R.string.delete, (dialogInterface, i) ->
                        reference.child(agendaID).removeValue().addOnSuccessListener(bVoid -> {
                            toastMsg(getString(R.string.agenda_delete_success));
                            Intent data = new Intent();
                            data.putExtra("delete", true);
                            data.putExtra("key", count);
                            setResult(RESULT_OK, data);
                            finish();
                        }).addOnFailureListener(e ->
                                toastMsg(getString(R.string.server_error))
                        )).setNegativeButton(R.string.no, (dialogInterface, i) -> {
                });

        AlertDialog alert = builder.create();
        alert.show();

    }

    public void toastMsg(String msg) {
        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        toast.show();
    }
}