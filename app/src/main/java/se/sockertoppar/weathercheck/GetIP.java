package se.sockertoppar.weathercheck;


import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class GetIP extends AsyncTask<String, Void, JSONObject> {

    String TAG = "tag";

    MainActivity context;
    JSONObject topLevel = null;

    String city = null;

    
    public GetIP(MainActivity context) {
        this.context = context;
    }

    @Override
    protected JSONObject doInBackground(String... strings) {
        Log.d(TAG, "GetIP");

        try {
            URL url = new URL(strings[0]);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder builder = new StringBuilder();

            String inputString;
            while ((inputString = bufferedReader.readLine()) != null) {
                builder.append(inputString);
            }

            topLevel = new JSONObject(builder.toString());


        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return topLevel;
    }

    @Override
    protected void onPostExecute(JSONObject jsonObjekt) {
        Log.d(TAG, "onPostExecute ip: jsonObjekt " + jsonObjekt);

        try {
            city = String.valueOf(jsonObjekt.getString("city"));
            if(!city.equals("null")) {
                context.weatherSearch(city);
            }else {
                double lat = Double.parseDouble(jsonObjekt.getString("lat"));
                double lng = Double.parseDouble(jsonObjekt.getString("lon"));
                context.weatherSearchIp("lat=" + lat + "&lon=" + lng);
            }

            context.setLatLong(String.valueOf(jsonObjekt.getString("lat")),
                    String.valueOf(jsonObjekt.getString("lon")));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}