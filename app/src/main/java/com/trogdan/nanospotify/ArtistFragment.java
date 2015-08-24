package com.trogdan.nanospotify;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;

import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;


import com.trogdan.nanospotify.data.MusicContract.ArtistImageEntry;
import com.trogdan.nanospotify.data.MusicContract.ArtistEntry;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * ArtistFragment is used provide a search mechanism for artists, and display the results of
 * an artist search.  Selection of an artist results in the firing of an explicit intent to display
 * the top 10 tracks for a selected artist.  Artist results are display with an image of the artist
 * on the left of a list entry, with artist name on the right of the list entry.
 */
public class ArtistFragment extends Fragment {

    private final String LOG_TAG = ArtistFragment.class.getSimpleName();

    private ArtistAdapter m_artistAdapter;
    private FetchArtistsTask m_fetchArtistsTask;
    private String m_previousArtist;
    private boolean m_twoPane;

    private static final int ARTIST_LOADER = 0;

    private static final String[] ARTIST_COLUMNS = {
            ArtistEntry.TABLE_NAME + "." + ArtistEntry._ID,
            ArtistEntry.COLUMN_DATE,
            ArtistEntry.COLUMN_NAME,
            ArtistEntry.COLUMN_QUERY,
            ArtistImageEntry.COLUMN_ARTIST_KEY,
            ArtistImageEntry.COLUMN_URI,
            ArtistImageEntry.COLUMN_WIDTH,
            ArtistImageEntry.COLUMN_HEIGHT
    };

    public ArtistFragment() {
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        m_twoPane = getResources().getBoolean(R.bool.two_pane);

        m_artistAdapter = new ArtistAdapter(this, new ArrayList<Artist>());
        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        final ListView artistListView = (ListView) rootView.findViewById(R.id.list_view_artists);
        artistListView.setAdapter(m_artistAdapter);

        /* Set the list item for an artist to fire an intent to load the top 10 tracks of a selected
           artist, passing the spotify ID of that artist.
         */
        artistListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                final Artist artist = (Artist) parent.getItemAtPosition(position);

                if (m_twoPane) {
                    if (savedInstanceState == null) {
                        TrackFragment fragment = new TrackFragment();
                        Bundle args = new Bundle();
                        args.putString(TrackFragment.TRACKQUERY_ARG, artist.id);
                        fragment.setArguments(args);

                        getActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.track_container, fragment, TrackFragment.TRACKFRAGMENT_TAG)
                                .commit();
                    }
                } else {
                    startActivity(new Intent(getActivity(),
                            TrackActivity.class).putExtra(Intent.EXTRA_TEXT, artist.id));
                }

            }
        });

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.mainfragment, menu);

        // Get the SearchView and set the searchable configuration
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        // Wanted to implement with <searchable>, but this seems to require making a searchable activity
        searchView.setQueryHint(getString(R.string.search_hint));

        // implementing the listener for the search view, which receives results from the spotifyService
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                prefs.edit().putString(getActivity().getString(R.string.pref_last_query_key), query).commit();
                getArtists(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String text) {
                return true;
            }
        });

        MenuItemCompat.setActionView(searchItem, searchView);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public void getArtists(String artist) {
        // No point spinning up a new query if it's a repeat
        if (artist != null && !artist.equals(m_previousArtist)) {
            m_fetchArtistsTask = new FetchArtistsTask();
            m_fetchArtistsTask.execute(artist);
            m_previousArtist = artist;
        }
    }

    public class FetchArtistsTask extends AsyncTask<String, Void, Void> {
        private final String LOG_TAG = FetchArtistsTask.class.getSimpleName();
        private final Context mContext;

        public FetchArtistsTask() {
            mContext = getActivity();
        }

        // Retrofit callbacks are performed on main UI thread, so no onPostExecute
        // needed.

        /**
         * Helper method to handle update of a artist query into the music database
         */
        private void addArtists(String query, ArtistsPager artistsPager) {
            long queryTime = System.currentTimeMillis();

            // Insert the new query results
            for (int i = 0; i < artistsPager.artists.items.size(); i++) {
                Artist artist = artistsPager.artists.items.get(i);

                // Now that the content provider is set up, inserting rows of data is pretty simple.
                // First create a ContentValues object to hold the data you want to insert.
                ContentValues values = new ContentValues();

                // Then add the data, along with the corresponding name of the data type,
                // so the content provider knows what kind of value is being inserted.
                values.put(ArtistEntry.COLUMN_DATE, queryTime);
                values.put(ArtistEntry.COLUMN_API_ID, artist.id);
                values.put(ArtistEntry.COLUMN_NAME, artist.name);
                values.put(ArtistEntry.COLUMN_QUERY, query);

                // Finally, insert data into the database.
                Uri insertedUri = mContext.getContentResolver().insert(ArtistEntry.CONTENT_URI, values);

                long artistId = ContentUris.parseId(insertedUri);

                for (int j = 0; j < artist.images.size(); j++) {
                    Image image = artist.images.get(j);

                    values = new ContentValues();

                    // Then add the data, along with the corresponding name of the data type,
                    // so the content provider knows what kind of value is being inserted.
                    values.put(ArtistImageEntry.COLUMN_DATE, queryTime);
                    values.put(ArtistImageEntry.COLUMN_URI, image.url);
                    values.put(ArtistImageEntry.COLUMN_WIDTH, image.width);
                    values.put(ArtistImageEntry.COLUMN_HEIGHT, image.height);
                    values.put(ArtistImageEntry.COLUMN_ARTIST_KEY, artistId);

                    if (mContext.getContentResolver().insert(ArtistImageEntry.CONTENT_URI, values) == null)
                    {
                        Log.e(LOG_TAG, "Error inserting artist image with url " + image.url);
                    }
                }
            }
        }

        @Override
        protected Void doInBackground(String... params) {

            if (params.length != 1) {
                Log.e(LOG_TAG, "Invalid params passed to doInBackground");
                return null;
            }

            final String query = params[0];
            Utility.getSpotifyService().searchArtists(query, new Callback<ArtistsPager>() {
                @Override
                public void success(ArtistsPager artistsPager, Response response) {
                    Log.d(LOG_TAG, "Artist query success: " + artistsPager.artists.total);

                    // Populating the adapter with query results.
                    m_artistAdapter.clear();
                    for (int i = 0; i < artistsPager.artists.items.size(); i++) {
                        m_artistAdapter.add(artistsPager.artists.items.get(i));
                        addArtists(query, artistsPager);
                    }

                    // Just display a toast that there were no results for the artist, per the
                    // directions, although I feel a more prominent persistent result would
                    // be more useful, in case the user doesn't see the toast.
                    if (artistsPager.artists.items.size() == 0) {
                        Toast.makeText(getActivity(),
                                R.string.artist_fail,
                                Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.d(LOG_TAG, "Artist query failure", error);
                }
            });

            return null;
        }

    }
}
