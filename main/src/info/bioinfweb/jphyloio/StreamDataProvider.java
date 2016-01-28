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
package info.bioinfweb.jphyloio;


import info.bioinfweb.commons.LongIDManager;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.formats.newick.NewickStringReader;
import info.bioinfweb.jphyloio.tools.SequenceTokensEventManager;

import java.util.Collection;
import java.util.Queue;



/**
 * Stream data providers are objects used by helper classes of JPhyloIO event readers (e.g. command readers for Nexus,
 * {@link NewickStringReader} or element readers for XML formats). They have two major functions:
 * <ol>
 *   <li>Delegate protected properties of their associated event reader to allow accessing to them by helper classes
 *       in other packages.</li>
 *   <li>Act as a repository for data that shall be shared among different helper classes of a reader.</li>
 * </ol>
 * 
 * @author Ben St&ouml;ver
 * @see AbstractEventReader#getStreamDataProvider()
 * @see AbstractEventReader#createStreamDataProvider()
 */
public class StreamDataProvider {
	private AbstractEventReader eventReader;
	
	
	public StreamDataProvider(AbstractEventReader eventReader) {
		super();
		this.eventReader = eventReader;
	}


	public AbstractEventReader getEventReader() {
		return eventReader;
	}
	
	
	/**
	 * Sets the current event collection to {@link #getUpcomingEvents()}.
	 * 
	 * @return the replaced event collection
	 */
	protected Collection<JPhyloIOEvent> resetCurrentEventCollection() {
		return getEventReader().resetCurrentEventCollection();
	}
	
	
	/**
	 * Sets a new current event collection.
	 * 
	 * @param newCollection the new collection to take up new events from now on 
	 * @return the replaced event collection
	 * @throws NullPointerException if {@code newCollection} is {@code null}
	 */
	protected Collection<JPhyloIOEvent> setCurrentEventCollection(Collection<JPhyloIOEvent> newCollection) {
		return getEventReader().setCurrentEventCollection(newCollection);
	}
	
	
	/**
	 * Returns the event collection that is currently used to take up new events.
	 * 
	 * @return the current event collection
	 */
	protected Collection<JPhyloIOEvent> getCurrentEventCollection() {
		return getEventReader().getCurrentEventCollection();
	}
	
	
	/**
	 * Determines whether the current event collection is different from the queue of upcoming events.
	 * 
	 * @return {@code false} if {@link #getCurrentEventCollection()} returns the same instance as {@link #getUpcomingEvents()}
	 *         or {@code true} otherwise
	 */
	protected boolean hasSpecialEventCollection() {
		return getEventReader().hasSpecialEventCollection(); 
	}
	
	
	public Queue<JPhyloIOEvent> getUpcomingEvents() {
		return getEventReader().getUpcomingEvents();
	}
	
	
	public LongIDManager getIdManager() {
		return getEventReader().getIDManager();
	}
	
	
	public SequenceTokensEventManager getSequenceTokensEventManager() {
		return getEventReader().getSequenceTokensEventManager();
	}


	public boolean isTranslateMatchToken() {
		return getEventReader().isTranslateMatchToken();
	}


	public String getMatchToken() {
		return getEventReader().getMatchToken();
	}
	
	
	public LongIDManager getIDManager() {
		return getEventReader().getIDManager();
	}
}
