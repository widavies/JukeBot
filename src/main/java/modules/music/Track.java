package modules.music;

import java.io.Serializable;


public class Track implements Serializable {

    private String identifier;

    public Track(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

}
