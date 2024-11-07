package com.ishaanbhela.geeksformovies.Preferences;

import androidx.annotation.NonNull;

public class regionModel {
    private String iso_3166_1;
    private String english_name;
    private String native_name;

    public regionModel(String iso_3166_1, String english_name, String native_name) {
        this.iso_3166_1 = iso_3166_1;
        this.english_name = english_name;
        this.native_name = native_name;
    }

    public String getIso_3166_1() {
        return iso_3166_1;
    }

    public void setIso_3166_1(String iso_3166_1) {
        this.iso_3166_1 = iso_3166_1;
    }

    public String getEnglish_name() {
        return english_name;
    }

    public void setEnglish_name(String english_name) {
        this.english_name = english_name;
    }

    public String getNative_name() {
        return native_name;
    }

    public void setNative_name(String native_name) {
        this.native_name = native_name;
    }

    @NonNull
    @Override
    public String toString() {
        return english_name;
    }
}
