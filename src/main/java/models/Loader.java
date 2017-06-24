package models;

/**
 * Created by Will Davies on 6/6/2017.
 */
public class Loader extends IO {

    public Loader() {
        super();
    }

    public Settings getSettings() {
        return (Settings)deserializeObject();
    }

    public void saveSettings(Settings settings) {
        serializeObject(settings);
    }

}
