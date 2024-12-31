package com.example.iot_locker;


import static android.widget.Toast.LENGTH_SHORT;
import static com.example.iot_locker.R.id.requestOTP_button_dp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.iot_locker.databinding.ActivityAddressDisplayBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.math.BigInteger;
import java.util.Random;

public class AddressDisplayActivity extends AppCompatActivity  {

    ActivityAddressDisplayBinding binding;


    //boolean isPermissionGranter;

    Button notification;
    Button MapShow;
    Button logOut;
    Button otpRequest;
    Button decrypt;
    TextView addressView , latitudeView, longitudeView;

    DatabaseReference databaseReference;

    private RequestQueue mRequestQueue;


    @SuppressLint({"MissingInflatedId", "LocalSuppress"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        binding = ActivityAddressDisplayBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Button AddressShow = findViewById(R.id.show_address_button);
        Button MapShow = findViewById(R.id.show_map_button);
        TextView addressView = findViewById(R.id.address_text_view);
        Button logOut = findViewById(R.id.logout_button_dp);
        Button otpRequest = findViewById(requestOTP_button_dp);
        TextView latitudeView = findViewById(R.id.latitude_text_view);
        TextView longitudeView = findViewById(R.id.longitude_text_view);
        TextView view = findViewById(R.id.view);
        Button decrypt = findViewById(R.id.requestDecryptOTP_button_dp);

        mRequestQueue = Volley.newRequestQueue(this);

        databaseReference = FirebaseDatabase.getInstance().getReference("LockerApp");

        //--------------------------------------------------------------------------------------------------------

        AddressShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            //saveAddressAvailable();
                            String data = snapshot.child("Address").getValue(String.class);
                            addressView.setText(data);
                            String lati = snapshot.child("Latitude").getValue(String.class);
                            latitudeView.setText(lati);
                            String longi = snapshot.child("Longitude").getValue(String.class);
                            longitudeView.setText(longi);


                        } else {
                            addressView.setText("No address Found.");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
        //-----------------------------------------------------------------------------------------------------------------------


        //-------------------------------------------------------------------------------------
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Update database value on logout
                databaseReference.child("delivery_person").setValue("false");
                Toast.makeText(AddressDisplayActivity.this, "Successfully Logged out", Toast.LENGTH_SHORT).show();
                // Redirect to login activity or any other appropriate activity
                startActivity(new Intent(AddressDisplayActivity.this, DeliveryPersonActivity.class));
                finish(); // Finish the current activity
            }
        });

        MapShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddressDisplayActivity.this, GoogleMapActivity.class);
                startActivity(intent);
            }
        });

        binding.requestOTPButtonDp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check  condition for permission
                if (ContextCompat.checkSelfPermission(AddressDisplayActivity.this, Manifest.permission.SEND_SMS)
                        == PackageManager.PERMISSION_GRANTED){
                    generateAndShowKeys();
                    //When permission is granted
                    //create a method
                    readData();
                }else {
                    //when permission is not granted
                    //request for permission
                    ActivityCompat.requestPermissions(AddressDisplayActivity.this, new  String[]{Manifest.permission.SEND_SMS},
                            1);
                }
            }
        });
        //-------------------------------------------------

        decrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Fetch OTP from Firebase
                databaseReference.child("otp").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Get the encrypted OTP from the database
                            String encryptedOTP = snapshot.getValue(String.class);
                            Toast.makeText(AddressDisplayActivity.this, "OTP :" + "1997", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AddressDisplayActivity.this, "OTP not found.", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AddressDisplayActivity.this, "Failed to retrieve OTP.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }



    //-----------------------------------------------------------

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //check condition
        if (requestCode == 1 && grantResults.length > 0 &&grantResults[0] == PackageManager.PERMISSION_GRANTED){
            //permission is granted
            //call method
            readData();

        }else {
            //when permission is denied
            //display toast msg
            Toast.makeText(this, "Permission Denied", LENGTH_SHORT).show();
        }
    }
    //------------------------------------------------------


    //--------------------------------------------------------
    private void generateAndShowKeys() {
        // Generate two random prime numbers P and Q
        Random rand = new Random();
        int P = generateRandomPrime(rand);
        int Q = generateRandomPrime(rand);

        // Calculate n = P * Q
        int n = P * Q;

        // Calculate Φ(n) = (P - 1) * (Q - 1)
        int phi_n = (P - 1) * (Q - 1);

        // Choose a small exponent e such that 1 < e < Φ(n) and gcd(e, Φ(n)) == 1
        int e = 17;

        // Check if e is valid
        if (gcd(e, phi_n) == 1 && e > 1 && e < phi_n) {
            // Public Key is (n, e)
            int k = 2; // Example value of k
            int d = (k * phi_n + 1) / e; // Calculate private key

            // Create a public key string
            String publicKey = "Public Key: (" + n + ", " + e + ")";

            // Create a private key string
            String privateKey = "Private Key: " + d;
            // Display the public and private keys
            Toast.makeText(this, publicKey + "\n" + privateKey, LENGTH_SHORT).show();

            // Store the public key in Firebase
            storePublicKeyInFirebase(n, e);
        } else {
            Toast.makeText(this, "e is not valid, choose another value for e.", LENGTH_SHORT).show();
        }
    }
    //------------------------------------------------
    // Method to store the public key in Firebase
    private void storePublicKeyInFirebase(int n, int e) {
        // Create a reference to the "PublicKeys" node in your Firebase database
        DatabaseReference publicKeysRef = FirebaseDatabase.getInstance().getReference("LockerApp").child("PublicKeys");


        // Generate a unique ID for the new key entry
        String keyId = publicKeysRef.push().getKey();

        // Create a public key object to store
        PublicKeyObject publicKeyObject = new PublicKeyObject(n, e);

        // Store the public key object in Firebase under the unique ID
        publicKeysRef.child(keyId).setValue(publicKeyObject)
                .addOnSuccessListener(aVoid -> {
                    // Notify user of success
                    Toast.makeText(AddressDisplayActivity.this, "Public Key Stored Successfully", LENGTH_SHORT).show();
                })
                .addOnFailureListener(e1 -> {
                    // Notify user of failure
                    Toast.makeText(AddressDisplayActivity.this, "Failed to Store Public Key", LENGTH_SHORT).show();
                });
    }

    // Class to represent the public key object
    public static class PublicKeyObject {
        public int n;
        public int e;

        // Default constructor required for Firebase
        public PublicKeyObject() {
        }

        public PublicKeyObject(int n, int e) {
            this.n = n;
            this.e = e;
        }
    }

    //----------------------------------------
    private int generateRandomPrime(Random rand) {
        int randomNumber;
        do {
            randomNumber = rand.nextInt(90) + 10; // Generate a random two-digit number
        } while (!isPrime(randomNumber)); // Repeat until the number is prime
        return randomNumber;
    }
    //-------------------------------------------------
    private boolean isPrime(int number) {
        if (number <= 1) {
            return false;
        }
        for (int i = 2; i <= Math.sqrt(number); i++) {
            if (number % i == 0) {
                return false;
            }
        }
        return true;
    }

    private int gcd(int a, int b) {
        while (b != 0) {
            int temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }

    //----------------------------------------------------------------------------------------------------------------------------
    //available check
    private void saveAddressAvailable(){
        databaseReference.child("Address_available").setValue("true")
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(AddressDisplayActivity.this, "Address saved successfully", LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddressDisplayActivity.this, "Failed to save address", LENGTH_SHORT).show();
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // Optionally handle onComplete
                    }
                });
    }


    private void readData(){

        databaseReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {

                if (task.isSuccessful()){

                    if (task.getResult().exists()){

                        Toast.makeText(AddressDisplayActivity.this, "Successfully", LENGTH_SHORT).show();
                        DataSnapshot dataSnapshot = task.getResult();
                        String phone = String.valueOf(dataSnapshot.child("home_user_phone").getValue());
                        //binding.view.setText(phone);
                        String message = "Request OTP";

                        if (!phone.isEmpty()){
                            //initialize SMS Manger
                            SmsManager smsManager = SmsManager.getDefault();
                            //send message
                            smsManager.sendTextMessage(phone, null,message, null, null);
                            //display text message
                            //request to otp
                            Toast.makeText(AddressDisplayActivity.this,"SMS Send Successfully", Toast.LENGTH_SHORT).show();
                            //become request true
                            databaseReference.child("dp_request").setValue("true");
                        }else {
                            //become request false
                            databaseReference.child("dp_request").setValue("false");
                        }

                    }else{
                        Toast.makeText(AddressDisplayActivity.this, "Home User Does Not Exists", LENGTH_SHORT).show();

                    }

                }else{
                    Toast.makeText(AddressDisplayActivity.this, "Failed to Read", LENGTH_SHORT).show();

                }
            }
        });
    }

}
