package org.ezhik.authtgem.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class TestEvent implements Listener {
    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (event.getResult() == PlayerLoginEvent.Result.ALLOWED) {
            if (event.getPlayer().hasPermission("minecraft.account.licensed")) {
                System.out.println("Игрок " + event.getPlayer().getName() + " с лицензией");
            } else {
                System.out.println("Игрок " + event.getPlayer().getName() + " без лицензии");
            }
        }
    }
}
