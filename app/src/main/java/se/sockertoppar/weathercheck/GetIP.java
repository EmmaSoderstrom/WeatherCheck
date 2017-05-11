package se.sockertoppar.weathercheck;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by User on 2017-05-10.
 */

public class GetIP extends AsyncTask<String, Void, JSONObject> {

    String TAG = "tag";

    MainActivity context;
    JSONObject topLevel = null;

    String city = null;

    
    public GetIP(MainActivity context, LinearLayout weatherLayout) {
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

            Log.d(TAG, "response" + builder.toString());
            topLevel = new JSONObject(builder.toString());


        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return topLevel;
    }

    @Override
    protected void onPostExecute(JSONObject jsonObjekt) {
        Log.d(TAG, "onPostExecute: ");
        Log.d(TAG, "onPostExecute: jsonObjekt " + jsonObjekt);

        try {

            city = String.valueOf(jsonObjekt.getString("city"));
            Log.d(TAG, "onPostExecute: city " + city);

            context.setIPCity(city);
            context.setLatLong(String.valueOf(jsonObjekt.getString("latitude")),
                    String.valueOf(jsonObjekt.getString("longitude")));

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }
    
}