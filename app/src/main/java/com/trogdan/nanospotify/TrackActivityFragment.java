package com.trogdan.nanospotify;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import kaaes.spotify.webapi.android.models.TracksPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * A placeholder fragment containing a simple view.
 */
public class TrackActivityFragment extends Fragment {

    private final String LOG_TAG = TrackActivityFragment.class.getSimpleName();

    private final SpotifyApi m_spotifyApi = new SpotifyApi();
    private final SpotifyService m_spotifyService = m_spotifyApi.getService();

    private TrackAdapter m_trackAdapter;

    public TrackActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        m_trackAdapter = new TrackAdapter(new ArrayList<Track>());
        final View rootView = inflater.inflate(R.layout.fragment_track, container, false);

        final ListView trackListView = (ListView)rootView.findViewById(R.id.list_view_tracks);
        trackListView.setAdapter(m_trackAdapter);

        trackListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
//                final Track track = (Track) parent.getItemAtPosition(position);
//
//                startActivity(new Intent(getActivity(),
//                        TrackActivity.class).putExtra(Intent.EXTRA_TEXT, track.href));
            }
        });

        final Intent i = getActivity().getIntent();
        if( i != null && i.hasExtra(Intent.EXTRA_TEXT)) {
            final String artistID = i.getStringExtra(Intent.EXTRA_TEXT);

            /* Directions say to add country code to the query string, but i'm not using query
               string directly.  Instead use the spotify-api options.
               In reality, to enforce locality
               restrictions, this should not be hard-coded or a preference, but pulled from the
               current location of the user.
             */
            Map<String, Object> options = new HashMap<>();
            options.put(SpotifyService.COUNTRY,
                    ((TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE))
                        .getSimCountryIso().toUpperCase());
            options.put(SpotifyService.OFFSET, 0);
            options.put(SpotifyService.LIMIT, 10);

            m_spotifyService.getArtistTopTrack(artistID, options, new Callback<Tracks>() {
                @Override
                public void success(Tracks tracks, Response response) {
                    Log.d(LOG_TAG, "Track query success: " + tracks.tracks.size());

                    m_trackAdapter.clear();
                    for (int i = 0; i < tracks.tracks.size() ; i++) {
                        m_trackAdapter.add(tracks.tracks.get(i));
                    }
                    if (tracks.tracks.size() == 0) {
                        Toast.makeText(getActivity(),
                                R.string.track_fail,
                                Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.d(LOG_TAG, "Track query failure", error);
                }
            });
        }
        return rootView;
    }

    private class TrackAdapter extends ArrayAdapter<Track> {
        private final String LOG_TAG = TrackAdapter.class.getSimpleName();

        public TrackAdapter(ArrayList<Track> items) {
            super(getActivity(), 0, items);
        }

        private String getClosestImageUriBySize(AlbumSimple album, ImageView view)
        {
            if (album.images.size() == 0) return null;

            //Get the smallest image that is larger than the imageview in both dimensions
            //or the largest available
            final int width = view.getDrawable().getIntrinsicWidth();
            final int height = view.getDrawable().getIntrinsicHeight();

            //spotify api says largest first
            for(int i = album.images.size()-1; i >= 0 ; i--)
            {
                final Image image = album.images.get(i);
                if(image.width >= width && image.height >= height)
                    return image.url;
            }

            return album.images.get(0).url;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater()
                        .inflate(R.layout.list_item_track, parent, false);
            }

            final Track item = getItem(position);
            final ImageView imageView = (ImageView)convertView
                    .findViewById(R.id.album_icon);

            final String albumUrl = getClosestImageUriBySize(item.album, imageView);

            if(albumUrl != null)
            {
                Log.d(LOG_TAG, "Loading picasso with album uri " + albumUrl);

                Picasso.with(getActivity())
                        .load(albumUrl)
                        .placeholder(R.mipmap.ic_launcher)
                        .noFade()
                        .fit()
                        .centerInside()
                        .into(imageView);
            }
            else
                imageView.setImageResource(R.mipmap.ic_launcher);

            final TextView albumTextView = (TextView)convertView
                    .findViewById(R.id.album_name_text);
            albumTextView.setText(item.album.name);
            final TextView trackTextView = (TextView)convertView
                    .findViewById(R.id.track_name_text);
            trackTextView.setText(item.name);

            return convertView;
        }
    }
}
