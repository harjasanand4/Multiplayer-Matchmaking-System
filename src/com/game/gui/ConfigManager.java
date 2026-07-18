package com.game.gui;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigManager {
    private static final String CONFIG_FILE = "user_config.txt";

    // Load a preference value from the config file
    public static String loadPreference(String key, String defaultValue) {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            props.load(fis);
        } catch (IOException e) {
            // File not found or error reading file; use default preference.
        }
        return props.getProperty(key, defaultValue);
    }

    // Save a preference value to the config file
    public static void savePreference(String key, String value) {
        Properties props = new Properties();

        // First load existing properties to preserve them
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            props.load(fis);
        } catch (IOException e) {
            // File might not exist yet, which is fine
        }

        // Set the new property value
        props.setProperty(key, value);

        // Save all properties
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            props.store(fos, "User Settings");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // For backward compatibility
    public static String loadMenuPreference() {
        return loadPreference("menuStyle", "GameMenu");
    }

    // For backward compatibility
    public static void saveMenuPreference(String menuStyle) {
        savePreference("menuStyle", menuStyle);
    }
}