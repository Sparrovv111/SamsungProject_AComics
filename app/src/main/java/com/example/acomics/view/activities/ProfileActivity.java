package com.example.acomics.view.activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.acomics.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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

        usernameView.setText(user.getDisplayName() != null ? user.getDisplayName() : "No username");
        emailView.setText("Почта: " + user.getEmail());

        // Форматируем дату регистрации
        long creationTimestamp = user.getMetadata().getCreationTimestamp();
        String formattedDate = formatRegistrationDate(creationTimestamp);
        registrationDateView.setText("Дата регистрации: " + formattedDate);
    }

    private String formatRegistrationDate(long timestamp) {
        try {
            // Создаем объект Date из timestamp
            Date date = new Date(timestamp);

            // Форматируем дату в нужный формат
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            return sdf.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return "Неизвестная дата";
        }
    }
}