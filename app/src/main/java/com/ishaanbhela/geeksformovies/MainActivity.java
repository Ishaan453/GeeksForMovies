package com.ishaanbhela.geeksformovies;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.ishaanbhela.geeksformovies.Database.SqLiteHelper;
import com.ishaanbhela.geeksformovies.searchedMovies.searchedMoviesAdapter;
import com.ishaanbhela.geeksformovies.searchedMovies.searchedMoviesModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerViewTrending, recyclerViewPopular, recyclerViewTopRated, recyclerViewUpcoming, recyclerViewSaved;
    private searchedMoviesAdapter movieAdapter;
    private List<searchedMoviesModel> trendingMovieList;
    private List<searchedMoviesModel> popularMovieList;
    private List<searchedMoviesModel> searchedMovieList;
    private List<searchedMoviesModel> topRatedMovieList, upcomingMovieList;
    private TextView searched, trending, popular, saved, topRated, upcoming;
    private ImageView searchIcon;
    private EditText searchEditText;
    private searchMovieFragment fragment;
    LinearLayout content;
    String url = MyApp.url;
    int cardWidth;


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

        content = findViewById(R.id.content);
        trendingMovieList = new ArrayList<>();
        searchIcon = findViewById(R.id.searchIcon);
        searchEditText = findViewById(R.id.editText);
        popularMovieList = new ArrayList<>();
        searchedMovieList = new ArrayList<>();
        trendingMovieList = new ArrayList<>();
        topRatedMovieList = new ArrayList<>();
        upcomingMovieList = new ArrayList<>();

        cardWidth = (int) getResources().getDimension(R.dimen.searchImageSize);

        int index = 0;
        trending = createTextView("Trending");
        content.addView(trending, index++);
        recyclerViewTrending = createRecyclerView();
        content.addView(recyclerViewTrending, index++);
        try {
            loadTrending();
        } catch (JSONException e) {
            Toast.makeText(this, "UNABLE TO LOAD TRENDING MOVIES", Toast.LENGTH_SHORT).show();
        }


        popular = createTextView("Popular");
        content.addView(popular, index++);
        recyclerViewPopular = createRecyclerView();
        content.addView(recyclerViewPopular, index++);
        try {
            loadPopular();
        } catch (JSONException e) {
            Toast.makeText(this, "UNABLE TO LOAD POPULAR MOVIES", Toast.LENGTH_SHORT).show();
        }


        topRated = createTextView("Top Rated");
        content.addView(topRated, index++);

        recyclerViewTopRated = createRecyclerView();
        content.addView(recyclerViewTopRated, index++);
        try {
            loadTopRated();
        } catch (JSONException e) {
            Toast.makeText(this, "UNABLE TO LOAD TOPRATED MOVIES", Toast.LENGTH_SHORT).show();
        }


        upcoming = createTextView("Upcoming");
        content.addView(upcoming, index++);

        recyclerViewUpcoming = createRecyclerView();
        content.addView(recyclerViewUpcoming, index++);
        try {
            loadUpcoming();
        } catch (JSONException e) {
            Toast.makeText(this, "UNABLE TO LOAD UPCOMING MOVIES", Toast.LENGTH_SHORT).show();
        }


        saved = createTextView("Saved");
        content.addView(saved, index++);

        recyclerViewSaved = createRecyclerView();
        content.addView(recyclerViewSaved, index++);
        loadSaved();

        searchIcon.setOnClickListener(v -> {
            try {
                if(searchEditText.getText().toString().isEmpty()){
                    Toast.makeText(this, "No search value provided", Toast.LENGTH_SHORT).show();
                }
                else{
                    searchMovieFragment existingFragment = (searchMovieFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                    if(existingFragment == null){
                        String searchedKeyword = searchEditText.getText().toString().trim();
                        fragment = searchMovieFragment.newInstance(searchedKeyword);
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                        transaction.setCustomAnimations(R.anim.search_fragment_in, R.anim.search_fragment_out, R.anim.search_fragment_in, R.anim.search_fragment_out);

                        transaction.add(R.id.fragment_container, fragment); // Use add() instead of replace()
                        transaction.addToBackStack(null); // Add this transaction to the back stack
                        transaction.commit();
                    }

                    else{
                        if(!existingFragment.searched.equals(searchEditText.getText().toString())){
                            existingFragment.searched = searchEditText.getText().toString();
                            existingFragment.loadMovies(1);
                            System.out.println("CHANGED");
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)){
                try {

                    return true;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            return false;
        });
        
    }

    private void loadUpcoming() throws JSONException{

        if(!upcomingMovieList.isEmpty()){
            movieAdapter = new searchedMoviesAdapter(upcomingMovieList, this, cardWidth);
            recyclerViewUpcoming.setAdapter(movieAdapter);
            return;
        }

        JSONObject jsonRequest = new JSONObject();
        jsonRequest.put("type", "upcoming");

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonRequest,
                response -> {
                    try {
                        System.out.println("GOT RESPONSE UPCOMING");
                        JSONArray results = response.getJSONArray("results");
                        addMovie(results, upcomingMovieList);
                        movieAdapter = new searchedMoviesAdapter(upcomingMovieList, this, cardWidth);
                        recyclerViewUpcoming.setAdapter(movieAdapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("MovieFetcher", "JSON parsing error: " + e.getMessage());
                    }
                },
                error -> Log.e("MovieFetcher", "Error: " + error.getMessage())
        );
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    private void loadTopRated() throws JSONException{

        if(!topRatedMovieList.isEmpty()){
            movieAdapter = new searchedMoviesAdapter(topRatedMovieList, this, cardWidth);
            recyclerViewTopRated.setAdapter(movieAdapter);
            return;
        }

        JSONObject jsonRequest = new JSONObject();
        jsonRequest.put("type", "topRated");

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonRequest,
                response -> {
                    try {
                        System.out.println("GOT RESPONSE TOP RATED");
                        JSONArray results = response.getJSONArray("results");
                        addMovie(results, topRatedMovieList);
                        movieAdapter = new searchedMoviesAdapter(topRatedMovieList, this, cardWidth);
                        recyclerViewTopRated.setAdapter(movieAdapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("MovieFetcher", "JSON parsing error: " + e.getMessage());
                    }
                },
                error -> Log.e("MovieFetcher", "Error: " + error.getMessage())
        );

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    private void loadSaved() {
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
        movieAdapter = new searchedMoviesAdapter(savedMoviesList, this, cardWidth);
        recyclerViewSaved.setAdapter(movieAdapter);

    }

    private void loadPopular() throws JSONException {
        if(!popularMovieList.isEmpty()){
            movieAdapter = new searchedMoviesAdapter(popularMovieList, this, cardWidth);
            recyclerViewPopular.setAdapter(movieAdapter);
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
                        movieAdapter = new searchedMoviesAdapter(popularMovieList, this, cardWidth);
                        recyclerViewPopular.setAdapter(movieAdapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("MovieFetcher", "JSON parsing error: " + e.getMessage());
                    }
                },
                error -> Log.e("MovieFetcher", "Error: " + error.getMessage())
        );

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    private void loadTrending() throws JSONException {
        
        if(!trendingMovieList.isEmpty()){
            movieAdapter = new searchedMoviesAdapter(trendingMovieList, this, cardWidth);
            recyclerViewTrending.setAdapter(movieAdapter);
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
                        movieAdapter = new searchedMoviesAdapter(trendingMovieList, this, cardWidth);
                        recyclerViewTrending.setAdapter(movieAdapter);
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


    private TextView createTextView(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextColor(getColor(R.color.white));
        textView.setTypeface(null, Typeface.BOLD);
        float pixelValue = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.TextSize), getResources().getDisplayMetrics());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, pixelValue);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(10, 10, 10, 10);
        textView.setLayoutParams(params);

        return textView;
    }

    private RecyclerView createRecyclerView(){
        RecyclerView recyclerView = new RecyclerView(this);
        recyclerView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));

        return recyclerView;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSaved();
    }
}