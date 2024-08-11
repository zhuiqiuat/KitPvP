package com.planetgallium.kitpvp.util;

import com.planetgallium.kitpvp.api.Kit;

import lombok.Getter;

import java.util.*;

public class CacheManager {

	private static final Map<String, String> usernameToUUID = new HashMap<>();
	@Getter
	private static final Map<String, Kit> kitCache = new HashMap<>();
	@Getter
	private static final Map<String, Menu> previewMenuCache = new HashMap<>();
	private static final Map<String, Map<String, Long>> abilityCooldowns = new HashMap<>();
	@Getter
	private static final Map<String, PlayerData> statsCache = new HashMap<>();
	@Getter
	private static final Set<String> potionSwitcherUsers = new HashSet<>();

	public static Map<String, String> getUUIDCache() {
		return usernameToUUID;
	}

	public static Map<String, Long> getPlayerAbilityCooldowns(String username) {
		if (!abilityCooldowns.containsKey(username)) {
			abilityCooldowns.put(username, new HashMap<>());
		}
		return abilityCooldowns.get(username);
	}

	public static void clearCaches() {
		kitCache.clear();
		previewMenuCache.clear();
		abilityCooldowns.clear();
		// stats, usernameToUUID, and cache isn't here as of right now
	}

}
