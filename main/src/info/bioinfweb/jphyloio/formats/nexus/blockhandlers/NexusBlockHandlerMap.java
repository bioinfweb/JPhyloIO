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
package info.bioinfweb.jphyloio.formats.nexus.blockhandlers;


import info.bioinfweb.jphyloio.formats.nexus.NexusEventReader;

import java.util.HashMap;
import java.util.Map;



/**
 * Manages Nexus block handlers for an instance of {@link NexusEventReader}.
 * 
 * @author Ben St&ouml;ver
 */
public class NexusBlockHandlerMap {
	private Map<String, NexusBlockHandler> handlers = new HashMap<String, NexusBlockHandler>();
	
	
	/**
	 * Adds a new handler to this map.
	 * <p>
	 * Note that this method can overwrite previous entries, if a previously added handler had a block name
	 * in common with the handler added here. 
	 * 
	 * @param handler the new handler to added
	 */
	public void addHandler(NexusBlockHandler handler) {
		for (String blockName : handler.getBlockNames()) {
			handlers.put(blockName.toUpperCase(), handler);
		}
	}
	
	
	/**
	 * Adds all handlers that are available in <i>JPhyloIO</i> to this instance.
	 */
	public void addJPhyloIOHandlers() {
		addHandler(new CharactersDataUnalignedBlockHandler());
		addHandler(new TreesBlockHandler());
		// add new classes here
	}
	
	
	/**
	 * Returns the handler that is registered for the specified block name.
	 * 
	 * @param blockName the name of the block (this method is not case sensitive)
	 * @return the according handler or {@code null} if no handler could be found
	 */
	public NexusBlockHandler getHandler(String blockName) {
		return handlers.get(blockName.toUpperCase());
	}
	
	
	/**
	 * Checks whether a handler for the specified block name is contained in this map.
	 * 
	 * @param blockName the name of the Nexus block
	 * @return {@code true} if a handler is available or {@code false} otherwise
	 */
	public boolean hasHandler(String blockName) {
		return handlers.containsKey(blockName.toUpperCase());
	}
}
