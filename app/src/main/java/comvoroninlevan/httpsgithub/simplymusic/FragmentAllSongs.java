package comvoroninlevan.httpsgithub.simplymusic;

import android.Manifest;
import android.app.LoaderManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import java.util.ArrayList;

import comvoroninlevan.httpsgithub.simplymusic.data.FavouritesContract;
import comvoroninlevan.httpsgithub.simplymusic.data.FavouritesDbHelper;

/**
 * Created by Levan on 11.11.2016.
 */

public class FragmentAllSongs extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    AllSongsCursorAdapter allSongsCursorAdapter;
    private static final int SONG_LOADER = 1;
    private ListView songsList;
    private boolean mDeleteSelector = false;
    AddToFavouritesArray favouritesArray = AddToFavouritesArray.getInstance();

    SongArrayList list = SongArrayList.getInstance();
    private MediaPlayerService mediaPlayerService = new MediaPlayerService();
    private Intent intent;
    private boolean musicBound = false;
    BindBoolean aBoolean = new BindBoolean();
    FavouritesDbHelper dbHelper;

    public FragmentAllSongs() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.list_view, container, false);

        permissionRequest();
        dbHelper = new FavouritesDbHelper(getActivity());

        songsList = (ListView) rootView.findViewById(R.id.listOfSongs);

        allSongsCursorAdapter = new AllSongsCursorAdapter(getActivity(), null);
        songsList.setAdapter(allSongsCursorAdapter);

        songsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                if(!aBoolean.bindAllSongs){
                    list.longList = new ArrayList<>();
                    aBoolean.bindAllSongs = true;
                    for(int i = 0; i <= songsList.getAdapter().getCount(); i++){
                        list.longList.add(i, songsList.getItemIdAtPosition(i));
                    }
                    mediaPlayerService.setList();
                }
                mediaPlayerService.setSong(position);
                mediaPlayerService.playSong();
            }
        });

        songsList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                showCheckBoxes();
                mDeleteSelector = true;
                return true;
            }
        });

        getActivity().getLoaderManager().initLoader(SONG_LOADER, null, this);

        return rootView;
    }

    // TODO USE ASYNC
    //________________________________________________PERMISSION_REQUEST_NOT_READY__________________

    //          SHOULD ASK FOR REQUEST ASYNCHRONOUSLY
    public void  permissionRequest(){
        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }else{

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){

            }else{
                permissionRequest();
            }
        }
    }
    //______________________________________________________________________________________________

    public void showCheckBoxes(){
        allSongsCursorAdapter.isCheckBoxVisible = true;
        allSongsCursorAdapter.notifyDataSetChanged();
        getActivity().invalidateOptionsMenu();
    }

    public void hideCheckBoxes(){

        for(int i = 0; i != allSongsCursorAdapter.getCount(); i++){

            if(favouritesArray.integerArray.contains(i)){
                favouritesArray.integerArray.remove(Integer.valueOf(i));
            }
        }
        allSongsCursorAdapter.isCheckBoxVisible = false;
        allSongsCursorAdapter.notifyDataSetChanged();
        getActivity().invalidateOptionsMenu();
    }

    public void addToFavourites(){
        ContentValues contentValues = new ContentValues();
        for(int i = 0; i != allSongsCursorAdapter.getCount(); i++){

            if(favouritesArray.integerArray.contains(i)){
                long id = allSongsCursorAdapter.getItemId(i);
                String ids = String.valueOf(id);
                contentValues.put(FavouritesContract.FavouritesEntry.SONG_ID, ids);
                getActivity().getContentResolver().insert(FavouritesContract.FavouritesEntry.CONTENT_URI, contentValues);
                favouritesArray.integerArray.remove(Integer.valueOf(i));
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.add_to_favourites_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if(mDeleteSelector){
            MenuItem menuItemDelete = menu.findItem(R.id.action_add_to_favourites);
            menuItemDelete.setVisible(true);
            MenuItem menuItemClear = menu.findItem(R.id.action_clear);
            menuItemClear.setVisible(true);
        }else{
            MenuItem menuItemDelete = menu.findItem(R.id.action_add_to_favourites);
            menuItemDelete.setVisible(false);
            MenuItem menuItemClear = menu.findItem(R.id.action_clear);
            menuItemClear.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_clear:
                hideCheckBoxes();
                mDeleteSelector = false;
                return true;
            case R.id.action_add_to_favourites:
                addToFavourites();
                hideCheckBoxes();
                mDeleteSelector = false;
                return true;
        }
        return super.onOptionsItemSelected(item);
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
            intent = new Intent(getActivity(), MediaPlayerService.class);
            getActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {

        return new CursorLoader(getActivity(), MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        allSongsCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        allSongsCursorAdapter.swapCursor(null);
    }

    @Override
    public void onPause() {
        super.onPause();

        for(int i = 0; i != allSongsCursorAdapter.getCount(); i++){

            if(favouritesArray.integerArray.contains(i)){
                favouritesArray.integerArray.remove(Integer.valueOf(i));
            }
        }

        aBoolean.bindAllSongs = false;
    }
}
