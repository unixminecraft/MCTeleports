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

package org.unixminecraft.mcteleports.bukkit.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;

@SerializableAs("player_data")
public final class PlayerData implements ConfigurationSerializable {
	
	private static final String KEY_NAME = "name";
	private static final String KEY_UUID = "uuid";
	
	private static final String DEFAULT_NAME = "COMPLETELY_RANDOM_NAME_" + String.valueOf((new Random()).nextInt());
	private static final UUID DEFAULT_UUID = UUID.randomUUID();
	
	private String name;
	private final UUID uuid;
	
	public PlayerData(final Player player) {
		
		this(player.getName(), player.getUniqueId());
	}
	
	public PlayerData(final String name, final UUID uuid) {
		
		this.name = name;
		this.uuid = uuid;
	}
	
	public static PlayerData deserialize(final Map<String, Object> playerDataConfiguration) {
		
		final String name;
		final UUID uuid;
		
		if(!playerDataConfiguration.containsKey(KEY_NAME)) {
			name = DEFAULT_NAME;
		}
		else if(playerDataConfiguration.get(KEY_NAME) == null) {
			name = DEFAULT_NAME;
		}
		else if(!(playerDataConfiguration.get(KEY_NAME) instanceof String)) {
			name = DEFAULT_NAME;
		}
		else {
			name = (String) playerDataConfiguration.get(KEY_NAME);
		}
		
		if(!playerDataConfiguration.containsKey(KEY_UUID)) {
			uuid = DEFAULT_UUID;
		}
		else if(playerDataConfiguration.get(KEY_UUID) == null) {
			uuid = DEFAULT_UUID;
		}
		else if(!(playerDataConfiguration.get(KEY_UUID) instanceof UUID)) {
			uuid = DEFAULT_UUID;
		}
		else {
			
			final String uuidValue = (String) playerDataConfiguration.get(KEY_UUID);
			UUID attemptedUUID = null;
			try {
				attemptedUUID = UUID.fromString(uuidValue);
			}
			catch(IllegalArgumentException e) {
				attemptedUUID = DEFAULT_UUID;
			}
			
			if(attemptedUUID == null) {
				uuid = DEFAULT_UUID;
			}
			else {
				uuid = attemptedUUID;
			}
		}
		
		return new PlayerData(name, uuid);
	}
	
	@Override
	public Map<String, Object> serialize() {
		
		final HashMap<String, Object> playerDataConfiguration = new HashMap<String, Object>();
		
		playerDataConfiguration.put(KEY_NAME, name);
		playerDataConfiguration.put(KEY_UUID, uuid.toString());
		
		return playerDataConfiguration;
	}
	
	public String getName() {
		
		return name;
	}
	
	public UUID getUniqueId() {
		
		return uuid;
	}
	
	public void setName(final String name) {
		
		this.name = name;
	}
}
