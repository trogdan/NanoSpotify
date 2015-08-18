package com.trogdan.nanospotify;

import android.app.Dialog;
import android.content.Intent;
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
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.trogdan.nanospotify.data.ParcelableTrack;
import com.trogdan.nanospotify.service.MusicService;

import java.util.ArrayList;


/**
 * A placeholder fragment containing a simple view.
 */
public class PlayerFragment extends DialogFragment {

    public static final String PLAYERFRAGMENT_TAG = "PFTAG";

    public static final String PLAYERTRACKS_ARG = "PTSARG";
    public static final String PLAYERFIRSTTRACK_ARG = "PFTARG";

    private ViewHolder mViewHolder;
    private ArrayList<ParcelableTrack> mTrackList;
    private int mCurrentTrack;

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
        final View rootView = inflater.inflate(R.layout.fragment_player, container, false);

        mViewHolder = new ViewHolder();

        mViewHolder.mArtistText = (TextView) rootView.findViewById(R.id.artist_player_text);
        mViewHolder.mAlbumText = (TextView) rootView.findViewById(R.id.album_player_text);
        mViewHolder.mTrackText = (TextView) rootView.findViewById(R.id.track_player_text);
        mViewHolder.mAlbumArtImage = (ImageView) rootView.findViewById(R.id.album_player_image);

        mViewHolder.mTogglePlaybackButton = (ImageButton) rootView.findViewById(R.id.play_button);
        mViewHolder.mTogglePlaybackButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                mViewHolder.mTogglePlaybackButton.setImageBitmap(null);

                if((int)mViewHolder.mTogglePlaybackButton.getTag() == android.R.drawable.ic_media_pause)
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
                if(mCurrentTrack - 1 < 0) return;
                mCurrentTrack--;

                // Perform action on click
                final ParcelableTrack track = mTrackList.get(mCurrentTrack);

                Intent i = new Intent(getActivity(), MusicService.class);
                i.setAction(MusicService.ACTION_SKIP);
                i.setData(track.getTrack());
                getActivity().startService(i);
            }
        });
        mViewHolder.mNextButton = (ImageButton) rootView.findViewById(R.id.next_button);
        mViewHolder.mNextButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(mCurrentTrack + 1 >= mTrackList.size()) return;
                mCurrentTrack++;

                // Perform action on click
                final ParcelableTrack track = mTrackList.get(mCurrentTrack);

                Intent i = new Intent(getActivity(), MusicService.class);
                i.setAction(MusicService.ACTION_PREVIOUS);
                i.setData(track.getTrack());
                getActivity().startService(i);
            }
        });

        mViewHolder.mSeekBar = (SeekBar) rootView.findViewById(R.id.seek_bar);
        // TODO seek

        final Bundle args = getArguments();
        if (args != null) {
            mCurrentTrack = args.getInt(PLAYERFIRSTTRACK_ARG);
            mTrackList = args.getParcelableArrayList(PLAYERTRACKS_ARG);

            final ParcelableTrack track = mTrackList.get(mCurrentTrack);

            mViewHolder.mArtistText.setText(track.getArtistName());
            mViewHolder.mAlbumText.setText(track.getAlbumName());
            mViewHolder.mTrackText.setText(track.getTrackName());

            // If an image is available load it, picasso is already async
            Picasso.with(getActivity())
                    .load(track.getAlbumArt())
                    .placeholder(R.mipmap.ic_launcher)
                    .noFade()
                    .fit()
                    .centerInside()
                    .into(mViewHolder.mAlbumArtImage);

            // Send an intent with the URI of the song to load. This is expected by MusicService.
            final Intent i = new Intent(getActivity(), MusicService.class);
            i.setAction(MusicService.ACTION_URLS);
            i.putExtras(args);
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
        public TextView mArtistText;
        public TextView mAlbumText;
        public TextView mTrackText;
        public ImageView mAlbumArtImage;
        public ImageButton mTogglePlaybackButton;
        public ImageButton mBackButton;
        public ImageButton mNextButton;
        public SeekBar mSeekBar;
    }
}
