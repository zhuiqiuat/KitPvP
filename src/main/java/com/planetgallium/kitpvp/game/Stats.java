package com.planetgallium.kitpvp.game;

import com.cryptomorin.xseries.XSound;
import com.planetgallium.kitpvp.Game;
import com.planetgallium.kitpvp.api.PlayerLevelUpEvent;
import com.planetgallium.kitpvp.util.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class Stats {

	private final Game plugin;
	private final Infobase database;
	private final Resources resources;
	private final Resource levels;

	public Stats(Game plugin, Arena arena) {
		this.plugin = plugin;
		this.database = plugin.getDatabase();
		this.resources = plugin.getResources();
		this.levels = plugin.getResources().getLevels();
	}

	public void createPlayer(Player p) {
		CacheManager.getUUIDCache().put(p.getName(), p.getUniqueId().toString());
		database.registerPlayerStats(p);
	}

	public boolean isPlayerRegistered(Player p) {
		if (CacheManager.getStatsCache().containsKey(p)) { // try to use cache first to be faster
			return true;
		}
		return database.isPlayerRegistered(p);
	}

	public double getKDRatio(Player p) {
		if (getStat("deaths", p) != 0) {
			double divided = (double) getStat("kills", p) / getStat("deaths", p);
			return Toolkit.round(divided, 2);
		}
		return 0.00;
	}

	public void removeExperience(Player p, int amount) {
		if (levels.getBoolean("Levels.Levels.Enabled")) {
			int currentExperience = getStat("experience", p);
			setStat("experience", p, currentExperience >= amount ? currentExperience - amount : 0);
		}
	}

	public void addExperience(Player p, int experienceToAdd) {
		if (levels.getBoolean("Levels.Levels.Enabled")) {
			int currentExperience = getStat("experience", p);
			int newExperience = applyPossibleXPMultiplier(p, currentExperience + experienceToAdd);
			setStat("experience", p, newExperience);
			if (getStat("experience", p) >= getRegularOrRelativeNeededExperience(p)) {
				levelUp(p);
				Bukkit.getPluginManager().callEvent(new PlayerLevelUpEvent(p, getStat("level", p)));
			}
		}
	}

	private int applyPossibleXPMultiplier(Player p, int experience) {
		double xpMultiplier = Toolkit.getPermissionAmountDouble(p, "kp.xpmultiplier.", 1.0);
		return (int) (experience * xpMultiplier);
	}

	public void levelUp(Player p) {
		if (getStat("level", p) < levels.getInt("Levels.Options.Maximum-Level")) {

			int newLevel = getStat("level", p) + 1;
			setStat("level", p, newLevel);
			setStat("experience", p, 0);

			List<String> levelUpCommands = levels.getStringList("Levels.Commands-On-Level-Up");
			Toolkit.runCommands(p, levelUpCommands, "%level%", String.valueOf(newLevel));

			if (levels.contains("Levels.Levels." + newLevel + ".Commands")) {
				List<String> commandsList = levels.getStringList("Levels.Levels." + newLevel + ".Commands");
				Toolkit.runCommands(p, commandsList, "%level%", String.valueOf(newLevel));
			}

			p.sendMessage(resources.getMessages().fetchString("Messages.Other.Level").replace("%level%",
					String.valueOf(newLevel)));
			Toolkit.playSoundToPlayer(p, "ENTITY_PLAYER_LEVELUP", 1);

		} else {
			setStat("experience", p, 0);
		}
	}

	public void addToStat(String identifier, Player p, int amount) {
		int updatedAmount = getStat(identifier, p) + amount;
		setStat(identifier, p, updatedAmount);
	}

	public void setStat(String identifier, Player p, int data) {
		if (!isPlayerRegistered(p)) {
			return;
		}

		getOrCreateStatsCache(p).setData(identifier, data);
	}

	public void pushCachedStatsToDatabase(Player p, boolean removeFromCacheAfter) {
		new BukkitRunnable() {
			@Override
			public void run() {
				if (!CacheManager.getStatsCache().containsKey(p)) {
					return; // nothing to push if stats cache is empty
				}

				database.setStatsData(p, getOrCreateStatsCache(p));
				if (removeFromCacheAfter) {
					CacheManager.getStatsCache().remove(p);
				}
			}
		}.runTaskAsynchronously(plugin);
	}

	public int getStat(String identifier, Player p) {
		if (!isPlayerRegistered(p)) {
			return -1;
		}

		return getOrCreateStatsCache(p).getData(identifier);
	}

	public PlayerData getOrCreateStatsCache(Player p) {
		if (!isPlayerRegistered(p)) {
			return new PlayerData(p, -1, -1, -1, -1, -1);
		}

		if (!CacheManager.getStatsCache().containsKey(p)) {
			CacheManager.getStatsCache().put(p, database.getStatsData(p));
		}
		return CacheManager.getStatsCache().get(p);
	}

	public int getRegularOrRelativeNeededExperience(Player p) {
		int level = getStat("level", p);

		if (levels.contains("Levels.Levels." + level + ".Experience-To-Level-Up")) {
			return levels.getInt("Levels.Levels." + level + ".Experience-To-Level-Up");
		}
		return levels.getInt("Levels.Options.Experience-To-Level-Up");
	}

}
