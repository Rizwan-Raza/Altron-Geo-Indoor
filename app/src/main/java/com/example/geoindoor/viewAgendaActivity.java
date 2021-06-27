package com.example.geoindoor;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.animation.ScaleAnimation;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.geoindoor.models.Agenda;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class viewAgendaActivity extends AppCompatActivity {

    private static final String TAG = "viewAgendaActivity";
    private final Agenda agendaModel = new Agenda();
    ListView listView;
    ArrayList<Agenda> agendaList = new ArrayList<>();
    DatabaseReference databaseReference;
    GestureDetector gestureDetector;
    private AgendaListAdapter adapter;
    private float mScale = 1f;
    private ScaleGestureDetector mScaleDetector;
    private String mobile, status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_agenda);
        getSupportActionBar().setTitle(R.string.view_agenda);

        // Pushing Mobile number
        mobile = getIntent().getStringExtra("userMobileNumber");

        listView = findViewById(R.id.listView);
        adapter = new AgendaListAdapter(this, R.layout.adapterview, agendaList);

        databaseReference = FirebaseDatabase.getInstance().getReference("agendas");
        databaseReference.orderByChild("mobile").equalTo(mobile).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //change
                //databaseReference.orderByChild("status").equalTo(false);
                boolean skipThis;
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    //change
                    Agenda agendas = dataSnapshot1.getValue(Agenda.class);
                    if (!agendas.isStatus()) {
                        skipThis = false;
                        for (Agenda agenda : agendaList) {
                            if (agenda.get_agendaId().equals(agendas.get_agendaId())) {
                                skipThis = true;
                            }
                        }
                        if (!skipThis) {
                            agendaList.add(agendas);
                            adapter.notifyDataSetChanged();
                        }
                    }
                    //Store date,time and destination on a new Trip.
//                    String datePassed = agendas.getDate().toString();
//                    String timePassed = agendas.getTime().toString();
//                    System.out.println("what is this date:" +datePassed);
//                    System.out.println("what is this time:" +timePassed);
//
//
//                    Long timestamp = null;
//                    try {
//                        timestamp = toMilli(datePassed+ " " + timePassed);
//                    } catch (ParseException e) {
//                        e.printStackTrace();
//                    }
//                    System.out.println("what is this :" +timestamp);

//                    agendas.setTimestamp(timestamp);

//                    Collections.sort(agendaList);

                }
                listView.setAdapter(adapter);
                if (agendaList.isEmpty()) {
                    toastMsg(getString(R.string.no_agenda));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            int count = (int) id;
            Intent intent = new Intent(viewAgendaActivity.this, detailActivity.class);
            intent.putExtra("count", count);
            intent.putExtra("mobileNumber", mobile);
            intent.putParcelableArrayListExtra("key", agendaList);
            startActivityForResult(intent, 101);
        });
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
                listView.startAnimation(scaleAnimation);

                return true;
            }
        });


    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        super.dispatchTouchEvent(ev);
        mScaleDetector.onTouchEvent(ev);
        gestureDetector.onTouchEvent(ev);
        return gestureDetector.onTouchEvent(ev);
    }

    public void toastMsg(String msg) {
        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        toast.show();
    }

//    public Long toMilli (String dateIn) throws ParseException {
//        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
//        Date date = (Date) formatter.parse(dateIn);
//        long output = date.getTime() / 1000L;
//        String str = Long.toString(output);
//        long timestamp = Long.parseLong(str) * 1000;
//        return timestamp;
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK) {
            if (data != null) {
                if (data.getBooleanExtra("delete", false)) {
                    int indexToDelete = data.getIntExtra("key", -1);
                    if (indexToDelete >= 0) {
                        Log.d("ACTIVITY", "Deleting key " + indexToDelete);
                        agendaList.remove(indexToDelete);
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        }
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
