package modules.lanutils;

import models.Loader;
import models.Settings;
import modules.Module;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import tools.Log;

import java.util.ArrayList;

public class LAN extends Module {

    private Settings settings;
    private String activeGame = "";

    /**
     * The active match.
     *
     * Entries should be in the format of
     * discordID:team
     */
    private MatchFinder.Match activeMatch;
    private ArrayList<MatchFinder.Match> results = new ArrayList<>();

    /**
     * This is the current game queue.
     *
     * Entries should already match in game, and be in the format player:skill:friendlyName
     */
    private ArrayList<String> queue = new ArrayList<>();

    public LAN() {
        super("LAN-Utils");
        settings = new Loader().getSettings();
    }

    @Override
    public boolean processCommand(GuildMessageReceivedEvent event) {
        String message = event.getMessage().getRawContent();
        String[] tokens = message.split("\\s+");

        if(message.startsWith("*lan")) {
            // We need to return team & ip string
            if(activeMatch != null) {
                for(String s : activeMatch.t1_players) {
                    if(s.equals(event.getAuthor().getName())) {
                        reply(event, "You are on "+activeMatch.team1Name+". Use this to connect: "+settings.getSetip(), true);
                        Log.log("User ["+event.getAuthor().getName()+"] issued command *lan. Response: "+"You are on "+activeMatch.team1Name+". Use this to connect: "+settings.getSetip());
                        return true;
                    }
                }
                for(String s : activeMatch.t2_players) {
                    if(s.equals(event.getAuthor().getName())) {
                        reply(event, "You are on "+activeMatch.team2Name+". Use this to connect: "+settings.getSetip(), true);
                        Log.log("User ["+event.getAuthor().getName()+"] issued command *lan. Response: "+"You are on "+activeMatch.team1Name+". Use this to connect: "+settings.getSetip());
                        return true;
                    }
                }
                reply(event, "You're not in the match. Contact Will or Isaac to be added.", true);
                Log.log("User ["+event.getAuthor().getName()+"] unsuccessfully used *lan");
                return true;
            }
        }

        if(getRole(event) < ADMIN) return false;

        try {
            if(message.startsWith("*dat add")) {
                try {
                    String discordID;
                    try {
                        discordID = event.getGuild().getMembersByName(tokens[1], true).get(0).getUser().getId();
                    } catch(Exception e) {
                        discordID = event.getGuild().getMembersByNickname(tokens[1], true).get(0).getUser().getId();
                    }
                    boolean result = settings.addEntry(tokens[2], discordID, message.split("\\s+")[3], message.split("\\s+")[4]);
                    if(!result) reply(event, "Added player "+tokens[2]+" with skill "+tokens[4]+" / 1000 in game "+tokens[3]+" to database.", true);
                    else reply(event, "Entry already existed. Skill updated to "+tokens[4]+" / 1000 in game "+tokens[3], true);
                    Log.log("User ["+event.getAuthor().getName()+"] added player "+tokens[2]+" with skill "+tokens[4]+" / 1000 in game "+tokens[3]+" to database. Overwrote? "+result);
                    new Loader().saveSettings(settings);
                    return true;
                } catch(Exception e) {
                    reply(event, "Could not find player "+tokens[1]+". Double check their Discord username.", true);
                    return true;
                }
            }
            else if(message.startsWith("*tol")) {
                settings.setTolerance(Integer.parseInt(tokens[1]));
                reply(event, "Set tolerance to "+settings.getTolerance()+".", true);
                Log.log("User ["+event.getAuthor().getName()+"] set the tolerance to "+settings.getTolerance()+".");
                new Loader().saveSettings(settings);
                return true;
            }
            else if(message.startsWith("*setip")) {
                settings.setSetip(tokens[1]);
                reply(event, "IP connect command set to "+settings.getSetip()+".", true);
                Log.log("User ["+event.getAuthor().getName()+"] set the IP to "+settings.getSetip()+".");
                new Loader().saveSettings(settings);
                return true;
            }
            else if(message.startsWith("*game")) {
                activeGame = tokens[1];
                reply(event, "Set active game to "+activeGame+".", true);
                Log.log("User ["+event.getAuthor().getName()+"] set the active game to "+activeGame+".");
                return true;
            }
            else if(message.equals("*clear")) {
                queue.clear();
                reply(event, "Lan-utils queue cleared.", true);
                Log.log("User ["+event.getAuthor().getName()+"] cleared the lan-utils queue");
                return true;
            }
            else if(message.equals("*qls")) {
                String temp = "Players in queue: \n";
                for(String s : queue) {
                    temp += event.getGuild().getMemberById(s.split(":")[0]).getUser().getName()+"\n";
                }
                reply(event, temp, true);
                Log.log("User ["+event.getAuthor().getName()+"] issued command *qls");
                return true;
            }
            else if(message.startsWith("*add")) {
                if(activeGame.equals("")) {
                    reply(event, "No active game is set. Set it with *game <game>.", true);
                    Log.log("User ["+event.getAuthor().getName()+"] issued command *add without active game set.");
                    return true;
                }
                String discordID;
                try {
                    discordID = event.getGuild().getMembersByName(tokens[1], true).get(0).getUser().getId();
                } catch(Exception e) {
                    discordID = event.getGuild().getMembersByNickname(tokens[1], true).get(0).getUser().getId();
                }
                // First check if user is already in queue
                if(queue.size() > 0) {
                    for(String s : queue) {
                        if(s.split(":")[0].equals(discordID)) {
                            reply(event, "Player "+tokens[1]+" is already in the queue.", true);
                            return true;
                        }
                    }
                }
                queue.add(discordID+":"+settings.getSkill(discordID, activeGame)+":"+tokens[1]);
                reply(event, "Player "+tokens[1]+" was successfully added to the queue.", true);
                Log.log("User ["+event.getAuthor().getName()+"] added player "+tokens[1]+" ("+discordID+") to queue with skill "+settings.getSkill(discordID, activeGame)+" / 1000 in game "+activeGame+".");
                return true;
            }
            else if(message.startsWith("*find")) {
                if(queue.size() == 0) {
                    reply(event, "There are no players in the queue. First set active game with *game <game>, then add players with *add <player>", true);
                    return true;
                }

                if(tokens.length == 1) results = MatchFinder.find(queue, settings.getTolerance(), -1, activeGame);
                else results = MatchFinder.find(queue, settings.getTolerance(), Integer.parseInt(tokens[1]), activeGame);
                for(int i = 0; i < results.size(); i++) {
                    String reply = "```\nMatch #"+i+"\nTeam 1 Skill: "+ results.get(i).t1_totalSkill+"\nTeam 2 Skill: "+results.get(i).t2_totalSkill;
                    String t1 = "\nTeam 1:";
                    for (int j = 0; j < results.get(i).t1_players.size(); j++) {
                        if(j == results.get(i).t1_players.size() - 1) t1 += " "+results.get(i).t1_players.get(j);
                        else t1 += " "+results.get(i).t1_players.get(j) + ", ";
                    }
                    reply += t1;
                    String t2 = "\nTeam 2:";
                    for (int j = 0; j < results.get(i).t2_players.size(); j++) {
                        if(j == results.get(i).t2_players.size() - 1) t2 += " "+results.get(i).t2_players.get(j);
                        else t2 += " "+results.get(i).t2_players.get(j) + ", ";
                    }
                    reply += t2 + "```";
                    reply(event, reply, false);
                }
                if(results.size() == 0) {
                    reply(event, "No matches. Try increasing match tolerance.", true);
                }

                Log.log("User ["+event.getAuthor().getName()+"] issued command *find and got "+results.size()+" results.");
                return true;
            }
            else if(message.startsWith("*choose")) {
                if(results.size() == 0) {
                    reply(event, "No matches have been found yet. Use *find to get started with finding matches.", true);
                    return true;
                }
                try {
                    activeMatch = results.get(Integer.parseInt(tokens[1]));
                    reply(event, "Active match set to #"+tokens[1]+".", true);
                    Log.log("User ["+event.getAuthor().getName()+"] choose match #"+tokens[1]+" as the active match.");
                } catch(Exception e) {
                    reply(event, "Invalid match number, please select a match number between 0 and "+(results.size()-1)+".", true);
                    Log.log("User ["+event.getAuthor().getName()+"] issued command *choose, but didn't specified the number.");
                    return true;
                }
                return true;
            }
            else if(message.equals("*ls")) {
                if(settings.getLanDatabase().size() == 0) {
                    reply(event, "There are no entries in the lan database.", true);
                    Log.log("User ["+event.getAuthor().getName()+"] issued command *ls, database was empty");
                    return true;

                }
                reply(event, settings.getDatabase(event), true);
                Log.log("User ["+event.getAuthor().getName()+"] issued command *ls");
                return true;
            }
            else if(message.equals("*end")) {
                results.clear();
                activeMatch = null;
                activeGame = "";
                reply(event, "Hard reset lan-utitilies module.", true);
                Log.log("User ["+event.getAuthor().getName()+"] issued command *end");
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
}
