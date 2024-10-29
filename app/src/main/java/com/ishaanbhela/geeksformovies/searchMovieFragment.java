package com.ishaanbhela.geeksformovies;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.ishaanbhela.geeksformovies.searchedMovies.searchedMoviesAdapter;
import com.ishaanbhela.geeksformovies.searchedMovies.searchedMoviesModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class searchMovieFragment extends Fragment {

    private static final String ARG_SEARCHED = "searched";
    public String searched;
    private String prevSearch = "";
    private RecyclerView searchedMovies;
    private List<searchedMoviesModel> movieList;
    private String url = MyApp.url;
    private JSONObject jsonObject;
    private TextView infoText;
    public ProgressBar progressBar;
    public boolean errorOccurred;

    // Factory method to create a new instance of the fragment with arguments
    public static searchMovieFragment newInstance(String searched) {
        searchMovieFragment fragment = new searchMovieFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SEARCHED, searched); // Put the searched keyword in the bundle
        fragment.setArguments(args); // Set the arguments to the fragment
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.search_fragment_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        searchedMovies = view.findViewById(R.id.searchedMovies);
        movieList = new ArrayList<>();
        infoText = view.findViewById(R.id.info_text);
        progressBar = view.findViewById(R.id.searchProgress);

        searchedMovies.setLayoutManager(new LinearLayoutManager(requireContext()));

        if (getArguments() != null) {
            searched = getArguments().getString(ARG_SEARCHED);
        }

        try {
            progressBar.setVisibility(View.VISIBLE);
            loadMovies(1);
        } catch (JSONException e) {
            Toast.makeText(view.getContext(), "ERROR OCCURRED", Toast.LENGTH_SHORT).show();
        }
    }

    public void loadMovies(int pageno) throws JSONException {
        if(searched.equals("Trending")){
            jsonObject = new JSONObject();
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
                            addMovie(results, movieList);
                            searchedMoviesAdapter movieAdapter = new searchedMoviesAdapter(movieList, requireContext(), 0);
                            searchedMovies.setAdapter(movieAdapter);
                            progressBar.setVisibility(View.GONE);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("MovieFetcher", "JSON parsing error: " + e.getMessage());
                        }
                    },
                    error -> Log.e("MovieFetcher", "Error: " + error.getMessage())
            );
            Volley.newRequestQueue(requireContext()).add(jsonObjectRequest);
        }

        else{
            if(searched.equals(prevSearch)){
                searchedMoviesAdapter movieAdapter = new searchedMoviesAdapter(movieList, requireContext(), 0);
                searchedMovies.setAdapter(movieAdapter);
            }

            infoText.setText("Searched for \"" + searched + "\"");
            movieList.clear();
            prevSearch = searched;

            jsonObject = new JSONObject();
            jsonObject.put("type", "search");
            jsonObject.put("search", searched);

            // Handle error
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                    response -> {
                        try {
                            System.out.println("GOT RESPONSE FOR SEARCHED MOVIE");
                            JSONArray results = response.getJSONArray("results");
                            addMovie(results, movieList);
                            searchedMoviesAdapter movieAdapter = new searchedMoviesAdapter(movieList, requireContext(), 0);
                            searchedMovies.setAdapter(movieAdapter);
                            progressBar.setVisibility(View.GONE);
                            errorOccurred = false;
                        } catch (JSONException e) {
                            e.printStackTrace();
                            errorOccurred = true;
                        }
                    },
                    error -> {
                        Toast.makeText(requireContext(), "Unable to load searched movie. Try again", Toast.LENGTH_SHORT).show();
                        errorOccurred = true;
                    });

            // Add the request to the Volley request queue
            Volley.newRequestQueue(requireContext()).add(jsonObjectRequest);
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
}
