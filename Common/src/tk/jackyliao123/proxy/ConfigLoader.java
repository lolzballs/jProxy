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
        while ((line = reader.readLine()) != null) {
            if (line.charAt(0) == '#') {
                continue;
            }
            String[] strings = line.split("=");
            properties.put(strings[0], strings[1]);
        }
        return properties;
    }
}
