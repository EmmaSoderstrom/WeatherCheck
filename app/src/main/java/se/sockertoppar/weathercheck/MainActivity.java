package se.sockertoppar.weathercheck;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {

    String TAG = "tag";
    JSONObject data = null;
    String url;
    String city = "Sidney";
    String units = "&units=metric";
    String language = "&lang=se";

    EditText inputCity;
    TextView textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        double lat = 40.712774, lon = -74.006091;

        //url = String.format("http://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=3a87cd70e4003ebbdf1c8272e812b2c2");

        inputCity = (EditText) findViewById(R.id.input_city);
        textView = (TextView) findViewById(R.id.textView);

        inputCity.setInputType(InputType.TYPE_CLASS_TEXT);
        inputCity.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // sker om det trycks enter
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {

                    Log.d(TAG, "onKey: Enter ");

                    city = inputCity.getText().toString();
                    url = String.format("http://api.openweathermap.org/data/2.5/weather?q="
                            + city
                            + units
                            + language
                            + "&appid=3a87cd70e4003ebbdf1c8272e812b2c2");

                    new GetWeatherTask(textView).execute(url);
                    return true;
                }
                return false;
            }
        });
    }





}
