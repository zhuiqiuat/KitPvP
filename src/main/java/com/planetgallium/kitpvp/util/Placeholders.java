package com.planetgallium.kitpvp.util;

import com.planetgallium.kitpvp.Game;
import org.bukkit.entity.Player;
import com.planetgallium.kitpvp.game.Arena;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class Placeholders extends PlaceholderExpansion {

	private final Arena arena;
	private final Map<String, String> placeholderAPItoBuiltIn;

	public Placeholders(Game plugin) {
		this.arena = plugin.getArena();
		this.placeholderAPItoBuiltIn = new HashMap<>();

		placeholderAPItoBuiltIn.put("stats_kills", "%kills%");
		placeholderAPItoBuiltIn.put("stats_deaths", "%deaths%");
		placeholderAPItoBuiltIn.put("stats_kd", "%kd%");
		placeholderAPItoBuiltIn.put("stats_leaderboard_kd", "%leaderboard_kd%");
		placeholderAPItoBuiltIn.put("stats_experience", "%xp%");
		placeholderAPItoBuiltIn.put("stats_level", "%level%");
		placeholderAPItoBuiltIn.put("player_killstreaks", "%streaks%");
		placeholderAPItoBuiltIn.put("player_maxkillstreaks", "%maxstreaks%");
		placeholderAPItoBuiltIn.put("player_kit", "%kit%");
		placeholderAPItoBuiltIn.put("max_level", "%max_level%");
		placeholderAPItoBuiltIn.put("max_xp", "%max_xp%");
		placeholderAPItoBuiltIn.put("level_prefix", "%level_prefix%");
	}

	@Override
	public String onPlaceholderRequest(Player p, @NotNull String identifier) {

		if (p != null) {
			return translatePlaceholderAPIPlaceholders(identifier, p);
		}
		return null;
	}

	public String translatePlaceholderAPIPlaceholders(String placeholderAPIIdentifier, Player p) {
		if (placeholderAPItoBuiltIn.containsKey(placeholderAPIIdentifier)) {
			String toBuiltInPlaceholder = placeholderAPItoBuiltIn.get(placeholderAPIIdentifier);
			return arena.getUtilities().replaceBuiltInPlaceholdersIfPresent(toBuiltInPlaceholder, p);
		} else {
			Toolkit.printToConsole(String.format(
					"&7[&b&lKIT-PVP&7] &cUnknown placeholder identifier [%s]. " + "Please see plugin page.",
					placeholderAPIIdentifier));
			return "invalid-placeholder";
		}
	}

	@Override
	public boolean canRegister() {
		return true;
	}

	@Override
	public boolean persist() {
		return true;
	}

	@Override
	public @NotNull String getAuthor() {
		return "Cervinakuy";
	}

	@Override
	public @NotNull String getIdentifier() {
		return "kitpvp";
	}

	@Override
	public @NotNull String getVersion() {
		return "1.0.0";
	}

}
