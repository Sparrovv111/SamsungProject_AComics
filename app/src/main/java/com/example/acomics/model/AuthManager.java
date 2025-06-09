package com.example.acomics.model;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AuthManager {
    private final FirebaseAuth mAuth;
    private final Context context;
    private AuthListener authListener;
    private final DatabaseReference databaseReference;

    public AuthManager(Context context) {
        this.context = context;
        this.mAuth = FirebaseAuth.getInstance();
        this.databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    public void setAuthListener(AuthListener authListener) {
        this.authListener = authListener;
    }

    public void registerUser(String username, String email, String password, String confirmPassword) {
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            showToast("Заполните все поля");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showToast("Пароли не совпадают");
            return;
        }

        if (password.length() < 6) {
            showToast("Пароль должен быть не менее 6 символов");
            return;
        }

        if (username.length() < 3) {
            showToast("Имя пользователя должно быть не менее 3 символов");
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Обновляем профиль с username
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(username)
                                    .build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(updateTask -> {
                                        if (updateTask.isSuccessful()) {
                                            saveUserToDatabase(user, username);

                                            sendVerificationEmail(user);
                                            if (authListener != null) {
                                                authListener.onRegistrationSuccess(email, username); // Добавлен username
                                            }
                                        } else {
                                            if (authListener != null) {
                                                authListener.onAuthError(updateTask.getException().getMessage());
                                            }
                                        }
                                    });
                        }
                    } else {
                        if (authListener != null) {
                            authListener.onAuthError(task.getException().getMessage());
                        }
                    }
                });
    }

    private void saveUserToDatabase(FirebaseUser user, String username) {
        User newUser = new User(user.getUid(), user.getEmail(), username);
        databaseReference.child("users").child(user.getUid()).setValue(newUser.toMap())
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        showToast("Ошибка сохранения данных пользователя");
                    }
                });
    }

    private void sendVerificationEmail(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        showToast("Письмо подтверждения отправлено на " + user.getEmail());
                    } else {
                        if (authListener != null) {
                            authListener.onAuthError(task.getException().getMessage());
                        }
                    }
                });
    }

    public void loginUser(String email, String password) {
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            showToast("Заполните все поля");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            if (user.isEmailVerified()) {
                                if (authListener != null) {
                                    authListener.onLoginSuccess();
                                }
                            } else {
                                if (authListener != null) {
                                    authListener.onEmailVerificationRequired(email);
                                }
                            }
                        }
                    } else {
                        if (authListener != null) {
                            authListener.onAuthError(task.getException().getMessage());
                        }
                    }
                });
    }

    public void resetPassword(String email) {
        if (TextUtils.isEmpty(email)) {
            showToast("Введите email");
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (authListener != null) {
                            authListener.onPasswordResetSent();
                        }
                    } else {
                        if (authListener != null) {
                            authListener.onAuthError(task.getException().getMessage());
                        }
                    }
                });
    }

    public void checkEmailVerification(String email) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            if (authListener != null) {
                authListener.onAuthError("Пользователь не авторизован");
            }
            return;
        }
        verifyEmailAfterReload();
    }

    private void verifyEmailAfterReload() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.reload().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (user.isEmailVerified()) {
                        if (authListener != null) {
                            authListener.onEmailVerified();
                        }
                    } else {
                        if (authListener != null) {
                            authListener.onEmailNotVerified();
                        }
                    }
                } else {
                    if (authListener != null) {
                        authListener.onAuthError(task.getException().getMessage());
                    }
                }
            });
        }
    }

    public void changePassword(String currentPassword, String newPassword, String confirmPassword) {
        if (TextUtils.isEmpty(currentPassword) ||
                TextUtils.isEmpty(newPassword) ||
                TextUtils.isEmpty(confirmPassword)) {
            showToast("Заполните все поля");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showToast("Пароли не совпадают");
            return;
        }

        if (newPassword.length() < 6) {
            showToast("Пароль должен быть не менее 6 символов");
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            showToast("Пользователь не авторизован");
            return;
        }

        AuthCredential credential = EmailAuthProvider
                .getCredential(user.getEmail(), currentPassword);

        user.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        user.updatePassword(newPassword)
                                .addOnCompleteListener(updateTask -> {
                                    if (updateTask.isSuccessful()) {
                                        if (authListener != null) {
                                            authListener.onPasswordChanged();
                                        }
                                    } else {
                                        if (authListener != null) {
                                            authListener.onAuthError(updateTask.getException().getMessage());
                                        }
                                    }
                                });
                    } else {
                        if (authListener != null) {
                            authListener.onAuthError(task.getException().getMessage());
                        }
                    }
                });
    }

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public void updateProfile(String newUsername, String newEmail, AuthListener listener) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            listener.onAuthError("Пользователь не авторизован");
            return;
        }

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(newUsername)
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        updateUserEmail(user, newEmail, listener);
                    } else {
                        listener.onAuthError(task.getException().getMessage());
                    }
                });
    }

    private void updateUserEmail(FirebaseUser user, String newEmail, AuthListener listener) {
        if (newEmail.equals(user.getEmail())) {
            listener.onProfileUpdated();
            return;
        }

        user.updateEmail(newEmail)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        user.sendEmailVerification()
                                .addOnCompleteListener(verificationTask -> {
                                    if (verificationTask.isSuccessful()) {
                                        listener.onEmailUpdateSent();
                                    } else {
                                        listener.onAuthError(verificationTask.getException().getMessage());
                                    }
                                });
                    } else {
                        listener.onAuthError(task.getException().getMessage());
                    }
                });
    }

    public interface AuthListener {
        void onRegistrationSuccess(String email, String username);
        void onLoginSuccess();
        void onEmailVerificationRequired(String email);
        void onPasswordResetSent();
        void onEmailVerified();
        void onEmailNotVerified();
        void onPasswordChanged();
        void onAuthError(String errorMessage);
        void onProfileUpdated();
        void onEmailUpdateSent();
    }
}