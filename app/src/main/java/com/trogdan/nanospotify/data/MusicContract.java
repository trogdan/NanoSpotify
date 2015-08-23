package com.trogdan.nanospotify.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by dan on 8/22/15.
 */
public class MusicContract {

    public static final String CONTENT_AUTHORITY = "com.trogdan.nanospotify";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_ARTISTS = "artists";
    public static final String PATH_ARTIST = "artist";
    public static final String PATH_ARTIST_IMAGE = "artist_image";
    public static final String PATH_TRACKS = "tracks";
    public static final String PATH_ALBUM = "album";

    public static final class ArtistEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ARTIST).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ARTIST;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ARTIST;

        public static final String TABLE_NAME = "artist";

        public static final String COLUMN_NAME = "name";
        // id of the artist as returned by api
        public static final String COLUMN_API_ID = "api_id";
        // date entered into table as second since the epoch
        public static final String COLUMN_DATE = "date";

        public static Uri buildArtistUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    // Keep around image entries, to allow finding the proper size image for configuration changes
    public static final class ArtistImageEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ARTIST_IMAGE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ARTIST_IMAGE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ARTIST_IMAGE;

        public static final String TABLE_NAME = "artist_image";

        // lookup into artist table
        public static final String COLUMN_ARTIST_KEY = "artist_id";

        public static final String COLUMN_URI = "uri";

        // date entered into table as second since the epoch
        public static final String COLUMN_DATE = "date";

        public static final String COLUMN_WIDTH = "width";
        public static final String COLUMN_HEIGHT = "height";

        public static Uri buildArtistImageUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class ArtistQueryEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ARTISTS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ARTISTS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ARTISTS;

        public static final String TABLE_NAME = "artist_query";

        // lookup into artist table
        public static final String COLUMN_ARTIST_KEY = "artist_id";

        // the query term entered by the user
        public static final String COLUMN_QUERY= "query";

        // date entered into table as second since the epoch
        public static final String COLUMN_DATE = "date";

        // the uri provide for the artist pager, not sure if we can use this to optimize later
        //public static final String COLUMN_URI = "uri";

        public static Uri buildArtistQueryUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
        public static Uri buildArtistQuery(String query) {
            return CONTENT_URI.buildUpon().appendPath(query).build();
        }

        public static Uri buildArtistQueryWithImageHeight(String query, String height) {
            return CONTENT_URI.buildUpon().appendPath(query)
                    .appendQueryParameter(ArtistImageEntry.COLUMN_HEIGHT, height).build();
        }

        public static String getArtistQuerySettingFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static long getImageHeightSettingFromUri(Uri uri) {
            if (uri.getPathSegments().size() > 2)
            {
                Long.parseLong(uri.getPathSegments().get(2));
            }
            return 0;
        }
    }

    public static final class AlbumEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ALBUM).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ALBUM;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ALBUM;

        public static final String TABLE_NAME = "album";

        public static final String COLUMN_NAME = "name";

        // id of the artist as returned by api
        public static final String COLUMN_API_ID = "api_id";
        // date entered into table as second since the epoch
        public static final String COLUMN_DATE = "date";
    }

    // Keep around image entries, to allow finding the proper size image for configuration changes
    public static final class AlbumImageEntry implements BaseColumns {
        public static final String TABLE_NAME = "album_image";

        // lookup into album table
        public static final String COLUMN_ALBUM_KEY = "album_id";

        public static final String COLUMN_URI = "uri";

        // date entered into table as second since the epoch
        public static final String COLUMN_DATE = "date";

        public static final String COLUMN_WIDTH = "width";
        public static final String COLUMN_HEIGHT = "height";
    }

    public static final class TrackEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRACKS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRACKS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRACKS;

        public static final String TABLE_NAME = "track";

        // lookup into artist table
        public static final String COLUMN_ARTIST_KEY = "artist_id";

        // lookup into album table
        public static final String COLUMN_ALBUM_KEY = "album_id";

        public static final String COLUMN_NAME = "name";

        // id of the artist as returned by api, shouldn't need this
        public static final String COLUMN_API_ID = "api_id";

        // date entered into table as second since the epoch
        public static final String COLUMN_DATE = "date";

        public static final String COLUMN_SAMPLE_URI = "sample_uri";
        public static final String COLUMN_SAMPLE_DURATION = "sample_duration";
    }
}
