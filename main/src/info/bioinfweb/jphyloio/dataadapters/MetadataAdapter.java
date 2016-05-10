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


import info.bioinfweb.jphyloio.JPhyloIOEventReader;
import info.bioinfweb.jphyloio.events.CommentEvent;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataContentEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.ResourceMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.UriOrStringIdentifier;

import java.io.IOException;
import java.util.Iterator;

import javax.xml.namespace.QName;



/**
 * Models metadata associated with a data object (which is provided by the same data adapter as the instance implementing this 
 * interface).
 * <p>
 * Note that this adapter contains all methods necessary to access metadata associated with the parent data object (characterized by the parent
 * adapter). In contrast to many other adapters, therefore this interface does not correspond to a grammar node (of the grammar defined in the 
 * documentation of {@link JPhyloIOEventReader}).
 * 
 * @author Ben St&ouml;ver
 * @since 0.0.0
 */
public interface MetadataAdapter {
	/**
	 * Iterates over the IDs of all metadata events directly nested under the associated data object.
	 * 
	 * @return an iterator of all according metaevent IDs
	 */
	public Iterator<String> getRootElementIDs();
	
	/**
	 * Iterates over the IDs of all metadata events directly nested under another metaevent.
	 * 
	 * @param parentID the ID of the parent metaevent
	 * @return an iterator of all according metaevent IDs (maybe empty if the specified element does not have any child elements)
	 * @throws IllegalArgumentException if no event with the specified ID is present in this adapter
	 */
	public Iterator<String> getChildElementIDs(String parentID);
	
	//TODO Adjust comment main text
	/**
	 * Returns the ID of a metaevent in this adapter that is associated with the specified predicate (as the default or any 
	 * alternative predicate). 
	 * 
	 * @param predicate the predicates linking the returned metadata item to its parent data object (RDF subject).
	 * @return an iterator over all IDs of the metaevents using the specified predicate or an empty iterator
	 */
	public Iterator<String> getIDsByPredicate(QName predicate);  //TODO Would that also be needed for string keys?
	
	/**
	 * Iterates over all alternative predicates or string identifier that would be valid for the specified metadata item.
	 * <p>
	 * The identifier that is returned first, is the preferred one. Writers may choose one of the alternatives, if they fit better
	 * to the target format.
	 * 
	 * @param id the ID of the associated metaevent
	 * @return an iterator returning at least one element
	 * @throws IllegalArgumentException if no event with the specified ID is present in this adapter
	 */
	public Iterator<UriOrStringIdentifier> getAlternativeIdentifiers(String id);
	
	/**
	 * Returns the resource or literal meta start event identified by the specified ID.  
	 * 
	 * @param id the ID of the requested start event
	 * @return an instance of {@link ResourceMetadataEvent} or {@link LiteralMetadataEvent}
	 * @throws IllegalArgumentException if no event with the specified ID is present in this adapter
	 */
	public LabeledIDEvent getMetadataStartEvent(String id);
	//TODO This and the next method could also be inherited from ObjectListDataAdapter. Should getCount() and getIDIterator() then refer to the root level?
	
	/**
	 * Writes a sequence of {@link LiteralMetadataContentEvent}s and {@link CommentEvent}s that are the contents of the specified
	 * literal metadata item.
	 * 
	 * @param receiver the receiver for the events
	 * @param id the ID of the parent literal metadata event
	 * @throws IOException if a I/O error occurs while writing the data
	 * @throws IllegalArgumentException if an unknown ID was specified
	 */
	public void writeLiteralContent(JPhyloIOEventReceiver receiver, String id) throws IOException;  // May also write comment events
}
