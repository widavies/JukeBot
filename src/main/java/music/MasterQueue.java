package music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import logging.Log;

import javax.swing.plaf.basic.BasicSliderUI;
import java.util.ArrayList;

/*
 * This class manages the currently playing queue.
 * All methods are documented, should be easy to understand.
 *
 *
 *
 *
 */
public class MasterQueue {
    // Audio libraries for controlling playback
    private AudioPlayer player;
    private AudioPlayerManager manager;

    // All the tracks currently in the queue
    private ArrayList<Track> tracks;

    // The currently playing track index
    private int current;

    public MasterQueue() {
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

    // Adds the track to the end of the queue
    public void add(Track track) {
        this.tracks.add(track);
    }

    // Adds the the track to the very next position in the queue
    public void addNext(Track track) {
        this.tracks.add(current + 1, track);
    }

    // Gets the number of songs in the queue
    public int getSongsInQueue() {
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

    // Starts playing music at index current
    public void play() {
        if(current >= tracks.size() || current < 0) current = 0; // loop back to the beginning if we reach the end of the queue
        manager.loadItem(tracks.get(current).getIdentifier(), new TrackLoadListener());
    }

    public AudioPlayer getPlayer() {
        return player;
    }

    /*  AUDIO LISTENERS */
    private class TrackLoadListener implements AudioLoadResultHandler {
        @Override
        public void trackLoaded(AudioTrack track) {
            player.playTrack(track);
        }

        @Override
        public void playlistLoaded(AudioPlaylist playlist) {

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
                //play();
            }
        }
        @Override
        public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {}
        @Override
        public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {}
    }
}
