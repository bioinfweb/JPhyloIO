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


import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import info.bioinfweb.jphyloio.events.CharacterSetIntervalEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.SetElementEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.exception.IllegalEventException;
import info.bioinfweb.jphyloio.exception.InconsistentAdapterDataException;
import info.bioinfweb.jphyloio.formats.nexus.NexusEventWriter;
import info.bioinfweb.jphyloio.formats.nexus.NexusWriterStreamDataProvider;



public class CharacterSetEventReceiver extends AbstractNexusEventReceiver {
	public CharacterSetEventReceiver(NexusWriterStreamDataProvider streamDataProvider) {
		super(streamDataProvider);
	}

	
	@Override
	protected boolean doAdd(JPhyloIOEvent event) throws IOException, XMLStreamException {
		if (getParentEvent() == null) {  // Such events are only allowed on the top level.
			switch (event.getType().getContentType()) {  // No check for topology type, since only SOLE is possible.
				case CHARACTER_SET_INTERVAL:
					CharacterSetIntervalEvent intervalEvent = event.asCharacterSetIntervalEvent();
					getWriter().write(' ');
					getWriter().write(Long.toString(intervalEvent.getStart() + 1));
					if (intervalEvent.getEnd() - intervalEvent.getStart() > 1) {
						getWriter().write(SET_TO_SYMBOL);
						getWriter().write(Long.toString(intervalEvent.getEnd()));
					}
					return true;
					
				case SET_ELEMENT:
					SetElementEvent setEvent = event.asSetElementEvent();
					if (setEvent.getLinkedObjectType().equals(EventContentType.CHARACTER_SET)) {
						getWriter().write(' ');
						String referencedSet = getParameterMap().getLabelEditingReporter().getEditedLabel(EventContentType.CHARACTER_SET, setEvent.getLinkedID());
						if (referencedSet == null) {
							throw new InconsistentAdapterDataException("A character set references the other character set with the ID " + setEvent.getLinkedID() 
									+ "\" that was not previously (or not at all) declared.");  //TODO If a subsequent set is referenced here, it could alternatively be fetched from the provider and be written directly here instead of referencing it.
						}
						else {
							getWriter().write(NexusEventWriter.formatToken(referencedSet));
						}
						return true;
					}  // Otherwise throw exception at the end of the method.
					
				default:
			}
		}
		throw IllegalEventException.newInstance(this, getParentEvent(), event);
	}
}
