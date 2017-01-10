package comvoroninlevan.httpsgithub.simplymusic;

import java.util.ArrayList;

/**
 * Created by Levan on 22.12.2016.
 */

public class AddToFavouritesArray {

    ArrayList<Integer> integerArray = new ArrayList<>();
    public static volatile AddToFavouritesArray instance = null;
    public AddToFavouritesArray() { }

    public static AddToFavouritesArray getInstance() {
        if (instance == null) {
            synchronized (AddToFavouritesArray.class) {
                if (instance == null) {
                    instance = new AddToFavouritesArray();
                }
            }
        }
        return instance;
    }
}
