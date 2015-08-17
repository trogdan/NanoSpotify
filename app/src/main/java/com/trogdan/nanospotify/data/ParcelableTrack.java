package com.trogdan.nanospotify.data;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by dan on 8/16/15.
 */
public class ParcelableTrack implements Parcelable {

    private String artistName;
    private String albumName;
    private Uri albumArt;
    private String trackName;
    private Uri track;
    private long duration;

    public String getArtistName() {
        return artistName;
    }

    public String getAlbumName() {
        return albumName;
    }

    public Uri getAlbumArt() {
        return albumArt;
    }

    public String getTrackName() {
        return trackName;
    }

    public Uri getTrack() {
        return track;
    }

    public long getDuration() {
        return duration;
    }


    public ParcelableTrack(String artistName, String albumName, Uri albumArt, String trackName, Uri track, long duration) {
        this.artistName = artistName;
        this.albumName = albumName;
        this.albumArt = albumArt;
        this.trackName = trackName;
        this.track = track;
        this.duration = duration;
    }

    public ParcelableTrack(Track track) {
        this.albumName = track.album.name;
        // TODO pick an appropriate sized image
        this.albumArt = Uri.parse(track.album.images.size() > 0 ? track.album.images.get(0).url : null);
        this.trackName = track.name;
        this.track = Uri.parse(track.preview_url);
        this.duration = track.duration_ms;

        // For now, just concantenate artists with a ','
        this.artistName = "";
        for(int j = 0; j < track.artists.size(); j++) {
            if(j > 0)
                this.artistName += ", ";
            this.artistName += track.artists.get(j).name;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.artistName);
        dest.writeString(this.albumName);
        dest.writeParcelable(this.albumArt, 0);
        dest.writeString(this.trackName);
        dest.writeParcelable(this.track, 0);
        dest.writeLong(this.duration);
    }

    protected ParcelableTrack(Parcel in) {
        this.artistName = in.readString();
        this.albumName = in.readString();
        this.albumArt = in.readParcelable(Uri.class.getClassLoader());
        this.trackName = in.readString();
        this.track = in.readParcelable(Uri.class.getClassLoader());
        this.duration = in.readLong();
    }

    public static final Parcelable.Creator<ParcelableTrack> CREATOR = new Parcelable.Creator<ParcelableTrack>() {
        public ParcelableTrack createFromParcel(Parcel source) {
            return new ParcelableTrack(source);
        }

        public ParcelableTrack[] newArray(int size) {
            return new ParcelableTrack[size];
        }
    };
}