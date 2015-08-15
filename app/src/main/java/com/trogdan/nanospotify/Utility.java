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

    /***
     * Android L (lollipop, API 21) introduced a new problem when trying to invoke implicit intent,
     * "java.lang.IllegalArgumentException: Service Intent must be explicit"
     *
     * If you are using an implicit intent, and know only 1 target would answer this intent,
     * This method will help you turn the implicit intent into the explicit form.
     *
     * Inspired from SO answer: http://stackoverflow.com/a/26318757/1446466
     * @param context
     * @param implicitIntent - The original implicit intent
     * @return Explicit Intent created from the implicit original intent
     */
    public static Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
        // Retrieve all services that can match the given intent
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);

        // Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }

        // Get component info and create ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);

        // Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);

        // Set the component to be explicit
        explicitIntent.setComponent(component);

        return explicitIntent;
    }
}
