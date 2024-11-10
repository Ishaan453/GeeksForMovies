package com.ishaanbhela.geeksformovies.Preferences;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
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
import com.ishaanbhela.geeksformovies.Database.SqLiteHelper;
import com.ishaanbhela.geeksformovies.MyApp;
import com.ishaanbhela.geeksformovies.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class userPreferenceFragmentClass extends Fragment {

    private Spinner languageSpinner, regionSpinner;
    private ChipGroup genreChipGroup;
    private Button addPreference;
    private SeekBar ratingSeekBar, runtimeSeekBar;
    private TextView ratingText, runtimeText;
    private CheckBox noPreferenceRating, noPreferenceRuntime;

    public Boolean preferenceChanged = false;


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
        addPreference = view.findViewById(R.id.savePreferenceButton);
        ratingSeekBar = view.findViewById(R.id.ratingSeekBar);
        runtimeSeekBar = view.findViewById(R.id.runtimeSeekBar);
        ratingText = view.findViewById(R.id.ratingValue);
        runtimeText = view.findViewById(R.id.runtimeValue);
        noPreferenceRuntime = view.findViewById(R.id.noPreferenceRuntime);
        noPreferenceRating = view.findViewById(R.id.noPreferenceRating);

        languages = new ArrayList<>();
        regions = new ArrayList<>();
        genres = new ArrayList<>();

        try {
            fetchPreferencesData();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "ERROR", Toast.LENGTH_SHORT).show();
        }

        addPreference.setOnClickListener(v -> {
            savePreference();
            Toast.makeText(requireContext(), "Preference Saved Successfully", Toast.LENGTH_SHORT).show();
            System.out.println(preferenceChanged);
        });



        noPreferenceRating.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                ratingSeekBar.setProgress(0);
                ratingSeekBar.setEnabled(false);
                ratingText.setText("No Preference");
            } else {
                ratingText.setText(String.valueOf(ratingSeekBar.getProgress()));
                ratingSeekBar.setEnabled(true);
            }
        });

        // Handle No Preference for Runtime
        noPreferenceRuntime.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                runtimeSeekBar.setProgress(0);
                runtimeSeekBar.setEnabled(false);
                runtimeText.setText("No Preference");
            } else {
                runtimeText.setText(String.valueOf(runtimeSeekBar.getProgress()));
                runtimeSeekBar.setEnabled(true);
            }
        });

        ratingSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ratingText.setText(String.valueOf(progress/2.0f));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        runtimeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                runtimeText.setText(String.valueOf(progress/2.0f));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void autoSelectSavedPreference(preferenceModel savedPreference) {
        for (int i = 0; i < languageSpinner.getCount(); i++) {
            languageModel language = (languageModel) languageSpinner.getItemAtPosition(i);
            if (language.getIso_639_1().equals(savedPreference.getPreferredLanguage())) {
                languageSpinner.setSelection(i);
                break;
            }
        }

        for (int i = 0; i < regionSpinner.getCount(); i++) {
            regionModel region = (regionModel) regionSpinner.getItemAtPosition(i);
            if (region.getIso_3166_1().equals(savedPreference.getPreferredRegion())) {
                regionSpinner.setSelection(i);
                break;
            }
        }

        try {
            JSONArray savedGenresArray = new JSONArray(savedPreference.getPreferredGenres());
            for (int i = 0; i < genreChipGroup.getChildCount(); i++) {
                Chip chip = (Chip) genreChipGroup.getChildAt(i);
                genresModel genre = (genresModel) chip.getTag();
                for (int j = 0; j < savedGenresArray.length(); j++) {
                    JSONObject savedGenre = savedGenresArray.getJSONObject(j);
                    int savedGenreId = savedGenre.getInt("id");
                    if (savedGenreId == genre.getId()) {
                        chip.setChecked(true);
                        break;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            // Handle any errors while parsing the saved genres
        }

        if (!savedPreference.getPreferredVoteAvg().equals("No Preference")) {
            ratingSeekBar.setProgress((int) (Float.parseFloat(savedPreference.getPreferredVoteAvg())*2));
            ratingText.setText(savedPreference.getPreferredVoteAvg());
        } else {
            noPreferenceRating.setChecked(true);
        }

        // Auto-select the runtime preference
        if (!savedPreference.getPreferredRuntime().equals("No Preference")) {
            runtimeSeekBar.setProgress(Integer.parseInt(savedPreference.getPreferredRuntime()));
            runtimeText.setText(savedPreference.getPreferredRuntime());
        } else {
            noPreferenceRuntime.setChecked(true);
        }
    }

    private void savePreference() {
        languageModel selectedLanguage = (languageModel) languageSpinner.getSelectedItem();
        regionModel selectedRegion = (regionModel) regionSpinner.getSelectedItem();
        List<genresModel> selectedGenres = new ArrayList<>();

        for (int i = 0; i < genreChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) genreChipGroup.getChildAt(i);
            if (chip.isChecked()) {
                System.out.println("CHIP ADDED");
                genresModel selectedGenre = (genresModel) chip.getTag();
                selectedGenres.add(selectedGenre);
            }
        }

        JSONArray selectedGenresJsonArray = new JSONArray();
        try{
            for (genresModel genre : selectedGenres) {
                JSONObject genreJson = new JSONObject();
                genreJson.put("id", genre.getId());
                genreJson.put("name", genre.getName());
                selectedGenresJsonArray.put(genreJson);
            }
        }
        catch (Exception e){
            System.out.println("Some error occurred");
            return;
        }
        String ratingPreference = "0";
        if(!ratingSeekBar.isSelected()){
            ratingPreference = ratingText.getText().toString();
        }
        String runtimePreference = "0";
        if(!runtimeSeekBar.isSelected()){
            runtimePreference = runtimeText.getText().toString();
        }

        preferenceModel preference = new preferenceModel(selectedLanguage.getIso_639_1(), selectedGenresJsonArray.toString(), selectedRegion.getIso_3166_1(), ratingPreference, "No Preference", runtimePreference);
        try {
            SqLiteHelper db = new SqLiteHelper(requireContext());
            db.saveUserPreferences(preference);
        }
        catch (Exception e){

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

                        SqLiteHelper db = new SqLiteHelper(requireContext());
                        preferenceModel savedPreference = db.getUserPreferences();
                        if(savedPreference != null){
                            autoSelectSavedPreference(savedPreference);
                        }

                    } catch (Exception e) {
                        System.out.println("Some error is occurring when parsing preferences response");
                    }
                },
                error -> {
                    System.out.println(error.toString());
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

        languages.sort((lang1, lang2) -> lang1.getEnglish_name().compareToIgnoreCase(lang2.getEnglish_name()));
        languages.add(0, new languageModel("0000", "No Preference", "No Preference"));
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

        regions.sort((reg1, reg2) -> reg1.getEnglish_name().compareToIgnoreCase(reg2.getEnglish_name()));
        regions.add(0, new regionModel("0000", "No Preference", "No Preference"));
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
                chip.setTag(g);
                chip.setBackgroundColor(getResources().getColor(R.color.lightLavender));
                chip.setCheckable(true);
                chip.setChecked(false);
                genreChipGroup.addView(chip);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
