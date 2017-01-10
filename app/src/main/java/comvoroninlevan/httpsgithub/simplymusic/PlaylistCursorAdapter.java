package comvoroninlevan.httpsgithub.simplymusic;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Levan on 02.12.2016.
 */

public class PlaylistCursorAdapter extends CursorAdapter{

    public PlaylistCursorAdapter(Context context, Cursor cursor){
        super(context, cursor, 0);
    }
    public boolean isAllItemsVisible;
    private ArrayPlaylist arrayPlaylist = ArrayPlaylist.getInstance();


    private static class ViewHolder{
        CheckBox checkBox;
        TextView playlistTitle;

        private ViewHolder(View view){

            checkBox = (CheckBox)view.findViewById(R.id.checkBox);
            playlistTitle = (TextView)view.findViewById(R.id.playlistTitle);
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View view = LayoutInflater.from(context).inflate(R.layout.playlist_item, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, final Cursor cursor) {

        final ViewHolder viewHolder = (ViewHolder)view.getTag();
        if(isAllItemsVisible){
            viewHolder.checkBox.setVisibility(View.VISIBLE);
        }else{
            viewHolder.checkBox.setVisibility(View.GONE);
        }

        final int position = cursor.getPosition();
        viewHolder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (b){
                    arrayPlaylist.integerPlayList.add(position);

                }else{
                    arrayPlaylist.integerPlayList.remove(Integer.valueOf(position));
                }
            }
        });

        int playlistName = cursor.getColumnIndex(MediaStore.Audio.Playlists.NAME);

        String playlist = cursor.getString(playlistName);

        viewHolder.playlistTitle.setText(playlist);
    }
}
