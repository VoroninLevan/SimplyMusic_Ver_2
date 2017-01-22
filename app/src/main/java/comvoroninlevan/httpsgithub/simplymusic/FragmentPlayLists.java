package comvoroninlevan.httpsgithub.simplymusic;

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * Created by Levan on 11.11.2016.
 */

public class FragmentPlayLists extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private ListView playlist;
    private CheckBox mCheckBox;
    private boolean mDeleteSelector = false;
    CheckBox checkBox;
    ArrayPlaylist arrayPlaylist = ArrayPlaylist.getInstance();

    PlaylistCursorAdapter mAdapter;
    private static final int LOADER = 3;

    public FragmentPlayLists(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.playlist, container, false);

        playlist = (ListView)rootView.findViewById(R.id.playlist);
        mAdapter = new PlaylistCursorAdapter(getActivity(), null);
        playlist.setAdapter(mAdapter);

        playlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int position, long id) {

                Intent intent = new Intent(getActivity(), PlaylistSongs.class);
                TextView name = (TextView)view.findViewById(R.id.playlistTitle);
                String playlistName = name.getText().toString();
                Uri currentPlaylistUri = ContentUris.withAppendedId(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, id);
                intent.setData(currentPlaylistUri);
                intent.putExtra("playlistName", playlistName);
                intent.putExtra("playlistId", id);
                startActivity(intent);
            }
        });

        playlist.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long id) {

                checkBox = (CheckBox)view.findViewById(R.id.checkBox);
                showCheckBoxes();
                mDeleteSelector = true;
                return true;
            }
        });

        FloatingActionButton floatingActionButton = (FloatingActionButton)rootView.findViewById(R.id.addPlayList);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showCreateConfirmationDialog();
            }
        });
        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            getActivity().getLoaderManager().initLoader(LOADER, null, this);
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction("comvoroninlevan.httpsgithub.simplymusic.PERMISSION_GRANTED");

        getActivity().registerReceiver(broadcastReceiver, filter);
        return rootView;
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equalsIgnoreCase("comvoroninlevan.httpsgithub.simplymusic.PERMISSION_GRANTED")){
                getActivity().getLoaderManager().initLoader(LOADER, null, FragmentPlayLists.this);
            }
        }
    };

    private void showCreateConfirmationDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint(R.string.hintPlaylist);
        builder.setMessage(R.string.createPlaylist);
        builder.setView(input);
        builder.setPositiveButton(R.string.positivePlaylist, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                createNewPlaylist(input.getText().toString());
            }
        });
        builder.setNegativeButton(R.string.negativePlaylist, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showErrorCreateConfirmationDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint(R.string.hintPlaylist);
        builder.setMessage(R.string.errorName);
        builder.setView(input);
        builder.setPositiveButton(R.string.positivePlaylist, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                createNewPlaylist(input.getText().toString());
            }
        });
        builder.setNegativeButton(R.string.negativePlaylist, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public static long getPlaylist(ContentResolver resolver, String name)
    {
        long id = -1;

        Cursor cursor = resolver.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Audio.Playlists._ID },
                MediaStore.Audio.Playlists.NAME + "=?",
                new String[] { name }, null);

        if (cursor != null) {
            if (cursor.moveToNext())
                id = cursor.getLong(0);
            cursor.close();
        }

        return id;
    }

    private void createNewPlaylist(String playlistName){
        ContentResolver resolver = getActivity().getContentResolver();

        long currentId = getPlaylist(resolver, playlistName);

        if(currentId == -1) {
            ContentValues values = new ContentValues(1);
            values.put(MediaStore.Audio.Playlists.NAME, playlistName);
            Uri uri = resolver.insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, values);
            resolver.notifyChange(uri, null);
            long id = Long.parseLong(uri.getLastPathSegment());
            Uri newPlaylistUri = MediaStore.Audio.Playlists.Members.getContentUri("external", id);
            toAddActivity(playlistName, newPlaylistUri);
        } else {
            showErrorCreateConfirmationDialog();
        }
    }

    private void toAddActivity(String playlistName, Uri newPlaylistUri){

        Intent intent = new Intent(getActivity(), AddActivity.class);
        intent.putExtra("playlistName", playlistName);
        intent.setData(newPlaylistUri);
        startActivity(intent);
    }

    public void showCheckBoxes(){
        mAdapter.isAllItemsVisible = true;
        mAdapter.notifyDataSetChanged();
        getActivity().invalidateOptionsMenu();
    }

    public void hideCheckBoxes(){
        mAdapter.isAllItemsVisible = false;
        mAdapter.notifyDataSetChanged();
        getActivity().invalidateOptionsMenu();
    }

    public void deletePlaylist(){

        for(int i = 0; i != mAdapter.getCount(); i++){

            ContentResolver contentResolver = getActivity().getContentResolver();
            if(arrayPlaylist.integerPlayList.contains(i)){
                long id = mAdapter.getItemId(i);
                Uri playlistUri = ContentUris.withAppendedId(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, id);
                contentResolver.delete(playlistUri, null, null);
                mAdapter.notifyDataSetChanged();
                arrayPlaylist.integerPlayList.remove(Integer.valueOf(i));
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.delete_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if(mDeleteSelector){
            MenuItem menuItemDelete = menu.findItem(R.id.action_delete);
            menuItemDelete.setVisible(true);
            MenuItem menuItemClear = menu.findItem(R.id.action_clear);
            menuItemClear.setVisible(true);
        }else{
            MenuItem menuItemDelete = menu.findItem(R.id.action_delete);
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
            case R.id.action_delete:
                deletePlaylist();
                hideCheckBoxes();
                mDeleteSelector = false;
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {

        return new CursorLoader(getActivity(), MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        mAdapter.swapCursor(null);
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
