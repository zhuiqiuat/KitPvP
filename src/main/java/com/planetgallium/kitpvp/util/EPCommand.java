package com.planetgallium.kitpvp.util;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public abstract class EPCommand implements CommandExecutor, TabCompleter {

	@Override
	public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
		onCommand(sender, args);
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		return onTabComplete(sender, args);
	}

	public abstract void onCommand(CommandSender sender, String[] args);

	public abstract List<String> onTabComplete(CommandSender sender, String[] args);

	public void sendMessage(CommandSender sender, String message) {
		sender.sendMessage(Toolkit.translate(message));
	}

}
