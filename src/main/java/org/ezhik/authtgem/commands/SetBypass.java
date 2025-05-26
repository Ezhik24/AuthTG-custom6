package org.ezhik.authtgem.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerLoginEvent;

import java.io.Console;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.SocketHandler;

public class SetBypass implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player player = null;
        if (commandSender instanceof Player) {
            player = (Player) commandSender;
        }
        if (player != null && !player.hasPermission("minetelegram.setbypass")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f&l[&b&lMT&f&l] &c&lУ вас нет доступа"));
            return false;
        }
        if (player != null && strings[0] == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f&l[&b&lMT&f&l] &c&lКоманда введена неверна,введите: /setbypass <ник>"));
            return false;
        } else if (strings[0] == null) {
            System.out.println("[AuthTG] Команда введена неверна,введите: /setbypass <ник>");
        }
        Player player1 = Bukkit.getPlayer(strings[0]);
        File file = new File("plugins/Minetelegram/users/" + player1.getUniqueId() + ".yml");
        YamlConfiguration userconfig = new YamlConfiguration();
        try {
            userconfig.load(file);
            userconfig.set("bypass", true);
        } catch (IOException e) {
            System.out.println("Error " + e);
        } catch (InvalidConfigurationException e) {
            System.out.println("Error " + e);
        }
        if (!file.exists()) {
            if (player != null) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f&l[&b&lMT&f&l] &aДанный игрок не зарегистрирован,если вы действительно хотите выдать bypass введите команду еще раз"));
            } else {
                System.out.println("[AuthTG] Данный игрок не зарегистрирован,если вы действительно хотите выдать bypass введите команду еще раз");
            }
            try {
                userconfig.save(file);
            } catch (IOException e) {
                System.out.println("Error saving file " + e);
            }
            return false;
        }
        if (player != null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&f&l[&b&lMT&f&l] &aУспешно!"));
        } else {
            System.out.println("[AuthTG] Успешно!");
        }
        try {
            userconfig.save(file);
        } catch (IOException e) {
            System.out.println("Error saving file " + e);
        }
        return true;

    }
}
