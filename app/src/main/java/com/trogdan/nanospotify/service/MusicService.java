/*   
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.trogdan.nanospotify.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.trogdan.nanospotify.PlayerActivity;
import com.trogdan.nanospotify.PlayerFragment;
import com.trogdan.nanospotify.R;
import com.trogdan.nanospotify.MainActivity;
import com.trogdan.nanospotify.data.ParcelableTrack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Service that handles media playback. This is the Service through which we perform all the media
 * handling in our application.  It waits for Intents (which come from one of our activities,
 * which signal the service to perform specific operations: Play, Pause, Rewind, Skip, etc.
 */
public class MusicService extends Service implements OnCompletionListener, OnPreparedListener,
                OnErrorListener, MusicFocusable {

    // The tag we put on debug messages
    final static String LOG_TAG = MusicService.class.getSimpleName();

    // These are the Intent actions that we are prepared to handle. Notice that the fact these
    // constants exist in our class is a mere convenience: what really defines the actions our
    // service can handle are the <action> tags in the <intent-filters> tag for our service in
    // AndroidManifest.xml.
    public static final String ACTION_TOGGLE_PLAYBACK = "com.trogdan.nanospotify.musicservice.action.TOGGLE_PLAYBACK";
    public static final String ACTION_PLAY = "com.trogdan.nanospotify.musicservice.action.PLAY";
    public static final String ACTION_PAUSE = "com.trogdan.nanospotify.musicservice.action.PAUSE";
    public static final String ACTION_STOP = "com.trogdan.nanospotify.musicservice.action.STOP";
    public static final String ACTION_NEXT = "com.trogdan.nanospotify.musicservice.action.NEXT";
    public static final String ACTION_PREVIOUS = "com.trogdan.nanospotify.musicservice.action.PREVIOUS";
    public static final String ACTION_SEEK = "com.trogdan.nanospotify.musicservice.action.SEEK";
    public static final String ACTION_URLS = "com.trogdan.nanospotify.musicservice.action.URLS";
    public static final String ACTION_TRACKS_REQUEST = "com.trogdan.nanospotify.musicservice.action.TRACKS_REQUEST";

    // These are local broadcasts used to notify our UI of current media service state.
    public static final String STATUS_SERVICE = "com.trogdan.noanospotify.musicservice.status";
    public static final String STATUS_CURRENT_POSITION = "com.trogdan.noanospotify.musicservice.status.CURRENT_POSITION";
    public static final String STATUS_CURRENT_TRACK = "com.trogdan.noanospotify.musicservice.status.CURRENT_TRACK";

    // The volume we set the media player to when we lose audio focus, but are allowed to reduce
    // the volume instead of stopping playback.
    public static final float DUCK_VOLUME = 0.1f;

    // our media player
    MediaPlayer mPlayer = null;

    // our AudioFocusHelper object, if it's available (it's available on SDK level >= 8)
    // If not available, this will be null. Always check for null before using!
    AudioFocusHelper mAudioFocusHelper = null;

    // indicates the state our service:
    enum State {
        Stopped,    // media player is stopped and not prepared to play
        Preparing,  // media player is preparing...
        Playing,    // playback active (media player ready!). (but the media player may actually be
                    // paused in this state if we don't have audio focus. But we stay in this state
                    // so that we know we have to resume playback once we get focus back)
        Paused      // playback paused (media player ready!)
    }

    public static State mState = State.Stopped;

    enum PauseReason {
        UserRequest,  // paused by user request
        FocusLoss,    // paused because of audio focus loss
    };

    // why did we pause? (only relevant if mState == State.Paused)
    PauseReason mPauseReason = PauseReason.UserRequest;

    // do we have audio focus?
    enum AudioFocus {
        NoFocusNoDuck,    // we don't have audio focus, and can't duck
        NoFocusCanDuck,   // we don't have focus, but can play at a low volume ("ducking")
        Focused           // we have full audio focus
    }
    AudioFocus mAudioFocus = AudioFocus.NoFocusNoDuck;

    // title of the song we are currently playing
    String mSongTitle = "";

    // Wifi lock that we hold when streaming files from the internet, in order to prevent the
    // device from shutting off the Wifi radio
    WifiLock mWifiLock;

    // The ID we use for the notification (the onscreen alert that appears at the notification
    // area at the top of the screen as an icon -- and as text as well if the user expands the
    // notification area).
    final int NOTIFICATION_ID = 1;

    AudioManager mAudioManager;
    NotificationManager mNotificationManager;

    Notification mNotification = null;

    // Keep track of the track list and current track send by a URI update
    private ArrayList<ParcelableTrack> mTrackList;
    private int mCurrentTrack;
    private int mNextTrack;

    // Used to send status back to UI
    private LocalBroadcastManager mBroadcaster;

    // Used to fire position updates
    private TimerTask mPositionTimerTask;
    private Timer mPositionTimer;

    /**
     * Makes sure the media player exists and has been reset. This will create the media player
     * if needed, or reset the existing media player if one already exists.
     */
    void createMediaPlayerIfNeeded() {
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();

            // Make sure the media player will acquire a wake-lock while playing. If we don't do
            // that, the CPU might go to sleep while the song is playing, causing playback to stop.
            //
            // Remember that to use this, we have to declare the android.permission.WAKE_LOCK
            // permission in AndroidManifest.xml.
            mPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

            // we want the media player to notify us when it's ready preparing, and when it's done
            // playing:
            mPlayer.setOnPreparedListener(this);
            mPlayer.setOnCompletionListener(this);
            mPlayer.setOnErrorListener(this);
        }
        else
            mPlayer.reset();
    }

    @Override
    public void onCreate() {
        Log.i(LOG_TAG, "debug: Creating service");

        // Create the Wifi lock (this does not acquire the lock, this just creates it)
        mWifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                        .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        // create the Audio Focus Helper, if the Audio Focus feature is available (SDK 8 or above)
        if (android.os.Build.VERSION.SDK_INT >= 8)
            mAudioFocusHelper = new AudioFocusHelper(getApplicationContext(), this);
        else
            mAudioFocus = AudioFocus.Focused; // no focus feature, so we always "have" audio focus

        // Create the mBroadcaster to send state back to UI
        mBroadcaster = LocalBroadcastManager.getInstance(this);

        PlayerActivity.isServiceLaunched = true;
    }

    /**
     * Called when we receive an Intent. When we receive an intent sent to us via startService(),
     * this is the method that gets called. So here we react appropriately depending on the
     * Intent's action, which specifies what is being requested of us.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String action = intent.getAction();
        if (action.equals(ACTION_TOGGLE_PLAYBACK)) processTogglePlaybackRequest();
        else if (action.equals(ACTION_PLAY)) processPlayRequest();
        else if (action.equals(ACTION_PAUSE)) processPauseRequest();
        else if (action.equals(ACTION_NEXT)) processNextRequest();
        else if (action.equals(ACTION_PREVIOUS)) processPreviousRequest();
        else if (action.equals(ACTION_STOP)) processStopRequest();
        else if (action.equals(ACTION_SEEK)) processSeekRequest(intent);
        else if (action.equals(ACTION_URLS)) processAddRequest(intent);
        else if (action.equals(ACTION_TRACKS_REQUEST)) processTracksRequest();

        // After all that's said and done, tell the UI which track we are currently on

        return START_NOT_STICKY; // Means we started the service, but don't want it to
                                 // restart in case it's killed.
    }

    void processTogglePlaybackRequest() {
        if (mState == State.Paused || mState == State.Stopped) {
            processPlayRequest();
        } else if (mState == State.Playing) {
            processPauseRequest();
        }
    }

    void processPlayRequest() {
        tryToGetAudioFocus();

        // actually play the song
        if (mState == State.Stopped) {
            playSong(mCurrentTrack);
        }
        else if (mState == State.Paused) {
            // If we're paused, just continue playback and restore the 'foreground service' state.
            mState = State.Playing;
            setUpAsForeground(mSongTitle + " (playing)");
            configAndStartMediaPlayer();
            startTimer();
        }

    }

    void processPauseRequest() {
        if (mState == State.Playing) {
            // Pause media player and cancel the 'foreground service' state.
            mState = State.Paused;
            mPlayer.pause();
            relaxResources(false); // while paused, we always retain the MediaPlayer
            // do not give up audio focus
            // Halt updates to UI
            stopTimer();
        }

    }

    void processNextRequest() {
        if (mState == State.Playing || mState == State.Paused) {
            tryToGetAudioFocus();
            processPauseRequest();
            playNextSong();
        }
    }

    void processPreviousRequest() {
        if (mState == State.Playing || mState == State.Paused) {
            tryToGetAudioFocus();
            processPauseRequest();
            playPreviousSong();
        }
    }

    void processStopRequest() {
        // Halt updates to UI
        stopTimer();

        processStopRequest(false);
    }

    void processStopRequest(boolean force) {
        if (mState == State.Playing || mState == State.Paused || force) {
            mState = State.Stopped;

            // let go of all resources...
            relaxResources(true);
            giveUpAudioFocus();

            // service is no longer necessary. Will be started again if needed.
            stopSelf();
        }
    }

    void processSeekRequest(Intent intent) {
        final boolean wasPlaying = (mState == State.Playing);

        if (mState == State.Playing)
        {
            processPauseRequest();
        }
        if (mState == State.Paused) {
            int position = intent.getIntExtra(PlayerFragment.PLAYERSEEK_ARG, 0);

            mPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                @Override
                public void onSeekComplete(MediaPlayer mediaPlayer) {
                    final Intent i = new Intent(STATUS_SERVICE);
                    i.putExtra(STATUS_CURRENT_POSITION, mPlayer.getCurrentPosition());
                    mBroadcaster.sendBroadcast(i);

                    if (wasPlaying)
                        processPlayRequest();
                }
            });

            mPlayer.seekTo(position);
        }
    }

    void processAddRequest(Intent intent) {
        if (mState == State.Playing)
        {
            processPauseRequest();
        }

        if (mState == State.Paused || mState == State.Stopped) {
            final Bundle args = intent.getExtras();

            // We explay a play request to come momentarily, there shouldn't be a way for the user
            // to unpause before that
            mNextTrack = args.getInt(PlayerFragment.PLAYERPLAYTRACK_ARG);
            final ArrayList nextList = args.getParcelableArrayList(PlayerFragment.PLAYERTRACKS_ARG);

            if (nextList != null)
            {
                mState = State.Stopped;
                relaxResources(true);

                mTrackList = nextList;
                mCurrentTrack = mNextTrack;
            }
            else if (mNextTrack != mCurrentTrack)
            {
                // Let's say we're stopped, so that the expected play operation will play the new track
                mState = State.Stopped;

                mCurrentTrack = mNextTrack;
            }

        }
    }

    void processTracksRequest() {
        if (mTrackList != null) {
            final Intent i = new Intent(STATUS_SERVICE);
            final Bundle args = new Bundle();
            args.putInt(PlayerFragment.PLAYERPLAYTRACK_ARG, mCurrentTrack);
            args.putParcelableArrayList(PlayerFragment.PLAYERTRACKS_ARG, mTrackList);
            i.putExtras(args);
            mBroadcaster.sendBroadcast(i);
        }
    }


    /**
     * Releases resources used by the service for playback. This includes the "foreground service"
     * status and notification, the wake locks and possibly the MediaPlayer.
     *
     * @param releaseMediaPlayer Indicates whether the Media Player should also be released or not
     */
    void relaxResources(boolean releaseMediaPlayer) {
        // stop being a foreground service
        stopForeground(true);

        // stop and release the Media Player, if it's available
        if (releaseMediaPlayer && mPlayer != null) {
            mPlayer.reset();
            mPlayer.release();
            mPlayer = null;
        }

        // we can also release the Wifi lock, if we're holding it
        if (mWifiLock.isHeld()) mWifiLock.release();
    }

    void giveUpAudioFocus() {
        if (mAudioFocus == AudioFocus.Focused && mAudioFocusHelper != null
                                && mAudioFocusHelper.abandonFocus())
            mAudioFocus = AudioFocus.NoFocusNoDuck;
    }

    /**
     * Reconfigures MediaPlayer according to audio focus settings and starts/restarts it. This
     * method starts/restarts the MediaPlayer respecting the current audio focus state. So if
     * we have focus, it will play normally; if we don't have focus, it will either leave the
     * MediaPlayer paused or set it to a low volume, depending on what is allowed by the
     * current focus settings. This method assumes mPlayer != null, so if you are calling it,
     * you have to do so from a context where you are sure this is the case.
     */
    void configAndStartMediaPlayer() {
        if (mAudioFocus == AudioFocus.NoFocusNoDuck) {
            // If we don't have audio focus and can't duck, we have to pause, even if mState
            // is State.Playing. But we stay in the Playing state so that we know we have to resume
            // playback once we get the focus back.
            if (mPlayer.isPlaying()) mPlayer.pause();
            return;
        }
        else if (mAudioFocus == AudioFocus.NoFocusCanDuck)
            mPlayer.setVolume(DUCK_VOLUME, DUCK_VOLUME);  // we'll be relatively quiet
        else
            mPlayer.setVolume(1.0f, 1.0f); // we can be loud

        if (!mPlayer.isPlaying()) mPlayer.start();
    }

    void tryToGetAudioFocus() {
        if (mAudioFocus != AudioFocus.Focused && mAudioFocusHelper != null
                        && mAudioFocusHelper.requestFocus())
            mAudioFocus = AudioFocus.Focused;
    }

    void playSong(int index) {
        if(index > mTrackList.size()-1) return;

        final ParcelableTrack track = mTrackList.get(mCurrentTrack);

        mState = State.Stopped;
        relaxResources(false); // release everything except MediaPlayer

        try {
            // set the source of the media player to a manual URL or path
            createMediaPlayerIfNeeded();
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mPlayer.setDataSource(mTrackList.get(index).getTrack().toString());

            mSongTitle = track.getTrackName();

            mState = State.Preparing;
            setUpAsForeground(mSongTitle + " (loading)");

            // starts preparing the media player in the background. When it's done, it will call
            // our OnPreparedListener (that is, the onPrepared() method on this class, since we set
            // the listener to 'this').
            //
            // Until the media player is prepared, we *cannot* call start() on it!
            mPlayer.prepareAsync();

            // If we are streaming from the internet, we want to hold a Wifi lock, which prevents
            // the Wifi radio from going to sleep while the song is playing. If, on the other hand,
            // we are *not* streaming, we want to release the lock if we were holding it before.
            mWifiLock.acquire();

            mNextTrack = mCurrentTrack + 1;

            startTimer();
        }
        catch (IOException ex) {
            Log.e("MusicService", "IOException playing next song: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    void playNextSong() {
        if (mNextTrack >= mTrackList.size()) return;
        mCurrentTrack = mNextTrack;
        playSong(mCurrentTrack);
    }

    void playPreviousSong() {
        if (mCurrentTrack - 1 < 0) return;
        mCurrentTrack--;
        playSong(mCurrentTrack);
    }

    /** Called when media player is done playing current song. */
    public void onCompletion(MediaPlayer player) {
        // The media player finished playing the current song, so we go ahead and start the next.
        playNextSong();
    }

    /** Called when media player is done preparing. */
    public void onPrepared(MediaPlayer player) {
        // The media player is done preparing. That means we can start playing!
        mState = State.Playing;
        updateNotification(mSongTitle + " (playing)");
        configAndStartMediaPlayer();

        final Intent i = new Intent(STATUS_SERVICE);
        i.putExtra(STATUS_CURRENT_TRACK, mCurrentTrack);
        mBroadcaster.sendBroadcast(i);
    }

    /** Updates the notification. */
    void updateNotification(String text) {
        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
                new Intent(getApplicationContext(), MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
        mNotification.setLatestEventInfo(getApplicationContext(),
                getResources().getString(R.string.app_name), text, pi);
        mNotificationManager.notify(NOTIFICATION_ID, mNotification);
    }

    /**
     * Configures service as a foreground service. A foreground service is a service that's doing
     * something the user is actively aware of (such as playing music), and must appear to the
     * user as a notification. That's why we create the notification here.
     */
    void setUpAsForeground(String text) {
        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
                new Intent(getApplicationContext(), MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
        mNotification = new Notification();
        mNotification.tickerText = text;
        mNotification.icon = R.mipmap.ic_launcher;
        mNotification.flags |= Notification.FLAG_ONGOING_EVENT;
        mNotification.setLatestEventInfo(getApplicationContext(), getString(R.string.app_name),
                text, pi);
        startForeground(NOTIFICATION_ID, mNotification);
    }

    /**
     * Called when there's an error playing media. When this happens, the media player goes to
     * the Error state. We warn the user about the error and reset the media player.
     */
    public boolean onError(MediaPlayer mp, int what, int extra) {
        //Toast.makeText(getApplicationContext(), "Media player error! Resetting.",
        //    Toast.LENGTH_SHORT).show();
        Log.e(LOG_TAG, "Error: what=" + String.valueOf(what) + ", extra=" + String.valueOf(extra));

        mState = State.Stopped;
        relaxResources(true);
        giveUpAudioFocus();
        return true; // true indicates we handled the error
    }

    public void onGainedAudioFocus() {
        //Toast.makeText(getApplicationContext(), "gained audio focus.", Toast.LENGTH_SHORT).show();
        Log.d(LOG_TAG, "gained audio focus.");

        mAudioFocus = AudioFocus.Focused;

        // restart media player with new focus settings
        if (mState == State.Playing)
            configAndStartMediaPlayer();
    }

    public void onLostAudioFocus(boolean canDuck) {
        //Toast.makeText(getApplicationContext(), "lost audio focus." + (canDuck ? "can duck" :
        //    "no duck"), Toast.LENGTH_SHORT).show();
        Log.d(LOG_TAG, "lost audio focus." + (canDuck ? "can duck" : "no duck"));

        mAudioFocus = canDuck ? AudioFocus.NoFocusCanDuck : AudioFocus.NoFocusNoDuck;

        // start/restart/pause media player with new focus settings
        if (mPlayer != null && mPlayer.isPlaying())
            configAndStartMediaPlayer();
    }

   @Override
    public void onDestroy() {
        // Service is being killed, so make sure we release our resources
        mState = State.Stopped;
        relaxResources(true);
        giveUpAudioFocus();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    private class DurationTimerTask extends TimerTask{
        private Intent mPositionIntent = new Intent(STATUS_SERVICE);

        @Override
        public void run() {
            if(mPlayer != null) {
                mPositionIntent.putExtra(STATUS_CURRENT_POSITION, mPlayer.getCurrentPosition());
                mBroadcaster.sendBroadcast(mPositionIntent);
            }
        }
    }

    public void startTimer() {
        mPositionTimer = new Timer();
        mPositionTimerTask = new DurationTimerTask();
        mPositionTimer.schedule(mPositionTimerTask, 0, 1000);
    }

    public void stopTimer() {
        if(mPositionTimer != null)
            mPositionTimer.cancel();
    }
}
