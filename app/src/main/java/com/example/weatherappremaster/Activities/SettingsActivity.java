package com.example.weatherappremaster.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.example.weatherappremaster.MainActivity;
import com.example.weatherappremaster.R;
import com.example.weatherappremaster.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {
ActivitySettingsBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());
        String[] unit = {"°C", "°F"};
        String[] locations = {"Current location"};

        ArrayAdapter<String> unit_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, unit);
        unit_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.unitSpinner.setAdapter(unit_adapter);

        ArrayAdapter<String> locations_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, locations);
        locations_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.startLocationSpinner.setAdapter(locations_adapter);

        binding.unitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                SharedPreferences preferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("unit", adapterView.getItemAtPosition(i).toString());
                editor.apply();
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