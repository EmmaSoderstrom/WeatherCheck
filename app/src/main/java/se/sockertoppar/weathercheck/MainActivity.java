package se.sockertoppar.weathercheck;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class MainActivity extends Activity implements AdapterView.OnItemClickListener {

    static String TAG = "tag";
    JSONObject data = null;

    String url;


    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";
    private static final String API_KEY_AUTOFILL = "AIzaSyDl0sL-EDUrQwepkBq5tuLeFwn67_mBxJE";

    private static final String WEATHER_API_BASE = "http://api.openweathermap.org/data/2.5/weather?q=";
    private String city = "Stockholm";
    private String units = "&units=metric";
    private String language = "&lang=se";
    private static final String API_KEY_WEATHER = "&appid=3a87cd70e4003ebbdf1c8272e812b2c2";

    AutoCompleteTextView autoCompView;
    EditText inputCity;
    LinearLayout weatherLayout;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        autoCompView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);
        autoCompView.setInputType(InputType.TYPE_CLASS_TEXT);
        weatherLayout = (LinearLayout) findViewById(R.id.weather_layout);

        //autofill textview
        autoCompView.setAdapter(new GooglePlacesAutocompleteAdapter(this, R.layout.city_list_item));
        autoCompView.setOnItemClickListener(this);

        //lägger till väder vyn
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View WeatherListItem = (View) inflater.inflate(R.layout.weather_list_item, null, true);
        weatherLayout.addView(WeatherListItem);

        //klick OnKeyListener för enter
        autoCompView.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // sker om det trycks enter
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {

                    weatherSearch();
                    return true;
                }
                return false;
            }
        });

    }

    public void onClickSearch(View view){
        //inputCity.requestFocus();
        weatherSearch();
    }

    public void weatherSearch(){
        Log.d(TAG, "onKey: Enter ");

        city = autoCompView.getText().toString();
        //city = inputCity.getText().toString();
        url = String.format(WEATHER_API_BASE
                + city
                + units
                + language
                + API_KEY_WEATHER);

        new GetWeatherTask(MainActivity.this, weatherLayout).execute(url);
    }




    public void onItemClick(AdapterView adapterView, View view, int position, long id) {
        String str = (String) adapterView.getItemAtPosition(position);
        //Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
        weatherSearch();
    }

    public static ArrayList autocomplete(String input) {
        ArrayList resultList = null;
        String urlAutofill = String.format(PLACES_API_BASE
                + TYPE_AUTOCOMPLETE
                + OUT_JSON);

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(urlAutofill);
            sb.append("?key=" + API_KEY_AUTOFILL);
            //sb.append("&components=country:se");
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.d(TAG, "Error processing Places API URL" + e);
            return resultList;
        } catch (IOException e) {
            Log.d(TAG, "Error connecting to Places API" + e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

            // Extract the Place descriptions from the results
            resultList = new ArrayList(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                Log.d(TAG, predsJsonArray.getJSONObject(i).getString("description"));
                Log.d(TAG, "============================================================");
                resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
            }
        } catch (JSONException e) {
            Log.d(TAG, "Cannot process JSON results" + e);
        }

        return resultList;
    }


    class GooglePlacesAutocompleteAdapter extends ArrayAdapter implements Filterable {
        private ArrayList resultList;

        public GooglePlacesAutocompleteAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public int getCount() {
            return resultList.size();
        }

        @Override
        public String getItem(int index) {
            return resultList.get(index).toString();
        }


        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {

                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                        // Retrieve the autocomplete results.
                        resultList = autocomplete(constraint.toString());

                        // Assign the data to the FilterResults
                        filterResults.values = resultList;
                        filterResults.count = resultList.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }
            };
            return filter;
        }
    }

}
