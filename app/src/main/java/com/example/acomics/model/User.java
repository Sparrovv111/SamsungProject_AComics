package com.example.acomics.model;

import androidx.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class User {
    private String uid;
    private String email;
    private String username;
    private String photoUrl;
    private long registrationDate;
    private long lastLogin;
    private boolean emailVerified;
    private String aboutMe;
    private int likesCount;
    private int viewsCount;
    private String position;

    // Обязательный пустой конструктор для Firebase
    public User() {
        // Пустой конструктор требуется для Firebase Data Snapshot
    }

    // Основной конструктор при регистрации
    public User(String uid, String email, String username) {
        this.uid = uid;
        this.email = email;
        this.username = username;
        this.registrationDate = System.currentTimeMillis();
        this.lastLogin = System.currentTimeMillis();
        this.emailVerified = false;
        this.aboutMe = "Расскажите о себе";
        this.likesCount = 0;
        this.viewsCount = 0;
        this.position = "Читатель";
    }

    // Метод для преобразования в Map (для Firebase)
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("email", email);
        result.put("username", username);
        result.put("photoUrl", photoUrl);
        result.put("registrationDate", registrationDate);
        result.put("lastLogin", lastLogin);
        result.put("emailVerified", emailVerified);
        result.put("aboutMe", aboutMe);
        result.put("likesCount", likesCount);
        result.put("viewsCount", viewsCount);
        result.put("position", position);
        return result;
    }

    // Геттеры и сеттеры
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public long getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(long registrationDate) {
        this.registrationDate = registrationDate;
    }

    public long getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(long lastLogin) {
        this.lastLogin = lastLogin;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getAboutMe() {
        return aboutMe;
    }

    public void setAboutMe(String aboutMe) {
        this.aboutMe = aboutMe;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    public int getViewsCount() {
        return viewsCount;
    }

    public void setViewsCount(int viewsCount) {
        this.viewsCount = viewsCount;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    // Дополнительные полезные методы
    @Exclude
    public void updateLastLogin() {
        this.lastLogin = System.currentTimeMillis();
    }

    @Exclude
    public String getInitials() {
        if (username == null || username.isEmpty()) {
            return "??";
        }
        String[] parts = username.split(" ");
        if (parts.length == 0) return "??";
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return (parts[0].charAt(0) + "" + parts[1].charAt(0)).toUpperCase();
    }

    @NonNull
    @Override
    public String toString() {
        return "User{" +
                "uid='" + uid + '\'' +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}