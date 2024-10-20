package com.ishaanbhela.geeksformovies.searchedMovies;

public class searchedMoviesModel {

    private int id;
    private String title;
    private String overview;
    private String releaseDate;
    private String posterPath;
    private double Rating;

    // Constructor
    public searchedMoviesModel(int id, String title, String overview, String releaseDate, String posterPath, double Rating) {
        this.id = id;
        this.title = title;
        this.overview = overview;
        this.releaseDate = releaseDate;
        this.posterPath = posterPath;
        this.Rating = Rating;
    }

    // Getter and Setter Methods

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public double getVoteAverage() {
        return Rating;
    }

    public void setVoteAverage(double Rating) {
        this.Rating = Rating;
    }
}
