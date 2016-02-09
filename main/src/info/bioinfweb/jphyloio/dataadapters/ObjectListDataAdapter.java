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


import java.io.IOException;
import java.util.Iterator;



/**
 * Allows to provide data for a list of phylogenetic objects. All objects need to be identified by
 * unique IDs, provided by {@link #getIDIterator()} and the event sequence for each object will be requested
 * be separate calls of {@link #writeData(JPhyloIOEventReceiver, String)}.
 * <p>
 * Such objects may e.g. be OTUs, token setsor character sets, depending on where instances of this 
 * interface are used.
 * 
 * @author Ben St&ouml;ver
 */
public interface ObjectListDataAdapter {
	/**
	 * Returns the number of objects to be returned by {@link #getIDIterator()}.
	 * 
	 * @return the number of objects in the list modeled by this instance
	 */
	public long getCount();
	
	/**
	 * Returns an iterator returning the IDs of all objects contained in the list modeled by this instance.
	 * 
	 * @return an iterator returning the IDs (Depending in the usage of this object, the returned iterator 
	 *         must return at least one element or can be empty, but it may never be {@code null}.)
	 */
	public Iterator<String> getIDIterator();
	
	/**
	 * Writes the events describing the specified object, including possible nested objects.
	 * 
	 * @param receiver the receiver for the events
	 * @param nodeID the ID of the requested node
	 * @throws IllegalArgumentException if an unknown ID was specified
	 */
	public void writeData(JPhyloIOEventReceiver receiver, String id) throws IllegalArgumentException, IOException;
}