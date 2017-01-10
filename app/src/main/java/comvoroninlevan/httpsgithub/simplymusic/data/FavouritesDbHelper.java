package comvoroninlevan.httpsgithub.simplymusic.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Levan on 24.11.2016.
 */

public class FavouritesDbHelper extends SQLiteOpenHelper {

    public static final String INTEGER_TYPE = " INTEGER";
    public static final String TEXT_TYPE = " TEXT";
    public static final String COMMA_SEP = ",";
    public static final String PRIM_KEY = " PRIMARY KEY";
    public static final String AUTOINCR = " AUTOINCREMENT";

    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + FavouritesContract.FavouritesEntry.TABLE_NAME + "(" +
                    FavouritesContract.FavouritesEntry._ID + INTEGER_TYPE + PRIM_KEY + AUTOINCR +
                    COMMA_SEP + FavouritesContract.FavouritesEntry.SONG_ID + TEXT_TYPE +
                    COMMA_SEP + FavouritesContract.FavouritesEntry.TITLE + TEXT_TYPE +
                    COMMA_SEP + FavouritesContract.FavouritesEntry.ARTISTS + TEXT_TYPE +");";
    public static final int DB_VERSION = 1;
    public static final String DB_NAME = "SimplyMusicFavourites.db";

    public FavouritesDbHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        onCreate(sqLiteDatabase);
    }
}
