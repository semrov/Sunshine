package com.semrov.jure.sunshine;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.semrov.jure.sunshine.data.WeatherContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment
{
    private static final String LOG_TAG = ForecastFragment.class.getName();

    ForecastAdapter mForecastAdapter;

    public ForecastFragment(){ }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this  fragment handles menu events
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        String locationSetting = Utility.getPreferredLocation(getActivity());

        // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";

        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(locationSetting,System.currentTimeMillis());
        Cursor cursor = getActivity().getContentResolver().query(weatherForLocationUri,
                null,null,null,sortOrder);

        //mForecastAdapter = //new ForecastAdapter(getActivity(),R.layout.list_item_forecast,R.id.list_item_forecast_textview,new ArrayList<String>());
        mForecastAdapter = new ForecastAdapter(getActivity(),cursor,0);
        View rootView = inflater.inflate(R.layout.fragment_main,container,false);
        ListView listView = rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);
        /*
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String forecast = mForecastAdapter.getItem(i);
                Intent intent = new Intent(getActivity(),DetailActivity.class);
                intent.putExtra("forecast",forecast);
                startActivity(intent);
            }
        });
        */
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.action_refresh:
            {
                updateWeather();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateWeather()
    {
        FetchWeatherTask fetchWeatherTask = new FetchWeatherTask(getActivity());
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String loc_key = getString(R.string.pref_location_key);
        String location = sharedPreferences.getString(loc_key,getString(R.string.pref_location_default));
        Log.v(LOG_TAG,location);
        fetchWeatherTask.execute(location,"7");
    }



    @Override
    public void onStart()
    {
        super.onStart();
        updateWeather();
    }



}
