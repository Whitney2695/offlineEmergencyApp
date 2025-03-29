package com.example.offlineemergencyapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView txtGPSStatus, txtLastSOS, txtContacts;
    private ProgressBar progressSending;
    private Button btnSOS, btnSettings;

    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final String SOS_PREF = "sos_prefs";
    private static final String LAST_SOS_TIME = "last_sos_time";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI elements
        txtGPSStatus = findViewById(R.id.txtGPSStatus);
        txtLastSOS = findViewById(R.id.txtLastSOS);
        txtContacts = findViewById(R.id.txtContacts);
        progressSending = findViewById(R.id.progressSending);
        btnSOS = findViewById(R.id.btnSOS);
        btnSettings = findViewById(R.id.btnSettings);

        // Check GPS status
        checkGPSStatus();

        // Load last SOS time
        loadLastSOSTime();

        // Load emergency contacts
        loadEmergencyContacts();

        // SOS Button Click Event
        btnSOS.setOnClickListener(v -> sendSOSAlert());

        // Settings Button Click Event (Updated to match `settings.java`)
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, settings.class);
            startActivity(intent);
        });
    }

    // Check GPS status
    private void checkGPSStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        txtGPSStatus.setText(isGPSEnabled ? "GPS: Enabled ✅" : "GPS: Disabled ❌");
    }

    // Load last SOS sent time
    private void loadLastSOSTime() {
        SharedPreferences sharedPreferences = getSharedPreferences(SOS_PREF, MODE_PRIVATE);
        String lastTime = sharedPreferences.getString(LAST_SOS_TIME, "Not Sent");
        txtLastSOS.setText("Last SOS: " + lastTime);
    }

    // Load emergency contacts (Dummy for now)
    private void loadEmergencyContacts() {
        txtContacts.setText("Emergency Contacts: 1234567890, 9876543210");
    }

    // Send SOS Alert
    private void sendSOSAlert() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, PERMISSION_REQUEST_CODE);
            return;
        }

        progressSending.setVisibility(View.VISIBLE);
        btnSOS.setEnabled(false);

        new Handler().postDelayed(() -> {
            String emergencyMessage = "⚠️ SOS ALERT! Emergency at location: " + getCurrentLocation();
            sendSMS("1234567890", emergencyMessage);
            saveLastSOSTime();
            progressSending.setVisibility(View.GONE);
            btnSOS.setEnabled(true);
            Toast.makeText(MainActivity.this, "SOS Sent!", Toast.LENGTH_SHORT).show();
        }, 3000); // Simulating sending delay
    }

    // Get current location (Dummy value for now)
    private String getCurrentLocation() {
        return "Latitude: -1.2921, Longitude: 36.8219 (Nairobi, Kenya)";
    }

    // Send SMS message
    private void sendSMS(String phoneNumber, String message) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, message, null, null);
    }

    // Save last SOS sent time
    private void saveLastSOSTime() {
        String currentTime = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault()).format(new Date());
        SharedPreferences.Editor editor = getSharedPreferences(SOS_PREF, MODE_PRIVATE).edit();
        editor.putString(LAST_SOS_TIME, currentTime);
        editor.apply();
        txtLastSOS.setText("Last SOS: " + currentTime);
    }

    // Handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendSOSAlert();
            } else {
                Toast.makeText(this, "SMS Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
