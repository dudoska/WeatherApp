package com.example.weatherappremaster.ui.slideshow;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.weatherappremaster.Activities.LoadActivity;
import com.example.weatherappremaster.Activities.MoreInfoActivity;
import com.example.weatherappremaster.Activities.SettingsActivity;
import com.example.weatherappremaster.Classes.City;
import com.example.weatherappremaster.Classes.ForecastData;
import com.example.weatherappremaster.MainActivity;
import com.example.weatherappremaster.R;
import com.example.weatherappremaster.databinding.FragmentSlideshowBinding;
import com.example.weatherappremaster.ui.home.HomeFragment;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

public class SlideshowFragment extends Fragment {
    private FragmentSlideshowBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        SlideshowViewModel slideshowViewModel = new ViewModelProvider(this).get(SlideshowViewModel.class);
        binding = FragmentSlideshowBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        updateLV();
        registerForContextMenu(binding.scLV);

        binding.floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MyTask().execute();
            }
        });

        binding.scLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String name = adapterView.getItemAtPosition(i).toString();
                Intent intent = new Intent(SlideshowFragment.this.getActivity(), LoadActivity.class);
                intent.putExtra("return_city", name);
                onDestroyView();
                startActivity(intent);
            }
        });

        return root;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, 1, 0, "Delete");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = info.position;

        Object it = binding.scLV.getItemAtPosition(position);
        String name = it.toString();

        switch (item.getItemId()) {
            case 1:
                try {
                    File cacheDir = requireContext().getApplicationContext().getCacheDir();
                    File data_file = new File(cacheDir, "data.json");

                    BufferedReader reader_data = new BufferedReader(new FileReader(data_file));
                    StringBuilder jsonString = new StringBuilder();
                    String line2;
                    while ((line2 = reader_data.readLine()) != null) {
                        jsonString.append(line2);
                    }
                    reader_data.close();
                    JSONObject json = new JSONObject(jsonString.toString());

                    json.getJSONObject("locations").remove(name);

                    FileWriter fileWriter = new FileWriter(data_file);
                    fileWriter.write(json.toString());
                    fileWriter.close();

                    updateLV();
                    Toast.makeText(getContext(), "Done!", Toast.LENGTH_SHORT).show();

                } catch (IOException | JSONException e) {
                    Log.e("TAG", "Error:" + e);
                }
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public class MyTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            String result = null;
            File cacheDir = requireContext().getApplicationContext().getCacheDir();
            File data_file = new File(cacheDir, "data.json");

            String name = binding.cityED.getText().toString();
            if (!name.equals("")){
                try {
                    URL url = new URL("https://api.weatherapi.com/v1/forecast.json?key=" + getString(R.string.api_key) + "&q=" + name + "&aqi=no");
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

                        try {
                           BufferedReader reader_data = new BufferedReader(new FileReader(data_file));
                           StringBuilder jsonString = new StringBuilder();
                           String line2;
                           while ((line2 = reader_data.readLine()) != null) {
                               jsonString.append(line2);
                           }
                           reader_data.close();
                           JSONObject json2 = new JSONObject(jsonString.toString());
                           JSONObject locations = json2.getJSONObject("locations");

                           boolean hasObject = locations.has(city);
                           if (hasObject) {
                               result = "3";
                           } else {
                               locations.put(city, location);
                               FileWriter fileWriter = new FileWriter(data_file);
                               fileWriter.write(json2.toString());
                               fileWriter.close();
                               result = "0";
                           }

                          } catch (IOException e) {
                              Log.e("TAG", "Error:" + e);
                          } catch (JSONException e) {
                              Log.e("TAG", "Error:" + e);
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
                updateLV();
                binding.cityED.setText("");
                Toast.makeText(getContext(), "Done!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void updateLV(){
        try {
            File cacheDir = requireContext().getApplicationContext().getCacheDir();
            File data_file = new File(cacheDir, "data.json");

            FileReader reader = new FileReader(data_file);
            StringBuilder jsonString = new StringBuilder();
            int ch;
            while ((ch = reader.read()) != -1) {
                jsonString.append((char) ch);
            }
            reader.close();

            JSONObject jsonObject = new JSONObject(jsonString.toString());
            JSONObject locations = jsonObject.getJSONObject("locations");
            Iterator<String> keys = locations.keys();
            ArrayList<String> names = new ArrayList<>();
            while (keys.hasNext()) {
                String key = keys.next();
                if (!key.equals("start_location")){
                   names.add(key);
                }
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity().getApplicationContext(), android.R.layout.simple_list_item_1, names);
            binding.scLV.setAdapter(adapter);
        } catch (Exception e) {
            Log.e("TAG", "Error:" + e);
        }
    }
}