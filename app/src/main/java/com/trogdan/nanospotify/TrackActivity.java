package com.trogdan.nanospotify;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class TrackActivity extends ActionBarActivity {

    private TrackFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);

        if (savedInstanceState == null) {
            fragment = new TrackFragment();

            final Intent i = getIntent();
            if( i != null && i.hasExtra(Intent.EXTRA_TEXT)) {
                // TODO, better way then forwarding an intent EXTRA?
                Bundle args = new Bundle();
                args.putString(TrackFragment.TRACKQUERY_ARG, i.getStringExtra(Intent.EXTRA_TEXT));
                fragment.setArguments(args);
            }

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.track_container, fragment)
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.track, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_open_player)
        {
            fragment.showPlayerDialog();
        }

        return super.onOptionsItemSelected(item);
    }

}
