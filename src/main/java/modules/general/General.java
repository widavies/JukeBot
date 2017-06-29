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

import java.io.File;
import java.io.FileInputStream;
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
                    File currentDir = new File(General.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
                    File f1 = new File(currentDir.getParentFile().getAbsoluteFile() + "/help.txt");
                    String[] help = IOUtils.toString(new FileInputStream(f1), "UTF-8").split(":");
                    if(getRole(event) == ADMIN) {
                        replyPrivately(event, help[6]);
                        replyPrivately(event, help[8]);
                    } else if(getRole(event) == MOD) replyPrivately(event, help[4]);
                     else replyPrivately(event, help[2]);


                    Log.log("User ["+event.getAuthor().getName()+"] requested help file. Role: "+getRole(event)+".");
                } catch(Exception e) {
                    Log.logError("Failed to load help.txt. Is it missing? Error message: "+e.getMessage());
                }
                return true;
            } else if(message.startsWith("!addinsult")) {
                settings.addInsult(message.split(":")[1]);
                new Loader().saveSettings(settings);
                reply(event, "Insult added to insult database.", true);
                Log.log("User ["+event.getAuthor().getName()+"] add insult "+message.split(":")[1]+" to the database.");
                return true;
            } else if(message.startsWith("!insult")) {
                String insult = settings.getInsult();
                if(replyPrivatelyToUser(event, message.split("\\s+")[1], insult+" - JukeBot")) {
                    reply(event, "Insult sent!", false);
                    Log.log("User ["+event.getAuthor().getName()+"] sent insult ["+insult+"] to "+message.split("\\s+")[1]);
                }
                else reply(event, "User could not be found.", false);
                return true;
            } else if(message.equals("!uptime")) {
                double time = ((double)(System.currentTimeMillis() - startTime) / 1000) / 60 / 60;
                reply(event, "JukeBot has been running for "+round(time, 3)+" hrs.", true);
                Log.log("User ["+event.getAuthor().getName()+"] request uptime, responding "+round(time, 3)+" hrs.");
                return true;
            }

            if(getRole(event) < MOD) return false;

            if(message.startsWith("!clear")) {
                TextChannel tc = event.getMessage().getTextChannel();
                    MessageHistory history = new MessageHistory(tc);
                    List<Message> messages = history.retrievePast(Integer.parseInt(message.split("\\s+")[1])).complete();
                    if(messages != null && message.length() > 0) {
                        for(Message m : messages) { m.delete().queue(); }
                    }
                event.getMessage().delete();
                Log.log("User ["+event.getAuthor().getName()+"] requested to clear "+message.split("\\s+")[1]+" messages from text channel "+tc.getName()+".");
            }

            else if(message.equals("!cleanup")) {
                for(TextChannel tc : event.getGuild().getTextChannels()) {
                    MessageHistory history = new MessageHistory(tc);
                    List<Message> messages = history.retrievePast(100).complete();
                    if(messages != null && message.length() > 0) {
                        for(Message m : messages) { if(m.getAuthor().isBot()) m.delete().queue(); }
                    }
                }
                event.getMessage().delete();
                Log.log("User ["+event.getAuthor().getName()+"] requested a bot cleanup");
                return true;
        }
        else if(message.equals("!dump")) {
            replyPrivately(event, Log.getMessages());
            return true;
        }

        } catch(Exception e) {
            reply(event, "Incorrect syntax. Type !help for help.", true);
            Log.logError("User ["+event.getAuthor().getName()+"] issued an incorrect command: "+message+" Error message: "+e.getMessage());
            return false;
        }
        return false;
    }

    @Override
    public boolean processVoiceCommand(String command) {
        return false;
    }

    private static double round (double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }
}
