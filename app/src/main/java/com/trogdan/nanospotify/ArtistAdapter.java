package com.trogdan.nanospotify;

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
    private ArtistFragment artistFragment;
    private final String LOG_TAG = ArtistAdapter.class.getSimpleName();

    private ViewHolder viewHolder;

    public ArtistAdapter(ArtistFragment artistFragment, ArrayList<Artist> items) {
        super(artistFragment.getActivity(), 0, items);
        this.artistFragment = artistFragment;
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
        ViewHolder viewHolder;
        View view = convertView;

        if (view == null) {
            view = artistFragment.getActivity().getLayoutInflater()
                    .inflate(R.layout.list_item_artist, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.imageView = (ImageView) view
                    .findViewById(R.id.artist_icon);
            viewHolder.textView = (TextView) view
                    .findViewById(R.id.artist_name_text);

            view.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder)view.getTag();
        }

        // Get the artist being loaded for the listview
        final Artist item = getItem(position);

        // Find the right size image to load
        final String imageUrl = getClosestImageUriBySize(item, viewHolder.imageView);

//        if (imageUrl != null) {
//            Log.d(LOG_TAG, "Loading picasso with artist uri " + imageUrl);
//        }

        viewHolder.imageView.setImageBitmap(null);

        // If an image is available load it
        Picasso.with(artistFragment.getActivity())
                .load(imageUrl)
                .placeholder(R.drawable.ic_artist_icon)
                .noFade()
                .fit()
                .centerInside()
                .into(viewHolder.imageView);

        viewHolder.textView.setText(item.name);

        return view;
    }

    private class ViewHolder {
        ImageView imageView;
        TextView textView;
    }
}
