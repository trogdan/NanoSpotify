package com.trogdan.nanospotify;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Image;

/**
 * Created by dan on 7/12/15.
 */
class ArtistAdapter extends ArrayAdapter<Artist> {
    private MainActivityFragment mainActivityFragment;
    private final String LOG_TAG = ArtistAdapter.class.getSimpleName();

    private ViewHolder viewHolder;

    public ArtistAdapter(MainActivityFragment mainActivityFragment, ArrayList<Artist> items) {
        super(mainActivityFragment.getActivity(), 0, items);
        this.mainActivityFragment = mainActivityFragment;
    }

    private String getClosestImageUriBySize(Artist artist, ImageView view) {
        if (artist.images.size() == 0) return null;

        // Get the smallest image that is larger than the imageview in both dimensions
        // or the largest available. No point using the 200px mentioned in the directions if
        // the view is larger or smaller than that threshold
        final int width = view.getDrawable().getIntrinsicWidth();
        final int height = view.getDrawable().getIntrinsicHeight();

        // spotify api says largest first
        for (int i = artist.images.size() - 1; i >= 0; i--) {
            final Image image = artist.images.get(i);
            if (image.width >= width && image.height >= height)
                return image.url;
        }

        return artist.images.get(0).url;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mainActivityFragment.getActivity().getLayoutInflater()
                    .inflate(R.layout.list_item_artist, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.imageView = (ImageView) convertView
                    .findViewById(R.id.artist_icon);
            viewHolder.textView = (TextView) convertView
                    .findViewById(R.id.artist_name_text);
        }

        // Get the artist being loaded for the listview
        final Artist item = getItem(position);

        // Find the right size image to load
        final String imageUrl = getClosestImageUriBySize(item, viewHolder.imageView);

        // If an image is available load it
        if (imageUrl != null) {
            Log.d(LOG_TAG, "Loading picasso with uri " + imageUrl);

            Picasso.with(mainActivityFragment.getActivity())
                    .load(imageUrl)
                    .placeholder(R.mipmap.ic_artist_icon)
                    .noFade()
                    .fit()
                    .centerInside()
                    .into(viewHolder.imageView);
        } else {
            // If no image, just use the default.
            viewHolder.imageView.setImageResource(R.mipmap.ic_artist_icon);
        }

        viewHolder.textView = (TextView) convertView
                .findViewById(R.id.artist_name_text);
        viewHolder.textView.setText(item.name);

        return convertView;
    }

    private class ViewHolder {
        ImageView imageView;
        TextView textView;
    }
}
