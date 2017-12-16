package com.semrov.jure.sunshine.data;

import android.provider.BaseColumns;

import java.sql.Time;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by Jure on 14.12.2017.
 */

public class WeatherContract
{


    public static long normalizeDate(long startDate)
    {
        Date date = new Date(startDate);
        //GregorianCalendar.
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(date);
        //gc.setGregorianChange();
        return gc.getTimeInMillis();
    }

    /*
    public static long normalizeDate(long startDate) {
        // normalize the start date to the beginning of the (UTC) day
        Time time = new Time(startDate);
        time.set(startDate);
        int julianDay = Time.getJulianDay(startDate, time.gmtoff);
        return time.setJulianDay(julianDay);
    }*/

    /*Inner class that defines the table contents of the location table*/
    public static class LocationEntry implements BaseColumns
    {
        public static final String TABLE_NAME = "location";
    }

    /*Inner class that defines the table contents of the weather table*/
    public static class WeatherEntry implements BaseColumns
    {
        public static final String TABLE_NAME = "weather";

        // Column with the foreign key into the location table.
        public static final String COLUMN_LOC_KEY = "location_id";
        // Date, stored as long in milliseconds since the epoch
        public static final String COLUMN_DATE = "date";
        // Weather id as returned by API, to identify the icon to be used
        public static final String COLUMN_WEATHER_ID = "weather_id";

        // Short description and long description of the weather, as provided by API.
        // e.g "clear" vs "sky is clear".
        public static final String COLUMN_SHORT_DESC = "short_desc";

        // Min and max temperatures for the day (stored as floats)
        public static final String COLUMN_MIN_TEMP = "min";
        public static final String COLUMN_MAX_TEMP = "max";

        // Humidity is stored as a float representing percentage
        public static final String COLUMN_HUMIDITY = "humidity";

        // Humidity is stored as a float representing percentage
        public static final String COLUMN_PRESSURE = "pressure";

        // Windspeed is stored as a float representing windspeed  mph
        public static final String COLUMN_WIND_SPEED = "wind";

        // Degrees are meteorological degrees (e.g, 0 is north, 180 is south).  Stored as floats.
        public static final String COLUMN_DEGREES = "degrees";
    }

}
