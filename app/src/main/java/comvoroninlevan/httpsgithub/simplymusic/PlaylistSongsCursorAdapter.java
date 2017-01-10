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
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Levan on 07.12.2016.
 */

public class PlaylistSongsCursorAdapter extends CursorAdapter {

    public PlaylistSongsCursorAdapter(Context context, Cursor cursor){
        super(context, cursor, 0);
    }

    public boolean isCheckBoxVisible;
    private DeleteFromPlaylist deleteFromPlaylist = DeleteFromPlaylist.getInstance();

    private static class ViewHolder{
        CheckBox checkBox;
        TextView title;
        TextView artist;

        private ViewHolder(View view){

            checkBox = (CheckBox)view.findViewById(R.id.favouritesCheckBox);
            title = (TextView)view.findViewById(R.id.title);
            artist = (TextView)view.findViewById(R.id.artist);
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View view = LayoutInflater.from(context).inflate(R.layout.playlist_song_item, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder)view.getTag();
        if (isCheckBoxVisible) {
            viewHolder.checkBox.setVisibility(View.VISIBLE);
        } else {
            viewHolder.checkBox.setVisibility(View.GONE);
        }

        final int position = cursor.getPosition();
        viewHolder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (b) {
                    deleteFromPlaylist.integerArray.add(position);

                } else {
                    deleteFromPlaylist.integerArray.remove(Integer.valueOf(position));
                }
            }
        });

        int playlistSongTitle = cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.TITLE);
        int playlistSongArtist = cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.ARTIST);

        String currentTitle = cursor.getString(playlistSongTitle);
        String currentArtist = cursor.getString(playlistSongArtist);

        viewHolder.title.setText(currentTitle);
        viewHolder.artist.setText(currentArtist);
    }
}
