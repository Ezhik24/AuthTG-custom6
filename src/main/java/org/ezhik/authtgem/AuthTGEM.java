package org.ezhik.authtgem;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.ezhik.authtgem.commands.*;
import org.ezhik.authtgem.events.*;
import org.ezhik.authtgem.message.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.telegram.telegrambots.meta.TelegramBotsApi;

import java.io.Console;

public final class AuthTGEM extends JavaPlugin {
    public static BotTelegram bot;
    public static MessageTranslationTG messageTG;
    public static MessageTranslationMC messageMC;

    @Override
    public void onEnable() {
        messageTG = new MessageTranslationTG();
        messageMC = new MessageTranslationMC();
        System.out.println("[AuthTG] Пожалуйста,подпишитесь на мой телеграмм канал https://t.me/ezhichek11");
        System.out.println("[AuthTG] Please,subscribe for my telegram channel https://t.me/ezhichek11");
        System.out.println("[AuthTG] Плагин включен!");
        System.out.println("[AuthTG] Plugin enabled!");
        Bukkit.getServer().getPluginManager().registerEvents(new FreezerEvent(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new OnJoinEvent(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new MuterEvent(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new BlockCommandEvent(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new BlockDropItemEvent(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new BlockDamageEvent(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new BlockPlaceBEvent(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new BlockDropBEvent(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerJoinAnotherEvent(), this);
        Handler handler = new Handler();
        handler.runTaskTimer(this,0,1);
        getCommand("code").setExecutor(new CodeCMD());
        getCommand("register").setExecutor(new RegisterCMD());
        getCommand("login").setExecutor(new LoginCMD());
        getCommand("changepassword").setExecutor(new ChangepasswordCMD());
        getCommand("setpassword").setExecutor(new SetPasswordCMD());
        getCommand("tgbc").setExecutor(new TgbcCMD());
        getCommand("addfriend").setExecutor(new AddFriendCMD());
        getCommand("removefriend").setExecutor(new RemFriendCMD());
        getCommand("listfriends").setExecutor(new ListFriendsCMD());
        getCommand("tellfriends").setExecutor(new TellFriendsCMD());
        getCommand("mcbc").setExecutor(new MCbcCMD());
        getCommand("logout").setExecutor(new LogoutCMD());
        getCommand("setbypass").setExecutor(new SetBypass());
        getCommand("unregister").setExecutor(new UnRegisterCMD());
        bot = new BotTelegram();
        if (bot.getBotToken() == "changeme" && bot.getBotUsername() == "changeme") {
            System.out.println("Please set your bot token and username in config.yml");
            System.out.println("Пожалуйста, укажите ваш токен и имя в config.yml");
        } else {
            TelegramBotsApi botsApi;
            try {
                botsApi = new TelegramBotsApi(DefaultBotSession.class);
                botsApi.registerBot(bot);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onDisable() {
        System.out.println("[AuthTG] Пожалуйста,подпишитесь на мой телеграмм канал https://t.me/ezhichek11");
        System.out.println("[AuthTG] Please,subcribe for my telegram channel https://t.me/ezhichek11");
        System.out.println("[AuthTG] Плагин выключен!");
        System.out.println("[AuthTG] Plugin disabled!");
    }
}