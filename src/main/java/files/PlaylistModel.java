package files;

import music.Track;

import java.util.ArrayList;

/**
 * Created by Will Davies on 6/6/2017.
 */
public class PlaylistModel {

    private ArrayList<Track> tracks;
    private String name;

    public PlaylistModel(String name, ArrayList<Track> tracks) {
        this.name = name;
        this.tracks = tracks;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Track> getTracks() {
        return tracks;
    }

}
