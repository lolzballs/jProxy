package tk.jackyliao123.proxy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class ConfigLoader {
    public static HashMap<String, String> loadConfig(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));

        HashMap<String, String> properties = new HashMap<String, String>();
        String line;
        int lineN = 0;
        while ((line = reader.readLine()) != null) {
            ++lineN;
            if (line.trim().length() == 0) {
                continue;
            }
            if (line.trim().startsWith("#")) {
                continue;
            }
            String[] strings = line.split("=");
            if (strings.length != 2) {
                Logger.error("Invalid config file, wrong number of '=' at line " + lineN);
                continue;
            }
            properties.put(strings[0].trim(), strings[1].trim());
        }
        return properties;
    }
}
