package com.semrov.jure.sunshine;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
//import android.widget.ShareActionProvider;


public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Create the detail fragment and add it to the activity
        // using a fragment transaction.
        Bundle args = new Bundle();
        args.putParcelable(DetailFragment.DETAIL_URI,getIntent().getData());

        DetailFragment df = new DetailFragment();
        df.setArguments(args);

        if(savedInstanceState == null)
        {
            getFragmentManager().beginTransaction()
                    .add(R.id.weather_detail_container,df)
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


}
