package com.example.weatherappremaster.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.example.weatherappremaster.MainActivity;
import com.example.weatherappremaster.R;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoadActivity extends AppCompatActivity {
    private LocationRequest locationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);

        Intent intent = getIntent();
        String return_city = intent.getStringExtra("return_city");

        File cacheDir = getCacheDir();
        File data_file = new File(cacheDir, "data.json");

        if (data_file.exists()){
            if (return_city == null){
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(data_file));
                    StringBuilder jsonString = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        jsonString.append(line);
                    }
                    reader.close();

                    JSONObject json = new JSONObject(jsonString.toString());

                    JSONObject locations = json.getJSONObject("locations");
                    String startLocation = locations.getString("start_location");

                    if (startLocation.equals("current_location")){
                        get_current_weather();
                    }
                    else {
                        get_location_weather(startLocation);
                    }

                } catch (IOException e) {
                    Log.e("TAG", "Error: " + e);
                } catch (JSONException e) {
                    Log.e("TAG", "Error: " + e);
                }
            }
            else {
                get_location_weather(return_city);
            }
        }
        else {
            try {
                FileWriter writer = new FileWriter(data_file);
                writer.write("{\"unit\": \"°C\", \"themes\": \"light\", \"locations\": {\"start_location\": \"current_location\"}}");
                writer.close();
            } catch (IOException e) {
                Log.e("TAG", "Error: " + e);
            }
            get_current_weather();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load);
    }

    private void get_location_weather(String location) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("https://api.weatherapi.com/v1/forecast.json?key=" + getString(R.string.api_key) + "&q=" + location + "&days=7&aqi=no&alerts=no");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String line;
                        StringBuilder response = new StringBuilder();

                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();

                        String json = response.toString();
                        JsonObject data = JsonParser.parseString(json).getAsJsonObject();

                        Intent intent = new Intent(LoadActivity.this, MainActivity.class);
                        intent.putExtra("data_weather", data.toString());
                        startActivity(intent);
                    } else {
                        Log.e("TAG", String.valueOf(responseCode));
                    }
                } catch (Exception e) {
                    Log.e("TAG", "Error:" + e);
                }
            }
        });
        thread.start();
    }

    private void get_current_weather(){
        if (ActivityCompat.checkSelfPermission(LoadActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if (isGPSEnable()){
                LocationServices.getFusedLocationProviderClient(LoadActivity.this).requestLocationUpdates(locationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(@NonNull LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        LocationServices.getFusedLocationProviderClient(LoadActivity.this).removeLocationUpdates(this);
                        if (locationResult != null && locationResult.getLocations().size() > 0){
                            int index = locationResult.getLocations().size() - 1;
                            double latitube = locationResult.getLocations().get(index).getLatitude();
                            double longitube = locationResult.getLocations().get(index).getLongitude();
                            String coordination = latitube + " " + longitube;

                            Thread thread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        URL url = new URL("https://api.weatherapi.com/v1/forecast.json?key=" + getString(R.string.api_key) + "&q=" + coordination + "&days=7&aqi=no&alerts=no");
                                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                        connection.setRequestMethod("GET");

                                        int responseCode = connection.getResponseCode();
                                        if (responseCode == HttpURLConnection.HTTP_OK) {
                                            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                                            String line;
                                            StringBuilder response = new StringBuilder();

                                            while ((line = reader.readLine()) != null) {
                                                response.append(line);
                                            }
                                            reader.close();

                                            String json = response.toString();
                                            JsonObject data = JsonParser.parseString(json).getAsJsonObject();

                                            Intent intent = new Intent(LoadActivity.this, MainActivity.class);
                                            intent.putExtra("data_weather", data.toString());
                                            startActivity(intent);
                                        } else {
                                            Log.e("TAG", String.valueOf(responseCode));
                                        }
                                    } catch (Exception e) {
                                        Log.e("TAG", "Error:" + e);
                                    }
                                }
                            });
                            thread.start();
                        }
                    }
                }, Looper.getMainLooper());
            }
            else {
                turnOnGPS();
            }
        }
        else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    private boolean isGPSEnable(){
        LocationManager locationManager = null;
        boolean isEnabled = false;

        if (locationManager == null){
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }

        isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return isEnabled;
    }

    private void turnOnGPS(){
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getApplicationContext()).checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    Toast.makeText(LoadActivity.this, "GPS is already tured on", Toast.LENGTH_SHORT).show();
                } catch (ApiException e) {
                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(LoadActivity.this, 2);
                            } catch (IntentSender.SendIntentException ex) {
                                ex.printStackTrace();
                            }
                            break;

                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            break;
                    }
                }
            }
        });
    }
}