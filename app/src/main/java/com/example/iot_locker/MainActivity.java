package com.example.iot_locker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;


import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Button buttonDeliveryPerson = findViewById(R.id.button_delivery_person);
        Button buttonHomeUser = findViewById(R.id.button_home_user);


        buttonDeliveryPerson.setOnClickListener(v -> {
            // Handle Delivery Person button click
            // Intent to navigate to Delivery Person activity
            Intent intent = new Intent(MainActivity.this, DeliveryPersonActivity.class);
            startActivity(intent);
        });

        buttonHomeUser.setOnClickListener(v -> {
            // Handle Home User button click
            // Intent to navigate to Home User activity
            Intent intent = new Intent(MainActivity.this, HomeUserActivity.class);
            startActivity(intent);
        });
    }
}