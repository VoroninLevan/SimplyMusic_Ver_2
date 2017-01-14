package comvoroninlevan.httpsgithub.simplymusic;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
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
 * Created by Levan on 06.12.2016.
 */

public class AllSongsCursorAdapter extends CursorAdapter {

    public AllSongsCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    public boolean isCheckBoxVisible;
    private AddToFavouritesArray favouritesArray = AddToFavouritesArray.getInstance();

    private static class ViewHolder {
        CheckBox checkBox;
        TextView title;
        TextView artist;
        ImageView albumArt;

        private ViewHolder(View view) {

            checkBox = (CheckBox) view.findViewById(R.id.favouritesCheckBox);
            title = (TextView) view.findViewById(R.id.title);
            artist = (TextView) view.findViewById(R.id.artist);
            albumArt = (ImageView) view.findViewById(R.id.albumArt);
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
    public void bindView(View view, final Context context, final Cursor cursor) {

        final ViewHolder viewHolder = (ViewHolder) view.getTag();
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
                    favouritesArray.integerArray.add(position);

                } else {
                    favouritesArray.integerArray.remove(Integer.valueOf(position));
                }
            }
        });

        int songTitle = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
        int songArtist = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);

        String currentTitle = cursor.getString(songTitle);
        String currentArtist = cursor.getString(songArtist);

        viewHolder.title.setText(currentTitle);
        viewHolder.artist.setText(currentArtist);

        //__________________________ALBUM_ART_______________________________________________________


        new AsyncTask<ViewHolder, Void, Bitmap>(){

            private ViewHolder viewHolder;

            @Override
            protected Bitmap doInBackground(ViewHolder... viewHolders) {
                viewHolder = viewHolders[0];
                int id = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
                long songId = cursor.getLong(id);

                Bitmap albumArt = getAlbumId(context, songId);
                return albumArt;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);
                if(bitmap != null) {
                    viewHolder.albumArt.setImageBitmap(bitmap);
                }else{
                    viewHolder.albumArt.setImageResource(R.drawable.placeholder);
                }
            }
        }.execute(viewHolder);


        /*
        int id = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
        long songId = cursor.getLong(id);

        Bitmap albumArt = getAlbumId(context, songId);

        if(albumArt != null) {
            viewHolder.albumArt.setImageBitmap(albumArt);
        }else{
            viewHolder.albumArt.setImageResource(R.drawable.placeholder);
        }

        */
    }

    private Bitmap getAlbumId(Context context, long id){

        Bitmap albumArt = null;
        String selection = MediaStore.Audio.Media._ID + " = " + id + "";
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[] {
                        MediaStore.Audio.Media._ID, MediaStore.Audio.Media.ALBUM_ID},
                selection, null, null);
        if (cursor.moveToFirst()) {
            long albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));

            albumArt = getAlbumArt(context, albumId);
        }
        cursor.close();
        return albumArt;

    }

    private Bitmap getAlbumArt(Context context, long albumId){

        Bitmap albumArt = null;
        String selection = MediaStore.Audio.Albums._ID + " = " + albumId + "";
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, null, selection, null, null);

        if(cursor.moveToFirst()){

            int art = cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART);

            String currentArt = cursor.getString(art);
            albumArt = BitmapFactory.decodeFile(currentArt);
        }
        cursor.close();
        return albumArt;
    }
}
