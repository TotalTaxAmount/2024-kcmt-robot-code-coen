package frc.robot.utils;

import edu.wpi.first.networktables.NetworkTableEvent;
import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.Robot;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Objects;

/**
 * Provides a way to tune stuff with network tables, and have the values persist over power offs or restarts
 */
public class ConfigManager {
    private static ConfigManager INSTANCE;

    private final File configFile = Path.of(Filesystem.getDeployDirectory().toPath().toString(), "tuning.json").toFile();

    private JSONObject json;

    private NetworkTableUtils NTTune = new NetworkTableUtils("Tune");

    /**
     * Get the instance of the config manager
     * @return Instance of config manager
     */
    public static ConfigManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ConfigManager();
        }

        return INSTANCE;
    }

    /**
     * Util class to allow for good network table tuning
     */
    public ConfigManager() {
        System.out.println("[INFO] Hello from config utils");
        try {
            if (configFile.createNewFile() || configFile.length() == 0) {
                System.out.println("[INFO] Created tuning file");
                this.json = this.getDefault();
                this.saveConfig();
            }
        } catch (IOException e) {
            System.out.println("[WARN] Failed to create config file: " + e);
            if (e.toString().contains("Permission")) {
                System.out.printf("[INFO] If you are getting a permission denied error please try the following: " +
                        "1. Connect the the roboRIO over ssh: `ssh admin@10.%s.%s.2`" +
                        "2. Create file and change permissions: `touch %s && chown lvuser:lvuser %s`%n",
                        String.valueOf(RobotController.getTeamNumber()).substring(0,2), String.valueOf(RobotController.getTeamNumber()).substring(2),
                        configFile.getAbsolutePath(), configFile.getAbsolutePath());
            }
        }

        this.json = parseConfig();
        this.initNT();
        this.initListener();
    }


    /**
     * Add a listener to network tables for a change in one of the tuning values
     */
    private void initListener() {
        NTTune.getTable().addListener((EnumSet.of(NetworkTableEvent.Kind.kValueAll)), (table, key1, event) -> {
            this.set(key1, table.getValue(key1).getValue());
            System.out.println("[DEBUG] Updated [" + key1 + "] to " + table.getEntry(key1).getDouble(-1));
            this.saveConfig();
        });
    }

    /**
     * Get the default settings (used to create the json file if it does not exist)
     * @return A default json object
     */
    public JSONObject getDefault() {
//        JSONObject defaultSettings = new JSONObject();

//        // INTAKE
//        defaultSettings.put("intake_notein_speed", 0.0);
//        defaultSettings.put("intake_shoot_speed", 0.0);
//
//        // SHOOTER
//        defaultSettings.put("shooter_speaker", 0.0);
//        defaultSettings.put("shooter_high_pass", 0.0);
//        defaultSettings.put("shooter_low_pass", 0.0);
//        defaultSettings.put("shooter_amp", 0.0);
//
//        // ARM
//        defaultSettings.put("placeholder", 0.0);

        return new JSONObject();
    }

    public void saveDefault() {
        this.json = getDefault();
        this.saveConfig();
    }

    /**
     * Add all the config options to network tables
     * should be run on robot power on.
     */
    public void initNT(){
        SendableChooser<Object> removeKey = new SendableChooser<>();

        for (Object key : this.json.keySet()) {
            Object value = this.json.get(key);
            removeKey.addOption((String) key, value);
            if (value.getClass().equals(Double.class)) { // TODO: Split ints and doubles
                NTTune.setDouble((String) key, (Double) value);
            } else if (value.getClass().equals(Boolean.class)) {
                NTTune.setBoolean((String) key, (Boolean) value);
            } else if (value.getClass().equals(String.class)) {
                NTTune.setString((String) key, (String) value);
            } else {
                System.out.println("[WARN] Value is an unknown type (" + value.getClass().getSimpleName() + ")");
            }
        }

        removeKey.onChange((value) -> {
            for (Object k : this.json.keySet()) {
                if (this.json.get(k).equals(value)) {
                    this.json.remove(k);
                    System.out.println("[DEBUG] Removed [" + k + "]");
                    this.saveConfig();
                }
            }
        });

        SmartDashboard.putData(removeKey);

    }

    /**
     * Get a value from the config
     * @param key          The key in the json
     * @param defaultValue A default value in case we fail to get the key
     * @return The value from the key
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type, T defaultValue) {
        if (!NTTune.getTable().getEntry(key).exists() || !this.json.containsKey(key)) {
            System.out.println("[WARN] " + key + " does not exist in network tables or json, creating a setting to " + defaultValue);
            NTTune.setEntry(key, type, defaultValue);
            this.set(key, defaultValue);
        }
        if (type.equals(Double.class) || type.equals(Integer.class)) {
            return (T) getDouble(key, (double)defaultValue);
        } else if (type.equals(String.class)) {
            return (T) getString(key, (String) defaultValue);
        } else if (type.equals(Boolean.class)) {
            return (T) getBoolean(key, (boolean) defaultValue);
        } else {
            System.out.println("[WARN] Could not get " + type.getSimpleName() + " " + key);
        }

        return defaultValue;
    }

    /**
     * Get a double from the config
     * @param key The key in the json
     * @param defaultValue A default value
     * @return A double (as an {@link Object})
     */
    private Object getDouble(String key, double defaultValue) {
        double res = defaultValue;
        try {
            res = (double) this.json.get(key);
        } catch (ClassCastException e) {
            System.out.println("[WARN] Failed to get " + key + " as a double");
        }

        return res;
    }

    /**
     * Get a Boolean from the config
     * @param key The key in the json
     * @param defaultValue A default value
     * @return A boolean (as an {@link Object})
     */
    private Object getBoolean(String key, boolean defaultValue) {
        boolean res = defaultValue;
        try {
            res = (boolean) this.json.get(key);
        } catch (ClassCastException e) {
            System.out.println("[WARN] Failed to get " + key + " as a boolean");
        }

        return res;
    }

    /**
     * Get a string from the config
     * @param key The key in the json
     * @param defaultValue A default value
     * @return A string (as an {@link Object})
     */
    private Object getString(String key, String defaultValue) {
        String res = defaultValue;
        try {
            res = (String) this.json.get(key);
        } catch (ClassCastException e) {
            System.out.println("[WARN] Failed to get " + key + " as a string");
        }

        return res;
    }


    /**
     * Set a value
     * @param key The key for the json file
     * @param value The value to set
     */
     @SuppressWarnings("unchecked")
     public <T> void set(String key, T value) {
        this.json.put(key, value);
        this.saveConfig();
     }

    /**
     * Get a {@link JSONObject} containing all the config
     * @return A {@link JSONObject} with the config data
     */
     public JSONObject getJson() {
         return this.json;
     }

    /**
     * Save the config to the config file location
     */
    public void saveConfig() {
         try {
             PrintWriter printWriter = new PrintWriter(this.configFile);
             printWriter.println(this.json.toJSONString());
             printWriter.close();
         } catch (FileNotFoundException e) {
             System.out.println("[WARN] Failed to save file: " + configFile + ": " + e);
         }
    }


    /**
     * Parse the config file
     * @return The parsed config as a {@link JSONObject}
     */
    private JSONObject parseConfig() {
        JSONObject jObj = new JSONObject();
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader(this.configFile));
            jObj = (JSONObject) obj;
        } catch (IOException | ParseException e ) {
            System.out.println("[ERROR] An error occurred while attempting to parse the config file: " + e);
        }
        return jObj;
    }

}
