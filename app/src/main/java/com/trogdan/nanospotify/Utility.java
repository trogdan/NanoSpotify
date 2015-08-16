package com.trogdan.nanospotify;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.widget.ImageView;

import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Image;

/**
 * Created by dan on 8/9/15.
 */
public class Utility {

    private static final SpotifyApi m_spotifyApi = new SpotifyApi();
    private static final SpotifyService m_spotifyService = m_spotifyApi.getService();

    public static SpotifyService getSpotifyService() {
        return m_spotifyService;
    }

    public static SpotifyApi getSpotifyApi() {
        return m_spotifyApi;
    }

    public static String getClosestImageUriBySize(List<Image> images, ImageView view) {
        if (images.size() == 0) return null;

        // Get the smallest image that is larger than the imageview in both dimensions
        // or the largest available
        final int width = view.getDrawable().getIntrinsicWidth();
        final int height = view.getDrawable().getIntrinsicHeight();

        // spotify api says largest first
        for (int i = images.size() - 1; i >= 0; i--) {
            final Image image = images.get(i);
            if (image.width >= width && image.height >= height)
                return image.url;
        }

        return images.get(0).url;
    }

}
