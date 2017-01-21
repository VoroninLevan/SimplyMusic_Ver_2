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

import com.squareup.picasso.Picasso;

import java.io.File;

/**
 * Created by Levan on 06.12.2016.
 */

public class AlbumsCursorAdapter extends CursorAdapter {

    public AlbumsCursorAdapter(Context context, Cursor cursor){
        super(context, cursor, 0);
    }

    private static class ViewHolder {
        TextView title;
        ImageView albumArt;

        private ViewHolder(View view) {

            title = (TextView) view.findViewById(R.id.albumName);
            albumArt = (ImageView) view.findViewById(R.id.albumArt);
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View view = LayoutInflater.from(context).inflate(R.layout.album_item, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        int currentArt = cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART);
        int albumTitle = cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM);

        String pathToAlbumArt = cursor.getString(currentArt);

        String title = cursor.getString(albumTitle);

        if(pathToAlbumArt != null) {
            Picasso.with(context)
                    .load(new File(pathToAlbumArt))
                    .into(viewHolder.albumArt);
        }else{
            viewHolder.albumArt.setImageResource(R.drawable.placeholder);
        }

        viewHolder.title.setLines(1);
        viewHolder.title.setText(title);
    }
}
