package org.ezhik.authtgem;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.ezhik.authtgem.events.FreezerEvent;
import org.ezhik.authtgem.events.MuterEvent;
import org.geysermc.api.Geyser;
import org.geysermc.api.GeyserApiBase;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.ezhik.authtgem.AuthTGEM.bot;

public class BotTelegram extends TelegramLongPollingBot {
    private String username = "changeme";
    private String token = "changeme";
    private static Map<String, String> nextStep = new HashMap<>();
    private static Map<String, UUID> playerUUID = new HashMap<>();
    private Map<String, String> sendMessageData = new HashMap<>();
    public static Map<String, String> curentplayer = new HashMap<>();
    public static Map<UUID,String> bedrockPlayer = new HashMap<>();
    public boolean authNecessarily = false;
    public Long adminChatID = 0L;
    public Integer threadChatID = 0;

    public BotTelegram() {
        YamlConfiguration config = new YamlConfiguration();
        File file = new File("plugins/Minetelegram/config.yml");
        if (!file.exists()) {
            config.set("username", username);
            config.set("token", token);
            config.set("authNecessarily", authNecessarily);
            config.set("adminChatID", adminChatID);
            config.set("threadChatID", threadChatID);
            try {
                config.save(file);
            } catch (Exception e) {
                System.out.println("Error creating config file: " + e);
            }
        } else {
            try {
                config.load(file);
            } catch (IOException e) {
                System.out.println("Error loading config file: " + e);
            } catch (InvalidConfigurationException e) {
                System.out.println("Error loading config file: " + e);
            }
            username = config.getString("username");
            token = config.getString("token");
            authNecessarily = config.getBoolean("authNecessarily");
            adminChatID = config.getLong("adminChatID");
            threadChatID = config.getInt("threadChatID");
        }
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            if (update.getMessage().getText().toString().startsWith("/")) {
                if (update.getMessage().getText().toString().equals("/start") || update.getMessage().getText().toString().equals("/link")) {
                    User.starcmd(update.getMessage());
                    nextStep.put(update.getMessage().getChatId().toString(), "askplayername");
                }
                if (update.getMessage().getText().toString().startsWith("/find")) {
                    String[] args = update.getMessage().getText().toString().split(" ");
                    if (args.length == 2) {
                        String username = User.findPlayerTG(args[1]);
                        if (username != null) {
                            this.sendMessage(update.getMessage().getChatId(), "[Бот] Найден ТГ аккаунт по нику " + args[1] + ": @" + username);
                        }
                    } else {
                        this.sendMessage(update.getMessage().getChatId(), "[Бот] Команда введена неверно. Введите /find [никнейм]");
                    }
                }
                if (update.getMessage().getText().toString().equals("/kickme")) {
                    User user = User.getOnlineUser(update.getMessage().getChatId());
                    if (user != null) {
                        user.kick();
                        user.sendMessage(AuthTGEM.messageTG.get("kickme_kick_succes"));
                    } else
                        this.sendMessage(update.getMessage().getChatId(), AuthTGEM.messageTG.get("kickme_player_notfound"));
                }
                if (update.getMessage().getText().toString().equals("/unlink")) {
                    User user = User.getOnlineUser(update.getMessage().getChatId());
                    if (user == null)
                        this.sendMessage(update.getMessage().getChatId(), AuthTGEM.messageTG.get("unlink_player_notfound"));
                    else user.unlink();
                }
                if (update.getMessage().getText().toString().equals("/resetpassword")) {
                    User user = User.getOnlineUser(update.getMessage().getChatId());
                    if (user != null) user.resetpassword();
                    else
                        this.sendMessage(update.getMessage().getChatId(), AuthTGEM.messageTG.get("resetpass_player_notfound"));
                }
                if (update.getMessage().getText().toString().equals("/tfoff")) {
                    User user = User.getOnlineUser(update.getMessage().getChatId());
                    if (user != null) user.setTwofactor(false);
                    else
                        this.sendMessage(update.getMessage().getChatId(), AuthTGEM.messageTG.get("tfoff_player_notfound"));
                }
                if (update.getMessage().getText().toString().equals("/tfon")) {
                    User user = User.getOnlineUser(update.getMessage().getChatId());
                    if (user != null) user.setTwofactor(true);
                    else
                        this.sendMessage(update.getMessage().getChatId(), AuthTGEM.messageTG.get("tfon_player_notfound"));
                }
                if (update.getMessage().getText().toString().equals("/accounts")) {
                    this.chosePlayer(update.getMessage().getChatId());
                }
                if (update.getMessage().getText().toString().equals("/friends")) {
                    this.showFriendsList(update.getMessage());
                }
                this.deleteMessage(update.getMessage());
            } else {
                if (!bedrockPlayer.isEmpty()) {
                    for (Map.Entry<UUID,String> map : bedrockPlayer.entrySet()) {
                        UUID uuid = map.getKey();
                        String key = map.getValue();
                        if (update.getMessage().getText().equals(key)) {
                            User.registerBedrock(update.getMessage(),uuid);
                            User user = User.getUser(uuid);
                            user.sendMessage(AuthTGEM.messageTG.get("code_account_activated"));
                        }
                    }
                }
                if (nextStep.containsKey(update.getMessage().getChatId().toString())) {
                    if (nextStep.get(update.getMessage().getChatId().toString()).equals("askpassword")) {
                        String password = update.getMessage().getText().toString().replace(" ", "").replace("\n", "");
                        String hash = PasswordHasher.hashPassword(password);
                        File file = new File("plugins/Minetelegram/users/" + playerUUID.get(update.getMessage().getChatId().toString()) + ".yml");
                        YamlConfiguration userconfig = YamlConfiguration.loadConfiguration(file);
                        if (hash.equals(userconfig.getString("password"))) {
                            User.register(update.getMessage(), playerUUID.get(update.getMessage().getChatId().toString()));
                            nextStep.put(update.getMessage().getChatId().toString(), "none");
                        } else {
                            this.sendMessage(update.getMessage().getChatId(), AuthTGEM.messageTG.get("tgasign_incorrect_password"));
                        }
                        this.deleteMessage(update.getMessage());
                    }
                    if (nextStep.get(update.getMessage().getChatId().toString()).equals("askplayername")) {
                        if (User.isNickname(update.getMessage().getText().toString())) {
                            Player player = Bukkit.getPlayer(update.getMessage().getText().toString());
                            UUID uuid = player.getUniqueId();
                            User user = User.getUser(uuid);
                            if (user != null) {
                                if (user.chatid.equals(update.getMessage().getChatId())) {
                                    this.sendMessage(update.getMessage().getChatId(), AuthTGEM.messageTG.get("account_already_tgasign"));
                                } else {
                                    this.sendMessage(update.getMessage().getChatId(), AuthTGEM.messageTG.get("account_already_tgasign_round"));
                                }
                            } else {
                                GeyserApiBase api;
                                try {
                                    api = Geyser.api();
                                } catch (Error error) {
                                    api = null;
                                    System.out.println("[AuthTG] Please, download GeyserMC and floodgate | Пожалуйста,загрузить GeyserMC и floodgate");
                                }
                                if (api != null && api.isBedrockPlayer(uuid)) {
                                    User.register(update.getMessage(), uuid);
                                    nextStep.put(update.getMessage().getChatId().toString(), "none");
                                } else {
                                    playerUUID.put(update.getMessage().getChatId().toString(), uuid);
                                    this.sendMessage(update.getMessage().getChatId(), AuthTGEM.messageTG.get("tgasign_check_password"));
                                    nextStep.put(update.getMessage().getChatId().toString(), "askpassword");
                                }
                            }
                        }
                    }
                    if (nextStep.get(update.getMessage().getChatId().toString()).equals("sendmsg")) {
                        nextStep.put(update.getMessage().getChatId().toString(), "none");
                        this.deleteMessage(update.getMessage());
                        User senderuser = User.getCurrentUser(update.getMessage().getChatId());
                        User frienduser = User.getUser(sendMessageData.get(update.getMessage().getChatId().toString()));
                        frienduser.sendMessageB(AuthTGEM.messageTG.getPNSendMSGmessage(senderuser.chatid) + update.getMessage().getText().toString(), senderuser.playername);
                    }
                    if (nextStep.get(update.getMessage().getChatId().toString()).equals("sendmcmsg")) {
                        nextStep.put(update.getMessage().getChatId().toString(), "none");
                        this.deleteMessage(update.getMessage());
                        User senderuser = User.getCurrentUser(update.getMessage().getChatId());
                        User frienduser = User.getUser(sendMessageData.get(update.getMessage().getChatId().toString()));
                        frienduser.player.sendMessage(ChatColor.translateAlternateColorCodes('&', AuthTGEM.messageMC.getSendMCmsgPN(senderuser.chatid) + update.getMessage().getText().toString()));

                    }
                    if (nextStep.get(update.getMessage().getChatId().toString()).equals("none"))
                        nextStep.remove(update.getMessage().getChatId().toString());
                }

            }
        }
        if (update.hasCallbackQuery()) {
            if (update.getCallbackQuery().getData().toString().startsWith("ys")) {
                String playername = update.getCallbackQuery().getData().toString().replace("ys", "");
                FreezerEvent.unfreezeplayer(playername);
                MuterEvent.unmute(playername);
                this.deleteMessage(update.getCallbackQuery().getMessage());
                Player player = Bukkit.getPlayer(playername);
                player.resetTitle();
                File file = new File("plugins/Minetelegram/users/" + player.getUniqueId() + ".yml");
                YamlConfiguration userconfig = YamlConfiguration.loadConfiguration(file);
                if (update.getMessage() != null && !userconfig.getString("username").equals(update.getMessage().getChat().getUserName())) {
                    userconfig.set("username", update.getMessage().getChat().getUserName());
                }
                userconfig.set("ipAddress", player.getAddress().getAddress().toString());
                try {
                    userconfig.save(file);
                } catch (IOException e) {
                    System.out.println("Error saving file: " + e);
                }
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', AuthTGEM.messageMC.get("succes_login_account")));
            }
            if (update.getCallbackQuery().getData().toString().startsWith("no")) {
                String playername = update.getCallbackQuery().getData().toString().replace("no", "");
                Handler.kick(playername, ChatColor.translateAlternateColorCodes('&', AuthTGEM.messageMC.get("rejected_login_account")));
                this.deleteMessage(update.getCallbackQuery().getMessage());
            }

            if (update.getCallbackQuery().getData().toString().startsWith("acc")) {
                String playername = update.getCallbackQuery().getData().toString().replace("acc", "");
                curentplayer.put(update.getCallbackQuery().getMessage().getChatId().toString(), playername);
                this.sendMessage(update.getCallbackQuery().getMessage().getChatId(), AuthTGEM.messageTG.getAccChoosePN(playername));
            }
            if (update.getCallbackQuery().getData().toString().startsWith("addfr")) {
                if (update.getCallbackQuery().getData().toString().startsWith("addfrys")) {
                    String friendname = update.getCallbackQuery().getData().toString().replace("addfrys", "");
                    User user1 = User.getOnlineUser(update.getCallbackQuery().getMessage().getChatId());
                    User user2 = User.getUser(Bukkit.getPlayer(friendname).getUniqueId());
                    if (!user1.friends.contains(user2.playername)) user1.addfriend(user2.playername);
                    if (!user2.friends.contains(user1.playername)) user2.addfriend(user1.playername);
                    user1.player.sendMessage(ChatColor.translateAlternateColorCodes('&', AuthTGEM.messageMC.getPNaddedUser1Friend(friendname)));
                    user2.player.sendMessage(ChatColor.translateAlternateColorCodes('&', AuthTGEM.messageMC.getPNaddedUser2Friend(user1.chatid)));
                    user1.sendMessage(AuthTGEM.messageTG.getAddFriendPN(friendname));
                    this.deleteMessage(update.getCallbackQuery().getMessage());
                }
                if (update.getCallbackQuery().getData().toString().startsWith("addfrno")) {
                    String friendname = update.getCallbackQuery().getData().toString().replace("addfrno", "");
                    User user2 = User.getUser(Bukkit.getPlayer(friendname).getUniqueId());
                    User user1 = User.getOnlineUser(update.getCallbackQuery().getMessage().getChatId());
                    user2.player.sendMessage(ChatColor.translateAlternateColorCodes('&', AuthTGEM.messageMC.get("bid_rejected")));
                    user1.sendMessage(AuthTGEM.messageTG.get("bid_succes_rejected"));
                    this.deleteMessage(update.getCallbackQuery().getMessage());
                }
            }
            if (update.getCallbackQuery().getData().toString().startsWith("chfr")) {
                String friendname = update.getCallbackQuery().getData().toString().replace("chfr", "");
                this.friendAction(friendname, update.getCallbackQuery().getMessage());
            }
            if (update.getCallbackQuery().getData().toString().startsWith("delfr")) {
                String friendname = update.getCallbackQuery().getData().toString().replace("delfr", "");
                User user = User.getCurrentUser(update.getCallbackQuery().getMessage().getChatId());
                this.deleteMessage(update.getCallbackQuery().getMessage());
                this.sendMessage(update.getCallbackQuery().getMessage().getChatId(), AuthTGEM.messageTG.get("del_friends") + user.remFriend(friendname));
            }
            if (update.getCallbackQuery().getData().toString().startsWith("sndmsg")) {
                String friendname = update.getCallbackQuery().getData().toString().replace("sndmsg", "");
                this.deleteMessage(update.getCallbackQuery().getMessage());
                User user = User.getCurrentUser(update.getCallbackQuery().getMessage().getChatId());
                this.sendMessage(update.getCallbackQuery().getMessage().getChatId(), AuthTGEM.messageTG.getSendMsgFriendPN(user.chatid));
                nextStep.put(update.getCallbackQuery().getMessage().getChatId().toString(), "sendmsg");
                sendMessageData.put(update.getCallbackQuery().getMessage().getChatId().toString(), friendname);
            }
            if (update.getCallbackQuery().getData().toString().startsWith("sndmcmsg")) {
                String friendname = update.getCallbackQuery().getData().toString().replace("sndmcmsg", "");
                this.deleteMessage(update.getCallbackQuery().getMessage());
                User user = User.getCurrentUser(update.getCallbackQuery().getMessage().getChatId());
                this.sendMessage(update.getCallbackQuery().getMessage().getChatId(), AuthTGEM.messageTG.getSendMCMsgFriendPN(user.chatid));
                nextStep.put(update.getCallbackQuery().getMessage().getChatId().toString(), "sendmcmsg");
                sendMessageData.put(update.getCallbackQuery().getMessage().getChatId().toString(), friendname);
            }
        }
    }

    public void sendMessage(Long Chatid, String message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(Chatid);
        sendMessage.setText(message);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            System.out.println("Error sending message: " + e);
        }
    }
    public void sendMessageThread(Long chatid,Integer thread ,String message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(message);
        sendMessage.setChatId(chatid);
        sendMessage.setMessageThreadId(thread);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            System.out.println("Error sending message: " + e);
        }
    }

    public void deleteMessage(Message message) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(message.getChatId());
        deleteMessage.setMessageId(message.getMessageId());
        try {
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            System.out.println("Error deleting message: " + e);
        }
    }

    public void chosePlayer(Long chatID) {
        InlineKeyboardMarkup players = new InlineKeyboardMarkup();
        List<String> playernames = User.getPlayerNames(chatID);

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        for (String name : playernames) {
            List<InlineKeyboardButton> colkeyb = new ArrayList<>();
            InlineKeyboardButton playerbtn = new InlineKeyboardButton();
            playerbtn.setText(name);
            playerbtn.setCallbackData("acc" + name);
            colkeyb.add(playerbtn);
            keyboard.add(colkeyb);
        }


        players.setKeyboard(keyboard);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatID);
        sendMessage.setText(AuthTGEM.messageTG.get("account_choose"));
        sendMessage.setReplyMarkup(players);
        try {
            this.execute(sendMessage);
        } catch (TelegramApiException e) {
            System.out.println("Error sending message: " + e);
        }
    }

    private void showFriendsList(Message message) {
        User user = User.getCurrentUser(message.getChatId());
        List<List<InlineKeyboardButton>> friends = new ArrayList<>();

        if (user.friends.size() == 0) {
            this.sendMessage(message.getChatId(), AuthTGEM.messageTG.get("friends_list_notfriend"));
            this.deleteMessage(message);
            return;
        }

        for (String friendname : user.friends) {
            List<InlineKeyboardButton> colkeyb = new ArrayList<>();
            InlineKeyboardButton freeplayerbtn = new InlineKeyboardButton();
            freeplayerbtn.setText(friendname + User.getplayerstatus(friendname));
            freeplayerbtn.setCallbackData("chfr" + friendname);
            colkeyb.add(freeplayerbtn);
            friends.add(colkeyb);
        }
        InlineKeyboardMarkup friendskeyb = new InlineKeyboardMarkup();
        friendskeyb.setKeyboard(friends);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId());
        sendMessage.setText(AuthTGEM.messageTG.get("friends_list"));
        sendMessage.setReplyMarkup(friendskeyb);
        try {
            this.execute(sendMessage);
        } catch (TelegramApiException e) {
            System.out.println("Error sending message: " + e);
        }
        this.deleteMessage(message);
    }

    private void friendAction(String friendname, Message message) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        InlineKeyboardButton delFriendButton = new InlineKeyboardButton();
        InlineKeyboardButton sendMessageButton = new InlineKeyboardButton();
        InlineKeyboardButton sendMCMessageButton = new InlineKeyboardButton();
        InlineKeyboardMarkup actionsKeyboard = new InlineKeyboardMarkup();

        delFriendButton.setText(AuthTGEM.messageTG.get("friends_act_remfriend"));
        delFriendButton.setCallbackData("delfr" + friendname);
        List<InlineKeyboardButton> delfriendcolkeyb = new ArrayList<>();
        delfriendcolkeyb.add(delFriendButton);
        keyboard.add(delfriendcolkeyb);
        sendMessageButton.setText(AuthTGEM.messageTG.get("friends_act_tgmsg"));
        sendMessageButton.setCallbackData("sndmsg" + friendname);
        List<InlineKeyboardButton> sendmsgcolkeyb = new ArrayList<>();
        sendmsgcolkeyb.add(sendMessageButton);
        keyboard.add(sendmsgcolkeyb);
        if (User.getUser(friendname).player != null) {
            sendMCMessageButton.setText(AuthTGEM.messageTG.get("friends_act_minecraftmsg"));
            sendMCMessageButton.setCallbackData("sndmcmsg" + friendname);
            List<InlineKeyboardButton> sendmcmsgcolkeyb = new ArrayList<>();
            sendmcmsgcolkeyb.add(sendMCMessageButton);
            keyboard.add(sendmcmsgcolkeyb);
        }
        actionsKeyboard.setKeyboard(keyboard);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId());
        sendMessage.setText(AuthTGEM.messageTG.getPNtgAct(friendname));
        sendMessage.setReplyMarkup(actionsKeyboard);
        try {
            this.execute(sendMessage);
        } catch (TelegramApiException e) {
            System.out.println("Error sending message: " + e);
        }
        this.deleteMessage(message);

    }
}