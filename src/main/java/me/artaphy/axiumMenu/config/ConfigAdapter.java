package me.artaphy.axiumMenu.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.Configuration;

import java.io.File;
import java.util.*;

/**
 * Adapter class to bridge Typesafe Config with Bukkit's ConfigurationSection.
 * This class allows the use of HOCON configuration files within the Bukkit ecosystem.
 */
public class ConfigAdapter implements ConfigurationSection {

    private final Config config;

    /**
     * Constructs a new ConfigAdapter from a file.
     *
     * @param file The HOCON configuration file.
     */
    public ConfigAdapter(File file) {
        this.config = ConfigFactory.parseFile(file);
    }


    /**
     * Constructs a new ConfigAdapter from an existing Config object.
     *
     * @param config The Typesafe Config object.
     */
    private ConfigAdapter(Config config) {
        this.config = config;
    }

    /**
     * Gets a set of keys at the current path.
     *
     * @param deep If true, all nested keys will be included.
     * @return A Set of key names.
     */
    @Override
    public @NotNull Set<String> getKeys(boolean deep) {
        return new HashSet<>(config.root().keySet());
    }

    /**
     * Gets a map of key-value pairs at the current path.
     *
     * @param deep If true, all nested keys will be included.
     * @return A Map of key-value pairs.
     */
    @Override
    public @NotNull Map<String, Object> getValues(boolean deep) {
        Map<String, Object> result = new HashMap<>();
        for (String key : getKeys(deep)) {
            result.put(key, get(key));
        }
        return result;
    }

    /**
     * Checks if the configuration contains the given path.
     *
     * @param path Path to check.
     * @return True if the path exists, false otherwise.
     */
    @Override
    public boolean contains(@NotNull String path) {
        return config.hasPath(path);
    }

    /**
     * Checks if the configuration contains the given path.
     *
     * @param path Path to check.
     * @param ignoreDefault Whether to ignore default values.
     * @return True if the path exists, false otherwise.
     */
    @Override
    public boolean contains(@NotNull String path, boolean ignoreDefault) {
        return contains(path);
    }

    /**
     * Checks if the specified path is set.
     *
     * @param path Path to check.
     * @return True if the path is set, false otherwise.
     */
    @Override
    public boolean isSet(@NotNull String path) {
        return config.hasPath(path);
    }

    /**
     * Gets the current path of this configuration.
     *
     * @return The current path or an empty string if at root.
     */
    @Override
    public @Nullable String getCurrentPath() {
        return "";
    }

    /**
     * Gets the name of this configuration.
     *
     * @return The name of this configuration.
     */
    @Override
    public @NotNull String getName() {
        return "";
    }

    /**
     * Gets the parent configuration section.
     *
     * @return The parent configuration section, or null if there is no parent.
     */
    @Override
    public @Nullable ConfigurationSection getParent() {
        return null;
    }

    @Override
    public @Nullable Object get(@NotNull String path) {
        return config.hasPath(path) ? config.getAnyRef(path) : null;
    }

    @Override
    public @Nullable Object get(@NotNull String path, @Nullable Object def) {
        return config.hasPath(path) ? config.getAnyRef(path) : def;
    }

    @Override
    public void set(@NotNull String path, @Nullable Object value) {
        throw new UnsupportedOperationException("set operation is not supported in ConfigAdapter");
    }

    @Override
    public @NotNull ConfigurationSection createSection(@NotNull String path) {
        throw new UnsupportedOperationException("createSection operation is not supported in ConfigAdapter");
    }

    @Override
    public @NotNull ConfigurationSection createSection(@NotNull String path, @NotNull Map<?, ?> map) {
        throw new UnsupportedOperationException("createSection operation is not supported in ConfigAdapter");
    }

    @Override
    public @Nullable String getString(@NotNull String path) {
        return config.hasPath(path) ? config.getString(path) : null;
    }

    @Override
    public @Nullable String getString(@NotNull String path, @Nullable String def) {
        return config.hasPath(path) ? config.getString(path) : def;
    }

    @Override
    public boolean isString(@NotNull String path) {
        return config.hasPath(path) && config.getValue(path).valueType().name().equals("STRING");
    }

    @Override
    public int getInt(@NotNull String path) {
        return config.hasPath(path) ? config.getInt(path) : 0;
    }

    @Override
    public int getInt(@NotNull String path, int def) {
        return config.hasPath(path) ? config.getInt(path) : def;
    }

    @Override
    public boolean isInt(@NotNull String path) {
        return config.hasPath(path) && config.getValue(path).valueType().name().equals("NUMBER");
    }

    @Override
    public boolean getBoolean(@NotNull String path) {
        return config.hasPath(path) && config.getBoolean(path);
    }

    @Override
    public boolean getBoolean(@NotNull String path, boolean def) {
        return config.hasPath(path) ? config.getBoolean(path) : def;
    }

    @Override
    public boolean isBoolean(@NotNull String path) {
        return config.hasPath(path) && config.getValue(path).valueType().name().equals("BOOLEAN");
    }

    @Override
    public double getDouble(@NotNull String path) {
        return config.hasPath(path) ? config.getDouble(path) : 0.0;
    }

    @Override
    public double getDouble(@NotNull String path, double def) {
        return config.hasPath(path) ? config.getDouble(path) : def;
    }

    @Override
    public boolean isDouble(@NotNull String path) {
        return config.hasPath(path) && config.getValue(path).valueType().name().equals("NUMBER");
    }

    @Override
    public long getLong(@NotNull String path) {
        return config.hasPath(path) ? config.getLong(path) : 0L;
    }

    @Override
    public long getLong(@NotNull String path, long def) {
        return config.hasPath(path) ? config.getLong(path) : def;
    }

    @Override
    public boolean isLong(@NotNull String path) {
        return config.hasPath(path) && config.getValue(path).valueType().name().equals("NUMBER");
    }

    @Override
    public @Nullable List<?> getList(@NotNull String path) {
        return config.hasPath(path) ? config.getAnyRefList(path) : null;
    }

    @Override
    public @Nullable List<?> getList(@NotNull String path, @Nullable List<?> def) {
        return config.hasPath(path) ? config.getAnyRefList(path) : def;
    }

    @Override
    public boolean isList(@NotNull String path) {
        return config.hasPath(path) && config.getValue(path).valueType().name().equals("LIST");
    }

    @Override
    public @NotNull List<String> getStringList(@NotNull String path) {
        return config.hasPath(path) ? config.getStringList(path) : new ArrayList<>();
    }

    @Override
    public @NotNull List<Integer> getIntegerList(@NotNull String path) {
        return config.hasPath(path) ? config.getIntList(path) : new ArrayList<>();
    }

    @Override
    public @NotNull List<Boolean> getBooleanList(@NotNull String path) {
        return config.hasPath(path) ? config.getBooleanList(path) : new ArrayList<>();
    }

    @Override
    public @NotNull List<Double> getDoubleList(@NotNull String path) {
        return config.hasPath(path) ? config.getDoubleList(path) : new ArrayList<>();
    }

    @Override
    public @NotNull List<Float> getFloatList(@NotNull String path) {
        List<Double> doubleList = getDoubleList(path);
        List<Float> floatList = new ArrayList<>();
        for (Double d : doubleList) {
            floatList.add(d.floatValue());
        }
        return floatList;
    }

    @Override
    public @NotNull List<Long> getLongList(@NotNull String path) {
        return config.hasPath(path) ? config.getLongList(path) : new ArrayList<>();
    }

    @Override
    public @NotNull List<Byte> getByteList(@NotNull String path) {
        List<Integer> intList = getIntegerList(path);
        List<Byte> byteList = new ArrayList<>();
        for (Integer i : intList) {
            byteList.add(i.byteValue());
        }
        return byteList;
    }

    @Override
    public @NotNull List<Character> getCharacterList(@NotNull String path) {
        List<String> stringList = getStringList(path);
        List<Character> charList = new ArrayList<>();
        for (String s : stringList) {
            if (!s.isEmpty()) {
                charList.add(s.charAt(0));
            }
        }
        return charList;
    }

    @Override
    public @NotNull List<Short> getShortList(@NotNull String path) {
        List<Integer> intList = getIntegerList(path);
        List<Short> shortList = new ArrayList<>();
        for (Integer i : intList) {
            shortList.add(i.shortValue());
        }
        return shortList;
    }

    @Override
    public @NotNull List<Map<?, ?>> getMapList(@NotNull String path) {
        if (!config.hasPath(path)) {
            return new ArrayList<>();
        }
        List<?> list = config.getList(path);
        List<Map<?, ?>> result = new ArrayList<>();
        for (Object obj : list) {
            if (obj instanceof Map<?, ?> map) {
                result.add(map);
            }
        }
        return result;
    }

    // 以下方法需要根据你的具体需求来实现
    @Override
    public @NotNull Vector getVector(@NotNull String path) {
        throw new UnsupportedOperationException("getVector is not supported in ConfigAdapter");
    }

    @Override
    public @NotNull Vector getVector(@NotNull String path, Vector def) {
        return def;
    }

    @Override
    public boolean isVector(@NotNull String path) {
        return false;
    }

    @Override
    public @NotNull OfflinePlayer getOfflinePlayer(@NotNull String path) {
        throw new UnsupportedOperationException("getOfflinePlayer is not supported in ConfigAdapter");
    }

    @Override
    public @NotNull OfflinePlayer getOfflinePlayer(@NotNull String path, OfflinePlayer def) {
        return def;
    }

    @Override
    public boolean isOfflinePlayer(@NotNull String path) {
        return false;
    }

    @Override
    public @NotNull ItemStack getItemStack(@NotNull String path) {
        throw new UnsupportedOperationException("getItemStack is not supported in ConfigAdapter");
    }

    @Override
    public @NotNull ItemStack getItemStack(@NotNull String path, ItemStack def) {
        return def;
    }

    @Override
    public boolean isItemStack(@NotNull String path) {
        return false;
    }

    @Override
    public @NotNull Color getColor(@NotNull String path) {
        throw new UnsupportedOperationException("getColor is not supported in ConfigAdapter");
    }

    @Override
    public @NotNull Color getColor(@NotNull String path, Color def) {
        return def;
    }

    @Override
    public boolean isColor(@NotNull String path) {
        return false;
    }

    @Override
    public @NotNull Location getLocation(@NotNull String path) {
        throw new UnsupportedOperationException("getLocation is not supported in ConfigAdapter");
    }

    @Override
    public @NotNull Location getLocation(@NotNull String path, Location def) {
        return def;
    }

    @Override
    public boolean isLocation(@NotNull String path) {
        return false;
    }

    /**
     * Gets a ConfigurationSection at the specified path.
     *
     * @param path Path of the ConfigurationSection to get.
     * @return Requested ConfigurationSection, or null if not found.
     */
    @Override
    public @Nullable ConfigurationSection getConfigurationSection(@NotNull String path) {
        return config.hasPath(path) ? new ConfigAdapter(config.getConfig(path)) : null;
    }

    /**
     * Checks if the specified path is a ConfigurationSection.
     *
     * @param path Path to check.
     * @return True if the path is a ConfigurationSection, false otherwise.
     */
    @Override
    public boolean isConfigurationSection(@NotNull String path) {
        return config.hasPath(path) && config.getValue(path).valueType().name().equals("OBJECT");
    }

    /**
     * Gets the default ConfigurationSection.
     *
     * @return The default ConfigurationSection, or null if not set.
     */
    @Override
    public @Nullable ConfigurationSection getDefaultSection() {
        return null;
    }

    /**
     * Adds a default value to the configuration.
     *
     * @param path Path to set the default for.
     * @param value Value to set the default to.
     */
    @Override
    public void addDefault(@NotNull String path, @Nullable Object value) {
        throw new UnsupportedOperationException("addDefault is not supported in ConfigAdapter");
    }

    @Override
    public @NotNull List<String> getComments(@NotNull String path) {
        return new ArrayList<>();
    }

    @Override
    public @NotNull List<String> getInlineComments(@NotNull String path) {
        return new ArrayList<>();
    }

    @Override
    public void setComments(@NotNull String path, @Nullable List<String> comments) {
        throw new UnsupportedOperationException("setComments is not supported in ConfigAdapter");
    }

    @Override
    public void setInlineComments(@NotNull String path, @Nullable List<String> comments) {
        throw new UnsupportedOperationException("setInlineComments is not supported in ConfigAdapter");
    }

    /**
     * Gets an object of a specific type from the configuration.
     *
     * @param path Path of the object to get.
     * @param clazz The type of the object.
     * @return The requested object, or null if not found.
     */
    @Override
    public <T> T getObject(@NotNull String path, @NotNull Class<T> clazz) {
        if (!config.hasPath(path)) {
            return null;
        }
        Object value = config.getAnyRef(path);
        return clazz.isInstance(value) ? clazz.cast(value) : null;
    }

    /**
     * Gets an object of a specific type from the configuration, with a default value.
     *
     * @param path Path of the object to get.
     * @param clazz The type of the object.
     * @param def The default value to return if the path is not found.
     * @return The requested object, or the default value if not found.
     */
    @Override
    public <T> T getObject(@NotNull String path, @NotNull Class<T> clazz, @Nullable T def) {
        T value = getObject(path, clazz);
        return value != null ? value : def;
    }

    /**
     * Gets the root configuration.
     *
     * @return The root configuration.
     */
    @Override
    public @Nullable Configuration getRoot() {
        return null; // ConfigAdapter is not a true Configuration object, so we return null
    }

    /**
     * Gets a serializable object from the configuration.
     *
     * @param path Path of the serializable object to get.
     * @param clazz The type of the serializable object.
     * @return The requested serializable object, or null if not found.
     */
    @Override
    public <T extends ConfigurationSerializable> @Nullable T getSerializable(@NotNull String path, @NotNull Class<T> clazz) {
        Object obj = get(path);
        return clazz.isInstance(obj) ? clazz.cast(obj) : null;
    }

    /**
     * Gets a serializable object from the configuration, with a default value.
     *
     * @param path Path of the serializable object to get.
     * @param clazz The type of the serializable object.
     * @param def The default value to return if the path is not found.
     * @return The requested serializable object, or the default value if not found.
     */
    @Override
    public <T extends ConfigurationSerializable> @Nullable T getSerializable(@NotNull String path, @NotNull Class<T> clazz, @Nullable T def) {
        T value = getSerializable(path, clazz);
        return value != null ? value : def;
    }
}