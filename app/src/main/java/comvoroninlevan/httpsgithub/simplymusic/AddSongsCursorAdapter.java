package comvoroninlevan.httpsgithub.simplymusic;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 * Created by Levan on 07.12.2016.
 */

public class AddSongsCursorAdapter extends CursorAdapter {

    public AddSongsCursorAdapter(Context context, Cursor cursor){
        super(context, cursor, 0);
    }

    private AddToPlaylistArray addToPlaylistArray = AddToPlaylistArray.getInstance();

    private static class ViewHolder {
        CheckBox checkBox;
        TextView title;
        TextView artist;

        private ViewHolder(View view) {

            checkBox = (CheckBox) view.findViewById(R.id.addCheckBox);
            title = (TextView) view.findViewById(R.id.addTitle);
            artist = (TextView) view.findViewById(R.id.addArtist);
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View view = LayoutInflater.from(context).inflate(R.layout.add_item, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        final int position = cursor.getPosition();
        viewHolder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (b) {
                    addToPlaylistArray.integerArray.add(position);

                } else {
                    addToPlaylistArray.integerArray.remove(Integer.valueOf(position));
                }
            }
        });

        int songTitle = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
        int songArtist = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);

        String currentTitle = cursor.getString(songTitle);
        String currentArtist = cursor.getString(songArtist);

        viewHolder.title.setText(currentTitle);
        viewHolder.artist.setText(currentArtist);
    }
}
