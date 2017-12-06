package com.semrov.jure.sunshine;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment
{
    ArrayAdapter<String> mForecastAdapter;

    public ForecastFragment(){ }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        // Create some dummy data for the ListView.
        String[] data = {
                "Mon 6/23 - Sunny - 31/17",
                "Tue 6/24 - Foggy - 21/8",
                "Wed 6/25 - Cloudy - 22/17",
                "Thurs 6/26 - Rainy - 18/11",
                "Fri 6/27 - Foggy - 21/10",
                "Sat 6/28 - TRAPPED IN WEATHERSTATION - 23/18",
                "Sun 6/29 - Sunny - 20/7"
        };


        List<String> weekForecast = new ArrayList<>(Arrays.asList(data));
        mForecastAdapter = new ArrayAdapter<String>(getActivity(),R.layout.list_item_forecast,R.id.list_item_forecast_textview,weekForecast);
        View rootView = inflater.inflate(R.layout.fragment_main,container,false);
        ListView listView = rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);
        return rootView;
    }
}
