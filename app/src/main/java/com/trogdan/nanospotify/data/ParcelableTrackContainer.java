package com.trogdan.nanospotify.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by dan on 8/16/15.
 */
public class ParcelableTrackContainer implements Parcelable {
    private ArrayList<ParcelableTrack> tracks;

    public ArrayList<ParcelableTrack> getTracks() {
        return tracks;
    }

    public ParcelableTrackContainer(ArrayList<ParcelableTrack> tracks) {
        this.tracks = tracks;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(tracks);
    }

    protected ParcelableTrackContainer(Parcel in) {
        this.tracks = in.createTypedArrayList(ParcelableTrack.CREATOR);
    }

    public static final Parcelable.Creator<ParcelableTrackContainer> CREATOR = new Parcelable.Creator<ParcelableTrackContainer>() {
        public ParcelableTrackContainer createFromParcel(Parcel source) {
            return new ParcelableTrackContainer(source);
        }

        public ParcelableTrackContainer[] newArray(int size) {
            return new ParcelableTrackContainer[size];
        }
    };
}
