package se.sockertoppar.weathercheck;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.format.Formatter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import static se.sockertoppar.weathercheck.GooglePlacesAutocompleteAdapter.context;


public class MainActivity extends AppCompatActivity {

    static String TAG = "tag";
    JSONObject data = null;
    MainActivity mainActivity;
    Context context;
    

    private static final String WEATHER_API_BASE = "http://api.openweathermap.org/data/2.5/weather?q=";
    private String city = "Stockholm";
    private String units = "&units=metric";
    private String language = "&lang=se";
    private static final String API_KEY_WEATHER = "&appid=3a87cd70e4003ebbdf1c8272e812b2c2";

    ViewGroup viewGroup;
    PlaceAutocompleteFragment autocompleteFragment;
    LinearLayout weatherLayout;

    LocationManager mLocationManager;
    LocationManager locationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainActivity = this;
        context = getApplicationContext();

        RelativeLayout mainLayout = (RelativeLayout) findViewById(R.id.main_layout);
        mainLayout.setBackgroundResource(R.drawable.backgound_gradient);
        setToolbar();

        viewGroup = (ViewGroup) ((ViewGroup) this.findViewById(android.R.id.content)).getChildAt(0);
        weatherLayout = (LinearLayout) findViewById(R.id.weather_layout);


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
                city = place.getAddress().toString();

                weatherSearch();
//                try {
//                    Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN).build(mainActivity);
//                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
//                } catch (GooglePlayServicesRepairableException e) {
//                    // TODO: Handle the error.
//                    Log.d(TAG, "ERROR catch 1 : " + e );
//                } catch (GooglePlayServicesNotAvailableException e) {
//                    // TODO: Handle the error.
//                    Log.d(TAG, "ERROR catch 2 : " + e );
//                }
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.d(TAG, "ERROR onError : " + status );
            }

        });






//        autoCompView.setInputType(InputType.TYPE_CLASS_TEXT);
//        //autofill textview
//        autoCompView.setAdapter(new GooglePlacesAutocompleteAdapter(this, R.layout.city_list_item));
//        autoCompView.setOnItemClickListener(this);
//
//        //klick OnKeyListener för enter
//        autoCompView.setOnKeyListener(new View.OnKeyListener() {
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
        View WeatherListItem = (View) inflater.inflate(R.layout.weather_list_item, null, true);
        weatherLayout.addView(WeatherListItem);


        NetworkDetect();

    }

    /**
     * Sätter toolbar
     */
    public void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout searchLayout = (LinearLayout) inflater.inflate(R.layout.searchbar, null,true);

        toolbar.addView(searchLayout);

    }

    public void onClickSearch(View view){
        //inputCity.requestFocus();
        weatherSearch();

    }

    public void weatherSearch(){
        Log.d(TAG, "onKey: Enter ");

        //city = autoCompView.getText().toString();

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



    //Check the internet connection.
    private void NetworkDetect() {
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
    }

    public void getCity(String IPaddress){
        //String urlIP = String.format("https://ipapi.co/" + IPaddress + "/json/");
        String urlIP = String.format("https://ipapi.co/" + "217.209.179.205" + "/json/");
        new GetIP(MainActivity.this, weatherLayout).execute(urlIP);
    }

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

    }

    public String GetDeviceipWiFiData() {

        WifiManager wm = (WifiManager) context.getSystemService(WIFI_SERVICE);
        @SuppressWarnings("deprecation")
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

        return ip;

    }

    public void setIPCity(String city){
        this.city = city;
        weatherSearch();
    }

    public void setLatLong(String stringLatitude, String stringLongitude){
        double latitude = Double.parseDouble(stringLatitude);
        double longitude = Double.parseDouble(stringLongitude);

        autocompleteFragment.setBoundsBias(new LatLngBounds(
                new LatLng(latitude, longitude),
                new LatLng(latitude, longitude)));
    }




}
