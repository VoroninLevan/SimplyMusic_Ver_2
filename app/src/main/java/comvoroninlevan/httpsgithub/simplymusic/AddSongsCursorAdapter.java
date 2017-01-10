package comvoroninlevan.httpsgithub.simplymusic;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 * Created by Levan on 07.12.2016.
 */

public class AddSongsCursorAdapter extends CursorAdapter {

    public AddSongsCursorAdapter(Context context, Cursor cursor){
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.add_item, viewGroup, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView title = (TextView)view.findViewById(R.id.addTitle);
        TextView artist = (TextView)view.findViewById(R.id.addArtist);

        int songTitle = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
        int songArtist = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);

        String currentTitle = cursor.getString(songTitle);
        String currentArtist = cursor.getString(songArtist);

        title.setText(currentTitle);
        artist.setText(currentArtist);
    }
}
