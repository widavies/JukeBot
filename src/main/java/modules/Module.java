package modules;

import modules.music.AudioPlayerReceiveHandler;
import modules.music.AudioPlayerSendHandler;
import modules.music.Queue;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.managers.AudioManager;

/**
 * JukeBot is modular. Any additional modules should
 * extend this class and control Discord with their own methods.
 *
 * @since v0.1
 * @author Will Davies
 */
public abstract class Module {

    protected final int DEFAULT = 1;
    protected final int MOD = 2;
    protected final int ADMIN = 3;

    /**
     * Processes a command from the console.
     * @param event the message received event, author, role, and message can be extracted from here
     * @return true if the command was relevant to this module and an action was performed
     */
    public abstract boolean processCommand(final GuildMessageReceivedEvent event);

    /**
     * Processes a voice command
     * @return true if the command was relevant to this module and an action was performed
     */
    public abstract boolean processVoiceCommand(final String command);

    /**
     * Replies to user who issued the command.
     * @param event the message receive event
     * @param message the message to send to the user
     * @param deleteOP whether to delete the original post
     */
    protected void reply(GuildMessageReceivedEvent event, String message, boolean deleteOP) {
        MessageChannel channel = event.getChannel();
        channel.sendMessage(message).queue();
        if(deleteOP) event.getMessage().delete().queue();
    }

    /**
     * Replies privately to the user who issued the command.
     * The command will be deleted.
     * @param event the message receive event
     * @param message the message to send to the user
     */
    protected void replyPrivately(GuildMessageReceivedEvent event, String message) {
        event.getAuthor().openPrivateChannel().queue((channel) -> send(channel, message));
        event.getMessage().delete().queue();
    }

    private void send(MessageChannel channel, String message) {
        channel.sendMessage(message).queue();
    }

    /**
     * Returns DEFAULT, MOD, or ADMIN of the user who sent the message.
     * If the user has multiple roles, the highest role is returned.
     * @param event the message receive event
     * @return the role of the author
     */
    protected int getRole(GuildMessageReceivedEvent event) {
        int temp = 0;
        for(Role r : event.getMember().getRoles()) {
            if(r.getName().equals("Admin")) return ADMIN;
            else if(r.getName().equals("Mod")) temp = MOD;
            else if(temp == 0) temp = DEFAULT;
        }
        return temp;
    }

    /**
     * Joins the current user's channel, otherwise the Radio channel.
     *
     * @param event
     */
    protected void smartSummon(GuildMessageReceivedEvent event, Queue queue) {
        VoiceChannel channel = event.getGuild().getMember(event.getAuthor()).getVoiceState().getChannel();
        if(channel != null) summon(channel, queue);
        else summon(event.getGuild().getVoiceChannelsByName("radio", true).get(0), queue);
    }

    /**
     * Joins the voice channel with the specified name
     * @param event
     * @param name
     */
    protected void summonByName(GuildMessageReceivedEvent event, Queue queue, String name) {
        if(event.getGuild().getVoiceChannels() == null || event.getGuild().getVoiceChannels().size() == 0) return;
        for(VoiceChannel vc : event.getGuild().getVoiceChannels()) {
            if(vc.getName().equalsIgnoreCase(name)){
                summon(vc, queue);
                reply(event, "Connecting to voice channel: "+name+".", true);
                return;
            }
        }
        reply(event, "Couldn't find a voice channel named "+name+".", true);
    }

    /**
     * Summons the bot to the specified channel
     * @param channel
     */
    private void summon(VoiceChannel channel, Queue queue) {
        AudioManager manager = channel.getGuild().getAudioManager();
        manager.setSendingHandler(new AudioPlayerSendHandler(queue.getPlayer()));
        manager.setReceivingHandler(new AudioPlayerReceiveHandler());
        manager.openAudioConnection(channel);
    }
}
