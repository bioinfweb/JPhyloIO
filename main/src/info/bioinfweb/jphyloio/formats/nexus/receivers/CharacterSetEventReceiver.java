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
import java.io.Writer;

import javax.xml.stream.XMLStreamException;

import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.CharacterSetIntervalEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.exception.IllegalEventException;
import info.bioinfweb.jphyloio.formats.nexus.NexusConstants;
import info.bioinfweb.jphyloio.formats.text.BasicTextCommentEventReceiver;



public class CharacterSetEventReceiver extends BasicTextCommentEventReceiver implements NexusConstants {
	public CharacterSetEventReceiver(Writer writer,	ReadWriteParameterMap parameterMap) {
		super(writer, parameterMap, Character.toString(COMMENT_START), Character.toString(COMMENT_END));
	}

	
	@Override
	protected boolean doAdd(JPhyloIOEvent event) throws IOException, XMLStreamException {
		if (event.getType().getContentType().equals(EventContentType.CHARACTER_SET_INTERVAL) && (getParentEvent() == null)) {  // Such events are only allowed on the top level.
			// No check for topology type, since only SOLE is possible.
			CharacterSetIntervalEvent intervalEvent = event.asCharacterSetIntervalEvent();
			getWriter().write(' ');
			getWriter().write(Long.toString(intervalEvent.getStart()));
			if (intervalEvent.getEnd() - intervalEvent.getStart() > 1) {
				getWriter().write(SET_TO_SYMBOL);
				getWriter().write(Long.toString(intervalEvent.getEnd() - 1));
			}
			return true;			
		}
		else {  // No other events would be valid here.
			throw IllegalEventException.newInstance(this, getParentEvent(), event);
		}
	}
}
