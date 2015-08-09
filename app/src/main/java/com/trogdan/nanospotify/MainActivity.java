package com.trogdan.nanospotify;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends ActionBarActivity {

    private final String ARTISTFRAGMENT_TAG = "AFTAG";


    private boolean mTwoPane;

    @Override
    protected void onResume() {
        super.onResume();

        // TODO update the track in our second pane using the fragment manager
//        if (location != null && !location.equals(mLocation)) {
        ArtistFragment ff = (ArtistFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_main);
        if(ff != null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String query = prefs.getString(getString(R.string.pref_last_query_key), "");
            ff.getArtists(query);
        }
//            if ( null != ff ) {
//                ff.onLocationChanged();
//            }
//            mLocation = location;
//        }
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
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.track_container, new TrackFragment(), TrackFragment.TRACKFRAGMENT_TAG)
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return super.onOptionsItemSelected(item);
    }

}
