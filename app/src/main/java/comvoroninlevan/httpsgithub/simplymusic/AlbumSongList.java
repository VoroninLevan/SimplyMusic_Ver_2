package comvoroninlevan.httpsgithub.simplymusic;

import android.app.LoaderManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by Levan on 06.12.2016.
 */

public class AlbumSongList extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    AlbumSongsCursorAdapter albumSongsCursorAdapter;
    private static final int ALBUM_SONGS_LOADER = 5;
    long id;
    private ListView albumSongsList;
    private ImageView albumArt;


    SongArrayList list = SongArrayList.getInstance();
    private MediaPlayerService mediaPlayerService = new MediaPlayerService();
    private Intent intent;
    private boolean musicBound = false;
    BindBoolean aBoolean = new BindBoolean();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.album_songs);

        Intent intent = getIntent();
        id = intent.getLongExtra("id", 0);

        albumArt = (ImageView)findViewById(R.id.albumArt);
        Bitmap art = getAlbumArt();
        if(art != null) {
            albumArt.setImageBitmap(art);
        }else{
            albumArt.setImageResource(R.drawable.playgold);
        }

        list.longList = new ArrayList<>();
        albumSongsList = (ListView)findViewById(R.id.albumSongs);
        albumSongsCursorAdapter = new AlbumSongsCursorAdapter(this, null);
        albumSongsList.setAdapter(albumSongsCursorAdapter);

        albumSongsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                if(!aBoolean.bindAlbum){
                    aBoolean.bindAlbum = true;
                    for(int i = 0; i <= albumSongsList.getAdapter().getCount(); i++){
                        list.longList.add(i, albumSongsList.getItemIdAtPosition(i));
                    }
                    mediaPlayerService.setList();
                }
                mediaPlayerService.setSong(position);
                mediaPlayerService.playSong();
            }
        });

        this.getLoaderManager().initLoader(ALBUM_SONGS_LOADER, null, this);
    }

    private Bitmap getAlbumArt(){

        Bitmap albumArt = null;
        String selection = MediaStore.Audio.Albums._ID + " = " + id + "";
        Cursor cursor = getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, null, selection, null, null);

        if(cursor.moveToFirst()){

            int art = cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART);

            String currentArt = cursor.getString(art);
            albumArt = BitmapFactory.decodeFile(currentArt);
        }
        cursor.close();
        return albumArt;
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MediaPlayerService.MusicBinder musicBinder = (MediaPlayerService.MusicBinder)iBinder;
            mediaPlayerService = musicBinder.getService();
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

            musicBound = false;
        }
    };


    @Override
    public void onStart() {
        super.onStart();
        if(intent == null){
            intent = new Intent(this, MediaPlayerService.class);
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String selection = "album_id = " + id;
        return new CursorLoader(this, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, selection, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        albumSongsCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        albumSongsCursorAdapter.swapCursor(null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        aBoolean.bindAlbum = false;
    }
}
