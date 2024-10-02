package me.artaphy.axiumMenu.config;

import me.artaphy.axiumMenu.AxiumMenu;
import me.artaphy.axiumMenu.exceptions.ConfigurationException;
import me.artaphy.axiumMenu.utils.ColorUtils;
import me.artaphy.axiumMenu.utils.Logger;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the configuration files for the AxiumMenu plugin.
 * This includes settings, language files, and other configuration data.
 */
public class ConfigManager {

    private final AxiumMenu plugin;
    private FileConfiguration settingsConfig;
    private File settingsFile;
    private final Map<String, FileConfiguration> languageConfigs;
    private String defaultLanguage;
    private boolean serveMode;
    private boolean debugMode;
    private final Map<String, String> languageCache;

    /**
     * Constructs a new ConfigManager instance.
     *
     * @param plugin The main plugin instance.
     */
    public ConfigManager(AxiumMenu plugin) {
        this.plugin = plugin;
        this.languageConfigs = new HashMap<>();
        this.languageCache = new ConcurrentHashMap<>();
    }

    /**
     * Loads all configuration files.
     * This includes settings and language files.
     */
    public void loadConfigs() {
        try {
            loadSettingsConfig();
            loadLanguageConfigs();
            validateConfig();
            this.serveMode = settingsConfig.getBoolean("serve_mode", false);
            this.debugMode = settingsConfig.getBoolean("debug", false);
            if (debugMode) {
                Logger.debug("Debug mode enabled");
                Logger.debug("Serve mode: " + serveMode);
                Logger.debug("Default language: " + defaultLanguage);
            }
            Logger.info(getLanguageString("config.loaded"));
        } catch (ConfigurationException e) {
            Logger.error("Failed to load configurations!", e);
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
            Logger.info("Settings config loaded successfully");
        } catch (Exception e) {
            Logger.error("Failed to load settings.yml", e);
            throw new ConfigurationException("Failed to load settings.yml", e);
        }
        
        // Removed validateConfig() call here
    }

    /**
     * Loads all language configuration files.
     */
    private void loadLanguageConfigs() {
        File langFolder = new File(plugin.getDataFolder(), "lang");
        if (!langFolder.exists() && !langFolder.mkdirs()) {
            Logger.warn("Failed to create language folder");
            return;
        }

        Logger.info("Language folder path: " + langFolder.getAbsolutePath());

        saveDefaultLanguageFile("en_US.yml");
        saveDefaultLanguageFile("zh_CN.yml");

        File[] langFiles = langFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (langFiles != null) {
            for (File file : langFiles) {
                String langCode = file.getName().replace(".yml", "");
                languageConfigs.put(langCode, YamlConfiguration.loadConfiguration(file));
            }
            Logger.info("Loaded " + languageConfigs.size() + " language files");
        } else {
            Logger.warn("No language files found in the lang folder");
        }

        defaultLanguage = settingsConfig.getString("language", "en_US");
        if (!languageConfigs.containsKey(defaultLanguage)) {
            Logger.warn("Default language " + defaultLanguage + " not found, using en_US as fallback");
            defaultLanguage = "en_US";
        }

        if (languageConfigs.isEmpty()) {
            Logger.error("No language files could be loaded. Creating default en_US.yml");
            createDefaultLanguageFile("en_US.yml");
            languageConfigs.put("en_US", YamlConfiguration.loadConfiguration(new File(langFolder, "en_US.yml")));
            defaultLanguage = "en_US";
        }

        if (debugMode) {
            languageConfigs.forEach((key, value) -> 
                Logger.debug("Loaded language: " + key + " with " + value.getKeys(true).size() + " keys"));
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
                    Files.copy(in, langFile.toPath());
                    Logger.info("Created language file: " + fileName);
                } else {
                    Logger.warn("Default language file not found in jar: " + fileName);
                    createDefaultLanguageFile(fileName);
                }
            } catch (IOException e) {
                Logger.error("Failed to save language file: " + fileName + "! Creating a default one.", e);
                createDefaultLanguageFile(fileName);
            }
        }
    }

    /**
     * Creates a default language file with basic entries.
     *
     * @param fileName The name of the language file to create.
     */
    private void createDefaultLanguageFile(String fileName) {
        File langFile = new File(plugin.getDataFolder(), "lang/" + fileName);
        YamlConfiguration config = new YamlConfiguration();
        config.set("config.loaded", "Configuration loaded successfully.");
        config.set("command.error.usage_open", "Usage: /axiummenu open <menu>");
        config.set("command.error.player_only", "This command can only be used by players.");
        config.set("command.error.menu_not_found", "Menu not found: %menu%");
        config.set("command.success.menu_opened", "Opened menu: %title%");
        // Add more default language strings here

        try {
            config.save(langFile);
            Logger.info("Created default language file: " + fileName);
        } catch (IOException e) {
            Logger.error("Failed to create default language file: " + fileName + "!", e);
        }
    }

    /**
     * Saves the settings configuration to file.
     */
    public void saveSettingsConfig() {
        try {
            settingsConfig.save(settingsFile);
        } catch (IOException e) {
            Logger.error("Failed to save settings.yml!", e);
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
     * Gets a language string for the default language.
     *
     * @param key The key of the language string.
     * @return The language string, colorized.
     */
    public String getLanguageString(String key) {
        return languageCache.computeIfAbsent(key, k -> {
            FileConfiguration langConfig = getLanguageConfig(defaultLanguage);
            String value = langConfig.getString(k, k);
            return ColorUtils.colorize(value);
        });
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
        validateSetting("debug", false);
        validateSetting("version", 1);

        String language = settingsConfig.getString("language", "en_US");
        if (!languageConfigs.containsKey(language)) {
            Logger.warn("Language file for " + language + " not found. Using en_US as fallback.");
            settingsConfig.set("language", "en_US");
            saveSettingsConfig();
        }

        String dbType = settingsConfig.getString("database.type");
        if (!Objects.equals(dbType, "sqlite") && !Objects.equals(dbType, "mysql")) {
            throw new ConfigurationException("Invalid database type: " + dbType);
        }

        if (Objects.equals(dbType, "mysql")) {
            validateMySQLSettings();
        }

        saveSettingsConfig();
    }

    private void validateMySQLSettings() throws ConfigurationException {
        String[] requiredSettings = {"host", "port", "database", "username", "password"};
        for (String setting : requiredSettings) {
            if (!settingsConfig.isSet("database.mysql." + setting)) {
                throw new ConfigurationException("Missing required MySQL setting: " + setting);
            }
        }
    }

    private void validateSetting(String path, Object defaultValue) {
        if (!settingsConfig.isSet(path)) {
            Logger.warn("Missing '" + path + "' setting in config. Using default: " + defaultValue);
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
        // 每次检查时都从配置文件读取最新的值
        return settingsConfig.getBoolean("serve_mode", false);
    }

    /**
     * Checks if debug mode is enabled.
     *
     * @return true if debug mode is enabled, false otherwise.
     */
    public boolean isDebugMode() {
        return debugMode;
    }
}