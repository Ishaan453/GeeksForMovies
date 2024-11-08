package com.ishaanbhela.geeksformovies.Database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ishaanbhela.geeksformovies.Preferences.preferenceModel;
import com.ishaanbhela.geeksformovies.cast.castModel;
import com.ishaanbhela.geeksformovies.productionCompany.productionCompanyModel;

import java.util.ArrayList;
import java.util.List;

public class SqLiteHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "movies_db";
    private static final int DATABASE_VERSION = 2;

    public SqLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the movies table
        String CREATE_MOVIES_TABLE = "CREATE TABLE IF NOT EXISTS movies (" +
                "id INTEGER PRIMARY KEY, " +
                "title TEXT, " +
                "poster_path TEXT, " +
                "tagline TEXT, " +
                "release_date TEXT, " +
                "runtime INTEGER, " +
                "rating REAL, " +
                "overview TEXT, " +
                "budget INTEGER, " +
                "revenue INTEGER, " +
                "genres TEXT, " +
                "languages TEXT)";
        db.execSQL(CREATE_MOVIES_TABLE);

        // Create the production_companies table
        String CREATE_PRODUCTION_COMPANIES_TABLE = "CREATE TABLE IF NOT EXISTS production_companies (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "movie_id INTEGER, " +
                "company_name TEXT, " +
                "logo_path TEXT, " +
                "FOREIGN KEY(movie_id) REFERENCES movies(id) ON DELETE CASCADE)";
        db.execSQL(CREATE_PRODUCTION_COMPANIES_TABLE);

        String CREATE_CAST_TABLE = "CREATE TABLE IF NOT EXISTS castInfo (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "movie_id INTEGER, " +
                "img_path TEXT, " +
                "name TEXT, " +
                "character TEXT, " +
                "FOREIGN KEY(movie_id) REFERENCES movies(id) ON DELETE CASCADE)";
        db.execSQL(CREATE_CAST_TABLE);

        String CREATE_USER_PREFERENCES_TABLE = "CREATE TABLE IF NOT EXISTS user_preferences (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "preferred_language TEXT, " +
                "preferred_genres TEXT, " +
                "preferred_region TEXT, " +
                "preferred_vote_avg TEXT, " +
                "preferred_watch_options TEXT," +
                "preferred_runtime INTEGER)";
        db.execSQL(CREATE_USER_PREFERENCES_TABLE);


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(newVersion == 2){
            String CREATE_USER_PREFERENCES_TABLE = "CREATE TABLE IF NOT EXISTS user_preferences (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "preferred_language TEXT, " +
                    "preferred_genres TEXT, " +
                    "preferred_region TEXT, " +
                    "preferred_vote_avg TEXT, " +
                    "preferred_watch_options TEXT," +
                    "preferred_runtime INTEGER)";
            db.execSQL(CREATE_USER_PREFERENCES_TABLE);
        }
    }

    // Check if the movie exists in the DB
    public boolean isMovieSaved(int movieId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("movies", new String[]{"id"}, "id=?", new String[]{String.valueOf(movieId)}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    // Insert movie and production companies
    public void insertMovie(int id, String title, String posterPath, String tagline, String releaseDate, int runtime,
                            double rating, String overview, long budget, long revenue, String genres, String languages,
                            List<productionCompanyModel> productionCompanies, List<castModel> castList) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Insert movie
        ContentValues movieValues = new ContentValues();
        movieValues.put("id", id);
        movieValues.put("title", title);
        movieValues.put("poster_path", posterPath);
        movieValues.put("tagline", tagline);
        movieValues.put("release_date", releaseDate);
        movieValues.put("runtime", runtime);
        movieValues.put("rating", rating);
        movieValues.put("overview", overview);
        movieValues.put("budget", budget);
        movieValues.put("revenue", revenue);
        movieValues.put("genres", genres);
        movieValues.put("languages", languages);

        db.insert("movies", null, movieValues);

        // Insert production companies associated with this movie
        for (int i = 0; i < productionCompanies.size(); i++) {
            productionCompanyModel company = productionCompanies.get(i);
            String companyName = company.getName();
            String logoPath = company.getLogo_path();

            ContentValues companyValues = new ContentValues();
            companyValues.put("movie_id", id);
            companyValues.put("company_name", companyName);
            companyValues.put("logo_path", logoPath);

            db.insert("production_companies", null, companyValues);
        }

        // Insert cast list
        for (castModel cast : castList) {
            ContentValues castValues = new ContentValues();
            castValues.put("movie_id", id);
            castValues.put("img_path", cast.getImgPath());
            castValues.put("name", cast.getName());
            castValues.put("character", cast.getCharacter());

            db.insert("castInfo", null, castValues);
        }

        db.close();
    }

    // Delete movie
    public void deleteMovie(int movieId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("movies", "id=?", new String[]{String.valueOf(movieId)});
        db.delete("production_companies", "movie_id=?", new String[]{String.valueOf(movieId)});
        db.delete("castInfo", "movie_id=?", new String[]{String.valueOf(movieId)});
        db.close();
    }

    // Get movie details
    public Cursor getMovieDetails(int movieId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query("movies", null, "id=?", new String[]{String.valueOf(movieId)}, null, null, null);
    }

    // Get production companies for a movie
    public List<productionCompanyModel> getProductionCompanies(int movieId) {
        List<productionCompanyModel> companies = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("production_companies", null, "movie_id=?", new String[]{String.valueOf(movieId)}, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                int companyNameIndex = cursor.getColumnIndex("company_name");
                int logoPathIndex = cursor.getColumnIndex("logo_path");
                if(companyNameIndex > 0 && logoPathIndex > 0){
                    String companyName = cursor.getString(companyNameIndex);
                    String logoPath = cursor.getString(logoPathIndex);
                    companies.add(new productionCompanyModel(logoPath, companyName));
                }
                else{
                    System.out.println("SOME ERRORR OCCURRED");
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return companies;
    }

    public List<castModel> getCastList(int movieId) {
        List<castModel> castList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query("castInfo", null, "movie_id=?", new String[]{String.valueOf(movieId)}, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                int imgPathIdx = cursor.getColumnIndex("img_path");
                int nameIdx = cursor.getColumnIndex("name");
                int characterIdx = cursor.getColumnIndex("character");

                if(imgPathIdx != -1 && nameIdx != -1 && characterIdx != -1){
                    String imgPath = cursor.getString(imgPathIdx);
                    String name = cursor.getString(nameIdx);
                    String character = cursor.getString(characterIdx);

                    castList.add(new castModel(name, imgPath, character));
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return castList;
    }

    public void saveUserPreferences(preferenceModel preference) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("preferred_language", preference.getPreferredLanguage());
        values.put("preferred_genres", preference.getPreferredGenres());
        values.put("preferred_region", preference.getPreferredRegion());
        values.put("preferred_vote_avg", preference.getPreferredVoteAvg());
        values.put("preferred_watch_options", preference.getPreferredWatchOptions());
        values.put("preferred_runtime", preference.getPreferredRuntime());

        Cursor cursor = db.rawQuery("SELECT id FROM user_preferences LIMIT 1", null);

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            int ID = cursor.getColumnIndex("id");
            int id = -1;
            if(ID != -1){
                id = cursor.getInt(ID);
            }
            System.out.println("Updating DB");
            db.update(
                    "user_preferences",
                    values,
                    "id = ?",
                    new String[]{String.valueOf(id)}
            );
            cursor.close();
        } else {
            db.insert("user_preferences", null, values);
        }

        db.close();
    }

    public preferenceModel getUserPreferences() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("user_preferences", null, null, null, null, null, null);

        preferenceModel userPreferences = null;

        // Check if there is at least one row in the cursor
        if (cursor != null && cursor.moveToFirst()) {
            // Make sure column exists before accessing it
            int languageIndex = cursor.getColumnIndex("preferred_language");
            int regionIndex = cursor.getColumnIndex("preferred_region");
            int genresIndex = cursor.getColumnIndex("preferred_genres");
            int voteAvgIndex = cursor.getColumnIndex("preferred_vote_avg");
            int watchOptionsIndex = cursor.getColumnIndex("preferred_watch_options");
            int runtimeIndex = cursor.getColumnIndex("preferred_runtime");

            if (languageIndex != -1 && regionIndex != -1 && genresIndex != -1 &&
                    voteAvgIndex != -1 && watchOptionsIndex != -1 && runtimeIndex != -1) {

                // Now safely retrieve the values
                String preferredLanguage = cursor.getString(languageIndex);
                String preferredRegion = cursor.getString(regionIndex);
                String preferredGenres = cursor.getString(genresIndex);
                String preferredVoteAvg = cursor.getString(voteAvgIndex);
                String preferredWatchOptions = cursor.getString(watchOptionsIndex);
                String preferredRuntime = cursor.getString(runtimeIndex);

                // Create the preferenceModel object
                userPreferences = new preferenceModel(preferredLanguage, preferredGenres, preferredRegion,
                        preferredVoteAvg, preferredWatchOptions, preferredRuntime);
            } else {
                // Handle the case where any of the columns are missing
                System.out.println("Error: One or more columns do not exist in the database.");
            }
        }
        else{
            return new preferenceModel("0000", "[]", "0000", "No Preference", "No Preference", "No Preference");
        }

        // Always close the cursor to avoid memory leaks
        if (cursor != null) {
            cursor.close();
        }
        db.close();

        return userPreferences;
    }
}
