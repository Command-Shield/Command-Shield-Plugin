package com.commandshield;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command; 
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class CommandShield extends JavaPlugin implements Listener {

    private List<String> blockedCommands;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadBlockedCommands();
        getServer().getPluginManager().registerEvents(this, this);
    }

    private void loadBlockedCommands() {
        blockedCommands = getConfig().getStringList("blocked-commands");
        if (blockedCommands == null) {
            blockedCommands = new ArrayList<>(); // Initialize to an empty list if null
        }
    }


    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage().toLowerCase(); // Full command entered by the player
      //  String[] parts = command.split(" ");
        String baseCommand = command;

        if (blockedCommands.stream().anyMatch(blocked -> 
                baseCommand.contains(blocked) || baseCommand.endsWith(":" + blocked))) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "This command is blocked on this server.");
        } 
    }

    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("cshield")) return false;

        if (!sender.hasPermission("commandshield.admin")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /cshield <help|list|add|remove> <command>");
            return false;
        }

        String action = args[0].toLowerCase();

        switch (action) {
            case "help":
                showHelp(sender);
                return true;
            case "list":
                listBlockedCommands(sender);
                return true;
            case "add":
            case "remove":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.YELLOW + "Usage: /cshield " + action + " <command>");
                    return false;
                }
                return handleModifyCommand(sender, action, args[1].toLowerCase());
            default:
                sender.sendMessage(ChatColor.YELLOW + "Usage: /cshield <add|remove|list|help> <command>");
                return false;
        }
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "Command Shield Help:");
        sender.sendMessage(ChatColor.AQUA + "/cshield add <command> - " + ChatColor.WHITE + "Add a command to the blocked list.");
        sender.sendMessage(ChatColor.AQUA + "/cshield remove <command> - " + ChatColor.WHITE + "Remove a command from the blocked list.");
        sender.sendMessage(ChatColor.AQUA + "/cshield list - " + ChatColor.WHITE + "List all blocked commands.");
        sender.sendMessage(ChatColor.AQUA + "/cshield help - " + ChatColor.WHITE + "Show this help menu.");
    }

    private void listBlockedCommands(CommandSender sender) {
        if (blockedCommands.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "No commands are currently blocked.");
        } else {
            sender.sendMessage(ChatColor.GREEN + "Blocked commands:");
            for (String cmd : blockedCommands) {
                sender.sendMessage(ChatColor.WHITE + cmd);
            }
        }
    }

    private boolean handleModifyCommand(CommandSender sender, String action, String commandToModify) {
        if (!sender.hasPermission("commandshield.admin")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (action.equals("add")) {
            if (!blockedCommands.contains(commandToModify)) {
                blockedCommands.add(commandToModify);
                getConfig().set("blocked-commands", blockedCommands);
                saveConfig();
                sender.sendMessage(ChatColor.GREEN + "Command '" + commandToModify + "' has been added to the blocked commands.");
            } else {
                sender.sendMessage(ChatColor.RED + "This command is already blocked.");
            }
        } else if (action.equals("remove")) {
            if (blockedCommands.remove(commandToModify)) {
                getConfig().set("blocked-commands", blockedCommands);
                saveConfig();
                sender.sendMessage(ChatColor.GREEN + "Command '" + commandToModify + "' has been removed from the blocked commands.");
            } else {
                sender.sendMessage(ChatColor.RED + "This command is not in the blocked commands list.");
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("cshield")) {
            if (args.length == 1) {
                return List.of("add", "remove", "list", "help");
            }
            if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
                return new ArrayList<>(blockedCommands); // Autocomplete from blocked commands
            }      
        }
        return null;
    }
}
