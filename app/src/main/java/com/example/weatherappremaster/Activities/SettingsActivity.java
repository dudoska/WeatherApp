package com.example.weatherappremaster.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.example.weatherappremaster.MainActivity;
import com.example.weatherappremaster.R;
import com.example.weatherappremaster.databinding.ActivitySettingsBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {
ActivitySettingsBinding binding;
String unit_default, startLocation_default;
String[] locations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());
        String[] unit = {"°C", "°F"};

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

            unit_default = json.getString("unit");
        } catch (IOException e) {
            Log.e("TAG", "Error: " + e);
        } catch (JSONException e) {
            Log.e("TAG", "Error: " + e);
        }

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
            JSONObject locals = json.getJSONObject("locations");

            Iterator<String> keys = locals.keys();
            ArrayList<String> valuesList = new ArrayList<>();

            while (keys.hasNext()) {
                String key = keys.next();
                if (!key.equals("start_location")){
                    valuesList.add(key);
                }
                else{
                    Log.d("TAG", key);
                    startLocation_default = locals.getString(key);
                    valuesList.add("current_location");
                }
            }
            locations = valuesList.toArray(new String[0]);
            Log.i("TAG", Arrays.toString(locations));

        } catch (IOException | JSONException e) {
            Log.e("TAG", "Error: " + e);
        }

        ArrayAdapter<String> unit_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, unit);
        unit_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.unitSpinner.setAdapter(unit_adapter);
        binding.unitSpinner.setSelection((unit_default.equals("°C")) ? 0 : 1);

        ArrayAdapter<String> locations_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, locations);
        locations_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.startLocationSpinner.setAdapter(locations_adapter);
        for (int i = 0; i < locations.length; i++) {
            if (locations[i].equals(startLocation_default)) {
                binding.startLocationSpinner.setSelection(i);
            }
        }

        binding.startLocationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (!Objects.equals(startLocation_default, adapterView.getItemAtPosition(i).toString())){
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

                        JSONObject locat = json.getJSONObject("locations");
                        locat.put("start_location", adapterView.getItemAtPosition(i).toString());

                        FileWriter fileWriter = new FileWriter(data_file);
                        fileWriter.write(json.toString());
                        fileWriter.close();
                    } catch (IOException | JSONException e) {
                        Log.e("TAG", "Error: " + e);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        binding.unitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (!Objects.equals(unit_default, adapterView.getItemAtPosition(i).toString())){
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
                        json.put("unit", adapterView.getItemAtPosition(i).toString());

                        FileWriter fileWriter = new FileWriter(data_file);
                        fileWriter.write(json.toString());
                        fileWriter.close();
                    } catch (IOException | JSONException e) {
                        Log.e("TAG", "Error: " + e);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        binding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, LoadActivity.class));
            }
        });

    }
}