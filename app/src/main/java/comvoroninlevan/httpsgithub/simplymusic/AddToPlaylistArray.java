package comvoroninlevan.httpsgithub.simplymusic;

import java.util.ArrayList;

/**
 * Created by Levan on 13.01.2017.
 */

public class AddToPlaylistArray {

    ArrayList<Integer> integerArray = new ArrayList<>();
    public static volatile AddToPlaylistArray instance = null;
    public AddToPlaylistArray() { }

    public static AddToPlaylistArray getInstance() {
        if (instance == null) {
            synchronized (AddToPlaylistArray.class) {
                if (instance == null) {
                    instance = new AddToPlaylistArray();
                }
            }
        }
        return instance;
    }
}
