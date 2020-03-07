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
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;

@SerializableAs("warp")
public final class Warp implements ConfigurationSerializable {
	
	private static final String PERMISSION_WARP_PREFIX = "mcteleports.warp.";
	
	private static final String KEY_NAME = "name";
	private static final String KEY_PERMISSION_ENABLED = "permission_enabled";
	private static final String KEY_LOCATION = "location";
	
	private static final String DEFAULT_NAME = "warp_" + String.valueOf((new Random()).nextInt());
	private static final boolean DEFAULT_PERMISSION_ENABLED = true;
	private static final Location DEFAULT_LOCATION = new Location(Bukkit.getWorld("world"), 63.0D, 85.0D, 0.0D, 0.0F, 0.0F);
	
	private final String name;
	private boolean permissionEnabled;
	private Location location;
	
	public Warp(final String name, final Player player) {
		
		this(name, DEFAULT_PERMISSION_ENABLED, player.getLocation());
	}
	
	private Warp(final String name, final boolean permissionEnabled, final Location location) {
		
		this.name = name;
		this.permissionEnabled = permissionEnabled;
		this.location = location;
	}
	
	public static Warp deserialize(final Map<String, Object> warpConfiguration) {
		
		if(warpConfiguration == null) {
			return new Warp(DEFAULT_NAME, DEFAULT_PERMISSION_ENABLED, DEFAULT_LOCATION);
		}
		
		final String name;
		final boolean permissionEnabled;
		final Location location;
		
		if(!warpConfiguration.containsKey(KEY_NAME)) {
			name = DEFAULT_NAME;
		}
		else if(warpConfiguration.get(KEY_NAME) == null) {
			name = DEFAULT_NAME;
		}
		else if(!(warpConfiguration.get(KEY_NAME) instanceof String)) {
			name = DEFAULT_NAME;
		}
		else {
			
			final HashSet<String> disallowedNames = new HashSet<String>();
			
			disallowedNames.add("set");
			disallowedNames.add("list");
			disallowedNames.add("permission");
			disallowedNames.add("remove");
			
			final String nameValue = ((String) warpConfiguration.get(KEY_NAME)).toLowerCase();
			
			if(disallowedNames.contains(nameValue.toLowerCase())) {
				name = DEFAULT_NAME;
			}
			else {
				name = nameValue;
			}
		}
		
		if(!warpConfiguration.containsKey(KEY_PERMISSION_ENABLED)) {
			permissionEnabled = DEFAULT_PERMISSION_ENABLED;
		}
		else if(warpConfiguration.get(KEY_PERMISSION_ENABLED) == null) {
			permissionEnabled = DEFAULT_PERMISSION_ENABLED;
		}
		else if(!(warpConfiguration.get(KEY_PERMISSION_ENABLED) instanceof Boolean)) {
			permissionEnabled = DEFAULT_PERMISSION_ENABLED;
		}
		else {
			permissionEnabled = ((Boolean) warpConfiguration.get(KEY_PERMISSION_ENABLED)).booleanValue();
		}
		
		if(!warpConfiguration.containsKey(KEY_LOCATION)) {
			location = DEFAULT_LOCATION;
		}
		else if(warpConfiguration.get(KEY_LOCATION) == null) {
			location = DEFAULT_LOCATION;
		}
		else if(!(warpConfiguration.get(KEY_LOCATION) instanceof Location)) {
			location = DEFAULT_LOCATION;
		}
		else {
			location = (Location) warpConfiguration.get(KEY_LOCATION);
		}
		
		return new Warp(name, permissionEnabled, location);
	}
	
	@Override
	public Map<String, Object> serialize() {
		
		final HashMap<String, Object> warpConfiguration = new HashMap<String, Object>();
		
		warpConfiguration.put(KEY_NAME, name);
		warpConfiguration.put(KEY_PERMISSION_ENABLED, permissionEnabled);
		warpConfiguration.put(KEY_LOCATION, location);
		
		return warpConfiguration;
	}
	
	public String getName() {
		
		return name;
	}
	
	public boolean getPermissionEnabled() {
		
		return permissionEnabled;
	}
	
	public String getPermission() {
		
		return PERMISSION_WARP_PREFIX + name;
	}
	
	public Location getLocation() {
		
		return location;
	}
	
	public void setPermissionEnabled(final boolean permissionEnabled) {
		
		this.permissionEnabled = permissionEnabled;
	}
	
	public void setLocation(final Location location) {
		
		this.location = location;
	}
}
