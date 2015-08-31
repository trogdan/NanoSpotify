package com.trogdan.nanospotify;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
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
import java.util.concurrent.TimeUnit;


/**
 * A placeholder fragment containing a simple view.
 */
public class PlayerFragment extends DialogFragment {

    public static final String PLAYERFRAGMENT_TAG = "PFTAG";

    public static final String PLAYERS_ARGS = "PFARGS";
    public static final String PLAYERTRACKS_ARG = "PTSARG";
    public static final String PLAYERPLAYTRACK_ARG = "PFTARG";
    public static final String PLAYERSEEK_ARG = "PSARG";
    public static final String PLAYERCHANGE_ARG = "PCARG";
    public static final String PLAYERCURRENTTRACK_ARG = "PCTARG";

    private ViewHolder mViewHolder;
    private ArrayList<ParcelableTrack> mTrackList;
    private ArrayList<ParcelableTrack> mPreviousList;
    private int mCurrentTrack;

    private BroadcastReceiver mReceiver;

    private String formatDurationSeconds(int seconds)
    {
        long hours = TimeUnit.SECONDS.toHours(seconds);
        seconds -= hours * 3600;

        long minutes = TimeUnit.SECONDS.toMinutes(seconds);
        seconds -= minutes * 60;

        if (hours > 0)
        {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
        else
        {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

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

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.hasExtra(MusicService.STATUS_CURRENT_POSITION))
                {
                    int position = intent.getIntExtra(MusicService.STATUS_CURRENT_POSITION, 0);
                    position /= 1000;
                    mViewHolder.mCurrentTimeText.setText(formatDurationSeconds(position));
                    mViewHolder.mSeekBar.setProgress(position);
                }
                if(intent.hasExtra(MusicService.STATUS_CURRENT_TRACK))
                {
                    // we now know the track has started, update the UI.
                    mCurrentTrack = intent.getIntExtra(MusicService.STATUS_CURRENT_TRACK, 0);
                    updateViewsFromTrack(mTrackList.get(mCurrentTrack));
                    setPlaybackButton(true);
                }
                if (intent.hasExtra(PLAYERPLAYTRACK_ARG) && intent.hasExtra(PLAYERTRACKS_ARG)) {
                    // Oh hey
                    Bundle args = intent.getExtras();
                    mCurrentTrack = args.getInt(PLAYERPLAYTRACK_ARG);
                    mTrackList = args.getParcelableArrayList(PLAYERTRACKS_ARG);

                    updateViewsFromTrack(mTrackList.get(mCurrentTrack));
                    setPlaybackButton(true);
                }
            }
        };

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mReceiver, new IntentFilter(MusicService.STATUS_SERVICE));

        mViewHolder = new ViewHolder();

        mViewHolder.mArtistText = (TextView) rootView.findViewById(R.id.artist_player_text);
        mViewHolder.mAlbumText = (TextView) rootView.findViewById(R.id.album_player_text);
        mViewHolder.mTrackText = (TextView) rootView.findViewById(R.id.track_player_text);
        mViewHolder.mAlbumArtImage = (ImageView) rootView.findViewById(R.id.album_player_image);
        mViewHolder.mCurrentTimeText = (TextView) rootView.findViewById(R.id.now_time_text);
        mViewHolder.mEndTimeText = (TextView) rootView.findViewById(R.id.end_time_text);

        mViewHolder.mSeekBar = (SeekBar) rootView.findViewById(R.id.seek_bar);
        mViewHolder.mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int position, boolean fromUser) { }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Intent i = new Intent(getActivity(), MusicService.class);
                i.setAction(MusicService.ACTION_SEEK);
                i.putExtra(PLAYERSEEK_ARG, seekBar.getProgress() * 1000);
                getActivity().startService(i);
            }
        });

        mViewHolder.mTogglePlaybackButton = (ImageButton) rootView.findViewById(R.id.play_button);
        mViewHolder.mTogglePlaybackButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                togglePlaybackButton();

                // Perform action on click
                Intent i = new Intent(getActivity(), MusicService.class);
                i.setAction(MusicService.ACTION_TOGGLE_PLAYBACK);
                getActivity().startService(i);
            }
        });

        mViewHolder.mBackButton = (ImageButton) rootView.findViewById(R.id.back_button);
        mViewHolder.mBackButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // TODO toast if no more songs
                // TODO share current track number with MusicService to prevent data mismatch
                if(mCurrentTrack - 1 < 0) return;
                mCurrentTrack--;

                updateViewsFromTrack(null);

                setPlaybackButton(true);

                Intent i = new Intent(getActivity(), MusicService.class);
                i.setAction(MusicService.ACTION_PREVIOUS);
                getActivity().startService(i);
            }
        });

        mViewHolder.mNextButton = (ImageButton) rootView.findViewById(R.id.next_button);
        mViewHolder.mNextButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // TODO toast if no more songs
                // TODO share current track number with MusicService to prevent data mismatch
                if (mCurrentTrack + 1 >= mTrackList.size()) return;
                mCurrentTrack++;

                updateViewsFromTrack(null);

                setPlaybackButton(true);

                Intent i = new Intent(getActivity(), MusicService.class);
                i.setAction(MusicService.ACTION_NEXT);
                getActivity().startService(i);
            }
        });

        Bundle args;
        if(savedInstanceState == null) {
            args = getArguments();
        }
        else
        {
            args = savedInstanceState.getBundle(PLAYERS_ARGS);
        }

        if (args != null) {
            mCurrentTrack = args.getInt(PLAYERPLAYTRACK_ARG);
            mTrackList = args.getParcelableArrayList(PLAYERTRACKS_ARG);

            if(mCurrentTrack == PlayerActivity.PLAYER_CURRENT_TRACK &&
                    mTrackList == null)
            {
                // Let's ask the service for what's playing
                final Intent i = new Intent(getActivity(), MusicService.class);
                i.setAction(MusicService.ACTION_TRACKS_REQUEST);
                getActivity().startService(i);
            }
            else {
                updateViewsFromTrack(null);

                setPlaybackButton(true);

                // Send an intent with the URIs of the songs to load. This is expected by MusicService.
                final Intent i = new Intent(getActivity(), MusicService.class);
                i.setAction(MusicService.ACTION_URLS);

                // Only include m_trackList if different
                if (args.getBoolean(PLAYERCHANGE_ARG)) {
                    i.putExtras(args);
                } else {
                    i.putExtra(PLAYERPLAYTRACK_ARG, mCurrentTrack);
                }
                getActivity().startService(i);

                // And start
                i.setAction(MusicService.ACTION_PLAY);
                getActivity().startService(i);
            }
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

    private void setPlaybackButton(boolean playState)
    {
        mViewHolder.mTogglePlaybackButton.setImageBitmap(null);

        if (!playState)
        {
            mViewHolder.mTogglePlaybackButton.setImageResource(android.R.drawable.ic_media_play);
        }
        else // playState shows pause
        {
            mViewHolder.mTogglePlaybackButton.setImageResource(android.R.drawable.ic_media_pause);
        }

    }

    private void togglePlaybackButton()
    {
        mViewHolder.mTogglePlaybackButton.setImageBitmap(null);

        if ((int)mViewHolder.mTogglePlaybackButton.getTag() == android.R.drawable.ic_media_pause)
        {
            setPlaybackButton(false);
            mViewHolder.mTogglePlaybackButton.setTag(android.R.drawable.ic_media_play);
        }
        else // ic_media_play
        {
            setPlaybackButton(true);
            mViewHolder.mTogglePlaybackButton.setTag(android.R.drawable.ic_media_pause);
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle args = getArguments();
        args.putInt(PLAYERPLAYTRACK_ARG, mCurrentTrack);
        outState.putBundle(PLAYERS_ARGS, args);
    }

    private void updateViewsFromTrack(ParcelableTrack track)
    {
        mViewHolder.mAlbumArtImage.setImageBitmap(null);

        if(track == null)
        {
            mViewHolder.mArtistText.setText("LOADING...");
            mViewHolder.mAlbumText.setText("");
            mViewHolder.mTrackText.setText("");
            mViewHolder.mAlbumArtImage.setImageResource(R.mipmap.ic_launcher);
        }
        else
        {
            mViewHolder.mArtistText.setText(track.getArtistName());
            mViewHolder.mAlbumText.setText(track.getAlbumName());
            mViewHolder.mTrackText.setText(track.getTrackName());

            mViewHolder.mTogglePlaybackButton.setTag(android.R.drawable.ic_media_pause);

            // If an image is available load it, picasso is already async
            Picasso.with(getActivity())
                    .load(track.getAlbumArt())
                    .placeholder(R.mipmap.ic_launcher)
                    .noFade()
                    .fit()
                    .centerInside()
                    .into(mViewHolder.mAlbumArtImage);

            // In our case, always a maximum of 30 seconds
            //mViewHolder.mEndTimeText.setText(formatDurationSeconds((int)(track.getDuration() / 1000)));
            int maxTime = track.getDuration() < 30 ? (int)(track.getDuration() / 1000) : 30;
            mViewHolder.mEndTimeText.setText(formatDurationSeconds(maxTime));

            mViewHolder.mSeekBar.setMax(maxTime);

        }
        mViewHolder.mSeekBar.setProgress(0);
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
        public TextView mCurrentTimeText;
        public TextView mEndTimeText;
    }

}
