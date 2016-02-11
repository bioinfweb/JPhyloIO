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
import java.util.ArrayList;
import java.util.List;

import info.bioinfweb.jphyloio.AbstractEventReceiver;
import info.bioinfweb.jphyloio.EventWriterParameterMap;
import info.bioinfweb.jphyloio.dataadapters.JPhyloIOEventReceiver;
import info.bioinfweb.jphyloio.dataadapters.TreeNetworkDataAdapter;
import info.bioinfweb.jphyloio.events.EdgeEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.MetaInformationEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;



/**
 * Event receiver that processes an event sequence generated by 
 * {@link TreeNetworkDataAdapter#writeEdgeData(JPhyloIOEventReceiver, String)}. It stores the edge start event
 * and nested comment events in its properties for later use. Additionally nested metadata up to the second level
 * are stored, not including XML metadata contents. 
 * <p>
 * Whether any metadata from the sequence have been ignored (as described above) can be obtained using the 
 * properties {@link #isIgnoredNestedMetadata()} and {@link #isIgnoredXMLMetadata()}.
 * <p>
 * If an instance of this class shall be reused, {@link #clear()} needs to be called before processing the next
 * event sequence.
 * 
 * @author Ben St&ouml;ver
 */
public class EdgeDataEventReceiver extends AbstractEventReceiver implements JPhyloIOEventReceiver {
	private static class Metadata {
		public String key;
		public List<String> values;
		
		public Metadata(String key) {
			super();
			this.key = key;
			this.values = new ArrayList<String>();
		}
		
		public Metadata(String key, String firstValue) {
			this(key);
			values.add(firstValue);
		}
	}
	
	
	private EdgeEvent edgeEvent = null;
	private List<Metadata> metadataList = new ArrayList<Metadata>();
	private List<JPhyloIOEvent> metadataEvents = new ArrayList<JPhyloIOEvent>();
	private List<JPhyloIOEvent> commentEvents = new ArrayList<JPhyloIOEvent>();
	private boolean ignoredXMLMetadata = false;
	private boolean ignoredNestedMetadata = false;
	private int metadataLevel = 0;
	
	
	public EdgeDataEventReceiver(Writer writer,	EventWriterParameterMap parameterMap) {
		super(writer, parameterMap);
	}

	
	public EdgeEvent getEdgeEvent() {
		return edgeEvent;
	}


	public List<JPhyloIOEvent> getMetadataEvents() {
		return metadataEvents;
	}


	public List<JPhyloIOEvent> getCommentEvents() {
		return commentEvents;
	}


	public boolean isIgnoredXMLMetadata() {
		return ignoredXMLMetadata;
	}


	public boolean isIgnoredNestedMetadata() {
		return ignoredNestedMetadata;
	}
	
	
	public void clear() {
		edgeEvent = null;
		metadataEvents.clear();
		commentEvents.clear();
		ignoredNestedMetadata = false;
		ignoredXMLMetadata = false;
		metadataLevel = 0;
	}
	
	
	@Override
	public boolean add(JPhyloIOEvent event) throws IllegalArgumentException, IOException {
		if (edgeEvent == null) {
			if (event.getType().getContentType().equals(EventContentType.EDGE) && 
					event.getType().getTopologyType().equals(EventTopologyType.START)) {
				
				edgeEvent = event.asEdgeEvent();  // May throw a class cast exception, if an invalid object with this type was specified.
			}
			else {
				throw new IllegalArgumentException("The first event in a tree/network edge subsequence must be an edge start event " + 
						"but was of the type " + event.getType());
			}
		}
		
		switch (event.getType().getContentType()) {
			case EDGE:
				if (event.getType().getTopologyType().equals(EventTopologyType.END)) {
					return false;  // No more events to come.
				}
				else {
					throw new IllegalArgumentException("Multiple edge start events are not allowed in a tree/network edge subsequence.");
				}
			case COMMENT:
				commentEvents.add(event);
				break;
			case META_INFORMATION:
				if (event.getType().getTopologyType().equals(EventTopologyType.START)) {
					metadataLevel++;
				}
				else {
					metadataLevel--;
				}
				
				MetaInformationEvent metaevent = event.asMetaInformationEvent();
				switch (metadataLevel) {
					case 0:
						Metadata metadata = new Metadata(metaevent.getKey());
						if (metaevent.hasValue()) {
							metadata.values.add(metaevent.getStringValue());
						}
						break;
					case 1:
						
						break;
					default:
						ignoredNestedMetadata = true;
						break;
				}
				if (metadataLevel <= 2) {  // Do not store events that are deeper nested
					metadataEvents.add(event);
				}
				else {
				}
				break;
			case META_XML_CONTENT:
				ignoredXMLMetadata = true;
				break;
			default:
				throw new IllegalArgumentException("Events of the type " + event.getType().getContentType() + 
						" are not allowed in a tree/network edge subsequence.");
		}
		return true;
	}
	
	
	public void writeMetadataAndComments() {
		
	}
}
