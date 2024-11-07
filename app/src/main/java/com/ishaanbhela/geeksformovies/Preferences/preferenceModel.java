package com.ishaanbhela.geeksformovies.Preferences;

public class preferenceModel {
    private String preferredLanguage;
    private String preferredGenres;
    private String preferredRegion;
    private String preferredVoteAvg;
    private String preferredWatchOptions;
    private int preferredRuntime;

    // Constructor
    public preferenceModel(String preferredLanguage, String preferredGenres, String preferredRegion,
                           String preferredVoteAvg, String preferredWatchOptions, int preferredRuntime) {
        this.preferredLanguage = preferredLanguage;
        this.preferredGenres = preferredGenres;
        this.preferredRegion = preferredRegion;
        this.preferredVoteAvg = preferredVoteAvg;
        this.preferredWatchOptions = preferredWatchOptions;
        this.preferredRuntime = preferredRuntime;
    }

    // Getters and setters for each field
    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public String getPreferredGenres() {
        return preferredGenres;
    }

    public void setPreferredGenres(String preferredGenres) {
        this.preferredGenres = preferredGenres;
    }

    public String getPreferredRegion() {
        return preferredRegion;
    }

    public void setPreferredRegion(String preferredRegion) {
        this.preferredRegion = preferredRegion;
    }

    public String getPreferredVoteAvg() {
        return preferredVoteAvg;
    }

    public void setPreferredVoteAvg(String preferredVoteAvg) {
        this.preferredVoteAvg = preferredVoteAvg;
    }

    public String getPreferredWatchOptions() {
        return preferredWatchOptions;
    }

    public void setPreferredWatchOptions(String preferredWatchOptions) {
        this.preferredWatchOptions = preferredWatchOptions;
    }

    public int getPreferredRuntime() {
        return preferredRuntime;
    }

    public void setPreferredRuntime(int preferredRuntime) {
        this.preferredRuntime = preferredRuntime;
    }
}

