package com.example.iot_locker;


import static android.widget.Toast.LENGTH_SHORT;
import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.nio.charset.StandardCharsets;


public class EnterAddressActivity extends AppCompatActivity {

    private BigInteger publicKeyN;
    private BigInteger publicKeyE;

    private Button enterAddressButton, logout, otpSend, check;
    private EditText addressEditText, otpEnter;
    private ProgressBar progressBar;
    DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // EdgeToEdge.enable(this);
        setContentView(R.layout.activity_enter_address);

        enterAddressButton = findViewById(R.id.send_address_button);
        logout = findViewById(R.id.logout_button_hm);
        addressEditText = findViewById(R.id.address_edit_text);
        otpEnter = findViewById(R.id.otp_edit_text);
        otpSend = findViewById(R.id.send_otp_button);
        progressBar = findViewById(R.id.progressBar);
        check = findViewById(R.id.check_button);


        // Initialize the database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("LockerApp");

        // Call publicKey() method to retrieve public key from Firebase
        publicKey();



//----------------------------------------------------------------------------------------------------------


        // Enter Address Button Click Listener
        enterAddressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String address = addressEditText.getText().toString().trim();
                if (!address.isEmpty()) {
                    saveAddress(address);
                    buttonGeoCoordinates();

                } else {
                    Toast.makeText(EnterAddressActivity.this, "Please enter an address", LENGTH_SHORT).show();
                }

            }

        });



        //----------------------------------------------------------------------------------------------------
        // Logout Button Click Listener
        logout.setOnClickListener(v -> {
            log();
        });

        //---------------------------------


        //check delivery person details button
        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCurrentDeliveryPerson();
                publicKey();

            }
        });




        //-------------------------------------------------------

        // OTP Send Button Click Listener
        otpSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String otp = otpEnter.getText().toString().trim();
                if (!otp.isEmpty()) {
                    if (publicKeyN != null && publicKeyE != null) {
                        // Encrypt OTP using RSA encryption method
                        String encryptedOtp = encrypt(otp, publicKeyN, publicKeyE);
                        // Save encrypted OTP and other operations
                        saveOTP(encryptedOtp);
                        checkAndSetNewOtpFlag();
                    } else {
                        Toast.makeText(EnterAddressActivity.this, "Public key not available", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(EnterAddressActivity.this, "Please enter an OTP", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    //-----------------------------------------------------------------
    // The method to retrieve public key and show delivery person details
    private void publicKey() {
        // You can either pass a fixed keyId or get it dynamically based on your needs
        String keyId = "-O6EtfdYKylFMNHgGd3l";  // Replace with the actual keyId

        // Create a reference to the "PublicKeys" node in your Firebase database
        DatabaseReference publicKeysRef = FirebaseDatabase.getInstance().getReference("LockerApp").child("PublicKeys");

        // Fetch the public key object using the unique keyId
        publicKeysRef.child(keyId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Get the PublicKeyObject from Firebase
                AddressDisplayActivity.PublicKeyObject publicKeyObject = task.getResult().getValue(AddressDisplayActivity.PublicKeyObject.class);

                if (publicKeyObject != null) {
                    // Use the public key values (n and e)
                    publicKeyN = BigInteger.valueOf(publicKeyObject.n);
                    publicKeyE = BigInteger.valueOf(publicKeyObject.e);

                    // Display the public key values to the user (or process them)
                    Toast.makeText(EnterAddressActivity.this, "Public Key: n=" + publicKeyN + ", e=" + publicKeyE, Toast.LENGTH_SHORT).show();

                    // Here, you can also call additional methods to display more details
                    // about the delivery person if you want to show other information
                    // showAdditionalDeliveryPersonDetails(); // if needed
                } else {
                    Toast.makeText(EnterAddressActivity.this, "No Public Key Found", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Notify user of failure
                Toast.makeText(EnterAddressActivity.this, "Failed to Retrieve Public Key", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // RSA Encryption Method
    public String encrypt(String plaintext, BigInteger n, BigInteger e) {
        try {
            BigInteger plaintextBigInt = new BigInteger(1, plaintext.getBytes(StandardCharsets.UTF_8));
            BigInteger ciphertext = plaintextBigInt.modPow(e, n);
            return ciphertext.toString(16); // Converts the ciphertext to a hexadecimal string
        } catch (Exception ex) {
            Log.e(TAG, "Encryption error", ex);
            return null;
        }
    }


    //--------------------------------------------------------------------------------------------
    // Add GeoCoordinates to Google Sheet
    private void addItemToSheet(final String latitude, final String longitude) {
        //final ProgressDialog dialog = ProgressDialog.show(EnterAddressActivity.this, "Adding Address", "Please Wait...");
        progressBar.setVisibility(View.VISIBLE);
        final String address = addressEditText.getText().toString().trim();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://script.google.com/macros/s/AKfycbwA5qJbCytpuoEvUxrE39qqrwlN8Shu19AQgeO2m1TlbQPnfFGqs8cSEj2XZk_KosuC/exec", response -> {
            //dialog.dismiss();
            progressBar.setVisibility(View.GONE);
            Toast.makeText(EnterAddressActivity.this, "" + response, LENGTH_SHORT).show();
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //dialog.dismiss();
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error adding item to sheet", error);

            }
        }
        ) {
            @NonNull
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                params.put("action", "addItem");
                params.put("Address", address);
                params.put("Latitude", latitude);
                params.put("Longitude", longitude);
                return params;
            }
        };

        int timeOut = 50000;
        RetryPolicy retryPolicy = new DefaultRetryPolicy(timeOut, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(retryPolicy);

        RequestQueue queue = Volley.newRequestQueue(EnterAddressActivity.this);
        queue.add(stringRequest);
    }

    //---------------------------------------------------------------------------------

    // Save Address to Firebase
    private void saveAddress(String address) {
        databaseReference.child("Address").setValue(address)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(EnterAddressActivity.this, "Address saved successfully", LENGTH_SHORT).show();
                        addressEditText.setText("");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EnterAddressActivity.this, "Failed to save address", LENGTH_SHORT).show();
                        Log.e(TAG, "Error saving address", e);

                    }
                });
    }

    //--------------------------------------------------------------------------------------------------
// Convert Address to GeoCoordinates and Save
    public void buttonGeoCoordinates() {
        Geocoder geocoder = new Geocoder(this);
        List<Address> addressList;

        try {
            addressList = geocoder.getFromLocationName(addressEditText.getText().toString(), 1);
            if (addressList != null && !addressList.isEmpty()) {
                double doubleLat = addressList.get(0).getLatitude();
                double doubleLong = addressList.get(0).getLongitude();
                String latitude = String.valueOf(doubleLat);
                String longitude = String.valueOf(doubleLong);
                databaseReference.child("Longitude").setValue(latitude);
                databaseReference.child("Latitude").setValue(longitude);

                addItemToSheet(latitude, longitude);

            }
        } catch (IOException e) {
            Log.e(TAG, "Error converting address to geocordinates", e);
        }
    }


    // Log Out and Redirect
    private void log() {
        databaseReference.child("home_user").setValue("false")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(EnterAddressActivity.this, "Successfully Logged out", LENGTH_SHORT).show();
                        startActivity(new Intent(EnterAddressActivity.this, HomeUserActivity.class));
                        finish();
                    }
                });
    }

    //method for check currently log delivery person details
    private void showCurrentDeliveryPerson() {
        RelativeLayout accceptRelativeLayout = findViewById(R.id.successLayout);
        View view = LayoutInflater.from(EnterAddressActivity.this).inflate(R.layout.activity_success_dialog, accceptRelativeLayout);

        Button check = view.findViewById(R.id.AcceptDone);
        Button cancle = view.findViewById(R.id.CancelBtn);
        TextView phone = view.findViewById(R.id.AcceptDes);

        //Set up a ValueEventListener to fetch the current delivery person's phone number
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    String phoneNumber = snapshot.child("CurrentDeliveryPerson").getValue(String.class);
                    phone.setText("Phone Number : " + phoneNumber);
                }
            }

            @SuppressLint("SetTextI18n")
            public void onCancelled(@NonNull DatabaseError error) {
                phone.setText("Failed to load data: " + error.getMessage());

            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(EnterAddressActivity.this);
        builder.setView(view);
        final AlertDialog alertDialog = builder.create();

        check.findViewById(R.id.AcceptDone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                databaseReference.child("hu_accept").setValue("true");
            }
        });

        cancle.findViewById(R.id.CancelBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                databaseReference.child("hu_accept").setValue("false");

            }
        });
        if (alertDialog.getWindow() != null){
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        alertDialog.show();

    }

    // Save Encrypted OTP to Firebase
    private void saveOTP(String encryptedOtp) {
        databaseReference.child("otp").setValue(encryptedOtp)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(EnterAddressActivity.this, "Encrypted OTP saved successfully", LENGTH_SHORT).show();
                    otpEnter.setText("");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EnterAddressActivity.this, "Failed to save OTP", LENGTH_SHORT).show();
                    Log.e(TAG, "Error saving OTP", e);
                });
    }


    // Check if OTP exists and set new_otp flag
    private void checkAndSetNewOtpFlag() {
        databaseReference.child("otp").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getValue() != null) {
                    databaseReference.child( "new_otp").setValue("true");

                    //send otp to delivery person
                    databaseReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if (task.getResult().exists()){
                                Toast.makeText(EnterAddressActivity.this, "Successfully", LENGTH_SHORT).show();
                                DataSnapshot dataSnapshot = task.getResult();
                                String dpPhone = String.valueOf(dataSnapshot.child("CurrentDeliveryPerson").getValue());
                                //binding.view.setText()
                                String otpMessage = String.valueOf(dataSnapshot.child("otp").getValue());

                                if (!dpPhone.isEmpty() && !otpMessage.isEmpty()){
                                    //initialize SMS Manager
                                    SmsManager smsManager = SmsManager.getDefault();
                                    //send message
                                    smsManager.sendTextMessage(dpPhone, null,otpMessage, null, null);
                                    //display text message
                                    //request to otp
                                    Toast.makeText(EnterAddressActivity.this,"SMS Send Successfully", Toast.LENGTH_SHORT).show();
                                    databaseReference.child( "refresh").setValue("true");

                                }
                            }

                        }
                    });

                } else {
                    databaseReference.child("new_otp").setValue("false");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error checking OTP value", error.toException());
            }
        });

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up any resources
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Handle pause state
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Handle resume state
    }
}