package krisapps.restartplus.events.listeners;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BossBar;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class OnPlayerJoin implements Listener {

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent joinEvent) {
        BossBar bar = Bukkit.getBossBar(NamespacedKey.fromString("restartplus:countdown_bar"));
        if (!bar.getPlayers().contains(joinEvent.getPlayer())) {
            bar.addPlayer(joinEvent.getPlayer());
        }
    }


}
