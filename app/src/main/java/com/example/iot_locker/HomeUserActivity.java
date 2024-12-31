package com.example.iot_locker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.Objects;

public class HomeUserActivity extends AppCompatActivity {

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("LockerApp");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_user);

        final EditText phone = findViewById(R.id.login_ph_hm);
        final EditText password = findViewById(R.id.login_password_hm);
        final Button login = findViewById(R.id.login_button_hm);
        final TextView registerRedirect = findViewById(R.id.login_register_hm);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String userPhone = phone.getText().toString();
                final String userPassword = password.getText().toString();

                if (userPhone.isEmpty() || userPassword.isEmpty()){

                    Toast.makeText(HomeUserActivity.this, "Please enter your username", Toast.LENGTH_SHORT).show();

                }
                else {

                    databaseReference.child("HomeUser").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if (snapshot.hasChild(userPhone)){

                                final String getPassword = snapshot.child(userPhone).child("password").getValue(String.class);

                                assert getPassword != null;
                                if (getPassword.equals(userPassword)){

                                    databaseReference.child("home_user").setValue("true");
                                    Toast.makeText(HomeUserActivity.this, "Successfully Logged in", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(HomeUserActivity.this, EnterAddressActivity.class));

                                }
                                else {

                                    Toast.makeText(HomeUserActivity.this, "Wrong Password", Toast.LENGTH_SHORT).show();
                                }

                            }
                            else {

                                Toast.makeText(HomeUserActivity.this, "Wrong Mobile Number", Toast.LENGTH_SHORT).show();

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
                startActivity(new Intent(HomeUserActivity.this, HomeUserRegisterActivity.class));
            }
        });


    }

}