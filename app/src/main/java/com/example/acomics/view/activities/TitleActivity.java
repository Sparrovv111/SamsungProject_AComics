package com.example.acomics.view.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.acomics.R;
import com.example.acomics.model.Title;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

public class TitleActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private Button bInfo, bChapters, bChats;
    private LinearLayout infoPage, chaptersPage, chatsPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title);

        // Инициализация элементов UI
        bInfo = findViewById(R.id.b_info);
        bChapters = findViewById(R.id.b_chapters);
        bChats = findViewById(R.id.b_chats);
        infoPage = findViewById(R.id.info_page);
        chaptersPage = findViewById(R.id.chapters_page);
        chatsPage = findViewById(R.id.chats_page);

        // Установка обработчиков кнопок
        setupTabListeners();

        db = FirebaseFirestore.getInstance();
        String t_id = getIntent().getStringExtra("t_id");

        if (t_id != null && !t_id.isEmpty()) {
            loadTitleData(t_id);
        } else {
            Toast.makeText(this, "t_id not provided", Toast.LENGTH_SHORT).show();
            finish();
        }
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