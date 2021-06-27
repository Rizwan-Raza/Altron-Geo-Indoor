package com.example.geoindoor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.geoindoor.models.Agenda;
import com.example.geoindoor.models.MarkerWrapper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ViewMyAgendasOnMap extends AppCompatActivity implements OnMapReadyCallback {

    boolean isPermissionGranted;
    MapView agendaMapView;
    GoogleMap mGoogleMapAgenda;
    FusedLocationProviderClient clientAgenda;
    String userId;
    boolean isAgendaPresent;
    List<String> address = new ArrayList<>();
    List<Marker> markerList = new ArrayList<>();
    List<MarkerWrapper> markerWrappers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_my_agendas_on_map);
        getSupportActionBar().setTitle(R.string.view_my_agenda_map);

        Intent intent = getIntent();
        userId = intent.getStringExtra("userMobileNumber");

        agendaMapView = findViewById(R.id.map_view_of_agendas);
        agendaMapView.onCreate(savedInstanceState);
        clientAgenda = LocationServices.getFusedLocationProviderClient(ViewMyAgendasOnMap.this);
        checkLocationPermission();
        checkPermissionGranted(clientAgenda);
    }

    private void checkPermissionGranted(FusedLocationProviderClient clientAgenda) {

        if (isPermissionGranted) {
            @SuppressLint("MissingPermission") Task<Location> task = clientAgenda.getLastLocation();
            task.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        agendaMapView.getMapAsync(new OnMapReadyCallback() {
                            @SuppressLint("MissingPermission")
                            @Override
                            public void onMapReady(GoogleMap googleMap) {
                                mGoogleMapAgenda = googleMap;
                                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                                Marker marker = mGoogleMapAgenda.addMarker(new MarkerOptions().position(latLng).title(getString(R.string.youAreHere)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                                marker.showInfoWindow();
                                markerList.add(marker);
                                mGoogleMapAgenda.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.0f));
                                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("agendas");
                                databaseReference.orderByChild("mobile").equalTo(userId).addValueEventListener(new ValueEventListener() {
                                    @RequiresApi(api = Build.VERSION_CODES.O)
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.hasChildren()) {
                                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                                Agenda agendas = dataSnapshot.getValue(Agenda.class);
                                                LocalDateTime now = LocalDateTime.now();
                                                DateTimeFormatter currentDateformat = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                                                String date = currentDateformat.format(now);
                                                Date systemDate = null;
                                                Date agendaDate = null;
                                                try {
                                                    systemDate = new SimpleDateFormat("dd-MM-yyyy").parse(date);
                                                    agendaDate = new SimpleDateFormat("dd-MM-yyyy").parse(agendas.getDate());
                                                } catch (ParseException e) {
                                                    e.printStackTrace();
                                                }
                                                if (systemDate.equals(agendaDate) && !agendas.isStatus()) {
                                                    address.add(agendas.getAddress());
                                                    markerWrappers.add(new MarkerWrapper(agendas.getAddress(), agendas.getType()));
                                                    isAgendaPresent = true;
                                                }
                                            }
                                            if (!isAgendaPresent) {
                                                toastMsg(getString(R.string.no_agenda_planned));
                                            } else {
                                                try {
                                                    addMarkersOnMap(address, markerWrappers);
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        });
                    }
                }
            });
        }
    }


    private void addMarkersOnMap(List<String> address, List<MarkerWrapper> markerWrappers) throws IOException {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        for (int i = 0; i < markerWrappers.size(); i++) {
            String markerAddress = markerWrappers.get(i).getMarkerAddress().toUpperCase();
            List<Address> addressList = geocoder.getFromLocationName(markerAddress, 1);
            if (addressList.size() > 0) {
                Address address1 = addressList.get(0);
                LatLng latLng = new LatLng(address1.getLatitude(), address1.getLongitude());
                mGoogleMapAgenda.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                String m = "_" + markerWrappers.get(i).getAgendaTye().toLowerCase();
                String markerType = m.replaceAll("\\s", "");
                Context context = getApplicationContext();
                Resources res = context.getResources();
                Drawable d = res.getDrawable(res.getIdentifier(markerType, "drawable", context.getPackageName()));
                Drawable sd = new ScaleDrawable(d, 0, 200, 200).getDrawable();
                Marker marker = mGoogleMapAgenda.addMarker(new MarkerOptions().position(latLng).icon(getBitmapFromIconsPack(sd)));
                mGoogleMapAgenda.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        Double lat = marker.getPosition().latitude;
                        Double lng = marker.getPosition().longitude;
                        /*Uri uri = Uri.parse("google.navigation:q=" + marker.getPosition().latitude + "," + marker.getPosition().longitude + "&mode = transit");
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        intent.setPackage("com.google.android.apps.maps");
                        startActivity(intent);*/
                        Intent intent = new Intent(getApplicationContext(), OutdoorNavType.class);
                        intent.putExtra("Marker_lat", String.valueOf(lat));
                        intent.putExtra("Marker_lng", String.valueOf(lng));
                        startActivity(intent);
                        return false;
                    }
                });
                markerList.add(marker);
            }
        }
        LatLngBounds latLngBounds = calculateBounds(markerList);
        int padding = 300;
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(latLngBounds, padding);
        mGoogleMapAgenda.animateCamera(cameraUpdate);
    }

    private LatLngBounds calculateBounds(List<Marker> markerList) {
        LatLngBounds latLngBounds;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (int i = 0; i < markerList.size(); i++) {
            builder.include(markerList.get(i).getPosition());
        }
        latLngBounds = builder.build();
        return latLngBounds;
    }

    private BitmapDescriptor getBitmapFromIconsPack(Drawable d) {
        Drawable drawable = d;
        drawable.setBounds(0, 0, 200, 200);
        Bitmap bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void checkLocationPermission() {
        Dexter.withContext(this).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                toastMsg(getString(R.string.location_perms));
                isPermissionGranted = true;
                clientAgenda = LocationServices.getFusedLocationProviderClient(ViewMyAgendasOnMap.this);
                checkPermissionGranted(clientAgenda);
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), "");
                intent.setData(uri);
                startActivity(intent);
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
            }
        }).check();

    }

    public void toastMsg(String msg) {
        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        toast.show();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
    }

    @Override
    protected void onStart() {
        super.onStart();
        agendaMapView.onStart();
    }


    @Override
    protected void onResume() {
        super.onResume();
        agendaMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        agendaMapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        agendaMapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        agendaMapView.onDestroy();

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        agendaMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        agendaMapView.onLowMemory();
    }
}