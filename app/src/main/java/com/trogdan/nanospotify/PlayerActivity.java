package com.trogdan.nanospotify;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.trogdan.nanospotify.data.ParcelableTrack;

import java.util.ArrayList;


public class PlayerActivity extends ActionBarActivity {

    public static boolean isServiceLaunched = false;

    public static final int PLAYER_CURRENT_TRACK = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_player);
        if (savedInstanceState == null) {
            final PlayerFragment fragment = new PlayerFragment();
            if (fragment != null) {
                fragment.setArguments(getIntent().getExtras());
                final FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .add(android.R.id.content, fragment)
                        .commit();
            }
        }

        ActionBar bar = getSupportActionBar();
        if (bar != null)
            bar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.player, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static void showPlayerDialog(
            ActionBarActivity activity,
            ArrayList<ParcelableTrack> trackList,
            int currentTrack,
            boolean changed,
            boolean twoPane) {

        final Bundle args = new Bundle();
        args.putParcelableArrayList(PlayerFragment.PLAYERTRACKS_ARG, trackList);
        args.putInt(PlayerFragment.PLAYERPLAYTRACK_ARG, currentTrack);
        args.putBoolean(PlayerFragment.PLAYERCHANGE_ARG, changed);

        if (twoPane) {
            final FragmentManager fragmentManager = activity.getSupportFragmentManager();

            final PlayerFragment playerFragment = new PlayerFragment();
            playerFragment.setArguments(args);
            // The device is using a large layout, so show the fragment as a dialog
            playerFragment.show(fragmentManager, PlayerFragment.PLAYERFRAGMENT_TAG);
        } else {
            Intent intent = new Intent(activity, PlayerActivity.class);
            intent.putExtras(args);
            activity.startActivity(intent);
        }
    }
}
