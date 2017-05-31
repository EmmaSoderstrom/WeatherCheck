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
import android.view.WindowManager;
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



public class MainActivity extends AppCompatActivity {

    static String TAG = "tag";
    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private static final String API_KEY_WEATHER = "&appid=3a87cd70e4003ebbdf1c8272e812b2c2";
    private static final String WEATHER_API_BASE = "http://api.openweathermap.org/data/2.5/weather?q=";
    private static final String WEATHER_API_BASE_IP = "http://api.openweathermap.org/data/2.5/weather?";
    private String units = "&units=metric";
    private String language = "&lang=se";

    MainActivity mainActivity;
    Context context;

    ViewGroup viewGroup;
    PlaceAutocompleteFragment autocompleteFragment;
    RelativeLayout weatherLayout;
    RelativeLayout weatherLayoutMoreInfo;
    ImageButton swapViewButton;
    boolean swipeOut = true;


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
        weatherLayout = (RelativeLayout) findViewById(R.id.swap_view);
        weatherLayoutMoreInfo = (RelativeLayout) findViewById(R.id.swap_view_more_info);
        swapViewButton = (ImageButton) findViewById(R.id.swap_view_button);


        //Google places autocomplete
        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

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
                String city = place.getName().toString();
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

        //hämtar en första position via ip-adress
        String urlIP = String.format("http://ip-api.com/json/");
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
    }

    /**
     * FloatingActionButton sök klick
     * Startar autocomplete vy
     * @param view
     */
    public void onClickSearch(View view){
        Log.d(TAG, "onClickSearch: ");

//        ((EditText)autocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input))
//                .setSelection(((EditText)autocompleteFragment.getView()
//                        .findViewById(R.id.place_autocomplete_search_input)).getText().length());


        try {
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);


        } catch (GooglePlayServicesRepairableException |
            GooglePlayServicesNotAvailableException e){
        }
    }

    /**
     * Autocomplete resultat
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                Log.d(TAG, "Place: " + place.getName());
                String city = place.getName().toString();

                weatherSearch(city);
                ((EditText)autocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input))
                        .setText(place.getName().toString());

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Log.i(TAG, status.getStatusMessage());


            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    /**
     * Sökmetod när det ska sökas på stad i textform
     * @param city
     */
    public void weatherSearch(String city){
        Log.d(TAG, "weatherSearch: city " + city);

        if(city.equals("null")){
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

    /**
     * Sökmetod när det ska sökas på stad via ip-adress
     * @param city
     */
    public void weatherSearchIp(String city){
        Log.d(TAG, "weatherSearch: city " + city);

        String urlWeather = String.format(WEATHER_API_BASE_IP
                + city
                + units
                + language
                + API_KEY_WEATHER);

        new GetWeatherTask(MainActivity.this, weatherLayout).execute(urlWeather);

        //stänger/döljer tangentbord
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(viewGroup.getWindowToken(), 0);
    }

    /**
     * Lägger till latitude och ongitude till autocomplet, så resultaten högst upp i listan är på städer i närområdet
     * @param stringLatitude
     * @param stringLongitude
     */
    public void setLatLong(String stringLatitude, String stringLongitude){
        //Log.d(TAG, "setLatLong: " + stringLatitude + " " + stringLongitude);
        double latitude = Double.parseDouble(stringLatitude);
        double longitude = Double.parseDouble(stringLongitude);

        autocompleteFragment.setBoundsBias(new LatLngBounds(
                new LatLng(latitude, longitude),
                new LatLng(latitude, longitude)));
    }

    /**
     * ImageButton pilen startar animition för att byta vyer mellen väder vy nu och detaljerad väder vy
     * @param view
     */
    public void onClickSwapView(View view){
        Log.d(TAG, "onClickSwapView: ");

        Animation mSlideInTop = AnimationUtils.loadAnimation(mainActivity, R.anim.slide_in_top);
        Animation mSlideOutTop = AnimationUtils.loadAnimation(mainActivity, R.anim.slide_out_top);
        Animation mSlideInBotton = AnimationUtils.loadAnimation(mainActivity, R.anim.slide_in_bottom);
        Animation mSlideOutBotton = AnimationUtils.loadAnimation(mainActivity, R.anim.slide_out_botton);

        if(swipeOut) {
            makeSwape(weatherLayoutMoreInfo, weatherLayout, mSlideInBotton, mSlideOutTop, swapViewButton);
            swipeOut = false;

        }else{
            makeSwape(weatherLayout, weatherLayoutMoreInfo, mSlideInTop, mSlideOutBotton, swapViewButton);
            swipeOut = true;

        }
    }

    /**
     * Kör animition för att byta väder vy
     * @param layouIn
     * @param layoutOut
     * @param slideIn
     * @param slideOut
     * @param button
     */
    public void makeSwape(View layouIn, View layoutOut, Animation slideIn, Animation slideOut, ImageButton button){
        layouIn.startAnimation(slideIn);
        layouIn.setVisibility(View.VISIBLE);
        layoutOut.startAnimation(slideOut);
        layoutOut.setVisibility(View.INVISIBLE);
        float deg = swapViewButton.getRotation() + 180F;
        button.animate().rotation(deg).setInterpolator(new AccelerateDecelerateInterpolator());
    }
}


