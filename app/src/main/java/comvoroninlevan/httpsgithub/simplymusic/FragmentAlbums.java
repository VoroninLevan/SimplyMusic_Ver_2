package comvoroninlevan.httpsgithub.simplymusic;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

/**
 * Created by Levan on 11.11.2016.
 */

public class FragmentAlbums extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    AlbumsCursorAdapter albumsCursorAdapter;
    private static final int ALBUM_LOADER = 2;

    public FragmentAlbums(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.album_grid, container, false);

        GridView albumGridView = (GridView)rootView.findViewById(R.id.album_grid);

        albumsCursorAdapter = new AlbumsCursorAdapter(getActivity(), null);
        albumGridView.setAdapter(albumsCursorAdapter);

        albumGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long id) {

                Intent intent = new Intent(getActivity(), AlbumSongList.class);
                intent.putExtra("id", id);
                startActivity(intent);
            }
        });

        getActivity().getLoaderManager().initLoader(ALBUM_LOADER, null, this);
        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {

        return new CursorLoader(getActivity(), MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        albumsCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        albumsCursorAdapter.swapCursor(null);
    }
}
