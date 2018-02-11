package com.semrov.jure.sunshine;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.semrov.jure.sunshine.data.WeatherContract;

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
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.Vector;

/**
 * Created by Jure on 5.2.2018.
 */
public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {
    //private ForecastFragment forecastFragment;
    private final Context mContext;
    private final String LOG_TAG = FetchWeatherTask.class.getName();

    public FetchWeatherTask(Context context) {
        mContext = context;
    }

    @Override
    protected String[] doInBackground(String... params) {
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String locationQuery = params[0];

        //Uri uriBuilder = new Uri.parse(WeatherUrlConstants.BASE_URL);

        // Will contain the raw JSON response as a string.
        String forecastJsonStr = null;

        try {
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are avaiable at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast
            Uri buildURI = Uri.parse(WeatherUrlConstants.BASE_URL)
                    .buildUpon()
                    .appendQueryParameter(WeatherUrlConstants.QUERY_PARAM, params[0])
                    .appendQueryParameter(WeatherUrlConstants.UNITS_PARAM,mContext.getString(R.string.pref_units_metric))
                    .appendQueryParameter(WeatherUrlConstants.FORMAT_PARAM, "json")
                    .appendQueryParameter(WeatherUrlConstants.DAYS_PARAM, params[1])
                    .appendQueryParameter(WeatherUrlConstants.APPID_PARAM, WeatherUrlConstants.APPID)
                    .build();

            Log.v(LOG_TAG, buildURI.toString());

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
        } finally {
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
            return getWeatherDataFromJson(forecastJsonStr,locationQuery);
        } catch (JSONException je) {
            Log.e(LOG_TAG, je.getMessage(), je);
            je.printStackTrace();
        }

        //returns null if getting error on getting or parsing error
        return null;
    }

    /**
     * Helper method to handle insertion of a new location in the weather database.
     *
     * @param locationSetting The location string used to request updates from the server.
     * @param cityName A human-readable city name, e.g "Mountain View"
     * @param lat the latitude of the city
     * @param lon the longitude of the city
     * @return the row ID of the added location.
     */
    long addLocation(String locationSetting, String cityName, double lat, double lon) {
        long locationId;
        String projection[] = new String[]{WeatherContract.LocationEntry._ID};
        String selection = WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?";
        String selectionArgs[] = new String[]{locationSetting};
        //Check if the location with this city name exists in the db
        // If it exists, return the current ID
        // Otherwise, insert it using the content resolver and the base URI
        Cursor c = mContext.getContentResolver().query(WeatherContract.LocationEntry.CONTENT_URI,projection,
                 selection,selectionArgs,null);

        if(c.moveToFirst())
        {
            int locationIdIndex = c.getColumnIndex(WeatherContract.LocationEntry._ID);
            locationId = c.getLong(locationIdIndex);
        }
        else
        {
            ContentValues cv = new ContentValues();
            cv.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,locationSetting);
            cv.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME,cityName);
            cv.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT,lat);
            cv.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG,lon);

            Uri insertedUri = mContext.getContentResolver().insert(WeatherContract.LocationEntry.CONTENT_URI,
                    cv);
            locationId = ContentUris.parseId(insertedUri);
        }
        c.close();
        return locationId;
    }

    //The date/time conversion
    private String getReadableDateString(Date time)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd");
        return dateFormat.format(time);
    }

    private String formatHighLowTemps(double high, double low)
    {
        //Data is fetch in celsius by default
        //We do this to avoid fetching data again
        //when we store data in database
        //Values converts to Fahrenheit here if needed
        String unit = getUnitsPreference();

        if(unit.equals(mContext.getString(R.string.pref_units_imperial)))
        {
            high = (high * 1.8) + 32;
            low = (low * 1.8) + 32;
        }
        else if(!unit.equals(mContext.getString(R.string.pref_units_metric)))
        {
            Log.d(LOG_TAG,"Unit not found: " + unit);
        }

        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        return  roundedLow + "/" + roundedHigh;
    }

    //Returns units selected in preference settings
    private String getUnitsPreference()
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        return sharedPreferences.getString(mContext.getString(R.string.pref_units_key),mContext.getString(R.string.pref_units_default));
    }

    /*
    This code will allow the FetchWeatherTask to continue to return the strings that
    the UX expects so that we can continue to test the application even once we begin using
    the database.
 */
    String[] convertContentValuesToUXFormat(Vector<ContentValues> cvv) {
        // return strings to keep UI functional for now
        String[] resultStrs = new String[cvv.size()];
        for ( int i = 0; i < cvv.size(); i++ ) {
            ContentValues weatherValues = cvv.elementAt(i);
            String highAndLow = formatHighLowTemps(
                    weatherValues.getAsDouble(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP),
                    weatherValues.getAsDouble(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP));
            resultStrs[i] = getReadableDateString(
                    new Date(weatherValues.getAsLong(WeatherContract.WeatherEntry.COLUMN_DATE))) +
                    " - " + weatherValues.getAsString(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC) +
                    " - " + highAndLow;
        }
        return resultStrs;
    }

    //Take a string containing data in JSON format and parse it
    //returns array of strings containing parsed data by days
    private String[] getWeatherDataFromJson(String forecastJsonStr, String locationSetting) throws JSONException
    {
        //names JSON object that need to be extracted

        // Location information
        final String OBJ_CITY = "city";
        final String OBJ_CITY_NAME = "name";
        final String OBJ_COORD = "coord";

        // Location coordinate
        final String OBJ_LATITUDE = "lat";
        final String OBJ_LONGITUDE = "lon";

        // Weather information.  Each day's forecast info is an element of the "list" array.
        final String OBJ_LIST = "list";
        final String OBJ_PRESSURE = "pressure";
        final String OBJ_HUMIDITY = "humidity";
        final String OBJ_WINDSPEED = "speed";
        final String OBJ_WIND_DIRECTION = "deg";

        final String OBJ_WEATHER = "weather";
        final String OBJ_WEATHER_ID = "id";
        final String OBJ_TEMP = "temp";
        final String OBJ_MIN_TEMP = "min";
        final String OBJ_MAX_TEMP = "max";
        final String OBJ_MAIN = "main";
        final String OBJ_COUNT = "cnt";

        try {

            JSONObject jsonForecast = new JSONObject(forecastJsonStr);

            JSONObject jsonCity = jsonForecast.getJSONObject(OBJ_CITY);
            String cityName = jsonCity.getString(OBJ_CITY_NAME);

            JSONObject jsonCoords = jsonCity.getJSONObject(OBJ_COORD);
            double latitude = jsonCoords.getDouble(OBJ_LATITUDE);
            double logitude = jsonCoords.getDouble(OBJ_LONGITUDE);

            long locationId = addLocation(locationSetting, cityName, latitude, logitude);

            JSONArray jsonForecastArray = jsonForecast.getJSONArray(OBJ_LIST);
            int num_days = jsonForecast.getInt(OBJ_COUNT);

            Vector<ContentValues> cv_vector = new Vector<>(num_days);

            //JSON reply returns forecast based on local time of city
            //we need to know GTM offset to translate data correctly

            GregorianCalendar gc = new GregorianCalendar(TimeZone.getDefault());

            for (int i = 0; i < num_days; i++) {
                double pressure;
                int humidity;
                double windDirection;
                double windSpeed;
                int weatherID;
                // use format "Day, description, low/high temp"
                String description;

                //Json object representing a day
                JSONObject jsonDay = jsonForecastArray.getJSONObject(i);

                pressure = jsonDay.getDouble(OBJ_PRESSURE);
                humidity = jsonDay.getInt(OBJ_HUMIDITY);
                windDirection = jsonDay.getDouble(OBJ_WIND_DIRECTION);
                windSpeed = jsonDay.getDouble(OBJ_WINDSPEED);

                //Get weather description object
                JSONArray jsonArrayWeather = jsonDay.getJSONArray(OBJ_WEATHER);
                JSONObject jsonWeather = jsonArrayWeather.getJSONObject(0);
                //Get weather description string
                description = jsonWeather.getString(OBJ_MAIN);
                weatherID = jsonWeather.getInt(OBJ_WEATHER_ID);

                //get min and max temp of the day
                JSONObject jsonTemp = jsonDay.getJSONObject(OBJ_TEMP);
                double temp_min = jsonTemp.getDouble(OBJ_MIN_TEMP);
                double temp_max = jsonTemp.getDouble(OBJ_MAX_TEMP);
                //highAndLow = formatHighLowTemps(temp_max, temp_min, unit);

                ContentValues weatherValues = new ContentValues();
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationId);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATE, gc.getTimeInMillis());
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, humidity);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, pressure);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, temp_max);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, temp_min);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, weatherID);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, windDirection);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, description);

                cv_vector.add(weatherValues);

                Date date = gc.getTime();
                //day = getReadableDateString(date);

                //adds one day
                gc.add(GregorianCalendar.DATE, 1);
            }

            if (cv_vector.size() > 0) {
                ContentValues[] weather_values = cv_vector.toArray(new ContentValues[cv_vector.size()]);
                int insertCount = mContext.getContentResolver().
                        bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI, weather_values);

                Log.d(LOG_TAG, "BulkInsert returned " + insertCount);
            }


            return convertContentValuesToUXFormat(cv_vector);
        }
        catch (JSONException e)
        {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }

}
