package com.example.acomics.view.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.acomics.R;
import com.example.acomics.model.Title;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

public class TitleActivity extends AppCompatActivity {
    private static final String TAG = "TitleActivity";
    private FirebaseFirestore db;
    private Button bInfo, bChapters, bChats, bRead;
    private LinearLayout infoPage, chaptersPage, chatsPage;
    private String t_id;
    private boolean hasChapters = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title);

        // Инициализация кнопки чтения
        bRead = findViewById(R.id.b_read);
        bRead.setVisibility(View.GONE); // По умолчанию скрыта

        // Проверяем, найдена ли кнопка
        if (bRead == null) {
            Log.e(TAG, "b_read button not found!");
        } else {
            Log.d(TAG, "b_read button initialized");
        }

        db = FirebaseFirestore.getInstance();
        t_id = getIntent().getStringExtra("t_id");

        if (t_id != null && !t_id.isEmpty()) {
            // Загружаем данные тайтла
            loadTitleData(t_id);

            // Проверяем наличие глав
            checkChaptersExistence();
        } else {
            Log.e(TAG, "t_id is null or empty");
            Toast.makeText(this, "Ошибка: t_id не предоставлен", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Обработчик кнопки чтения
        bRead.setOnClickListener(v -> {
            Log.d(TAG, "bRead clicked");
            if (hasChapters) {
                openFirstChapter();
            } else {
                Toast.makeText(this, "Нет доступных глав", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkChaptersExistence() {
        Log.d(TAG, "Checking chapters for t_id: " + t_id);

        db.collection("chapters")
                .whereEqualTo("t_id", t_id)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        hasChapters = !task.getResult().isEmpty();
                        Log.d(TAG, "Chapters exist: " + hasChapters);

                        runOnUiThread(() -> {
                            if (bRead != null) {
                                bRead.setVisibility(hasChapters ? View.VISIBLE : View.GONE);
                                bRead.setEnabled(true); // Убедимся, что кнопка включена

                                // Временная проверка: меняем цвет для видимости
                                bRead.setBackgroundColor(Color.GREEN);
                            }
                        });
                    } else {
                        Log.e(TAG, "Error checking chapters", task.getException());
                    }
                });
    }

    private void openFirstChapter() {
        Log.d(TAG, "Opening first chapter for t_id: " + t_id);

        // Показать индикатор загрузки
        ProgressBar progressBar = new ProgressBar(this);
        progressBar.setVisibility(View.VISIBLE);

        db.collection("chapters")
                .whereEqualTo("t_id", t_id)
                .orderBy("chapter_number", Query.Direction.ASCENDING)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        QueryDocumentSnapshot document = (QueryDocumentSnapshot) task.getResult().getDocuments().get(0);

                        if (document.contains("imageURL") && document.contains("chapter_number")) {
                            String imageURL = document.getString("imageURL");
                            Long chapterNumberLong = document.getLong("chapter_number");
                            int chapterNumber = chapterNumberLong != null ? chapterNumberLong.intValue() : 1;

                            openReadActivity(imageURL, chapterNumber);
                        } else {
                            Log.e(TAG, "Chapter document missing required fields");
                            Toast.makeText(this, "Ошибка формата главы", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Error loading first chapter", task.getException());
                        Toast.makeText(this, "Ошибка загрузки главы", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openReadActivity(String imageURL, int chapterNumber) {
        Log.d(TAG, "Opening ReadActivity with chapter: " + chapterNumber);

        Intent intent = new Intent(this, ReadActivity.class);
        intent.putExtra("imageURL", imageURL);
        intent.putExtra("chapterNumber", chapterNumber);
        intent.putExtra("t_id", t_id);
        startActivity(intent);
    }

    private void setupTabListeners() {
        // Проверка инициализации кнопок
        if (bInfo == null) Log.e("TitleActivity", "bInfo is null");
        if (bChapters == null) Log.e("TitleActivity", "bChapters is null");
        if (bChats == null) Log.e("TitleActivity", "bChats is null");

        bInfo.setOnClickListener(v -> showTab("INFO"));
        bChapters.setOnClickListener(v -> showTab("CHAPTERS"));
        bChats.setOnClickListener(v -> showTab("CHATS"));
    }

    private void showTab(String tab) {
        Log.d("TitleActivity", "Showing tab: " + tab);

        // Сброс цвета всех кнопок
        resetButtonColors();

        // Скрытие всех страниц
        hideAllPages();

        // Активация выбранной вкладки
        switch (tab) {
            case "INFO":
                bInfo.setTextColor(ContextCompat.getColor(this, R.color.dark_blue));
                infoPage.setVisibility(View.VISIBLE);
                break;
            case "CHAPTERS":
                bChapters.setTextColor(ContextCompat.getColor(this, R.color.dark_blue));
                chaptersPage.setVisibility(View.VISIBLE);
                break;
            case "CHATS":
                bChats.setTextColor(ContextCompat.getColor(this, R.color.dark_blue));
                chatsPage.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void resetButtonColors() {
        int blackColor = ContextCompat.getColor(this, R.color.black);
        bInfo.setTextColor(blackColor);
        bChapters.setTextColor(blackColor);
        bChats.setTextColor(blackColor);
    }

    private void hideAllPages() {
        infoPage.setVisibility(View.GONE);
        chaptersPage.setVisibility(View.GONE);
        chatsPage.setVisibility(View.GONE);
    }

    private void loadTitleData(String t_id) {
        Log.d("TitleActivity", "Loading title data for ID: " + t_id);

        db.collection("titles").document(t_id)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Title title = task.getResult().toObject(Title.class);
                        if (title != null) {
                            Log.d("TitleActivity", "Title loaded: " + title.getName());
                            updateUI(title);
                            // По умолчанию показываем вкладку INFO
                            showTab("INFO");
                        } else {
                            Log.e("TitleActivity", "Title object is null");
                        }
                    } else {
                        String error = task.getException() != null ?
                                task.getException().getMessage() : "Unknown error";
                        Log.e("TitleActivity", "Error loading title: " + error);
                        Toast.makeText(this,
                                "Error loading title: " + error,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @SuppressLint("SetTextI18n")
    private void updateUI(Title title) {
        // Находим все View элементы
        TextView tComicsName = findViewById(R.id.t_comics_name);
        TextView tDate = findViewById(R.id.t_date);
        TextView tType = findViewById(R.id.t_type);
        TextView tDescription = findViewById(R.id.t_description);
        TextView tGenres = findViewById(R.id.t_genres);
        TextView tTags = findViewById(R.id.t_tags);
        TextView tAuthors = findViewById(R.id.t_authors);
        ImageView titlePicture = findViewById(R.id.title_image);

        // Проверка инициализации элементов
        if (tComicsName == null) Log.e("TitleActivity", "tComicsName not found");
        if (tDate == null) Log.e("TitleActivity", "tDate not found");
        if (tType == null) Log.e("TitleActivity", "tType not found");
        if (tDescription == null) Log.e("TitleActivity", "tDescription not found");
        if (tGenres == null) Log.e("TitleActivity", "tGenres not found");
        if (tTags == null) Log.e("TitleActivity", "tTags not found");
        if (tAuthors == null) Log.e("TitleActivity", "tAuthors not found");
        if (titlePicture == null) Log.e("TitleActivity", "titlePicture not found");

        // Устанавливаем данные
        tComicsName.setText(title.getName());
        tDate.setText(title.getDate());
        tType.setText(title.getType());
        tDescription.setText(title.getDescription());
        tGenres.setText("Жанры: " + title.getGenres());
        tTags.setText("Теги: " + title.getTags());
        tAuthors.setText(title.getAuthors());

        // Загружаем изображение
        if (title.getPictureURL() != null && !title.getPictureURL().isEmpty()) {
            Log.d("TitleActivity", "Loading image: " + title.getPictureURL());
            Picasso.get()
                    .load(title.getPictureURL())
                    .placeholder(R.drawable.none_title_image)
                    .error(R.drawable.none_title_image)
                    .into(titlePicture, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            Log.d("TitleActivity", "Image loaded successfully");
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e("TitleActivity", "Error loading image", e);
                        }
                    });
        } else {
            Log.w("TitleActivity", "Empty image URL, using placeholder");
            titlePicture.setImageResource(R.drawable.none_title_image);
        }
    }
}