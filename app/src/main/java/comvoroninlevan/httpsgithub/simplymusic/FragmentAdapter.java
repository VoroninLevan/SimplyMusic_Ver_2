package comvoroninlevan.httpsgithub.simplymusic;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by Levan on 11.11.2016.
 */

public class FragmentAdapter extends FragmentPagerAdapter {

    private Context mContext;

    public FragmentAdapter(Context context, FragmentManager fragmentManager){
        super(fragmentManager);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        if(position == 0){
            return new FragmentAllSongs();
        }else if(position == 1){
            return new FragmentAlbums();
        }else if(position == 2){
            return new FragmentPlayLists();
        }else{
            return new FragmentFavourites();
        }
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if(position == 0){
            return mContext.getString(R.string.allSongs);
        }else if(position == 1){
            return mContext.getString(R.string.albums);
        }else if(position == 2){
            return mContext.getString(R.string.playLists);
        }else{
            return mContext.getString(R.string.favourites);
        }
    }
}
