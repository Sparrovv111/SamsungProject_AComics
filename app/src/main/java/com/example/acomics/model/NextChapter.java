package com.example.acomics.model;

public class NextChapter {
    private String id;
    private String tid;
    private String name;
    private String pictureURL;

    public NextChapter() {}

    public NextChapter(String id, String name, String pictureURL) {
        this.id = id;
        this.tid = tid;
        this.name = name;
        this.pictureURL = pictureURL;
    }

    // Геттеры и сеттеры
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTid() {
        return tid;
    }

    public void setT_id(String tid) {
        this.tid = tid;
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
}