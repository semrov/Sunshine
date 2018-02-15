package com.semrov.jure.sunshine;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.semrov.jure.sunshine.data.WeatherContract;


/**
 * Created by Jure on 10.2.2018.
 */
/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter
{
    public ForecastAdapter(Context context, Cursor c, int flags)
    {
        super(context,c,flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_forecast,viewGroup,false);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        // Read weather icon ID from cursor
        int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_ID);
        // Use placeholder image for now
        ImageView iconView = (ImageView) view.findViewById(R.id.list_item_icon);
        iconView.setImageResource(R.mipmap.ic_launcher);

        // Reads date from cursor
        long date_ms = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);
        TextView dateView = view.findViewById(R.id.list_item_date_textview);
        dateView.setText(Utility.getFriendlyDayString(context,date_ms));

        // Reads weather forecast from cursor
        String forecast = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        TextView forecastView = view.findViewById(R.id.list_item_forecast_textview);
        forecastView.setText(forecast);

        // Read user preference for metric or imperial temperature units
        boolean isMetric = Utility.isMetric(context);

        // Read high temperature from cursor
        double high = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
        TextView highView = view.findViewById(R.id.list_item_high_textview);
        highView.setText(Utility.formatTemperature(high, isMetric));

        // Read low temperature from cursor
        double low = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        TextView lowView = view.findViewById(R.id.list_item_low_textview);
        lowView.setText(Utility.formatTemperature(low, isMetric));

    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(Context c, double high, double low) {
        boolean isMetric = Utility.isMetric(c);
        String highLowStr = Utility.formatTemperature(high, isMetric) + "/" + Utility.formatTemperature(low, isMetric);
        return highLowStr;
    }



}
