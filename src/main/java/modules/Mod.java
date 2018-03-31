package modules;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.util.ArrayList;
import java.util.List;

public abstract class Mod {

    protected GuildMessageReceivedEvent event;

    public abstract boolean processCommand(String command);

    public abstract boolean processVoiceCommand(String command);

    protected int getRole() {
        for(Role r : event.getMember().getRoles()) {
            if(r.getName().equals("Admin")) return ROLE.ADMIN;
            else if(r.getName().equals("Mod")) return ROLE.MOD;
        }
        return ROLE.DEFAULT;
    }

    protected void reply(String message, boolean deleteOP) {
        MessageChannel channel = event.getChannel();
        channel.sendMessage(message).queue();
        if(deleteOP) event.getMessage().delete().queue();
    }

    protected void replyToBot(String message) {
        MessageChannel channel = event.getGuild().getTextChannelsByName("bot", true).get(0);
        channel.sendMessage(message).queue();
    }

    protected void replyPrivately(String user, String message) {
        List<Member> members = event.getGuild().getMembers();

        for(Member m : members) {
            if(m.getNickname().equalsIgnoreCase(user)) {
                m.getUser().openPrivateChannel().queue((channel) -> send(channel, message));
                return;
            }
        }
    }

    protected void kickUser(String user) {
        List<Member> members = event.getGuild().getMembers();

        for(Member m : members) {
            if(m.getNickname().equalsIgnoreCase(user)) {
                event.getGuild().getController().kick(m).queue();
                return;
            }
        }
    }

    public abstract static class ROLE {
        public static int ADMIN = 3;
        public static int MOD = 2;
        public static int DEFAULT = 1;
    }

    public void setEvent(GuildMessageReceivedEvent event) {
        this.event = event;
    }

    private void send(MessageChannel channel, String message) {
        channel.sendMessage(message).queue();
    }

}
