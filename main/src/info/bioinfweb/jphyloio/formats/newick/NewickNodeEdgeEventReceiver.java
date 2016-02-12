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
import java.util.Iterator;
import java.util.List;

import info.bioinfweb.jphyloio.AbstractEventReceiver;
import info.bioinfweb.jphyloio.EventWriterParameterMap;
import info.bioinfweb.jphyloio.events.CommentEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.MetaInformationEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.events.type.EventType;



public class NewickNodeEdgeEventReceiver<E extends JPhyloIOEvent> extends AbstractEventReceiver 
		implements NewickConstants {
	
	public static final char STRING_DELEMITER_REPLACEMENT = '\'';
	
	
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
	
	
	private EventContentType startEventType;
	private E startEvent = null;
	private List<Metadata> metadataList = new ArrayList<Metadata>();
	private List<CommentEvent> commentEvents = new ArrayList<CommentEvent>();
	private boolean ignoredXMLMetadata = false;
	private boolean ignoredNestedMetadata = false;
	private int metadataLevel = 0;
	
	
	public NewickNodeEdgeEventReceiver(Writer writer,	EventWriterParameterMap parameterMap, EventContentType startEventType) {
		super(writer, parameterMap);
		this.startEventType = startEventType;
	}

	
	public EventContentType getStartEventType() {
		return startEventType;
	}


	public E getStartEvent() {
		return startEvent;
	}


	public boolean isIgnoredXMLMetadata() {
		return ignoredXMLMetadata;
	}


	public boolean isIgnoredNestedMetadata() {
		return ignoredNestedMetadata;
	}
	
	
	public void clear() {
		startEvent = null;
		commentEvents.clear();
		ignoredNestedMetadata = false;
		ignoredXMLMetadata = false;
		metadataLevel = 0;
	}
	
	
	private String createValue(MetaInformationEvent event) {
		if (event.getObjectValue() instanceof Number) {
			return event.getStringValue();  // Do not enclose numbers in string delimiters.
		}
		else {
			return HotCommentDataReader.STRING_DELIMITER + 
					event.getStringValue().replace(HotCommentDataReader.STRING_DELIMITER, STRING_DELEMITER_REPLACEMENT) + 
					HotCommentDataReader.STRING_DELIMITER;
		}
	}
	
	
	@Override
	public boolean add(JPhyloIOEvent event) throws IllegalArgumentException, IOException {
		if (startEvent == null) {
			if (event.getType().getContentType().equals(startEventType) && 
					event.getType().getTopologyType().equals(EventTopologyType.START)) {
				
				startEvent = (E)event;  // May throw a class cast exception later, if an invalid object with this type was specified.
			}
			else {
				throw new IllegalArgumentException("The first event in this subsequence is expected to have the type " + 
						new EventType(startEventType, EventTopologyType.START) + " but was of the type of type " + event.getType());
			}
		}
		
		if (startEventType.equals(event.getType().getContentType())) {  // Needs to be handled here, because switch only allows constants
			if (event.getType().getTopologyType().equals(EventTopologyType.END)) {
				return false;  // No more events to come.
			}
			else {
				throw new IllegalArgumentException("Multiple edge start events are not allowed in a tree/network edge subsequence.");
			}
		}
		else {
			switch (event.getType().getContentType()) {
				case COMMENT:
					commentEvents.add(event.asCommentEvent());
					break;
				case META_INFORMATION:
					if (event.getType().getTopologyType().equals(EventTopologyType.START)) {
						metadataLevel++;
						
						if (metadataLevel == 1) {  // Ignore nested events.
							MetaInformationEvent metaevent = event.asMetaInformationEvent();
							Metadata metadata = new Metadata(metaevent.getKey());
							if (metaevent.hasValue()) {
								metadata.values.add(createValue(metaevent));
							}
						}  //TODO Implement adding array values, when concept is clear.
						else {
							ignoredNestedMetadata = true;
						}
					}
					else {
						metadataLevel--;
					}
					break;
				case META_XML_CONTENT:
					ignoredXMLMetadata = true;
					break;
				default:
					throw new IllegalArgumentException("Events of the type " + event.getType().getContentType() + 
							" are not allowed in a tree/network edge subsequence.");
			}
		}
		return true;
	}
	
	
	public void writeMetadata() throws IOException {
		if (!metadataList.isEmpty()) {
			getWriter().write(COMMENT_START);
			getWriter().write(HOT_COMMENT_START_SYMBOL);
			Iterator<Metadata> iterator = metadataList.iterator();
			while (iterator.hasNext()) {
				Metadata metadata = iterator.next();
				getWriter().write(metadata.key);
				getWriter().write(ALLOCATION_SYMBOL);
				if (!metadata.values.isEmpty()) {
					if (metadata.values.size() == 1) {
						getWriter().write(metadata.values.get(0));  // Necessary string delimiters have already been added.
					}
					else {
						Iterator<String> valuesIterator = metadata.values.iterator();
						getWriter().write(FIELD_START_SYMBOL);
						while (valuesIterator.hasNext()) {
							getWriter().write(valuesIterator.next());
							if (valuesIterator.hasNext()) {
								getWriter().write(FIELD_VALUE_SEPARATOR_SYMBOL);
							}
						}
						getWriter().write(FIELD_END_SYMBOL);
					}
				}
				if (iterator.hasNext()) {
					getWriter().write(ALLOCATION_SEPARATOR_SYMBOL);
				}
			}
			getWriter().write(COMMENT_END);
		}
	}

	
	public void writeComments() throws IOException {
		Iterator<CommentEvent> iterator = commentEvents.iterator();
		while (iterator.hasNext()) {
			getWriter().write(COMMENT_START);
			CommentEvent event = iterator.next();
			getWriter().write(event.getContent());
			
			while (event.isContinuedInNextEvent() && iterator.hasNext()) {
				event = iterator.next();
				getWriter().write(event.getContent());
			}
			getWriter().write(COMMENT_END);
		}
	}
}
