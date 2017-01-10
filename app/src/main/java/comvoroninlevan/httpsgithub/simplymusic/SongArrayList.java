package comvoroninlevan.httpsgithub.simplymusic;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by Levan on 12.12.2016.
 */

public class SongArrayList {

    ArrayList<Long> longList;
    private static volatile SongArrayList instance = null;
    private SongArrayList() { }

    public static SongArrayList getInstance() {
        if (instance == null) {
            synchronized (SongArrayList.class) {
                if (instance == null) {
                    instance = new SongArrayList();
                }
            }
        }

        return instance;
    }
}
