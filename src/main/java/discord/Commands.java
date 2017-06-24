package discord;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;
import files.IO;
import files.Loader;
import files.PlaylistModel;
import files.Settings;
import music.AudioPlayerReceiveHandler;
import music.AudioPlayerSendHandler;
import music.MasterQueue;
import music.Track;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.guild.GenericGuildMessageEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.AudioManager;
import tools.Constants;

import java.util.ArrayList;
import java.util.function.Consumer;

/*
 * Listens to channel commands processes them.
 *
 * This is basically the center of activity in this bot
 */
public class Commands extends ListenerAdapter {

    private MasterQueue queue;
    private LiveSpeechRecognizer recongizer;


    public Commands() {
        queue = new MasterQueue();
        Configuration configuration = new Configuration();
        configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
        configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
        configuration.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");

        try {
            recongizer = new LiveSpeechRecognizer(configuration);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        String message = event.getMessage().getRawContent();
        Settings settings = new Loader().getSettings();
        if(settings == null) new Loader().saveSettings(new Settings());
        if(message.startsWith("!ping")) {
            sendMessage(event, "pong!");
        }

        /* ADMIN ONLY COMMANDS */
        if(!isAdmin(event)) return;

        /* MUSIC COMMANDS */
        try {
            if(message.startsWith("!add")) {
                queue.add(new Track(message.split("\\s+")[1]));
                sendMessage(event, "Added song to queue. There are now "+queue.getSongsInQueue()+" songs in the queue.");
            }
            else if(message.startsWith("!l")) {
                System.out.println("Listening...");
                recongizer.startRecognition(true);
                SpeechResult result = recongizer.getResult();
                recongizer.stopRecognition();

                System.out.println("Prediction: "+result.getHypothesis());
            }
            else if(message.equals("!play")) {
                if(queue.getSongsInQueue() == 0) {
                    sendMessage(event, "There aren't any songs in the queue");
                    return;
                }
                VoiceChannel channel = event.getGuild().getMember(event.getAuthor()).getVoiceState().getChannel();
                if(channel != null) summon(channel);
                else summon(event.getGuild().getVoiceChannelsByName("radio", true).get(0));
                queue.play();
            }

            else if(message.equals("!clear") || message.equals("!stop")) {
                queue.clear();
                sendMessage(event, "Queue cleared. Queue contains 0 songs");
            }
            else if(message.equals("!skip")) {
                queue.skip();
                sendMessage(event, "Skipping song");
            }
            else if(message.equals("!back")) {
                queue.back();
                sendMessage(event, "Previous song");
            }
            else if(message.equals("!playnow")) {
                queue.addNext(new Track(message.split("\\s+")[1]));
                queue.skip();
                sendMessage(event, "Playing song now");
            }
            else if(message.equals("!pause")) {
                queue.pause();
                sendMessage(event, "JukeBot is paused, use !resume to resume");
            }
            else if(message.equals("!resume")) {
                VoiceChannel channel = event.getGuild().getMember(event.getAuthor()).getVoiceState().getChannel();
                if(channel != null) summon(channel);
                else summon(event.getGuild().getVoiceChannelById("Radio"));
                queue.resume();
                sendMessage(event,"Resuming music...");
            }
            else if(message.startsWith("!vol")) {
                queue.setVolume(Integer.parseInt(message.split("\\s+")[1]));
                sendMessage(event, "Set volume to: "+Integer.parseInt(message.split("\\s+")[1]));
            }
            else if(message.startsWith("!moan")) {
                VoiceChannel channel = event.getGuild().getMember(event.getAuthor()).getVoiceState().getChannel();
                if(channel != null) summon(channel);
                else summon(event.getGuild().getVoiceChannelById("Radio"));
                queue.addNext(new Track("https://www.youtube.com/watch?v=SNxYku74Q9s"));
                queue.skip();
            }
            /* PLAYLIST MANAGEMENT */
            else if(message.startsWith("!save")) {
                if(settings.doesExist(message.split("\\s+")[1])) {
                    sendMessage(event, "A playlist with that name already exists.");
                    return;
                }
                settings.addPlaylist(message.split("\\s+")[1], queue.getTracks());
                sendMessage(event, "Playlist "+message.split("\\s+")[1]+" saved with "+queue.getSongsInQueue()+" songs.");
            }
            else if(message.startsWith("!playlist")) {
                queue.clear();
                if(!settings.doesExist(message.split("\\s+")[1])) {
                    sendMessage(event, "The playlist "+message.split("\\s+")[1]+" does not exist");
                    return;
                }
                VoiceChannel channel = event.getGuild().getMember(event.getAuthor()).getVoiceState().getChannel();
                if(channel != null) summon(channel);
                else summon(event.getGuild().getVoiceChannelById("radio"));
                PlaylistModel playlist = settings.getPlaylist(message.split("\\s+")[1]);
                for(Track t : playlist.getTracks()) {
                    queue.addNext(t);
                }
                queue.play();
                sendMessage(event, "Playing playlist...");
            }
            else if(message.startsWith("!ls")) {
                ArrayList<PlaylistModel> playlists = settings.getPlaylists();
                sendMessage(event, "Listing playlists....");
                for(int i = 0; i < playlists.size(); i++) {
                    sendMessageNoDelete(event, "["+i+"] "+playlists.get(i).getName());
                }
            }
            else if(message.startsWith("delplaylist")) {
                if(!settings.doesExist(message.split("\\s+")[1])) {
                    sendMessage(event, "The playlist "+message.split("\\s+")[1]+" does not exist");
                    return;
                }
                PlaylistModel playlist = settings.getPlaylist(message.split("\\s+")[1]);
                settings.remove(playlist);
                sendMessage(event, "Deleted playlist successfully");
            }
            else if(message.equals("!help")) {
                event.getAuthor().openPrivateChannel().queue((channel) -> sendAndLog(channel, Constants.HELP_TEXT));
            }
            new Loader().saveSettings(settings);

        } catch(Exception e) {
            e.printStackTrace();
            sendMessage(event, "Incorrect syntax. Type !help for help.");
        }
    }

    // Returns true if the message was sent by an Admin
    public void summon(VoiceChannel channel) {
        AudioManager manager = channel.getGuild().getAudioManager();
        manager.setSendingHandler(new AudioPlayerSendHandler(queue.getPlayer()));
        manager.setReceivingHandler(new AudioPlayerReceiveHandler());
        manager.openAudioConnection(channel);
    }

    private boolean isAdmin(GuildMessageReceivedEvent event) {
        for(Role r : event.getGuild().getMember(event.getAuthor()).getRoles()) if(r.getName().equalsIgnoreCase("Admin")) return true;
        return false;
    }

    private void sendMessage(GuildMessageReceivedEvent event, String message) {
        MessageChannel channel = event.getChannel();
        channel.sendMessage(message).queue();
        event.getMessage().delete().queue();
    }

    private void sendMessageNoDelete(GuildMessageReceivedEvent event, String message) {
        MessageChannel channel = event.getChannel();
        channel.sendMessage(message).queue();
    }

    public void sendAndLog(MessageChannel channel, String message)
    {
        // Here we use a lambda expressions which names the callback parameter -response- and uses that as a reference
        // in the callback body -System.out.printf("Sent Message %s", response)-
        Consumer<Message> callback = (response) -> System.out.printf("Sent Message %s", response);
        channel.sendMessage(message).queue(callback); // ^ calls that
    }

}
