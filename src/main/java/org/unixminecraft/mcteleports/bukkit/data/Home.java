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
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;

@SerializableAs("home")
public final class Home implements ConfigurationSerializable {
	
	private static final String KEY_UUID = "uuid";
	private static final String KEY_LOCATION = "location";
	
	private static final UUID DEFAULT_UUID = UUID.randomUUID();
	private static final Location DEFAULT_LOCATION = null;
	
	private final UUID uuid;
	private Location location;
	
	public Home(final Player player) {
		
		this(player.getUniqueId(), player.getLocation());
	}
	
	public Home(final UUID uuid, final Location location) {
		
		this.uuid = uuid;
		this.location = location;
	}
	
	public static Home deserialize(final Map<String, Object> homeConfiguration) {
		
		if(homeConfiguration == null) {
			return new Home(DEFAULT_UUID, DEFAULT_LOCATION);
		}
		
		final UUID uuid;
		final Location location;
		
		if(!homeConfiguration.containsKey(KEY_UUID)) {
			uuid = DEFAULT_UUID;
		}
		else if(homeConfiguration.get(KEY_UUID) == null) {
			uuid = DEFAULT_UUID;
		}
		else if(!(homeConfiguration.get(KEY_UUID) instanceof String)) {
			uuid = DEFAULT_UUID;
		}
		else {
			
			final String uuidValue = (String) homeConfiguration.get(KEY_UUID);
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
		
		if(!homeConfiguration.containsKey(KEY_LOCATION)) {
			location = DEFAULT_LOCATION;
		}
		else if(homeConfiguration.get(KEY_LOCATION) == null) {
			location = DEFAULT_LOCATION;
		}
		else if(!(homeConfiguration.get(KEY_LOCATION) instanceof Location)) {
			location = DEFAULT_LOCATION;
		}
		else {
			location = (Location) homeConfiguration.get(KEY_LOCATION);
		}
		
		return new Home(uuid, location);
	}
	
	@Override
	public Map<String, Object> serialize() {
		
		final HashMap<String, Object> homeConfiguration = new HashMap<String, Object>();
		
		homeConfiguration.put(KEY_UUID, uuid.toString());
		homeConfiguration.put(KEY_LOCATION, location);
		
		return homeConfiguration;
	}
	
	public UUID getUniqueId() {
		
		return uuid;
	}
	
	public Location getLocation() {
		
		return location;
	}
	
	public void setLocation(final Location location) {
		
		this.location = location;
	}
}
