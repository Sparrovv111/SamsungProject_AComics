package com.example.acomics.view.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.acomics.R;
import com.example.acomics.model.AuthManager;

public class LoginFragment extends Fragment implements AuthManager.AuthListener {

    // Views
    private LinearLayout loginLayout, registerLayout, forgotPasswordLayout, confirmEmailLayout, changePasswordLayout;
    private EditText usernameEditText, passwordEditText, emailRegisterEditText, passwordRegisterEditText,
            confirmPasswordRegisterEditText, emailForgotPasswordEditText, confirmationCodeEditText,
            currentPasswordEditText, newPasswordEditText, confirmNewPasswordEditText;
    private TextView registerTextView, forgotPasswordTextView, resendCodeTextView;
    private Button registerButton, resetPasswordButton, confirmEmailButton, changePasswordButton, backButton;
    private Button loginButton;

    private AuthManager authManager;
    private String userEmailForConfirmation;

    public enum Screen {
        LOGIN,
        REGISTER,
        FORGOT_PASSWORD,
        CONFIRM_EMAIL,
        CHANGE_PASSWORD
    }

    private Screen currentScreen = Screen.LOGIN;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authManager = new AuthManager(requireContext());
        authManager.setAuthListener(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);

        initViews(rootView);
        setupListeners();
        showScreen(currentScreen);

        return rootView;
    }

    private void initViews(View rootView) {
        // Инициализация всех layout
        loginLayout = rootView.findViewById(R.id.login_layout);
        registerLayout = rootView.findViewById(R.id.register_layout);
        forgotPasswordLayout = rootView.findViewById(R.id.forgot_password_layout);
        confirmEmailLayout = rootView.findViewById(R.id.confirm_email_layout);
        changePasswordLayout = rootView.findViewById(R.id.change_password_layout);

        // Инициализация элементов входа
        usernameEditText = rootView.findViewById(R.id.username);
        passwordEditText = rootView.findViewById(R.id.edit_username);
        registerTextView = rootView.findViewById(R.id.textViewRegister);
        forgotPasswordTextView = rootView.findViewById(R.id.textViewForgotPassword);
        loginButton = rootView.findViewById(R.id.buttonLogin);

        // Инициализация регистрации
        emailRegisterEditText = rootView.findViewById(R.id.editTextEmailRegister);
        passwordRegisterEditText = rootView.findViewById(R.id.editTextPasswordRegister);
        confirmPasswordRegisterEditText = rootView.findViewById(R.id.editTextConfirmPasswordRegister);
        registerButton = rootView.findViewById(R.id.buttonRegister);

        // Инициализация восстановления пароля
        emailForgotPasswordEditText = rootView.findViewById(R.id.email);
        resetPasswordButton = rootView.findViewById(R.id.buttonResetPassword);

        // Инициализация подтверждения email
        confirmationCodeEditText = rootView.findViewById(R.id.editTextConfirmationCode);
        confirmEmailButton = rootView.findViewById(R.id.buttonConfirmEmail);
        resendCodeTextView = rootView.findViewById(R.id.textViewResendCode);

        // Инициализация смены пароля
        currentPasswordEditText = rootView.findViewById(R.id.editTextCurrentPassword);
        newPasswordEditText = rootView.findViewById(R.id.editTextNewPassword);
        confirmNewPasswordEditText = rootView.findViewById(R.id.editTextConfirmNewPassword);
        changePasswordButton = rootView.findViewById(R.id.buttonChangePasswordConfirm);

        // Кнопка назад
        backButton = rootView.findViewById(R.id.button_back);
    }

    private void setupListeners() {
        // Переход к регистрации
        registerTextView.setOnClickListener(v -> showScreen(Screen.REGISTER));

        // Переход к восстановлению пароля
        forgotPasswordTextView.setOnClickListener(v -> showScreen(Screen.FORGOT_PASSWORD));

        // Кнопка назад
        backButton.setOnClickListener(v -> {
            if (currentScreen != Screen.LOGIN) {
                showScreen(Screen.LOGIN);
            } else {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            }
        });

        // Регистрация пользователя
        registerButton.setOnClickListener(v -> {
            String email = emailRegisterEditText.getText().toString().trim();
            String password = passwordRegisterEditText.getText().toString().trim();
            String confirmPassword = confirmPasswordRegisterEditText.getText().toString().trim();
            authManager.registerUser(email, password, confirmPassword);
        });

        // Сброс пароля
        resetPasswordButton.setOnClickListener(v -> {
            String email = emailForgotPasswordEditText.getText().toString().trim();
            authManager.resetPassword(email);
        });

        // Повторная отправка кода подтверждения
        resendCodeTextView.setOnClickListener(v -> {
            // Реализуем в AuthListener
            if (userEmailForConfirmation != null) {
                authManager.checkEmailVerification(userEmailForConfirmation);
            }
        });

        // Вход пользователя
        loginButton.setOnClickListener(v -> {
            String email = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            authManager.loginUser(email, password);
        });

        // Подтверждение email
        confirmEmailButton.setOnClickListener(v -> {
            if (userEmailForConfirmation != null) {
                authManager.checkEmailVerification(userEmailForConfirmation);
            }
        });

        // Смена пароля
        changePasswordButton.setOnClickListener(v -> {
            String currentPassword = currentPasswordEditText.getText().toString().trim();
            String newPassword = newPasswordEditText.getText().toString().trim();
            String confirmPassword = confirmNewPasswordEditText.getText().toString().trim();
            authManager.changePassword(currentPassword, newPassword, confirmPassword);
        });
    }

    // Реализация AuthListener
    @Override
    public void onRegistrationSuccess(String email) {
        userEmailForConfirmation = email;
        showScreen(Screen.CONFIRM_EMAIL);
    }

    @Override
    public void onLoginSuccess() {
        if (getActivity() != null) {
            getActivity().onBackPressed();
        }
    }

    @Override
    public void onEmailVerificationRequired(String email) {
        userEmailForConfirmation = email;
        showScreen(Screen.CONFIRM_EMAIL);
    }

    @Override
    public void onPasswordResetSent() {
        showScreen(Screen.LOGIN);
    }

    @Override
    public void onEmailVerified() {
        if (currentScreen == Screen.REGISTER) {
            showScreen(Screen.LOGIN);
        } else {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        }
    }

    @Override
    public void onEmailNotVerified() {
        // Можно показать сообщение
    }

    @Override
    public void onPasswordChanged() {
        showScreen(Screen.LOGIN);
    }

    @Override
    public void onAuthError(String errorMessage) {
        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
    }

    private void showScreen(Screen screen) {
        // Скрыть все экраны
        loginLayout.setVisibility(View.GONE);
        registerLayout.setVisibility(View.GONE);
        forgotPasswordLayout.setVisibility(View.GONE);
        confirmEmailLayout.setVisibility(View.GONE);
        changePasswordLayout.setVisibility(View.GONE);

        // Показать запрошенный экран
        switch (screen) {
            case LOGIN:
                loginLayout.setVisibility(View.VISIBLE);
                break;
            case REGISTER:
                registerLayout.setVisibility(View.VISIBLE);
                break;
            case FORGOT_PASSWORD:
                forgotPasswordLayout.setVisibility(View.VISIBLE);
                break;
            case CONFIRM_EMAIL:
                confirmEmailLayout.setVisibility(View.VISIBLE);
                break;
            case CHANGE_PASSWORD:
                changePasswordLayout.setVisibility(View.VISIBLE);
                break;
        }

        currentScreen = screen;
    }
}