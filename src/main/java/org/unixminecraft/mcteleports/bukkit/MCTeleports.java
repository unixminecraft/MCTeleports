/*
 * MCTeleports Copyright (C) 2019-2020 unixminecraft
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */

package org.unixminecraft.mcteleports.bukkit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.unixminecraft.mcteleports.bukkit.data.Home;
import org.unixminecraft.mcteleports.bukkit.data.PlayerData;
import org.unixminecraft.mcteleports.bukkit.data.Spawn;
import org.unixminecraft.mcteleports.bukkit.data.Warp;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;

import net.md_5.bungee.api.ChatColor;

public final class MCTeleports extends JavaPlugin implements Listener {
	
	private static final String PERMISSION_COMMAND_HOME_SET_SELF = "mcteleports.command.home.set.self";
	private static final String PERMISSION_COMMAND_HOME_SET_OTHER = "mcteleports.command.home.set.other";
	private static final String PERMISSION_COMMAND_HOME_TELEPORT_SELF = "mcteleports.command.home.teleport.self";
	private static final String PERMISSION_COMMAND_HOME_TELEPORT_OTHER = "mcteleports.command.home.teleport.other";
	
	private static final String PERMISSION_COMMAND_SPAWN_SET = "mcteleports.command.spawn.set";
	private static final String PERMISSION_COMMAND_SPAWN_TELEPORT = "mcteleports.command.spawn.teleport";
	
	private static final String PERMISSION_COMMAND_WARP_USE = "mcteleports.command.warp.use";
	private static final String PERMISSION_COMMAND_WARP_SET = "mcteleports.command.warp.set";
	private static final String PERMISSION_COMMAND_WARP_TELEPORT = "mcteleports.command.warp.teleport";
	private static final String PERMISSION_COMMAND_WARP_LIST = "mcteleports.command.warp.list";
	private static final String PERMISSION_COMMAND_WARP_PERMISSION = "mcteleports.command.warp.permission";
	private static final String PERMISSION_COMMAND_WARP_REMOVE = "mcteleports.command.warp.remove";
	
	private static final String PLAYER_DIRECTORY_NAME = "Player_Data";
	private static final String HOME_DIRECTORY_NAME = "Home_Data";
	private static final String SPAWN_DIRECTORY_NAME = "Spawn_Data";
	private static final String WARP_DIRECTORY_NAME = "Warp_Data";
	
	private Logger logger;
	
	private MVWorldManager mvWorldManager;
	
	private File playerDirectory;
	private File homeDirectory;
	private File spawnDirectory;
	private File warpDirectory;
	
	private ConcurrentHashMap<String, UUID> playerNameToId;
	private ConcurrentHashMap<UUID, String> playerIdToName;
	private ConcurrentHashMap<UUID, Home> homes;
	private ConcurrentHashMap<String, Spawn> spawns;
	private ConcurrentHashMap<String, Warp> warps;
	
	private HashSet<Character> allowedCharacters;
	
	@Override
	public void onEnable() {
		
		logger = getLogger();
		
		displayLicenseInformation();
		
		final PluginManager pluginManager = getServer().getPluginManager();
		
		final Plugin possibleMultiverseCorePlugin = pluginManager.getPlugin("Multiverse-Core");
		if(possibleMultiverseCorePlugin == null) {
			
			logger.log(Level.SEVERE, "Multiverse-Core plugin not found, cannot start MCTeleports.");
			throw new RuntimeException("Multiverse-Core plugin not found, cannot start MCTeleports.");
		}
		if(!(possibleMultiverseCorePlugin instanceof MultiverseCore)) {
			
			logger.log(Level.SEVERE, "Possible Multiverse-Core plugin not the correct plugin, cannot start MCTeleports.");
			throw new RuntimeException("Possible Multiverse-Core plugin not the correct plugin, cannot start MCTeleports.");
		}
		
		final MultiverseCore multiverseCorePlugin = (MultiverseCore) possibleMultiverseCorePlugin;
		mvWorldManager = multiverseCorePlugin.getMVWorldManager();
		
		ConfigurationSerialization.registerClass(PlayerData.class);
		ConfigurationSerialization.registerClass(Home.class);
		ConfigurationSerialization.registerClass(Spawn.class);
		ConfigurationSerialization.registerClass(Warp.class);
		
		playerDirectory = new File(getDataFolder(), PLAYER_DIRECTORY_NAME);
		final String playerDirectoryPath = playerDirectory.getPath();
		
		try {
			if(!playerDirectory.exists()) {
				try {
					playerDirectory.mkdirs();
				}
				catch(SecurityException e) {
					
					logger.log(Level.SEVERE, "Unable to create player directory at " + playerDirectoryPath);
					logger.log(Level.SEVERE, "SecurityException thrown.", e);
					throw new RuntimeException("SecurityException thrown.", e);
				}
			}
		}
		catch(SecurityException e) {
			
			logger.log(Level.SEVERE, "Unable to verify player directory exists at " + playerDirectoryPath);
			logger.log(Level.SEVERE, "SecurityException thrown.", e);
			throw new RuntimeException("SecurityException thrown.", e);
		}
		
		homeDirectory = new File(getDataFolder(), HOME_DIRECTORY_NAME);
		final String homeDirectoryPath = homeDirectory.getPath();
		
		try {
			if(!homeDirectory.exists()) {
				try {
					homeDirectory.mkdirs();
				}
				catch(SecurityException e) {
					
					logger.log(Level.SEVERE, "Unable to create home directory at " + homeDirectoryPath);
					logger.log(Level.SEVERE, "SecurityException thrown.", e);
					throw new RuntimeException("SecurityException thrown.", e);
				}
			}
		}
		catch(SecurityException e) {
			
			logger.log(Level.SEVERE, "Unable to verify home directory exists at " + homeDirectoryPath);
			logger.log(Level.SEVERE, "SecurityException thrown.", e);
			throw new RuntimeException("SecurityException thrown.", e);
		}
		
		spawnDirectory = new File(getDataFolder(), SPAWN_DIRECTORY_NAME);
		final String spawnDirectoryPath = spawnDirectory.getPath();
		
		try {
			if(!spawnDirectory.exists()) {
				try {
					spawnDirectory.mkdirs();
				}
				catch(SecurityException e) {
					
					logger.log(Level.SEVERE, "Unable to create spawn directory at " + spawnDirectoryPath);
					logger.log(Level.SEVERE, "SecurityException thrown.", e);
					throw new RuntimeException("SecurityException thrown.", e);
				}
			}
		}
		catch(SecurityException e) {
			
			logger.log(Level.SEVERE, "Unable to verify spawn directory exists at " + spawnDirectoryPath);
			logger.log(Level.SEVERE, "SecurityException thrown.", e);
			throw new RuntimeException("SecurityException thrown.", e);
		}
		
		warpDirectory = new File(getDataFolder(), WARP_DIRECTORY_NAME);
		final String warpDirectoryPath = warpDirectory.getPath();
		
		try {
			if(!warpDirectory.exists()) {
				try {
					warpDirectory.mkdirs();
				}
				catch(SecurityException e) {
					
					logger.log(Level.SEVERE, "Unable to create warp directory at " + warpDirectoryPath);
					logger.log(Level.SEVERE, "SecurityException thrown.", e);
					throw new RuntimeException("SecurityException thrown.", e);
				}
			}
		}
		catch(SecurityException e) {
			
			logger.log(Level.SEVERE, "Unable to verify warp directory exists at " + warpDirectoryPath);
			logger.log(Level.SEVERE, "SecurityException thrown.", e);
			throw new RuntimeException("SecurityException thrown.", e);
		}
		
		playerNameToId = new ConcurrentHashMap<String, UUID>();
		playerIdToName = new ConcurrentHashMap<UUID, String>();
		homes = new ConcurrentHashMap<UUID, Home>();
		spawns = new ConcurrentHashMap<String, Spawn>();
		warps = new ConcurrentHashMap<String, Warp>();
		
		for(final File playerConfigurationFile : playerDirectory.listFiles()) {
			
			final String playerConfigurationFilePath = playerConfigurationFile.getPath();
			final YamlConfiguration playerConfiguration = new YamlConfiguration();
			
			try {
				playerConfiguration.load(playerConfigurationFile);
			}
			catch(FileNotFoundException e) {
				
				logger.log(Level.WARNING, "Unable to load player configuration file at " + playerConfigurationFilePath);
				logger.log(Level.WARNING, "Skipping player.");
				logger.log(Level.WARNING, "FileNotFoundException thrown.", e);
				continue;
			}
			catch(IOException e) {
				
				logger.log(Level.WARNING, "Unable to load player configuration file at " + playerConfigurationFilePath);
				logger.log(Level.WARNING, "Skipping player.");
				logger.log(Level.WARNING, "IOException thrown.", e);
				continue;
			}
			catch(InvalidConfigurationException e) {
				
				logger.log(Level.WARNING, "Unable to load player configuration file at " + playerConfigurationFilePath);
				logger.log(Level.WARNING, "Skipping player.");
				logger.log(Level.WARNING, "InvalidConfigurationException thrown.", e);
				continue;
			}
			catch(IllegalArgumentException e) {
				
				logger.log(Level.WARNING, "Unable to load player configuration file at " + playerConfigurationFilePath);
				logger.log(Level.WARNING, "Skipping player.");
				logger.log(Level.WARNING, "IllegalArgumentException thrown.", e);
				continue;
			}
			
			final PlayerData playerData = playerConfiguration.getSerializable("player_data", PlayerData.class);
			
			playerNameToId.put(playerData.getName(), playerData.getUniqueId());
			playerIdToName.put(playerData.getUniqueId(), playerData.getName());
		}
		
		for(final File homeConfigurationFile : homeDirectory.listFiles()) {
			
			final String homeConfigurationFilePath = homeConfigurationFile.getPath();
			final YamlConfiguration homeConfiguration = new YamlConfiguration();
			
			try {
				homeConfiguration.load(homeConfigurationFile);
			}
			catch(FileNotFoundException e) {
				
				logger.log(Level.WARNING, "Unable to load home configuration file at " + homeConfigurationFilePath);
				logger.log(Level.WARNING, "Skipping home.");
				logger.log(Level.WARNING, "FileNotFoundException thrown.", e);
				continue;
			}
			catch(IOException e) {
				
				logger.log(Level.WARNING, "Unable to load home configuration file at " + homeConfigurationFilePath);
				logger.log(Level.WARNING, "Skipping home.");
				logger.log(Level.WARNING, "IOException thrown.", e);
				continue;
			}
			catch(InvalidConfigurationException e) {
				
				logger.log(Level.WARNING, "Unable to load home configuration file at " + homeConfigurationFilePath);
				logger.log(Level.WARNING, "Skipping home.");
				logger.log(Level.WARNING, "InvalidConfigurationException thrown.", e);
				continue;
			}
			catch(IllegalArgumentException e) {
				
				logger.log(Level.WARNING, "Unable to load home configuration file at " + homeConfigurationFilePath);
				logger.log(Level.WARNING, "Skipping home.");
				logger.log(Level.WARNING, "IllegalArgumentException thrown.", e);
				continue;
			}
			
			final ConfigurationSection homeConfigurationSection = homeConfiguration.getConfigurationSection("home");
			final String playerIdValue = homeConfigurationSection.getString("uuid");
			final String worldName = getWorldName(homeConfigurationSection);
			
			if(!isWorldLoaded(worldName)) {
				
				logger.log(Level.INFO, "Unable to load home for UUID " + playerIdValue);
				logger.log(Level.INFO, "Skipping home.");
				logger.log(Level.INFO, "World " + worldName + " is not loaded.");
				continue;
			}
			
			final Home home = homeConfiguration.getSerializable("home", Home.class);
			
			homes.put(home.getUniqueId(), home);
		}
		
		for(final File spawnConfigurationFile : spawnDirectory.listFiles()) {
			
			final String spawnConfigurationFilePath = spawnConfigurationFile.getPath();
			final YamlConfiguration spawnConfiguration = new YamlConfiguration();
			
			try {
				spawnConfiguration.load(spawnConfigurationFile);
			}
			catch(FileNotFoundException e) {
				
				logger.log(Level.WARNING, "Unable to load spawn configuration file at " + spawnConfigurationFilePath);
				logger.log(Level.WARNING, "Skipping spawn.");
				logger.log(Level.WARNING, "FileNotFoundException thrown.", e);
				continue;
			}
			catch(IOException e) {
				
				logger.log(Level.WARNING, "Unable to load spawn configuration file at " + spawnConfigurationFilePath);
				logger.log(Level.WARNING, "Skipping spawn.");
				logger.log(Level.WARNING, "IOException thrown.", e);
				continue;
			}
			catch(InvalidConfigurationException e) {
				
				logger.log(Level.WARNING, "Unable to load spawn configuration file at " + spawnConfigurationFilePath);
				logger.log(Level.WARNING, "Skipping spawn.");
				logger.log(Level.WARNING, "InvalidConfigurationException thrown.", e);
				continue;
			}
			catch(IllegalArgumentException e) {
				
				logger.log(Level.WARNING, "Unable to load spawn configuration file at " + spawnConfigurationFilePath);
				logger.log(Level.WARNING, "Skipping spawn.");
				logger.log(Level.WARNING, "IllegalArgumentException thrown.", e);
				continue;
			}
			
			final ConfigurationSection spawnConfigurationSection = spawnConfiguration.getConfigurationSection("spawn");
			final String worldName = getWorldName(spawnConfigurationSection);
			
			if(!isWorldLoaded(worldName)) {
				
				logger.log(Level.INFO, "Unable to load spawn for world " + worldName);
				logger.log(Level.INFO, "Skipping spawn.");
				logger.log(Level.INFO, "World " + worldName + " is not loaded.");
				continue;
			}
			
			final Spawn spawn = spawnConfiguration.getSerializable("spawn", Spawn.class);
			
			spawns.put(spawn.getLocation().getWorld().getName(), spawn);
		}
		
		for(final File warpConfigurationFile : warpDirectory.listFiles()) {
			
			final String warpConfigurationFilePath = warpConfigurationFile.getPath();
			final YamlConfiguration warpConfiguration = new YamlConfiguration();
			
			try {
				warpConfiguration.load(warpConfigurationFile);
			}
			catch(FileNotFoundException e) {
				
				logger.log(Level.WARNING, "Unable to load warp configuration file at " + warpConfigurationFilePath);
				logger.log(Level.WARNING, "Skipping warp.");
				logger.log(Level.WARNING, "FileNotFoundException thrown.", e);
				continue;
			}
			catch(IOException e) {
				
				logger.log(Level.WARNING, "Unable to load warp configuration file at " + warpConfigurationFilePath);
				logger.log(Level.WARNING, "Skipping warp.");
				logger.log(Level.WARNING, "IOException thrown.", e);
				continue;
			}
			catch(InvalidConfigurationException e) {
				
				logger.log(Level.WARNING, "Unable to load warp configuration file at " + warpConfigurationFilePath);
				logger.log(Level.WARNING, "Skipping warp.");
				logger.log(Level.WARNING, "InvalidConfigurationException thrown.", e);
				continue;
			}
			catch(IllegalArgumentException e) {
				
				logger.log(Level.WARNING, "Unable to load warp configuration file at " + warpConfigurationFilePath);
				logger.log(Level.WARNING, "Skipping warp.");
				logger.log(Level.WARNING, "IllegalArgumentException thrown.", e);
				continue;
			}
			
			final ConfigurationSection warpConfigurationSection = warpConfiguration.getConfigurationSection("warp");
			final String warpName = warpConfigurationSection.getString("name");
			final String worldName = getWorldName(warpConfigurationSection);
			
			if(!isWorldLoaded(worldName)) {
				
				logger.log(Level.INFO, "Unable to load warp " + warpName);
				logger.log(Level.INFO, "Skipping warp.");
				logger.log(Level.INFO, "World " + worldName + " is not loaded.");
				continue;
			}
			
			final Warp warp = warpConfiguration.getSerializable("warp", Warp.class);
			
			warps.put(warp.getName().toLowerCase(), warp);
		}
		
		pluginManager.registerEvents(this, this);
		
		allowedCharacters = new HashSet<Character>();
		
		String allowedCharacterValues = "";
		
		allowedCharacterValues += "abcdefghijklmnopqrstuvwxyz";
		allowedCharacterValues += "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		allowedCharacterValues += "0123456789";
		allowedCharacterValues += "-_";
		
		for(int index = 0; index < allowedCharacterValues.length(); index++) {
			allowedCharacters.add(Character.valueOf(allowedCharacterValues.charAt(index)));
		}
	}
	
	@Override
	public boolean onCommand(final CommandSender commandSender, final Command command, final String label, final String[] parameters) {
		
		final HashSet<String> commands = new HashSet<String>();
		
		commands.add("sethome");
		commands.add("home");
		commands.add("setspawn");
		commands.add("spawn");
		commands.add("warp");
		
		if(commands.contains(command.getName())) {
			if(!(commandSender instanceof Player)) {
				
				commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cNo permission.&r"));
				return true;
			}
		}
		
		final Player player = (Player) commandSender;
		
		if(command.getName().equals("sethome")) {
			
			if(parameters.length == 0) {
				
				if(!player.hasPermission(PERMISSION_COMMAND_HOME_SET_SELF)) {
					sendMessage(player, "&cNo permission.&r");
					return true;
				}
				
				final Home home = new Home(player);
				homes.put(home.getUniqueId(), home);
				
				sendMessage(player, "&aHome set.&r");
				
				if(!save(home)) {
					sendMessage(player, "&cError saving home, please contact a server administrator.&r");
				}
				
				return true;
			}
			else if(parameters.length == 1) {
				
				if(!player.hasPermission(PERMISSION_COMMAND_HOME_SET_OTHER)) {
					sendMessage(player, "&cNo permission.&r");
					return true;
				}
				
				final String playerName = parameters[0];
				final UUID playerId;
				final Home home;
				
				if(playerName.equalsIgnoreCase(player.getName())) {
					playerId = player.getUniqueId();
				}
				else if(!playerNameToId.containsKey(playerName.toLowerCase())) {
					sendMessage(player, "&cPlayer&r &6" + playerName + "&r &cnot found.&r");
					return true;
				}
				else {
					playerId = playerNameToId.get(playerName.toLowerCase());
				}
				
				if(homes.containsKey(playerId)) {
					home = homes.get(playerId);
				}
				else {
					home = new Home(playerId, player.getLocation());
				}
				
				home.setLocation(player.getLocation());
				homes.put(playerId, home);
				
				sendMessage(player, "&aHome set.&r");
				
				if(!save(home)) {
					sendMessage(player, "&cError saving home, please contact a server administrator.&r");
				}
				
				return true;
			}
		    else {
		    	
		    	if(player.hasPermission(PERMISSION_COMMAND_HOME_SET_OTHER)) {
		    		sendMessage(player, "&cSyntax: /sethome [&r&c&oplayer name&r&c]&r");
		    	}
		    	else if(player.hasPermission(PERMISSION_COMMAND_HOME_SET_SELF)) {
		    		sendMessage(player, "&cSyntax: /sethome&r");
		    	}
		    	else {
					sendMessage(player, "&cNo permission.&r");
		    	}
		    	
				return true;
			}
		}
		else if(command.getName().equals("home")) {
			
			if(parameters.length == 0) {
				
				if(!player.hasPermission(PERMISSION_COMMAND_HOME_TELEPORT_SELF)) {
					sendMessage(player, "&cNo permission.&r");
					return true;
				}
				
				if(!homes.containsKey(player.getUniqueId())) {
					sendMessage(player, "&cNo home set.&r");
					return true;
				}
				
				final Home home = homes.get(player.getUniqueId());
				if(!player.teleport(home.getLocation())) {
					sendMessage(player, "&cError teleporting to your home. Please contact a server administrator.&r");
					return true;
				}
				
				sendMessage(player, "&aTeleported.&r");
				return true;
			}
			else if(parameters.length == 1) {
				
				if(!player.hasPermission(PERMISSION_COMMAND_HOME_TELEPORT_OTHER)) {
					sendMessage(player, "&cNo permission.&r");
					return true;
				}
				
				final String playerName = parameters[0];
				final boolean samePlayer;
				final UUID playerId;
				
				if(playerName.equalsIgnoreCase(player.getName())) {
					samePlayer = true;
					playerId = player.getUniqueId();
				}
				else if(!playerNameToId.containsKey(playerName.toLowerCase())) {
					sendMessage(player, "&cPlayer&r &6" + playerName + "&r &cnot found.&r");
					return true;
				}
				else {
					samePlayer = false;
					playerId = playerNameToId.get(playerName.toLowerCase());
				}
				
				if(!homes.containsKey(player.getUniqueId())) {
					sendMessage(player, "&cNo home set.&r");
					return true;
				}
				
				final Home home = homes.get(player.getUniqueId());
				if(!player.teleport(home.getLocation())) {
					if(samePlayer) {
						sendMessage(player, "&cError teleporting to your home. Please contact a server administrator.&r");
					}
					else {
						sendMessage(player, "&cError teleporting to&r &6" + playerIdToName.get(playerId) + "'s&r &chome. Please contact a server administrator.&r");
					}
					return true;
				}
				
				sendMessage(player, "&aTeleported.&r");
				return true;
			}
		    else {
		    	
		    	if(player.hasPermission(PERMISSION_COMMAND_HOME_TELEPORT_OTHER)) {
		    		sendMessage(player, "&cSyntax: /home [&r&c&oplayer name&r&c]&r");
		    	}
		    	else if(player.hasPermission(PERMISSION_COMMAND_HOME_TELEPORT_SELF)) {
		    		sendMessage(player, "&cSyntax: /home&r");
		    	}
		    	else {
		    		sendMessage(player, "&cNo permission.&r");
		    	}
		    	
				return true;
			}
		}
		else if(command.getName().equals("setspawn")) {
			
			if(!player.hasPermission(PERMISSION_COMMAND_SPAWN_SET)) {
				sendMessage(player, "&cNo permission.&r");
				return true;
			}
			
			if(parameters.length != 0) {
				sendMessage(player, "&cSyntax: /setspawn&r");
				return true;
			}
			
			final Spawn spawn = new Spawn(player.getLocation());
			spawns.put(spawn.getLocation().getWorld().getName(), spawn);
			
			sendMessage(player, "&aSpawn set.&r");
			
			if(!save(spawn)) {
				sendMessage(player, "&cError saving spawn for this world, please contact a server administrator.&r");
			}
			
			return true;
		}
		else if(command.getName().equals("spawn")) {
			
			if(!player.hasPermission(PERMISSION_COMMAND_SPAWN_TELEPORT)) {
				sendMessage(player, "&cNo permission.&r");
				return true;
			}
			
			final String worldName;
			
			if(parameters.length == 0) {
				worldName = player.getLocation().getWorld().getName();
			}
			else if(parameters.length == 1) {
				worldName = parameters[0];
			}
			else {
				sendMessage(player, "&cSyntax: /spawn [&r&c&oworld name&r&c]&r");
				return true;
			}
			
			if(!spawns.containsKey(worldName)) {
				sendMessage(player, "&cNo spawn set for this world.&r");
				return true;
			}
			
			final Spawn spawn = spawns.get(worldName);
			if(!player.teleport(spawn.getLocation())) {
				sendMessage(player, "&cError teleporting to the world spawn. Please contact a server administrator.&r");
				return true;
			}
			
			sendMessage(player, "&aTeleported.&r");
			return true;
		}
		else if(command.getName().equals("warp")) {
			
			if(!player.hasPermission(PERMISSION_COMMAND_WARP_USE)) {
				sendMessage(player, "&cNo permission.&r");
				return true;
			}
			
			if(parameters.length == 0) {
				sendMessage(player, "&bThis is the /warp command.&r");
				return true;
			}
			
			final String argument1 = parameters[0];
			if(argument1.equalsIgnoreCase("set")) {
				
				if(!player.hasPermission(PERMISSION_COMMAND_WARP_SET)) {
					sendMessage(player, "&cNo permission.&r");
					return true;
				}
				
				if(parameters.length != 2) {
					sendMessage(player, "&cSyntax: /warp set <&r&c&owarp name&r&c>&r");
					return true;
				}
				
				final String warpName = parameters[1].toLowerCase();
				
				for(int index = 0; index < warpName.length(); index++) {
					if(!allowedCharacters.contains(Character.valueOf(warpName.charAt(index)))) {
						sendMessage(player, "&cYou cannot use the following character in a warp name:&r &6" + String.valueOf(warpName.charAt(index)) + "&r&c.&r");
						return true;
					}
				}
				
				final HashSet<String> disallowedNames = new HashSet<String>();
				
				disallowedNames.add("set");
				disallowedNames.add("list");
				disallowedNames.add("permission");
				disallowedNames.add("remove");
				
				if(disallowedNames.contains(warpName)) {
					sendMessage(player, "&cYou may not use that name for a warp.&r");
					return true;
				}
				
				final Warp warp;
				final boolean alreadyExists;
				
				if(warps.containsKey(warpName)) {
					
					warp = warps.get(warpName);
					warp.setLocation(player.getLocation());
					alreadyExists = true;
				}
				else {
					
					warp = new Warp(warpName, player);
					alreadyExists = false;
				}
				
				warps.put(warpName, warp);
				
				if(alreadyExists) {
					sendMessage(player, "&aWarp updated.&r");
				}
				else {
					sendMessage(player, "&aWarp created.&r");
				}
				
				if(!save(warp)) {
					sendMessage(player, "&cError saving warp, please contact a server administrator.&r");
				}
				
				return true;
			}
			else if(argument1.equalsIgnoreCase("list")) {
				
				if(!player.hasPermission(PERMISSION_COMMAND_WARP_LIST)) {
					sendMessage(player, "&cNo permission.&r");
					return true;
				}
				
				if(parameters.length != 1) {
					sendMessage(player, "&cSyntax: /warp list&r");
					return true;
				}
				
				String response = "";
				final Iterator<Warp> warpIterator = warps.values().iterator();
				
				while(warpIterator.hasNext()) {
					
					if(response.length() > 0) {
						response += "&b,&r ";
					}
					
					response += "&a" + warpIterator.next().getName() + "&r";
					
					if(warpIterator.hasNext()) {
						response += "&a,&r &b" + warpIterator.next().getName() + "&r";
					}
					else {
						break;
					}
				}
				
				sendMessage(player, "&6Warps&r");
				sendMessage(player, "&8----------------&r");
				sendMessage(player, response);
				return true;
			}
			else if(argument1.equalsIgnoreCase("permission")) {
				
				if(!player.hasPermission(PERMISSION_COMMAND_WARP_PERMISSION)) {
					sendMessage(player, "&cNo permission.&r");
					return true;
				}
				
				if(parameters.length != 3) {
					sendMessage(player, "&cSyntax: /warp permission <&r&c&owarp name&r&c> <true|false>&r");
					return true;
				}
				
				final String warpName = parameters[1].toLowerCase();
				final String permissionEnabledValue = parameters[2].toLowerCase();
				final boolean permissionEnabled;
				
				if(!warps.containsKey(warpName)) {
					sendMessage(player, "&cWarp&r &6" + warpName + "&r &cnot found.&r");
					return true;
				}
				
				if(permissionEnabledValue.equals("true")) {
					permissionEnabled = true;
				}
				else if(permissionEnabledValue.equals("false")) {
					permissionEnabled = false;
				}
				else {
					sendMessage(player, "&cSyntax: /warp permission <&r&c&owarp name&r&c> <true|false>&r");
					return true;
				}
				
				final Warp warp = warps.get(warpName);
				if(permissionEnabled == warp.getPermissionEnabled()) {
					
					sendMessage(player, "&bWarp&r &6" + warpName + "&r &balready has permission enabled set to&r &6" + String.valueOf(permissionEnabled) + "&r&b.&r");
					return true;
				}
				else {
					
					warp.setPermissionEnabled(permissionEnabled);
					warps.put(warpName, warp);
					
					sendMessage(player, "&aWarp updated.&r");
					
					if(!save(warp)) {
						sendMessage(player, "&cError saving warp, please contact a server administrator.&r");
					}
					
					return true;
				}
			}
			else if(argument1.equalsIgnoreCase("remove")) {
				
				if(!player.hasPermission(PERMISSION_COMMAND_WARP_REMOVE)) {
					sendMessage(player, "&cNo permission.&r");
					return true;
				}
				
				if(parameters.length != 2) {
					sendMessage(player, "&cSyntax: /warp remove <&r&c&owarp name&r&c>&r");
					return true;
				}
				
				final String warpName = parameters[1].toLowerCase();
				if(!warps.containsKey(warpName)) {
					sendMessage(player, "&cWarp&r &6" + warpName + "&r &cnot found.&r");
					return true;
				}
				
				warps.remove(warpName);
				
				final File warpConfigurationFile = new File(warpDirectory, warpName + ".yml");
				final String warpConfigurationFilePath = warpConfigurationFile.getPath();
				
				try {
					if(warpConfigurationFile.exists()) {
						try {
							if(warpConfigurationFile.delete()) {
								
								sendMessage(player, "&aWarp removed.&r");
								return true;
							}
							else {
								
								sendMessage(player, "&cError while attempting to remove warp, please contact a server administrator.&r");
								logger.log(Level.WARNING, "Warp configuration file not deleted from " + warpConfigurationFilePath);
								return true;
							}
						}
						catch(SecurityException e) {
							
							sendMessage(player, "&cError while attempting to remove warp, please contact a server administrator.&r");
							logger.log(Level.WARNING, "Warp configuration file not deleted from " + warpConfigurationFilePath);
							logger.log(Level.WARNING, "SecurityException thrown.", e);
							return true;
						}
					}
					else {
						
						sendMessage(player, "&cError while attempting to remove warp, please contact a server administrator.&r");
						logger.log(Level.WARNING, "Warp configuration file does not exist, warp exists.");
						logger.log(Level.WARNING, "Warp configuration file location is " + warpConfigurationFilePath);
						return true;
					}
				}
				catch(SecurityException e) {
					
					sendMessage(player, "&cError while attempting to remove warp, please contact a server administrator.&r");
					logger.log(Level.WARNING, "Warp configuration file not validated to exist at " + warpConfigurationFilePath);
					logger.log(Level.WARNING, "SecurityException thrown.", e);
					return true;
				}
			}
			else {
				
				if(!player.hasPermission(PERMISSION_COMMAND_WARP_TELEPORT)) {
					sendMessage(player, "&cNo permission.&r");
					return true;
				}
				
				if(!warps.containsKey(argument1.toLowerCase())) {
					sendMessage(player, "&cNo warp&r &6" + argument1 + "&r &cfound.&r");
					return true;
				}
				
				final Warp warp = warps.get(argument1.toLowerCase());
				if(warp.getPermissionEnabled() && !player.hasPermission(warp.getPermission())) {
					sendMessage(player, "&cNo permission.&r");
					return true;
				}
				
				if(!player.teleport(warp.getLocation())) {
					sendMessage(player, "&cError teleporting to the warp. Please contact a server administrator.&r");
					return true;
				}
				
				sendMessage(player, "&aTeleported.&r");
				return true;
			}
		}
		else {
			
			sendMessage(player, "&cInternal error, command not properly registered to MCTeleports.&r");
			return true;
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(final PlayerJoinEvent event) {
		
		final Player player = event.getPlayer();
		final String playerName = player.getName();
		final UUID playerId = player.getUniqueId();
		
		if(!playerIdToName.containsKey(playerId)) {
			
			playerNameToId.put(playerName.toLowerCase(), playerId);
			playerIdToName.put(playerId, playerName);
			
			logger.log(Level.INFO, "New player login.");
			logger.log(Level.INFO, "Name: " + playerName);
			logger.log(Level.INFO, "UUID: " + playerId.toString());
			
			if(save(new PlayerData(player))) {
				logger.log(Level.INFO, "Save for UUID successful: " + playerId.toString());
			}
			else {
				logger.log(Level.WARNING, "Save for UUID unsuccessful: " + playerId.toString());
			}
		}
		else if(!playerNameToId.containsKey(playerName.toLowerCase())) {
			
			final String oldPlayerName = playerIdToName.get(playerId);
			playerNameToId.remove(oldPlayerName.toLowerCase());
			
			playerNameToId.put(playerName.toLowerCase(), playerId);
			playerIdToName.put(playerId, playerName);
			
			logger.log(Level.INFO, "Name change player login.");
			logger.log(Level.INFO, "Old Name: " + oldPlayerName);
			logger.log(Level.INFO, "New Name: " + playerName);
			logger.log(Level.INFO, "UUID: " + playerId.toString());
			
			if(save(new PlayerData(player))) {
				logger.log(Level.INFO, "Save for UUID successful: " + playerId.toString());
			}
			else {
				logger.log(Level.WARNING, "Save for UUID unsuccessful: " + playerId.toString());
			}
		}
		else {
			
			logger.log(Level.INFO, "Normal player login.");
			logger.log(Level.INFO, "Name: " + playerName);
			logger.log(Level.INFO, "UUID: " + playerId.toString());
		}
	}
	
	private String getWorldName(final ConfigurationSection configurationSection) {
		
		return configurationSection.getConfigurationSection("location").getString("world");
	}
	
	private boolean isWorldLoaded(final String worldName) {
		
		final MultiverseWorld multiverseWorld = mvWorldManager.getMVWorld(worldName);
		
		if(multiverseWorld == null) {
			return false;
		}
		
		return multiverseWorld.getAutoLoad();
	}
	
	private void sendMessage(final Player player, final String message) {
		
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
	}
	
	private boolean save(final PlayerData playerData) {
		
		final YamlConfiguration playerDataConfiguration = new YamlConfiguration();
		
		playerDataConfiguration.set("player_data", playerData);
		
		final File playerDataConfigurationFile = new File(playerDirectory, playerData.getUniqueId().toString() + ".yml");
		final String playerDataConfigurationFilePath = playerDataConfigurationFile.getPath();
		
		try {
			if(!playerDataConfigurationFile.exists()) {
				try {
					playerDataConfigurationFile.createNewFile();
				}
				catch(IOException e) {
					
					logger.log(Level.WARNING, "Unable to create player data configuration file at " + playerDataConfigurationFilePath);
					logger.log(Level.WARNING, "IOException thrown.", e);
					return false;
				}
				catch(SecurityException e) {
					
					logger.log(Level.WARNING, "Unable to create player data configuration file at " + playerDataConfigurationFilePath);
					logger.log(Level.WARNING, "SecurityException thrown.", e);
					return false;
				}
			}
		}
		catch(SecurityException e) {
			
			logger.log(Level.WARNING, "Unable to verify if player data configuration file exists at " + playerDataConfigurationFilePath);
			logger.log(Level.WARNING, "SecurityException thrown.", e);
			return false;
		}
		
		try {
			playerDataConfiguration.save(playerDataConfigurationFile);
		}
		catch(IOException e) {
			
			logger.log(Level.WARNING, "Unable to save player data configuration file at " + playerDataConfigurationFilePath);
			logger.log(Level.WARNING, "IOException thrown.", e);
			return false;
		}
		
		return true;
	}
	
	private boolean save(final Home home) {
		
		final YamlConfiguration homeConfiguration = new YamlConfiguration();
		
		homeConfiguration.set("home", home);
		
		final File homeConfigurationFile = new File(homeDirectory, home.getUniqueId().toString() + ".yml");
		final String homeConfigurationFilePath = homeConfigurationFile.getPath();
		
		try {
			if(!homeConfigurationFile.exists()) {
				try {
					homeConfigurationFile.createNewFile();
				}
				catch(IOException e) {
					
					logger.log(Level.WARNING, "Unable to create home configuration file at " + homeConfigurationFilePath);
					logger.log(Level.WARNING, "IOException thrown.", e);
					return false;
				}
				catch(SecurityException e) {
					
					logger.log(Level.WARNING, "Unable to create home configuration file at " + homeConfigurationFilePath);
					logger.log(Level.WARNING, "SecurityException thrown.", e);
					return false;
				}
			}
		}
		catch(SecurityException e) {
			
			logger.log(Level.WARNING, "Unable to verify if home configuration file exists at " + homeConfigurationFilePath);
			logger.log(Level.WARNING, "SecurityException thrown.", e);
			return false;
		}
		
		try {
			homeConfiguration.save(homeConfigurationFile);
		}
		catch(IOException e) {
			
			logger.log(Level.WARNING, "Unable to save home configuration file at " + homeConfigurationFilePath);
			logger.log(Level.WARNING, "IOException thrown.", e);
			return false;
		}
		
		return true;
	}
	
	private boolean save(final Spawn spawn) {
		
		final YamlConfiguration spawnConfiguration = new YamlConfiguration();
		
		spawnConfiguration.set("spawn", spawn);
		
		final File spawnConfigurationFile = new File(spawnDirectory, spawn.getLocation().getWorld().getName() + ".yml");
		final String spawnConfigurationFilePath = spawnConfigurationFile.getPath();
		
		try {
			if(!spawnConfigurationFile.exists()) {
				try {
					spawnConfigurationFile.createNewFile();
				}
				catch(IOException e) {
					
					logger.log(Level.WARNING, "Unable to create spawn configuration file at " + spawnConfigurationFilePath);
					logger.log(Level.WARNING, "IOException thrown.", e);
					return false;
				}
				catch(SecurityException e) {
					
					logger.log(Level.WARNING, "Unable to create spawn configuration file at " + spawnConfigurationFilePath);
					logger.log(Level.WARNING, "SecurityException thrown.", e);
					return false;
				}
			}
		}
		catch(SecurityException e) {
			
			logger.log(Level.WARNING, "Unable to verify if spawn configuration file exists at " + spawnConfigurationFilePath);
			logger.log(Level.WARNING, "SecurityException thrown.", e);
			return false;
		}
		
		try {
			spawnConfiguration.save(spawnConfigurationFile);
		}
		catch(IOException e) {
			
			logger.log(Level.WARNING, "Unable to save spawn configuration file at " + spawnConfigurationFilePath);
			logger.log(Level.WARNING, "IOException thrown.", e);
			return false;
		}
		
		return true;
	}
	
	private boolean save(final Warp warp) {
		
		final YamlConfiguration warpConfiguration = new YamlConfiguration();
		
		warpConfiguration.set("warp", warp);
		
		final File warpConfigurationFile = new File(warpDirectory, warp.getName().toLowerCase() + ".yml");
		final String warpConfigurationFilePath = warpConfigurationFile.getPath();
		
		try {
			if(!warpConfigurationFile.exists()) {
				try {
					warpConfigurationFile.createNewFile();
				}
				catch(IOException e) {
					
					logger.log(Level.WARNING, "Unable to create warp configuration file at " + warpConfigurationFilePath);
					logger.log(Level.WARNING, "IOException thrown.", e);
					return false;
				}
				catch(SecurityException e) {
					
					logger.log(Level.WARNING, "Unable to create warp configuration file at " + warpConfigurationFilePath);
					logger.log(Level.WARNING, "SecurityException thrown.", e);
					return false;
				}
			}
		}
		catch(SecurityException e) {
			
			logger.log(Level.WARNING, "Unable to verify if warp configuration file exists at " + warpConfigurationFilePath);
			logger.log(Level.WARNING, "SecurityException thrown.", e);
			return false;
		}
		
		try {
			warpConfiguration.save(warpConfigurationFile);
		}
		catch(IOException e) {
			
			logger.log(Level.WARNING, "Unable to save warp configuration file at " + warpConfigurationFilePath);
			logger.log(Level.WARNING, "IOException thrown.", e);
			return false;
		}
		
		return true;
	}
	
	private void displayLicenseInformation() {
		
		logger.log(Level.INFO, "//===================================================//");
		logger.log(Level.INFO, "// MCTeleports Copyright (C) 2019-2020 unixminecraft //");
		logger.log(Level.INFO, "//                                                   //");
		logger.log(Level.INFO, "// This program is free software: you can            //");
		logger.log(Level.INFO, "// redistribute it and/or modify it under the terms  //");
		logger.log(Level.INFO, "// of these GNU General Public License as published  //");
		logger.log(Level.INFO, "// by the Free Software Foundation, either version 3 //");
		logger.log(Level.INFO, "// of the License, or (at your opinion) any later    //");
		logger.log(Level.INFO, "// version.                                          //");
		logger.log(Level.INFO, "//                                                   //");
		logger.log(Level.INFO, "// This program is distributed in the hope that it   //");
		logger.log(Level.INFO, "// will be useful, but WITHOUT ANY WARRANTY; without //");
		logger.log(Level.INFO, "// even the implied warranty of MERCHANTABILITY or   //");
		logger.log(Level.INFO, "// FITNESS FOR A PARTICULAR PURPOSE. See GNU General //");
		logger.log(Level.INFO, "// Public License for more details.                  //");
		logger.log(Level.INFO, "//                                                   //");
		logger.log(Level.INFO, "// You should have received a copy of the GNU        //");
		logger.log(Level.INFO, "// General Public License along with this program.   //");
		logger.log(Level.INFO, "// If not, see <http://www.gnu.org/licenses/>        //");
		logger.log(Level.INFO, "//===================================================//");
	}
}
