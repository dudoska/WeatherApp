package com.example.weatherappremaster.Classes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.example.weatherappremaster.R;
import java.util.List;

public class ForecastAdapter extends ArrayAdapter<ForecastData> {
    private LayoutInflater inflater;

    public ForecastAdapter(Context context, List<ForecastData> data) {
        super(context, 0, data);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.forecast_item_layout, parent, false);
        }

        TextView textView1 = convertView.findViewById(R.id.DataTV);
        TextView textView2 = convertView.findViewById(R.id.AvgtempTV);
        ImageView imageView = convertView.findViewById(R.id.IconIV);

        ForecastData item = getItem(position);

        textView1.setText(item.getData());
        textView2.setText(item.getAvgtemp());
        Glide.with(this.getContext()).load("https:" + item.getIcon()).into(imageView);
        return convertView;
    }
}
