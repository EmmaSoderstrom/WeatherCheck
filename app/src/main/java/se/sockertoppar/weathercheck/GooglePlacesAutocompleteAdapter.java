package se.sockertoppar.weathercheck;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

//import static se.sockertoppar.weathercheck.MainActivity.autocomplete;

/**
 * Created by User on 2017-05-05.
 */

public class GooglePlacesAutocompleteAdapter extends ArrayAdapter implements Filterable {

    static String TAG = "tag";
    static Context context;

    private ArrayList resultList;

    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";
    private static final String API_KEY_AUTOFILL = "AIzaSyDl0sL-EDUrQwepkBq5tuLeFwn67_mBxJE";

    //int textViewResourceId;

    public GooglePlacesAutocompleteAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        //this.textViewResourceId = textViewResourceId;
        this.context = context;
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

                    // Footer
                    ImageView iv = new ImageView(context);
                    iv.setImageResource(R.drawable.powered_by_google_on_white);
                    resultList.add(iv);

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

    public static ArrayList autocomplete(String input) {
        ArrayList resultList = null;
        String urlAutofill = String.format(PLACES_API_BASE
                + TYPE_AUTOCOMPLETE
                + OUT_JSON);

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sBuilder = new StringBuilder(urlAutofill);
            sBuilder.append("?key=" + API_KEY_AUTOFILL);
            //sBuilder.append("&components=country:se");
            sBuilder.append("&input=" + URLEncoder.encode(input, "utf8"));

            URL url = new URL(sBuilder.toString());
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
            View view;
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            // Create a JSON object hierarchy from the results
            JSONObject jsonObject = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObject.getJSONArray("predictions");

            // Extract the Place descriptions from the results
            resultList = new ArrayList(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                Log.d(TAG, predsJsonArray.getJSONObject(i).getString("description"));
                Log.d(TAG, "============================================================");
                //lägger till resultat i lista som ska visas
                resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
            }
        } catch (JSONException e) {
            Log.d(TAG, "Cannot process JSON results" + e);
        }

        return resultList;
    }







//    public static ArrayList autocomplete(String input) {
//        ArrayList resultList = null;
//        String urlAutofill = String.format(PLACES_API_BASE
//                + TYPE_AUTOCOMPLETE
//                + OUT_JSON);
//
//        HttpURLConnection conn = null;
//        StringBuilder jsonResults = new StringBuilder();
//        try {
//            StringBuilder sBuilder = new StringBuilder(urlAutofill);
//            sBuilder.append("?key=" + API_KEY_AUTOFILL);
//            //sBuilder.append("&components=country:se");
//            sBuilder.append("&input=" + URLEncoder.encode(input, "utf8"));
//
//            URL url = new URL(sBuilder.toString());
//            conn = (HttpURLConnection) url.openConnection();
//            InputStreamReader in = new InputStreamReader(conn.getInputStream());
//
//            // Load the results into a StringBuilder
//            int read;
//            char[] buff = new char[1024];
//            while ((read = in.read(buff)) != -1) {
//                jsonResults.append(buff, 0, read);
//            }
//        } catch (MalformedURLException e) {
//            Log.d(TAG, "Error processing Places API URL" + e);
//            return resultList;
//        } catch (IOException e) {
//            Log.d(TAG, "Error connecting to Places API" + e);
//            return resultList;
//        } finally {
//            if (conn != null) {
//                conn.disconnect();
//            }
//        }
//
//        try {
//            View view;
//            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//
//            // Create a JSON object hierarchy from the results
//            JSONObject jsonObject = new JSONObject(jsonResults.toString());
//            JSONArray predsJsonArray = jsonObject.getJSONArray("predictions");
//
//            // Extract the Place descriptions from the results
//            resultList = new ArrayList(predsJsonArray.length());
//            for (int i = 0; i < predsJsonArray.length(); i++) {
//                Log.d(TAG, predsJsonArray.getJSONObject(i).getString("description"));
//                Log.d(TAG, "============================================================");
//                //lägger till resultat i lista som ska visas
//                if (i != (resultList.size() - 1)) {
//                    view = inflater.inflate(R.layout.city_list_item, null);
//                    TextView listCity = (TextView)view.findViewById(R.id.list_city);
//                    listCity.setText(predsJsonArray.getJSONObject(i).getString("description"));
//                }
//                else{
//                    view = inflater.inflate(R.layout.city_list_item_google_logo, null);
//                }
//                resultList.add(view);
//                //resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
//            }
//        } catch (JSONException e) {
//            Log.d(TAG, "Cannot process JSON results" + e);
//        }
//
//
//
//
//        //if (convertView == null) {
//
//
////        if (position != (resultList.size() - 1)) {
////            view = inflater.inflate(R.layout.city_list_item, null);
////        }
////        else{
////            view = inflater.inflate(R.layout.city_list_item_google_logo, null);
////        }
////        //else {
////            //view = convertView;
////        //}
////
////        if (position != (resultList.size() - 1)) {
////            TextView autocompleteTextView = (TextView) view.findViewById(R.id.autocompleteText);
////            autocompleteTextView.setText(resultList.get(position));
////        }
////        else {
////            ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
////            // not sure what to do 😀
////        }
//
//        //ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
//
//        addGoogleLoggo(resultList);
//
//        return resultList;
//    }


    static public void addGoogleLoggo(ArrayList resultList){
        resultList.add("hej2222");
    }
}