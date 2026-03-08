package org.example.lab6perfect.config;

import java.io.InputStream;
import java.util.Properties;
public class Configuration {
    private static final String CONFIG_FILE = "config.properties";
    private static Properties properties;

    static {
        properties = new Properties();
        try (InputStream input = Configuration.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new RuntimeException("Fișierul " + CONFIG_FILE + " nu a fost găsit!");
            }
            properties.load(input);
        } catch (Exception e) {
            throw new RuntimeException("Eroare la încărcarea configurației: " + e.getMessage(), e);
        }
    }

    public static Properties getProperties() {
        return properties;
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
}