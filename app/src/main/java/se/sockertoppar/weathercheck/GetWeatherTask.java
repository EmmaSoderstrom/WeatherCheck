package se.sockertoppar.weathercheck;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;

/**
 * Created by User on 2017-05-02.
 */

public class GetWeatherTask extends AsyncTask<String, Void, JSONObject> {

    String TAG = "tag";

    MainActivity context;
    JSONObject topLevel = null;

    private RelativeLayout weatherLayout;
    ImageView weatherIcon;

    private String stringCity;


    public GetWeatherTask(MainActivity context, RelativeLayout weatherLayout) {
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


        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return topLevel;
    }

    @Override
    protected void onPostExecute(JSONObject jsonObjekt) {

        weatherIcon = (ImageView) weatherLayout.findViewById(R.id.weather_icon);
        TextView weatherIconText = (TextView) weatherLayout.findViewById(R.id.weather_icon_text);
        TextView weatherCity = (TextView) weatherLayout.findViewById(R.id.weather_city);
        TextView weatherCountry = (TextView) weatherLayout.findViewById(R.id.weather_country);
        TextView weatherTemp = (TextView) weatherLayout.findViewById(R.id.weather_temp);

        String iconCode = null;
        String iconUrl = null;
        String weatherDescription = null;
        String mainTemp = null;
        String name = null;
        String sysCountry = null;

        if(jsonObjekt != null) {
            try {

                JSONArray weather = jsonObjekt.getJSONArray("weather");
                JSONObject weatherObjekt = weather.getJSONObject(0);
                //icon
                iconCode = String.valueOf(weatherObjekt.getString("icon"));
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


            } catch (JSONException e) {
                e.printStackTrace();
            }


            new DownloadImage().execute(iconUrl);
            setMoodBackgund(iconCode);
            weatherIconText.setText(weatherDescription);
            weatherCity.setText(name);
            weatherCountry.setText(sysCountry);

            // tar bort så det bara blir en decimal utan att runda av?
            //mainTemp = Math.round((mainTemp / sum * 100) * 10) / 10;
            //DecimalFormat df = new DecimalFormat("#.##");
            //df.format(mainTemp);
            //if(mainTemp.length() >) {
                //mainTemp = mainTemp.substring(0, 3);
            //}
            weatherTemp.setText(mainTemp);
        }
    }

    public void setMoodBackgund(String iconUrl){
        Log.d(TAG, "changeModeBackgrund: iconUrl " + iconUrl);
        RelativeLayout mainLayout = (RelativeLayout) context.findViewById(R.id.main_layout);


        switch (iconUrl){
            case "01d": //sol
                mainLayout.setBackgroundResource(R.drawable.backgound_gradient_sun);
                break;
            case "02d": //lite moln
                mainLayout.setBackgroundResource(R.drawable.backgound_gradient_few_clouds);
                break;
            case "03d": //tunna moln
                mainLayout.setBackgroundResource(R.drawable.backgound_gradient_scattered_clouds);
                break;
            case "04d": //moln
                mainLayout.setBackgroundResource(R.drawable.backgound_gradient_broken_clouds);
                break;
            case "09d": //mycket regn / showerrain
                mainLayout.setBackgroundResource(R.drawable.backgound_gradient_showerrain);
                break;
            case "10d": //lite regn
                mainLayout.setBackgroundResource(R.drawable.backgound_gradient_rain);
                break;
            case "11d": //åska
                mainLayout.setBackgroundResource(R.drawable.backgound_gradient_thunder);
                break;
            case "13d": //snö
                mainLayout.setBackgroundResource(R.drawable.backgound_gradient_snow);

                break;
            case "50d": //dimma
                mainLayout.setBackgroundResource(R.drawable.backgound_gradient_mist);
                break;
            default:
                mainLayout.setBackgroundResource(R.drawable.backgound_gradient_night);
                break;

        }
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