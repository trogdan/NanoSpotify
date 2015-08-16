package com.trogdan.nanospotify;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.squareup.picasso.Picasso;
import com.trogdan.nanospotify.service.MusicService;


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

    private ViewHolder mViewHolder;

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

        mViewHolder = new ViewHolder();
        mViewHolder.mTogglePlaybackButton = (ImageButton) rootView.findViewById(R.id.play_button);
        mViewHolder.mTogglePlaybackButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if(mViewHolder.mTogglePlaybackButton.getTag() == android.R.drawable.ic_media_pause)
                {
                    mViewHolder.mTogglePlaybackButton.setImageResource(android.R.drawable.ic_media_play);
                    mViewHolder.mTogglePlaybackButton.setTag(android.R.drawable.ic_media_play);
                }
                else //ic_media_play
                {
                    mViewHolder.mTogglePlaybackButton.setImageResource(android.R.drawable.ic_media_pause);
                    mViewHolder.mTogglePlaybackButton.setTag(android.R.drawable.ic_media_pause);
                }

                // Perform action on click
                Intent i = new Intent(getActivity(), MusicService.class);
                i.setAction(MusicService.ACTION_TOGGLE_PLAYBACK);
                getActivity().startService(i);
            }
        });
        mViewHolder.mBackButton = (ImageButton) rootView.findViewById(R.id.back_button);
        mViewHolder.mBackButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Intent i = new Intent(getActivity(), MusicService.class);
                i.setAction(MusicService.ACTION_SKIP);
                getActivity().startService(i);
            }
        });
        mViewHolder.mNextButton = (ImageButton) rootView.findViewById(R.id.next_button);
        mViewHolder.mNextButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Intent i = new Intent(getActivity(), MusicService.class);
                i.setAction(MusicService.ACTION_PREVIOUS);
                getActivity().startService(i);
            }
        });

        mViewHolder.mSeekBar = (SeekBar) rootView.findViewById(R.id.seek_bar);
        // TODO seek

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

            // Send an intent with the URL of the song to load. This is expected by
            // MusicService.
            Intent i = new Intent(getActivity(), MusicService.class);
            i.setAction(MusicService.ACTION_URL);
            Uri uri = Uri.parse(args.getString(PLAYERTRACK_ARG));
            i.setData(uri);
            getActivity().startService(i);

            mViewHolder.mTogglePlaybackButton.setTag(android.R.drawable.ic_media_pause);
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

    private class ViewHolder
    {
        public ImageButton mTogglePlaybackButton;
        public ImageButton mBackButton;
        public ImageButton mNextButton;
        public SeekBar mSeekBar;
    }
}
