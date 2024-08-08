package com.planetgallium.kitpvp.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import com.planetgallium.kitpvp.Game;
import com.planetgallium.kitpvp.game.Arena;
import com.planetgallium.kitpvp.util.CacheManager;
import com.planetgallium.kitpvp.util.EPCommand;
import com.planetgallium.kitpvp.util.Resource;
import com.planetgallium.kitpvp.util.Resources;
import com.planetgallium.kitpvp.util.Toolkit;

public class SpawnCommand extends EPCommand {

	private final List<String> spawnUsers = new ArrayList<>();

	private final Game plugin;
	private final Arena arena;
	private final Resources resources;
	private final Resource config;
	private final Resource messages;

	public SpawnCommand(Game game) {
		this.plugin = game;
		this.arena = game.getArena();
		this.resources = game.getResources();
		this.config = resources.getConfig();
		this.messages = resources.getMessages();
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(messages.fetchString("Messages.General.Player"));
			return;
		}
		Player p = (Player) sender;
		if (!p.hasPermission("kp.default")) {
			sender.sendMessage(
					messages.fetchString("Messages.General.Permission").replace("%permission%", "kp.default"));
			return;
		}
		if (!config.contains("Arenas." + p.getWorld().getName())) {
			p.sendMessage(messages.fetchString("Messages.Error.Arena").replace("%arena%", p.getWorld().getName()));
			return;
		}

		if (spawnUsers.contains(p.getName())) {
			return;
		}

		p.sendMessage(messages.fetchString("Messages.Commands.Teleporting"));
		spawnUsers.add(p.getName());
		Toolkit.playSoundToPlayer(p, "ENTITY_ITEM_PICKUP", -1);

		Location beforeLocation = p.getLocation();

		new BukkitRunnable() {
			public int time = config.getInt("Spawn.Time") + 1;

			@Override
			public void run() {
				time--;

				if (time != 0) {
					if (p.getGameMode() != GameMode.SPECTATOR) {
						if (beforeLocation.getBlockX() != p.getLocation().getBlockX()
								|| beforeLocation.getBlockY() != p.getLocation().getBlockY()
								|| beforeLocation.getBlockZ() != p.getLocation().getBlockZ()) {
							p.sendMessage(messages.fetchString("Messages.Error.Moved"));
							spawnUsers.remove(p.getName());
							cancel();
							return;
						}
						
						p.sendMessage(
								messages.fetchString("Messages.Commands.Time").replace("%time%", String.valueOf(time)));
						Toolkit.playSoundToPlayer(p, "BLOCK_NOTE_BLOCK_SNARE", 1);

					} else {
						spawnUsers.remove(p.getName());
						cancel();
					}
				} else {
					p.sendMessage(messages.fetchString("Messages.Commands.Teleport"));

					arena.toSpawn(p, p.getWorld().getName());

					if (config.getBoolean("Arena.ClearKitOnCommandSpawn")) {
						clearKit(p);
					}

					spawnUsers.remove(p.getName());
					Toolkit.playSoundToPlayer(p, "ENTITY_ENDERMAN_TELEPORT", 1);
					cancel();
				}
			}
		}.runTaskTimer(plugin, 0L, 20L);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args) {
		return null;
	}

	private void clearKit(Player p) {
		CacheManager.getPotionSwitcherUsers().remove(p.getName());

		p.getInventory().setArmorContents(null);
		p.getInventory().clear();

		Toolkit.setMaxHealth(p, 20);
		p.setHealth(20.0);

		for (PotionEffect effect : p.getActivePotionEffects()) {
			p.removePotionEffect(effect.getType());
		}

		if (config.getBoolean("Arena.GiveItemsOnClear")) {
			arena.giveArenaItems(p);
		}

		arena.getKits().resetPlayerKit(p.getName());
	}

}
