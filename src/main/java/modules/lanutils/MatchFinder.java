package modules.lanutils;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Will Davies on 6/24/2017.
 */
public class MatchFinder {

    public static ArrayList<Match> find(ArrayList<String> queue, int tolerance, int display, String activeGame) {
        ArrayList<Match> matchesFound = new ArrayList<>();

        if(queue.size() <= 0) {
            System.out.println("There are no players in the queue.");
            return matchesFound;
        }

        ArrayList<String> chart = new ArrayList<>();

        for (int i = 0; i < Math.pow(2, queue.size()); i++) {
            String s = Integer.toBinaryString(i);
            while (s.length() < queue.size()) {
                s += "0";
            }
            chart.add(s);
        }

        ArrayList<Match> matches = new ArrayList<>();

        // Process the possibilites
        for (int row = 0, id = 1; row < chart.size(); row++, id ++) {
            Match match = new Match();
            match.ID = id;
            match.updateTeamNames(activeGame);
            for (int col = 0; col < chart.get(row).length(); col++) {
                if(chart.get(row).charAt(col) == '0') {
                    match.t1_players.add(queue.get(col).split(":")[2]);
                    match.t1_totalSkill += Integer.parseInt(queue.get(col).split(":")[1]);
                } else {
                    match.t2_players.add(queue.get(col).split(":")[2]);
                    match.t2_totalSkill += Integer.parseInt(queue.get(col).split(":")[1]);
                }
            }
            matches.add(match);
        }

        ArrayList<Match> viable = new ArrayList<>();
        // Sort the matches
        for (int i = 0; i < matches.size(); i++) {
            if(Math.abs(matches.get(i).t1_totalSkill - matches.get(i).t2_totalSkill) <= tolerance) {
                viable.add(matches.get(i));
            }
        }

        // Randomly pick one of them
        Random r = new Random();
        Match selected;
        try {
            selected = viable.get(r.nextInt(viable.size()));
        } catch (Exception e) {
            System.out.println("Match not found. Try increasing match tolerance.");
            return matchesFound;
        }
        // Output to the user
        if(display == -1) {
            printMatchInfo(selected);
            matchesFound.add(selected);
        }
        else {
            for(int i = 0; i < viable.size() && i < display; i++) {
                printMatchInfo(viable.get(i));
            }
            matchesFound.addAll(viable);
        }

        return matchesFound;
    }

    /**
     * Prints the information out about the match
     */
    private static void printMatchInfo(Match match) {
        System.out.println("FOUND A MATCH: ");
        System.out.println("ID: "+match.ID);
        System.out.println("Team 1 skill: " + match.t1_totalSkill);
        String t1 = "";
        for (int j = 0; j < match.t1_players.size(); j++) {
            t1 += match.t1_players.get(j) + ", ";
        }
        System.out.println(t1);
        System.out.println("Team 2 skill: " + match.t2_totalSkill);
        String t2 = "";
        for (int j = 0; j < match.t2_players.size(); j++) {
            t2 += match.t2_players.get(j) + ", ";
        }
        System.out.println(t2);
        System.out.println();
    }

    public static class Match {
        public int ID;
        public String team1Name;
        public String team2Name;

        public void updateTeamNames(String activeGame) {
            team1Name = "Team 1";
            team2Name = "Team 2";
        }

        public ArrayList<String> t1_players = new ArrayList<String>();
        public int t1_totalSkill = 0;

        public ArrayList<String> t2_players = new ArrayList<String>();
        public int t2_totalSkill = 0;
    }
}
