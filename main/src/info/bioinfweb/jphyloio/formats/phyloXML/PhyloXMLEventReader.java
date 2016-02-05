/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015-2016  Ben St�ver, Sarah Wiechers
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
package info.bioinfweb.jphyloio.formats.phyloXML;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Comment;
import javax.xml.stream.events.XMLEvent;

import info.bioinfweb.jphyloio.events.CommentEvent;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.formats.nexml.XMLElementReaderKey;
import info.bioinfweb.jphyloio.formats.xml.AbstractXMLEventReader;
import info.bioinfweb.jphyloio.formats.xml.XMLStreamDataProvider;



public class PhyloXMLEventReader extends AbstractXMLEventReader implements PhyloXMLConstants {
	private static final Map<XMLElementReaderKey, AbstractPhyloXMLElementReader> ELEMENT_READER_MAP = createMap();
	
	
	public PhyloXMLEventReader(File file) throws IOException, XMLStreamException {
		super(true, file);
	}

	
	public PhyloXMLEventReader(InputStream stream) throws IOException, XMLStreamException {
		super(true, stream);
	}
	

	public PhyloXMLEventReader(int maxTokensToRead, XMLEventReader xmlReader) {
		super(true, maxTokensToRead, xmlReader);
	}
	

	public PhyloXMLEventReader(Reader reader) throws IOException, XMLStreamException {
		super(true, reader);
	}
	

	public PhyloXMLEventReader(XMLEventReader xmlReader) {
		super(true, xmlReader);
	}
	
	
	private static Map<XMLElementReaderKey, AbstractPhyloXMLElementReader> createMap() {
		Map<XMLElementReaderKey, AbstractPhyloXMLElementReader> map = new HashMap<XMLElementReaderKey, AbstractPhyloXMLElementReader>();
		
		AbstractPhyloXMLElementReader cladeStartReader = new AbstractPhyloXMLElementReader() {
			@Override
			protected void readEvent(XMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
				String id = DEFAULT_NODE_ID_PREFIX + streamDataProvider.getIDManager().createNewID();
				streamDataProvider.getCurrentEventCollection().add(new LabeledIDEvent(EventContentType.NODE, id, null));
			//TODO probably read name/label and node_id from according subtags
			}
		};
		
		AbstractPhyloXMLElementReader cladeEndReader = new AbstractPhyloXMLElementReader() {
			@Override
			protected void readEvent(XMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
				streamDataProvider.getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(EventContentType.NODE, EventTopologyType.END));
			}
		};
		
		map.put(new XMLElementReaderKey(null, null, XMLStreamConstants.START_DOCUMENT), new AbstractPhyloXMLElementReader() {
			@Override
			protected void readEvent(XMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
				streamDataProvider.getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(EventContentType.DOCUMENT, EventTopologyType.START));
			}
		});
		
		map.put(new XMLElementReaderKey(null, null, XMLStreamConstants.END_DOCUMENT), new AbstractPhyloXMLElementReader() {
			@Override
			protected void readEvent(XMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
				streamDataProvider.getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(EventContentType.DOCUMENT, EventTopologyType.END));
			}
		});
				
		map.put(new XMLElementReaderKey(TAG_PHYLOXML, TAG_PHYLOGENY, XMLStreamConstants.START_ELEMENT), new AbstractPhyloXMLElementReader() {
			@Override
			protected void readEvent(XMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
				String id = DEFAULT_TREE_ID_PREFIX + streamDataProvider.getIDManager().createNewID();
				streamDataProvider.getCurrentEventCollection().add(new LabeledIDEvent(EventContentType.TREE, id, null));
				//TODO probably read name/label and id from according subtags
			}
		});
		
		map.put(new XMLElementReaderKey(TAG_PHYLOXML, TAG_PHYLOGENY, XMLStreamConstants.END_ELEMENT), new AbstractPhyloXMLElementReader() {
			@Override
			protected void readEvent(XMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
				streamDataProvider.getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(EventContentType.TREE, EventTopologyType.END));			}
		});
		
		map.put(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_CLADE, XMLStreamConstants.START_ELEMENT), cladeStartReader);
		
		map.put(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_CLADE, XMLStreamConstants.END_ELEMENT), cladeEndReader);
		
		map.put(new XMLElementReaderKey(TAG_CLADE, TAG_CLADE, XMLStreamConstants.START_ELEMENT), cladeStartReader);
		
		map.put(new XMLElementReaderKey(TAG_CLADE, TAG_CLADE, XMLStreamConstants.END_ELEMENT), cladeEndReader);
		
		map.put(new XMLElementReaderKey(null, null, XMLStreamConstants.COMMENT), new AbstractPhyloXMLElementReader() {			
			@Override
			protected void readEvent(XMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
				String comment = ((Comment)event).getText();
				streamDataProvider.getCurrentEventCollection().add(new CommentEvent(comment, false));
			}
		});
		
		return map;
	}

	
	@Override
	public int getMaxCommentLength() {
		return 0;
	}
	

	@Override
	public void setMaxCommentLength(int maxCommentLength) {}

	
	@Override
	protected XMLStreamDataProvider createStreamDataProvider() {
		return new XMLStreamDataProvider(this);
	}


	@Override
	protected XMLStreamDataProvider getStreamDataProvider() {
		return (XMLStreamDataProvider)super.getStreamDataProvider();
	}
	
	
	@Override
	protected void readNextEvent() throws Exception {
		while (getXMLReader().hasNext() && getUpcomingEvents().isEmpty()) {
			XMLEvent xmlEvent = getXMLReader().nextEvent();
			QName parentTag = null;			
			QName elementTag = null;
			
			switch (xmlEvent.getEventType()) {
				case XMLStreamConstants.START_DOCUMENT:
					elementTag = null;
					break;
				case XMLStreamConstants.END_DOCUMENT:
					elementTag = null;
					break;
				case XMLStreamConstants.START_ELEMENT:
					elementTag = xmlEvent.asStartElement().getName();
					break;
				case XMLStreamConstants.END_ELEMENT:
					getEncounteredTags().pop();
					elementTag = xmlEvent.asEndElement().getName();
					break;
				default: 
					break;  // Nothing to do.
			}

			if (!getEncounteredTags().isEmpty()) {
				parentTag = getEncounteredTags().peek();
			}
			else {
				parentTag = TAG_ROOT;
			}		
			
			if (xmlEvent.isStartElement()) {
				getEncounteredTags().push(xmlEvent.asStartElement().getName());
			}

			AbstractPhyloXMLElementReader elementReader = getElementReader(parentTag, elementTag, xmlEvent.getEventType());
			if (elementReader != null) {
				elementReader.readEvent(getStreamDataProvider(), xmlEvent);
			}			
		}		
	}
	
	
	protected AbstractPhyloXMLElementReader getElementReader(QName parentTag, QName elementTag, int eventType) {
		AbstractPhyloXMLElementReader result = ELEMENT_READER_MAP.get(new XMLElementReaderKey(parentTag, elementTag, eventType));
		if (result == null) {
			result = ELEMENT_READER_MAP.get(new XMLElementReaderKey(null, elementTag, eventType));
			if (result == null) {
				result = ELEMENT_READER_MAP.get(new XMLElementReaderKey(parentTag, null, eventType));
				if (result == null) {
					result = ELEMENT_READER_MAP.get(new XMLElementReaderKey(null, null, eventType));
				}
			}
		}
		return result;
	}
}
