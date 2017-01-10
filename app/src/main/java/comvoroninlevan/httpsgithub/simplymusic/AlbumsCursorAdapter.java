package comvoroninlevan.httpsgithub.simplymusic;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Levan on 06.12.2016.
 */

public class AlbumsCursorAdapter extends CursorAdapter {

    public AlbumsCursorAdapter(Context context, Cursor cursor){
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.album_item, viewGroup, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ImageView albumArt = (ImageView)view.findViewById(R.id.albumArt);
        TextView albumName = (TextView)view.findViewById(R.id.albumName);

        int currentArt = cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART);
        int albumTitle = cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM);

        String art = cursor.getString(currentArt);
        Bitmap bitmap = BitmapFactory.decodeFile(art);

        String title = cursor.getString(albumTitle);

        if(bitmap != null) {
            albumArt.setImageBitmap(bitmap);
        }else{
            albumArt.setImageResource(R.drawable.playgold);
        }

        albumName.setLines(1);
        albumName.setText(title);
    }
}
