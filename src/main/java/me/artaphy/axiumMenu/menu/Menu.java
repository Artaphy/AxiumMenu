package me.artaphy.axiumMenu.menu;

import me.artaphy.axiumMenu.AxiumMenu;
import me.artaphy.axiumMenu.events.Action;
import me.artaphy.axiumMenu.events.Condition;
import me.artaphy.axiumMenu.events.EventHandler;
import me.artaphy.axiumMenu.events.MenuEvent;
import me.artaphy.axiumMenu.events.MenuOpenEvent;
import me.artaphy.axiumMenu.events.actions.ActionFactory;
import me.artaphy.axiumMenu.events.conditions.ConditionFactory;
import me.artaphy.axiumMenu.exceptions.MenuLoadException;
import me.artaphy.axiumMenu.utils.ColorUtils;
import me.artaphy.axiumMenu.utils.Logger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Represents a complete menu in the AxiumMenu plugin.
 * This class is the core component that handles menu creation, layout, and interaction.
 * <p>
 * Features:
 * - YAML-based configuration
 * - Dynamic layout system
 * - Conditional actions
 * - Event handling
 * - Multiple activation methods
 * - PlaceholderAPI integration
 * <p>
 * A menu consists of:
 * - A title
 * - A layout defining item positions
 * - A collection of menu items
 * - Activation methods (commands, chat triggers, items)
 * - Event handlers
 */
public class Menu {
    private final String name;
    private final String title;
    private int rows;
    private final MenuType type;
    private List<String> layout;
    private final Map<Integer, MenuItem> items;
    private final List<MenuActivator> activators;
    private boolean isExpired = false;
    private final Map<String, List<EventHandler>> events;
    private final Map<String, String> processedRowCache = new ConcurrentHashMap<>();
    private long lastAccessTime = System.currentTimeMillis();

    /**
     * Constructs a new Menu instance from a configuration section.
     * This constructor handles the complete initialization of the menu,
     * including layout processing, item loading, and event registration.
     *
     * @param name The unique identifier for this menu
     * @param config The configuration section containing menu details
     * @throws MenuLoadException if there are errors in the menu configuration
     */
    public Menu(String name, ConfigurationSection config) throws MenuLoadException {
        this.name = name;
        this.title = config.getString("title", "Default Title");
        this.type = MenuType.fromString(config.getString("type", "CHEST"));
        this.items = new HashMap<>();
        this.activators = new ArrayList<>();
        this.events = new HashMap<>();

        validateConfig(config);
        loadLayout(config);
        loadItems(config.getConfigurationSection("items"));
        loadActivators(config.getConfigurationSection("activators"));
        loadEvents(config);

        Logger.debug("Menu '" + name + "' loaded successfully with " + items.size() + " items.");
    }

    /**
     * Validates the menu configuration.
     * Ensures all required fields are present and properly formatted.
     *
     * @param config The configuration section to validate
     * @throws MenuLoadException if the configuration is invalid
     */
    private void validateConfig(ConfigurationSection config) throws MenuLoadException {
        if (!config.contains("title")) {
            throw new MenuLoadException("Missing 'title' for menu: " + name);
        }
        if (!config.contains("layout") && !config.contains("rows")) {
            throw new MenuLoadException("Missing 'layout' or 'rows' for menu: " + name);
        }
        if (!config.contains("items")) {
            throw new MenuLoadException("Missing 'items' section for menu: " + name);
        }
    }

    /**
     * Loads and processes the menu layout.
     * The layout system supports:
     * - Multiple rows
     * - Custom item identifiers
     * - Row-based positioning
     * - Automatic size calculation
     *
     * @param config The configuration section containing layout information
     * @throws MenuLoadException if the layout is invalid
     */
    private void loadLayout(ConfigurationSection config) throws MenuLoadException {
        this.rows = config.getInt("rows", 6);
        
        if (config.isList("layout")) {
            this.layout = config.getStringList("layout");
        } else if (config.isString("layout")) {
            this.layout = Arrays.asList(config.getString("layout", "").split("\n"));
        } else {
            throw new MenuLoadException("Invalid layout configuration for menu: " + name);
        }

        if (!config.contains("rows")) {
            this.rows = Math.min(layout.size(), 6);
        } else {
            this.layout = layout.subList(0, Math.min(layout.size(), rows));
        }

        this.layout = this.layout.stream()
            .map(this::processLayoutRow)
            .collect(Collectors.toList());

        Logger.debug("Menu '" + name + "' layout loaded with " + rows + " rows.");
    }

    /**
     * Processes a layout row to ensure it meets format requirements.
     * Implements caching for improved performance on repeated access.
     *
     * @param row The raw layout row to process
     * @return The processed row string
     */
    private String processLayoutRow(String row) {
        return processedRowCache.computeIfAbsent(row, this::processRowUncached);
    }

    private String processRowUncached(String row) {
        StringBuilder processedRow = new StringBuilder();
        int count = 0;
        boolean inQuotes = false;
        StringBuilder currentToken = new StringBuilder();

        for (char c : row.toCharArray()) {
            if (count >= 9) break;

            if (c == '`') {
                inQuotes = !inQuotes;
                currentToken.append(c);
                if (!inQuotes) {
                    processedRow.append(currentToken);
                    currentToken = new StringBuilder();
                    count++;
                }
            } else if (inQuotes) {
                currentToken.append(c);
            } else {
                processedRow.append(c);
                count++;
            }
        }

        return processedRow.toString();
    }

    /**
     * Loads menu items from configuration.
     * Each item can have:
     * - Custom material
     * - Display name
     * - Lore
     * - Actions
     * - Conditions
     *
     * @param itemsSection The configuration section containing item definitions
     * @throws MenuLoadException if there are errors loading items
     */
    private void loadItems(ConfigurationSection itemsSection) throws MenuLoadException {
        if (itemsSection == null) {
            throw new MenuLoadException("Missing 'items' section for menu: " + name);
        }
    
        Logger.debug("Loading items for menu: " + name);
        for (String key : itemsSection.getKeys(false)) {
            ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
            if (itemSection != null) {
                try {
                    Logger.debug("Loading item: " + key);
                    MenuItem menuItem = new MenuItem(itemSection);
                    if (key.equals("close") && itemSection.contains("slot")) {
                        int slot = itemSection.getInt("slot");
                        items.put(slot, menuItem);
                        Logger.debug("Placed 'close' item at slot: " + slot);
                    } else {
                        placeItemInLayout(key, menuItem);
                        Logger.debug("Placed item in layout: " + key);
                    }
                    Logger.debug("Loaded item '" + key + "' with " + menuItem.getActionSets().size() + " action sets");
                    for (MenuItem.ActionSet actionSet : menuItem.getActionSets()) {
                        Logger.debug("  Action set: condition=" + (actionSet.condition() != null) + 
                                     ", actions=" + actionSet.actions().size() + 
                                     ", denyActions=" + actionSet.denyActions().size());
                    }
                } catch (Exception e) {
                    Logger.error("Error loading item '" + key + "' for menu '" + name + "': " + e.getMessage(), e);
                }
            }
        }
    
        Logger.debug("Menu '" + name + "' loaded " + items.size() + " items.");
    }

    /**
     * Places items in the menu according to the layout.
     * Maps layout identifiers to actual inventory slots.
     *
     * @param key The identifier in the layout
     * @param menuItem The MenuItem to place
     */
    private void placeItemInLayout(String key, MenuItem menuItem) {
        int index = 0;
        for (String row : layout) {
            for (char c : row.toCharArray()) {
                if (String.valueOf(c).equals(key)) {
                    items.put(index, menuItem);
                }
                index++;
            }
        }
    }

    /**
     * Loads menu activators from configuration.
     * Supports multiple activation methods:
     * - Commands
     * - Chat triggers
     * - Item interactions
     *
     * @param activatorsSection The configuration section containing activator definitions
     */
    private void loadActivators(ConfigurationSection activatorsSection) {
        if (activatorsSection == null) {
            return;
        }
        for (String key : activatorsSection.getKeys(false)) {
            ConfigurationSection section = activatorsSection.getConfigurationSection(key);
            if (key.equals("command") || key.equals("chat") || key.equals("item")) {
                activators.add(new MenuActivator(Objects.requireNonNullElse(section, activatorsSection)));
            }
        }
        Logger.debug("Menu '" + name + "' loaded " + activators.size() + " activators.");
    }

    /**
     * Loads event handlers for the menu.
     * Events can be:
     * - Open events
     * - Close events
     * - Click events
     * Each event can have conditions and actions.
     *
     * @param config The configuration section containing event definitions
     */
    private void loadEvents(ConfigurationSection config) {
        ConfigurationSection eventsSection = config.getConfigurationSection("events");
        if (eventsSection == null) return;

        for (String eventType : eventsSection.getKeys(false)) {
            List<EventHandler> handlers = new ArrayList<>();
            if (eventsSection.isList(eventType)) {
                for (Map<?, ?> handlerMap : eventsSection.getMapList(eventType)) {
                    handlers.add(createEventHandler(handlerMap));
                }
            } else if (eventsSection.isConfigurationSection(eventType)) {
                ConfigurationSection handlerSection = eventsSection.getConfigurationSection(eventType);
                handlers.add(createEventHandler(handlerSection));
            } else {
                // Single action or condition
                handlers.add(createEventHandler(eventsSection.get(eventType)));
            }
            events.put(eventType, handlers);
        }
        Logger.debug("Menu '" + name + "' loaded events for " + events.size() + " event types.");
    }

    /**
     * Creates an event handler from a configuration object.
     * Supports both simple and complex handler configurations.
     *
     * @param config The configuration object defining the handler (can be Map, ConfigurationSection, or String)
     * @return The created EventHandler instance
     */
    private EventHandler createEventHandler(Object config) {
        switch (config) {
            case Map<?, ?> map -> {
                return createEventHandlerFromMap(map);
            }
            case ConfigurationSection section -> {
                // Convert ConfigurationSection to Map
                Map<String, Object> map = section.getValues(false);
                return createEventHandlerFromMap(map);
            }
            case String actionStr -> {
                // Handle single action string
                return new EventHandler(null, List.of(Objects.requireNonNull(ActionFactory.createAction(actionStr))), null);
                // Handle single action string
            }
            case null, default -> {
                Logger.warn("Unsupported event handler configuration type: " +
                        (config != null ? config.getClass().getName() : "null"));
                return new EventHandler(null, new ArrayList<>(), null);
            }
        }
    }

    /**
     * Creates an event handler from a Map configuration.
     *
     * @param config The map containing the handler configuration
     * @return The created EventHandler instance
     */
    private EventHandler createEventHandlerFromMap(Map<?, ?> config) {
        Condition condition = null;
        List<Action> actions = new ArrayList<>();
        List<Action> denyActions = new ArrayList<>();

        if (config.containsKey("condition")) {
            condition = ConditionFactory.createCondition(config.get("condition").toString());
        }

        if (config.containsKey("actions")) {
            Object actionsObj = config.get("actions");
            if (actionsObj instanceof List<?>) {
                for (Object actionObj : (List<?>) actionsObj) {
                    actions.add(ActionFactory.createAction(actionObj.toString()));
                }
            } else if (actionsObj instanceof String) {
                actions.add(ActionFactory.createAction(actionsObj.toString()));
            }
        }

        if (config.containsKey("deny")) {
            Object denyObj = config.get("deny");
            if (denyObj instanceof List<?>) {
                for (Object actionObj : (List<?>) denyObj) {
                    denyActions.add(ActionFactory.createAction(actionObj.toString()));
                }
            } else if (denyObj instanceof String) {
                denyActions.add(ActionFactory.createAction(denyObj.toString()));
            }
        }

        return new EventHandler(condition, actions, denyActions);
    }

    /**
     * Executes the event handlers for a specific event type.
     * Processes all handlers registered for the given event type,
     * evaluating conditions and executing appropriate actions.
     *
     * @param eventType The type of event to execute
     * @param player The player involved in the event
     */
    public void executeEvent(String eventType, Player player) {
        List<EventHandler> handlers = events.get(eventType);
        if (handlers != null) {
            MenuEvent event = new MenuEvent(this, player);
            for (EventHandler handler : handlers) {
                if (!handler.handle(event)) {
                    break;
                }
            }
        }
    }

    /**
     * Opens the menu for a player.
     * This method handles:
     * - Event firing
     * - Inventory creation
     * - Thread safety
     * - Event execution
     *
     * @param player The player to open the menu for
     */
    public void open(Player player) {
        lastAccessTime = System.currentTimeMillis();
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(AxiumMenu.getInstance(), () -> open(player));
            return;
        }

        MenuOpenEvent event = new MenuOpenEvent(player, this);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        Inventory inventory = createInventory(new MenuInventoryHolder(this));
        player.openInventory(inventory);
        executeEvent("open", player);
    }

    /**
     * Creates an inventory for this menu.
     * Handles different inventory types and sizes,
     * placing items according to the menu layout.
     *
     * @param holder The InventoryHolder for this inventory
     * @return The created Inventory instance
     */
    public Inventory createInventory(InventoryHolder holder) {
        Inventory inventory;
        if (type == MenuType.CHEST) {
            inventory = Bukkit.createInventory(holder, rows * 9, ColorUtils.colorize(title));
        } else {
            inventory = Bukkit.createInventory(holder, type.getBukkitType(), ColorUtils.colorize(title));
        }

        for (Map.Entry<Integer, MenuItem> entry : items.entrySet()) {
            int slot = entry.getKey();
            if (slot >= 0 && slot < inventory.getSize()) {
                inventory.setItem(slot, entry.getValue().getItemStack());
            } else {
                Logger.warn("Attempted to set item at invalid slot " + slot + " in menu " + name);
            }
        }
        return inventory;
    }

    /**
     * Gets the name of the menu.
     *
     * @return The menu's unique identifier
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the title of the menu.
     *
     * @return The menu's display title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the MenuItem at a specific slot.
     *
     * @param slot The slot to get the item from
     * @return An Optional containing the MenuItem at the specified slot, or empty if not found
     */
    public Optional<MenuItem> getItemAt(int slot) {
        return Optional.ofNullable(items.get(slot));
    }

    /**
     * Gets the number of items in this menu.
     *
     * @return The total number of items in the menu
     */
    public int getItemCount() {
        return items.size();
    }

    /**
     * Marks this menu as expired.
     * Expired menus are typically reloaded or removed from cache.
     */
    public void markAsExpired() {
        this.isExpired = true;
    }

    /**
     * Checks if this menu is expired.
     *
     * @return true if the menu is expired, false otherwise
     */
    public boolean isExpired() {
        return isExpired;
    }

    /**
     * Gets the list of activators for this menu.
     *
     * @return The list of MenuActivator objects for this menu
     */
    public List<MenuActivator> getActivators() {
        return activators;
    }

    /**
     * Gets the last time this menu was accessed.
     *
     * @return The timestamp of the last access
     */
    public long getLastAccessTime() {
        return lastAccessTime;
    }

    /**
     * Executes the actions associated with a menu item.
     * Handles action execution asynchronously to prevent server lag.
     * Commands are executed on the main thread to comply with Bukkit's thread safety requirements.
     *
     * @param player The player who triggered the actions
     * @param item The MenuItem containing the actions to execute
     */
    public void executeItemActions(Player player, MenuItem item) {
        // 在主线程执行命令相关操作
        Bukkit.getScheduler().runTask(AxiumMenu.getInstance(), () -> {
            for (MenuItem.ActionSet actionSet : item.getActionSets()) {
                Condition condition = actionSet.condition();
                Logger.debug("Condition: " + (condition != null ? condition.getClass().getSimpleName() : "null"));
                if (condition == null || condition.check(player)) {
                    Logger.debug("Condition met or no condition, executing actions");
                    for (Action action : actionSet.actions()) {
                        Logger.debug("Executing action: " + action.getClass().getSimpleName());
                        if (!action.execute(player)) {
                            Logger.debug("Action returned false, stopping execution");
                            return;
                        }
                    }
                } else if (!actionSet.denyActions().isEmpty()) {
                    Logger.debug("Condition not met, executing deny actions");
                    for (Action action : actionSet.denyActions()) {
                        Logger.debug("Executing deny action: " + action.getClass().getSimpleName());
                        if (!action.execute(player)) {
                            Logger.debug("Deny action returned false, stopping execution");
                            return;
                        }
                    }
                }
            }
            Logger.debug("Finished executing all actions for item");
        });
    }
}
