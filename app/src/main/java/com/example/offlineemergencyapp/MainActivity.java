package com.example.offlineemergencyapp;

import android.location.Location;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.offlineemergencyapp.helpers.LocationHelper;
import com.example.offlineemergencyapp.helpers.ShakeDetector;
import com.example.offlineemergencyapp.services.EmergencyContactService;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView txtGPSStatus, txtLastSOS, txtContacts;
    private ProgressBar progressSending;
    private Button btnSOS, btnSettings;
    private LocationHelper locationHelper;
    private EmergencyContactService contactService;
    private ShakeDetector shakeDetector;
    private List<String> emergencyContacts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // UI Elements
        txtGPSStatus = findViewById(R.id.txtGPSStatus);
        txtLastSOS = findViewById(R.id.txtLastSOS);
        txtContacts = findViewById(R.id.txtContacts);
        progressSending = findViewById(R.id.progressSending);
        btnSOS = findViewById(R.id.btnSOS);
        btnSettings = findViewById(R.id.btnSettings);

        // Initialize Helpers
        locationHelper = new LocationHelper(this, new LocationHelper.GPSCallback() {
            @Override
            public void onGPSStatusChanged(boolean enabled) {
                updateGPSStatus(enabled);
            }
        });

        contactService = new EmergencyContactService(new EmergencyContactService.ContactsCallback() {
            @Override
            public void onContactsReceived(List<String> contacts) {
                updateEmergencyContacts(contacts);
            }
        });

        shakeDetector = new ShakeDetector(this, new ShakeDetector.ShakeListener() {
            @Override
            public void onShakeDetected() {
                sendSOS();
            }
        });

        // Fetch Contacts from Firebase
        contactService.fetchEmergencyContacts();

        // Button Click Listeners
        btnSOS.setOnClickListener(v -> sendSOS());
        btnSettings.setOnClickListener(v -> openSettings());

        // Apply Window Insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            v.setPadding(insets.getInsets(WindowInsetsCompat.Type.systemBars()).left,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).top,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).right,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom);
            return insets;
        });
    }

    private void sendSOS() {
        progressSending.setVisibility(View.VISIBLE);

        locationHelper.getCurrentLocation(new LocationHelper.LocationCallback() {
            @Override
            public void onLocationReceived(Location location) {
                String locationMessage = (location != null) ?
                        "https://maps.google.com/?q=" + location.getLatitude() + "," + location.getLongitude() :
                        "GPS unavailable";

                String message = "🚨 EMERGENCY SOS! Need help! 🚨\nLocation: " + locationMessage;

                if (emergencyContacts.isEmpty()) {
                    Toast.makeText(MainActivity.this, "No emergency contacts available!", Toast.LENGTH_LONG).show();
                    progressSending.setVisibility(View.GONE);
                    return;
                }

                for (String number : emergencyContacts) {
                    SmsManager.getDefault().sendTextMessage(number, null, message, null, null);
                }

                Toast.makeText(MainActivity.this, "SOS Alert Sent!", Toast.LENGTH_LONG).show();
                progressSending.setVisibility(View.GONE);
            }
        });
    }

    private void updateGPSStatus(boolean enabled) {
        txtGPSStatus.setText("GPS: " + (enabled ? "Enabled ✅" : "Disabled ❌"));
    }

    private void updateEmergencyContacts(List<String> contacts) {
        this.emergencyContacts = contacts;
        txtContacts.setText("Contacts: " + (contacts.isEmpty() ? "None Added" : String.join(", ", contacts)));
    }

    private void openSettings() {
        Toast.makeText(this, "Go to Firebase Console to add contacts", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        locationHelper.startLocationUpdates();
        shakeDetector.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationHelper.stopLocationUpdates();
        shakeDetector.stop();
    }
}
