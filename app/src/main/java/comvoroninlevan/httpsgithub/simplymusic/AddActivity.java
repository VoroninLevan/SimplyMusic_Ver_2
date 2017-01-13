package comvoroninlevan.httpsgithub.simplymusic;

import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;

/**
 * Created by Levan on 13.11.2016.
 */

public class AddActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private ListView songsListView;
    private Uri mPlaylistUri;
    private AddToPlaylistArray addToPlaylistArray = AddToPlaylistArray.getInstance();

    AddSongsCursorAdapter addSongsCursorAdapter;
    private static final int ADD_LOADER = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_view);

        Intent intent = getIntent();
        mPlaylistUri = intent.getData();
        String playlistName = intent.getExtras().getString("playlistName", null);
        setTitle(playlistName);
        addSongsCursorAdapter = new AddSongsCursorAdapter(this, null);
        songsListView = (ListView)findViewById(R.id.listOfSongs);
        songsListView.setAdapter(addSongsCursorAdapter);
        songsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });
        this.getLoaderManager().initLoader(ADD_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_done:
                addSongsToPlaylist();
                finish();
                return true;
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addSongsToPlaylist(){

        for(int i = 0; i != addSongsCursorAdapter.getCount(); i++){
            ContentResolver resolver = getContentResolver();
            int base = 0;
            if(addToPlaylistArray.integerArray.contains(i)){
                long id = addSongsCursorAdapter.getItemId(i);
                ContentValues values = new ContentValues(2);
                values.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, base + id);
                values.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, id);
                resolver.insert(mPlaylistUri, values);
                addToPlaylistArray.integerArray.remove(Integer.valueOf(i));
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {

        return new CursorLoader(this, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        addSongsCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        addSongsCursorAdapter.swapCursor(null);
    }


}
