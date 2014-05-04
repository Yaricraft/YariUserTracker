package com.yaricraft.YariUserTracker;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class Listeners implements Listener {
    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        String player = event.getPlayer().getName().toLowerCase();
        if(!YariUserTracker.mapPlayers.containsKey(player))
        {
        	event.getPlayer().sendMessage("You were added to the usertracker with the default "+String.valueOf(YariUserTracker.getConfig("config", "repstart"))+" points.");
        	YariUserTracker.mapPlayers.put(player, Integer.parseInt(YariUserTracker.getConfig("config", "repstart")));
        }
    }
}
