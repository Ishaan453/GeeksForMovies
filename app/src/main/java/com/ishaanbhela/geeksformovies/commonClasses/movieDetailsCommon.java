package com.ishaanbhela.geeksformovies.commonClasses;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.ishaanbhela.geeksformovies.MyApp;
import com.ishaanbhela.geeksformovies.movieDetails;
import com.ishaanbhela.geeksformovies.watchOptions.watchOptionAdapter;
import com.ishaanbhela.geeksformovies.watchOptions.watchOptionsModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class movieDetailsCommon {
    String url = MyApp.url;

    public interface TrailerCallback {
        void onSuccess(String result);
        void onError(String error);
    }

    public void fetchTrailerURL(Context context, int movieId, TrailerCallback callback) throws JSONException {
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
                        // System.out.println("GOT RESPONSE TRAILER FROM COMMON CLASS");
                        // System.out.println(response);
                        callback.onSuccess(response.getString("trailerURL"));
                    } catch (JSONException e) {
                        callback.onError("ERROR" + e);
                    }
                },
                error -> Log.e("MovieFetcher", "Error WHILE FETCHING: " + error.getMessage() + error)
        );
        Volley.newRequestQueue(context).add(jsonObjectRequest);
    }

    public interface watchOptionsCallback{
        void onSuccess(JSONObject response) throws JSONException;
        void onError(String error);
    }

    public void fetchWatchOptions(Context context, int movieId, watchOptionsCallback callback) throws JSONException {
        RequestQueue queue = Volley.newRequestQueue(context);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "watchOptions");
        jsonObject.put("movieId", ""+movieId);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject, response -> {
            try{
                callback.onSuccess(response);

            } catch (Exception e){
                Toast.makeText(context, "Error occured movieDetailsCommon.72", Toast.LENGTH_SHORT).show();
                callback.onError("SOME ERROR OCCURED");
            }
        }, error -> {
            callback.onError("SOME ERROR OCCURED movieDetailsCommon.76");
        });

        queue.add(jsonObjectRequest);
    }

    public void populateWatchOptionsSuccess(JSONObject response, List<watchOptionsModel> watchOptionList) throws JSONException {

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
    }
}
