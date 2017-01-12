package comvoroninlevan.httpsgithub.simplymusic;

import android.app.LoaderManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by Levan on 25.11.2016.
 */

public class PlaylistSongs extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    PlaylistSongsCursorAdapter playlistSongsCursorAdapter;
    private static final int SONGS_FROM_PLAYLIST = 6;
    private Uri playlistUri;
    private long playlistId;
    private String playlistName;
    ListView playlistSongs;
    private boolean mDeleteSelector = false;
    DeleteFromPlaylist deleteFromPlaylist = DeleteFromPlaylist.getInstance();

    SongArrayList list = SongArrayList.getInstance();
    private MediaPlayerService mediaPlayerService = new MediaPlayerService();
    private Intent intent;
    private boolean musicBound = false;
    BindBoolean aBoolean = new BindBoolean();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlist_songs);

        Intent intent = getIntent();
        playlistUri = intent.getData();
        playlistName = intent.getExtras().getString("playlistName", null);
        setTitle(playlistName);
        playlistId = intent.getExtras().getLong("playlistId", 0);
        playlistSongs = (ListView)findViewById(R.id.playlistSongs);

        playlistSongsCursorAdapter = new PlaylistSongsCursorAdapter(this, null);
        playlistSongs.setAdapter(playlistSongsCursorAdapter);
        playlistSongs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                if(!aBoolean.bindPlaylist){
                    list.longList = new ArrayList<>();
                    aBoolean.bindPlaylist = true;
                    for(int i = 0; i <= playlistSongs.getAdapter().getCount(); i++){
                        list.longList.add(i, playlistSongs.getItemIdAtPosition(i));
                    }
                    mediaPlayerService.setList();
                }
                mediaPlayerService.setSong(position);
                mediaPlayerService.playSongFromPlaylist(playlistUri);
            }
        });
        playlistSongs.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                showCheckBoxes();
                mDeleteSelector = true;
                return true;
            }
        });
        this.getLoaderManager().initLoader(SONGS_FROM_PLAYLIST, null, this);
    }

    public void showCheckBoxes(){
        playlistSongsCursorAdapter.isCheckBoxVisible = true;
        playlistSongsCursorAdapter.notifyDataSetChanged();
        this.invalidateOptionsMenu();
    }

    public void hideCheckBoxes(){

        for(int i = 0; i != playlistSongsCursorAdapter.getCount(); i++){

            if(deleteFromPlaylist.integerArray.contains(i)){
                deleteFromPlaylist.integerArray.remove(Integer.valueOf(i));
            }
        }
        playlistSongsCursorAdapter.isCheckBoxVisible = false;
        playlistSongsCursorAdapter.notifyDataSetChanged();
        this.invalidateOptionsMenu();
    }

    private void deleteSongsFromPlaylist(){

        for(int i = 0; i != playlistSongsCursorAdapter.getCount(); i++){
            if(deleteFromPlaylist.integerArray.contains(i)){
                long id = playlistSongsCursorAdapter.getItemId(i);
                Uri uri = MediaStore.Audio.Playlists.Members.getContentUri(
                        "external", playlistId);
                String where = MediaStore.Audio.Playlists.Members._ID + " =?";
                String songId = Long.toString(id);
                String[] whereArgs = { songId };
                getContentResolver().delete(uri, where, whereArgs);
                deleteFromPlaylist.integerArray.remove(Integer.valueOf(i));
            }
        }
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
            intent.setData(playlistUri);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {

        Uri uri = Uri.withAppendedPath(playlistUri, "members");
        return new CursorLoader(this, uri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        playlistSongsCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        playlistSongsCursorAdapter.swapCursor(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_songs_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(mDeleteSelector){
            MenuItem menuItemDelete = menu.findItem(R.id.action_delete_from_playlist);
            menuItemDelete.setVisible(true);
            MenuItem menuItemClear = menu.findItem(R.id.action_clear);
            menuItemClear.setVisible(true);
        }else{
            MenuItem menuItemDelete = menu.findItem(R.id.action_delete_from_playlist);
            menuItemDelete.setVisible(false);
            MenuItem menuItemClear = menu.findItem(R.id.action_clear);
            menuItemClear.setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_delete_from_playlist:
                deleteSongsFromPlaylist();
                hideCheckBoxes();
                mDeleteSelector = false;
                return true;
            case R.id.action_clear:
                hideCheckBoxes();
                mDeleteSelector = false;
                return true;
            case R.id.action_add_songs:
                Intent intent = new Intent(this, AddActivity.class);
                intent.setData(playlistUri);
                intent.putExtra("playlistName", playlistName);
                startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onStop() {
        super.onStop();
        aBoolean.bindPlaylist = false;
        for(int i = 0; i != playlistSongsCursorAdapter.getCount(); i++){

            if(deleteFromPlaylist.integerArray.contains(i)){
                deleteFromPlaylist.integerArray.remove(Integer.valueOf(i));
            }
        }
    }
}
