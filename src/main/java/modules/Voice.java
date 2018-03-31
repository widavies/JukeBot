package modules;

import com.cpjd.speechGeneration.SilenceAudioSendHandler;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.managers.AudioManager;
import voice.SpeechCallback;
import voice.SpeechReceiver;

import java.util.ArrayList;

public class Voice extends Mod {

    private SpeechReceiver speechReceiver;

    private ArrayList<Mod> modules;

    public Voice(ArrayList<Mod> modules) {
        this.modules = modules;
    }

    @Override
    public boolean processCommand(String command) {
        if(command.equalsIgnoreCase("summon")) {
            VoiceChannel channel = event.getGuild().getVoiceChannelsByName("General 1", true).get(0);
            AudioManager manager = channel.getGuild().getAudioManager();
            manager.setSendingHandler(new SilenceAudioSendHandler());

            speechReceiver = new SpeechReceiver("okay bought", new SpeechCallback() {
                @Override
                public void commandReceived(String command) {
                    for(Mod mod : modules) {
                        if(mod.processCommand(command)) break;
                    }
                }

                @Override
                public boolean botAwakeRequest(User... users) {
                    for(User u : users) {
                        for(Role r : (event.getGuild().getMemberById(u.getId()).getRoles())) {
                            if(r.getName().equalsIgnoreCase("Admin") || r.getName().equalsIgnoreCase("Mod")) {
                                reply("/tts bot is woke", false);
                                return true;
                            }
                        }
                    }

                    return false;
                }
            });

            speechReceiver.setCombinedAudio(true);
            speechReceiver.setVoiceCommandTimeout(2);

            manager.setReceivingHandler(speechReceiver);

            manager.openAudioConnection(channel);
            return true;
        }

        return false;
    }

    @Override
    public boolean processVoiceCommand(String command) {
        return false;
    }
}
