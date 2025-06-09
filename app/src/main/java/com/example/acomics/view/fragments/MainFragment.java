package com.example.acomics.view.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.acomics.R;
import com.example.acomics.model.DatabaseManager;
import com.example.acomics.model.LastChapter;
import com.example.acomics.model.LibraryItem;
import com.example.acomics.model.NextChapter;
import com.example.acomics.model.Title;
import com.example.acomics.model.User;
import com.example.acomics.view.activities.AuthActivity;
import com.example.acomics.view.activities.EditProfileActivity;
import com.example.acomics.view.activities.TitleActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainFragment extends Fragment {

    private FirebaseFirestore db;

    private static final int EDIT_PROFILE_REQUEST = 1001;

    private String currentTitleId;

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
    private DatabaseReference userRef;
    private ValueEventListener userListener;

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
    private Screen previousScreen = Screen.HOME;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
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
            openProfileScreen();
        } else {
            openLoginScreen();
        }
    }

    private void openProfileScreen() {
        showScreen(Screen.PROFILE);
        fillProfileData();
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
            if (id == R.id.menu_refresh) {
                refreshProfile();
                return true;
            } else if (id == R.id.menu_edit_profile) {
                openEditProfile();
                return true;
            } else if (id == R.id.menu_logout) {
                logoutUser();
                return true;
            }
            return false;
        });

        menuButton.setOnClickListener(v -> popupMenu.show());

        // Обновляем данные аутентификации
        updateAuthProfileViews(profileView, user);

        // Загружаем дополнительные данные из базы
        loadUserDataFromDatabase(user.getUid(), profileView);
    }

    private void updateAuthProfileViews(View profileView, FirebaseUser user) {
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

    private void loadUserDataFromDatabase(String userId, View profileView) {
        // Удаляем предыдущий слушатель, если был
        if (userRef != null && userListener != null) {
            userRef.removeEventListener(userListener);
        }

        // Получаем ссылку на данные пользователя
        userRef = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(userId);

        userListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    updateDatabaseProfileViews(profileView, snapshot);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(),
                        "Ошибка загрузки данных: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        };

        userRef.addValueEventListener(userListener);
    }

    private void updateDatabaseProfileViews(View profileView, DataSnapshot snapshot) {
        TextView aboutMe = profileView.findViewById(R.id.about_me);
        TextView kolLikes = profileView.findViewById(R.id.kol_likes);
        TextView kolLooks = profileView.findViewById(R.id.kol_looks);
        TextView post = profileView.findViewById(R.id.post);

        // Получаем данные из снимка
        String aboutMeValue = snapshot.child("aboutMe").getValue(String.class);
        Integer likesCount = snapshot.child("likesCount").getValue(Integer.class);
        Integer viewsCount = snapshot.child("viewsCount").getValue(Integer.class);
        String position = snapshot.child("position").getValue(String.class);

        // Устанавливаем значения
        aboutMe.setText("О себе: " + (aboutMeValue != null ? aboutMeValue : ""));
        kolLikes.setText("Лайков поставлено: " + (likesCount != null ? likesCount : 0));
        kolLooks.setText("Всего просмотрено: " + (viewsCount != null ? viewsCount : 0));
        post.setText("Должность: " + (position != null ? position : "Читатель"));
    }

    private void refreshProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        user.reload().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                View profileView = profileLayout.getChildAt(0);
                if (profileView != null) {
                    // Обновляем данные аутентификации
                    updateAuthProfileViews(profileView, user);

                    // Перезагружаем данные из базы
                    if (userRef != null) {
                        userRef.get().addOnCompleteListener(dbTask -> {
                            if (dbTask.isSuccessful() && dbTask.getResult() != null) {
                                updateDatabaseProfileViews(profileView, dbTask.getResult());
                            }
                        });
                    }

                    Toast.makeText(requireContext(), "Профиль обновлен", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(requireContext(),
                        "Ошибка обновления: " + task.getException().getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void updateProfileViews(View profileView, User userData) {
        FirebaseUser authUser = mAuth.getCurrentUser();
        if (authUser == null) return;

        TextView username = profileView.findViewById(R.id.username);
        TextView email = profileView.findViewById(R.id.email);
        TextView registrationDate = profileView.findViewById(R.id.date_of_register);
        TextView aboutMe = profileView.findViewById(R.id.about_me);

        // Устанавливаем данные пользователя
        username.setText(authUser.getDisplayName() != null ?
                authUser.getDisplayName() : authUser.getEmail());
        email.setText("Почта: " + authUser.getEmail());

        // Форматируем дату регистрации
        long creationTimestamp = authUser.getMetadata().getCreationTimestamp();
        String formattedDate = formatRegistrationDate(creationTimestamp);
        registrationDate.setText("Дата регистрации: " + formattedDate);

        // Устанавливаем "О себе" из базы данных
        aboutMe.setText("О себе: " + userData.getAboutMe());
    }

    private void openEditProfile() {
        Intent intent = new Intent(getActivity(), EditProfileActivity.class);
        startActivityForResult(intent, EDIT_PROFILE_REQUEST);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_PROFILE_REQUEST && resultCode == Activity.RESULT_OK) {
            refreshProfile();
        }
    }

    private void loadHomeData() {
        // Получаем корневой View главной страницы
        View homeView = homeLayout.getChildAt(0);
        if (homeView == null) return;

        // Загружаем данные из Firestore
        db.collection("next_chapters")
                .limit(3) // Ограничиваем 3 элементами
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<NextChapter> chapters = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            NextChapter chapter = document.toObject(NextChapter.class);
                            chapter.setId(document.getId()); // Устанавливаем ID документа
                            chapters.add(chapter);
                        }
                        updateNextChapterViews(homeView, chapters);
                    } else {
                        Toast.makeText(requireContext(),
                                "Ошибка загрузки глав",
                                Toast.LENGTH_SHORT).show();
                    }
                });
        db.collection("last_chapters")
                .limit(3)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            Log.w("MainFragment", "last_chapters collection is empty");
                            Toast.makeText(requireContext(), "Нет данных об обновлениях", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d("MainFragment", "Loaded last_chapters: " + task.getResult().size() + " items");
                            List<LastChapter> lastChapters = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("MainFragment", "Document ID: " + document.getId());
                                LastChapter chapter = document.toObject(LastChapter.class);
                                chapter.setId(document.getId());
                                lastChapters.add(chapter);
                            }
                            updateLastChapterViews(homeView, lastChapters);
                        }
                    } else {
                        Log.e("MainFragment", "Error loading last_chapters", task.getException());
                        Toast.makeText(requireContext(),
                                "Ошибка загрузки обновлений: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadLibraryData() {
        View libraryView = libraryLayout.getChildAt(0);
        if (libraryView == null) return;

        db.collection("library")
                .limit(3)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<LibraryItem> items = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            LibraryItem item = document.toObject(LibraryItem.class);
                            item.setId(document.getId());
                            items.add(item);
                        }
                        updateLibraryViews(libraryView, items);
                    } else {
                        Toast.makeText(requireContext(),
                                "Ошибка загрузки библиотеки",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateLibraryViews(View libraryView, List<LibraryItem> items) {
        ImageView imageView1 = libraryView.findViewById(R.id.l_ch_1);
        ImageView imageView2 = libraryView.findViewById(R.id.l_ch_2);
        ImageView imageView3 = libraryView.findViewById(R.id.l_ch_3);

        ImageView[] imageViews = {imageView1, imageView2, imageView3};

        for (int i = 0; i < 3; i++) {
            if (i < items.size()) {
                LibraryItem item = items.get(i);
                String imageUrl = item.getPictureURL();
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Picasso.get()
                            .load(imageUrl)
                            .placeholder(R.drawable.none)
                            .error(R.drawable.none)
                            .into(imageViews[i]);
                } else {
                    imageViews[i].setImageResource(R.drawable.none);
                }
                imageViews[i].setOnClickListener(v -> {
                    if (item.getT_id() != null && !item.getT_id().isEmpty()) {
                        showTitleScreen(item.getT_id());
                    }
                });
            } else {
                imageViews[i].setImageResource(R.drawable.none);
            }
        }
    }

    private void updateNextChapterViews(View homeView, List<NextChapter> chapters) {
        for (int i = 0; i < Math.min(chapters.size(), 3); i++) {
            NextChapter chapter = chapters.get(i);
            int frameLayoutId, imageViewId, textViewId;

            switch (i) {
                case 0:
                    frameLayoutId = R.id.n_ch_1;
                    imageViewId = R.id.u_comics_background_1;
                    textViewId = R.id.u_comics_text_1;
                    break;
                case 1:
                    frameLayoutId = R.id.n_ch_2;
                    imageViewId = R.id.u_comics_background_2;
                    textViewId = R.id.u_comics_text_2;
                    break;
                case 2:
                    frameLayoutId = R.id.n_ch_3;
                    imageViewId = R.id.u_comics_background_3;
                    textViewId = R.id.u_comics_text_3;
                    break;
                default:
                    continue;
            }

            FrameLayout frameLayout = homeView.findViewById(frameLayoutId);
            ImageView imageView = frameLayout.findViewById(imageViewId);
            TextView textView = frameLayout.findViewById(textViewId);

            // Устанавливаем название главы
            textView.setText(chapter.getName());

            // Загружаем изображение с помощью Picasso
            if (chapter.getPictureURL() != null && !chapter.getPictureURL().isEmpty()) {
                Picasso.get()
                        .load(chapter.getPictureURL())
                        .placeholder(R.drawable.none) // Заглушка
                        .error(R.drawable.none) // Изображение при ошибке
                        .into(imageView);
            }
            else {
                imageView.setImageResource(R.drawable.none);
            }

            frameLayout.setOnClickListener(v -> {
                if (chapter.getTid() != null && !chapter.getTid().isEmpty()) {
                    Log.d("TitleDebug", "Opening title with t_id: " + chapter.getTid());
                    showTitleScreen(chapter.getTid());
                } else {
                    Log.e("TitleDebug", "t_id is missing for chapter: " + chapter.getName());
                }
            });
        }
    }

    private void updateLastChapterViews(View homeView, List<LastChapter> chapters) {
        Log.d("MainFragment", "Updating last chapter views. Chapters count: " + chapters.size());

        for (int i = 0; i < Math.min(chapters.size(), 3); i++) {
            LastChapter chapter = chapters.get(i);
            Log.d("MainFragment", "Processing chapter: " + chapter.getName());

            int frameLayoutId, imageViewId, textViewId;

            switch (i) {
                case 0:
                    frameLayoutId = R.id.u_ch_1;
                    imageViewId = R.id.n_comics_background_1;
                    textViewId = R.id.n_comics_text_1;
                    break;
                case 1:
                    frameLayoutId = R.id.u_ch_2;
                    imageViewId = R.id.n_comics_background_2;
                    textViewId = R.id.n_comics_text_2;
                    break;
                case 2:
                    frameLayoutId = R.id.u_ch_3;
                    imageViewId = R.id.n_comics_background_3;
                    textViewId = R.id.n_comics_text_3;
                    break;
                default:
                    continue;
            }

            FrameLayout frameLayout = homeView.findViewById(frameLayoutId);
            if (frameLayout == null) {
                Log.e("MainFragment", "FrameLayout not found for index: " + i);
                continue;
            }

            ImageView imageView = frameLayout.findViewById(imageViewId);
            TextView textView = frameLayout.findViewById(textViewId);

            if (imageView == null || textView == null) {
                Log.e("MainFragment", "ImageView or TextView not found for index: " + i);
                continue;
            }

            // Устанавливаем название главы
            textView.setText(chapter.getName());
            Log.d("MainFragment", "Set text: " + chapter.getName());

            // Загружаем изображение
            String imageUrl = chapter.getPictureURL();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Log.d("MainFragment", "Loading image: " + imageUrl);
                Picasso.get()
                        .load(imageUrl)
                        .placeholder(R.drawable.none)
                        .error(R.drawable.none)
                        .into(imageView, new Callback() {
                            @Override
                            public void onSuccess() {
                                Log.d("MainFragment", "Image loaded successfully");
                            }

                            @Override
                            public void onError(Exception e) {
                                Log.e("MainFragment", "Error loading image", e);
                            }
                        });
            } else {
                Log.w("MainFragment", "Empty image URL, using placeholder");
                imageView.setImageResource(R.drawable.none);
            }

            frameLayout.setOnClickListener(v -> {
                if (chapter.getT_id() != null && !chapter.getT_id().isEmpty()) {
                    showTitleScreen(chapter.getT_id());
                }
            });
        }
    }

    private void openTitleActivity(String t_id) {
        Intent intent = new Intent(getActivity(), TitleActivity.class);
        intent.putExtra("t_id", t_id);
        startActivity(intent);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("currentTitleId", currentTitleId);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            currentTitleId = savedInstanceState.getString("currentTitleId");
        }
    }

    private void showTitleScreen(String t_id) {
        currentTitleId = t_id;
        showScreen(Screen.TITLE);
    }

    private void loadTitleData() {

        if (currentTitleId == null || currentTitleId.isEmpty()) {
            Log.e("TitleDebug", "currentTitleId is null or empty");
            return;
        }

        Log.d("TitleDebug", "Loading title data for t_id: " + currentTitleId);

        View titleView = titleLayout.getChildAt(0);
        if (titleView == null) {
            Log.e("TitleDebug", "titleView is null");
            return;
        }

        // Используем запрос вместо прямого доступа по ID документа
        db.collection("titles")
                .whereEqualTo("t_id", currentTitleId) // Ищем по полю t_id
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            QueryDocumentSnapshot document = (QueryDocumentSnapshot) task.getResult().getDocuments().get(0);
                            Title title = document.toObject(Title.class);
                            Log.d("TitleDebug", "Title found: " + title.getName());
                            updateTitleView(titleView, title);
                        } else {
                            Log.e("TitleDebug", "No document with t_id: " + currentTitleId);
                            Toast.makeText(requireContext(),
                                    "Данные не найдены",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("TitleDebug", "Error loading title: " + task.getException());
                        Toast.makeText(requireContext(),
                                "Ошибка загрузки: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @SuppressLint("SetTextI18n")
    private void updateTitleView(View titleView, Title title) {
        TextView tComicsName = titleView.findViewById(R.id.t_comics_name);
        TextView tDate = titleView.findViewById(R.id.t_date);
        TextView tType = titleView.findViewById(R.id.t_type);
        TextView tDescription = titleView.findViewById(R.id.t_description);
        TextView tGenres = titleView.findViewById(R.id.t_genres);
        TextView tTags = titleView.findViewById(R.id.t_tags);
        TextView tAuthors = titleView.findViewById(R.id.t_authors);
        ImageView titlePicture = titleView.findViewById(R.id.title_image);

        // Устанавливаем данные
        tComicsName.setText(title.getName());
        tDate.setText(title.getDate());
        tType.setText(title.getType());
        tDescription.setText(title.getDescription());
        tGenres.setText("Жанры: " + title.getGenres());
        tTags.setText("Теги: " + title.getTags());
        tAuthors.setText(title.getAuthors());

        // Загружаем изображение
        if (titlePicture == null) {
            Log.e("TitleUI", "title_picture not found");
        } else {
            if (title.getPictureURL() != null && !title.getPictureURL().isEmpty()) {
                Picasso.get()
                        .load(title.getPictureURL())
                        .placeholder(R.drawable.none_title_image)
                        .error(R.drawable.none_title_image)
                        .into(titlePicture);
                Log.d("TitleUI", "Image loading started");
            } else {
                titlePicture.setImageResource(R.drawable.none_title_image);
                Log.d("TitleUI", "Using placeholder image");
            }
        }
    }

    public void handleBackPressed() {
        if (currentScreen == Screen.TITLE) {
            showScreen(previousScreen); // Вернуться на предыдущий экран
        } else {
            requireActivity().finish(); // Закрыть приложение
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

        if (screen == Screen.HOME) {
            homeLayout.setVisibility(View.VISIBLE);
            loadHomeData(); // Загружаем данные для главной страницы
        }

        if (screen != Screen.TITLE) {
            previousScreen = currentScreen;
        }

        if (screen == Screen.LIBRARY) {
            // Загружаем макет если он ещё не загружен
            if (libraryLayout.getChildCount() == 0) {
                LayoutInflater.from(requireContext())
                        .inflate(R.layout.activity_library, libraryLayout, true);
            }
            libraryLayout.setVisibility(View.VISIBLE);
            loadLibraryData(); // Загрузка данных библиотеки
        }

        updateButtonStates(screen);

        switch (screen) {
            case HOME:
                homeLayout.setVisibility(View.VISIBLE);
                break;
            case LIBRARY:
                if (libraryLayout.getChildCount() == 0) {
                    LayoutInflater.from(requireContext())
                            .inflate(R.layout.activity_library, libraryLayout, true);
                }
                libraryLayout.setVisibility(View.VISIBLE);
                loadLibraryData(); // Загружаем данные
                break;
            case CHATS:
                chatsLayout.setVisibility(View.VISIBLE);
                break;
            case NEWS:
                newsLayout.setVisibility(View.VISIBLE);
                break;
            case TITLE:
                if (titleLayout.getChildCount() == 0) {
                    LayoutInflater.from(requireContext())
                            .inflate(R.layout.activity_title, titleLayout, true);
                }
                titleLayout.setVisibility(View.VISIBLE);
                loadTitleData(); // Загружаем данные для тайтла
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