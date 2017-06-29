package tools;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class Log {

    private static final int MESSAGES_TO_KEEP = 100;
    private static ArrayList<String> messages = new ArrayList<>();

    public static void log(String message) {
        messages.add("[JukeBot] ["+convertTime(System.currentTimeMillis())+"] [*] "+message);
        if(messages.size() > MESSAGES_TO_KEEP) {
            messages.remove(0);
        }
        System.out.println(messages.get(messages.size() - 1));
    }

    public static String getMessages() {
        String temp = "\n";
        for(String s : messages) {
            temp += "```"+s+"```\n";
        }
        return temp;
    }

    public static void logError(String message) {
        messages.add("[JukeBot] [ERROR] ["+convertTime(System.currentTimeMillis())+"] [*] "+message);
        if(messages.size() > MESSAGES_TO_KEEP) {
            messages.remove(0);
        }
        System.err.println(messages.get(messages.size() - 1));

    }

    public static String convertTime(long timeMillis) {
        if(timeMillis == 0) return "Never";
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault());
        Date resultdate = new Date(timeMillis);
        return sdf.format(resultdate);
    }


}
