package com.planetgallium.kitpvp.util;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import lombok.Getter;
import lombok.Setter;

public class PlayerData {
	@Getter
	private final Player p;
	@Getter
	private final Map<String, Integer> data;
	@Getter
	private final Map<String, Long> kitCooldowns;
	@Getter
	@Setter
	private long lastPVPTime = 0;
	@Getter
	@Setter
	private Player lastPVPPlayer = null;

	public PlayerData(Player p, int kills, int deaths, int experience, int level, int maxstreaks) {
		this.p = p;
		this.data = new HashMap<>();
		this.kitCooldowns = new HashMap<>();

		data.put("kills", kills);
		data.put("deaths", deaths);
		data.put("experience", experience);
		data.put("level", level);
		data.put("maxstreaks", maxstreaks);
	}

	public void setData(String identifier, int value) {
		data.put(identifier, value);
	}

	public void addKitCooldown(String kitName, long timeKitLastUsed) {
		kitCooldowns.put(kitName, timeKitLastUsed);
	}

	public int getData(String identifier) {
		return data.get(identifier);
	}

	public long getTimeKitLastUsed(String kitName) {
		return kitCooldowns.get(kitName);
	}

}
