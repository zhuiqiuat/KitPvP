package com.planetgallium.kitpvp.listener;

import com.planetgallium.kitpvp.Game;
import com.planetgallium.kitpvp.util.Resource;

import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.weather.WeatherChangeEvent;

import com.planetgallium.kitpvp.game.Arena;
import com.planetgallium.kitpvp.util.Toolkit;

public class ArenaListener implements Listener {

	private final Arena arena;
	private final Resource config;

	public ArenaListener(Game plugin) {
		this.arena = plugin.getArena();
		this.config = plugin.getResources().getConfig();
	}

	@EventHandler
	public void onBreak(BlockBreakEvent e) {
		Player p = e.getPlayer();

		if (Toolkit.inArena(p) && config.getBoolean("Arena.PreventBlockBreaking")) {
			e.setCancelled(!p.hasPermission("kp.arena.blockbreaking"));
		}
	}

	@EventHandler
	public void onPlace(BlockPlaceEvent e) {
		Player p = e.getPlayer();

		if (Toolkit.inArena(p) && config.getBoolean("Arena.PreventBlockPlacing")) {
			e.setCancelled(!p.hasPermission("kp.arena.blockplacing"));
		}

	}

	@EventHandler
	public void onItemDamage(PlayerItemDamageEvent e) {
		if (Toolkit.inArena(e.getPlayer()) && config.getBoolean("Arena.PreventItemDurabilityDamage")) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onDrop(PlayerDropItemEvent e) {
		Player p = e.getPlayer();

		if (Toolkit.inArena(p) && config.getBoolean("Arena.PreventItemDropping")) {
			e.setCancelled(!p.hasPermission("kp.arena.itemdropping"));
		}
	}

	@EventHandler
	public void onHunger(FoodLevelChangeEvent e) {
		Player p = (Player) e.getEntity();

		if (Toolkit.inArena(p) && config.getBoolean("Arena.PreventHunger")) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onExplode(EntityExplodeEvent e) {
		if (Toolkit.inArena(e.getEntity()) && config.getBoolean("Arena.PreventBlockBreaking")) {
			if (e.getEntityType() == EntityType.TNT) { // enable TNT explosion animation
				e.blockList().clear();
				e.setCancelled(false);
				return;
			}
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onEntityDamage(EntityDamageByEntityEvent e) {
		if (Toolkit.inArena(e.getEntity())) {

			if (e.getEntity() instanceof Player && e.getDamager() instanceof Projectile) {

				Player damagedPlayer = (Player) e.getEntity();
				if (config.getBoolean("Arena.NoKitProtection")) {
					if (!arena.getKits().playerHasKit(damagedPlayer.getName())) {
						e.setCancelled(true);
						return;
					}
				}
				if (!(((Arrow) e.getDamager()).getShooter() instanceof Player))
					return;
				if (!config.getBoolean("PVPMode.Enable"))
					return;
				Player damager = (Player) ((Arrow) e.getDamager()).getShooter();
				if (damager.equals(damagedPlayer))
					return;
				long time = System.currentTimeMillis();
				arena.getStats().getOrCreateStatsCache(damagedPlayer).setLastPVPTime(time);
				arena.getStats().getOrCreateStatsCache(damagedPlayer).setLastPVPPlayer(damager);
				arena.getStats().getOrCreateStatsCache(damager).setLastPVPTime(time);
				arena.getStats().getOrCreateStatsCache(damager).setLastPVPPlayer(damagedPlayer);

			} else if (e.getEntity() instanceof Damageable) {

				if (e.getDamager().getType() == EntityType.TNT) {
					if (e.getEntity() instanceof ArmorStand || !(e.getEntity() instanceof LivingEntity)) {
						// for preventing breakage of paintings, item frames, etc.
						e.setCancelled(true);
					}
				}

			}
		}
	}

	@EventHandler
	public void onWeatherChange(WeatherChangeEvent e) {
		if (Toolkit.inArena(e.getWorld()) && config.getBoolean("Arena.KeepWeatherAtSunny")) {
			if (e.toWeatherState()) {
				e.setCancelled(true);
				e.getWorld().setStorm(false);
				e.getWorld().setThundering(false);
				e.getWorld().setWeatherDuration(0);
			}
		}
	}

	@EventHandler
	public void onExplosion(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			Player damagedPlayer = (Player) e.getEntity();

			if (Toolkit.inArena(damagedPlayer)) {

				if (e.getCause() == DamageCause.BLOCK_EXPLOSION || e.getCause() == DamageCause.ENTITY_EXPLOSION
						|| e.getCause() == DamageCause.FIRE || e.getCause() == DamageCause.FIRE_TICK) {
					if (config.getBoolean("Arena.NoKitProtection")) {
						if (!arena.getKits().playerHasKit(damagedPlayer.getName())) {
							e.setCancelled(true);
						}
					}

				} else if (e.getCause() == DamageCause.FALL) {

					if (config.getBoolean("Arena.PreventFallDamage")) {
						e.setCancelled(true);
						// only canceling if preventing fall damage is enabled, this allows for
						// WorldGuard to step in
					}

				} else if (damagedPlayer.getGameMode() == GameMode.SPECTATOR) {
					e.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		if (!Toolkit.inArena(p))
			return;
		if (e.getClickedBlock() == null)
			return;
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		Block b = e.getClickedBlock();
		if (b instanceof Chest) {
			if (!config.getBoolean("Arena.PreventChestOpen"))
				return;
			e.setCancelled(true);
			return;
		}
		if (b.getType() == Material.POTTED_CHERRY_SAPLING) {
			e.setCancelled(true);
			return;
		}
	}

	@EventHandler
	public void onPearl(PlayerTeleportEvent e) {
		Player p = e.getPlayer();
		if (Toolkit.inArena(p)) {
			if (e.getCause() == TeleportCause.ENDER_PEARL) {
				e.setCancelled(true);
				p.teleport(e.getTo());
			}
		}
	}

	@EventHandler
	public void onHangingEntityBreakByTNT(HangingBreakEvent e) {
		if (Toolkit.inArena(e.getEntity())) {
			if (e.getCause() == HangingBreakEvent.RemoveCause.EXPLOSION) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	void onItemCraft(CraftItemEvent e) {
		if (!config.getBoolean("Arena.PreventItemCraft"))
			return;
		if (!Toolkit.inArena(e.getWhoClicked()))
			return;
		if (e.getWhoClicked().hasPermission("Arena.PreventItemCraft"))
			return;
		e.setCancelled(true);
	}

	@EventHandler
	void onSignChange(SignChangeEvent e) {
		if (!config.getBoolean("Arena.PreventSignChange"))
			return;
		if (!Toolkit.inArena(e.getPlayer()))
			return;
		if (e.getPlayer().hasPermission("Arena.PreventSignChange"))
			return;
		e.setCancelled(true);
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	void onPickArrow(PlayerPickupArrowEvent e) {
		Player p = e.getPlayer();
		if (Toolkit.inArena(p)) {
			if (!arena.getKits().playerHasKit(p.getName())) {
				e.setCancelled(true);
				return;
			}
			List<String> kits = config.getStringList("Arena.ArrowKits");
			if (kits.contains(arena.getKits().getKitOfPlayer(p.getName()).getName()))
				return;
			e.setCancelled(true);
		}
	}

}
