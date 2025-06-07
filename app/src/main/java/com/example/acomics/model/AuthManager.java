package com.example.acomics.model;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class AuthManager {
    private final FirebaseAuth mAuth;
    private final Context context;
    private AuthListener authListener;

    public AuthManager(Context context) {
        this.context = context;
        this.mAuth = FirebaseAuth.getInstance();
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
            // Если пользователь вышел, пытаемся войти снова
            mAuth.signInWithEmailAndPassword(email, "temporaryPassword")
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            verifyEmailAfterReload();
                        } else {
                            if (authListener != null) {
                                authListener.onAuthError("Ошибка проверки подтверждения");
                            }
                        }
                    });
        } else {
            verifyEmailAfterReload();
        }
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

    public interface AuthListener {
        void onRegistrationSuccess(String email, String username);
        void onLoginSuccess();
        void onEmailVerificationRequired(String email);
        void onPasswordResetSent();
        void onEmailVerified();
        void onEmailNotVerified();
        void onPasswordChanged();
        void onAuthError(String errorMessage);
    }
}