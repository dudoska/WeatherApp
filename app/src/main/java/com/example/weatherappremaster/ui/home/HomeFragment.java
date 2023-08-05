package com.example.weatherappremaster.ui.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.weatherappremaster.Activities.MoreInfoActivity;
import com.example.weatherappremaster.Classes.ForecastAdapter;
import com.example.weatherappremaster.Classes.ForecastData;
import com.example.weatherappremaster.MainActivity;
import com.example.weatherappremaster.R;
import com.example.weatherappremaster.databinding.FragmentHomeBinding;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Intent intent = getActivity().getIntent();
        String JS = intent.getStringExtra("data");
        JsonObject data = new JsonParser().parse(JS).getAsJsonObject();

        SharedPreferences preferences = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
        String unit = preferences.getString("unit", "°C");

        updateUI(data, unit);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void updateUI(JsonObject data, String unit){
        Glide.with(this).load("https:" + data.getAsJsonObject("current").getAsJsonObject("condition").get("icon").getAsString()).into(binding.IconWeather);
        binding.TemperatureTV.setText(data.getAsJsonObject("current").get((unit.equals("°C")) ? "temp_c" : "temp_f").getAsString() + " " + unit);
        binding.ConditionTV.setText(data.getAsJsonObject("current").getAsJsonObject("condition").get("text").getAsString());
        binding.CityTV.setText(data.getAsJsonObject("location").get("name").getAsString());

        List<ForecastData> d = new ArrayList<>();
        JsonArray forecast = data.getAsJsonObject("forecast").getAsJsonArray("forecastday");
        for (int i = 0; i < forecast.size(); i++) {
            JsonObject forecastDay = forecast.get(i).getAsJsonObject();
            String date = (i == 0) ? "Today" : forecastDay.get("date").getAsString();
            String avgTemp = forecastDay.getAsJsonObject("day").get((unit.equals("°C")) ? "avgtemp_c" : "avgtemp_f").getAsString();
            String icon = forecastDay.getAsJsonObject("day").getAsJsonObject("condition").get("icon").getAsString();
            d.add(new ForecastData(date, avgTemp + " " + unit, icon));
        }
        ForecastAdapter adapter = new ForecastAdapter(HomeFragment.this.getActivity(), d);
        ListView listView = binding.forecastLV;
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                JsonObject forecastDay = forecast.get(i).getAsJsonObject();
                Intent intent = new Intent(HomeFragment.this.getActivity(), MoreInfoActivity.class);
                intent.putExtra("forecastDay", forecastDay.toString());
                onDestroyView();
                startActivity(intent);
            }
        });
    }

}