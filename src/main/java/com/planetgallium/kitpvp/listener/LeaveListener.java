package com.planetgallium.kitpvp.listener;

import com.planetgallium.kitpvp.util.PlayerData;
import com.planetgallium.kitpvp.util.Resource;
import com.planetgallium.kitpvp.util.Resources;
import com.planetgallium.kitpvp.util.Toolkit;

import java.util.List;

import org.bukkit.Effect;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.planetgallium.kitpvp.Game;
import com.planetgallium.kitpvp.game.Arena;
import com.planetgallium.kitpvp.game.KillStreaks;

public class LeaveListener implements Listener {

	private final Game plugin;
	private final Arena arena;
	private final Resources resources;
	private final Resource config;

	public LeaveListener(Game plugin) {
		this.plugin = plugin;
		this.arena = plugin.getArena();
		this.resources = plugin.getResources();
		this.config = resources.getConfig();
	}

	@EventHandler
	public void onLeave(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		long time = System.currentTimeMillis();
		if (Toolkit.inArena(p)) {
			PlayerData pd = arena.getStats().getOrCreateStatsCache(p);
			if ((time - pd.getLastPVPTime()) < config.getInt("PVPMode.Time")) {
				Player victim = p;
				Player killer = pd.getLastPVPPlayer();
				if (killer == null) {
					arena.deletePlayer(p);
					return;
				}
				PlayerData pd2 = arena.getStats().getOrCreateStatsCache(killer);
				if (!killer.isOnline()) {
					arena.deletePlayer(p);
					return;
				}
				if (pd2.getLastPVPPlayer() == null) {
					arena.deletePlayer(p);
					return;
				}
				if (!pd2.getLastPVPPlayer().equals(victim)) {
					arena.deletePlayer(p);
					return;
				}
				if (!arena.getKits().playerHasKit(killer.getName())) {
					arena.deletePlayer(p);
					return;
				}
				if (!arena.getKits().playerHasKit(victim.getName())) {
					arena.deletePlayer(p);
					return;
				}
				creditWithKill(victim, killer);
				broadcast(victim.getWorld(), getDeathMessage(victim, killer, "PlayerQuit"));
				broadcast(victim.getWorld(), config.fetchString("Death.Sound.Sound"),
						config.getInt("Death.Sound.Pitch"));

				arena.getStats().addToStat("deaths", victim, 1);
				arena.getStats().removeExperience(victim,
						resources.getLevels().getInt("Levels.Options.Experience-Taken-On-Death"));

				if (config.getBoolean("Arena.DeathParticle")) {
					victim.getWorld().playEffect(victim.getLocation().add(0.0D, 1.0D, 0.0D), Effect.STEP_SOUND, 152);
				}

				Toolkit.runCommands(victim, config.getStringList("Death.Commands"), "%victim%", victim.getName());

			}
			arena.deletePlayer(p);
		}

	}

	@EventHandler
	public void onWorldChange(PlayerChangedWorldEvent e) {
		if (Toolkit.inArena(e.getFrom())) { // if they left from a kitpvp arena
			Player p = e.getPlayer();

			if (config.getBoolean("Arena.ClearInventoryOnLeave")) {
				p.getInventory().clear();
				p.getInventory().setArmorContents(null);
			}

			arena.removePlayer(p);
			// no need to clear stats from cache; that will be done on player quit above
		}
	}

	private String getDeathMessage(Player victim, Player killer, String type) {
		String deathMessage = config.fetchString("Death.Messages." + type);

		if (killer != null) {
			deathMessage = deathMessage.replace("%killer%", killer.getName()).replace("%killer_health%",
					String.valueOf(Toolkit.round(killer.getHealth(), 2)));
		} else {
			deathMessage = config.fetchString("Death.Messages.Unknown"); // if killer is null (left the server, or some
																			// other unknown reason)
		}

		if (victim != null) {
			deathMessage = deathMessage.replace("%victim%", victim.getName());
		}

		return deathMessage;
	}

	private void broadcast(World world, String message) {
		if (config.getBoolean("Death.Messages.Enabled")) {
			for (Player all : world.getPlayers()) {
				all.sendMessage(Toolkit.translate(message));
			}
		}
	}

	private void broadcast(World world, String soundName, int pitch) {
		if (config.getBoolean("Death.Sound.Enabled")) {
			for (Player all : world.getPlayers()) {
				Toolkit.playSoundToPlayer(all, soundName, pitch);
			}
		}
	}

	private void creditWithKill(Player victim, Player killer) {
		if (victim != null && killer != null) {
			if (!victim.getName().equals(killer.getName())) {
				arena.getStats().addToStat("kills", killer, 1);
				arena.getStats().addExperience(killer,
						resources.getLevels().getInt("Levels.Options.Experience-Given-On-Kill"));

				KillStreaks ks = arena.getKillStreaks();
				if (!killer.getName().equals(victim.getName()))
					ks.addStreak(killer);
				ks.resetStreak(victim);
				List<String> killCommands = config.getStringList("Kill.Commands");
				killCommands = Toolkit.replaceInList(killCommands, "%victim%", victim.getName());
				Toolkit.runCommands(killer, killCommands, "%killer%", killer.getName());

				if (resources.getScoreboard().getBoolean("Scoreboard.General.Enabled")) {
					new BukkitRunnable() {
						@Override
						public void run() {
							arena.updateScoreboards(killer, false);
						}
					}.runTaskLater(plugin, 20L);
				}
			}
		}
	}

}
