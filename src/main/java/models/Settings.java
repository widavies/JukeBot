package models;

import modules.music.Track;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

public class Settings implements Serializable {

    private ArrayList<PlaylistModel> playlists;
    private ArrayList<String> insults;

    /**
     * The database for lan utilities.
     * Entries are in the format
     *
     * <Discord-ID> <Game> <Skill 1-1000>
     */
    private ArrayList<String> lanDatabase;
    private int tolerance;
    private String setip; // IP connect command for the server

    public Settings() {
        playlists = new ArrayList<>();
        lanDatabase = new ArrayList<>();
        insults = new ArrayList<>();
    }

    public void addInsult(String insult) {
        insults.add(insult);
    }

    public String getInsult() {
        if(insults.size() == 0) return "No insults found in database";
        return insults.get(new Random().nextInt(insults.size()));
    }


    public String getSetip() {
        return setip;
    }

    public void setSetip(String setip) {
        this.setip = setip;
    }

    public ArrayList<String> getLanDatabase() {
        return lanDatabase;
    }

    public String getDatabase(GuildMessageReceivedEvent event) {
        String temp = "";
        for(String s : lanDatabase) {
            temp += s+"\n";
        }
        return temp;
    }

    public void setTolerance(int tolerance) {
        this.tolerance = tolerance;
    }

    public int getTolerance() {
        return tolerance;
    }

    public boolean addEntry(String friendlyName, String discordID, String game, String skill) {
        // check if we need to overwrite an entry
        for(int i = 0; i < lanDatabase.size(); i++) {

            if(lanDatabase.get(i).split(":")[0].equals(discordID) && lanDatabase.get(i).split(":")[1].equalsIgnoreCase(game)) {
                lanDatabase.set(i, discordID+":"+game+":"+skill+":"+friendlyName);
                return true;
            }
        }

        lanDatabase.add(discordID+":"+game+":"+skill+":"+friendlyName);
        return false;
    }

    public int getSkill(String discordID, String game) {
        for(String s: lanDatabase) {
            if(s.split(":")[0].equals(discordID) && s.split(":")[1].equalsIgnoreCase(game)) {
                return Integer.parseInt(s.split(":")[2]);
            }
        }
        return 0;
    }

    public void deleteEntry(String discordID, String game) {
        for(int i = 0; i < lanDatabase.size(); i++) {
            if(lanDatabase.get(i).split(":")[0].equals(discordID) && lanDatabase.get(i).split(":")[1].equalsIgnoreCase(game)) {
                lanDatabase.remove(i);
                return;
            }
        }
    }

    public void deletePlayer(String discordID) {
        for(int i = 0; i < lanDatabase.size(); i++) {
            if(lanDatabase.get(i).split(":")[0].equals("discord")) {
                lanDatabase.remove(i);
                i = 0;
            }
        }
    }

    public void deleteDatabase() {
        lanDatabase.clear();
    }

    public void addPlaylist(String name, ArrayList<Track> tracks) {
        playlists.add(new PlaylistModel(name, tracks));
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
