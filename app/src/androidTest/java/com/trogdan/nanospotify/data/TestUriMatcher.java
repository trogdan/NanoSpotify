package com.trogdan.nanospotify.data;

import android.content.UriMatcher;
import android.net.Uri;
import android.test.AndroidTestCase;

public class TestUriMatcher extends AndroidTestCase {
    public static final String TEST_ARTIST_QUERY = "marlo";
    public static final String TEST_HEIGHT = "200";

    // content://com.trogdan.nanospotify/artist/"
    private static final Uri TEST_ARTIST_QUERY_DIR = MusicContract.ArtistEntry.buildArtistQuery(TEST_ARTIST_QUERY);
    private static final Uri TEST_ARTIST_QUERY_WITH_HEIGHT_DIR = MusicContract.ArtistEntry.buildArtistQueryWithImageHeight(TEST_ARTIST_QUERY, TEST_HEIGHT);

    public void testUriMatcher() {
        UriMatcher testMatcher = MusicProvider.buildUriMatcher();

        assertEquals("Error: The ARTISTS_QUERY_WITH_ARTIST URI was matched incorrectly.",
                testMatcher.match(TEST_ARTIST_QUERY_DIR), MusicProvider.ARTISTS_QUERY_WITH_ARTIST);
        assertEquals("Error: The ARTISTS_QUERY_WITH_ARTIST WITH IMAGE HEIGHT URI was matched incorrectly.",
                testMatcher.match(TEST_ARTIST_QUERY_WITH_HEIGHT_DIR), MusicProvider.ARTISTS_QUERY_WITH_ARTIST);

    }
}
