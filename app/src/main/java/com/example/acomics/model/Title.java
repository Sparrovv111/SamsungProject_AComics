package com.example.acomics.model;

public class Title {
    private String authors;
    private String date;
    private String description;
    private String genres;
    private String name;
    private String pictureURL;
    private String t_id;
    private String tags;
    private String type;

    // Обязательный пустой конструктор
    public Title() {}

    // Геттеры и сеттеры для всех полей
    public String getAuthors() {
        return authors;
    }

    public String getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public String getGenres() {
        return genres;
    }

    public String getName() {
        return name;
    }

    public String getPictureURL() {
        return pictureURL;
    }

    public String getT_id() {
        return t_id;
    }

    public String getTags() {
        return tags;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public void setT_id(String t_id) {
        this.t_id = t_id;
    }

    public void setPictureURL(String pictureURL) {
        this.pictureURL = pictureURL;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setGenres(String genres) {
        this.genres = genres;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }
}