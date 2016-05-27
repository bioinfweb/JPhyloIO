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

import javax.xml.stream.XMLStreamException;

import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.dataadapters.implementations.receivers.BasicEventReceiver;
import info.bioinfweb.jphyloio.events.CommentEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralContentSequenceType;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataContentEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.exception.InconsistentAdapterDataException;
import info.bioinfweb.jphyloio.exception.JPhyloIOWriterException;



public class NewickNodeEdgeEventReceiver<E extends JPhyloIOEvent> extends BasicEventReceiver<Writer> 
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
	
	
	private List<Metadata> metadataList = new ArrayList<Metadata>();
	private List<CommentEvent> commentEvents = new ArrayList<CommentEvent>();
	private boolean ignoredXMLMetadata = false;
	private boolean ignoredNestedMetadata = false;
	private StringBuilder currentLiteralValue = new StringBuilder();
	
	
	public NewickNodeEdgeEventReceiver(Writer writer,	ReadWriteParameterMap parameterMap) {
		super(writer, parameterMap);
	}

	
	public boolean isIgnoredXMLMetadata() {
		return ignoredXMLMetadata;
	}


	public boolean isIgnoredNestedMetadata() {
		return ignoredNestedMetadata;
	}
	
	
	private void clearCurrentLiteralValue() {
		currentLiteralValue.delete(0, currentLiteralValue.length());
	}
	
	
	public void clear() {
		commentEvents.clear();
		ignoredNestedMetadata = false;
		ignoredXMLMetadata = false;
		clearCurrentLiteralValue();
	}
	
	
	@Override
	protected void handleLiteralMetaStart(LiteralMetadataEvent event) throws IOException, XMLStreamException {
		if (getParentEvents().isEmpty()) {
			if (event.getSequenceType().equals(LiteralContentSequenceType.SIMPLE) || 
					event.getSequenceType().equals(LiteralContentSequenceType.SIMPLE_ARRAY)) {
				
				String key = event.getPredicate().getStringRepresentation();
				if (key == null) {
					if (event.getPredicate().getURI() == null) {
						throw new JPhyloIOWriterException("A literal metadata event without predicate or alternative string representation was encountered.");  // Should not happen, since this was already checked in the constructor of URIOrStringIdentifier.
					}
					else {
						key = event.getPredicate().getURI().getLocalPart();  // uri cannot be null, if stringRepresentation was null. 
					}
				}
				metadataList.add(new Metadata(key));
				//TODO Add values later. Possibly throw exception e.g. in handleMetaEndEvent, if no value was specified or allow empty annotations.
			}
			else {  // Will also be executed for OTHER.
				ignoredXMLMetadata = true;
			}
		}
		else {
			ignoredNestedMetadata = true;
		}
	}


	@Override
	protected void handleLiteralContentMeta(LiteralMetadataContentEvent event) throws IOException, XMLStreamException {
		if (metadataList.isEmpty()) {
			throw new InternalError("No metadata entry was added for the parent literal meta event.");  // Should not happen.
		}
		else {
			currentLiteralValue.append(event.getStringValue());  // Such events cannot have non-string object values.
			if (!event.isContinuedInNextEvent()) {
				String value = currentLiteralValue.toString();;
				if (!(event.getObjectValue() instanceof Number)) {
					value = NAME_DELIMITER + value.replaceAll("\\" + NAME_DELIMITER, "" + NAME_DELIMITER + NAME_DELIMITER) + NAME_DELIMITER;
				}
				
				metadataList.get(metadataList.size() - 1).values.add(value);
				clearCurrentLiteralValue();
			}
		}
	}


	@Override
	protected void handleMetaEndEvent(JPhyloIOEvent event) throws IOException, XMLStreamException {
		if (event.getType().getContentType().equals(EventContentType.META_LITERAL) && currentLiteralValue.length() > 0) {
			throw new InconsistentAdapterDataException("A literal meta end event was encounterd, although the last literal meta content "
					+ "event was marked to be continued in a subsequent event.");
		}
		//TODO The functionality of this method should be moved to AbstractEventReceiver.
	}


	@Override
	protected void handleComment(CommentEvent event) {
		commentEvents.add(event.asCommentEvent());
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
								getWriter().write(' ');
							}
						}
						getWriter().write(FIELD_END_SYMBOL);
					}
				}
				if (iterator.hasNext()) {
					getWriter().write(ALLOCATION_SEPARATOR_SYMBOL);
					getWriter().write(' ');
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
