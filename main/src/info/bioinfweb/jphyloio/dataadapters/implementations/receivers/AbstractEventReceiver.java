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
import java.util.Stack;

import javax.xml.stream.XMLStreamException;



public abstract class AbstractEventReceiver<W extends Object> implements JPhyloIOEventReceiver {
	private W writer;
	private ReadWriteParameterMap parameterMap;
	
	private Stack<JPhyloIOEvent> parentEvents = new Stack<JPhyloIOEvent>();
	
	private long ignoredComments = 0;
	private long ignoredLiteralMetadata = 0;
	private long ignoredResourceMetadata = 0;
	
	
	public AbstractEventReceiver(W writer, ReadWriteParameterMap parameterMap) {
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
	
	
	protected abstract boolean doAdd(JPhyloIOEvent event) throws IOException, XMLStreamException;
	
	
	@Override
	public boolean add(JPhyloIOEvent event) throws IOException {
		try {
			boolean result = true;
			JPhyloIOEvent parentEvent = getParentEvent();
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
						IllegalEventException.newInstance(this, parentEvent, event);
					}
					break;
				case META_LITERAL_CONTENT:
					if ((parentEvent == null) || !parentEvent.getType().getContentType().equals(EventContentType.META_LITERAL)) {
						handleLiteralContentMeta(event.asLiteralMetadataContentEvent());
					}
					else {
						IllegalEventException.newInstance(this, parentEvent, event);
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
						IllegalEventException.newInstance(this, parentEvent, event);
					}
			}
			
			if (event.getType().getTopologyType().equals(EventTopologyType.START)) {
				getParentEvents().add(event);
			}
			else if (event.getType().getTopologyType().equals(EventTopologyType.END)) {
				if ((parentEvent == null) || !parentEvent.getType().getContentType().equals(event.getType().getContentType())) {
					IllegalEventException.newInstance(this, parentEvent, event);
				}
				else {
					getParentEvents().pop();
				}
			}
			return result;
		}
		catch (XMLStreamException e) {
			throw new JPhyloIOWriterException("An XMLStream exception with the message \"" + e.getMessage() + 
					"\" occured when trying to add an event to this receiver.", e);
		}
	}
}
