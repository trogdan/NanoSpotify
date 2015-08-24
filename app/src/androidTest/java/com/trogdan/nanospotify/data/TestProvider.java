package com.trogdan.nanospotify.data;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

import com.trogdan.nanospotify.data.MusicContract.ArtistEntry;
import com.trogdan.nanospotify.data.MusicContract.ArtistImageEntry;


/**
 * Created by dan on 8/23/15.
 */
public class TestProvider extends AndroidTestCase{

    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    /*
      This helper function deletes all records from both database tables using the ContentProvider.
      It also queries the ContentProvider to make sure that the database has been successfully
      deleted, so it cannot be used until the Query and Delete functions have been written
      in the ContentProvider.
    */
    public void deleteAllRecordsFromProvider() {
        mContext.getContentResolver().delete(
                ArtistEntry.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                ArtistImageEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                ArtistEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from artist table during delete", 0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                ArtistImageEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from artist IMAGE table during delete", 0, cursor.getCount());
        cursor.close();

    }

    public void deleteAllRecords() {
        deleteAllRecordsFromProvider();
    }

    // Since we want each test to start with a clean slate, run deleteAllRecords
    // in setUp (called by the test runner before each test).
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecords();
    }

    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        // We define the component name based on the package name from the context and the
        // MusicProvider class.
        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                MusicProvider.class.getName());
        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertEquals("Error: MusicContract registered with authority: " + providerInfo.authority +
                            " instead of authority: " + MusicContract.CONTENT_AUTHORITY,
                    providerInfo.authority, MusicContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            // I guess the provider isn't registered correctly.
            assertTrue("Error: MusicContract not registered at " + mContext.getPackageName(),
                    false);
        }
    }

    public void testGetType() {
        // content://com.trogdan.nanospotify/artist/
        String type = mContext.getContentResolver().getType(ArtistEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.trogdan.nanospotify/artist/
        assertEquals("Error: the ArtistEntry CONTENT_URI should return ArtistEntry.CONTENT_TYPE",
                ArtistEntry.CONTENT_TYPE, type);

        String artistQuery = "marlo";
        // content://com.trogdan.nanospotify/artist/marlo
        type = mContext.getContentResolver().getType(
                ArtistEntry.buildArtistQuery(artistQuery));
        // vnd.android.cursor.dir/com.trogdan.nanospotify/artists/marlo
        assertEquals("Error: the ArtistEntry CONTENT_URI with query should return ArtistEntry.CONTENT_ITEM_TYPE",
                ArtistEntry.CONTENT_ITEM_TYPE, type);

        String testHeight = "300";
        // content://com.trogdan.nanospotify/artist/marlo/300
        type = mContext.getContentResolver().getType(
                ArtistEntry.buildArtistQueryWithImageHeight(artistQuery, testHeight));
        // vnd.android.cursor.item/com.trogdan.nanospotify/artists/marlo/300
        assertEquals("Error: the ArtistEntry CONTENT_URI with location and date should return ArtistEntry.CONTENT_ITEM_TYPE",
                ArtistEntry.CONTENT_ITEM_TYPE, type);

        // content://com.trogdan.nanospotify/artist_image/
        type = mContext.getContentResolver().getType(ArtistImageEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.trogdan.nanospotify/artist_image
        assertEquals("Error: the ArtistImageEntry CONTENT_URI should return ArtistImageEntry.CONTENT_TYPE",
                ArtistImageEntry.CONTENT_TYPE, type);
    }

    public void testArtistQuery() {
        // insert our test records into the database
        MusicDBHelper dbHelper = new MusicDBHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = TestUtilities.createArtistValues();
        long artistRowId = TestUtilities.insertArtistValues(mContext);

        ContentValues imageValues = TestUtilities.createArtistImage(artistRowId);

        long imageRowId = db.insert(ArtistImageEntry.TABLE_NAME, null, imageValues);
        assertTrue("Unable to Insert ArtistImageEntry into the Database", imageRowId != -1);

        db.close();

        // Test the basic content provider query
        Cursor queryCursor = mContext.getContentResolver().query(
                ArtistEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testArtistsQuery", queryCursor, testValues);
    }

    public void testUpdateArtistQuery() {
        // Create a new map of values, where column names are the keys
        ContentValues values = TestUtilities.createArtistValues();

        Uri artistUri = mContext.getContentResolver().
                insert(ArtistEntry.CONTENT_URI, values);
        long artistRowId = ContentUris.parseId(artistUri);

        // Verify we got a row back.
        assertTrue(artistRowId != -1);
        Log.d(LOG_TAG, "New row id: " + artistRowId);

        ContentValues updatedValues = new ContentValues(values);
        updatedValues.put(ArtistEntry._ID, artistRowId);
        updatedValues.put(ArtistEntry.COLUMN_DATE, System.currentTimeMillis());

        // Create a cursor with observer to make sure that the content provider is notifying
        // the observers as expected
        Cursor artistCursor = mContext.getContentResolver().query(ArtistEntry.CONTENT_URI, null, null, null, null);

        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        artistCursor.registerContentObserver(tco);

        int count = mContext.getContentResolver().update(
                ArtistEntry.CONTENT_URI, updatedValues, ArtistEntry._ID + "= ?",
                new String[] { Long.toString(artistRowId)});
        assertEquals(count, 1);

        // Test to make sure our observer is called.  If not, we throw an assertion.
        //
        // Students: If your code is failing here, it means that your content provider
        // isn't calling getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();

        artistCursor.unregisterContentObserver(tco);
        artistCursor.close();

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                ArtistEntry.CONTENT_URI,
                null,   // projection
                ArtistEntry._ID + " = " + artistRowId,
                null,   // Values for the "where" clause
                null    // sort order
        );

        TestUtilities.validateCursor("testUpdateArtistLocation.  Error validating artist entry update.",
                cursor, updatedValues);

        cursor.close();
    }

    public void testInsertReadProvider() {
        ContentValues testValues = TestUtilities.createArtistValues();

        // Register a content observer for our insert.  This time, directly with the content resolver
        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(ArtistEntry.CONTENT_URI, true, tco);
        Uri artistUri = mContext.getContentResolver().insert(ArtistEntry.CONTENT_URI, testValues);

        // Did our content observer get called?   If this fails, your insert location
        // isn't calling getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        long artistRowId = ContentUris.parseId(artistUri);

        // Verify we got a row back.
        assertTrue(artistRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                ArtistEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating ArtistEntry.",
                cursor, testValues);

        // Fantastic.  Now that we have a artist, add some image!
        ContentValues imageValues = TestUtilities.createArtistImage(artistRowId);
        // The TestContentObserver is a one-shot class
        tco = TestUtilities.getTestContentObserver();

        mContext.getContentResolver().registerContentObserver(ArtistImageEntry.CONTENT_URI, true, tco);

        Uri imageInsertUri = mContext.getContentResolver()
                .insert(ArtistImageEntry.CONTENT_URI, imageValues);
        assertTrue(imageInsertUri != null);

        // Did our content observer get called?  If this fails, your insert image
        // in your ContentProvider isn't calling
        // getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        // A cursor is your primary interface to the query results.
        Cursor imageCursor = mContext.getContentResolver().query(
                ArtistImageEntry.CONTENT_URI,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // columns to group by
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating ArtistImageEntry insert.",
                imageCursor, imageValues);


        // A cursor is your primary interface to the query results.
        Cursor queryCursor = mContext.getContentResolver().query(
                ArtistEntry.CONTENT_URI,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // columns to group by
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating ArtistEntry query.",
                queryCursor, testValues);
        // Add the image and artist  values in with the query data so that we can make
        // sure that the join worked and we actually get all the values back
        testValues.putAll(imageValues);

        // Get the joined data
        queryCursor = mContext.getContentResolver().query(
                ArtistEntry.buildArtistQueryWithImageHeight(TestUriMatcher.TEST_ARTIST_QUERY, TestUriMatcher.TEST_HEIGHT),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        TestUtilities.validateCursor("testInsertReadProvider.  Error validating joined Data.",
                queryCursor, testValues);

        queryCursor = mContext.getContentResolver().query(
                ArtistEntry.buildArtistQuery(TestUriMatcher.TEST_ARTIST_QUERY),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        TestUtilities.validateCursor("testInsertReadProvider.  Error validating joined Data.",
                queryCursor, testValues);
    }
}
