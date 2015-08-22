package com.trogdan.nanospotify;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.trogdan.nanospotify.data.ParcelableTrack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * TrackFragment is used to display the top 10 tracks of a selected artist.  Spotify api
 * is passed through the Intent.EXTRA_TEXT.  Current mock-up uses album art on the left of the entry
 * with album name and track name on the right of the entry, with track name given focus via font size
 */
public class TrackFragment extends Fragment {

    public static final String TRACKFRAGMENT_TAG = "TFTAG";
    public static final String TRACKQUERY_ARG = "TQARG";

    private final String LOG_TAG = TrackFragment.class.getSimpleName();

    private TrackAdapter m_trackAdapter;
    private FetchTracksTask m_fetchTracksTask;
    private String m_previousArtistId;
    private boolean m_twoPane;

    public TrackFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        m_twoPane = getResources().getBoolean(R.bool.two_pane);

        m_trackAdapter = new TrackAdapter(this, new ArrayList<Track>());
        final View rootView = inflater.inflate(R.layout.fragment_track, container, false);

        final ListView trackListView = (ListView)rootView.findViewById(R.id.list_view_tracks);
        trackListView.setAdapter(m_trackAdapter);

        /* Set the list item for a track to fire an intent to play the track of a selected
           artist, passing the spotify ID of that track.
         */
        trackListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                ArrayList<ParcelableTrack> trackList = new ArrayList<ParcelableTrack>();
                for(int i = 0; i < parent.getCount(); i++)
                {
                    final Track track = (Track) parent.getItemAtPosition(i);
                    final ParcelableTrack parcelableTrack = new ParcelableTrack(track);
                    trackList.add(parcelableTrack);
                }

                showPlayerDialog(trackList, position);
            }
        });

        Bundle args = getArguments();
        if (args != null) {
            getTracks(args.getString(TRACKQUERY_ARG));
        }

        return rootView;
    }

    public void showPlayerDialog(ArrayList<ParcelableTrack> trackList, int firstTrack) {

        final Bundle args = new Bundle();
        args.putParcelableArrayList(PlayerFragment.PLAYERTRACKS_ARG, trackList);
        args.putInt(PlayerFragment.PLAYERFIRSTTRACK_ARG, firstTrack);

        if (m_twoPane) {
            final FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

            final PlayerFragment playerFragment = new PlayerFragment();
            playerFragment.setArguments(args);
            // The device is using a large layout, so show the fragment as a dialog
            playerFragment.show(fragmentManager, PlayerFragment.PLAYERFRAGMENT_TAG);
        } else {
            Intent intent = new Intent(getActivity(), PlayerActivity.class);
            intent.putExtras(args);
            startActivity(intent);
        }
    }


    public void getTracks(String artistId) {
        // No point spinning up a new query if it's a repeat
        if(artistId != null && !artistId.equals(m_previousArtistId)) {
            m_fetchTracksTask = new FetchTracksTask();
            m_fetchTracksTask.execute(artistId);
            m_previousArtistId = artistId;
        }
    }

    public class FetchTracksTask extends AsyncTask<String, Void, Void> {
        private final String LOG_TAG = FetchTracksTask.class.getSimpleName();

        // Retrofit callbacks are performed on main UI thread, so no onPostExecute
        // needed.

        @Override
        protected Void doInBackground(String... params) {

            if (params.length != 1) {
                Log.e(LOG_TAG, "Invalid params passed to doInBackground");
                return null;
            }

            /* Directions say to add country code to the query string, but i'm not using query
               string directly.  Instead use the spotify-api options.

               In reality, to enforce locality restrictions, this should not be hard-coded or a
               preference, but pulled from the current location of the user.
             */
            Map<String, Object> options = new HashMap<>();
            options.put(SpotifyService.COUNTRY,
                    Locale.getDefault().getCountry().toUpperCase());
            options.put(SpotifyService.OFFSET, 0);
            options.put(SpotifyService.LIMIT, 10);

            // Get the artist top 10 tracks
            Utility.getSpotifyService().getArtistTopTrack(params[0], options, new Callback<Tracks>() {
                @Override
                public void success(Tracks tracks, Response response) {
                    Log.d(LOG_TAG, "Track query success: " + tracks.tracks.size());

                    m_trackAdapter.clear();
                    for (int i = 0; i < tracks.tracks.size(); i++) {
                        m_trackAdapter.add(tracks.tracks.get(i));
                    }

                    // Again, not sure of decision to use toast, but display no results available
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

            return null;
        }
    }
}
