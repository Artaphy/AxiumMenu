package me.artaphy.axiumMenu.menu;

import me.artaphy.axiumMenu.events.Action;
import me.artaphy.axiumMenu.events.Condition;
import me.artaphy.axiumMenu.events.actions.ActionFactory;
import me.artaphy.axiumMenu.events.conditions.ConditionFactory;
import me.artaphy.axiumMenu.exceptions.MenuLoadException;
import me.artaphy.axiumMenu.utils.ColorUtils;
import me.artaphy.axiumMenu.utils.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

import java.util.stream.Collectors;

/**
 * Represents an item within a menu in the AxiumMenu plugin.
 * This class handles the creation, configuration, and behavior of individual menu items.
 * Each menu item can have multiple action sets that are executed when the item is clicked.
 */
public class MenuItem {

    private final ItemStack itemStack;
    private final List<ActionSet> actionSets;

    /**
     * Constructs a new MenuItem instance from a configuration section.
     *
     * @param config The configuration section containing item details.
     */
    public MenuItem(ConfigurationSection config) {
        Logger.debug("Creating MenuItem from config: " + config.getName());
        this.itemStack = createItemStack(config);
        this.actionSets = loadActionSets(config);
        Logger.debug("MenuItem created with " + actionSets.size() + " action sets");
    }

    /**
     * Creates an ItemStack based on the configuration provided.
     * This method handles material parsing, amount setting, and meta data configuration.
     *
     * @param config The configuration section containing item details
     * @return A configured ItemStack instance
     */
    private ItemStack createItemStack(ConfigurationSection config) {
        // Parse material
        String materialName = Optional.ofNullable(config.getString("material"))
                .map(s -> s.replace("\"", ""))
                .orElse("STONE");
        Material material = Material.matchMaterial(materialName);
        if (material == null) {
            Bukkit.getLogger().warning("Invalid material: " + materialName + ". Using STONE instead.");
            material = Material.STONE;
        }

        // Parse amount
        int amount = config.getInt("amount", 1);

        // Create ItemStack
        ItemStack itemStack = new ItemStack(material, amount);

        // Set item meta
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            // Set display name
            String displayName = config.getString("name");
            if (displayName != null) {
                meta.setDisplayName(ColorUtils.colorize(displayName));
            }

            // Set lore
            List<String> lore = config.getStringList("lore");
            if (!lore.isEmpty()) {
                List<String> colorizedLore = lore.stream()
                        .map(ColorUtils::colorize)
                        .collect(Collectors.toList());
                meta.setLore(colorizedLore);
            }

            itemStack.setItemMeta(meta);
        }

        return itemStack;
    }

    /**
     * Loads and parses action sets from the configuration.
     * Action sets can be either simple actions or conditional action sets with conditions and deny actions.
     *
     * @param config The configuration section containing action definitions
     * @return A list of parsed ActionSet instances
     */
    private List<ActionSet> loadActionSets(ConfigurationSection config) {
        List<ActionSet> result = new ArrayList<>();

        Logger.debug("Loading actions from config section: " + config.getCurrentPath());
        Logger.debug("Config keys: " + String.join(", ", config.getKeys(false)));

        if (!config.isSet("actions")) {
            Logger.debug("No actions section found");
            return result;
        }

        List<?> actionObjects = config.getList("actions");
        if (actionObjects == null) {
            Logger.debug("Actions list is null");
            return result;
        }

        for (Object obj : actionObjects) {
            if (obj instanceof String actionString) {
                Logger.debug("Found direct action string: " + actionString);
                Action action = ActionFactory.createAction(actionString);
                if (action != null) {
                    result.add(new ActionSet(null, List.of(action), new ArrayList<>()));
                    Logger.debug("Loaded direct action: " + actionString);
                }
            } else if (obj instanceof Map<?, ?> map) {
                Logger.debug("Found conditional action set: " + map);

                String conditionStr = (String) map.get("condition");
                Condition condition = null;
                if (conditionStr != null) {
                    condition = ConditionFactory.createCondition(conditionStr);
                    Logger.debug("Loaded condition: " + condition);
                }

                List<Action> actions = new ArrayList<>();
                Object actionsObj = map.get("actions");
                if (actionsObj instanceof List<?> actionsList) {
                    List<String> actionStrings = actionsList.stream()
                            .filter(o -> o instanceof String)
                            .map(o -> (String) o)
                            .collect(Collectors.toList());
                    actions = loadActions(actionStrings);
                } else if (actionsObj instanceof String actionStr) {
                    actions = loadActions(List.of(actionStr));
                }

                Logger.debug("Loaded " + actions.size() + " actions for condition");

                List<Action> denyActions = new ArrayList<>();
                Object denyObj = map.get("deny");
                if (denyObj instanceof List<?> denyList) {
                    List<String> denyStrings = denyList.stream()
                            .filter(o -> o instanceof String)
                            .map(o -> (String) o)
                            .collect(Collectors.toList());
                    denyActions = loadActions(denyStrings);
                } else if (denyObj instanceof String denyStr) {
                    denyActions = loadActions(List.of(denyStr));
                }

                Logger.debug("Loaded " + denyActions.size() + " deny actions for condition");

                result.add(new ActionSet(condition, actions, denyActions));
            }
        }

        Logger.debug("Loaded " + result.size() + " action sets for menu item");
        return result;
    }

    /**
     * Loads individual actions from a list of action strings.
     * Each action string is parsed and converted into an Action instance using the ActionFactory.
     *
     * @param actionStrings List of action strings to be parsed
     * @return List of parsed Action instances
     */
    private List<Action> loadActions(List<String> actionStrings) {
        return actionStrings.stream()
                .map(ActionFactory::createAction)
                .filter(Objects::nonNull)
                .peek(action -> Logger.debug("Loaded action: " + action.getClass().getSimpleName()))
                .collect(Collectors.toList());
    }

    /**
     * Gets the ItemStack associated with this menu item.
     *
     * @return The ItemStack for this menu item.
     */
    public ItemStack getItemStack() {
        return itemStack;
    }

    /**
     * Gets the action sets associated with this menu item.
     *
     * @return The list of action sets for this menu item.
     */
    public List<ActionSet> getActionSets() {
        return actionSets;
    }

    /**
     * Record class representing a set of actions that can be executed.
     * An action set can have a condition, main actions, and deny actions.
     *
     * @param condition The condition that must be met for the actions to execute
     * @param actions The list of actions to execute if the condition is met
     * @param denyActions The list of actions to execute if the condition is not met
     */
    public record ActionSet(Condition condition, List<Action> actions, List<Action> denyActions) {
    }

    /**
     * Validates the configuration of a menu item.
     * This method ensures all required fields are present and properly formatted.
     * Currently unused but retained for future use.
     *
     * @param config The configuration section to validate
     * @throws MenuLoadException if the configuration is invalid
     */
    @SuppressWarnings("unused")
    private void validateConfig(ConfigurationSection config) throws MenuLoadException {
        // 添加更严格的配置验证
        if (!config.contains("material")) {
            throw new MenuLoadException("Missing required field 'material'");
        }
        
        // 验证动作配置
        if (config.contains("actions")) {
            validateActions(config.get("actions"));
        }
    }

    /**
     * Validates the actions configuration structure.
     * Ensures that action configurations follow the expected format.
     *
     * @param actionsConfig The action configuration object to validate
     * @throws MenuLoadException if the action configuration is invalid
     */
    private void validateActions(Object actionsConfig) throws MenuLoadException {
        if (actionsConfig == null) {
            return;
        }

        if (actionsConfig instanceof List) {
            for (Object action : (List<?>) actionsConfig) {
                validateAction(action);
            }
        } else {
            validateAction(actionsConfig);
        }
    }

    /**
     * Validates an individual action configuration.
     * Checks if the action follows either the simple format (string) or the conditional format (map).
     *
     * @param action The action object to validate
     * @throws MenuLoadException if the action format is invalid
     */
    private void validateAction(Object action) throws MenuLoadException {
        if (action instanceof String actionStr) {
            // 验证简单动作格式
            if (!actionStr.contains(":") && !actionStr.equals("close")) {
                throw new MenuLoadException("Invalid action format: " + actionStr);
            }
        } else if (action instanceof Map) {
            // 验证条件动作格式
            @SuppressWarnings("unchecked")
            Map<String, Object> actionMap = (Map<String, Object>) action;
            if (!actionMap.containsKey("condition")) {
                throw new MenuLoadException("Conditional action missing 'condition' field");
            }
            if (!actionMap.containsKey("actions")) {
                throw new MenuLoadException("Conditional action missing 'actions' field");
            }
        } else {
            throw new MenuLoadException("Invalid action type: " + action.getClass().getName());
        }
    }
}
