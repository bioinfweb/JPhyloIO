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
package info.bioinfweb.jphyloio.dataadapters.implementations.receivers;


import java.io.IOException;

import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.dataadapters.JPhyloIOEventReceiver;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.events.type.EventType;



/**
 * Abstract event receiver implementation that expects a certain event as the start of the sequence.
 * 
 * @author Ben St&ouml;ver
 *
 * @param <E> the class of the expected start event
 */
public abstract class CertainStartEventReceiver<W extends Object, E extends JPhyloIOEvent> extends AbstractEventReceiver<W> 
		implements JPhyloIOEventReceiver {
	
	private EventContentType startEventType;
	private E startEvent = null;

	
	public CertainStartEventReceiver(W writer,	ReadWriteParameterMap parameterMap, EventContentType startEventType) {
		super(writer, parameterMap);
		this.startEventType = startEventType;
	}


	public EventContentType getStartEventType() {
		return startEventType;
	}


	public E getStartEvent() {
		return startEvent;
	}


	public void clear() {
		startEvent = null;
	}
	
	
	protected abstract boolean doAdd(JPhyloIOEvent event) throws IllegalArgumentException, IOException;
	
	
	protected abstract void processStartEvent(E startEvent) throws IOException;
	
	
	@Override
	public boolean add(JPhyloIOEvent event) throws IllegalArgumentException, IOException {
		if (startEvent == null) {
			if (event.getType().getContentType().equals(startEventType) && 
					event.getType().getTopologyType().equals(EventTopologyType.START)) {
				
				startEvent = (E)event;  // May throw a class cast exception later, if an invalid object with this type was specified.
				processStartEvent(startEvent);
				return true;
			}
			else {
				throw new IllegalArgumentException("The first event in this subsequence is expected to have the type " + 
						new EventType(startEventType, EventTopologyType.START) + " but was of the type of type " + event.getType());
			}
		}
		else if (startEventType.equals(event.getType().getContentType())) {  // Needs to be handled here, because switch only allows constants
			if (event.getType().getTopologyType().equals(EventTopologyType.END)) {
				return false;  // No more events to come.
			}
			else {
				throw new IllegalArgumentException("Multiple start events of the type " + startEventType + 
						" are not allowed in this sequence.");
			}
		}
		else {
			return doAdd(event);
		}
	}
}
