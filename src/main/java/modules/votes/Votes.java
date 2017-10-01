package modules.votes;

import modules.Module;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import tools.Log;

import java.util.ArrayList;

public class Votes extends Module {

    private Poll poll;

    public Votes() {
        super("Votes");
    }

    @Override
    public boolean processCommand(GuildMessageReceivedEvent event) {
        String message = event.getMessage().getRawContent();
        String[] tokens = message.split(",");

        if(message.startsWith("!cast")) {
            if(poll == null || !poll.open) {
                reply(event, "There aren't any open polls right now. :(", true);
                return true;
            }

            int vote = Integer.parseInt(message.split("\\s+")[1]);

            if(vote > poll.items.size() || vote < 1) {
                reply(event, "Invalid vote ID, please choose a valid ID between 1 and "+poll.items.size()+".", true);
                return true;
            }
            // Check if the user has already voted
            if(poll.hasVoted(event.getAuthor().getId())) {
                reply(event, "You've already voted for this poll.", true);
                return true;
            }

            // Perform vote
            poll.items.get(vote - 1).upvote();
            poll.voters.add(event.getAuthor().getId());
            reply(event, event.getAuthor().getName()+"'s vote has been recorded.", true);
            Log.log("User ["+event.getAuthor().getName()+"] voted for item # "+(vote - 1)+".");
        }

        if(getRole(event) < MOD) return false;
        try {

            if (message.startsWith("!register")) {
                poll = new Poll();
                poll.title = tokens[1];
                String voteString = "";
                for(int i = 0; i < tokens.length - 2; i++) {
                    poll.items.add(new Item((i + 1)+") "+tokens[i + 2]));
                    if(i == tokens.length - 1) voteString += (i + 1) + ") "+ tokens[i + 2] +"\n";
                    else voteString += (i + 1) + ") "+ tokens[i + 2] +"\n";
                }
                reply(event, "@everyone "+event.getAuthor().getName()+" opened poll \""+tokens[1]+"\"\n"+voteString+"\n*Vote with !cast <#>*", true);
                Log.log("User ["+event.getAuthor().getName()+"] opened a poll.");
                return true;
            }
            else if(message.startsWith("!close")) {
                if(poll != null) poll.open = false;
                reply(event, "Poll closed", true);
                Log.log("User ["+event.getAuthor().getName()+"] closed a poll.");
                return true;
            }
            else if(message.startsWith("!results")) {
                if(poll != null) {
                    poll.open = false;
                    String response = "Results of poll \""+poll.title+"\""+"\n";
                    if(poll.getWinner(event) != null) {
                        response += poll.getWinner(event).name.substring(3) + " was the winner with "+poll.getWinner(event).votes+" votes";
                        for(Item i : poll.items) {
                            if(i.name.equals(poll.getWinner(event).name)) continue;
                            response += "\n"+i.name.substring(3)+" had "+i.votes+" votes.";
                        }
                    } else return true;
                    reply(event, response, true);
                    Log.log("User ["+event.getAuthor().getName()+"] closed a poll and displayed results.");
                    return true;
                } else {
                    reply(event, "Couldn't get results, no poll found.", true);
                    Log.log("User ["+event.getAuthor().getName()+"] tried to get results from an empty poll.");
                    return true;
                }
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

    public class Poll {
        public String title;
        public boolean open;
        public ArrayList<Item> items;
        public ArrayList<String> voters;

        public Poll() {
            items = new ArrayList<>();
            this.voters = new ArrayList<>();
            open = true;
        }

        public boolean hasVoted(String ID) {
            for(String s : voters) {
                if(s.equals(ID)) return true;
            }
            return false;
        }

        public Item getWinner(GuildMessageReceivedEvent event) {
            // find max
            int max = 0;
            for(Item i : items) {
                if(i.votes > max) max = i.votes;
            }

            // check for ties
            int count = 0;
            for(Item i : items) {
                if(i.votes == max) {
                    count++;
                }
            }

            // output if tie
            if(count > 1) {
                String item = "";
                for(int i = 0; i < count; i++) {
                    if(items.get(i).votes == max) {
                        item += items.get(i).name+" ";
                    }
                }

                reply(event, "Vote ended in a "+count+" way tie between "+item, true);
                return null;
            }

            // return item with max votes
            for(Item i : items) {
                if(i.votes == max) {
                    return i;
                }
            }

            return null;
        }
    }

    public class Item {
        public String name;
        public int votes;

        public Item(String name) {
            this.name = name;
        }

        public void upvote() {
            votes++;
        }


    }
}
