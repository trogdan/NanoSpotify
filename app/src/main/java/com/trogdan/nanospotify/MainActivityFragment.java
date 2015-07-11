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
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;
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
                        if(artistsPager.artists.items.size() == 0)
                        {
                            Toast.makeText(getActivity(), R.string.artist_fail, Toast.LENGTH_LONG).show();
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

        private String getClosestImageUriBySize(Artist artist, ImageView view)
        {
            if (artist.images.size() == 0) return null;

            //Get the smallest image that is larger than the imageview in both dimensions
            //or the largest available
            int width = view.getDrawable().getIntrinsicWidth();
            int height = view.getDrawable().getIntrinsicHeight();

            //spotify api says largest first
            for(int i = artist.images.size()-1; i >= 0 ; i--)
            {
                Image image = artist.images.get(i);
                if(image.width >= width && image.height >= height)
                    return image.url;
            }

            return artist.images.get(0).url;
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

            String imageUrl = getClosestImageUriBySize(item, imageView);

            if(imageUrl != null)
            {
                Log.d(LOG_TAG, "Loading picasso with uri " + imageUrl);

                Picasso.with(getActivity())
                        .load(imageUrl)
                        .placeholder(R.mipmap.ic_launcher)
                        .noFade()
                        .fit()
                        .centerInside()
                        .into(imageView);
            }
            else
                imageView.setImageResource(R.mipmap.ic_launcher);

            TextView textView = (TextView)convertView
                    .findViewById(R.id.artist_name_text);
            textView.setText(item.name);

            return convertView;
        }
    }
}
