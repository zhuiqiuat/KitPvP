package com.planetgallium.kitpvp.listener;

import com.planetgallium.kitpvp.game.Arena;
import com.planetgallium.kitpvp.game.Kits;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.planetgallium.kitpvp.Game;
import com.planetgallium.kitpvp.util.Resource;
import com.planetgallium.kitpvp.util.Resources;
import com.planetgallium.kitpvp.util.Toolkit;

public class AttackListener implements Listener {

	private final Resources resources;
	private final Kits kits;
	private final Resource config;

	public AttackListener(Game plugin) {
		this.resources = plugin.getResources();
		this.kits = plugin.getArena().getKits();
		this.config = resources.getConfig();
	}

	@EventHandler
	public void onDamageDealt(EntityDamageByEntityEvent e) {
		if (e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
			Player damagedPlayer = (Player) e.getEntity();
			Player damager = (Player) e.getDamager();

			if (Toolkit.inArena(damagedPlayer) && !damagedPlayer.hasMetadata("NPC")) {
				Arena arena = Game.getInstance().getArena();
				if (config.getBoolean("Arena.NoKitProtection")) {
					if (!kits.playerHasKit(damagedPlayer.getName())) {
						damager.sendMessage(resources.getMessages().fetchString("Messages.Error.Invincible"));
						e.setCancelled(true);
						return;
					}

					if (kits.playerHasKit(damagedPlayer.getName()) && !kits.playerHasKit(damager.getName())) {
						damager.sendMessage(resources.getMessages().fetchString("Messages.Error.Kit"));
						e.setCancelled(true);
						return;
					}
				}
				if (!config.getBoolean("PVPMode.Enable"))
					return;
				if (damager.equals(damagedPlayer))
					return;
				long time = System.currentTimeMillis();
				arena.getStats().getOrCreateStatsCache(damagedPlayer).setLastPVPTime(time);
				arena.getStats().getOrCreateStatsCache(damagedPlayer).setLastPVPPlayer(damager);
				arena.getStats().getOrCreateStatsCache(damager).setLastPVPTime(time);
				arena.getStats().getOrCreateStatsCache(damager).setLastPVPPlayer(damagedPlayer);
			}
		}
	}

	@EventHandler
	public void onDamage(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			Player damagedPlayer = (Player) e.getEntity();

			if (Toolkit.inArena(damagedPlayer)) {
				if (config.getBoolean("Arena.NoKitProtection")) {
					if (!kits.playerHasKit(damagedPlayer.getName())) {
						if (e.getCause() != DamageCause.VOID) {
							e.setCancelled(true);
						}
					}
				}
			}
		}
	}

}
