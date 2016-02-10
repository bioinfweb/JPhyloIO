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
package info.bioinfweb.jphyloio.formats.newick;


import java.io.IOException;
import java.io.Writer;

import info.bioinfweb.jphyloio.AbstractEventReceiver;
import info.bioinfweb.jphyloio.AbstractEventWriter;
import info.bioinfweb.jphyloio.EventWriterParameterMap;
import info.bioinfweb.jphyloio.dataadapters.JPhyloIOEventReceiver;
import info.bioinfweb.jphyloio.dataadapters.OTUListDataAdapter;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.events.type.EventType;



public class NodeDataEventReceiver extends AbstractEventReceiver implements JPhyloIOEventReceiver {
	private static enum Status {
		BEFORE_START,
		IN_NODE,
		END;
	}
	
	
	private Status status = Status.BEFORE_START;
	//private long metaLevel
	private OTUListDataAdapter firstOTUList;
	
	
	public NodeDataEventReceiver(Writer writer,	EventWriterParameterMap parameterMap, OTUListDataAdapter firstOTUList) {
		super(writer, parameterMap);
		this.firstOTUList = firstOTUList;
	}
	
	
	@Override
	public boolean add(JPhyloIOEvent event) throws IllegalArgumentException, ClassCastException, IOException {
		switch (status) {
			case BEFORE_START:
				EventType nodeStart = new EventType(EventContentType.NODE, EventTopologyType.START);
				if (event.getType().equals(nodeStart)) {
					getWriter().write(AbstractEventWriter.getLinkedOTUName(event.asLinkedOTUEvent(), firstOTUList));
				}
				else {
					throw new IllegalArgumentException(nodeStart + 
							" expected as the first event in the sequence in writeNodeData(), but was "	+ event.getType());
				}
				break;
			case IN_NODE:
				switch (event.getType().getContentType()) {
					case META_INFORMATION:
						break;
					case META_XML_CONTENT:
						break;
					case COMMENT:
						break;
					case NODE:
						break;
					default:
						
				}
				//TODO Write metadata
				break;
			case END:
				throw new IllegalArgumentException("No more events expected after ");
		}
		return true;
	}
}
