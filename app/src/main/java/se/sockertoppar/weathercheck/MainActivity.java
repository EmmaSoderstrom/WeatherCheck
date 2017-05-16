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
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;

import android.widget.ImageButton;
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
    ImageButton swapViewButton;

    int windowHight;

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
        swapViewButton = (ImageButton) findViewById(R.id.swap_view_button);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        windowHight = size.x;


        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
//        autocompleteFragment.setBoundsBias(new LatLngBounds(
//                new LatLng(-33.880490, 151.184363),
//                new LatLng(-33.858754, 151.229596)));

        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES)
                .build();
        autocompleteFragment.setFilter(typeFilter);


        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.d(TAG, "Place: " + place.getName() + " " + place.getAddress());
                String city = place.getAddress().toString();

                weatherSearch(city);
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.d(TAG, "ERROR onError : " + status );
            }

        });


//        //klick OnKeyListener för enter
//        autocompleteFragment.setOnKeyListener(new View.OnKeyListener() {
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                // sker om det trycks enter
//                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
//
//                    weatherSearch();
//                    return true;
//                }
//                return false;
//            }
//        });






        //lägger till väder vyn
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View weatherListItem = (View) inflater.inflate(R.layout.weather_list_item, null, true);


        weatherLayout.addView(weatherListItem);

//        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) weatherListItem.getLayoutParams();
//        params.height = (int)windowHight;
//        weatherListItem.setLayoutParams(params);
        ViewGroup.LayoutParams params = weatherListItem.getLayoutParams();
        params.height = (int)windowHight;
        //params.width = 320;
        weatherListItem.setLayoutParams(params);


        RelativeLayout.LayoutParams layout_description =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);

        weatherListItem.setLayoutParams(layout_description);

        //weatherListItem.setPadding(0, 200, 0, 0);
        weatherListItem.setBackgroundColor((Color.parseColor("#000000")));

        Log.d(TAG, "onCreate: windowHight " + windowHight);
        Log.d(TAG, "onCreate: weatherListItem " + weatherListItem.getHeight());



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
        int actionbarheight = toolbar.getHeight();

        Log.d(TAG, "setToolbar: String.valueOf(actionbarheight)" + actionbarheight);




        Log.d(TAG, "setToolbar: toolbar.getHeight() " + searchLayout.getHeight());

    }

    public void onClickSearch(View view){

        int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
        try {
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);

        } catch (GooglePlayServicesRepairableException |

                GooglePlayServicesNotAvailableException e){
            // TODO: Handle the error.
        }

    }

    public void weatherSearch(String city){
        Log.d(TAG, "weatherSearch: city " + city);

//        if(city == null){
//            city = "Stockholm";
//        }else{
//
//        }

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
    
    public void onClickSwapView(View view){
        Log.d(TAG, "onClickSwapView: ");
        TranslateAnimation animation = new TranslateAnimation(0, 0, 0,(windowHight + 600) * -1);
        animation.setDuration(1000);
        animation.setFillAfter(false);
        animation.setAnimationListener(new MyAnimationListener());

        weatherLayout.startAnimation(animation);

        //swapViewButton.setImageDrawable(R.drawable.ic_keyboard_arrow_up_white_50dp);
    }


    public void setIPCity(String city){
        Log.d(TAG, "setIPCity: " + city);
        weatherSearch(city);
    }

    public void setLatLong(String stringLatitude, String stringLongitude){
        Log.d(TAG, "setLatLong: " + stringLatitude);
        double latitude = Double.parseDouble(stringLatitude);
        double longitude = Double.parseDouble(stringLongitude);

        autocompleteFragment.setBoundsBias(new LatLngBounds(
                new LatLng(latitude, longitude),
                new LatLng(latitude, longitude)));
    }

    private class MyAnimationListener implements Animation.AnimationListener {

        @Override
        public void onAnimationEnd(Animation animation) {
            weatherLayout.clearAnimation();
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(0, (windowHight + 200) * - 1);
            weatherLayout.setLayoutParams(lp);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }

    }



}






//Check the internet connection.
   /* private void getFirstLocationNetworkDetect() {
        Log.d(TAG, "NetwordDetect: ");
        boolean WIFI = false;
        boolean MOBILE = false;

        ConnectivityManager CM = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] networkInfo = CM.getAllNetworkInfo();

        for (NetworkInfo netInfo : networkInfo) {
            if (netInfo.getTypeName().equalsIgnoreCase("WIFI")) {
                if (netInfo.isConnected())
                    WIFI = true;
            }
            if (netInfo.getTypeName().equalsIgnoreCase("MOBILE")) {
                if (netInfo.isConnected())
                    MOBILE = true;
            }
        }

        if(WIFI == true) {
            String IPaddress = GetDeviceipWiFiData();
            Log.d(TAG, "WIFI: IPaddress " + IPaddress);
            getCity(IPaddress);
        }
        if(MOBILE == true) {
            String IPaddress = GetDeviceipMobileData();
            Log.d(TAG, "MOBILE: IPaddress " + IPaddress);
            getCity(IPaddress);
        }
    }*/
/*
    public void getCity(String IPaddress){
        String urlIP = String.format("https://ipapi.co/" + IPaddress + "/json/");
        //String urlIP = String.format("https://ipapi.co/" + "217.209.179.205" + "/json/");
        new GetIP(MainActivity.this).execute(urlIP);
    }*/
/*
    public String GetDeviceipMobileData(){

        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();

                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    // for getting IPV4 format
                    //if (!inetAddress.isLoopbackAddress() && InetAddressUtils.isIPv4Address(ipv4 = inetAddress.getHostAddress())) {
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address){
                        String ip = inetAddress.getHostAddress().toString();
                        return ip;
                    }
                }
            }
        } catch (Exception ex) {
            Log.e("IP Address", ex.toString());
        }
        return null;

    }*/
/*
    public String GetDeviceipWiFiData() {

        WifiManager wm = (WifiManager) context.getSystemService(WIFI_SERVICE);
        @SuppressWarnings("deprecation")
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

        return ip;

    }*/