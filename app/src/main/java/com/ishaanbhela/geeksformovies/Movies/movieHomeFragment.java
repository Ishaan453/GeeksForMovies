package com.ishaanbhela.geeksformovies.Movies;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.ishaanbhela.geeksformovies.Database.SqLiteHelper;
import com.ishaanbhela.geeksformovies.MyApp;
import com.ishaanbhela.geeksformovies.Preferences.preferenceModel;
import com.ishaanbhela.geeksformovies.Preferences.userPreferenceFragmentClass;
import com.ishaanbhela.geeksformovies.R;
import com.ishaanbhela.geeksformovies.searchedMovies.searchedMoviesAdapter;
import com.ishaanbhela.geeksformovies.searchedMovies.searchedMoviesModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class movieHomeFragment extends Fragment {

    private RecyclerView recyclerViewTrending, recyclerViewPopular, recyclerViewTopRated, recyclerViewUpcoming, recyclerViewSaved, recyclerViewPreference;
    private searchedMoviesAdapter movieAdapter;
    private List<searchedMoviesModel> trendingMovieList;
    private List<searchedMoviesModel> popularMovieList;
    private List<searchedMoviesModel> topRatedMovieList, upcomingMovieList, preferenceMovieList;
    private TextView trending, popular, saved, topRated, upcoming, preference, NoFound;
    private ImageView searchIcon;
    private EditText searchEditText;
    private searchMovieFragment fragment;
    private SwipeRefreshLayout refreshLayout;
    private String previousPreferenceURL;
    ProgressBar progressBarTrending, progressBarPopular, progressBarTopRated, progressBarUpcoming, progressBarSaved, progressBarPreference;
    LinearLayout content;
    String url = MyApp.url;
    int cardWidth;
    String basePreferenceURL = "https://api.themoviedb.org/3/discover/movie?include_adult=false&include_video=false&language=en-US&page=1&sort_by=popularity.desc&primary_release_date.gte=2012-01-01";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.movie_home_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        content = view.findViewById(R.id.content);
        trendingMovieList = new ArrayList<>();
        searchIcon = view.findViewById(R.id.searchIcon);
        searchEditText = view.findViewById(R.id.editText);
        refreshLayout = view.findViewById(R.id.refreshLayout);
        popularMovieList = new ArrayList<>();
        trendingMovieList = new ArrayList<>();
        topRatedMovieList = new ArrayList<>();
        upcomingMovieList = new ArrayList<>();
        preferenceMovieList = new ArrayList<>();

        cardWidth = (int) getResources().getDimension(R.dimen.searchImageSize);

        if(isAdded()){
            int index = 0;

            preference = createTextView("Your Preferences");
            content.addView(preference, index++);
            NoFound = createTextView("\t\t\t\t\t\tNo Movies Found");
            NoFound.setVisibility(View.GONE);
            content.addView(NoFound, index++);
            progressBarPreference = createProgressBar();
            content.addView(progressBarPreference, index++);
            progressBarPreference.setVisibility(View.VISIBLE);
            recyclerViewPreference = createRecyclerView();
            content.addView(recyclerViewPreference, index++);

            trending = createTextView("Global Trending");
            content.addView(trending, index++);
            progressBarTrending = createProgressBar();
            content.addView(progressBarTrending, index++);
            progressBarTrending.setVisibility(View.VISIBLE);
            recyclerViewTrending = createRecyclerView();
            content.addView(recyclerViewTrending, index++);

            popular = createTextView("Global Popular");
            content.addView(popular, index++);
            progressBarPopular = createProgressBar();
            content.addView(progressBarPopular, index++);
            progressBarPopular.setVisibility(View.VISIBLE);
            recyclerViewPopular = createRecyclerView();
            content.addView(recyclerViewPopular, index++);

            topRated = createTextView("Global Top Rated");
            content.addView(topRated, index++);
            progressBarTopRated = createProgressBar();
            content.addView(progressBarTopRated, index++);
            progressBarTopRated.setVisibility(View.VISIBLE);
            recyclerViewTopRated = createRecyclerView();
            content.addView(recyclerViewTopRated, index++);

            upcoming = createTextView("Global Upcoming");
            content.addView(upcoming, index++);
            progressBarUpcoming = createProgressBar();
            content.addView(progressBarUpcoming, index++);
            progressBarUpcoming.setVisibility(View.VISIBLE);
            recyclerViewUpcoming = createRecyclerView();
            content.addView(recyclerViewUpcoming, index++);

            try {
                loadAll();
            } catch (JSONException e) {
                if(isAdded()){
                    Toast.makeText(requireContext(), "UNABLE TO LOAD UPCOMING MOVIES", Toast.LENGTH_SHORT).show();
                }

            }


            saved = createTextView("Saved");
            content.addView(saved, index++);

            recyclerViewSaved = createRecyclerView();
            content.addView(recyclerViewSaved, index++);
            loadSaved();
        }



        searchIcon.setOnClickListener(v -> {
            try {
                if(isAdded()) search();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)){
                try {
                    if(isAdded()) search();
                    return true;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            return false;
        });

        refreshLayout.setOnRefreshListener(() -> {
            try {
                loadAll();
                refreshLayout.setRefreshing(false);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });

    }


    private void loadAll() throws JSONException {
        if(!upcomingMovieList.isEmpty() && !topRatedMovieList.isEmpty() && !trendingMovieList.isEmpty() && !popularMovieList.isEmpty() && isAdded()){
            movieAdapter = new searchedMoviesAdapter(upcomingMovieList, requireContext(), cardWidth);
            recyclerViewUpcoming.setAdapter(movieAdapter);

            movieAdapter = new searchedMoviesAdapter(topRatedMovieList, requireContext(), cardWidth);
            recyclerViewTopRated.setAdapter(movieAdapter);

            movieAdapter = new searchedMoviesAdapter(popularMovieList, requireContext(), cardWidth);
            recyclerViewPopular.setAdapter(movieAdapter);

            movieAdapter = new searchedMoviesAdapter(trendingMovieList, requireContext(), cardWidth);
            recyclerViewTrending.setAdapter(movieAdapter);
            return;
        }

        String preferenceURL = getPreferenceURL();
        previousPreferenceURL = preferenceURL;
        System.out.println(preferenceURL);


        JSONObject jsonRequest = new JSONObject();
        jsonRequest.put("type", "home_page");
        jsonRequest.put("preferenceURL", preferenceURL);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonRequest,
                response -> {
                    try {
                        if(!preferenceURL.equals(basePreferenceURL)){
                            JSONObject preferenceObj = response.getJSONObject("preference");
                            JSONArray preferenceArr = preferenceObj.getJSONArray("results");
                            addMovie(preferenceArr, preferenceMovieList);
                            if(isAdded()){
                                movieAdapter = new searchedMoviesAdapter(preferenceMovieList, requireContext(), cardWidth);
                                recyclerViewPreference.setAdapter(movieAdapter);
                                progressBarPreference.setVisibility(View.GONE);

                                if(preferenceMovieList.isEmpty()){
                                    NoFound.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                        else{
                            this.preference.setVisibility(View.GONE);
                            this.progressBarPreference.setVisibility(View.GONE);
                            this.recyclerViewPreference.setVisibility(View.GONE);
                        }

                        JSONObject trendingObj = response.getJSONObject("trending");
                        JSONArray trendingArr = trendingObj.getJSONArray("results");
                        addMovie(trendingArr, trendingMovieList);
                        if(isAdded()){
                            movieAdapter = new searchedMoviesAdapter(trendingMovieList, requireContext(), cardWidth);
                            recyclerViewTrending.setAdapter(movieAdapter);
                            progressBarTrending.setVisibility(View.GONE);
                        }

                        JSONObject popularObj = response.getJSONObject("popular");
                        JSONArray popularArr = popularObj.getJSONArray("results");
                        addMovie(popularArr, popularMovieList);
                        if(isAdded()){
                            movieAdapter = new searchedMoviesAdapter(popularMovieList, requireContext(), cardWidth);
                            recyclerViewPopular.setAdapter(movieAdapter);
                            progressBarPopular.setVisibility(View.GONE);
                        }

                        JSONObject topRatedObj = response.getJSONObject("toprated");
                        JSONArray topRatedArr = topRatedObj.getJSONArray("results");
                        addMovie(topRatedArr, topRatedMovieList);
                        if(isAdded()){
                            movieAdapter = new searchedMoviesAdapter(topRatedMovieList, requireContext(), cardWidth);
                            recyclerViewTopRated.setAdapter(movieAdapter);
                            progressBarTopRated.setVisibility(View.GONE);
                        }

                        JSONObject upcomingObj = response.getJSONObject("upcoming");
                        JSONArray upcomingArr = upcomingObj.getJSONArray("results");
                        addMovie(upcomingArr, upcomingMovieList);
                        if(isAdded()){
                            movieAdapter = new searchedMoviesAdapter(upcomingMovieList, requireContext(), cardWidth);
                            recyclerViewUpcoming.setAdapter(movieAdapter);
                            progressBarUpcoming.setVisibility(View.GONE);
                        }

                    } catch (JSONException e) {
                        if(isAdded()){
                            Toast.makeText(requireContext(), "Some error occurred while parsing response", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                error -> {
                    if (isAdded()){
                        Toast.makeText(requireContext(), "Unable to load Upcoming Movies. Try again", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        if(isAdded()){
            Volley.newRequestQueue(requireContext()).add(jsonObjectRequest);
        }

    }

    private void loadSaved() {
        if(isAdded()){
            SqLiteHelper dbHelper = new SqLiteHelper(requireContext());  // Initialize the SQLite helper
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
            movieAdapter = new searchedMoviesAdapter(savedMoviesList, requireContext(), cardWidth);
            recyclerViewSaved.setAdapter(movieAdapter);
        }
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
        TextView textView = new TextView(requireContext());
        textView.setText(text);
        textView.setTextColor(getResources().getColor(R.color.white, null));
        textView.setTypeface(null, Typeface.BOLD);
        float pixelValue = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.TextSize), getResources().getDisplayMetrics());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, pixelValue);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        int m_l_r_t = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.margin_l_r_t), getResources().getDisplayMetrics());
        int m_bottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.margin_bottom), getResources().getDisplayMetrics());
        params.setMargins(m_l_r_t, 0, m_l_r_t, m_bottom);
        textView.setElevation(5);
        textView.setLayoutParams(params);

        return textView;
    }

    private RecyclerView createRecyclerView(){
        RecyclerView recyclerView = new RecyclerView(requireContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(
                0,
                0,
                0,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.margin_l_r_t), getResources().getDisplayMetrics()));
        recyclerView.setLayoutParams(params);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));

        return recyclerView;
    }

    private ProgressBar createProgressBar(){
        ProgressBar progressBar = new ProgressBar(requireContext());
        progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.white, null), android.graphics.PorterDuff.Mode.MULTIPLY);
        progressBar.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        return progressBar;
    }

    private void search() throws JSONException {
        if(searchEditText.getText().toString().isEmpty() && isAdded()){
            Toast.makeText(requireContext(), "No search value provided", Toast.LENGTH_SHORT).show();
        }
        else{
            searchMovieFragment existingFragment = (searchMovieFragment) requireActivity().getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if(existingFragment == null){
                String searchedKeyword = searchEditText.getText().toString().trim();
                fragment = searchMovieFragment.newInstance(searchedKeyword);
                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();

                transaction.setCustomAnimations(R.anim.search_fragment_in, R.anim.search_fragment_out, R.anim.search_fragment_in, R.anim.search_fragment_out);

                transaction.add(R.id.fragment_container, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }

            else{
                if(!existingFragment.searched.equals(searchEditText.getText().toString()) || existingFragment.errorOccurred){
                    existingFragment.progressBar.setVisibility(View.VISIBLE);
                    existingFragment.searched = searchEditText.getText().toString();
                    existingFragment.loadMovies(1);
                    System.out.println("CHANGED");
                }
            }
        }
    }

    public String getPreferenceURL() throws JSONException {
        SqLiteHelper db = new SqLiteHelper(requireContext());
        preferenceModel preference = db.getUserPreferences();
        StringBuilder genreIdsBuilder = new StringBuilder();
        JSONArray genreArray = new JSONArray(preference.getPreferredGenres());

        if (genreArray.length() > 0) {
            for (int i = 0; i < genreArray.length(); i++) {
                JSONObject genreObject = genreArray.getJSONObject(i);
                String genreId = genreObject.getString("id");

                if (genreIdsBuilder.length() > 0) {
                    genreIdsBuilder.append("|");
                }
                genreIdsBuilder.append(genreId);
            }
        }

        String preferredLanguage = preference.getPreferredLanguage();
        String preferredRegion = preference.getPreferredRegion();
        String preferredRatingGTE = preference.getPreferredVoteAvg();
        String preferredRuntimeLTE = preference.getPreferredRuntime();
        // String preferredWatchOption = preference.getPreferredWatchOptions();

        StringBuilder preferenceURL = new StringBuilder(basePreferenceURL);
        if(!preferredLanguage.equals("0000")){
            preferenceURL.append("&with_original_language=").append(preferredLanguage);
        }
        if(!preferredRegion.equals("0000")){
            preferenceURL.append("&watch_region=").append(preferredRegion);
        }
        if(!preferredRatingGTE.equals("No Preference")){
            preferenceURL.append("&vote_average.gte=").append(preferredRatingGTE);
        }
        if(!preferredRuntimeLTE.equals("No Preference")){
            preferenceURL.append("&with_runtime.lte=").append(preferredRuntimeLTE);
        }
        if(!genreIdsBuilder.toString().isEmpty()){
            preferenceURL.append("&with_genres=").append(genreIdsBuilder);
        }

        return preferenceURL.toString();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSaved();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if(!hidden){
            System.out.println("ON RESUME CALLED");

                JSONObject jsonRequest = new JSONObject();

                String preferenceURL = null;
                try {
                    preferenceURL = getPreferenceURL();
                    if(preferenceURL.equals(previousPreferenceURL)){
                        return;
                    }
                    previousPreferenceURL = preferenceURL;
                    System.out.println(preferenceURL);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                try {
                    jsonRequest.put("type", "preference");
                    jsonRequest.put("preferenceURL", preferenceURL);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }


                String finalPreferenceURL = preferenceURL;
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                        Request.Method.POST,
                        url,
                        jsonRequest,
                        response -> {
                            try {

                                System.out.println(response);
                                if(!finalPreferenceURL.equals(basePreferenceURL)){
                                    System.out.println("IN IFFF");
                                    preferenceMovieList = new ArrayList<>();
                                    this.preference.setVisibility(View.VISIBLE);
                                    this.progressBarPreference.setVisibility(View.VISIBLE);
                                    this.recyclerViewPreference.setVisibility(View.VISIBLE);
                                    JSONArray preferenceArr = response.getJSONArray("results");
                                    addMovie(preferenceArr, preferenceMovieList);
                                    if(isAdded()){
                                        movieAdapter = new searchedMoviesAdapter(preferenceMovieList, requireContext(), cardWidth);
                                        recyclerViewPreference.setAdapter(movieAdapter);
                                        progressBarPreference.setVisibility(View.GONE);
                                    }

                                    if(!preferenceMovieList.isEmpty()){
                                        NoFound.setVisibility(View.GONE);
                                    }
                                    else {
                                        NoFound.setVisibility(View.VISIBLE);
                                    }
                                }
                                else{
                                    this.preference.setVisibility(View.GONE);
                                    this.progressBarPreference.setVisibility(View.GONE);
                                    this.recyclerViewPreference.setVisibility(View.GONE);
                                }

                            } catch (JSONException e) {
                                if(isAdded()){
                                    Toast.makeText(requireContext(), "Some error occurred while parsing response", Toast.LENGTH_SHORT).show();
                                }
                            }
                        },
                        error -> {
                            if (isAdded()){
                                Toast.makeText(requireContext(), "Unable to load Upcoming Movies. Try again", Toast.LENGTH_SHORT).show();
                            }
                        }
                );
                if(isAdded()){
                    Volley.newRequestQueue(requireContext()).add(jsonObjectRequest);
                }
        }
    }
}
