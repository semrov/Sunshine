package com.semrov.jure.sunshine;

/**
 * Created by Jure on 7.12.2017.
 */

//Constants for accessing weather data
public final class WeatherUrlConstants
{
    //   http://api.openweathermap.org/data/2.5/forecast/daily?q=Logatec&appid=ccdb30e5915364ec2380ab284e3ada58&units=metric&cnt=7

    public static final String APPID = "ccdb30e5915364ec2380ab284e3ada58";
    public static final String APPID_PARAM = "appid";
    public static final String BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
    public static final String QUERY_PARAM = "q";
    public static final String FORMAT_PARAM = "mode";
    public static final String UNITS_PARAM = "units";
    public static final String DAYS_PARAM = "cnt";

}
