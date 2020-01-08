/*
 * MCTeleports Copyright (C) 2019 unixminecraft
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

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

@SerializableAs("spawn")
public final class Spawn implements ConfigurationSerializable {
	
	private static final String KEY_LOCATION = "location";
	
	private static final Location DEFAULT_LOCATION = new Location(Bukkit.getWorld("world"), 63.0D, 85.0D, 0.0D, 0.0F, 0.0F);
	
	private Location location;
	
	public Spawn(final Location location) {
		
		this.location = location;
	}
	
	public static Spawn deserialize(final Map<String, Object> spawnConfiguration) {
		
		if(spawnConfiguration == null) {
			return new Spawn(DEFAULT_LOCATION);
		}
		
		final Location location;
		
		if(!spawnConfiguration.containsKey(KEY_LOCATION)) {
			location = DEFAULT_LOCATION;
		}
		else if(spawnConfiguration.get(KEY_LOCATION) == null) {
			location = DEFAULT_LOCATION;
		}
		else if(!(spawnConfiguration.get(KEY_LOCATION) instanceof Location)) {
			location = DEFAULT_LOCATION;
		}
		else {
			location = (Location) spawnConfiguration.get(KEY_LOCATION);
		}
		
		return new Spawn(location);
	}
	
	@Override
	public Map<String, Object> serialize() {
		
		final HashMap<String, Object> spawnConfiguration = new HashMap<String, Object>();
		
		spawnConfiguration.put(KEY_LOCATION, location);
		
		return spawnConfiguration;
	}
	
	public Location getLocation() {
		
		return location;
	}
	
	public void setLocation(final Location location) {
		
		this.location = location;
	}
}
