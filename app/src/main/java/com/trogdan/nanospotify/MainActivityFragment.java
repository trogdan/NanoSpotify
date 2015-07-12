package com.trogdan.nanospotify;

import android.content.Intent;
import android.os.AsyncTask;
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

import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * MainActivityFragment is used provide a search mechanism for artists, and display the results of
 * an artist search.  Selection of an artist results in the firing of an explicit intent to display
 * the top 10 tracks for a selected artist.  Artist results are display with an image of the artist
 * on the left of a list entry, with artist name on the right of the list entry.
 */
public class MainActivityFragment extends Fragment {

    private final String LOG_TAG = MainActivityFragment.class.getSimpleName();

    private final SpotifyApi m_spotifyApi = new SpotifyApi();
    private final SpotifyService m_spotifyService = m_spotifyApi.getService();
    private ArtistAdapter m_artistAdapter;
    private FetchArtistsTask m_fetchArtistsTask;

    public MainActivityFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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

                startActivity(new Intent(getActivity(),
                        TrackActivity.class).putExtra(Intent.EXTRA_TEXT, artist.id));
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
        if (artist != null) {
            m_fetchArtistsTask = new FetchArtistsTask();
            m_fetchArtistsTask.execute(artist);
        }
    }

    public class FetchArtistsTask extends AsyncTask<String, Void, Void> {
        private final String LOG_TAG = FetchArtistsTask.class.getSimpleName();

        // Retrofit callbacks are performed on main UI thread, so no onPostExecute
        // needed.

        @Override
        protected Void doInBackground(String... params) {

            if (params.length != 1) {
                Log.e(LOG_TAG, "Invalid params passed to doInBackground");
                return null;
            }

            m_spotifyService.searchArtists(params[0], new Callback<ArtistsPager>() {
                @Override
                public void success(ArtistsPager artistsPager, Response response) {
                    Log.d(LOG_TAG, "Artist query success: " + artistsPager.artists.total);

                    // Populating the adapter with query results.
                    m_artistAdapter.clear();
                    for (int i = 0; i < artistsPager.artists.items.size(); i++) {
                        m_artistAdapter.add(artistsPager.artists.items.get(i));
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
