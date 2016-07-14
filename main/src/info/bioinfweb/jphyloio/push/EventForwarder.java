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
package info.bioinfweb.jphyloio.push;


import info.bioinfweb.jphyloio.JPhyloIOEventReader;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;



/**
 * Tool class that consumes all events provided by an implementation of {@link JPhyloIOEventReader} and forwards them
 * to one or more {@link JPhyloIOEventListener}s.
 * 
 * @author Ben St&ouml;ver
 */
public class EventForwarder {
	private JPhyloIOEventReader reader;
	private ParentEventInformation parentEventInformation;
	private List<JPhyloIOEventListener> listeners;
	
	
	public EventForwarder(JPhyloIOEventReader reader) {
		super();
		this.reader = reader;
		parentEventInformation = new ParentEventInformation();
		listeners = new ArrayList<JPhyloIOEventListener>();
	}


	/**
	 * Returns the list of listeners to which this instance forwards its consumed events.
	 * 
	 * @return the modifiable listener list
	 */
	public List<JPhyloIOEventListener> getListeners() {
		return listeners;
	}


	/**
	 * Consumes all available events from the specified listener and forwards them to the registered listeners.
	 * 
	 * @param reader the reader to read the events from
	 * @throws Exception if {@code reader} throws an exception while parsing
	 */
	public void readAll() throws Exception {
		doReadUntil(null);
	}
	
	
	/**
	 * Consumes all available events from the specified listener and forwards them to the registered listeners
	 * until an event of the specified type is reached. (The event of the specified type is not consumed.)
	 * 
	 * @param reader the reader to read the events from
	 * @param type the type of the event that shall trigger the end of reading
	 * @throws Exception if {@code reader} throws an exception while parsing
	 */
	public void readUntil(EventContentType type) throws Exception {
		doReadUntil(EnumSet.of(type));
	}
	
	
	/**
	 * Consumes all available events from the specified listener and forwards them to the registered listeners
	 * until any event of one of the specified types is reached. (The event of the specified type is not consumed.)
	 * 
	 * @param reader the reader to read the events from
	 * @param types a set of types of the events that shall trigger the end of reading
	 * @throws Exception if {@code reader} throws an exception while parsing
	 */
	public void readUntil(Set<EventContentType> types) throws Exception {
		doReadUntil(types);
	}
	
	
	private void doReadUntil(Set<EventContentType> types) throws Exception {
		while (reader.hasNextEvent() && ((types == null) || !types.contains(reader.peek().getType()))) {
			JPhyloIOEvent event = reader.next();
			if (event.getType().getTopologyType().equals(EventTopologyType.END)) {
				parentEventInformation.pop();  // Throws an exception, if more end than start events are encountered.
			}
			
			for (JPhyloIOEventListener listener : listeners) {
				listener.processEvent(reader, parentEventInformation, event);
			}
			
			if (event.getType().getTopologyType().equals(EventTopologyType.START)) {
				parentEventInformation.add(event);
			}
		}
	}
}
