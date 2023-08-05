package com.example.weatherappremaster.ui.slideshow;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.weatherappremaster.Activities.LoadActivity;
import com.example.weatherappremaster.Activities.SettingsActivity;
import com.example.weatherappremaster.Classes.City;
import com.example.weatherappremaster.Classes.ForecastData;
import com.example.weatherappremaster.MainActivity;
import com.example.weatherappremaster.databinding.FragmentSlideshowBinding;
import com.example.weatherappremaster.ui.home.HomeFragment;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// https://www.tutorialspoint.com/how-to-fix-android-os-networkonmainthreadexception

public class SlideshowFragment extends Fragment {
    private FragmentSlideshowBinding binding;
    private String TOKEN = "ae8905d694e64e0c8dc152219231007";
    SharedPreferences preferences = SlideshowFragment.this.getActivity().getSharedPreferences("sc", Context.MODE_PRIVATE);
    Map<String, ?> allData = preferences.getAll();

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        SlideshowViewModel slideshowViewModel = new ViewModelProvider(this).get(SlideshowViewModel.class);
        binding = FragmentSlideshowBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        if (!allData.isEmpty()) {
            updateLV(allData);
        }

        binding.floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MyTask().execute();
            }
        });
        
        return root;
    }

    public class MyTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            String result = null;

            String name = binding.cityED.getText().toString();
            if (name != null){
                try {
                    URL url = new URL("https://api.weatherapi.com/v1/forecast.json?key=" + TOKEN + "&q=" + name + "&aqi=no");
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
                        String city = data.getAsJsonObject("location").get("name").getAsString();
                        String location = data.getAsJsonObject("location").get("lat").getAsString() + " " + data.getAsJsonObject("location").get("lon").getAsString();

                        if (!allData.containsKey(city)) {
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString(city, location);
                            editor.apply();
                            result = "0";
                        }
                        else {
                            result = "3";
                        }

                    } else {
                        result = "2";
                    }
                } catch (Exception e) {
                    Log.e("TAG", "Error:" + e);
                }
            }
            else {
                result = "1";
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result == "1"){
                Toast.makeText(getContext(), "The field is empty", Toast.LENGTH_SHORT).show();
            } else if (result == "2") {
                Toast.makeText(getContext(), "Could not find city", Toast.LENGTH_SHORT).show();
            } else if (result == "3") {
                Toast.makeText(getContext(), "This city is already in the favorites", Toast.LENGTH_SHORT).show();
            } else if (result == "0") {
                updateLV(allData);
                Toast.makeText(getContext(), "Done!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void updateLV(Map<String, ?> allData){
        List<String> sc = new ArrayList<>();
        for (Map.Entry<String, ?> entry : allData.entrySet()) {
            String key = (String) entry.getKey();
            sc.add(key);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(SlideshowFragment.this.getActivity(), android.R.layout.simple_list_item_1, sc);
        binding.scLV.setAdapter(adapter);
    }
}