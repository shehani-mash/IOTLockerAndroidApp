package com.example.iot_locker;

import static com.example.iot_locker.R.*;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Objects;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.awt.font.NumericShaper;

public class DeliveryPersonActivity extends AppCompatActivity {


    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("LockerApp");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_delivery_person);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        final EditText phone = findViewById(R.id.phoneNumberDp);
       final EditText password = findViewById(R.id.login_password_dp);
       final Button login = findViewById(R.id.login_button_dp);
       final TextView registerRedirect = findViewById(R.id.login_register_dp);


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String userPhone = phone.getText().toString();
                final String userPassword = password.getText().toString();

                if (userPhone.isEmpty() || userPassword.isEmpty()){

                    Toast.makeText(DeliveryPersonActivity.this, "Please enter your username", Toast.LENGTH_SHORT).show();

                }
                else {

                    databaseReference.child("DeliveryPersons").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if (snapshot.hasChild(userPhone)){

                                final String getPassword = snapshot.child(userPhone).child("password").getValue(String.class);

                                assert getPassword != null;
                                if (getPassword.equals(userPassword)){

                                    databaseReference.child("delivery_person").setValue("true");

                                    Toast.makeText(DeliveryPersonActivity.this, "Successfully Logged in", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(DeliveryPersonActivity.this, AddressDisplayActivity.class));

                                    // Store the phone number of the logged-in delivery person
                                    databaseReference.child("CurrentDeliveryPerson").setValue(userPhone);

                                }
                                else {

                                    Toast.makeText(DeliveryPersonActivity.this, "Wrong Password", Toast.LENGTH_SHORT).show();
                                }

                            }
                            else {

                                Toast.makeText(DeliveryPersonActivity.this, "Wrong Mobile Number", Toast.LENGTH_SHORT).show();

                            }



                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }

            }
        });


       registerRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Navigate to the register page
                startActivity(new Intent(DeliveryPersonActivity.this, DeliveryPersonRegisterActivity.class));
            }
        });


    }


}