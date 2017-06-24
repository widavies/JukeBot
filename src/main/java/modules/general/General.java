package modules.general;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import tools.Log;
import modules.Module;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.io.IOUtils;

import java.time.OffsetDateTime;

/**
 * Created by Will Davies on 6/24/2017.
 */
public class General extends Module {
    @Override
    public boolean processCommand(GuildMessageReceivedEvent event) {
        String message = event.getMessage().getRawContent();

        try {

        if(message.equals("!ping")) {
            reply(event, "ping", true);
            return true;
        }
        else if(message.equals("!help")) {
            try {
                String help = IOUtils.toString(this.getClass().getResourceAsStream("/help.txt"), "UTF-8");
                replyPrivately(event, help);
            } catch(Exception e) {
                Log.logError("Failed to load help.txt. Is it missing?");
            }
            return true;
        }

        if(getRole(event) < MOD) return false;

        if(message.startsWith("!clear")) {
            Log.log("clearing");
            for(Message m : event.getGuild().getTextChannelsByName(message.split("\\s+")[1], true).get(0).getHistory().getRetrievedHistory()) {
                if(m.getAuthor().toString().equalsIgnoreCase(message.split("\\s+")[1]) && !(m.getCreationTime().isBefore(OffsetDateTime.now().minusMinutes(20)))) {
                    event.getGuild().getTextChannelsByName(message.split("\\s+")[1], true).get(0).deleteMessageById(m.getId()).queue();
                }
            }
        }

        if(message.startsWith("!cleanup")) {
            for(TextChannel tc : event.getGuild().getTextChannels()) {
                for(Message m : tc.getHistory().getRetrievedHistory()) {
                    if(m.getAuthor().isBot()) {
                        tc.deleteMessageById(m.getId()).queue();
                    }
                }
            }


        }

        } catch(Exception e) {
            e.printStackTrace();
            reply(event, "Incorrect syntax. Type !help for help.", true);
            return false;
        }


        return false;
    }

    @Override
    public boolean processVoiceCommand(String command) {
        return false;
    }
}
