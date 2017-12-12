package com.semrov.jure.sunshine;

import android.app.Fragment;
import android.content.Intent;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
//import android.widget.ShareActionProvider;
import android.support.v7.widget.ShareActionProvider;
import android.widget.TextView;

public class DetailActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if(savedInstanceState == null)
        {
            getFragmentManager().beginTransaction()
                    .add(R.id.container,new DetailFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail,menu);
        return true;
    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this,SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public static class DetailFragment extends Fragment
    {
        private static final String LOG_TAG = DetailFragment.class.getSimpleName();
        private static final String FORECAST_SHARE_TAG = "#SunshineApp";
        private String mforecastString;

        public DetailFragment()
        {
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_detail,container,false);
            Intent i = getActivity().getIntent();
            TextView tv = rootView.findViewById(R.id.tv_detail_forecast);
            mforecastString = i.getStringExtra("forecast");
            tv.setText(mforecastString);
            return rootView;
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            // Inflate the menu; this adds items to the action bar if it is present.
            inflater.inflate(R.menu.detailfragment,menu);

            // Locate MenuItem with ShareActionProvider
            MenuItem menuItem = menu.findItem(R.id.action_share);

            // Fetch and store ShareActionProvider
            ShareActionProvider shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

            // Attach an intent to this ShareActionProvider.  You can update this at any time,
            // like when the user selects a new piece of data they might like to share.
            if (shareActionProvider != null) {
                shareActionProvider.setShareIntent(createShareIntent());
            }

        }

        private Intent createShareIntent()
        {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.putExtra(Intent.EXTRA_TEXT,mforecastString + " " + FORECAST_SHARE_TAG);
            Log.d(LOG_TAG,mforecastString + " " + FORECAST_SHARE_TAG);
            return shareIntent;
        }

    }
}
