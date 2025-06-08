package com.example.acomics.view.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.acomics.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class EditProfileActivity extends AppCompatActivity {

    private EditText editUsername, editEmail, editAboutMe;
    private Button buttonSave, buttonChangePassword;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            finish();
            return;
        }

        editUsername = findViewById(R.id.edit_username);
        editEmail = findViewById(R.id.edit_email);
        editAboutMe = findViewById(R.id.edit_about_me);
        buttonSave = findViewById(R.id.buttonSaveProfile);
        buttonChangePassword = findViewById(R.id.buttonChangePassword);

        // Заполняем текущие данные
        editUsername.setText(currentUser.getDisplayName());
        editEmail.setText(currentUser.getEmail());

        buttonSave.setOnClickListener(v -> updateProfile());
        buttonChangePassword.setOnClickListener(v -> openChangePassword());

        findViewById(R.id.button_back).setOnClickListener(v -> onBackPressed());
    }

    private void updateProfile() {
        String username = editUsername.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String aboutMe = editAboutMe.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            editUsername.setError("Введите имя пользователя");
            return;
        }

        // Обновляем имя пользователя
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .build();

        currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        updateEmail(email);
                        setResult(RESULT_OK);
                    } else {
                        Toast.makeText(EditProfileActivity.this,
                                "Ошибка обновления имени: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateEmail(String email) {
        if (email.equals(currentUser.getEmail())) {
            Toast.makeText(this, "Профиль обновлен", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentUser.updateEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Отправляем письмо подтверждения
                        currentUser.sendEmailVerification()
                                .addOnCompleteListener(verificationTask -> {
                                    if (verificationTask.isSuccessful()) {
                                        Toast.makeText(EditProfileActivity.this,
                                                "Проверьте новый email для подтверждения",
                                                Toast.LENGTH_LONG).show();
                                        finish();
                                    } else {
                                        Toast.makeText(EditProfileActivity.this,
                                                "Ошибка отправки подтверждения: " + verificationTask.getException().getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(EditProfileActivity.this,
                                "Ошибка обновления email: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openChangePassword() {
        // Реализация смены пароля
        Toast.makeText(this, "Функция смены пароля", Toast.LENGTH_SHORT).show();
    }
}