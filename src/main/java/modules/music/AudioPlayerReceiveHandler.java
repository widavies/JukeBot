package modules.music;

import ie.corballis.sox.SoXEncoding;
import ie.corballis.sox.Sox;
import ie.corballis.sox.WrongParametersException;
import net.dv8tion.jda.core.audio.AudioReceiveHandler;
import net.dv8tion.jda.core.audio.CombinedAudio;
import net.dv8tion.jda.core.audio.UserAudio;
import tools.Log;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


/*
 * Handles voice recognition
 */
public class AudioPlayerReceiveHandler implements AudioReceiveHandler {

    private byte[] toBeProccessed;
    private int count;

    public AudioPlayerReceiveHandler() {


    }

    @Override
    public void handleCombinedAudio(CombinedAudio combinedAudio) {
        if(toBeProccessed == null) { // first frame
            toBeProccessed = combinedAudio.getAudioData(1.0);
        } else { // other frame
            byte[] old = toBeProccessed;
            byte[] audio = combinedAudio.getAudioData(1.0);
            byte[] toBeProcessed = new byte[old.length + audio.length];
            for(int i = 0; i < old.length; i++) toBeProcessed[i] = old[i];
            for(int i = old.length; i < old.length + audio.length; i++) toBeProcessed[i] = audio[i - old.length];
            count++;
            if(count >= 150) {
                try {
                    AudioInputStream is = AudioSystem.getAudioInputStream(new ByteArrayInputStream(toBeProcessed));
                    System.out.println(is.getFormat().getSampleRate());

                } catch(Exception e) {
                    e.printStackTrace();
                }


                count = 0;
                toBeProcessed = null;
            }
        }


    }

    @Override
    public void handleUserAudio(UserAudio userAudio) {

    }

    @Override
    public boolean canReceiveCombined() {
        return true;
    }

    @Override
    public boolean canReceiveUser() {
        return false;
    }

}
