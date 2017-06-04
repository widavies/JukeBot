package main;

import discord.Commands;
import logging.Log;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import tools.Constants;

import javax.security.auth.login.LoginException;

/* Design specifications - Will
 *
 * -We need a logger class so we can keep track of events happening on the server, logger classe needs to save logs to a file
 * -This bot will run on a Raspberry PI, it needs to be stable and bug free so it can run forever, I don't wanna deal with
 * rebooting, etc.
 * -The bot will be moduler, seperate functions by packages and link them with the Commands class
 *
 */


/*
 * TODO:
 *
 * -Method that a String of how long the bot has been running
 * -Method that returns system stats (Raspberry PI)
 * -IO class that can serialiaze and deserialize a Settings object
 * -Playlist & Track models for serialization
 * -Logger utility (can save files to Settings model)
 *
 *
 *
 *
 *
 */

public class Main {

    public static void main(String[] args) {
        Log.log("Starting JukeBot...");
        try {
            new JDABuilder(AccountType.BOT).setToken(Constants.DISCORD_TOKEN).addEventListener(new Commands()).buildBlocking();
        } catch (InterruptedException e) {
            Log.log("Interrupted exception.");
        } catch (RateLimitedException e) {
            Log.logError("Rate limited exception.");
        } catch (LoginException e) {
            Log.logError("Failed to login.");
        }
    }

}
