package main;

import models.Loader;
import models.Settings;
import modules.Mod;
import modules.General;
import modules.Voice;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import tools.Constants;
import tools.Log;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;

/**
 * Top level class, start bot here.
 *
 * @since v0.1
 * @author Will Davies
 */

public class Main extends ListenerAdapter {

    private final ArrayList<Mod> modules;

    public Main() {
        Settings settings = new Loader().getSettings();
        if(settings == null) {
            new Loader().saveSettings(new Settings());
            Log.log("Settings file doesn't exist, creating one.");
        }

        modules = new ArrayList<>();
        modules.add(new General());
        modules.add(new Voice(modules));
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        for(Mod m : modules) {
            /*
             * Channel management
             */
            String textChannel = event.getChannel().getName().toLowerCase();

            if((textChannel.equals("videos") && !event.getMessage().getRawContent().contains("youtube.com"))
                    || (textChannel.equals("reddit") && !event.getMessage().getRawContent().contains("reddit.com"))
                    || (textChannel.equals("songs") && !event.getMessage().getRawContent().contains("spotify.com"))) {
                event.getMessage().delete().queue();
                return;
            }

            m.setEvent(event);

            try {
                m.processCommand(event.getMessage().getRawContent());
            } catch(Exception e) {
                Log.logError("Failed to process a command in a module.");
            }

        }
    }

    public static void main(String[] args) {
        Log.log("Starting JukeBot v0.2");
        try {
            new JDABuilder(AccountType.BOT).setToken(Constants.TOKEN).addEventListener(new Main()).buildBlocking();
        } catch (InterruptedException e) {
            Log.log("Interrupted exception.");
        } catch (RateLimitedException e) {
            Log.logError("Rate limited exception.");
        } catch (LoginException e) {
            Log.logError("Failed to login.");
        }
    }
}
