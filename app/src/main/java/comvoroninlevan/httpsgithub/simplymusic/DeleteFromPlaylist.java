package comvoroninlevan.httpsgithub.simplymusic;

import java.util.ArrayList;

/**
 * Created by Levan on 09.01.2017.
 */

public class DeleteFromPlaylist {

    ArrayList<Integer> integerArray = new ArrayList<>();
    public static volatile DeleteFromPlaylist instance = null;
    public DeleteFromPlaylist() { }

    public static DeleteFromPlaylist getInstance() {
        if (instance == null) {
            synchronized (DeleteFromPlaylist.class) {
                if (instance == null) {
                    instance = new DeleteFromPlaylist();
                }
            }
        }
        return instance;
    }
}
