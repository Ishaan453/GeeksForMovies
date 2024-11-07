package com.ishaanbhela.geeksformovies.Preferences;

import androidx.annotation.NonNull;

public class languageModel {
    private String iso_639_1;
    private String english_name;
    private String name;

    public languageModel(String iso_639_1, String english_name, String name) {
        this.iso_639_1 = iso_639_1;
        this.english_name = english_name;
        this.name = name;
    }

    public String getIso_639_1() {
        return iso_639_1;
    }

    public void setIso_639_1(String iso_639_1) {
        this.iso_639_1 = iso_639_1;
    }

    public String getEnglish_name() {
        return english_name;
    }

    public void setEnglish_name(String english_name) {
        this.english_name = english_name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @NonNull
    @Override
    public String toString() {
        return english_name;
    }
}
