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
package info.bioinfweb.jphyloio.formats.xml;


import info.bioinfweb.jphyloio.JPhyloIOEventReader;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.events.type.EventType;
import info.bioinfweb.jphyloio.push.JPhyloIOEventListener;

import java.io.IOException;

import javax.xml.stream.XMLEventFactory;



/**
 * Implements shared functionality for custom XML readers.
 * 
 * @author Sarah Wiechers
 *
 */
public abstract class AbstractMetaXMLReader<P extends AbstractXMLEventReader<XMLReaderStreamDataProvider<P>>> {
	private P jPhyloIOEventReader;
	private MetaEventListener listener = new MetaEventListener();
	private XMLEventFactory eventFactory = XMLEventFactory.newInstance();
	private boolean endReached = false;
	private boolean startDocumentFired;
	private boolean endDocumentFired;
	
	
	private class MetaEventListener implements JPhyloIOEventListener { 
		@Override
		public void processEvent(JPhyloIOEventReader source, JPhyloIOEvent event) throws IOException {
			if (source.peek().getType().equals(new EventType(EventContentType.META_LITERAL, EventTopologyType.END))) {
				setEndReached();
			}
		}
	}


	public AbstractMetaXMLReader(P jPhyloIOEventReader) {
		super();
		this.jPhyloIOEventReader = jPhyloIOEventReader;
		getJPhyloIOEventReader().addEventListener(listener);
	}
	

	public Object getProperty(String name) throws IllegalArgumentException {
		return getJPhyloIOEventReader().getXMLReader().getProperty(name);  // It is possible that implementation specific objects have properties that can be changed by the application in a way that our code does not work anymore
	}
	
	
	protected void setEndReached() {
		endReached = true;
		getJPhyloIOEventReader().removeEventListener(listener);
	}


	public P getJPhyloIOEventReader() {
		return jPhyloIOEventReader;
	}


	protected XMLEventFactory getEventFactory() {
		return eventFactory;
	}


	protected boolean isEndReached() {
		return endReached;
	}


	protected boolean isStartDocumentFired() {
		return startDocumentFired;
	}


	protected void setStartDocumentFired(boolean startDocumentFired) {
		this.startDocumentFired = startDocumentFired;
	}


	protected boolean isEndDocumentFired() {
		return endDocumentFired;
	}


	protected void setEndDocumentFired(boolean endDocumentFired) {
		this.endDocumentFired = endDocumentFired;
	}
}
