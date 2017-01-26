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
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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

public class FragmentFavourites extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private ListView favouritesSongsList;
    private FavouritesCursorAdapter favouritesCursorAdapter;
    SongArrayList list = SongArrayList.getInstance();
    private MediaPlayerService mediaPlayerService = new MediaPlayerService();
    private boolean mDeleteSelector = false;
    private static final int FAVOURITES_LOADER = 8;
    BindBoolean aBoolean = new BindBoolean();
    FavouritesDbHelper dbHelper;
    private boolean musicBound = false;
    private Intent intent;
    AddToFavouritesArray favouritesArray = AddToFavouritesArray.getInstance();

    public FragmentFavourites(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.favourites_list_view, container, false);


        dbHelper = new FavouritesDbHelper(getActivity());

        favouritesSongsList = (ListView) rootView.findViewById(R.id.listOfSongs);

        favouritesCursorAdapter = new FavouritesCursorAdapter(getActivity(), null);
        favouritesSongsList.setAdapter(favouritesCursorAdapter);

        View emptyView = rootView.findViewById(R.id.empty_view);
        favouritesSongsList.setEmptyView(emptyView);

        favouritesSongsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                if(!aBoolean.bindFavourites){
                    list.longList = new ArrayList<>();
                    aBoolean.bindFavourites = true;
                    for(int i = 0; i <= favouritesSongsList.getAdapter().getCount(); i++){
                        list.longList.add(i, favouritesSongsList.getItemIdAtPosition(i));
                    }
                    mediaPlayerService.setList();
                }
                mediaPlayerService.setSong(position);
                mediaPlayerService.playSong();
            }
        });

        favouritesSongsList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                showCheckBoxes();
                mDeleteSelector = true;
                return true;
            }
        });

        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            String[] ids = getDataFromDb();
            if (ids.length > 0) {
                getActivity().getLoaderManager().initLoader(FAVOURITES_LOADER, null, this);
            }
        }

        return rootView;
    }

    public void showCheckBoxes(){
        favouritesCursorAdapter.isCheckBoxVisible = true;
        favouritesCursorAdapter.notifyDataSetChanged();
        getActivity().invalidateOptionsMenu();
    }

    public void hideCheckBoxes(){
        favouritesCursorAdapter.isCheckBoxVisible = false;
        favouritesCursorAdapter.notifyDataSetChanged();
        getActivity().invalidateOptionsMenu();
    }

    public void deleteFromFavourites(){
        for(int i = 0; i != favouritesCursorAdapter.getCount(); i++){

            if(favouritesArray.integerArray.contains(i)){
                long id = favouritesCursorAdapter.getItemId(i);
                String where = FavouritesContract.FavouritesEntry.SONG_ID + " =?";
                String currentId = String.valueOf(id);
                String[] whereArgs = { currentId };
                getActivity().getContentResolver().delete(FavouritesContract.FavouritesEntry.CONTENT_URI, where, whereArgs);
                favouritesArray.integerArray.remove(Integer.valueOf(i));
            }
        }
    }

    public String[] getDataFromDb(){
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        String[] proj = {FavouritesContract.FavouritesEntry._ID, FavouritesContract.FavouritesEntry.SONG_ID};
        Cursor cursor = database.query(FavouritesContract.FavouritesEntry.TABLE_NAME, null, null, null, null, null, null);
        String[] ids = new String[cursor.getCount()];
        int i = 0;
        while(cursor.moveToNext()){
            String id = cursor.getString(cursor.getColumnIndex("song_id"));
            ids[i] = id;
            i++;
        }
        cursor.close();
        return ids;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.delete_from_favourites_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if(mDeleteSelector){
            MenuItem menuItemDelete = menu.findItem(R.id.action_delete_from_favourites);
            menuItemDelete.setVisible(true);
            MenuItem menuItemClear = menu.findItem(R.id.action_clear);
            menuItemClear.setVisible(true);
        }else{
            MenuItem menuItemDelete = menu.findItem(R.id.action_delete_from_favourites);
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
            case R.id.action_delete_from_favourites:
                deleteFromFavourites();
                hideCheckBoxes();
                getActivity().getLoaderManager().restartLoader(FAVOURITES_LOADER, null, this);
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
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        String selection = MediaStore.Audio.Media._ID + " in (";
        String[] ids = getDataFromDb();
        for (int a = 0; a < ids.length; a++) {
            selection += "?, ";
        }
        selection = selection.substring(0, selection.length() - 2) + ")";
        if(ids.length > 0) {
            return new CursorLoader(getActivity(), MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, selection, ids, null);
        }else{
            return new CursorLoader(getActivity(), MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Audio.Media._ID, null, null);
        }

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        favouritesCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        favouritesCursorAdapter.swapCursor(null);
    }

    @Override
    public void onPause() {
        super.onPause();
        aBoolean.bindFavourites = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            String[] ids = getDataFromDb();
            if (ids.length > 0) {
                getActivity().getLoaderManager().restartLoader(FAVOURITES_LOADER, null, this);
            }
        }
        favouritesCursorAdapter.notifyDataSetChanged();
    }
}
