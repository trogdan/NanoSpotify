package com.trogdan.nanospotify.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import com.trogdan.nanospotify.utils.PollingCheck;

import java.util.Map;
import java.util.Set;

/*
    Students: These are functions and some test data to make it easier to test your database and
    Content Provider.  Note that you'll want your WeatherContract class to exactly match the one
    in our solution to use these as-given.
 */
public class TestUtilities extends AndroidTestCase {
    static final String TEST_QUERY = "marlo";
    static final long TEST_HEIGHT = 400;
    static final long TEST_DATE = 1419033600L;  // December 20th, 2014

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }


    static ContentValues createArtistValues() {
        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();
        testValues.put(MusicContract.ArtistEntry.COLUMN_NAME, "MarLo");
        testValues.put(MusicContract.ArtistEntry.COLUMN_API_ID, "ABCD1234EF56");
        testValues.put(MusicContract.ArtistEntry.COLUMN_DATE, System.currentTimeMillis() / 1000);

        return testValues;
    }

    static ContentValues createArtistImage(long artistRowId) {
        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();
        testValues.put(MusicContract.ArtistImageEntry.COLUMN_ARTIST_KEY, artistRowId);
        testValues.put(MusicContract.ArtistImageEntry.COLUMN_URI, "http://TODO");
        testValues.put(MusicContract.ArtistImageEntry.COLUMN_HEIGHT, TEST_HEIGHT);
        testValues.put(MusicContract.ArtistImageEntry.COLUMN_WIDTH, TEST_HEIGHT);
        testValues.put(MusicContract.ArtistEntry.COLUMN_DATE, System.currentTimeMillis() / 1000);

        return testValues;
    }

    static ContentValues createQueryValues(long artistRowId) {
        ContentValues testValues = new ContentValues();
        testValues.put(MusicContract.ArtistQueryEntry.COLUMN_ARTIST_KEY, artistRowId);
        testValues.put(MusicContract.ArtistQueryEntry.COLUMN_DATE, TEST_DATE);
        testValues.put(MusicContract.ArtistQueryEntry.COLUMN_QUERY, "marlo");

        return testValues;
    }

    static long insertArtistValues(Context context) {
        // insert our test records into the database
        MusicDBHelper dbHelper = new MusicDBHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues testValues = TestUtilities.createArtistValues();

        long locationRowId;
        locationRowId = db.insert(MusicContract.ArtistEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue("Error: Failure to insert artist Values", locationRowId != -1);

        return locationRowId;
    }

    /*
        The functions we provide inside of TestProvider use this utility class to test
        the ContentObserver callbacks using the PollingCheck class that we grabbed from the Android
        CTS tests.

        Note that this only tests that the onChange function is called; it does not test that the
        correct Uri is returned.
     */
    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }
}
