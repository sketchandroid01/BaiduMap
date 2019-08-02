package com.baidumap;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.microsoft.maps.GeoboundingBox;
import com.microsoft.maps.Geolocation;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

class LocalSearch {


    public static final String CREDENTIALS_KEY = "AlHHm7gEo-WeqL_cjJYw2-Gx9Ehmn6Ie5E8LZe17v9a-H2JQBO4cBenPpSjFnvPS";

    private static final String URL_ENDPOINT
            = "http://dev.virtualearth.net/REST/v1/Locations/"
            + "{query}" + "?"
            + "&includeNeighborhood="
            + "&maxResults=1"
            + "&include="
            + "&key=" + CREDENTIALS_KEY;

    static class Poi {
        String name;
        Geolocation location;
    }

    interface Callback {
        void onSuccess(List<Poi> results);
        void onFailure();
    }

    static void sendRequest(Context context, String query, GeoboundingBox bounds,
                            Callback callback) {
        if (query == null || query.isEmpty()) {
            Toast.makeText(context, "Invalid query", Toast.LENGTH_LONG).show();
            return;
        }

        String boundsStr = String.format(Locale.ROOT,
            "%.6f,%.6f,%.6f,%.6f",
            bounds.south, bounds.west, bounds.north, bounds.east);

        String url = URL_ENDPOINT.replace("{query}", query);
        Log.d("TAG", "url = "+url);

        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(new StringRequest(
            Request.Method.GET,
            URL_ENDPOINT.replace("{query}", query),
            (String response) -> {
                List<Poi> results = parse(response);
                if (results == null || results.isEmpty()) {
                    callback.onFailure();
                    return;
                }
                callback.onSuccess(results);
            },
            (VolleyError error) -> {
                callback.onFailure();
                Log.d("TAG", "error = "+error);
            }
        ));
    }

    private static List<Poi> parse(String json) {
        List<Poi> results = new ArrayList<>();

        try {

            Log.d("TAG", "Json = "+json);

            JSONObject responseJObj = new JSONObject(json);
            JSONArray resourceSetsJArr = responseJObj.getJSONArray("resourceSets");
            JSONObject resourceSetJObj = resourceSetsJArr.getJSONObject(0);
            JSONArray resourcesJArr = resourceSetJObj.getJSONArray("resources");

            JSONObject resourceJObj = resourcesJArr.getJSONObject(0);
            Poi poi = new Poi();

            poi.name = resourceJObj.getString("name");

            JSONObject pointJObj = resourceJObj.getJSONObject("point");
            JSONArray coordinatesJArr = pointJObj.getJSONArray("coordinates");
            poi.location = new Geolocation(
                    coordinatesJArr.getDouble(0),
                    coordinatesJArr.getDouble(1));

            results.add(poi);

            /*for (int i = 0; i < resourcesJArr.length(); ++i) {
                JSONObject resourceJObj = resourcesJArr.getJSONObject(i);
                Poi poi = new Poi();

                poi.name = resourceJObj.getString("name");

                JSONObject pointJObj = resourceJObj.getJSONObject("point");
                JSONArray coordinatesJArr = pointJObj.getJSONArray("coordinates");
                poi.location = new Geolocation(
                        coordinatesJArr.getDouble(0),
                        coordinatesJArr.getDouble(1));

                results.add(poi);
            }*/
        }
        catch (Exception ex) {
            return null;
        }

        return results;
    }

}
