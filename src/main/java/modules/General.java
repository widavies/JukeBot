package modules;

import models.Loader;
import models.Settings;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageHistory;
import net.dv8tion.jda.core.entities.TextChannel;
import tools.Constants;
import tools.Log;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

public class General extends Mod {

    private Settings settings;

    @Override
    public boolean processCommand(String command) {
        String message = event.getMessage().getRawContent();

        if(message.equalsIgnoreCase("ping")) {
            reply("pong!", true);
            return true;
        }

        if(getRole() < ROLE.MOD) return false;

        if(message.equalsIgnoreCase("cleanup")) {
            for(TextChannel tc : event.getGuild().getTextChannels()) {
                MessageHistory history = new MessageHistory(tc);
                List<Message> messages = history.retrievePast(100).complete();
                if(messages != null) {
                    for(Message m : messages) { if(m.getAuthor().isBot()) m.delete().queue(); }
                }
            }
            event.getMessage().delete().queue();
            Log.log("User ["+event.getAuthor().getName()+"] requested a bot cleanup");
        }
        else if(message.startsWith("clear")) {
                MessageHistory history = new MessageHistory(event.getChannel());
                List<Message> messages = history.retrievePast(Integer.parseInt(message.split("\\s+")[1]) + 1).complete();
                if(messages != null) {
                    for (Message m : messages) {
                        m.delete().queue();
                    }
                }

            event.getMessage().delete().queue();
            Log.log("User ["+event.getAuthor().getName()+"] requested a text channel clear");
        }



        return false;
    }

    @Override
    public boolean processVoiceCommand(String command) {
        command = command.toLowerCase().trim();

        if(command.startsWith("kick")) {
            replyPrivately(command.split("\\s+")[1], Constants.SERVER_INVITE_LINK);
            kickUser(command.split("\\s+")[1]);
            return true;
        } else if(command.startsWith("message")) {
            replyPrivately(command.split("\\s+")[1], command.split("\\s+")[2]);
            return true;
        } else if(command.contains("clear messages")) {
            for(TextChannel tc : event.getGuild().getTextChannels()) {
                MessageHistory history = new MessageHistory(tc);
                List<Message> messages = history.retrievePast(20).complete();
                if(messages != null) {
                    for(Message m : messages) { m.delete().queue(); }
                }
            }
            event.getMessage().delete().queue();
            Log.log("User ["+event.getAuthor().getName()+"] requested a text channel cleanup");
            return true;
        }

        return false;
    }

}
