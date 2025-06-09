package com.example.acomics.model;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DatabaseManager {
    private static DatabaseManager instance;
    private final DatabaseReference databaseReference;

    private DatabaseManager() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public void saveUser(User user) {
        if (user.getUid() != null) {
            databaseReference.child("users").child(user.getUid()).setValue(user.toMap());
        }
    }

    public void updateAboutMe(String userId, String aboutMe) {
        if (userId != null) {
            databaseReference.child("users").child(userId).child("aboutMe").setValue(aboutMe);
        }
    }

    public DatabaseReference getUserReference(String userId) {
        return databaseReference.child("users").child(userId);
    }
}