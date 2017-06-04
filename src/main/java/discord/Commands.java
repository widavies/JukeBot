package discord;

import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.guild.GenericGuildMessageEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.AudioManager;

/*
 * Listens to channel commands processes them.
 *
 * This is basically the center of activity in this bot
 */
public class Commands extends ListenerAdapter {

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        String message = event.getMessage().getRawContent();

        if(message.startsWith("!ping")) {
            sendMessage(event, "pong!");
        }

        /* ADMIN ONLY COMMANDS */
        if(!isAdmin(event)) return;

        /* MUSIC COMMANDS */



    }

    // Returns true if the message was sent by an Admin
    private boolean isAdmin(GuildMessageReceivedEvent event) {
        for(Role r : event.getGuild().getMember(event.getAuthor()).getRoles()) if(r.getName().equalsIgnoreCase("Admin")) return true;
        return false;
    }

    private void sendMessage(GuildMessageReceivedEvent event, String message) {
        MessageChannel channel = event.getChannel();
        channel.sendMessage(message).queue();
        event.getMessage().delete().queue();
    }

}