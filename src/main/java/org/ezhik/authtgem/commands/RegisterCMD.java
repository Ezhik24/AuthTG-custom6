package org.ezhik.authtgem.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.ezhik.authtgem.AuthTGEM;
import org.ezhik.authtgem.BotTelegram;
import org.ezhik.authtgem.PasswordHasher;
import org.ezhik.authtgem.User;
import org.ezhik.authtgem.events.FreezerEvent;
import org.ezhik.authtgem.events.MuterEvent;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class RegisterCMD implements CommandExecutor {


    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length < 2) {
            commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', AuthTGEM.messageMC.get("register_wrong_command")));
            return false;
        }
        Player player = (Player) commandSender;
        File file = new File("plugins/AuthTG/users/" + player.getUniqueId() + ".yml");
        YamlConfiguration userconfig = YamlConfiguration.loadConfiguration(file);
        if (file.exists() && !userconfig.contains("bypass")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', AuthTGEM.messageMC.get("register_already_register")));
            return false;
        }
        if (!strings[0].equals(strings[1])) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', AuthTGEM.messageMC.get("register_wrong_passwords")));
            return false;
        }
        userconfig.set("password", PasswordHasher.hashPassword(strings[0]));
        userconfig.set("playername", player.getName());
        try {
            userconfig.save(file);
        } catch (IOException e) {
            System.out.println("Error saving config file: " + e);
        }
        if (AuthTGEM.bot.authNecessarily) {
            if (userconfig.getBoolean("bypass")) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', AuthTGEM.messageMC.get("register_successful_register")));
                player.resetTitle();
                FreezerEvent.unfreezeplayer(player.getName());
                MuterEvent.unmute(player.getName());
                return true;
            }
            player.sendTitle(ChatColor.translateAlternateColorCodes('&', AuthTGEM.messageMC.get("account_auth_nessery1_bed")), AuthTGEM.messageMC.get("account_auth_nessery2_bed"), 0,10000000,0);
            String code = User.generateConfirmationCode();
            MuterEvent.mute(player.getName(), ChatColor.translateAlternateColorCodes('&', AuthTGEM.messageMC.getCodeBedrock(code)));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', AuthTGEM.messageMC.getCodeBedrock(code)));
            BotTelegram.bedrockPlayer.put(player.getUniqueId(), code);
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', AuthTGEM.messageMC.get("register_successful_register")));
            player.resetTitle();
            FreezerEvent.unfreezeplayer(player.getName());
            MuterEvent.unmute(player.getName());
        }
        return true;
    }
}
