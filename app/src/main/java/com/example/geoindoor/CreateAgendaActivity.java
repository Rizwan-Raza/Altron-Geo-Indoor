package com.example.geoindoor;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.geoindoor.models.Agenda;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CreateAgendaActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener, AdapterView.OnItemSelectedListener {

    private static final Integer[] layouts = {
            R.id.agenda_step1,
            R.id.agenda_step2,
            R.id.agenda_step3,
            R.id.agenda_step4,
            R.id.agenda_loader
    };
    private final Agenda agendaModel = new Agenda();
    TextView errorText;
    Button nextBtn;
    Spinner spinner;
    String[] agendaTypes;
    SpinnerItem[] spinnerItems;
    DatePickerDialog datePickerDialog;
    TimePickerDialog timePickerDialog;
    Calendar mNow = Calendar.getInstance();
    private TextInputEditText agendaP2M, agendaContact, agendaNotes;
    private TextInputLayout agendaNameTIL, agendaDateTIL, agendaTimeTIL, agendaAddressTIL;
    private EditText agendaName, agendaDate, agendaTime, agendaAddress;
    private String agendaKey, keyLeft, keyRight, mobile, timestamp, timeToNotify, dateToNotify, todayDate;
    private int pos = 0, hour, minute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_agenda);
        getSupportActionBar().setTitle(R.string.create_agenda);

        int year = mNow.get(Calendar.YEAR);
        int month = mNow.get(Calendar.MONTH);
        int day = mNow.get(Calendar.DAY_OF_MONTH);
        hour = mNow.get(Calendar.HOUR_OF_DAY);
        minute = mNow.get(Calendar.MINUTE);

        todayDate = (day < 9 ? "0" + day : String.valueOf(day)) + "-" + (month < 9 ? "0" + (month + 1) : String.valueOf(month + 1)) + "-" + year;

//        mNow.set(Calendar.YEAR, year);
//        mNow.set(Calendar.MONTH, month);
//        mNow.set(Calendar.DAY_OF_MONTH, day);
//        mNow.set(Calendar.HOUR_OF_DAY, hour);
//        mNow.set(Calendar.MINUTE, minute);
//        mNow.set(Calendar.SECOND, 0);

        agendaNameTIL = findViewById(R.id.agenda_name_txt);
        agendaName = agendaNameTIL.getEditText();
        agendaDateTIL = findViewById(R.id.agenda_date_txt);
        agendaDate = agendaDateTIL.getEditText();
        agendaTimeTIL = findViewById(R.id.agenda_time_txt);
        agendaTime = agendaTimeTIL.getEditText();
        agendaAddressTIL = findViewById(R.id.agenda_address_txt);
        agendaAddress = agendaAddressTIL.getEditText();

        agendaP2M = findViewById(R.id.agenda_personToMeet_txt);
        agendaContact = findViewById(R.id.agenda_contact_txt);
        agendaNotes = findViewById(R.id.agenda_notes_txt);

        errorText = findViewById(R.id.error_txt);

        nextBtn = findViewById(R.id.create_agenda_nx_btn);
        spinner = findViewById(R.id.agenda_type_dd);
        agendaTypes = getResources().getStringArray(R.array.agenda_types);
        spinnerItems = new SpinnerItem[]{
                new SpinnerItem(R.drawable.agenda_type, "Agenda Type"),
                new SpinnerItem(R.drawable.appointment, "Appointment"),
                new SpinnerItem(R.drawable.bakery, "Bakery"),
                new SpinnerItem(R.drawable.get_together, "Get Together"),
                new SpinnerItem(R.drawable.hospital, "Hospital"),
                new SpinnerItem(R.drawable.meeting, "Meeting"),
                new SpinnerItem(R.drawable.social_event, "Social Event"),
                new SpinnerItem(R.drawable.walk, "Walk"),
                new SpinnerItem(R.drawable.other, "Other"),
        };
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this,
                R.layout.create_agenda_spinner_default_item, agendaTypes) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, View convertView,
                                        @NonNull ViewGroup parent) {
                return getCustomView(position, parent);
            }
        };
        adapter.setDropDownViewResource(R.layout.create_agenda_spinner_dropdown_item);
        spinner.setAdapter(adapter);


        datePickerDialog = new DatePickerDialog(
                CreateAgendaActivity.this, CreateAgendaActivity.this, year, month, day);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.setTitle(R.string.select_date);

        timestamp = String.valueOf(mNow.getTimeInMillis());

        // Pushing Mobile number
        mobile = getIntent().getStringExtra("userMobileNumber");
        agendaModel.setMobile(mobile);

        // Google Places API autocomplete
        Places.initialize(getApplicationContext(), getString(R.string.api_key));
        agendaAddress.setFocusable(false);
        agendaAddress.setOnClickListener(v -> {
            List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS);
            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList).build(CreateAgendaActivity.this);
            startActivityForResult(intent, 100);
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            Place place = Autocomplete.getPlaceFromIntent(data);
            agendaAddress.setText(place.getAddress());
        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            Status status = Autocomplete.getStatusFromIntent(data);
            Toast.makeText(CreateAgendaActivity.this, status.getStatusMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void openDatePicker(View v) {
        datePickerDialog.show();
    }

    public void openTimePicker(View v) {
        timePickerDialog = new TimePickerDialog(CreateAgendaActivity.this, CreateAgendaActivity.this, hour, minute, false);
        timePickerDialog.setTitle(R.string.select_time);
        timePickerDialog.show();
    }

    public void btn_agendaNext(View view) {
        // Making error text invisible
        errorText.setVisibility(View.GONE);

        // switch base validation for fields
        switch (pos) {
            case 0:
                if (agendaName.getText().toString().isEmpty()) {
                    agendaNameTIL.setError(getString(R.string.agenda_name_error));
                    return;
                } else {
                    agendaNameTIL.setError(null);
                }
                if (spinner.getSelectedItemPosition() == 0) {
                    ((TextView) spinner.getSelectedView()).setTextColor(Color.RED);
                    errorText.setText(R.string.agenda_type_error);
                    errorText.setVisibility(View.VISIBLE);
                    return;
                } else {
                    ((TextView) spinner.getSelectedView()).setTextColor(getColor(R.color.colorPrimary));
                }
                break;
            case 1:
                if (agendaDate.getText().toString().isEmpty()) {
                    agendaDateTIL.setError(getString(R.string.agenda_date_error));
                    return;
                } else {
                    agendaDateTIL.setError(null);
                }
                if (agendaTime.getText().toString().isEmpty()) {
                    agendaTimeTIL.setError(getString(R.string.agenda_time_error));
                    return;
                } else {
                    agendaTimeTIL.setError(null);
                }
                String[] time = timeToNotify.split(":");
                int mH = Integer.parseInt(time[0]);
                int mM = Integer.parseInt(time[1]);
                if (dateToNotify.equals(todayDate) && mH <= hour && mM <= minute) {
                    errorText.setText(R.string.agenda_type_error);
                    errorText.setVisibility(View.VISIBLE);
                    return;
                } else {
                    errorText.setVisibility(View.GONE);
                }
                break;
            case 2:
                if (agendaAddress.getText().toString().isEmpty()) {
                    agendaAddressTIL.setError(getString(R.string.agenda_address_error));
                    return;
                } else {
                    agendaAddressTIL.setError(null);
                }
                break;

        }
        // calling next for limited times
        if (pos < 3) {
            pos = pos + 1;
            // hiding other fields
            renderLayout();
        } else {
            for (int i = 0; i < 4; i++) {
                findViewById(layouts[i]).setVisibility(View.GONE);
            }
            findViewById(layouts[4]).setVisibility(View.VISIBLE);
            agendaKey = mobile + "-" + keyLeft + keyRight;
            Log.d("DEBUG", "btn_agendaNext: " + agendaKey);


            agendaModel.set_agendaId(agendaKey);
            agendaModel.setName(agendaName.getText().toString());
            agendaModel.setType(spinnerItems[spinner.getSelectedItemPosition()].value);
            agendaModel.setAddress(agendaAddress.getText().toString());
            agendaModel.setPersonToMeet(agendaP2M.getText().toString());
            agendaModel.setContactNumber(agendaContact.getText().toString());
            agendaModel.setNotes(agendaNotes.getText().toString());
            agendaModel.setTimestamp(timestamp);

            String eventAddress = agendaAddress.getText().toString();
            String eventName = agendaName.getText().toString();
            String agendaText = eventName + " at " + eventAddress;

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference agendas = database.getReference("agendas");

            agendas.child(agendaKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        agendas.child(agendaKey).setValue(agendaModel).addOnSuccessListener(aVoid -> {

                            Toast.makeText(CreateAgendaActivity.this, R.string.agenda_create_success, Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(CreateAgendaActivity.this, HomePage.class);
                            intent.putExtra("userMobileNumber", mobile);
//                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            resetForm();
                            startActivity(intent);
                            resetActivity(0);
//                finish();
//                Log.d("AGENDA", agendaModel.toString());
                        }).addOnFailureListener(e -> {
                            Log.d("DEBUG", "btn_agendaNext: " + e.getMessage());
                            Toast.makeText(CreateAgendaActivity.this, R.string.agenda_create_failure, Toast.LENGTH_LONG).show();
                            finish();
                        });
                    } else {
                        Toast.makeText(CreateAgendaActivity.this, R.string.agenda_create_conflict, Toast.LENGTH_LONG).show();
                        resetActivity(1);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            setAlarm(agendaText, dateToNotify, timeToNotify);
        }
    }

    @Override
    public void onDateSet(DatePicker datePicker, int y, int m, int d) {
        String lM = (m < 9 ? "0" + (m + 1) : String.valueOf(m + 1));
        String lD = (d < 9 ? "0" + d : String.valueOf(d));
        String selectedDate = lD + "-" + lM + "-" + y;
        dateToNotify = selectedDate;
        agendaDate.setText(selectedDate);
        keyLeft = y + "" + lM + "" + lD;
        agendaModel.setDate(selectedDate);
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int h, int m) {
        if (dateToNotify != null && dateToNotify.equals(todayDate) && h <= hour && m <= minute) {
            errorText.setText(R.string.agenda_time_past);
            errorText.setVisibility(View.VISIBLE);
            agendaTime.setText(null);
            return;
        } else {
            errorText.setVisibility(View.GONE);
        }
        String lH = (h < 10 ? "0" + h : String.valueOf(h)), lM = (m < 10 ? "0" + m : String.valueOf(m));

        String selectedTime = lH + ":" + lM;
        timeToNotify = selectedTime;
        agendaTime.setText(selectedTime);
        keyRight = lH + "" + lM;
        agendaModel.setTime(selectedTime);
    }

    private void setAlarm(String text, String date, String time) {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);


        Intent intent = new Intent(getApplicationContext(), AlarmBrodcast.class);
        intent.putExtra("event", text);
        intent.putExtra("time", date);
        intent.putExtra("date", time);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
        String dateAndTime = date + " " + timeToNotify;
        DateFormat formatter = new SimpleDateFormat("d-M-yyyy hh:mm");
        try {
            Date date1 = formatter.parse(dateAndTime);
            am.set(AlarmManager.RTC_WAKEUP, date1.getTime(), pendingIntent);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        // finish();

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        agendaModel.setType(spinnerItems[position].value);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    public View getCustomView(int position, ViewGroup parent) {

        View row = getLayoutInflater().inflate(R.layout.create_agenda_spinner_dropdown_item, parent, false);

        TextView label = row.findViewById(R.id.spinner_item_text);
        ImageView icon = row.findViewById(R.id.spinner_item_icon);
//        String currentAgenda = agendaTypes[position];

        label.setText(agendaTypes[position]);
        icon.setImageResource(spinnerItems[position].drawable);
        if (position == 0) {
            // Set the disable item text color
            label.setTextColor(Color.GRAY);
            label.setTextSize(16);
            icon.setImageAlpha(0);
        } else {
            label.setTextColor(Color.BLACK);
        }

        return row;
    }

    private void resetActivity(int posToShow) {
        pos = posToShow;
        findViewById(layouts[pos]).setVisibility(View.VISIBLE);
        for (int i = 0; i < 5; i++) {
            if (i == pos)
                continue;
            findViewById(layouts[i]).setVisibility(View.GONE);
        }
        nextBtn.setText(R.string.next);
    }

    private void resetForm() {
        agendaName.setText("");
        spinner.setSelection(0);
        agendaDate.setText("");
        agendaTime.setText("");
        agendaAddress.setText("");
        agendaP2M.setText("");
        agendaContact.setText("");
        agendaNotes.setText("");
    }

    boolean backEvent() {
        if (pos > 0) {
            pos--;
            renderLayout();
            return false;
        }
        return true;
    }

    void renderLayout() {
        for (int i = 0; i < 4; i++) {
            if (i == pos) {
                continue;
            }
            findViewById(layouts[i]).setVisibility(View.GONE);
        }
        // showing required fields
        findViewById(layouts[pos]).setVisibility(View.VISIBLE);

        // calling submit on last fields
        if (pos == 3) {
            nextBtn.setText(R.string.submit);
        }

    }

    @Override
    public void onBackPressed() {
        if (backEvent()) {
            super.onBackPressed();
        }
    }

    public static class SpinnerItem {
        int drawable;
        String value;

        SpinnerItem(int d, String value) {
            this.drawable = d;
            this.value = value;
        }
    }
}