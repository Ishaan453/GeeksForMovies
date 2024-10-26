package com.ishaanbhela.geeksformovies;

import android.content.Intent;
import android.database.Cursor;
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

import androidx.appcompat.app.AppCompatActivity;
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
import com.ishaanbhela.geeksformovies.commonClasses.movieDetailsCommon;
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

public class savedMovieDetails extends AppCompatActivity {

    private TextView movieTitle, movieTagline, movieGenres, movieReleaseDate, movieRuntime, movieRating, movieOverview, movieBudget, movieRevenue, movieLanguages;
    private ImageView moviePoster;
    private RecyclerView productionCompaniesRecyclerView, castRecyclerView, watchOptionsRecyclerView;
    private List<productionCompanyModel> productionCompanyList;
    private List<castModel> castList;
    private List<watchOptionsModel> watchOptionList;
    private productionCompanyAdapter productionCompaniesAdapter;
    private Button deleteMovie;
    private Button playTrailer;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private String url = MyApp.url, trailerURL;

    private int movieId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        // Initialize UI elements
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
        productionCompaniesRecyclerView = findViewById(R.id.production_companies_recycler);
        castRecyclerView = findViewById(R.id.cast_recycler);
        watchOptionsRecyclerView = findViewById(R.id.watch_recycler);
        collapsingToolbarLayout = findViewById(R.id.collapsingToolbar);
        playTrailer = findViewById(R.id.play_trailer_button);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenHeight = displayMetrics.heightPixels;
        int collapsingToolbarHeight = (int) (screenHeight * 0.65);
        ViewGroup.LayoutParams params = collapsingToolbarLayout.getLayoutParams();
        params.height = collapsingToolbarHeight;
        collapsingToolbarLayout.setLayoutParams(params);

        deleteMovie = findViewById(R.id.saveUnsave);
        deleteMovie.setText("Delete from Saved Movies");

        productionCompanyList = new ArrayList<>();
        watchOptionList = new ArrayList<>();
        
        movieDetailsCommon common = new movieDetailsCommon();

        // Get movie ID from intent
        movieId = getIntent().getIntExtra("MOVIE_ID", -1);
        loadMovieDetails(movieId);
        try {
            common.fetchWatchOptions(this, movieId, new movieDetailsCommon.watchOptionsCallback() {
                @Override
                public void onSuccess(JSONObject response) throws JSONException {
                    common.populateWatchOptionsSuccess(response, watchOptionList);
                    watchOptionsRecyclerView.setLayoutManager(new LinearLayoutManager(savedMovieDetails.this, LinearLayoutManager.HORIZONTAL, false));
                    watchOptionAdapter watchAdapter = new watchOptionAdapter(watchOptionList);
                    watchOptionsRecyclerView.setAdapter(watchAdapter);
                }

                @Override
                public void onError(String error) {
                    watchOptionsRecyclerView.setLayoutManager(new LinearLayoutManager(savedMovieDetails.this, LinearLayoutManager.HORIZONTAL, false));
                    watchOptionAdapter watchAdapter = new watchOptionAdapter(watchOptionList);
                    watchOptionsRecyclerView.setAdapter(watchAdapter);
                }
            });
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        try {
            common.fetchTrailerURL(this, movieId, new movieDetailsCommon.TrailerCallback() {
                @Override
                public void onSuccess(String result) {
                    trailerURL = result;
                    if(!trailerURL.equals("NA")){
                        playTrailer.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(savedMovieDetails.this, "SOME ERROR OCCURRED", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (JSONException e) {
            Toast.makeText(savedMovieDetails.this, "SOME ERROR OCCURRED", Toast.LENGTH_SHORT).show();
        }

        playTrailer.setOnClickListener(v -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(trailerURL)));
        });

        deleteMovie.setOnClickListener(v -> {
            new SqLiteHelper(this).deleteMovie(movieId);
            finish();
        });
    }

    private void loadMovieDetails(int movieId) {
        SqLiteHelper dbHelper = new SqLiteHelper(this);
        Cursor cursor = dbHelper.getMovieDetails(movieId);

        NumberFormat format = NumberFormat.getInstance();
        format.setMaximumFractionDigits(2);

        if (cursor != null && cursor.moveToFirst()) {
            // Check and get column indices
            int titleIndex = cursor.getColumnIndex("title");
            int taglineIndex = cursor.getColumnIndex("tagline");
            int releaseDateIndex = cursor.getColumnIndex("release_date");
            int runtimeIndex = cursor.getColumnIndex("runtime");
            int ratingIndex = cursor.getColumnIndex("rating");
            int overviewIndex = cursor.getColumnIndex("overview");
            int budgetIndex = cursor.getColumnIndex("budget");
            int revenueIndex = cursor.getColumnIndex("revenue");
            int posterPathIndex = cursor.getColumnIndex("poster_path");
            int genresIndex = cursor.getColumnIndex("genres");
            int languagesIndex = cursor.getColumnIndex("languages");

            // Now check if the indices are valid (not -1)
            if (titleIndex != -1) {
                String title = cursor.getString(titleIndex);
                movieTitle.setText(title);
            }

            if (taglineIndex != -1) {
                String tagline = cursor.getString(taglineIndex);
                movieTagline.setText(tagline);
            }

            if (releaseDateIndex != -1) {
                String releaseDate = cursor.getString(releaseDateIndex);
                movieReleaseDate.setText(releaseDate);
            }

            if (runtimeIndex != -1) {
                int runtime = cursor.getInt(runtimeIndex);
                movieRuntime.setText("Runtime: " + runtime + " min");
            }

            if (ratingIndex != -1) {
                double rating = cursor.getDouble(ratingIndex);
                movieRating.setText("Rating: " + rating + "/10");
            }

            if (overviewIndex != -1) {
                String overview = cursor.getString(overviewIndex);
                movieOverview.setText(overview);
            }

            if (budgetIndex != -1) {
                long budget = cursor.getLong(budgetIndex);
                movieBudget.setText("Budget: $" + format.format(budget));
            }

            if (revenueIndex != -1) {
                long revenue = cursor.getLong(revenueIndex);
                movieRevenue.setText("Revenue: $" + format.format(revenue));
            }

            if (posterPathIndex != -1) {
                String posterPath = cursor.getString(posterPathIndex);
                // Load Poster Image using Glide
                Glide.with(savedMovieDetails.this).load("https://image.tmdb.org/t/p/w500" + posterPath)
                        .error(R.drawable.placeholder)
                        .placeholder(R.drawable.placeholder)
                        .into(moviePoster);
            }

            if (genresIndex != -1) {
                String genres = cursor.getString(genresIndex);
                movieGenres.setText("Genres: " + genres);
            }

            if (languagesIndex != -1) {
                String languages = cursor.getString(languagesIndex);
                movieLanguages.setText("Languages: " + languages);
            }

            productionCompanyList = dbHelper.getProductionCompanies(movieId);
            productionCompaniesAdapter = new productionCompanyAdapter(productionCompanyList, this);
            productionCompaniesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            productionCompaniesRecyclerView.setAdapter(productionCompaniesAdapter);

            castList = dbHelper.getCastList(movieId);
            castRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            castAdapter cast_adapter = new castAdapter(castList, this);
            castRecyclerView.setAdapter(cast_adapter);

            cursor.close();  // Close the cursor when done
        } else {
            // Handle the case when the movie is not found
            movieTitle.setText("Movie not found");
        }
    }
}
