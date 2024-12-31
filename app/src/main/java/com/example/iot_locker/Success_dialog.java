package com.example.iot_locker;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class Success_dialog extends AppCompatActivity {

    private TextView phone;
    private Button accept, cancle;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_success_dialog);

        // Correctly initialize the views
        phone = findViewById(R.id.AcceptDes);
        accept = findViewById(R.id.AcceptDone);
        cancle = findViewById(R.id.CancelBtn);

        // Initialize the database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("LockerApp");

        //Set up a ValueEventListener to fetch the current delivery person's phone number
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    String phoneNumber = snapshot.child("CurrentDeliveryPerson").getValue(String.class);
                    phone.setText(phoneNumber);
                }
            }

            @SuppressLint("SetTextI18n")
            public void onCancelled(@NonNull DatabaseError error) {
                phone.setText("Failed to load data: " + error.getMessage());

            }
        });



    }
}