package com.example.iot_locker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.iot_locker.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;


public class HomeUserRegisterActivity extends AppCompatActivity {


    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("LockerApp");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_user_register);

        final EditText name = findViewById(R.id.register_name_reg_hm);
        final EditText phone = findViewById(R.id.register_phone_reg_hm);
        final EditText password = findViewById(R.id.register_password_reg_hm);
        final Button register = findViewById(R.id.button_register_reg_hm);
        final TextView loginRedirect = findViewById(R.id.register_login_link_hm);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String userName = name.getText().toString();
                final String userPhone = phone.getText().toString();
                final String userPassword = password.getText().toString();

                if (userName.isEmpty() || userPhone.isEmpty() || userPassword.isEmpty()){
                    Toast.makeText(HomeUserRegisterActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                }
                else {

                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if (snapshot.hasChild(userPhone)){

                                Toast.makeText(HomeUserRegisterActivity.this, "This email is already registered", Toast.LENGTH_SHORT).show();
                            }
                            else {

                                databaseReference.child("HomeUser").child(userPhone).child("name").setValue(userName);
                                databaseReference.child("HomeUser").child(userPhone).child("phone").setValue(userPhone);
                                databaseReference.child("HomeUser").child(userPhone).child("password").setValue(userPassword);
                                databaseReference.child("home_user_phone").setValue(userPhone);


                                Toast.makeText(HomeUserRegisterActivity.this, "User Register Successfully", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(HomeUserRegisterActivity.this, HomeUserActivity.class));
                                finish();

                                

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
        });

        loginRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(HomeUserRegisterActivity.this, HomeUserActivity.class));

            }
        });
    }

}