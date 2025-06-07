package com.example.acomics.view.activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.acomics.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            displayUserProfile(user);
        }
    }

    private void displayUserProfile(FirebaseUser user) {
        TextView usernameView = findViewById(R.id.username);
        TextView emailView = findViewById(R.id.email);
        TextView registrationDateView = findViewById(R.id.date_of_register);

        usernameView.setText(user.getDisplayName() != null ?user.getDisplayName() : "No username");

        emailView.setText("Почта: " + user.getEmail());

        registrationDateView.setText("Дата регистрации: " + new java.util.Date(user.getMetadata().getCreationTimestamp()).toString());

        // Здесь можно добавить загрузку дополнительных данных из базы
    }
}