package com.example.offlineemergencyapp.services;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class EmergencyContactService {

    private final FirebaseFirestore db;
    private final CollectionReference contactsRef;
    private final ContactsCallback callback;

    public EmergencyContactService(ContactsCallback callback) {
        this.db = FirebaseFirestore.getInstance();
        this.contactsRef = db.collection("emergency_contacts"); // Firestore Collection
        this.callback = callback;
    }

    public void fetchEmergencyContacts() {
        contactsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<String> contacts = new ArrayList<>();
                task.getResult().forEach(document -> contacts.add(document.getString("phone_number")));
                callback.onContactsReceived(contacts);
            } else {
                Log.e("Firebase", "Failed to fetch contacts", task.getException());
                callback.onContactsReceived(new ArrayList<>());
            }
        });
    }

    public interface ContactsCallback {
        void onContactsReceived(List<String> contacts);
    }
}
