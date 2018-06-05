package com.example.jakub.weatherapp;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    Context context;
    public LocationService locationService;
    private int MY_PERMISSIONS;
    private TextView longtitude,latitude;
    private TextView humidity,temperature,pressure,city;
    private ImageView imageView;
    private Location lastKnownLoc;
    private Spinner spinner;
    private RequestQueue queue;
    private List<Weather> list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        longtitude = findViewById(R.id.longtitudeTextView);
        latitude = findViewById(R.id.latitudeTextView);
        humidity = findViewById(R.id.humidityTextView);
        temperature = findViewById(R.id.tempTextView);
        pressure = findViewById(R.id.pressureTextView);
        city = findViewById(R.id.cityTextView);
        imageView = findViewById(R.id.imageView);
        spinner = findViewById(R.id.spinner);

        context = this;

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS);
            }
        } else {
            startGPSService();
        }

       list = new ArrayList<>(Arrays.asList(
                new Weather(0,0,"My location"),
                new Weather(19.949562,49.299181,"Zakopane"),
                new Weather(17.038538,51.107885,"Wrocław"),
                new Weather(19.104079,50.286264,"Sosnowiec"),
                new Weather(21.012229,52.229676,"Warszawa"),
                new Weather(20.947890,49.624060,"Grybów")

        ));

        ArrayAdapter<Weather> spinnerArrayAdapter = new ArrayAdapter<Weather>
                (this, android.R.layout.simple_spinner_item, list);

        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerArrayAdapter);
        spinner.setOnItemSelectedListener(this);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (String a:permissions
             ) {
            if(a.equals("android.permission.ACCESS_FINE_LOCATION")){
                startGPSService();
            }
        }
    }



    private void startGPSService(){
        final Intent serviceStart = new Intent(this.getApplication(), LocationService.class);
        this.getApplication().startService(serviceStart);
        this.getApplication().bindService(serviceStart, serviceConnection, Context.BIND_AUTO_CREATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("GPSLocationUpdates"));
    }



    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            String name = className.getClassName();

            if (name.endsWith("LocationService")) {
                locationService = ((LocationService.LocationServiceBinder) service).getService();
                locationService.startUpdatingLocation();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            if (className.getClassName().equals("LocationService")) {
                locationService = null;

            }
        }
    };


    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            Bundle b = intent.getBundleExtra("Location");
            lastKnownLoc = (Location) b.getParcelable("Location");
            if(lastKnownLoc != null){
                if(city.getText().toString().equals("City:Earth")||city.getText().toString().equals("City:")){
                    VolleySingelton volleySingelton = VolleySingelton.getInstance(context);
                    RequestQueue queue = volleySingelton.getRequestQueue();
                    GsonRequest<Weather> myReq = new GsonRequest<Weather>(
                            ("http://api.openweathermap.org/data/2.5/weather?lat="+String.valueOf(lastKnownLoc.getLatitude())+"&lon="+String.valueOf(lastKnownLoc.getLongitude())+"&units=metric&appid=" + getString(R.string.ApiKey)),
                            Weather.class,
                            null,
                            createMyReqSuccessListener(),
                            createMyReqErrorListener());
                    queue.add(myReq);
                }
                list.get(0).latitude = lastKnownLoc.getLatitude();
                list.get(0).longitude = lastKnownLoc.getLongitude();
            }
        }
    };

    private Response.Listener<Weather> createMyReqSuccessListener() {
        return new Response.Listener<Weather>() {
            @Override
            public void onResponse(Weather response) {
                city.setText(getString(R.string.CITY)+String.valueOf(response.city));
                temperature.setText(getString(R.string.TEMP)+String.valueOf(response.temp));
                pressure.setText(getString(R.string.PRESSURE)+String.valueOf(response.pressure));
                humidity.setText(getString(R.string.HUMIDITY)+String.valueOf(response.humidity));
                latitude.setText(getString(R.string.LATITUDE)+String.valueOf(response.latitude));
                longtitude.setText(getString(R.string.LONGITUDE)+String.valueOf(response.longitude));
                String icon = "w"+response.icon;
                int resourceImage = getResources().getIdentifier(icon, "drawable", getPackageName());
                imageView.setImageResource(resourceImage);

            }
        };
    }

    private Response.ErrorListener createMyReqErrorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Do whatever you want to do with error.getMessage();
            }
        };
    }





    @Override
    public void onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("GPSLocationUpdates"));
        super.onResume();
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Weather thisWeatherObj = list.get(position);

        VolleySingelton volleySingelton = VolleySingelton.getInstance(context);
        RequestQueue queue = volleySingelton.getRequestQueue();
        GsonRequest<Weather> myReq = new GsonRequest<Weather>(
                ("http://api.openweathermap.org/data/2.5/weather?lat="+String.valueOf(thisWeatherObj.latitude)+"&lon="+String.valueOf(thisWeatherObj.longitude)+"&units=metric&appid=" + getString(R.string.ApiKey)),
                Weather.class,
                null,
                createMyReqSuccessListener(),
                createMyReqErrorListener());

        queue.add(myReq);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        VolleySingelton volleySingelton = VolleySingelton.getInstance(context);
        RequestQueue queue = volleySingelton.getRequestQueue();

        GsonRequest<Weather> myReq = new GsonRequest<Weather>(
                ("http://api.openweathermap.org/data/2.5/weather?lat="+String.valueOf(lastKnownLoc.getLatitude()+"&lon="+String.valueOf(lastKnownLoc.getLongitude())+"&units=metric&appid=" + getString(R.string.ApiKey))),
                Weather.class,
                null,
                createMyReqSuccessListener(),
                createMyReqErrorListener());

        queue.add(myReq);
    }
}