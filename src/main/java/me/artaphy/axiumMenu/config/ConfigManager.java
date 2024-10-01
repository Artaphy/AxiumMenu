package me.artaphy.axiumMenu.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import me.artaphy.axiumMenu.AxiumMenu;
import me.artaphy.axiumMenu.utils.ColorUtils;

import java.io.File;
import java.io.IOException;
import me.artaphy.axiumMenu.exceptions.ConfigurationException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Manages the configuration files for the AxiumMenu plugin.
 * This includes settings, language files, and other configuration data.
 */
public class ConfigManager {

    private final AxiumMenu plugin;
    private FileConfiguration settingsConfig;
    private File settingsFile;
    private final Map<String, FileConfiguration> languageConfigs;
    private final File dataFolder;
    private String defaultLanguage;
    private boolean serveMode;
    private final Cache<String, String> languageCache;

    /**
     * Constructs a new ConfigManager instance.
     *
     * @param plugin The main plugin instance.
     */
    public ConfigManager(AxiumMenu plugin) {
        this.plugin = plugin;
        this.languageConfigs = new HashMap<>();
        this.dataFolder = new File(plugin.getDataFolder(), "data");
        this.languageCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();
    }

    /**
     * Loads all configuration files.
     * This includes settings, language files, and initializes the data folder.
     */
    public void loadConfigs() {
        try {
            loadSettingsConfig();
            loadLanguageConfigs();
            createDataFolder();
            this.serveMode = settingsConfig.getBoolean("serve_mode", false);
            plugin.getLogger().info(getLanguageString("config.loaded"));
        } catch (ConfigurationException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save settings.yml!", e);
        }
    }

    /**
     * Loads the main settings configuration file.
     */
    private void loadSettingsConfig() throws ConfigurationException {
        settingsFile = new File(plugin.getDataFolder(), "settings.yml");
        if (!settingsFile.exists()) {
            plugin.saveResource("settings.yml", false);
        }
        try {
            settingsConfig = YamlConfiguration.loadConfiguration(settingsFile);
        } catch (Exception e) {
            throw new ConfigurationException("Failed to load settings.yml", e);
        }
        
        validateConfig();
    }

    /**
     * Loads all language configuration files.
     */
    private void loadLanguageConfigs() {
        File langFolder = new File(plugin.getDataFolder(), "lang");
        if (!langFolder.exists()) {
            boolean created = langFolder.mkdirs();
            if (!created) {
                plugin.getLogger().warning("Failed to create menus folder");
            }
        }

        plugin.getLogger().info("Language folder path: " + langFolder.getAbsolutePath());
        plugin.getLogger().info("Language folder exists: " + langFolder.exists());

        // Save default language files
        saveDefaultLanguageFile("en_US.yml");
        saveDefaultLanguageFile("zh_CN.yml");

        File[] langFiles = langFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (langFiles != null) {
            plugin.getLogger().info("Found " + langFiles.length + " language files");
            for (File file : langFiles) {
                String langCode = file.getName().replace(".yml", "");
                languageConfigs.put(langCode, YamlConfiguration.loadConfiguration(file));
                plugin.getLogger().info("Loaded language file: " + file.getName());
            }
        } else {
            plugin.getLogger().warning("No language files found in the lang folder");
        }

        defaultLanguage = settingsConfig.getString("language", "en_US");
        if (!languageConfigs.containsKey(defaultLanguage)) {
            plugin.getLogger().warning("Default language " + defaultLanguage + " not found, using en_US as fallback");
            defaultLanguage = "en_US";
        }

        for (Map.Entry<String, FileConfiguration> entry : languageConfigs.entrySet()) {
            plugin.getLogger().info("Loaded language: " + entry.getKey() + " with " + entry.getValue().getKeys(true).size() + " keys");
        }
    }

    /**
     * Saves a default language file if it doesn't exist.
     *
     * @param fileName The name of the language file to save.
     */
    private void saveDefaultLanguageFile(String fileName) {
        File langFile = new File(plugin.getDataFolder(), "lang/" + fileName);
        if (!langFile.exists()) {
            try (InputStream in = plugin.getResource("lang/" + fileName)) {
                if (in != null) {
                    plugin.saveResource("lang/" + fileName, false);
                } else {
                    plugin.getLogger().warning("Missing language file: " + fileName);
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Error while saving language file: " + fileName);
                plugin.getLogger().log(Level.SEVERE, "Failed to save language file: " + fileName + "!", e);
            }
        }
    }

    /**
     * Creates the data folder if it doesn't exist.
     */
    private void createDataFolder() {
        if (!dataFolder.exists()) {
            boolean created = dataFolder.mkdirs();
            if (!created) {
                plugin.getLogger().warning("Failed to create menus folder");
            }
        }
    }

    /**
     * Gets the settings configuration.
     *
     * @return The FileConfiguration for the settings.
     */
    @SuppressWarnings("unused")
    public FileConfiguration getSettingsConfig() {
        return settingsConfig;
    }

    /**
     * Saves the settings configuration to file.
     */
    public void saveSettingsConfig() {
        try {
            settingsConfig.save(settingsFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save settings.yml!", e);
        }
    }

    /**
     * Gets a language configuration for a specific language code.
     *
     * @param langCode The language code.
     * @return The FileConfiguration for the specified language.
     */
    public FileConfiguration getLanguageConfig(String langCode) {
        return languageConfigs.getOrDefault(langCode, languageConfigs.get(defaultLanguage));
    }

    /**
     * Gets the data folder for the plugin.
     *
     * @return The File object representing the data folder.
     */
    @SuppressWarnings("unused")
    public File getDataFolder() {
        return dataFolder;
    }

    /**
     * Gets a language string for the default language.
     *
     * @param key The key of the language string.
     * @return The language string, colorized.
     */
    public String getLanguageString(String key) {
        String cachedValue = languageCache.getIfPresent(key);
        if (cachedValue != null) {
            return cachedValue;
        }
        FileConfiguration langConfig = getLanguageConfig(defaultLanguage);
        String value = langConfig.getString(key);

        if (value == null) value = key;

        value = ColorUtils.colorize(value);
        languageCache.put(key, value);
        return value;
    }

    /**
     * Validates the configuration, setting default values if necessary.
     */
    private void validateConfig() throws ConfigurationException {
        validateSetting("language", "en_US");
        validateSetting("database.type", "sqlite");
        validateSetting("database.mysql.host", "localhost");
        validateSetting("database.mysql.port", 3306);
        validateSetting("database.mysql.database", "axiummenu");
        validateSetting("database.mysql.username", "root");
        validateSetting("database.mysql.password", "password");
        validateSetting("serve_mode", false);
        validateSetting("version", 1);
    
        // Save any changes made during validation
        try {
            settingsConfig.save(settingsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save settings after validation: " + e.getMessage());
            throw new ConfigurationException("Failed to save settings after validation", e);
        }
    }

    private void validateSetting(String path, Object defaultValue) {
        if (!settingsConfig.isSet(path)) {
            plugin.getLogger().warning("Missing '" + path + "' setting in config. Using default: " + defaultValue);
            settingsConfig.set(path, defaultValue);
        }
    }

    /**
     * Toggles the serve mode (auto-reload) for menu files.
     *
     * @return The new state of the serve mode.
     */
    public boolean toggleServeMode() {
        serveMode = !serveMode;
        settingsConfig.set("serve_mode", serveMode);
        saveSettingsConfig();
        return serveMode;
    }

    /**
     * Checks if serve mode (auto-reload) is enabled.
     *
     * @return true if serve mode is enabled, false otherwise.
     */
    public boolean isServeMode() {
        return serveMode;
    }
}