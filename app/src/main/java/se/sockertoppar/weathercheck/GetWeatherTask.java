package se.sockertoppar.weathercheck;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.net.URI;
import java.net.URL;

import static android.R.attr.data;

/**
 * Created by User on 2017-05-02.
 */

public class GetWeatherTask extends AsyncTask<String, Void, JSONObject> {

    String TAG = "tag";

    MainActivity context;
    JSONObject topLevel = null;

    private LinearLayout weatherLayout;
    ImageView weatherIcon;
    //View WeatherListItem;
    //private TextView weatherIconText;
    //private TextView weatherTemp;

    private String stringCity;


    /*public GetWeatherTask(TextView weatherTemp) {
        this.weatherTemp = weatherTemp;
    }*/

    public GetWeatherTask(MainActivity context, LinearLayout weatherLayout) {
        this.context = context;
        this.weatherLayout = weatherLayout;
    }

    @Override
    protected JSONObject doInBackground(String... strings) {

        String mainTemp = "UNDEFINED";


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

            //JSONObject main = topLevel.getJSONObject("main");
            //mainTemp = String.valueOf(main.getDouble("temp"));


            //JSONArray weather = topLevel.getJSONArray("weather");
            //JSONObject weatherObjekt = weather.getJSONObject(0);

            //String.valueOf(weatherObjekt.getInt("id"));
            //String weatherDescription = String.valueOf(weatherObjekt.getString("description"));
            //String.valueOf(weatherObjekt.getString("main"));
            //String.valueOf(weatherObjekt.getString("icon"));



        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return topLevel;
    }

    @Override
    protected void onPostExecute(JSONObject jsonObjekt) {

        /*LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        WeatherListItem = (View) inflater.inflate(R.layout.weather_list_item, null, true);
        weatherLayout.addView(WeatherListItem);*/

        weatherIcon = (ImageView) weatherLayout.findViewById(R.id.weather_icon);
        TextView weatherIconText = (TextView) weatherLayout.findViewById(R.id.weather_icon_text);
        TextView weatherCity = (TextView) weatherLayout.findViewById(R.id.weather_city);
        TextView weatherCountry = (TextView) weatherLayout.findViewById(R.id.weather_country);
        TextView weatherTemp = (TextView) weatherLayout.findViewById(R.id.weather_temp);

        //JSONObject main = null;
        String iconUrl = null;
        String weatherDescription = null;
        String mainTemp = null;
        String name = null;
        String sysCountry = null;

        try {

            if(jsonObjekt != null) {
                JSONArray weather = jsonObjekt.getJSONArray("weather");
                JSONObject weatherObjekt = weather.getJSONObject(0);
                //icon
                String iconCode = String.valueOf(weatherObjekt.getString("icon"));
                iconUrl = "http://openweathermap.org/img/w/" + iconCode + ".png";

                //icon text
                weatherDescription = String.valueOf(weatherObjekt.getString("description"));

                //temperatur
                JSONObject main = jsonObjekt.getJSONObject("main");
                mainTemp = String.valueOf(main.getDouble("temp"));

                //stad namn
                name = String.valueOf(jsonObjekt.getString("name"));
                //lands kod
                JSONObject sys = jsonObjekt.getJSONObject("sys");
                sysCountry = String.valueOf(sys.getString("country"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


        new DownloadImage().execute(iconUrl);
        weatherIconText.setText(weatherDescription);
        weatherCity.setText(name);
        weatherCountry.setText(", " + sysCountry);
        weatherTemp.setText(mainTemp + " °c");

        //weatherTemp = (TextView) weatherLayout.findViewById(R.id.weather_temp);
        //weatherTemp.setText(mainTemp + " °c");
    }

    private class DownloadImage extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... URL) {

            String imageURL = URL[0];

            Bitmap bitmap = null;
            try {
                // Download Image from URL
                InputStream input = new java.net.URL(imageURL).openStream();
                // Decode Bitmap
                bitmap = BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            weatherIcon.setImageBitmap(result);
        }
    }
}
