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
package info.bioinfweb.jphyloio.formats.nexus.receivers;


import info.bioinfweb.jphyloio.events.CharacterSetIntervalEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.SetElementEvent;
import info.bioinfweb.jphyloio.exception.IllegalEventException;
import info.bioinfweb.jphyloio.formats.nexus.NexusWriterStreamDataProvider;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;



/**
 * General implementation for event receivers writing Nexus set commands.
 * 
 * @author Ben St&ouml;ver
 * @since 0.0.0
 */
public abstract class AbstractNexusSetsEventReceiver extends AbstractNexusEventReceiver {
	public AbstractNexusSetsEventReceiver(NexusWriterStreamDataProvider streamDataProvider) {
		super(streamDataProvider);
	}

	
	protected abstract boolean handleCharacterSetInterval(CharacterSetIntervalEvent event) throws IOException;
	
	protected abstract boolean handleSetElement(SetElementEvent event) throws IOException;
	
	
	@Override
	protected boolean doAdd(JPhyloIOEvent event) throws IOException, XMLStreamException {
		boolean result = false;
		if (getParentEvent() == null) {  // Such events are only allowed on the top level.
			switch (event.getType().getContentType()) {  // No check for topology type, since only SOLE is possible.
				case CHARACTER_SET_INTERVAL:
					result = handleCharacterSetInterval(event.asCharacterSetIntervalEvent());
					break;
				case SET_ELEMENT:
					result = handleSetElement(event.asSetElementEvent());
					break;
				default:  // Throw exception below.
					break;
			}
		}
		
		if (result) {
			return true;
		}
		else {
			throw IllegalEventException.newInstance(this, getParentEvent(), event);
		}
	}
}
