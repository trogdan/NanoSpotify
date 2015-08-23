package com.trogdan.nanospotify.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * Created by dan on 8/22/15.
 */
public class MusicProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MusicDBHelper mOpenHelper;

    static final int ARTIST = 100;
    static final int ARTIST_IMAGE = 101;
    static final int ARTISTS_QUERY = 102;
    static final int ARTISTS_QUERY_WITH_ARTIST = 103;
    static final int ARTISTS_QUERY_WITH_ARTIST_AND_HEIGHT = 104;
    static final int TRACK = 300;

    private static final SQLiteQueryBuilder sArtistQueryBuilder;

    static{
        sArtistQueryBuilder = new SQLiteQueryBuilder();

        //This is an inner join
        sArtistQueryBuilder.setTables(
                MusicContract.ArtistQueryEntry.TABLE_NAME + " INNER JOIN " +
                        MusicContract.ArtistEntry.TABLE_NAME +
                        " ON " + MusicContract.ArtistQueryEntry.TABLE_NAME +
                        "." + MusicContract.ArtistQueryEntry.COLUMN_ARTIST_KEY +
                        " = " + MusicContract.ArtistEntry.TABLE_NAME +
                        "." + MusicContract.ArtistEntry._ID + " INNER JOIN " +
                        MusicContract.ArtistImageEntry.TABLE_NAME +
                        " ON " + MusicContract.ArtistImageEntry.TABLE_NAME +
                        "." + MusicContract.ArtistImageEntry.COLUMN_ARTIST_KEY +
                        " = " + MusicContract.ArtistEntry.TABLE_NAME +
                        "." + MusicContract.ArtistEntry._ID);
    }

    private static final String sArtistQuerySelection =
            MusicContract.ArtistQueryEntry.TABLE_NAME +
                    "." + MusicContract.ArtistQueryEntry.COLUMN_QUERY + " = ? AND CASE WHEN (" +
                    MusicContract.ArtistImageEntry.TABLE_NAME +
                    "." + MusicContract.ArtistImageEntry.COLUMN_HEIGHT + " - ? ) > 0 THEN ( SELECT MIN ( " +
                    MusicContract.ArtistImageEntry.TABLE_NAME +
                    "." + MusicContract.ArtistImageEntry.COLUMN_HEIGHT + " ) FROM " +
                    MusicContract.ArtistImageEntry.TABLE_NAME + ") ELSE ( SELECT MAX ( " +
                    MusicContract.ArtistImageEntry.TABLE_NAME +
                    "." + MusicContract.ArtistImageEntry.COLUMN_HEIGHT + " ) FROM " +
                    MusicContract.ArtistImageEntry.TABLE_NAME + ") END";


//    private static final String sTrackSelection =
//            MusicContract.TrackEntry.TABLE_NAME +
//                    "." + MusicContract.TrackEntry.COLUMN_ARTIST_KEY + " = ? ";
//
//    private static final String sAlbumSelection =
//            MusicContract.AlbumEntry.TABLE_NAME +
//                    "." + MusicContract.AlbumEntry._ID + " = ? ";

    static UriMatcher buildUriMatcher() {
        // 1) The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case. Add the constructor below.
        UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        // 2) Use the addURI function to match each of the types.  Use the constants from
        // WeatherContract to help define the types to the UriMatcher.
        sURIMatcher.addURI(
                MusicContract.CONTENT_AUTHORITY,
                MusicContract.PATH_ARTISTS + "/*",
                ARTISTS_QUERY_WITH_ARTIST);

        sURIMatcher.addURI(
                MusicContract.CONTENT_AUTHORITY,
                MusicContract.PATH_ARTISTS + "/*/#",
                ARTISTS_QUERY_WITH_ARTIST_AND_HEIGHT);

        sURIMatcher.addURI(
                MusicContract.CONTENT_AUTHORITY,
                MusicContract.PATH_ARTIST_IMAGE,
                ARTIST_IMAGE);

        sURIMatcher.addURI(
                MusicContract.CONTENT_AUTHORITY,
                MusicContract.PATH_ARTIST,
                ARTIST);

        sURIMatcher.addURI(
                MusicContract.CONTENT_AUTHORITY,
                MusicContract.PATH_ARTISTS,
                ARTISTS_QUERY);

        // 3) Return the new matcher!
        return sURIMatcher;
    }

    private Cursor getArtistsByQuerySetting(Uri uri, String[] projection, String sortOrder) {
        String artistSetting = MusicContract.ArtistQueryEntry.getArtistQuerySettingFromUri(uri);
        long imageHeight = MusicContract.ArtistQueryEntry.getImageHeightSettingFromUri(uri);

        String[] selectionArgs;
        String selection;

        selection = sArtistQuerySelection;
        selectionArgs = new String[]{artistSetting, Long.toString(imageHeight)};

        return sArtistQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MusicDBHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case ARTISTS_QUERY_WITH_ARTIST_AND_HEIGHT:
                return MusicContract.ArtistQueryEntry.CONTENT_ITEM_TYPE;
            case ARTISTS_QUERY_WITH_ARTIST:
                return MusicContract.ArtistQueryEntry.CONTENT_ITEM_TYPE;
            case ARTISTS_QUERY:
                return MusicContract.ArtistQueryEntry.CONTENT_TYPE;
            case ARTIST:
                return MusicContract.ArtistEntry.CONTENT_TYPE;
            case ARTIST_IMAGE:
                return MusicContract.ArtistImageEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "artists/*/*"
            case ARTISTS_QUERY_WITH_ARTIST_AND_HEIGHT:
            {
                retCursor = getArtistsByQuerySetting(uri, projection, sortOrder);
                break;
            }
            // "artists/*"
            case ARTISTS_QUERY_WITH_ARTIST:
            {
                retCursor = getArtistsByQuerySetting(uri, projection, sortOrder);
                break;
            }
            // "artists"
            case ARTISTS_QUERY:
            {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MusicContract.ArtistQueryEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "artist_image"
            case ARTIST_IMAGE:
            {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MusicContract.ArtistImageEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "artist"
            case ARTIST:
            {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MusicContract.ArtistEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case ARTIST: {
                long _id = db.insert(MusicContract.ArtistEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = MusicContract.ArtistEntry.buildArtistUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case ARTIST_IMAGE: {
                long _id = db.insert(MusicContract.ArtistImageEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = MusicContract.ArtistImageEntry.buildArtistImageUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case ARTISTS_QUERY: {
                long _id = db.insert(MusicContract.ArtistQueryEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = MusicContract.ArtistQueryEntry.buildArtistQueryUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        switch (match) {
            case ARTIST:
                rowsDeleted = db.delete(
                        MusicContract.ArtistEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ARTIST_IMAGE:
                rowsDeleted = db.delete(
                        MusicContract.ArtistImageEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ARTISTS_QUERY:
                rowsDeleted = db.delete(
                        MusicContract.ArtistQueryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case ARTIST:
                rowsUpdated = db.update(MusicContract.ArtistEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case ARTIST_IMAGE:
                rowsUpdated = db.update(MusicContract.ArtistImageEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case ARTISTS_QUERY:
                rowsUpdated = db.update(MusicContract.ArtistQueryEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            // TODO
//            case WEATHER:
//                db.beginTransaction();
//                int returnCount = 0;
//                try {
//                    for (ContentValues value : values) {
//                        long _id = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, value);
//                        if (_id != -1) {
//                            returnCount++;
//                        }
//                    }
//                    db.setTransactionSuccessful();
//                } finally {
//                    db.endTransaction();
//                }
//                getContext().getContentResolver().notifyChange(uri, null);
//                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
