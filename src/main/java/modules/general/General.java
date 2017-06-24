package modules.general;

import models.Loader;
import models.Settings;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageHistory;
import net.dv8tion.jda.core.entities.TextChannel;
import tools.Log;
import modules.Module;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.io.IOUtils;

import java.time.OffsetDateTime;
import java.util.List;

public class General extends Module {

    private Settings settings;
    private long startTime;

    public General() {
        super("General");
        settings = new Loader().getSettings();

        startTime = System.currentTimeMillis();
    }

    @Override
    public boolean processCommand(GuildMessageReceivedEvent event) {
        String message = event.getMessage().getRawContent();
        try {
            if(message.equals("!ping")) {
                reply(event, "ping", true);
                return true;
            } else if(message.equals("!help")) {
                try {
                    if(getRole(event) == ADMIN) {
                        String help = IOUtils.toString(this.getClass().getResourceAsStream("/help.txt"), "UTF-8");
                        String help2 = IOUtils.toString(this.getClass().getResourceAsStream("/help2.txt"), "UTF-8");
                        replyPrivately(event, help);
                        replyPrivately(event, help2);
                    } else if(getRole(event) == MOD) {
                        String help = IOUtils.toString(this.getClass().getResource("/mod.txt"), "UTF-8");
                        replyPrivately(event, help);
                    } else {
                        String help = IOUtils.toString(this.getClass().getResource("/default.txt"), "UTF-8");
                        replyPrivately(event, help);
                    }

                } catch(Exception e) {
                    Log.logError("Failed to load help.txt. Is it missing? Error message: "+e.getMessage());
                }
                return true;
            } else if(message.startsWith("!addinsult")) {
                settings.addInsult(message.split(",")[1]);
                new Loader().saveSettings(settings);
                reply(event, "Insult added to insult database.", true);
                return true;
            } else if(message.startsWith("!insult")) {
                replyPrivatelyToUser(event, message.split("\\s+")[1], settings.getInsult()+" - "+event.getAuthor().getName());
                return true;
            } else if(message.equals("!uptime")) {
                double time = ((double)(System.currentTimeMillis() - startTime) / 1000) / 60 / 60;
                reply(event, "JukeBot has been running for "+time+" hrs.", true);
                return true;
            }

            if(getRole(event) < MOD) return false;


            if(message.startsWith("!clear")) {
                for(TextChannel tc : event.getGuild().getTextChannels()) {
                    MessageHistory history = new MessageHistory(tc);
                    List<Message> messages = history.retrievePast(Integer.parseInt(message.split("\\s+")[1])).complete();
                    if(messages != null && message.length() > 0) {
                        for(Message m : messages) { if(m.getCreationTime().isAfter(OffsetDateTime.now().minusMinutes(20)) && m.getAuthor().getName().equalsIgnoreCase(message.split("\\s+")[1])) m.delete().queue(); }
                    }
                }
            }

            else if(message.equals("!cleanup")) {
                for(TextChannel tc : event.getGuild().getTextChannels()) {
                    MessageHistory history = new MessageHistory(tc);
                    List<Message> messages = history.retrievePast(20).complete();
                    if(messages != null && message.length() > 0) {
                        for(Message m : messages) { if(m.getAuthor().isBot()) m.delete().queue(); }
                    }
                }
        }
        else if(message.equals("!dump")) {
            replyPrivately(event, Log.getMessages());
            return true;
        }


        } catch(Exception e) {
            e.printStackTrace();
            reply(event, "Incorrect syntax. Type !help for help.", true);
            Log.logError("User ["+event.getAuthor().getName()+"] issued an incorrect command: "+message);
            return false;
        }
        return false;
    }

    @Override
    public boolean processVoiceCommand(String command) {
        return false;
    }
}
