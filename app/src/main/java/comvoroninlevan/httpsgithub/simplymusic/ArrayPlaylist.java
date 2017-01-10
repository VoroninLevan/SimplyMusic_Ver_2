package comvoroninlevan.httpsgithub.simplymusic;

import java.util.ArrayList;

/**
 * Created by Levan on 20.12.2016.
 */

public class ArrayPlaylist {

    ArrayList<Integer> integerPlayList = new ArrayList<>();
    public static volatile ArrayPlaylist instance = null;
    public ArrayPlaylist() { }

    public static ArrayPlaylist getInstance() {
        if (instance == null) {
            synchronized (ArrayPlaylist.class) {
                if (instance == null) {
                    instance = new ArrayPlaylist();
                }
            }
        }
        return instance;
    }
}
