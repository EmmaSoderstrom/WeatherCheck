package se.sockertoppar.weathercheck;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;

import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.json.JSONObject;


import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;


public class MainActivity extends AppCompatActivity {

    static String TAG = "tag";
    JSONObject data = null;
    MainActivity mainActivity;
    Context context;


    private static final String WEATHER_API_BASE = "http://api.openweathermap.org/data/2.5/weather?q=";
    //private String city = "Stockholm";
    private String units = "&units=metric";
    private String language = "&lang=se";
    private static final String API_KEY_WEATHER = "&appid=3a87cd70e4003ebbdf1c8272e812b2c2";

    ViewGroup viewGroup;
    PlaceAutocompleteFragment autocompleteFragment;
    RelativeLayout weatherLayout;
    RelativeLayout weatherLayoutMoreInfo;
    ImageButton swapViewButton;
    boolean swipeOut = true;

    //int windowHight;

    LocationManager mLocationManager;
    LocationManager locationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainActivity = this;
        context = getApplicationContext();

        RelativeLayout mainLayout = (RelativeLayout) findViewById(R.id.main_layout);
        mainLayout.setBackgroundResource(R.drawable.backgound_gradient_sun);
        setToolbar();

        viewGroup = (ViewGroup) ((ViewGroup) this.findViewById(android.R.id.content)).getChildAt(0);
        //weatherLayout = (RelativeLayout) findViewById(R.id.weather_layout);
        weatherLayout = (RelativeLayout) findViewById(R.id.swap_view);
        weatherLayoutMoreInfo = (RelativeLayout) findViewById(R.id.swap_view_more_info);
        swapViewButton = (ImageButton) findViewById(R.id.swap_view_button);

        //Display display = getWindowManager().getDefaultDisplay();
        //Point size = new Point();
        //display.getSize(size);
        //windowHight = size.y;


        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

//        autocompleteFragment.setBoundsBias(new LatLngBounds(
//                new LatLng(-33.880490, 151.184363),
//                new LatLng(-33.858754, 151.229596)));

        ((EditText)autocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input))
                .setTextColor(Color.parseColor("#FFFFFF"));
        ((EditText)autocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input))
                .setHintTextColor(Color.parseColor("#60FFFFFF"));

        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES)
                .build();
        autocompleteFragment.setFilter(typeFilter);


        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Log.d(TAG, "Place: " + place.getName() + " " + place.getAddress());
                String city = place.getAddress().toString();

                weatherSearch(city);
            }

            @Override
            public void onError(Status status) {
                Log.d(TAG, "ERROR onError detta : " + status );
            }

        });


        //lägger till väder vyn
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View weatherListItem = inflater.inflate(R.layout.weather_list_item, null, true);
        View weatherListMoreInfo = inflater.inflate(R.layout.weather_list_more_info, null, true);

        weatherLayout.addView(weatherListItem);
        weatherLayoutMoreInfo.addView(weatherListMoreInfo);
        weatherLayoutMoreInfo.setVisibility(View.INVISIBLE);


        String urlIP = String.format("https://ipapi.co/json/");
        new GetIP(MainActivity.this).execute(urlIP);


    }

    /**
     * Sätter toolbar
     */
    public void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout searchLayout = (RelativeLayout) inflater.inflate(R.layout.searchbar, null,true);

        toolbar.addView(searchLayout);


        //ActionBar tool = getActionBar();
        //int actionbarheight = toolbar.getHeight();

        //Log.d(TAG, "setToolbar: String.valueOf(actionbarheight)" + actionbarheight);

        //Log.d(TAG, "setToolbar: toolbar.getHeight() " + searchLayout.getHeight());

    }

    public void onClickSearch(View view){

        int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
        try {
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);

        } catch (GooglePlayServicesRepairableException |

                GooglePlayServicesNotAvailableException e){

        }

    }

    public void weatherSearch(String city){
        Log.d(TAG, "weatherSearch: city " + city);

        if(city.equals("null")){
            Log.d(TAG, "weatherSearch: citySearch == null ");
            city = "Stockholm";
        }
        

        String urlWeather = String.format(WEATHER_API_BASE
                + city
                + units
                + language
                + API_KEY_WEATHER);

        new GetWeatherTask(MainActivity.this, weatherLayout).execute(urlWeather);

        //stänger/döljer tangentbord
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(viewGroup.getWindowToken(), 0);
    }

    public void setIPCity(String city){
        Log.d(TAG, "setIPCity: " + city);
        weatherSearch(city);
    }

    public void setLatLong(String stringLatitude, String stringLongitude){
        Log.d(TAG, "setLatLong: " + stringLatitude + " " + stringLongitude);
        double latitude = Double.parseDouble(stringLatitude);
        double longitude = Double.parseDouble(stringLongitude);

        autocompleteFragment.setBoundsBias(new LatLngBounds(
                new LatLng(latitude, longitude),
                new LatLng(latitude, longitude)));
    }

    public void onClickSwapView(View view){
        Log.d(TAG, "onClickSwapView: ");

        Animation mSlideInTop = AnimationUtils.loadAnimation(mainActivity, R.anim.slide_in_top);
        Animation mSlideOutTop = AnimationUtils.loadAnimation(mainActivity, R.anim.slide_out_top);
        Animation mSlideInBotton = AnimationUtils.loadAnimation(mainActivity, R.anim.slide_in_bottom);
        Animation mSlideOutBotton = AnimationUtils.loadAnimation(mainActivity, R.anim.slide_out_botton);


        if(swipeOut) {
            weatherLayout.startAnimation(mSlideOutTop);
            weatherLayout.setVisibility(View.INVISIBLE);
            weatherLayoutMoreInfo.startAnimation(mSlideInBotton);
            weatherLayoutMoreInfo.setVisibility(View.VISIBLE);
            rotate180(swapViewButton);

            swipeOut = false;
        }else{
            weatherLayout.startAnimation(mSlideInTop);
            weatherLayout.setVisibility(View.VISIBLE);
            weatherLayoutMoreInfo.startAnimation(mSlideOutBotton);
            weatherLayoutMoreInfo.setVisibility(View.INVISIBLE);
            rotate180(swapViewButton);

            swipeOut = true;
        }
    }

    private void rotate180(View view){
        float deg = swapViewButton.getRotation() + 180F;
        view.animate().rotation(deg).setInterpolator(new AccelerateDecelerateInterpolator());
    }

}


