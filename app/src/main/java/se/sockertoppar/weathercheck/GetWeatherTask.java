package se.sockertoppar.weathercheck;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.R.attr.data;

/**
 * Created by User on 2017-05-02.
 */

public class GetWeatherTask extends AsyncTask<String, Void, String> {

    String TAG = "tag";
    private TextView textView;
    private RelativeLayout relLayout;
    private String stringCity;




    public GetWeatherTask(TextView textView) {
        this.textView = textView;
    }

    public GetWeatherTask(RelativeLayout relLayout) {
        this.relLayout = relLayout;
    }

    @Override
    protected String doInBackground(String... strings) {

        //String weatherArry[] = {3};
        String weatherArry[] = new String[3];
        String mainTemp = "UNDEFINED";
        //String weatherMain = "UNDEFINED";
        //weatherDescription = "UNDEFINED";


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
            JSONObject topLevel = new JSONObject(builder.toString());

            JSONObject main = topLevel.getJSONObject("main");
            mainTemp = String.valueOf(main.getDouble("temp"));


            JSONArray weather = topLevel.getJSONArray("weather");
            JSONObject weatherObjekt = weather.getJSONObject(0);

            //String.valueOf(weatherObjekt.getInt("id"));
            String weatherDescription = String.valueOf(weatherObjekt.getString("description"));
            //String.valueOf(weatherObjekt.getString("main"));
            //String.valueOf(weatherObjekt.getString("icon"));

            Log.d(TAG, "doInBackground: weatherMain " + weatherDescription );




        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return mainTemp;
    }

    @Override
    protected void onPostExecute(String temp) {
        textView.setText("Current Weather: " + temp);

        //double  celsius = Double.parseDouble(temp) - 273.15;
        //textView.setText("Current Weather: " + celsius);
    }
}