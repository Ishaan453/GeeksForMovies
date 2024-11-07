package com.ishaanbhela.geeksformovies.Preferences;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.ishaanbhela.geeksformovies.MyApp;
import com.ishaanbhela.geeksformovies.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class userPreferenceFragmentClass extends Fragment {

    private Spinner languageSpinner, regionSpinner;
    private ChipGroup genreChipGroup;
    List<languageModel> languages;
    List<regionModel> regions;
    List<genresModel> genres;

    String url = MyApp.url;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.preference_layout, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        languageSpinner = view.findViewById(R.id.languageSpinner);
        regionSpinner = view.findViewById(R.id.regionSpinner);
        genreChipGroup = view.findViewById(R.id.genresChipGroup);

        languages = new ArrayList<>();
        regions = new ArrayList<>();
        genres = new ArrayList<>();

        try {
            fetchPreferencesData();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "ERROR", Toast.LENGTH_SHORT).show();
        }
    }


    private void fetchPreferencesData() throws JSONException {
        System.out.println("Fetching Data");

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        JSONObject object = new JSONObject();
        object.put("type", "user_preference");

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, object,
                response -> {
                    try {
                        JSONArray languagesArray = response.getJSONArray("languages");

                        JSONObject regionsObject = response.getJSONObject("regions");
                        JSONArray regionsArray = regionsObject.getJSONArray("results");

                        JSONObject genresObject = response.getJSONObject("genres");
                        JSONArray genresArray = genresObject.getJSONArray("genres");

                        populateLanguageSpinner(languagesArray);
                        populateRegionSpinner(regionsArray);
                        populateGenresChipGroup(genresArray);
                        System.out.println("Data added");

                    } catch (Exception e) {
                        System.out.println("Some error is occurring when parsing preferences response");
                    }
                },
                error -> {
                    // Handle error
                });

        queue.add(jsonObjectRequest);
    }

    private void populateLanguageSpinner(JSONArray languagesArray) throws IllegalStateException {
        for (int i = 0; i < languagesArray.length(); i++) {
            try {
                JSONObject language = languagesArray.getJSONObject(i);
                String english_name = language.getString("english_name");
                String iso_639_1 = language.getString("iso_639_1");
                String name = language.getString("name");
                languages.add(new languageModel(iso_639_1, english_name, name));
            } catch (JSONException e) {
                e.printStackTrace();
                System.out.println("Error while populating languages Spinner");
            }
        }

        ArrayAdapter<languageModel> languageAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, languages);
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(languageAdapter);
    }

    private void populateRegionSpinner(JSONArray regionsArray) throws IllegalStateException {
        for (int i = 0; i < regionsArray.length(); i++) {
            try {
                JSONObject region = regionsArray.getJSONObject(i);
                String english_name = region.getString("english_name");
                String iso_3166_1 = region.getString("iso_3166_1");
                String name = region.getString("native_name");
                regions.add(new regionModel(iso_3166_1, english_name, name));

            } catch (JSONException e) {
                e.printStackTrace();
                System.out.println("Error while populating Region Spinner");
            }
        }

        ArrayAdapter<regionModel> regionAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, regions);
        regionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        regionSpinner.setAdapter(regionAdapter);
    }

    private void populateGenresChipGroup(JSONArray genresArray) throws IllegalStateException {
        genreChipGroup.removeAllViews();
        for (int i = 0; i < genresArray.length(); i++) {
            try {
                JSONObject genre = genresArray.getJSONObject(i);
                String name = genre.getString("name");
                int id = genre.getInt("id");
                genresModel g = new genresModel(id, name);
                genres.add(g);

                Chip chip = new Chip(requireContext());
                chip.setText(g.toString());
                chip.setCheckable(true);
                chip.setChecked(false);
                genreChipGroup.addView(chip);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


}
