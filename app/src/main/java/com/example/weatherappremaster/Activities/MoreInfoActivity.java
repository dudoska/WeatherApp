package com.example.weatherappremaster.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.example.weatherappremaster.Classes.ForecastAdapter;
import com.example.weatherappremaster.Classes.ForecastData;
import com.example.weatherappremaster.R;
import com.example.weatherappremaster.databinding.ActivityMoreInfoBinding;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MoreInfoActivity extends AppCompatActivity {
    ActivityMoreInfoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityMoreInfoBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        String JS = intent.getStringExtra("forecastDay");
        JsonObject forecastDay = new JsonParser().parse(JS).getAsJsonObject();

        try {
            File cacheDir = getCacheDir();
            File data_file = new File(cacheDir, "data.json");
            BufferedReader reader = new BufferedReader(new FileReader(data_file));
            StringBuilder jsonString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
            reader.close();

            JSONObject json = new JSONObject(jsonString.toString());
            String unit = json.getString("unit");
            updateUI(forecastDay, unit);

        } catch (IOException e) {
            Log.e("TAG", "Error: " + e);
        } catch (JSONException e) {
            Log.e("TAG", "Error: " + e);
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateUI(JsonObject forecastDay, String unit){
        binding.maxtempC.setText("MaxTemp: " + forecastDay.getAsJsonObject("day").get((unit.equals("°C")) ? "maxtemp_c" : "maxtemp_f").toString() + " " + unit);
        binding.mintempC.setText("MinTemp: " + forecastDay.getAsJsonObject("day").get((unit.equals("°C")) ? "mintemp_c" : "mintemp_f").toString() + " " + unit);
        binding.avgtempC.setText("AvgTemp: " + forecastDay.getAsJsonObject("day").get((unit.equals("°C")) ? "avgtemp_c" : "avgtemp_f").toString() + " " + unit);
        binding.maxwindKph.setText("MaxWind: " + forecastDay.getAsJsonObject("day").get("maxwind_kph").toString() + " kph");
        binding.totalprecipMm.setText("TotalPrecip: " + forecastDay.getAsJsonObject("day").get("totalprecip_mm").toString() + " mm");
        binding.avghumidity.setText("AvgHumidity: " + forecastDay.getAsJsonObject("day").get("avghumidity").toString() + "%");
        binding.uv.setText("UV: " + forecastDay.getAsJsonObject("day").get("uv").toString());
        binding.sunrise.setText(forecastDay.getAsJsonObject("astro").get("sunrise").toString());
        binding.sunset.setText(forecastDay.getAsJsonObject("astro").get("sunset").toString());

        JsonArray forecast = forecastDay.getAsJsonArray("hour").getAsJsonArray();
        List<ForecastData> d = new ArrayList<>();
        for (int i = 0; i < forecast.size(); i++) {
            JsonObject forecastHour = forecast.get(i).getAsJsonObject();
            String time = forecastHour.get("time").getAsString();
            String temp = forecastHour.get((unit.equals("°C")) ? "temp_c" : "temp_f").getAsString();
            String icon = forecastHour.getAsJsonObject("condition").get("icon").getAsString();
            d.add(new ForecastData(time, temp + " " + unit, icon));
        }
        ForecastAdapter adapter = new ForecastAdapter(this, d);
        ListView listView = findViewById(R.id.forecastDayLV);
        listView.setAdapter(adapter);
    }
}