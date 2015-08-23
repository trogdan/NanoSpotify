package com.trogdan.nanospotify.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;

public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    // Since we want each test to start with a clean slate
    void deleteTheDatabase() {
        mContext.deleteDatabase(MusicDBHelper.DATABASE_NAME);
    }

    /*
        This function gets called before each test is executed to delete the database.  This makes
        sure that we always have a clean test.
     */
    public void setUp() {
        deleteTheDatabase();
    }

    /*
        Note that this only tests that the Artist table has the correct columns
     */
    public void testCreateDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(MusicContract.ArtistEntry.TABLE_NAME);
        tableNameHashSet.add(MusicContract.ArtistImageEntry.TABLE_NAME);
        tableNameHashSet.add(MusicContract.ArtistQueryEntry.TABLE_NAME);

        mContext.deleteDatabase(MusicDBHelper.DATABASE_NAME);
        SQLiteDatabase db = new MusicDBHelper(this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext() );

        // if this fails, it means that your database doesn't contain the 3 tables
        assertTrue("Error: Your database was created without both the artist, artistImage and " +
                        "artistQuery tables",
                tableNameHashSet.isEmpty());

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + MusicContract.ArtistEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> locationColumnHashSet = new HashSet<String>();
        locationColumnHashSet.add(MusicContract.ArtistEntry._ID);
        locationColumnHashSet.add(MusicContract.ArtistEntry.COLUMN_API_ID);
        locationColumnHashSet.add(MusicContract.ArtistEntry.COLUMN_DATE);
        locationColumnHashSet.add(MusicContract.ArtistEntry.COLUMN_NAME);


        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            locationColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required location
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required artist entry columns",
                locationColumnHashSet.isEmpty());
        db.close();
    }

    /*
        Students:  Here is where you will build code to test that we can insert and query the
        location database.  We've done a lot of work for you.  You'll want to look in TestUtilities
        where you can uncomment out the "createNorthPoleLocationValues" function.  You can
        also make use of the ValidateCurrentRecord function from within TestUtilities.
    */
    public void testArtistTable() {
        insertArtist();
    }

    public void testArtistImageTable() {
        // First insert the artist, and then use the locationRowId to insert
        // the image. Make sure to cover as many failure cases as you can.

        long artistRowId = insertArtist();

        // Make sure we have a valid row ID.
        assertFalse("Error: Location Not Inserted Correctly", artistRowId == -1L);

        // First step: Get reference to writable database
        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        MusicDBHelper dbHelper = new MusicDBHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Second Step (artist image): Create image values
        ContentValues imageValues = TestUtilities.createArtistImage(artistRowId);

        // Third Step (artist image): Insert ContentValues into database and get a row ID back
        long imageRowId = db.insert(MusicContract.ArtistImageEntry.TABLE_NAME, null, imageValues);
        assertTrue(imageRowId != -1);

        // Fourth Step: Query the database and receive a Cursor back
        // A cursor is your primary interface to the query results.
        Cursor imageCursor = db.query(
                MusicContract.ArtistImageEntry.TABLE_NAME,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );

        // Move the cursor to the first valid database row and check to see if we have any rows
        assertTrue("Error: No Records returned from image query", imageCursor.moveToFirst());

        // Fifth Step: Validate the image query
        TestUtilities.validateCurrentRecord("testInsertReadDb artistImageEntry failed to validate",
                imageCursor, imageValues);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse("Error: More than one record returned from image query",
                imageCursor.moveToNext());

        // Sixth Step: Close cursor and database
        imageCursor.close();
        dbHelper.close();
    }

    public void testArtistQueryTable() {
        // First insert the artist, and then use the locationRowId to insert
        // the image. Make sure to cover as many failure cases as you can.

        long artistRowId = insertArtist();

        // Make sure we have a valid row ID.
        assertFalse("Error: Location Not Inserted Correctly", artistRowId == -1L);

        // First step: Get reference to writable database
        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        MusicDBHelper dbHelper = new MusicDBHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Second Step (artist query): Create query values
        ContentValues queryValues = TestUtilities.createQueryValues(artistRowId);

        // Third Step (artist query): Insert ContentValues into database and get a row ID back
        long queryRowId = db.insert(MusicContract.ArtistQueryEntry.TABLE_NAME, null, queryValues);
        assertTrue(queryRowId != -1);

        // Fourth Step: Query the database and receive a Cursor back
        // A cursor is your primary interface to the query results.
        Cursor queryCursor = db.query(
                MusicContract.ArtistQueryEntry.TABLE_NAME,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );

        // Move the cursor to the first valid database row and check to see if we have any rows
        assertTrue("Error: No Records returned from query query", queryCursor.moveToFirst());

        // Fifth Step: Validate the query query
        TestUtilities.validateCurrentRecord("testInsertReadDb artistQueryEntry failed to validate",
                queryCursor, queryValues);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse("Error: More than one record returned from query query",
                queryCursor.moveToNext());

        // Sixth Step: Close cursor and database
        queryCursor.close();
        dbHelper.close();
    }

    public long insertArtist() {
        // First step: Get reference to writable database
        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        MusicDBHelper dbHelper = new MusicDBHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Second Step: Create ContentValues of what you want to insert
        // (you can use the createNorthPoleLocationValues if you wish)
        ContentValues testValues = TestUtilities.createArtistValues();

        // Third Step: Insert ContentValues into database and get a row ID back
        long artistRowId;
        artistRowId = db.insert(MusicContract.ArtistEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue(artistRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // Fourth Step: Query the database and receive a Cursor back
        // A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
                MusicContract.ArtistEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        // Move the cursor to a valid database row and check to see if we got any records back
        // from the query
        assertTrue( "Error: No Records returned from artist query", cursor.moveToFirst() );

        // Fifth Step: Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)
        TestUtilities.validateCurrentRecord("Error: Artist Query Validation Failed",
                cursor, testValues);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse( "Error: More than one record returned from artist query",
                cursor.moveToNext() );

        // Sixth Step: Close Cursor and Database
        cursor.close();
        db.close();
        return artistRowId;
    }
}
