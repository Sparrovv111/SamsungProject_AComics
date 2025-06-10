package com.example.acomics.view.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.acomics.R;
import com.squareup.picasso.Picasso;

import java.util.concurrent.atomic.AtomicReference;

public class ReadActivity extends AppCompatActivity {
    private static final String TAG = "ReadActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);

        ImageView tImage = findViewById(R.id.t_image);
        Button buttonReturn = findViewById(R.id.button_return);
        TextView chTextName = findViewById(R.id.ch_text_name);

        // Получаем данные из Intent
        String imageURL = getIntent().getStringExtra("imageURL");
        int chapterNumber = getIntent().getIntExtra("chapterNumber", 1);
        AtomicReference<String> t_id = new AtomicReference<>(getIntent().getStringExtra("t_id"));

        if (imageURL == null || t_id.get() == null) {
            Log.e(TAG, "Missing data in intent");
            Toast.makeText(this, "Ошибка загрузки главы", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Устанавливаем название главы
        chTextName.setText("Глава " + chapterNumber);

        // Загружаем изображение
        if (!imageURL.isEmpty()) {
            Picasso.get()
                    .load(imageURL)
                    .placeholder(R.drawable.p123)
                    .error(R.drawable.p123)
                    .into(tImage, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "Image loaded successfully");
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e(TAG, "Error loading image", e);
                            Toast.makeText(ReadActivity.this, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            tImage.setImageResource(R.drawable.p123);
            Log.w(TAG, "Empty image URL, using placeholder");
        }

        // Обработчик кнопки возврата
        buttonReturn.setOnClickListener(v -> {
            Log.d(TAG, "Return button clicked");

            t_id.set(getIntent().getStringExtra("t_id"));
            if (t_id.get() != null) {
                Intent intent = new Intent(this, TitleActivity.class);
                intent.putExtra("t_id", t_id.get());
                startActivity(intent);
                finish();
            } else {
                Log.e(TAG, "t_id is null on return");
                finish();
            }
        });
    }
}