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
import info.bioinfweb.jphyloio.dataadapters.implementations.receivers.BasicEventReceiver;
import info.bioinfweb.jphyloio.events.CommentEvent;
import info.bioinfweb.jphyloio.formats.nexus.NexusConstants;



public class BasicNexusEventReceiver extends BasicEventReceiver<Writer> implements NexusConstants {
	public BasicNexusEventReceiver(Writer writer,	ReadWriteParameterMap parameterMap) {
		super(writer, parameterMap);
	}
	
	
	@Override
	protected void handleComment(CommentEvent event) throws IOException, XMLStreamException {
		getWriter().write(COMMENT_START);
		getWriter().write(event.getContent());
		getWriter().write(COMMENT_END);
		//TODO Handle continued comments
	}	
}
