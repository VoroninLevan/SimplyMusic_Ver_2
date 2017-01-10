package comvoroninlevan.httpsgithub.simplymusic.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * Created by Levan on 09.12.2016.
 */

public class FavouritesProvider extends ContentProvider{

    private FavouritesDbHelper favouritesDbHelper;

    private static final int FAVOURITES = 100;
    private static final int FAVOURITES_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static{
        sUriMatcher.addURI(FavouritesContract.CONTENT_AUTHORITY, FavouritesContract.PATH_FAVOURITES, FAVOURITES);
        sUriMatcher.addURI(FavouritesContract.CONTENT_AUTHORITY, FavouritesContract.PATH_FAVOURITES + "/#", FAVOURITES_ID);
    }

    @Override
    public boolean onCreate() {
        favouritesDbHelper = new FavouritesDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        SQLiteDatabase database = favouritesDbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch(match){
            case FAVOURITES:
                cursor = database.query(FavouritesContract.FavouritesEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            case FAVOURITES_ID:
                selection = FavouritesContract.FavouritesEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(FavouritesContract.FavouritesEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown Uri " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case FAVOURITES:
                return insertSong(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }

    private Uri insertSong(Uri uri, ContentValues contentValues){

        SQLiteDatabase database = favouritesDbHelper.getWritableDatabase();

        long id = database.insert(FavouritesContract.FavouritesEntry.TABLE_NAME, null, contentValues);
        if(id == -1){
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = favouritesDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        switch (match){
            case FAVOURITES:
                rowsDeleted = database.delete(FavouritesContract.FavouritesEntry.TABLE_NAME,
                        selection, selectionArgs);
                break;
            case FAVOURITES_ID:
                selection = FavouritesContract.FavouritesEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(FavouritesContract.FavouritesEntry.TABLE_NAME,
                        selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        if(rowsDeleted != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch(match){
            case FAVOURITES:
                return FavouritesContract.FavouritesEntry.CONTENT_LIST_TYPE;
            case FAVOURITES_ID:
                return FavouritesContract.FavouritesEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
