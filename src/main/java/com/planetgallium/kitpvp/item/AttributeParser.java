package com.planetgallium.kitpvp.item;

import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XPotion;
import com.planetgallium.kitpvp.Game;
import com.planetgallium.kitpvp.util.Resource;
import com.planetgallium.kitpvp.util.Toolkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class AttributeParser {

	// PotionEffect
	private static final PotionEffectType FALLBACK_EFFECT_TYPE = PotionEffectType.NAUSEA;
	private static final int FALLBACK_EFFECT_DURATION_SECONDS = 10;
	private static final int FALLBACK_EFFECT_AMPLIFIER_NON_ZERO_BASED = 1;

	// ItemStack
	private static final Material FALLBACK_ITEM_MATERIAL = Material.BEDROCK;
	private static final int FALLBACK_ITEM_AMOUNT = 1;
	private static final Enchantment FALLBACK_ITEM_ENCHANTMENT = Enchantment.THORNS;
	private static final int FALLBACK_ITEM_ENCHANTMENT_AMPLIFIER = 1;

	public static List<PotionEffect> getEffectsFromPath(Resource resource, String path) {

		List<PotionEffect> effects = new ArrayList<>();
		ConfigurationSection effectsSection = resource.getConfigurationSection(path);

		if (effectsSection != null) {

			for (String effectName : effectsSection.getKeys(false)) {
				PotionEffectType type = XPotion.matchXPotion(effectName).get().getPotionEffectType();
				int amplifier = resource.getInt(path + "." + effectName + ".Amplifier");
				int duration = resource.getInt(path + "." + effectName + ".Duration");

				effects.add(new PotionEffect(type != null ? type : FALLBACK_EFFECT_TYPE,
						duration != 0 ? duration : FALLBACK_EFFECT_DURATION_SECONDS,
						amplifier != 0 ? amplifier : FALLBACK_EFFECT_AMPLIFIER_NON_ZERO_BASED));
			}

		}

		return effects;

	}

	public static ItemStack getItemStackFromPath(Resource resource, String path)
			throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		if (!resource.contains(path))
			return null;

		ItemStack item = XMaterial.matchXMaterial(FALLBACK_ITEM_MATERIAL).parseItem();
		ItemMeta meta = item.getItemMeta();

		// BASIC ITEM INFORMATION //

		if (resource.contains(path + ".Name")) {
			meta.setDisplayName(resource.fetchString(path + ".Name"));
		}

		if (resource.contains(path + ".Lore")) {
			meta.setLore(resource.getStringList(path + ".Lore"));
		}

		if (resource.contains(path + ".Material")) {
			String materialValue = resource.fetchString(path + ".Material");
			Optional<XMaterial> possibleMaterial = XMaterial.matchXMaterial(materialValue);

			if (possibleMaterial.isPresent()) {
				item.setType(possibleMaterial.get().parseMaterial());
			} else {
				Toolkit.printToConsole(String.format("&7[&b&lKIT-PVP&7] &cUnknown material [%s], defaulting to [%s].",
						materialValue, FALLBACK_ITEM_MATERIAL));
			}
		}

		if (resource.contains(path + ".Amount")) {
			item.setAmount(resource.getInt(path + ".Amount"));
		} else {
			item.setAmount(FALLBACK_ITEM_AMOUNT);
		}

		item.setItemMeta(meta);

		// CUSTOM ATTRIBUTES //

		if (resource.contains(path + ".Effects")) {
			item = setEffectsFromPath(item, resource, path);
		}

		if (Game.getInstance().getResources().getConfig().getBoolean("Arena.PreventItemDurabilityDamage")) {
			setUnbreakable(item);
		}

		if (resource.contains(path + ".Dye")) {
			dyeItem(item, Toolkit.getColorFromConfig(resource, path + ".Dye"));
		}

		if (resource.contains(path + ".Skull")) {
			setSkull(item, resource.fetchString(path + ".Skull"));
		}

		if (resource.contains(path + ".Durability")) {
			setCustomDurability(item, resource.getInt(path + ".Durability"));
		}

		if (resource.contains(path + ".Enchantments")) {
			Map<Enchantment, Integer> enchantments = new HashMap<>();
			ConfigurationSection section = resource.getConfigurationSection(path + ".Enchantments");

			for (String enchantmentName : section.getKeys(false)) {
				Enchantment enchantment = FALLBACK_ITEM_ENCHANTMENT;
				Optional<XEnchantment> enchantmentFromConfig = XEnchantment
						.matchXEnchantment(enchantmentName.toUpperCase());
				if (enchantmentFromConfig.isPresent()) {
					enchantment = enchantmentFromConfig.get().getEnchant();
				} else {
					Toolkit.printToConsole(String.format(
							"&7[&b&lKIT-PVP&7] &cUnknown enchantment [%s], defaulting to [THORNS].", enchantmentName));
				}
				int amplifier = resource.getInt(path + ".Enchantments." + enchantmentName);

				enchantments.put(enchantment != null ? enchantment : FALLBACK_ITEM_ENCHANTMENT,
						amplifier != 0 ? amplifier : FALLBACK_ITEM_ENCHANTMENT_AMPLIFIER);
			}

			item.addUnsafeEnchantments(enchantments);
		}

		return item;
	}

	private static void setCustomDurability(ItemStack item, int damageAmount) {
		if (Toolkit.versionToNumber() < 113) {
			item.setDurability((short) damageAmount);

		} else if (Toolkit.versionToNumber() >= 113) {
			ItemMeta meta = item.getItemMeta();

			if (meta instanceof Damageable) {
				((Damageable) meta).setDamage(damageAmount);
				item.setItemMeta(meta);
			}
		}
	}

	private static void setSkull(ItemStack item, String skullOwner) {
		item.setType(XMaterial.PLAYER_HEAD.parseMaterial());

		SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
		skullMeta.setOwner(skullOwner);

		item.setItemMeta(skullMeta);
	}

	private static void dyeItem(ItemStack item, Color color) {
		if (Toolkit.hasMatchingMaterial(item, "LEATHER_HELMET")
				|| Toolkit.hasMatchingMaterial(item, "LEATHER_CHESTPLATE")
				|| Toolkit.hasMatchingMaterial(item, "LEATHER_LEGGINGS")
				|| Toolkit.hasMatchingMaterial(item, "LEATHER_BOOTS")) {

			LeatherArmorMeta dyedMeta = (LeatherArmorMeta) item.getItemMeta();
			dyedMeta.setColor(color);
			item.setItemMeta(dyedMeta);
		}
	}

	private static void setUnbreakable(ItemStack item)
			throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
		if (Toolkit.versionToNumber() <= 114) {
			ItemMeta meta = item.getItemMeta();

			Method spigotMethod = meta.getClass().getMethod("spigot");
			spigotMethod.setAccessible(true);

			Object spigotInstance = spigotMethod.invoke(meta);
			Class spigotClass = spigotInstance.getClass();

			Method setUnbreakableMethod = spigotClass.getDeclaredMethod("setUnbreakable", boolean.class);
			setUnbreakableMethod.setAccessible(true);

			setUnbreakableMethod.invoke(spigotInstance, true);
		} else if (Toolkit.versionToNumber() > 114) {
			item.getItemMeta().setUnbreakable(true);
		}
	}

	private static ItemStack setEffectsFromPath(ItemStack item, Resource resource, String path) {
		String effectsPath = path + ".Effects";
		String firstChildEffectName = resource.getConfigurationSection(effectsPath).getKeys(false).toArray()[0]
				.toString();
		String firstChildPath = path + ".Effects." + firstChildEffectName;
		boolean isCustom = resource.contains(firstChildPath + ".Duration");

		if (Toolkit.versionToNumber() == 18 && !isCustom) {
			return setVanillaEffectsFromPath(item, resource, effectsPath);
		}

		if (isCustom) {
			setCustomEffectsFromPath(item, resource, effectsPath);
		} else {
			setVanillaEffectsFromPath(item, resource, effectsPath);
		}

		return item;
	}

	private static ItemStack setVanillaEffectsFromPath(ItemStack item, Resource resource, String path) {
		PotionMeta potionMeta = (PotionMeta) item.getItemMeta();

		String firstChildEffectName = resource.getConfigurationSection(path).getKeys(false).toArray()[0].toString();
		String firstChildPath = path + "." + firstChildEffectName;

		PotionType potionType = XPotion.matchXPotion(firstChildEffectName).get().getPotionType();
		boolean isUpgraded = resource.getBoolean(firstChildPath + ".Upgraded");
		boolean isExtended = resource.getBoolean(firstChildPath + ".Extended");

		if (Toolkit.versionToNumber() == 18) {
			boolean isSplash = resource.fetchString(path.replace("Effects", "") + ".Type").equals("SPLASH_POTION");
			Potion potion = Potion.fromItemStack(item);
			potion.setSplash(isSplash);
			potion.setType(potionType);
			potion.setLevel(getVanillaLevel(potionType, isUpgraded));
			if (isExtended)
				potion.setHasExtendedDuration(true);

			return potion.toItemStack(item.getAmount());
		} else if (Toolkit.versionToNumber() >= 19) {
			potionMeta.setBasePotionData(new PotionData(potionType, isExtended, isUpgraded));
		}

		item.setItemMeta(potionMeta);

		return item;
	}

	private static void setCustomEffectsFromPath(ItemStack item, Resource resource, String path) {
		PotionMeta potionMeta = (PotionMeta) item.getItemMeta();

		for (String effectName : resource.getConfigurationSection(path).getKeys(false)) {
			String specificPath = path + "." + effectName;
			potionMeta.addCustomEffect(new PotionEffect(PotionEffectType.getByName(effectName),
					resource.getInt(specificPath + ".Duration") * 20, resource.getInt(specificPath + ".Amplifier") - 1),
					true);
		}

		item.setItemMeta(potionMeta);
	}

	private static int getVanillaLevel(PotionType type, boolean isUpgraded) {
		switch (type) {
		case INVISIBILITY:
		case FIRE_RESISTANCE:
		case NIGHT_VISION:
		case SLOWNESS:
		case WATER_BREATHING:
		case WEAKNESS:
			return 1;
		case HARMING:
		case HEALING:
		case LEAPING:
		case POISON:
		case REGENERATION:
		case SWIFTNESS:
		case STRENGTH:
			return isUpgraded ? 2 : 1;
		default:
			return 1;
		}
	}

}
