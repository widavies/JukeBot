package modules.music;

import net.dv8tion.jda.core.audio.AudioReceiveHandler;
import net.dv8tion.jda.core.audio.CombinedAudio;
import net.dv8tion.jda.core.audio.UserAudio;


/*
 * Handles voice recognition
 */
public class AudioPlayerReceiveHandler implements AudioReceiveHandler {

    public AudioPlayerReceiveHandler() {



    }

    @Override
    public boolean canReceiveCombined() {
        return true;
    }

    @Override
    public boolean canReceiveUser() {
        return false;
    }

    @Override
    public void handleCombinedAudio(CombinedAudio combinedAudio) {

    }

    @Override
    public void handleUserAudio(UserAudio userAudio) {

    }
}
