package main;

import models.Loader;
import models.Settings;
import modules.Module;
import modules.general.General;
import modules.music.Music;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import tools.Log;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import tools.Constants;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;

/**
 * Top level class, start bot here.
 *
 * @since v0.1
 * @author Will Davies
 */

public class Main extends ListenerAdapter {

    private final ArrayList<Module> modules;

    public Main() {
        Settings settings = new Loader().getSettings();
        if(settings == null) {
            new Loader().saveSettings(new Settings());
            Log.log("Settings file doesn't exist, creating one.");
        }

        modules = new ArrayList<>();
        modules.add(new General());
        modules.add(new Music());
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        for(Module m : modules) m.processCommand(event);
    }

    public static void main(String[] args) {
        Log.log("Starting JukeBot...");
        try {
            new JDABuilder(AccountType.BOT).setToken(Constants.DISCORD_TOKEN).addEventListener(new Main()).buildBlocking();
        } catch (InterruptedException e) {
            Log.log("Interrupted exception.");
        } catch (RateLimitedException e) {
            Log.logError("Rate limited exception.");
        } catch (LoginException e) {
            Log.logError("Failed to login.");
        }
    }

}
