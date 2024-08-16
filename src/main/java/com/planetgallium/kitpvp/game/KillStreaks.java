package com.planetgallium.kitpvp.game;

import java.util.HashMap;

import com.cryptomorin.xseries.messages.Titles;
import com.planetgallium.kitpvp.Game;
import com.planetgallium.kitpvp.util.*;

import lombok.Getter;

import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class KillStreaks implements Listener {

//	private final Resources resources;
	private final Resource killConfig;
	@Getter
	private final HashMap<Player, Integer> kills;

	public KillStreaks(Resources resources) {
//		this.resources = resources;
		this.killConfig = resources.getKillStreaks();
		this.kills = new HashMap<>();
	}

	public void runStreakCase(String streakType, Player p) {
		String username = p.getName();
		int streakNumber = getStreak(p);
		World world = p.getWorld();

		String pathPrefix = streakType + "." + streakNumber;

		if (killConfig.contains(pathPrefix)) {

			// TITLE
			if (killConfig.contains(pathPrefix + ".Title")) {
				String title = killConfig.fetchString(pathPrefix + ".Title.Title").replace("%player%", username)
						.replace("%streak%", String.valueOf(streakNumber));
				String subtitle = killConfig.fetchString(pathPrefix + ".Title.Subtitle").replace("%player%", username)
						.replace("%streak%", String.valueOf(streakNumber));

				for (Player local : world.getPlayers()) {
					Titles.sendTitle(local, 20, 60, 20, title, subtitle);
				}
			}

			// SOUND
			if (killConfig.contains(pathPrefix + ".Sound")) {
				Sound sound = Toolkit.safeSound(killConfig.fetchString(pathPrefix + ".Sound.Sound"));
				int pitch = killConfig.getInt(pathPrefix + ".Sound.Pitch");

				for (Player local : world.getPlayers()) {
					local.playSound(local.getLocation(), sound, 1, pitch);
				}
			}

			// MESSAGE
			if (killConfig.contains(pathPrefix + ".Message")) {
				String message = killConfig.fetchString(pathPrefix + ".Message.Message")
						.replace("%streak%", String.valueOf(streakNumber)).replace("%player%", username);

				for (Player local : world.getPlayers()) {
					local.sendMessage(message);
				}
			}

			// COMMANDS
			if (killConfig.contains(pathPrefix + ".Commands")) {
				Toolkit.runCommands(p, killConfig.getStringList(pathPrefix + ".Commands"), "none", "none");
			}

		}

	}

	public int getStreak(Player p) {
		if (!kills.containsKey(p)) {
			kills.put(p, 0);
		}

		return kills.get(p);
	}

	public void resetStreak(Player p) {
		if (kills.containsKey(p)) {
			runStreakCase("EndStreaks", p);
			kills.put(p, 0);
		}
	}

	public void setStreak(Player p, int streak) {
		kills.put(p, streak);
	}

	public void addStreak(Player p) {
		Arena arena = Game.getInstance().getArena();
		kills.put(p, kills.get(p) + 1);
		if (kills.get(p) > arena.getStats().getStat("maxstreaks", p)) {
			arena.getStats().setStat("maxstreaks", p, kills.get(p));
		}
		runStreakCase("KillStreaks", p);
	}

}
