package com.ishaanbhela.geeksformovies.Preferences;

import androidx.annotation.NonNull;

public class genresModel {

    private int id;
    private String name;

    public genresModel(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }
}
