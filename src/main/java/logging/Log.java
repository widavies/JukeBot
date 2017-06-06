package logging;

public class Log {

    public static void log(String message) {
        System.out.println("[JukeBot] " + message);
    }

    public static void logError(String message) {
        System.err.println("[JukeBot] [Error]"+message);

    }


}
