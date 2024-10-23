package com.ishaanbhela.geeksformovies;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.ishaanbhela.geeksformovies.Database.SqLiteHelper;
import com.ishaanbhela.geeksformovies.cast.castAdapter;
import com.ishaanbhela.geeksformovies.cast.castModel;
import com.ishaanbhela.geeksformovies.productionCompany.productionCompanyAdapter;
import com.ishaanbhela.geeksformovies.productionCompany.productionCompanyModel;
import com.ishaanbhela.geeksformovies.watchOptions.watchOptionAdapter;
import com.ishaanbhela.geeksformovies.watchOptions.watchOptionsModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class movieDetails extends AppCompatActivity {

    private TextView movieTitle, movieTagline, movieGenres, movieReleaseDate, movieRuntime, movieRating, movieOverview, movieBudget, movieRevenue, movieLanguages;
    private ImageView moviePoster;
    private Button playTrailer;
    private RecyclerView productionCompaniesRecyclerView, castsRecyclerView, watchOptionsRecyclerView;
    private List<productionCompanyModel> productionCompanyList;
    private List<castModel> casts;
    private List<watchOptionsModel> watchOptionList;
    private productionCompanyAdapter productionCompaniesAdapter;
    private Button saveUnsave;
    private CollapsingToolbarLayout collapsingToolbarLayout;

    private int movieId;
    private String title, posterPath, tagline, releaseDate, overview, genres, languages, trailerURL;
    private int runtime;
    private long budget, revenue;
    private Double rating;

    String url = "https://2f2vjaxr6x6uqpdvqmwpmwch6a0pzera.lambda-url.ap-south-1.on.aws/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_movie_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        movieTitle = findViewById(R.id.movie_title);
        movieTagline = findViewById(R.id.movie_tagline);
        movieGenres = findViewById(R.id.movie_genres);
        movieReleaseDate = findViewById(R.id.movie_release_date);
        movieRuntime = findViewById(R.id.movie_runtime);
        movieRating = findViewById(R.id.movie_rating);
        movieOverview = findViewById(R.id.movie_overview);
        movieBudget = findViewById(R.id.movie_budget);
        movieRevenue = findViewById(R.id.movie_revenue);
        movieLanguages = findViewById(R.id.movie_languages);
        moviePoster = findViewById(R.id.movie_poster);
        playTrailer = findViewById(R.id.play_trailer_button);
        productionCompaniesRecyclerView = findViewById(R.id.production_companies_recycler);
        saveUnsave = findViewById(R.id.saveUnsave);
        castsRecyclerView = findViewById(R.id.cast_recycler);
        watchOptionsRecyclerView = findViewById(R.id.watch_recycler);
        collapsingToolbarLayout = findViewById(R.id.collapsingToolbar);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenHeight = displayMetrics.heightPixels;
        int collapsingToolbarHeight = (int) (screenHeight * 0.65);
        ViewGroup.LayoutParams params = collapsingToolbarLayout.getLayoutParams();
        params.height = collapsingToolbarHeight;
        collapsingToolbarLayout.setLayoutParams(params);

        productionCompanyList = new ArrayList<>();
        casts = new ArrayList<>();
        watchOptionList = new ArrayList<>();
        SqLiteHelper dbHelper = new SqLiteHelper(this);

        movieId = getIntent().getIntExtra("MOVIE_ID", -1);

        // Call the API to get movie details
        try {
            getMovieDetails(movieId);
            getWatchOptions(movieId);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }


        if(dbHelper.isMovieSaved(movieId)){
            saveUnsave.setText("Delete from Saved Movies");
        }

        try {
            getTrailerURL(movieId, new TrailerCallback() {
                @Override
                public void onSuccess(String result) {
                    trailerURL = result;
                    if(!trailerURL.equals("NA")){
                        playTrailer.setVisibility(View.VISIBLE);
                    }

                }

                @Override
                public void onError(String error) {
                    Toast.makeText(movieDetails.this, "SOME ERROR OCCURED", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (JSONException e) {
            Toast.makeText(movieDetails.this, "SOME ERROR OCCURED", Toast.LENGTH_SHORT).show();
        }

        saveUnsave.setOnClickListener(v -> {
            if (saveUnsave.getText().equals("Save")) {
                // Save movie and production companies to the database
                dbHelper.insertMovie(movieId, title, posterPath, tagline, releaseDate, runtime, rating,
                        overview, budget, revenue, genres, languages, productionCompanyList, casts);
                saveUnsave.setText("Delete from Saved Movies");
            } else {
                // Delete movie and its associated production companies
                dbHelper.deleteMovie(movieId);
                saveUnsave.setText("Save");
            }
        });

        playTrailer.setOnClickListener(v -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(trailerURL)));
        });
    }

    public interface TrailerCallback {
        void onSuccess(String result);
        void onError(String error);
    }

    private void getTrailerURL(int movieId, final TrailerCallback callback) throws JSONException{
        JSONObject jsonRequest = new JSONObject();
        jsonRequest.put("type", "trailer");
        jsonRequest.put("movieId", ""+movieId);

        System.out.println(jsonRequest);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonRequest,
                response -> {
                    try {
                        System.out.println("GOT RESPONSE TRAILER");
                        System.out.println(response);
                        callback.onSuccess(response.getString("trailerURL"));
                    } catch (JSONException e) {
                        callback.onError("ERROR" + e);
                    }
                },
                error -> Log.e("MovieFetcher", "Error WHILE FETCHING: " + error.getMessage() + error)
        );
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }


    private void getWatchOptions(int movieId) throws JSONException {
        RequestQueue queue = Volley.newRequestQueue(this);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "watchOptions");
        jsonObject.put("movieId", ""+movieId);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject, response -> {
            try{
                System.out.println(response.toString());
                if (response.has("flatrate")) {
                    JSONArray flatRateArray = response.getJSONArray("flatrate");
                    for (int i = 0; i < flatRateArray.length(); i++) {
                        JSONObject providerJson = flatRateArray.getJSONObject(i);
                        watchOptionsModel provider = new watchOptionsModel(
                                providerJson.getString("logo_path"),
                                providerJson.getString("provider_name"),
                                "Subscription"
                        );
                        watchOptionList.add(provider);
                    }
                }

                if (response.has("rent")) {
                    JSONArray rentArray = response.getJSONArray("rent");
                    for (int i = 0; i < rentArray.length(); i++) {
                        JSONObject providerJson = rentArray.getJSONObject(i);
                        watchOptionsModel provider = new watchOptionsModel(
                                providerJson.getString("logo_path"),
                                providerJson.getString("provider_name"),
                                "Rent"
                        );
                        watchOptionList.add(provider);
                    }
                }

                if (response.has("buy")) {
                    JSONArray rentArray = response.getJSONArray("buy");
                    for (int i = 0; i < rentArray.length(); i++) {
                        JSONObject providerJson = rentArray.getJSONObject(i);
                        watchOptionsModel provider = new watchOptionsModel(
                                providerJson.getString("logo_path"),
                                providerJson.getString("provider_name"),
                                "Buy"
                        );
                        watchOptionList.add(provider);
                    }
                }

                watchOptionsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
                watchOptionAdapter watchAdapter = new watchOptionAdapter(watchOptionList);
                watchOptionsRecyclerView.setAdapter(watchAdapter);


            } catch (Exception e){
                System.out.println("ERRORR");
            }
        }, error -> {
            watchOptionsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            watchOptionAdapter watchAdapter = new watchOptionAdapter(watchOptionList);
            watchOptionsRecyclerView.setAdapter(watchAdapter);
        });

        queue.add(jsonObjectRequest);
    }


    private void getMovieDetails(int movieId) throws JSONException {
        RequestQueue queue = Volley.newRequestQueue(this);

        NumberFormat format = NumberFormat.getInstance();
        format.setMaximumFractionDigits(2);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "movieDetails");
        jsonObject.put("movieId", ""+movieId);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                response -> {
                    try {
                        // Populate UI with data
                        title = response.getString("title");
                        movieTitle.setText(title);

                        tagline = response.getString("tagline");
                        movieTagline.setText(tagline);

                        releaseDate = response.getString("release_date");
                        movieReleaseDate.setText(releaseDate);

                        runtime = response.getInt("runtime");
                        movieRuntime.setText("Runtime: " + format.format(runtime) + " min");

                        rating = response.getDouble("vote_average");
                        movieRating.setText("Rating: " + format.format(rating) + "/10");

                        overview = response.getString("overview");
                        movieOverview.setText(overview);

                        posterPath = response.getString("poster_path");

                        // Load Poster Image using Picasso
                        Glide.with(movieDetails.this).load("https://image.tmdb.org/t/p/w500" + posterPath)
                                .error(R.drawable.placeholder)
                                .placeholder(R.drawable.placeholder)
                                .into(moviePoster);

                        // Budget and Revenue
                        budget = response.getLong("budget");
                        revenue = response.getLong("revenue");
                        if(budget != 0){
                            movieBudget.setText("Budget: $" + format.format(budget));
                        }
                        else{
                            movieBudget.setText("Budget: NA");
                        }

                        if(revenue != 0) {
                            movieRevenue.setText("Revenue: $" + format.format(revenue));
                        }
                        else{
                            movieRevenue.setText("Revenue: NA");
                        }

                        // Genres
                        JSONArray genresArray = response.getJSONArray("genres");
                        StringBuilder genres = new StringBuilder();
                        for (int i = 0; i < genresArray.length(); i++) {
                            JSONObject genre = genresArray.getJSONObject(i);
                            genres.append(genre.getString("name"));
                            if (i != genresArray.length() - 1) {
                                genres.append(", ");
                            }
                        }
                        this.genres = genres.toString();
                        movieGenres.setText(this.genres);

                        // Spoken Languages
                        JSONArray languagesArray = response.getJSONArray("spoken_languages");
                        StringBuilder languages = new StringBuilder();
                        for (int i = 0; i < languagesArray.length(); i++) {
                            JSONObject language = languagesArray.getJSONObject(i);
                            languages.append(language.getString("english_name"));
                            if (i != languagesArray.length() - 1) {
                                languages.append(", ");
                            }
                        }
                        this.languages = languages.toString();
                        movieLanguages.setText("Languages: " + this.languages);

                        // Production Companies
                        JSONArray productionCompaniesArray = response.getJSONArray("production_companies");
                        productionCompanyList.clear();
                        for (int i = 0; i < productionCompaniesArray.length(); i++) {
                            JSONObject company = productionCompaniesArray.getJSONObject(i);
                            String companyName = company.getString("name");
                            String logoPath = company.optString("logo_path", null);  // Sometimes logo_path can be null
                            if(!logoPath.equals("null")){
                                productionCompanyList.add(new productionCompanyModel(logoPath, companyName));
                            }

                        }
                        productionCompaniesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
                        productionCompaniesAdapter = new productionCompanyAdapter(productionCompanyList, this);
                        productionCompaniesRecyclerView.setAdapter(productionCompaniesAdapter);


                        JSONObject credits = response.getJSONObject("credits");
                        JSONArray castArray = credits.getJSONArray("cast");
                        // Loop through the cast array
                        for (int i = 0; i < castArray.length(); i++) {
                            // Get each cast member as a JSONObject
                            JSONObject castMember = castArray.getJSONObject(i);

                            // Extract name, profile_path, and character
                            String name = castMember.getString("name");
                            String profilePath = castMember.getString("profile_path");
                            String character = castMember.getString("character");

                            // Create a new castModel object and add it to the list
                            castModel cast = new castModel(name, profilePath, character);
                            casts.add(cast);
                        }
                        System.out.println("SETTED FROM HEREEE!!!!");
                        castsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
                        castAdapter cast_adapter = new castAdapter(casts, this);
                        castsRecyclerView.setAdapter(cast_adapter);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
                    // Handle error
                    error.printStackTrace();
                });

        queue.add(jsonObjectRequest);

    }
}