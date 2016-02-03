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
package info.bioinfweb.jphyloio.dataadapters;


import java.util.Iterator;



/**
 * Allows to provide data for a list of phylogenetic objects. All objects need to be identified by
 * unique IDs, provided by {@link #getIDs()} and the event sequence for each object will be requested
 * be separate calls of {@link #writeData(JPhyloIOEventReceiver, String)}.
 * <p>
 * Such objects may e.g. be OTUs, token setsor character sets, depending on where instances of this 
 * interface are used.
 * 
 * @author Ben St&ouml;ver
 */
public interface ObjectListDataAdapter {
	/**
	 * Returns an iterator returning the IDs of all objects contained in the list modeled by this instance.
	 * 
	 * @return an iterator returning the IDs (Must return at least one element.)
	 */
	public Iterator<String> getIDs();
	
	/**
	 * Writes the events describing the specified object, including possible nested objects.
	 * 
	 * @param writer the writer accepting the events
	 * @param nodeID the ID of the requested node
	 */
	public void writeData(JPhyloIOEventReceiver writer, String id);
}
