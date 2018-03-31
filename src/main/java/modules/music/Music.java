package modules.music;

import models.Loader;
import models.PlaylistModel;
import models.Settings;
import modules.Mod;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageHistory;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import tools.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages commands and music playing for JukeBot.
 */
public class Music extends Mod {

    private Queue queue;
    private Settings settings;

    public Music() {
        queue = new Queue();
        settings = new Loader().getSettings();
    }

    @Override
    public boolean processCommand(String message){
        try {
            if (message.startsWith("add")) {
                queue.add(new Track(message.split("\\s+")[1]));
                String reply;
                if (queue.getSongsInQueue() == 1) reply = "Added to queue. There is 1 song in the queue.";
                else reply = "Added to queue. There are " + queue.getSongsInQueue() + " songs in the queue.";
                reply(reply, true);
                Log.log("User ["+event.getAuthor().getName()+"] added ["+message.split("\\s+")[1]+"] to the current playlist.");
                return true;
            } else if (message.equals("play") && (getRole() == ROLE.MOD || !queue.isAlreadyPlaying())) {
                if(queue.getSongsInQueue() == 0) {
                    reply("There aren't any songs in the queue. :(", true);
                    return true;
                }
                queue.play();
                reply( "♫♫♫♫", true);
                Log.log("User ["+event.getAuthor().getName()+"] issued command: play.");
                return true;
            }

            if (getRole() != ROLE.MOD) return false;

            if (message.equals("skip")) {
                queue.skip();
                reply("Skipping song...", true);
                Log.log("User ["+event.getAuthor().getName()+"] issued command: skip.");
                return true;
            }
            else if(message.equals("shuffle")) {
                queue.shuffle();
                reply("Shuffling queue. Queue contains "+queue.getSongsInQueue()+" song(s).", true);
                Log.log("User ["+event.getAuthor().getName()+"] issued command: shuffle.");
            }
            else if (message.equals("back")) {
                queue.back();
                reply( "Playing previous song...", true);
                Log.log("User ["+event.getAuthor().getName()+"] issued command: back");
                return true;
            } else if (message.equals("stop")) {
                queue.clear();
                reply( "Queue cleared", true);
                Log.log("User ["+event.getAuthor().getName()+"] issued command: stop");
                return true;
            } else if (message.equals("pause")) {
                queue.pause();
                reply( "Paused. Type resume to resume music.", true);
                Log.log("User ["+event.getAuthor().getName()+"] issued command: pause");
                return true;
            } else if(message.equals("resume")) {
                queue.resume();
                reply( "Resuming tunes...", true);
                Log.log("User ["+event.getAuthor().getName()+"] issued command: resume");
                return true;
            } else if (message.startsWith("!volume") || message.startsWith("!vol")) {
                int temp = queue.getVolume();
                queue.setVolume(Integer.parseInt(message.split("\\s+")[1]));
                reply( "Volume changed from "+temp+" to "+queue.getVolume(), true);
                Log.log("User ["+event.getAuthor().getName()+"] changed volume from "+temp+" to "+queue.getVolume()+".");
                return true;
            } else if (message.startsWith("!playnow")) {
                queue.addNext(new Track(message.split("\\s+")[1]));
                queue.skip();
                Log.log("User ["+event.getAuthor().getName()+"] issued command: playnow with url: "+message.split("\\s+")[1]);
                reply( "Playing song now.", true);
                return true;
            } else if(message.startsWith("!summon")) {
            }

        /* PLAYLIST MANAGEMENT */
            String[] tokens = message.split("\\s+");
            if (message.equals("!ls")) {
                ArrayList<PlaylistModel> playlists = settings.getPlaylists();
                if (playlists == null || playlists.size() == 0) {
                    reply( "No playlists found. Use !pcreate to create a playlist.", true);
                    return true;
                }
                String temp = "";
                for (int i = 0; i < playlists.size(); i++) {
                    temp += playlists.get(i).getName() + ", ";
                    if (i == playlists.size() - 1) temp = temp.substring(0, temp.length() - 2);
                }
                reply( "Found " + playlists.size() + " playlist(s): " + temp, true);
                Log.log("User ["+event.getAuthor().getName()+"] issued command: ls");
                return true;
            }
            else if(message.startsWith("!song?")) {
                Log.log("User ["+event.getAuthor()+" issued command: !song?");
                reply( "Song: "+queue.getTracks().get(queue.getCurrent()).getIdentifier(), true);
            }
            else if(message.startsWith("!search")) {
                //smartSummon( queue);
                Log.log("User ["+event.getAuthor().getName()+"] is searching YouTube with query: "+event.getMessage().getContent().substring(8));

                Track track = new SpotifyToYoutube().search(event.getMessage().getContent().substring(8));
                if(track == null) {
                    reply( "An error occurred when searching for the song. Check syntax of command or spelling of query.", true);
                    return true;
                } else {
                    queue.addNext(track);
                    queue.skip();
                    queue.play();
                    reply( "Found track, playing now...", true);
                }
            }
            else if (message.startsWith("!pcreate")) {
                if (tokens.length == 1) {
                    reply( "You must specify a playlist name.", true);
                    return true;
                } else if (tokens.length == 2) { // creating blank playlist
                    PlaylistModel pm = settings.getPlaylist(tokens[1]);
                    if(pm != null) {
                        reply( "Playlist "+tokens[1]+" already exists.", true);
                        return true;
                    }
                    settings.addPlaylist(tokens[1], new ArrayList<>());
                    reply( "Created playlist " + tokens[1] + ". Use !p <name> add <url> to start adding songs.", true);
                    new Loader().saveSettings(settings);
                    Log.log("User ["+event.getAuthor().getName()+"] created an empty playlist, "+tokens[1]+".");
                    return true;
                } else if (tokens[2].contains("spotify")) { // creating from Spotify playlist
                    PlaylistModel pm = settings.getPlaylist(tokens[1]);
                    if(pm != null) {
                        reply( "Playlist "+tokens[1]+" already exists.", true);
                        return true;
                    }
                    // Smart playlist getter
                    reply( "Accessing Spotify and YouTube databases. This will take about 30 seconds.", true);
                    ArrayList<Track> tracks = new SpotifyToYoutube().convert(tokens[2]);
                    if(tracks == null) {
                        reply( "Error occured while creating playlist. Please check syntax and try again.", true);
                        return true;
                    }
                    settings.addPlaylist(tokens[1], tracks);
                    reply( "Created playlist " + tokens[1] + " with " + tracks.size() + " songs. Use !p "+tokens[1]+ " to play your new playlist.", false);
                    Log.log("User ["+event.getAuthor().getName()+"] converted a Spotify playlist into a local YouTube playlist, "+tokens[1]+".");
                    new Loader().saveSettings(settings);
                    return true;
                } else if (tokens[2].equals("q")) {
                    PlaylistModel pm = settings.getPlaylist(tokens[1]);
                    if(pm != null) {
                        reply( "Playlist "+tokens[1]+" already exists.", true);
                        return true;
                    }
                    settings.addPlaylist(tokens[1], queue.getTracks());
                    reply( "Created playlist " + tokens[1] + " from current queue with " + queue.getSongsInQueue() + " tracks.", true);
                    new Loader().saveSettings(settings);
                    Log.log("User ["+event.getAuthor().getName()+"] created playlist "+tokens[1]+" from the current queue.");
                    return true;
                }
            } else if(message.startsWith("!iv")) {
                // Get a list of URLs
                TextChannel tc = event.getGuild().getTextChannelsByName("importantvideos", true).get(0);
                MessageHistory history = new MessageHistory(tc);
                List<Message> messages = history.retrievePast(100).complete();
                if(message.length() == 0) {
                    reply( "No videos found in text channel", true);
                    return true;
                }
                ArrayList<Track> temp = new ArrayList<>();
                if(messages != null && message.length() > 0) {
                    for(Message m : messages) { temp.add(new Track(m.getRawContent())); }
                }
                queue.clear();
                //smartSummon( queue);
                for (Track t : temp) queue.add(t);
                queue.play();
                reply( "Playing #importantvideos...", true);
                Log.log("User ["+event.getAuthor().getName()+"] started the important videos playlist.");
                return true;
            } else if (message.startsWith("!p ")) {
                if (tokens.length == 2) {
                    PlaylistModel pm = settings.getPlaylist(tokens[1]);
                    if (pm == null) {
                        reply( "Playlist " + tokens[1] + " does not exist.", true);
                        return true;
                    }
                    queue.clear();
                    //smartSummon( queue);
                    for (Track t : pm.getTracks()) queue.add(t);
                    queue.play();
                    reply( "Playing playlist " + tokens[1] + ".", true);
                    Log.log("User ["+event.getAuthor().getName()+"] started playlist "+tokens[1]+".");
                    return true;
                }
                else if (tokens.length == 3 && tokens[1].equals("del")) {
                    PlaylistModel pm = settings.getPlaylist(tokens[2]);
                    if (pm == null) {
                        reply( "Playlist " + tokens[2] + " does not exist.", true);
                        return true;
                    }
                    settings.remove(pm);
                    new Loader().saveSettings(settings);
                    reply( "Playlist " + tokens[2] + " was deleted.", true);
                    Log.log("User ["+event.getAuthor().getName()+"] delete playlist "+tokens[2]+".");
                    return true;
                }
                else if (tokens[2].equals("add") && tokens.length == 4) {
                    PlaylistModel pm = settings.getPlaylist(tokens[1]);
                    if (pm == null) {
                        reply( "Playlist " + tokens[1] + " does not exist.", true);
                        return true;
                    }
                    pm.addTrack(new Track(tokens[3]));
                    new Loader().saveSettings(settings);
                    reply( "1 song was added to playlist " + tokens[1] + ".", true);
                    Log.log("User ["+event.getAuthor().getName()+"] added url "+tokens[3]+" to playlist "+tokens[1]+".");
                    return true;
                }
            }

        } catch(Exception e) {
            e.printStackTrace();
            reply("Incorrect syntax. Type !help for help.", true);

            Log.logError("User ["+event.getAuthor().getName()+"] issued an incorrect command: "+message+" Error message: "+e.getMessage());
            return false;
        }

        return false;
    }

    @Override
    public boolean processVoiceCommand(final String command) {
        return false;
    }

}
