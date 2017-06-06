package files;

import com.sun.media.jfxmedia.events.PlayerStateEvent;
import music.Track;

import java.io.Serializable;
import java.util.ArrayList;

public class Settings implements Serializable {

    private ArrayList<PlaylistModel> playlists;

    public Settings() {
        playlists = new ArrayList<>();
    }

    public void addPlaylist(String name, ArrayList<Track> tracks) {
        playlists.add(new PlaylistModel(name, tracks));
    }

    public boolean doesExist(String name) {
        if(playlists == null || playlists.size() == 0) return false;


        for(PlaylistModel pm : playlists) {
            if(pm.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public PlaylistModel getPlaylist(String name) {
        if(playlists == null || playlists.size() == 0) return null;

        for(PlaylistModel pm : playlists) {
            if(pm.getName().equals(name)) {
                return pm;
            }
        }
        return null;
    }

    public ArrayList<PlaylistModel> getPlaylists() {
        return playlists;
    }

    public void remove(PlaylistModel playlist) {
        playlists.remove(playlist);
    }


}
