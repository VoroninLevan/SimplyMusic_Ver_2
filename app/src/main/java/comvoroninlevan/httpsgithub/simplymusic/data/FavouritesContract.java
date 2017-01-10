package comvoroninlevan.httpsgithub.simplymusic.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Levan on 24.11.2016.
 */

public final class FavouritesContract {

    public static final String CONTENT_AUTHORITY = "comvoroninlevan.httpsgithub.simplymusic";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_FAVOURITES = "favourites";

    public static final class FavouritesEntry implements BaseColumns{

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_FAVOURITES);

        //Table Name
        public static final String TABLE_NAME = "favourites";

        //Columns
        public static final String _ID = BaseColumns._ID;
        public static final String SONG_ID = "song_id";
        public static final String ARTISTS = "artists";
        public static final String TITLE = "title";

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVOURITES;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVOURITES;
    }
}
