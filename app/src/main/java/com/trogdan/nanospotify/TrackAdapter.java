package com.trogdan.nanospotify;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by dan on 7/12/15.
 */
class TrackAdapter extends ArrayAdapter<Track> {
    private TrackFragment trackActivityFragment;
    private final String LOG_TAG = TrackAdapter.class.getSimpleName();

    public TrackAdapter(TrackFragment trackActivityFragment, ArrayList<Track> items) {
        super(trackActivityFragment.getActivity(), 0, items);
        this.trackActivityFragment = trackActivityFragment;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        View view = convertView;

        if (view == null) {
            view = trackActivityFragment.getActivity().getLayoutInflater()
                    .inflate(R.layout.list_item_track, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.imageView = (ImageView) view
                    .findViewById(R.id.album_icon);
            viewHolder.albumTextView = (TextView) view
                    .findViewById(R.id.album_name_text);
            viewHolder.trackTextView = (TextView) view
                    .findViewById(R.id.track_name_text);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        // Get the track being loaded for the listview
        final Track item = getItem(position);

        // Find the right size image to load
        final String albumUrl = Utility.getClosestImageUriBySize(item.album.images, viewHolder.imageView);

        viewHolder.imageView.setImageBitmap(null);

        if (albumUrl != null) {
            Log.d(LOG_TAG, "Loading picasso with album uri " + albumUrl + " for position " + Integer.toString(position));
        }
        else  {
            Log.d(LOG_TAG, "No album uri for position " + Integer.toString(position));
        }

        // If an image is available load it
        Picasso.with(trackActivityFragment.getActivity())
                .load(albumUrl)
                .placeholder(R.drawable.ic_track_icon)
                .noFade()
                .fit()
                .centerInside()
                .into(viewHolder.imageView);

        // Set texts
        viewHolder.albumTextView.setText(item.album.name);
        viewHolder.trackTextView.setText(item.name);

        return view;
    }

    private class ViewHolder {
        ImageView imageView;
        TextView albumTextView;
        TextView trackTextView;
    }
}
