package com.example.acomics.model;

public class LibraryItem {
    private String id;
    private String name;
    private String pictureURL;
    private String t_id;

    public LibraryItem() {}  // Обязательный пустой конструктор

    public LibraryItem(String id, String name, String pictureURL, String t_id) {
        this.id = id;
        this.name = name;
        this.pictureURL = pictureURL;
        this.t_id = t_id;
    }

    // Геттеры и сеттеры
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPictureURL() {
        return pictureURL;
    }

    public void setPictureURL(String pictureURL) {
        this.pictureURL = pictureURL;
    }

    public String getT_id() {
        return t_id;
    }

    public void setT_id(String t_id) {
        this.t_id = t_id;
    }
}