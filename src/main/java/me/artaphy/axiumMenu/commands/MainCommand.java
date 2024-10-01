package me.artaphy.axiumMenu.commands;

import me.artaphy.axiumMenu.AxiumMenu;
import me.artaphy.axiumMenu.menu.Menu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Handles the main command execution for the AxiumMenu plugin.
 * This class is responsible for processing all subcommands of the plugin.
 */
public class MainCommand implements CommandExecutor {

    private final AxiumMenu plugin;

    /**
     * Constructs a new MainCommand instance.
     *
     * @param plugin The main plugin instance.
     */
    public MainCommand(AxiumMenu plugin) {
        this.plugin = plugin;
    }

    /**
     * Executes the command, handling various subcommands.
     *
     * @param sender  The command sender.
     * @param command The command being executed.
     * @param label   The alias of the command used.
     * @param args    The arguments provided with the command.
     * @return true if the command was processed, false otherwise.
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "open":
                if (args.length < 2) {
                    sender.sendMessage(plugin.getConfigManager().getLanguageString("command.error.usage_open"));
                    return true;
                }
                openMenu(sender, args[1]);
                break;
            case "reload":
                reloadPlugin(sender);
                break;
            case "list":
                listMenus(sender);
                break;
            case "serve":
                toggleServeMode(sender);
                break;
            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    /**
     * Sends the help message to the command sender.
     *
     * @param sender The command sender to receive the help message.
     */
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(plugin.getConfigManager().getLanguageString("command.help.title"));
        sender.sendMessage(plugin.getConfigManager().getLanguageString("command.help.open"));
        sender.sendMessage(plugin.getConfigManager().getLanguageString("command.help.reload"));
        sender.sendMessage(plugin.getConfigManager().getLanguageString("command.help.list"));
        sender.sendMessage(plugin.getConfigManager().getLanguageString("command.help.serve"));
    }

    /**
     * Opens a specified menu for a player.
     *
     * @param sender   The command sender (must be a player).
     * @param menuName The name of the menu to open.
     */
    private void openMenu(CommandSender sender, String menuName) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getLanguageString("command.error.player_only"));
            return;
        }

        Menu menu = plugin.getMenuManager().getMenu(menuName);
        if (menu == null) {
            sender.sendMessage(plugin.getConfigManager().getLanguageString("command.error.menu_not_found").replace("%menu%", menuName));
            return;
        }

        menu.open((Player) sender);
        sender.sendMessage(plugin.getConfigManager().getLanguageString("command.success.menu_opened").replace("%title%", menu.getTitle()));
    }

    /**
     * Reloads the plugin configuration and menus.
     *
     * @param sender The command sender to receive the reload confirmation.
     */
    private void reloadPlugin(CommandSender sender) {
        plugin.getConfigManager().loadConfigs();
        plugin.getMenuManager().loadMenus();
        sender.sendMessage(plugin.getConfigManager().getLanguageString("command.success.config_reloaded"));
    }

    /**
     * Lists all available menus to the command sender.
     *
     * @param sender The command sender to receive the menu list.
     */
    private void listMenus(CommandSender sender) {
        Map<String, Menu> menus = plugin.getMenuManager().getAllMenus();
        plugin.getLogger().info("Attempting to get language string for 'command.list.title'");
        String title = plugin.getConfigManager().getLanguageString("command.list.title");
        plugin.getLogger().info("Retrieved title: " + title);
        sender.sendMessage(title);
        
        if (menus.isEmpty()) {
            plugin.getLogger().info("Attempting to get language string for 'command.list.empty'");
            String emptyMessage = plugin.getConfigManager().getLanguageString("command.list.empty");
            plugin.getLogger().info("Retrieved empty message: " + emptyMessage);
            sender.sendMessage(emptyMessage);
        } else {
            for (Menu menu : menus.values()) {
                plugin.getLogger().info("Attempting to get language string for 'command.list.format'");
                String format = plugin.getConfigManager().getLanguageString("command.list.format");
                plugin.getLogger().info("Retrieved format: " + format);
                String message = format.replace("%name%", menu.getName())
                                       .replace("%title%", menu.getTitle());
                sender.sendMessage(message);
            }
        }
    }

    /**
     * Toggles the serve mode (auto-reload) for menu files.
     *
     * @param sender The command sender to receive the toggle confirmation.
     */
    private void toggleServeMode(CommandSender sender) {
        boolean newState = plugin.getConfigManager().toggleServeMode();
        String message = newState ? 
            plugin.getConfigManager().getLanguageString("command.serve.enabled") :
            plugin.getConfigManager().getLanguageString("command.serve.disabled");
        sender.sendMessage(message);
    }
}