package modules.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import tools.Log;

import java.util.ArrayList;
import java.util.Collections;

/*
 * This class manages the currently playing queue.
 * All methods are documented, should be easy to understand.
 *
 *
 *
 *
 */
public class Queue {
    // Audio libraries for controlling playback
    private AudioPlayer player;
    private AudioPlayerManager manager;

    // All the tracks currently in the queue
    private ArrayList<Track> tracks;

    // The currently playing track index
    public int current;

    public Queue() {
        tracks = new ArrayList<>();

        manager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(manager);
        player = manager.createPlayer();
        player.addListener(new PlayerListener());
    }

    // Sets the playback volume
    public void setVolume(int volume) {
        player.setVolume(volume);
    }

    public boolean isAlreadyPlaying() {
        return player.isPaused() || player.getPlayingTrack() != null;
    }

    // Adds the track to the end of the queue
    public void add(Track track) {
        this.tracks.add(track);
    }

    // Adds the the track to the very next position in the queue
    public void addNext(Track track) {
        if(current + 1 >= tracks.size()) {
            add(track);
            return;
        }
        this.tracks.add(current + 1, track);
    }

    public void shuffle() {
        player.stopTrack();
        if(tracks != null && tracks.size() > 0) Collections.shuffle(tracks);
        play();
    }

    // Gets the number of songs in the queue
    public int getSongsInQueue() {
        if(tracks == null) return 0;
        return this.tracks.size();
    }

    // Stops and clears the queue
    public void clear() {
        player.stopTrack();
        this.tracks.clear();
    }

    public void back() {
        current--;
        player.stopTrack();
        play();
    }

    public ArrayList<Track> getTracks() {
        return tracks;
    }

    // Pauses the queue
    public void pause() {
        player.setPaused(true);
    }

    // Resumes the queue
    public void resume() {
        player.setPaused(false);
    }

    // Skips the current song
    public void skip() {
        player.stopTrack();
        current++;
        play();
    }

    public int getCurrent() {
        return current;
    }

    // Starts playing music at index current
    public void play() {
        if(current >= tracks.size() || current < 0) current = 0; // loop back to the beginning if we reach the end of the queue
        manager.loadItem(tracks.get(current).getIdentifier(), new TrackLoadListener());
    }

    public int getVolume() {
        return player.getVolume();
    }

    public AudioPlayer getPlayer() {
        return player;
    }

    /*  AUDIO LISTENERS */
    private class TrackLoadListener implements AudioLoadResultHandler {
        @Override
        public void trackLoaded(AudioTrack track) {
            Log.log("Loaded track "+track.getIdentifier()+" successfully.");
            player.playTrack(track);
        }

        @Override
        public void playlistLoaded(AudioPlaylist playlist) {
            // Remove playlist "audio tracks"
            for(int i = 0; i < tracks.size(); i++) {
                if(tracks.get(i).getIdentifier().contains("playlist")) {
                    tracks.remove(i);
                    i = 0;
                }
            }

            // Loaded playlist
            ArrayList<Track> temp = new ArrayList<>();
            for(AudioTrack track : playlist.getTracks()) temp.add(new Track(track.getIdentifier()));
            Collections.shuffle(temp);
            tracks.addAll(temp);
            play();
        }

        @Override
        public void noMatches() { // we couldn't find the track, let's remove it from the queue
            Log.log("Couldn't find the track. Playing next song.");
            tracks.remove(current);
            play();

        }

        @Override
        public void loadFailed(FriendlyException throwable) {
            Log.log("Couldn't load the track. Playing next song.");
            tracks.remove(current);
            play();
        }
    }

    private class PlayerListener extends AudioEventAdapter {
        @Override
        public void onPlayerPause(AudioPlayer player) {}
        @Override
        public void onPlayerResume(AudioPlayer player) {}
        @Override
        public void onTrackStart(AudioPlayer player, AudioTrack track) {}
        @Override
        public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
            if (endReason.mayStartNext) {
                current++;
                play();
            }
        }
        @Override
        public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {}
        @Override
        public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {}
    }
}
