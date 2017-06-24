package models;

import java.io.*;


public class IO {

    private File dir;

        public IO() {
            String osName = System.getProperty("os.name").toLowerCase();

            if(osName.contains("win")) {
                dir = new File((System.getenv("APPDATA") + File.separator + "JukeBot" + File.separator));
            } else if(osName.contains("mac")) {
                dir = new File(System.getProperty("user.home") + "/Library/Application Support/JukeBot"+File.separator);
            } else if(osName.contains("nux")) {
                dir = new File(System.getProperty("user.home")+"/JukeBot");
            }

            if(!dir.exists()) dir.mkdir();
        }

        protected boolean serializeObject(Object object) {
            try {
                FileOutputStream fos = new FileOutputStream(dir+"/settings.ser");
                ObjectOutputStream out = new ObjectOutputStream(fos);
                out.writeObject(object);
                out.close();
                fos.close();
                return true;
            } catch(Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        protected Object deserializeObject() {
            try {
                FileInputStream fis = new FileInputStream(dir+"/settings.ser");
                ObjectInputStream in = new ObjectInputStream(fis);
                Object o = in.readObject();
                in.close();
                fis.close();
                return o;
            } catch(Exception e) {
                return null;
            }
        }
    }


