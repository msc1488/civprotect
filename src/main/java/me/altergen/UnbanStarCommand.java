package me.altergen;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class UnbanStarCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Использование: /unbanstar <ник>");
            return true;
        }

        String name = args[0];
        try {
            // Используем BanList для разбана
            Bukkit.getBanList(BanList.Type.NAME).pardon(name);
            sender.sendMessage(ChatColor.GREEN + "Игрок " + name + " разбанен!");
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Ошибка: " + e.getMessage());
        }
        return true;
    }
}