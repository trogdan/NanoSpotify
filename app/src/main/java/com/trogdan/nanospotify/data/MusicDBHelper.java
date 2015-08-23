package com.trogdan.nanospotify.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.trogdan.nanospotify.data.MusicContract.ArtistEntry;
import com.trogdan.nanospotify.data.MusicContract.ArtistImageEntry;
import com.trogdan.nanospotify.data.MusicContract.ArtistQueryEntry;
import com.trogdan.nanospotify.data.MusicContract.AlbumEntry;
import com.trogdan.nanospotify.data.MusicContract.AlbumImageEntry;
import com.trogdan.nanospotify.data.MusicContract.TrackEntry;

import kaaes.spotify.webapi.android.models.Artists;

/**
 * Created by dan on 8/22/15.
 */
public class MusicDBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "music.db";

    public MusicDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_ARTIST_TABLE = "CREATE TABLE " + ArtistEntry.TABLE_NAME + " (" +
                // Why AutoIncrement here, and not above?
                // Unique keys will be auto-generated in either case.
                ArtistEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                ArtistEntry.COLUMN_DATE + " INTEGER NOT NULL, " +
                ArtistEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                ArtistEntry.COLUMN_API_ID + " TEXT NOT NULL, " +

                // To assure the application have just one artist entry per day
                // let's created a UNIQUE constraint with REPLACE strategy
                " UNIQUE (" + ArtistEntry.COLUMN_DATE + ", " +
                ArtistEntry.COLUMN_API_ID + ") ON CONFLICT REPLACE);";

        sqLiteDatabase.execSQL(SQL_CREATE_ARTIST_TABLE);

        final String SQL_CREATE_ARTIST_IMAGE_TABLE = "CREATE TABLE " + ArtistImageEntry.TABLE_NAME + " (" +
                ArtistImageEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                // the ID of the location entry associated with this artist data
                ArtistImageEntry.COLUMN_ARTIST_KEY + " INTEGER NOT NULL, " +

                ArtistImageEntry.COLUMN_DATE + " INTEGER NOT NULL, " +
                ArtistImageEntry.COLUMN_URI + " TEXT NOT NULL, " +
                ArtistImageEntry.COLUMN_WIDTH + " INTEGER NOT NULL, " +
                ArtistImageEntry.COLUMN_HEIGHT + " INTEGER NOT NULL, " +

                // To assure the application have just one artist image entry per day
                // let's created a UNIQUE constraint with REPLACE strategy
                " UNIQUE (" + ArtistImageEntry.COLUMN_DATE + ", " +
                ArtistImageEntry.COLUMN_ARTIST_KEY + ", " +
                ArtistImageEntry.COLUMN_WIDTH + ", " +
                ArtistImageEntry.COLUMN_HEIGHT + ") ON CONFLICT REPLACE);";

        sqLiteDatabase.execSQL(SQL_CREATE_ARTIST_IMAGE_TABLE);

        final String SQL_CREATE_ARTIST_QUERY_TABLE = "CREATE TABLE " + ArtistQueryEntry.TABLE_NAME + " (" +
                ArtistQueryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                // the ID of the location entry associated with this query data
                ArtistQueryEntry.COLUMN_ARTIST_KEY + " INTEGER NOT NULL, " +

                ArtistQueryEntry.COLUMN_DATE + " INTEGER NOT NULL, " +
                ArtistQueryEntry.COLUMN_QUERY + " TEXT NOT NULL, " +

                // To assure the application have just one artist query entry per day
                // let's created a UNIQUE constraint with REPLACE strategy
                // TODO I doubt spotify is case-sensitive, but double-check
                " UNIQUE (" + ArtistQueryEntry.COLUMN_DATE + ", " +
                ArtistQueryEntry.COLUMN_QUERY +  ") ON CONFLICT REPLACE);";

        sqLiteDatabase.execSQL(SQL_CREATE_ARTIST_QUERY_TABLE);

        final String SQL_CREATE_ALBUM_TABLE = "CREATE TABLE " + AlbumEntry.TABLE_NAME + " (" +
                // Why AutoIncrement here, and not above?
                // Unique keys will be auto-generated in either case.
                AlbumEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                AlbumEntry.COLUMN_DATE + " INTEGER NOT NULL, " +
                AlbumEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                AlbumEntry.COLUMN_API_ID + " TEXT NOT NULL, " +

                // To assure the application have just one album entry per day
                // let's created a UNIQUE constraint with REPLACE strategy
                " UNIQUE (" + AlbumEntry.COLUMN_DATE + ", " +
                AlbumEntry.COLUMN_API_ID + ") ON CONFLICT REPLACE);";

        sqLiteDatabase.execSQL(SQL_CREATE_ALBUM_TABLE);

        final String SQL_CREATE_ALBUM_IMAGE_TABLE = "CREATE TABLE " + AlbumImageEntry.TABLE_NAME + " (" +
                AlbumImageEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                // the ID of the location entry associated with this album data
                AlbumImageEntry.COLUMN_ALBUM_KEY + " INTEGER NOT NULL, " +

                AlbumImageEntry.COLUMN_DATE + " INTEGER NOT NULL, " +
                AlbumImageEntry.COLUMN_URI + " TEXT NOT NULL, " +
                AlbumImageEntry.COLUMN_WIDTH + " INTEGER NOT NULL, " +
                AlbumImageEntry.COLUMN_HEIGHT + " INTEGER NOT NULL, " +

                // To assure the application have just one album image entry per day
                // let's created a UNIQUE constraint with REPLACE strategy
                " UNIQUE (" + AlbumImageEntry.COLUMN_DATE + ", " +
                AlbumImageEntry.COLUMN_ALBUM_KEY + ", " +
                AlbumImageEntry.COLUMN_WIDTH + ", " +
                AlbumImageEntry.COLUMN_HEIGHT + ") ON CONFLICT REPLACE);";

        sqLiteDatabase.execSQL(SQL_CREATE_ALBUM_IMAGE_TABLE);

        final String SQL_CREATE_TRACK_QUERY_TABLE = "CREATE TABLE " + TrackEntry.TABLE_NAME + " (" +
                TrackEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                // the ID of the artist entry associated with this track data
                TrackEntry.COLUMN_ARTIST_KEY + " INTEGER NOT NULL, " +

                // the ID of the album entry associated with this track data
                TrackEntry.COLUMN_ALBUM_KEY + " INTEGER NOT NULL, " +

                TrackEntry.COLUMN_DATE + " INTEGER NOT NULL, " +
                TrackEntry.COLUMN_API_ID + " TEXT NOT NULL, " +
                TrackEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                TrackEntry.COLUMN_SAMPLE_URI + " TEXT NOT NULL, " +
                TrackEntry.COLUMN_SAMPLE_DURATION + " INTEGER NOT NULL, " +

                // To assure the application have just one artist query entry per day
                // let's created a UNIQUE constraint with REPLACE strategy
                // TODO constraint to make sure no more than 10 tracks per artist
                // for now, grab the 10 latest during a query
                " UNIQUE (" + TrackEntry.COLUMN_DATE + ", " +
                TrackEntry.COLUMN_ARTIST_KEY + ", " +
                TrackEntry.COLUMN_API_ID + ") ON CONFLICT REPLACE);";

        sqLiteDatabase.execSQL(SQL_CREATE_TRACK_QUERY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ArtistEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ArtistImageEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ArtistQueryEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + AlbumEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + AlbumImageEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TrackEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
