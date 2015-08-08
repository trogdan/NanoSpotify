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
    private TrackActivityFragment trackActivityFragment;
    private final String LOG_TAG = TrackAdapter.class.getSimpleName();

    public TrackAdapter(TrackActivityFragment trackActivityFragment, ArrayList<Track> items) {
        super(trackActivityFragment.getActivity(), 0, items);
        this.trackActivityFragment = trackActivityFragment;
    }

    private String getClosestImageUriBySize(AlbumSimple album, ImageView view) {
        if (album.images.size() == 0) return null;

        // Get the smallest image that is larger than the imageview in both dimensions
        // or the largest available
        final int width = view.getDrawable().getIntrinsicWidth();
        final int height = view.getDrawable().getIntrinsicHeight();

        // spotify api says largest first
        for (int i = album.images.size() - 1; i >= 0; i--) {
            final Image image = album.images.get(i);
            if (image.width >= width && image.height >= height)
                return image.url;
        }

        return album.images.get(0).url;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = trackActivityFragment.getActivity().getLayoutInflater()
                    .inflate(R.layout.list_item_track, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.imageView = (ImageView) convertView
                    .findViewById(R.id.album_icon);
            viewHolder.albumTextView = (TextView) convertView
                    .findViewById(R.id.album_name_text);
            viewHolder.trackTextView = (TextView) convertView
                    .findViewById(R.id.track_name_text);
            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        // Get the track being loaded for the listview
        final Track item = getItem(position);

        // Find the right size image to load
        final String albumUrl = getClosestImageUriBySize(item.album, viewHolder.imageView);

        // If an image is available load it
        if (albumUrl != null) {
            Log.d(LOG_TAG, "Loading picasso with album uri " + albumUrl);

            Picasso.with(trackActivityFragment.getActivity())
                    .load(albumUrl)
                    .placeholder(R.mipmap.ic_track_icon)
                    .noFade()
                    .fit()
                    .centerInside()
                    .into(viewHolder.imageView);
        } else {
            // If no image, just use the default.
            viewHolder.imageView.setImageResource(R.mipmap.ic_track_icon);
        }
        // Set texts
        viewHolder.albumTextView.setText(item.album.name);
        viewHolder.trackTextView.setText(item.name);

        return convertView;
    }

    private class ViewHolder {
        ImageView imageView;
        TextView albumTextView;
        TextView trackTextView;
    }
}
