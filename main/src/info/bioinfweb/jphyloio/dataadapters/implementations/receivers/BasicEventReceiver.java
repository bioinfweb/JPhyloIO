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


import info.bioinfweb.commons.log.ApplicationLogger;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.dataadapters.JPhyloIOEventReceiver;
import info.bioinfweb.jphyloio.events.CommentEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataContentEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.ResourceMetadataEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.exception.IllegalEventException;
import info.bioinfweb.jphyloio.exception.JPhyloIOWriterException;

import java.io.IOException;
import java.io.Writer;
import java.util.Stack;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;



/**
 * Most event receivers will be inherited from this class. It implements basic shared functionalities mainly for handling
 * metadata and comments. The default behaviour of some methods may need to be overwritten by inherited classes.
 * 
 * @author Ben St&ouml;ver
 *
 * @param <W> the type of writer to write the data to (usually {@link Writer} or {@link XMLStreamWriter})
 */
public class BasicEventReceiver<W extends Object> implements JPhyloIOEventReceiver {
	private W writer;
	private ReadWriteParameterMap parameterMap;
	
	private Stack<JPhyloIOEvent> parentEvents = new Stack<JPhyloIOEvent>();
	
	private long ignoredComments = 0;
	private long ignoredLiteralMetadata = 0;
	private long ignoredResourceMetadata = 0;
	
	
	public BasicEventReceiver(W writer, ReadWriteParameterMap parameterMap) {
		super();
		this.writer = writer;
		this.parameterMap = parameterMap;
	}


	protected W getWriter() {
		return writer;
	}


	protected ReadWriteParameterMap getParameterMap() {
		return parameterMap;
	}
	
	
	protected ApplicationLogger getLogger() {
		return getParameterMap().getApplicationLogger(ReadWriteParameterMap.KEY_LOGGER);
	}


	public Stack<JPhyloIOEvent> getParentEvents() {
		return parentEvents;
	}
	
	
	public JPhyloIOEvent getParentEvent() {
		if (!getParentEvents().isEmpty()) {
			return getParentEvents().peek();
		}
		else {
			return null;			
		}		
	}


	public long getIgnoredComments() {
		return ignoredComments;
	}
	
	
	public boolean didIgnoreComments() {
		return getIgnoredComments() > 0;
	}


	protected void addIgnoredComments(long addend) {
		ignoredComments += addend;
	}


	public long getIgnoredLiteralMetadata() {
		return ignoredLiteralMetadata;
	}


	public boolean didIgnoreLiteralMetadata() {
		return getIgnoredLiteralMetadata() > 0;
	}


	protected void addIgnoredLiteralMetadata(long addend) {
		ignoredLiteralMetadata += addend;
	}
	
	
	public long getIgnoredResourceMetadata() {
		return ignoredResourceMetadata;
	}


	public boolean didIgnoreResourceMetadata() {
		return getIgnoredResourceMetadata() > 0;
	}

	
	public boolean didIgnoreMetadata() {
		return didIgnoreLiteralMetadata() || didIgnoreResourceMetadata();
	}
	
	
	public long getIgnoredMetadata() {
		return getIgnoredLiteralMetadata() + getIgnoredResourceMetadata();
	}
	

	protected void addIgnoredResourceMetadata(long addend) {
		ignoredResourceMetadata += addend;
	}
	
	
	protected void handleLiteralMetaStart(LiteralMetadataEvent event) throws IOException, XMLStreamException {
		addIgnoredLiteralMetadata(1);
	}
	
	
	protected void handleLiteralContentMeta(LiteralMetadataContentEvent event) throws IOException, XMLStreamException {}
	
	
	protected void handleResourceMetaStart(ResourceMetadataEvent event) throws IOException, XMLStreamException {
		addIgnoredResourceMetadata(1);
	}
	
	
	protected void handleComment(CommentEvent event) throws IOException, XMLStreamException {
		addIgnoredComments(1);
	}
	
	
	protected void handleMetaEndEvent(JPhyloIOEvent event) throws IOException, XMLStreamException {}
	
	
	/**
	 * This method is called internally by {@link #add(JPhyloIOEvent)} to process events that do not model metadata or
	 * comments. (Such events are treated by the according special methods of this class).
	 * <p>
	 * This default implementation just throws an {@link IllegalEventException}. Inherited classes that need to support other
	 * events then these modeling metadata or comments, must overwrite this method.
	 * 
	 * @param event the event to be processed
	 * @return {@code true} if more events can be written to this acceptor or {@code false} if writing should
	 *         be aborted
	 * @throws IllegalEventException if the specified event is illegal in this acceptor in general or at
	 *         the current position in the sequence
	 * @throws ClassCastException if an event object was specified that is not an instance of a class associated 
	 *         with its type as document in {@link EventContentType}
	 * @throws IOException if an I/O error occurs when writing to the underlying stream
	 * @throws XMLStreamException if an XML stream error occurs when writing to the underlying stream
	 */
	protected boolean doAdd(JPhyloIOEvent event) throws IOException, XMLStreamException {
		throw IllegalEventException.newInstance(this, getParentEvent(), event);
	}
	
	
	@Override
	public boolean add(JPhyloIOEvent event) throws IOException {
		try {
			boolean result = true;
			JPhyloIOEvent parentEvent = getParentEvent();
			
			if (event.getType().getTopologyType().equals(EventTopologyType.END)) {
				if ((parentEvent == null) || !parentEvent.getType().getContentType().equals(event.getType().getContentType())) {
					throw IllegalEventException.newInstance(this, parentEvent, event);
				}
				else {
					getParentEvents().pop();
					parentEvent = getParentEvent();
				}
			}			
			
			switch (event.getType().getContentType()) {
				case META_RESOURCE:
					if (event.getType().getTopologyType().equals(EventTopologyType.START)) {
						handleResourceMetaStart(event.asResourceMetadataEvent());
					}
					else {
						handleMetaEndEvent(event);
					}
					break;
				case META_LITERAL:
					if ((parentEvent == null) || !parentEvent.getType().getContentType().equals(EventContentType.META_LITERAL)) {
						if (event.getType().getTopologyType().equals(EventTopologyType.START)) {
							handleLiteralMetaStart(event.asLiteralMetadataEvent());
						}
						else {
							handleMetaEndEvent(event);
						}
					}
					else {
						throw IllegalEventException.newInstance(this, parentEvent, event);
					}
					break;
				case META_LITERAL_CONTENT:
					if ((parentEvent != null) && parentEvent.getType().getContentType().equals(EventContentType.META_LITERAL)) {
						handleLiteralContentMeta(event.asLiteralMetadataContentEvent());
					}
					else {
						throw IllegalEventException.newInstance(this, parentEvent, event);
					}
					break;
				case COMMENT:
					handleComment(event.asCommentEvent());
					break;
				default:
					if (parentEvent == null) {
						result = doAdd(event);
					}					
					else {
						throw IllegalEventException.newInstance(this, parentEvent, event);
					}
			}			
			
			if (event.getType().getTopologyType().equals(EventTopologyType.START)) {
				getParentEvents().add(event);
			}
			
			return result;
		}
		catch (XMLStreamException e) {
			throw new JPhyloIOWriterException("An XMLStream exception with the message \"" + e.getMessage() + 
					"\" occured when trying to add an event to this receiver.", e);
		}
	}
}
