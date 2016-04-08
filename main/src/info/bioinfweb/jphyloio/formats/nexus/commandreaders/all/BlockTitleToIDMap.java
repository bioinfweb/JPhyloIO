/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015-2016  Ben Stöver, Sarah Wiechers
 * <http://bioinfweb.info/JPhyloIO>
 * 
 * This file is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This file is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package info.bioinfweb.jphyloio.formats.nexus.commandreaders.all;


import info.bioinfweb.jphyloio.formats.nexus.NexusEventReader;

import java.util.Map;
import java.util.TreeMap;



/**
 * Used internally be {@link NexusEventReader} and related classes to map Nexus block titles to <i>JPhyloIO</i> IDs.
 * <p>
 * Application code will usually not need to use this class directly.
 * 
 * @author Ben St&ouml;ver
 * @since 0.0.0
 */
public class BlockTitleToIDMap {
	private static final String KEY_SEPARATOR = "|";
	
	
	private Map<String, String> map = new TreeMap<String, String>();
	
	
	public String get(String blockTypeName, String blockTitle) {
		return map.get(blockTypeName + KEY_SEPARATOR + blockTitle);
	}
	
	
	public void put(String blockTypeName, String blockTitle, String id) {
		map.put(blockTypeName + KEY_SEPARATOR + blockTitle, id);
	}
}
