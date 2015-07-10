package com.trogdan.nanospotify;

import android.app.SearchManager;
import android.content.Context;
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
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private final String LOG_TAG = MainActivityFragment.class.getSimpleName();

    private final SpotifyApi m_spotifyApi = new SpotifyApi();
    private final SpotifyService m_spotifyService = m_spotifyApi.getService();
    private ArtistAdapter m_artistAdapter;

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
        m_artistAdapter = new ArtistAdapter(new ArrayList<Artist>());
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        ListView artistListView;

        artistListView = (ListView)rootView.findViewById(R.id.list_view_artists);
        artistListView.setAdapter(m_artistAdapter);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.mainfragment, menu);

        // Get the SearchView and set the searchable configuration
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        // implementing the listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                m_spotifyService.searchArtists(query, new Callback<ArtistsPager>() {
                    @Override
                    public void success(ArtistsPager artistsPager, Response response) {
                        Log.d(LOG_TAG, "Artist success: " + artistsPager.artists.total);

                        m_artistAdapter.clear();
                        for(int i = 0; i < artistsPager.artists.items.size(); i++)
                        {
                            m_artistAdapter.add(artistsPager.artists.items.get(i));
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.d(LOG_TAG, "Artist failure", error);
                    }
                });
                return false;
            }

            @Override
            public boolean onQueryTextChange(String text) {
                return true;
            }
        });

        MenuItemCompat.setActionView(searchItem, searchView);
    }

    private class ArtistAdapter extends ArrayAdapter<Artist> {
        private final String LOG_TAG = ArtistAdapter.class.getSimpleName();

        public ArtistAdapter(ArrayList<Artist> items) {
            super(getActivity(), 0, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater()
                        .inflate(R.layout.list_item_artist, parent, false);
            }

            Artist item = getItem(position);
            ImageView imageView = (ImageView)convertView
                    .findViewById(R.id.artist_icon);

            //imageView.setImageResource(R.drawable.brian_up_close);

            Log.d(LOG_TAG, "Loading picasso with uri " + item.uri + " and href " + item.href);
            //Picasso.with(getActivity())
            //        .load(item.href)
            //        .noFade()
            //        .into(imageView);

            return convertView;
        }
    }
}
