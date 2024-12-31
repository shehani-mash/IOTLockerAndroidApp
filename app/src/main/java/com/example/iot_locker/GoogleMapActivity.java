package com.example.iot_locker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Locale;

public class GoogleMapActivity extends AppCompatActivity implements LocationListener {

    LocationManager locationManager;
    private DatabaseReference databaseReference;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_map);


        databaseReference = FirebaseDatabase.getInstance().getReference().child("LockerApp");




        getLocation();
        mapOpen();

        if (ContextCompat.checkSelfPermission(GoogleMapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(GoogleMapActivity.this, new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION
            },100);
        }


        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    //locker place
                    String destination = snapshot.child("Address").getValue(String.class);
                    String lati = snapshot.child("Latitude").getValue(String.class);
                    String longi = snapshot.child("Longitude").getValue(String.class);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @SuppressLint("MissingPermission")
    private  void getLocation()
    {
        try {
            locationManager = (LocationManager) getApplication().getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, GoogleMapActivity.this);
        }catch (Exception e){
            e.printStackTrace();
        }
    }



       @Override
        public void onLocationChanged (@NonNull Location location){


           String sourceLatitude = String.valueOf(location.getLatitude());
           String sourceLongitude = String.valueOf(location.getLongitude());

            Toast.makeText(this, "Latitude:" + sourceLatitude + ", Longitude: " + sourceLongitude, Toast.LENGTH_SHORT).show();

            databaseReference.child("current_Lat").setValue(sourceLatitude);
            databaseReference.child("current_Lng").setValue(sourceLongitude);

            try {
                //current location delivery person
                Geocoder geocoder = new Geocoder(GoogleMapActivity.this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                String source = addresses.get(0).getAddressLine(0);
                databaseReference.child("Current_Location").setValue(source);

            } catch (Exception e) {
                e.printStackTrace();

            }
        }



    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        LocationListener.super.onStatusChanged(provider, status, extras);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        LocationListener.super.onProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        LocationListener.super.onProviderDisabled(provider);
    }

    public void mapOpen(){
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    //locker place
                    String source = snapshot.child("Current_Location").getValue(String.class);
                    String destination = snapshot.child("Address").getValue(String.class);

                    Uri uri = Uri.parse("https://www.google.com/maps/dir/" + source + "/" + destination);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.setPackage("com.google.android.apps.maps");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}