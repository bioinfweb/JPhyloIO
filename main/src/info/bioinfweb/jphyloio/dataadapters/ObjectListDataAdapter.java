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


import info.bioinfweb.jphyloio.events.JPhyloIOEvent;

import java.io.IOException;
import java.util.Iterator;



/**
 * Allows to provide data for a list of phylogenetic objects. All objects need to be identified by
 * unique IDs, provided by {@link #getIDIterator()}. The start event of each object will be requested by 
 * separate calls of {@link #getObjectStartEvent(String)} and the event sequence by
 * separate calls of {@link #writeContentData(JPhyloIOEventReceiver, String)}.
 * <p>
 * Such objects may e.g. be OTUs, token sets or character sets, depending on where instances of this 
 * interface are used.
 * 
 * @author Ben St&ouml;ver
 */
public interface ObjectListDataAdapter<E extends JPhyloIOEvent> {
	/**
	 * Returns the start event of an object determined by the specified object ID.
	 * <p>
	 * 
	 * @param id the ID of the requested object
	 * @return an instance of a labeled ID event that describes the specified object
	 * @throws IllegalArgumentException if no to object for the specified ID is present 
	 */
	public E getObjectStartEvent(String id) throws IllegalArgumentException;
	
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
	 * Writes the nested events in the specified object describing its contents.
	 * 
	 * @param receiver the receiver for the events
	 * @param nodeID the ID of the requested node
	 * @throws IOException if a I/O error occurs while writing the data
	 * @throws IllegalArgumentException if an unknown ID was specified
	 */
	public void writeContentData(JPhyloIOEventReceiver receiver, String id) throws IOException, IllegalArgumentException;
}
