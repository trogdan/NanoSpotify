package com.trogdan.nanospotify.data;

import android.content.UriMatcher;
import android.net.Uri;
import android.test.AndroidTestCase;

public class TestUriMatcher extends AndroidTestCase {
    public static final String TEST_ARTIST_QUERY = "marlo";
    public static final String TEST_HEIGHT = "200";

    // content://com.trogdan.nanospotify/artists/"
    private static final Uri TEST_ARTIST_QUERY_DIR = MusicContract.ArtistEntry.buildArtistQuery(TEST_ARTIST_QUERY);
    private static final Uri TEST_ARTIST_QUERY_WITH_HEIGHT_DIR = MusicContract.ArtistEntry.buildArtistQueryWithImageHeight(TEST_ARTIST_QUERY, TEST_HEIGHT);

    /*
        Students: This function tests that your UriMatcher returns the correct integer value
        for each of the Uri types that our ContentProvider can handle.  Uncomment this when you are
        ready to test your UriMatcher.
     */
    public void testUriMatcher() {
        UriMatcher testMatcher = MusicProvider.buildUriMatcher();

        assertEquals("Error: The ARTISTS_QUERY_WITH_ARTIST URI was matched incorrectly.",
                testMatcher.match(TEST_ARTIST_QUERY_DIR), MusicProvider.ARTISTS_QUERY_WITH_ARTIST);
        assertEquals("Error: The ARTISTS_QUERY_WITH_ARTIST WITH IMAGE HEIGHT URI was matched incorrectly.",
                testMatcher.match(TEST_ARTIST_QUERY_WITH_HEIGHT_DIR), MusicProvider.ARTISTS_QUERY_WITH_ARTIST);

    }
}
