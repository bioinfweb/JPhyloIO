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
package info.bioinfweb.jphyloio.exception;


import info.bioinfweb.jphyloio.JPhyloIOEventReader;
import info.bioinfweb.jphyloio.dataadapters.JPhyloIOEventReceiver;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;



/**
 * This exception in thrown by implementations of {@link JPhyloIOEventReceiver} and indicates that a data adapter passed
 * an event that is illegal at the current position according to the grammar defined in the documentation of 
 * {@link JPhyloIOEventReader}.
 * <p>
 * The cause for this exception would usually be an error in the implementation of a data adapter.
 * 
 * @author Ben St&ouml;ver
 * @since 0.0.0
 */
public class IllegalEventException extends InconsistentAdapterDataException {
	private static final long serialVersionUID = 1L;
	
	
	private JPhyloIOEventReceiver receiver;
	private EventContentType parentType;
	private JPhyloIOEvent invalidEvent;
	
	
	public IllegalEventException(JPhyloIOEventReceiver receiver, EventContentType parentType,	JPhyloIOEvent invalidEvent) {
		super("An event of the type " + invalidEvent.getType().getContentType() + " was encountered under an event of the type " + 
				parentType + " which is invalid in this receiver.");
		
		if (receiver == null) {
			throw new NullPointerException("receiver must not be null");
		}
		else if (parentType == null) {
			throw new NullPointerException("parentEvent must not be null");
		}  // invalidEvent would have cause an NPE in the generation of the message.
		else {
			this.receiver = receiver;
			this.parentType = parentType;
			this.invalidEvent = invalidEvent;
		}
	}


	public JPhyloIOEventReceiver getReceiver() {
		return receiver;
	}


	public EventContentType getParentType() {
		return parentType;
	}


	public JPhyloIOEvent getInvalidEvent() {
		return invalidEvent;
	}
}
