package com.planetgallium.kitpvp.listener;

import com.planetgallium.kitpvp.util.Resource;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import com.planetgallium.kitpvp.Game;
import com.planetgallium.kitpvp.game.Arena;
import com.planetgallium.kitpvp.util.Toolkit;

public class JoinListener implements Listener {

	private final Game plugin;
	private final Arena arena;
	private final Resource config;

	public JoinListener(Game plugin) {
		this.plugin = plugin;
		this.arena = plugin.getArena();
		this.config = plugin.getResources().getConfig();
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();

		arena.getStats().createPlayer(p);

		if (Toolkit.inArena(p)) {
			if (config.getBoolean("Arena.ClearInventoryOnJoin")) {
				p.getInventory().clear();
				p.getInventory().setArmorContents(null);
			}

			arena.addPlayer(p, config.getBoolean("Arena.ToSpawnOnJoin"), config.getBoolean("Arena.GiveItemsOnJoin"));
		}
	}

	@EventHandler
	public void onWorldChange(PlayerChangedWorldEvent e) {
		Player p = e.getPlayer();

		if (Toolkit.inArena(p)) {
			if (config.getBoolean("Arena.ClearInventoryOnJoin")) {
				p.getInventory().clear();
				p.getInventory().setArmorContents(null);
			}

			arena.addPlayer(p, config.getBoolean("Arena.ToSpawnOnJoin"), config.getBoolean("Arena.GiveItemsOnJoin"));
		}
	}

}
