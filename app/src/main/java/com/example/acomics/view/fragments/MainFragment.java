package com.example.acomics.view.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.acomics.R;
import com.example.acomics.view.activities.AuthActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainFragment extends Fragment {

    // Layouts for different screens
    private FrameLayout homeLayout;
    private FrameLayout libraryLayout;
    private FrameLayout chatsLayout;
    private FrameLayout newsLayout;
    private FrameLayout titleLayout;
    private FrameLayout profileLayout;

    // Navigation buttons
    private Button homeButton;
    private Button libraryButton;
    private Button chatsButton;
    private Button profileButton;
    private FirebaseAuth mAuth;

    private View rootView;

    private enum Screen {
        HOME,
        LIBRARY,
        CHATS,
        NEWS,
        TITLE,
        LOGIN,
        PROFILE
    }

    private Screen currentScreen = Screen.HOME;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Инициализация ВСЕХ элементов перед использованием
        homeLayout = rootView.findViewById(R.id.home_layout);
        libraryLayout = rootView.findViewById(R.id.library_layout);
        chatsLayout = rootView.findViewById(R.id.chats_layout);
        newsLayout = rootView.findViewById(R.id.news_layout);
        titleLayout = rootView.findViewById(R.id.title_layout);
        profileLayout = rootView.findViewById(R.id.profile_layout);

        homeButton = rootView.findViewById(R.id.button_home);
        libraryButton = rootView.findViewById(R.id.button_library);
        chatsButton = rootView.findViewById(R.id.button_chats);
        profileButton = rootView.findViewById(R.id.button_profile);

        showScreen(Screen.HOME);
        setupListeners();

        return rootView;
    }

    private void setupListeners() {
        // Кнопки навигации
        if (homeButton == null || profileButton == null) return;

        homeButton.setOnClickListener(v -> showScreen(Screen.HOME));
        libraryButton.setOnClickListener(v -> showScreen(Screen.LIBRARY));
        chatsButton.setOnClickListener(v -> showScreen(Screen.CHATS));
        profileButton.setOnClickListener(v -> checkAuthAndOpenProfile());

        // Кнопки внутри контента
        View homeContent = homeLayout.getChildAt(0);
        if (homeContent != null) {
            View moreNewsButton = homeContent.findViewById(R.id.button_more_news);
            if (moreNewsButton != null) {
                moreNewsButton.setOnClickListener(v -> showScreen(Screen.NEWS));
            }
        }

        // Chat items (in chats screen)
        //rootView.findViewById(R.id.chat_avatar1).setOnClickListener(v -> showTitleScreen());

        // Comics items (in library screen)
//        rootView.findViewById(R.id.n_comics1).setOnClickListener(v -> showTitleScreen());
//        rootView.findViewById(R.id.n_comics2).setOnClickListener(v -> showTitleScreen());
        // Add more click listeners for other comics as needed
    }

    private void checkAuthAndOpenProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            // Пользователь авторизован - открываем профиль
            openProfileScreen();
        } else {
            // Пользователь не авторизован - открываем экран входа
            openLoginScreen();
        }
    }

    private void openProfileScreen() {
        // Вместо запуска Activity показываем экран профиля внутри фрагмента
        showScreen(Screen.PROFILE);
        fillProfileData(); // Заполняем данные профиля
    }

    private void openLoginScreen() {
        // Вариант 1: Открываем активность авторизации
        startActivity(new Intent(getActivity(), AuthActivity.class));

        // Вариант 2: Показываем фрагмент входа внутри MainFragment
        showScreen(Screen.LOGIN);
    }

    private void showTitleScreen() {
        showScreen(Screen.TITLE);
    }

    @SuppressLint("SetTextI18n")
    private void fillProfileData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        // Получаем корневой View профиля
        View profileView = profileLayout.getChildAt(0);
        if (profileView == null) return;

        // Находим кнопку меню
        Button menuButton = profileView.findViewById(R.id.p_button_menu);

        PopupMenu popupMenu = new PopupMenu(requireContext(), menuButton);

        popupMenu.inflate(R.menu.profile_menu);

        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_edit_profile) {
                openEditProfile();
                return true;
            } else if (id == R.id.menu_logout) {
                logoutUser();
                return true;
            }
            return false;
        });

        menuButton.setOnClickListener(v -> popupMenu.show());

        TextView username = profileView.findViewById(R.id.username);
        TextView email = profileView.findViewById(R.id.email);
        TextView registrationDate = profileView.findViewById(R.id.date_of_register);

        // Устанавливаем данные пользователя
        username.setText(user.getDisplayName() != null ? user.getDisplayName() : user.getEmail());
        email.setText("Почта: " + user.getEmail());

        // Форматируем дату регистрации
        long creationTimestamp = user.getMetadata().getCreationTimestamp();
        String formattedDate = formatRegistrationDate(creationTimestamp);
        registrationDate.setText("Дата регистрации: " + formattedDate);
    }

    private void openEditProfile() {
        // Реализация перехода на экран редактирования профиля
        Toast.makeText(requireContext(), "Редактирование профиля", Toast.LENGTH_SHORT).show();
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        if (getActivity() != null) {
            startActivity(new Intent(getActivity(), AuthActivity.class));
            getActivity().finish();
        }
    }

    private String formatRegistrationDate(long timestamp) {
        try {
            Date date = new Date(timestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            return sdf.format(date);
        } catch (Exception e) {
            return "Неизвестная дата";
        }
    }

    private void showScreen(Screen screen) {
        if (homeLayout == null || profileLayout == null) return;
        // Скрываем  все экраны
        homeLayout.setVisibility(View.GONE);
        libraryLayout.setVisibility(View.GONE);
        chatsLayout.setVisibility(View.GONE);
        newsLayout.setVisibility(View.GONE);
        titleLayout.setVisibility(View.GONE);
        profileLayout.setVisibility(View.GONE);

        updateButtonStates(screen);

        switch (screen) {
            case HOME:
                homeLayout.setVisibility(View.VISIBLE);
                break;
            case LIBRARY:
                libraryLayout.setVisibility(View.VISIBLE);
                break;
            case CHATS:
                chatsLayout.setVisibility(View.VISIBLE);
                break;
            case NEWS:
                newsLayout.setVisibility(View.VISIBLE);
                break;
            case TITLE:
                titleLayout.setVisibility(View.VISIBLE);
                break;
            case PROFILE:
                profileLayout.setVisibility(View.VISIBLE);
        }
        currentScreen = screen;
    }

    private void updateButtonStates(Screen screen) {
        homeButton.setBackgroundResource(R.drawable.icon_home);
        libraryButton.setBackgroundResource(R.drawable.icon_library);
        chatsButton.setBackgroundResource(R.drawable.icon_chats);
        profileButton.setBackgroundResource(R.drawable.icon_use);

        switch (screen) {
            case HOME:
                homeButton.setBackgroundResource(R.drawable.icon_home_active);
                break;
            case LIBRARY:
                libraryButton.setBackgroundResource(R.drawable.icon_library_activity);
                break;
            case CHATS:
                chatsButton.setBackgroundResource(R.drawable.icon_chats_active);
                break;
            case NEWS:
                break;
            case TITLE:
                break;
            case PROFILE:
                profileButton.setBackgroundResource(R.drawable.icon_use_active);
                break;
        }
    }
}