package comvoroninlevan.httpsgithub.simplymusic;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Levan on 06.12.2016.
 */

public class AlbumSongsCursorAdapter extends CursorAdapter {

    public AlbumSongsCursorAdapter(Context context, Cursor cursor){
        super(context, cursor, 0);
    }

    private static class ViewHolder{
        CheckBox checkBox;
        TextView title;
        TextView artist;
        ImageView imageView;

        private ViewHolder(View view){

            checkBox = (CheckBox)view.findViewById(R.id.favouritesCheckBox);
            title = (TextView)view.findViewById(R.id.title);
            artist = (TextView)view.findViewById(R.id.artist);
            imageView = (ImageView)view.findViewById(R.id.albumArt);
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View view = LayoutInflater.from(context).inflate(R.layout.song_item, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder)view.getTag();
        viewHolder.checkBox.setVisibility(View.GONE);
        viewHolder.imageView.setVisibility(View.GONE);

        int albumSongTitle = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
        int albumSongArtist = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);

        String currentSongTitle = cursor.getString(albumSongTitle);
        String currentSongArtist = cursor.getString(albumSongArtist);

        viewHolder.title.setText(currentSongTitle);
        viewHolder.artist.setText(currentSongArtist);
    }
}
