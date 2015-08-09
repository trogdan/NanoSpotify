package com.trogdan.nanospotify;

import android.app.Dialog;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * A placeholder fragment containing a simple view.
 */
public class PlayerFragment extends DialogFragment {

    public static final String PLAYERFRAGMENT_TAG = "PFTAG";

    // TODO DB and content provider
    public static final String PLAYERTRACK_ARG = "PTARG";
    public static final String PLAYERTRACKNAME_ARG = "PTNARG";
    public static final String PLAYERTRACKDURATION_ARG = "PTDARG";
    public static final String PLAYERALBUMNAME_ARG = "PALNARG";
    public static final String PLAYERALBUMART_ARG = "PALAARG";
    public static final String PLAYERARTISTNAME_ARG = "PARNARG";

    public PlayerFragment() {
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getDialog()== null) {
            return;
        }

        // Not sure why, but root layout in fragment_player.xml seems to be totally ignored.
        getDialog().getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.MATCH_PARENT);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_player, container, false);

        Bundle args = getArguments();
        if (args != null) {
            ImageView albumImageView = (ImageView) rootView.findViewById(R.id.album_image);

            // If an image is available load it, it's already async
            Picasso.with(getActivity())
                    .load(args.getString(PLAYERALBUMART_ARG))
                    .placeholder(R.mipmap.ic_launcher)
                    .noFade()
                    .fit()
                    .centerInside()
                    .into(albumImageView);
        }

        setStyle(DialogFragment.STYLE_NORMAL, R.style.PlayerDialog);

        return rootView;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

}
