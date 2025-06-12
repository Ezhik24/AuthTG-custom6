package org.ezhik.authtgem.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.ezhik.authtgem.AuthTGEM;
import org.ezhik.authtgem.Handler;

import java.io.File;
import java.io.IOException;

public class LogoutCMD implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player player = (Player) commandSender;
        File file = new File("plugins/AuthTG/users/" + player.getUniqueId() + ".yml");
        YamlConfiguration userconfig = YamlConfiguration.loadConfiguration(file);
        userconfig.set("ipAddress", "none");
        Handler.kick(player.getName(), AuthTGEM.messageMC.get("logout"));
        try {
            userconfig.save(file);
        } catch (IOException e) {
            System.out.println("Error saving file " + e);
        }
        return true;
    }
}
