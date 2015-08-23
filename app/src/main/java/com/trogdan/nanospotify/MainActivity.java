package com.trogdan.nanospotify;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends ActionBarActivity {

    private final String ARTISTFRAGMENT_TAG = "AFTAG";

    private TrackFragment trackFragment;
    private boolean mTwoPane;

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTwoPane = getResources().getBoolean(R.bool.two_pane);

        if (mTwoPane) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.

            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                trackFragment = new TrackFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.track_container, trackFragment, TrackFragment.TRACKFRAGMENT_TAG)
                        .commit();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_open_player)
        {
            trackFragment.showPlayerDialog();
        }

        return super.onOptionsItemSelected(item);
    }


}
