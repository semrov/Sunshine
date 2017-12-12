package com.semrov.jure.sunshine;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment
{
    private static final String LOG_TAG = ForecastFragment.class.getName();

    ArrayAdapter<String> mForecastAdapter;

    public ForecastFragment(){ }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this  fragment handles menu events
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mForecastAdapter = new ArrayAdapter<String>(getActivity(),R.layout.list_item_forecast,R.id.list_item_forecast_textview,new ArrayList<String>());
        View rootView = inflater.inflate(R.layout.fragment_main,container,false);
        ListView listView = rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String forecast = mForecastAdapter.getItem(i);
                Intent intent = new Intent(getActivity(),DetailActivity.class);
                intent.putExtra("forecast",forecast);
                startActivity(intent);
            }
        });
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
        FetchWeatherTask fetchWeatherTask = new FetchWeatherTask();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String loc_key = getString(R.string.pref_location_key);
        String location = sharedPreferences.getString(loc_key,getString(R.string.pref_location_default));
        Log.v(LOG_TAG,location);
        fetchWeatherTask.execute(location,"7");
    }

    //Returns units selected in preference settings
    private String getUnitsPreference()
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return sharedPreferences.getString(getString(R.string.pref_units_key),getString(R.string.pref_units_default));
    }

    @Override
    public void onStart()
    {
        super.onStart();
        updateWeather();
    }

    //The date/time conversion
    private String getReadableDateString(Date time)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd");
        return dateFormat.format(time);
    }

    private String formatHighLowTemps(double high, double low, String unit)
    {
        if(unit.equals(getString(R.string.pref_units_imperial)))
        {
            high = (high * 1.8) + 32;
            low = (low * 1.8) + 32;
        }
        else if(!unit.equals(getString(R.string.pref_units_metric)))
        {
            Log.d(LOG_TAG,"Unit not found: " + unit);
        }

        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        return  roundedLow + "/" + roundedHigh;
    }

    //Take a string containing data in JSON format and parse it
    //returns array of strings containing parsed data by days
    private String[] getWeatherDataFromJson(String forecastJsonStr) throws JSONException
    {
        //names JSON object that need to be extracted
        final String OBJ_LIST = "list";
        final String OBJ_WEATHER = "weather";
        final String OBJ_TEMP = "temp";
        final String OBJ_MIN_TEMP = "min";
        final String OBJ_MAX_TEMP = "max";
        final String OBJ_MAIN = "main";
        final String OBJ_COUNT = "cnt";

        JSONObject jsonForecast = new JSONObject(forecastJsonStr);
        JSONArray jsonForecastArray = jsonForecast.getJSONArray(OBJ_LIST);
        int num_days = jsonForecast.getInt(OBJ_COUNT);

        //JSON reply returns forecast based on local time of city
        //we need to know GTM offset to translate data correctly

        GregorianCalendar gc = new GregorianCalendar(TimeZone.getDefault());
        String weatherForecast[] = new String[num_days];

        //Data is fetch in celsius by default
        //We do this to avoid fetching data again
        //when we store data in database
        //Values converts to Fahrenheit here if needed
        String unit = getUnitsPreference();

        for (int i = 0; i < num_days; i++)
        {
            // use format "Day, description, low/high temp"
            String day, description, highAndLow;

            //Json object representing a day
            JSONObject jsonDay = jsonForecastArray.getJSONObject(i);
            //Get weather description object
            JSONArray jsonArrayWeather = jsonDay.getJSONArray(OBJ_WEATHER);
            JSONObject jsonWeather = jsonArrayWeather.getJSONObject(0);
            //Get weather description string
            description = jsonWeather.getString(OBJ_MAIN);

            //get min and max temp of the day
            JSONObject jsonTemp = jsonDay.getJSONObject(OBJ_TEMP);
            double temp_min = jsonTemp.getDouble(OBJ_MIN_TEMP);
            double temp_max = jsonTemp.getDouble(OBJ_MAX_TEMP);
            highAndLow = formatHighLowTemps(temp_max,temp_min,unit);

            Date date = gc.getTime();
            day = getReadableDateString(date);

            //adds one day
            gc.add(GregorianCalendar.DATE,1);

            weatherForecast[i] = day + " - " + description + " - " + highAndLow;
        }
        for (String s : weatherForecast)
        {
            Log.v(LOG_TAG,s);
        }

        //returns parsed forecast
        return weatherForecast;
    }

    public class FetchWeatherTask extends AsyncTask<String,Void,String[]>
    {
        private final String LOG_TAG = FetchWeatherTask.class.getName();

        @Override
        protected String[] doInBackground(String... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            //Uri uriBuilder = new Uri.parse(WeatherUrlConstants.BASE_URL);

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                Uri buildURI = Uri.parse(WeatherUrlConstants.BASE_URL)
                        .buildUpon()
                        .appendQueryParameter(WeatherUrlConstants.QUERY_PARAM,params[0])
                        .appendQueryParameter(WeatherUrlConstants.UNITS_PARAM,getString(R.string.pref_units_default))
                        .appendQueryParameter(WeatherUrlConstants.FORMAT_PARAM,"json")
                        .appendQueryParameter(WeatherUrlConstants.DAYS_PARAM,params[1])
                        .appendQueryParameter(WeatherUrlConstants.APPID_PARAM,WeatherUrlConstants.APPID)
                        .build();

                Log.v(LOG_TAG,buildURI.toString());

                URL url = new URL(buildURI.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();

                //Log.v(LOG_TAG,"Forecast parsing string: " + forecastJsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            try {
                return getWeatherDataFromJson(forecastJsonStr);
            }
            catch (JSONException je)
            {
                Log.e(LOG_TAG,je.getMessage(),je);
                je.printStackTrace();
            }

            //returns null if getting error on getting or parsing error
            return null;
        }

        @Override
        protected void onPostExecute(String[] forecasts)
        {
            if(forecasts != null) {
                mForecastAdapter.clear();
                mForecastAdapter.addAll(forecasts);
                mForecastAdapter.notifyDataSetChanged();
            }
        }
    }


}
