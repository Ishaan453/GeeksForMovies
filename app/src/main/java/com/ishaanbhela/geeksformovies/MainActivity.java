package com.ishaanbhela.geeksformovies;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.admanager.AdManagerAdView;
import com.ishaanbhela.geeksformovies.Database.SqLiteHelper;
import com.ishaanbhela.geeksformovies.searchedMovies.searchedMoviesAdapter;
import com.ishaanbhela.geeksformovies.searchedMovies.searchedMoviesModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private searchedMoviesAdapter movieAdapter;
    private List<searchedMoviesModel> trendingMovieList;
    private List<searchedMoviesModel> popularMovieList;
    private List<searchedMoviesModel> savedMovieList;
    private List<searchedMoviesModel> searchedMovieList;
    private TextView searched, trending, popular, saved;
    private ImageView searchIcon;
    private EditText searchEditText;
    AdManagerAdView adView;
    String url = "https://2f2vjaxr6x6uqpdvqmwpmwch6a0pzera.lambda-url.ap-south-1.on.aws/";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        trendingMovieList = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerView);
        searched = findViewById(R.id.searched_button);
        trending = findViewById(R.id.trending_button);
        popular = findViewById(R.id.popular_button);
        saved = findViewById(R.id.saved_button);
        searchIcon = findViewById(R.id.searchIcon);
        searchEditText = findViewById(R.id.editText);

        popularMovieList = new ArrayList<>();
        savedMovieList = new ArrayList<>();
        searchedMovieList = new ArrayList<>();
        trendingMovieList = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        try {
            loadTrending();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        MobileAds.initialize(this, initializationStatus -> {});
        adView = findViewById(R.id.bannerAd);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        
        searched.setOnClickListener(v -> {
            try {
                loadSearched(searched.getText().toString());
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
        
        trending.setOnClickListener(v -> {
            try {
                loadTrending();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
        
        popular.setOnClickListener(v -> {
            try {
                loadPopular();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
        
        saved.setOnClickListener(v -> {
            loadSaved();
        });

        searchIcon.setOnClickListener(v -> {
            try {
                loadSearched(searchEditText.getText().toString());
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
        
    }

    private void loadSaved() {
        saved.setSelected(true);
        popular.setSelected(false);
        searched.setSelected(false);
        trending.setSelected(false);

        SqLiteHelper dbHelper = new SqLiteHelper(this);  // Initialize the SQLite helper
        List<searchedMoviesModel> savedMoviesList = new ArrayList<>();

        // Fetch saved movies from the database
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM movies", null);

        // If there are saved movies, iterate over the results and add them to the list
        if (cursor.moveToFirst()) {
            do {
                // Extract data from the cursor
                int idIndex = cursor.getColumnIndex("id");
                int titleIndex = cursor.getColumnIndex("title");
                int overviewIndex = cursor.getColumnIndex("overview");
                int releaseDateIndex = cursor.getColumnIndex("release_date");
                int posterPathIndex = cursor.getColumnIndex("poster_path");
                int ratingIndex = cursor.getColumnIndex("rating");

                // Ensure all indices are valid
                if (idIndex != -1 && titleIndex != -1 && overviewIndex != -1 && releaseDateIndex != -1 && posterPathIndex != -1 && ratingIndex != -1) {
                    int id = cursor.getInt(idIndex);
                    String title = cursor.getString(titleIndex);
                    String overview = cursor.getString(overviewIndex);
                    String releaseDate = cursor.getString(releaseDateIndex);
                    String posterPath = cursor.getString(posterPathIndex);
                    double rating = cursor.getDouble(ratingIndex);

                    // Create a movie model object and add it to the list
                    searchedMoviesModel movie = new searchedMoviesModel(id, title, overview, releaseDate, posterPath, rating);
                    savedMoviesList.add(movie);
                }
            } while (cursor.moveToNext());
        }

        cursor.close();  // Close cursor after use

        // Set up the RecyclerView with the saved movies
        movieAdapter = new searchedMoviesAdapter(savedMoviesList, this);
        recyclerView.setAdapter(movieAdapter);

    }

    private void loadPopular() throws JSONException {
        saved.setSelected(false);
        popular.setSelected(true);
        searched.setSelected(false);
        trending.setSelected(false);

        if(!popularMovieList.isEmpty()){
            movieAdapter = new searchedMoviesAdapter(popularMovieList, this);
            recyclerView.setAdapter(movieAdapter);
            return;
        }

        JSONObject jsonRequest = new JSONObject();
        jsonRequest.put("type", "popular");

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonRequest,
                response -> {
                    try {
                        System.out.println("GOT RESPONSE POPULAR");
                        System.out.println(response.toString());
                        JSONArray results = response.getJSONArray("results");
                        addMovie(results, popularMovieList);
                        movieAdapter = new searchedMoviesAdapter(popularMovieList, this);
                        recyclerView.setAdapter(movieAdapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("MovieFetcher", "JSON parsing error: " + e.getMessage());
                    }
                },
                error -> Log.e("MovieFetcher", "Error: " + error.getMessage())
        );

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    private void loadSearched(String search) throws JSONException {
        saved.setSelected(false);
        popular.setSelected(false);
        searched.setSelected(true);
        trending.setSelected(false);

        if(search.equals("") || search.isEmpty()){
            Toast.makeText(this, "No search value provided", Toast.LENGTH_SHORT).show();
            return;
        }
        else if(search.equals(searched.getText().toString())){
            movieAdapter = new searchedMoviesAdapter(searchedMovieList, this);
            recyclerView.setAdapter(movieAdapter);
            return;
        }
        searched.setText(search);
        searched.setVisibility(View.VISIBLE);
        searchedMovieList.clear();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "search");
        jsonObject.put("search", search);

        // Create a new request to fetch movie data from the API
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                response -> {
                    try {
                        // Parse the results from the API response
                        JSONArray results = response.getJSONArray("results");
                        addMovie(results, searchedMovieList);
                        // Update RecyclerView with the new data
                        movieAdapter = new searchedMoviesAdapter(searchedMovieList, this);
                        recyclerView.setAdapter(movieAdapter);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    // Handle error
                    error.printStackTrace();
                });

        // Add the request to the Volley request queue
        Volley.newRequestQueue(this).add(jsonObjectRequest);


    }

    private void loadTrending() throws JSONException {

        saved.setSelected(false);
        popular.setSelected(false);
        searched.setSelected(false);
        trending.setSelected(true);
        
        if(!trendingMovieList.isEmpty()){
            movieAdapter = new searchedMoviesAdapter(trendingMovieList, this);
            recyclerView.setAdapter(movieAdapter);
            return;
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "trending");

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonObject,
                response -> {
                    try {
                        System.out.println("GOT RESPONSE TRENDING");
                        System.out.println(response.toString());
                        JSONArray results = response.getJSONArray("results");
                        addMovie(results, trendingMovieList);
                        movieAdapter = new searchedMoviesAdapter(trendingMovieList, this);
                        recyclerView.setAdapter(movieAdapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("MovieFetcher", "JSON parsing error: " + e.getMessage());
                    }
                },
                error -> Log.e("MovieFetcher", "Error: " + error.getMessage())
        );
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    private void addMovie(JSONArray results, List<searchedMoviesModel> movieList){
        try{
            for (int i = 0; i < results.length(); i++) {
                JSONObject movieJson = results.getJSONObject(i);

                // Extracting the data
                int id = movieJson.getInt("id");
                String title = movieJson.getString("title");
                String overview = movieJson.getString("overview");
                String releaseDate = movieJson.getString("release_date");
                String posterPath = movieJson.getString("poster_path");
                double rating = movieJson.getDouble("vote_average");

                // Creating a new movie model instance and adding it to the list
                searchedMoviesModel movie = new searchedMoviesModel(id, title, overview, releaseDate, posterPath, rating);
                movieList.add(movie);
            }
        }catch (Exception e){
            System.out.println("ERRORR");
        }
    }
}