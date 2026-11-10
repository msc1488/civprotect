package me.altergen;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CivProtectCommand implements CommandExecutor {
    private CivProtect plugin;

    public CivProtectCommand(CivProtect plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Использование:");
            sender.sendMessage(ChatColor.YELLOW + "/civprotect list" + ChatColor.GRAY + " - показать белые списки");
            sender.sendMessage(ChatColor.YELLOW + "/civprotect addstar <ник>" + ChatColor.GRAY + " - добавить в whitelist-star");
            sender.sendMessage(ChatColor.YELLOW + "/civprotect removestar <ник>" + ChatColor.GRAY + " - удалить из whitelist-star");
            sender.sendMessage(ChatColor.YELLOW + "/civprotect addop <ник>" + ChatColor.GRAY + " - добавить в whitelist-op");
            sender.sendMessage(ChatColor.YELLOW + "/civprotect removeop <ник>" + ChatColor.GRAY + " - удалить из whitelist-op");
            sender.sendMessage(ChatColor.YELLOW + "/civprotect reload" + ChatColor.GRAY + " - перезагрузить конфиг");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "list":
                sender.sendMessage(ChatColor.YELLOW + "=== Белый список звёздочек ===");
                if (plugin.getWhitelistStar().isEmpty()) {
                    sender.sendMessage(ChatColor.GRAY + "Пусто");
                } else {
                    for (String name : plugin.getWhitelistStar()) {
                        sender.sendMessage(ChatColor.GREEN + "- " + name);
                    }
                }
                sender.sendMessage(ChatColor.YELLOW + "=== Белый список OP ===");
                if (plugin.getWhitelistOp().isEmpty()) {
                    sender.sendMessage(ChatColor.GRAY + "Пусто");
                } else {
                    for (String name : plugin.getWhitelistOp()) {
                        sender.sendMessage(ChatColor.GREEN + "- " + name);
                    }
                }
                break;

            case "addstar":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Использование: /civprotect addstar <ник>");
                    return true;
                }
                plugin.addToWhitelistStar(args[1]);
                sender.sendMessage(ChatColor.GREEN + "Игрок " + args[1] + " добавлен в whitelist-star.");
                break;

            case "removestar":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Использование: /civprotect removestar <ник>");
                    return true;
                }
                plugin.removeFromWhitelistStar(args[1]);
                sender.sendMessage(ChatColor.GREEN + "Игрок " + args[1] + " удалён из whitelist-star.");
                break;

            case "addop":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Использование: /civprotect addop <ник>");
                    return true;
                }
                plugin.addToWhitelistOp(args[1]);
                sender.sendMessage(ChatColor.GREEN + "Игрок " + args[1] + " добавлен в whitelist-op.");
                break;

            case "removeop":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Использование: /civprotect removeop <ник>");
                    return true;
                }
                plugin.removeFromWhitelistOp(args[1]);
                sender.sendMessage(ChatColor.GREEN + "Игрок " + args[1] + " удалён из whitelist-op.");
                break;

            case "reload":
                plugin.reload();
                sender.sendMessage(ChatColor.GREEN + "✅ CivProtect перезагружен!");
                break;

            default:
                sender.sendMessage(ChatColor.RED + "Неизвестная команда!");
                break;
        }
        return true;
    }
}