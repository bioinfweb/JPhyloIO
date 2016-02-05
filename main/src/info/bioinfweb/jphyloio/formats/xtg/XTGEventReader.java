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
package info.bioinfweb.jphyloio.formats.xtg;


import info.bioinfweb.commons.io.XMLUtils;
import info.bioinfweb.jphyloio.events.CommentEvent;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.formats.xml.AbstractXMLEventReader;
import info.bioinfweb.jphyloio.formats.xml.XMLElementReaderKey;
import info.bioinfweb.jphyloio.formats.xml.XMLStreamDataProvider;

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
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;



public class XTGEventReader extends AbstractXMLEventReader implements XTGConstants {
	private static final Map<XMLElementReaderKey, AbstractXTGElementReader> ELEMENT_READER_MAP = createMap();
	
	
	public XTGEventReader(File file) throws IOException, XMLStreamException {
		super(true, file);
	}

	
	public XTGEventReader(InputStream stream) throws IOException, XMLStreamException {
		super(true, stream);
	}
	

	public XTGEventReader(int maxTokensToRead, XMLEventReader xmlReader) {
		super(true, maxTokensToRead, xmlReader);
	}
	

	public XTGEventReader(Reader reader) throws IOException, XMLStreamException {
		super(true, reader);
	}
	

	public XTGEventReader(XMLEventReader xmlReader) {
		super(true, xmlReader);
	}
	
	
	private static Map<XMLElementReaderKey, AbstractXTGElementReader> createMap() {
		Map<XMLElementReaderKey, AbstractXTGElementReader> map = new HashMap<XMLElementReaderKey, AbstractXTGElementReader>();
		
		AbstractXTGElementReader nodeStartReader = new AbstractXTGElementReader() {			
			@Override
			public void readEvent(XMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
				StartElement element = event.asStartElement();
				String id = DEFAULT_NODE_ID_PREFIX + streamDataProvider.getIdManager().createNewID();
				String label = XMLUtils.readStringAttr(element, ATTR_TEXT, null);
				streamDataProvider.getCurrentEventCollection().add(new LabeledIDEvent(EventContentType.NODE, id, label));		
			}
		};
		
		AbstractXTGElementReader nodeEndReader = new AbstractXTGElementReader() {			
			@Override
			public void readEvent(XMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
				streamDataProvider.getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(EventContentType.NODE, EventTopologyType.END));
			}
		};
		
		map.put(new XMLElementReaderKey(null, null, XMLStreamConstants.START_DOCUMENT), new AbstractXTGElementReader() {
			@Override
			public void readEvent(XMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
				streamDataProvider.getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(EventContentType.DOCUMENT, EventTopologyType.START));
			}
		});
		
		map.put(new XMLElementReaderKey(null, null, XMLStreamConstants.END_DOCUMENT), new AbstractXTGElementReader() {
			@Override
			public void readEvent(XMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
				streamDataProvider.getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(EventContentType.DOCUMENT, EventTopologyType.END));
			}
		});
		
		map.put(new XMLElementReaderKey(TAG_TREE, TAG_NODE, XMLStreamConstants.START_ELEMENT), nodeStartReader);
		
		map.put(new XMLElementReaderKey(TAG_TREE, TAG_NODE, XMLStreamConstants.END_ELEMENT), nodeEndReader);
		
		map.put(new XMLElementReaderKey(TAG_NODE, TAG_NODE, XMLStreamConstants.START_ELEMENT), nodeStartReader);
		
		map.put(new XMLElementReaderKey(TAG_NODE, TAG_NODE, XMLStreamConstants.END_ELEMENT), nodeEndReader);
		
//		map.put(new XMLElementReaderKey(TAG_NODE, TAG_BRANCH, XMLStreamConstants.START_ELEMENT), new AbstractXTGElementReader() {			
//			@Override
//			public void readEvent(XMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
//				System.out.println("Branch start");
//			}
//		});
//		
//		map.put(new XMLElementReaderKey(TAG_NODE, TAG_BRANCH, XMLStreamConstants.END_ELEMENT), new AbstractXTGElementReader() {			
//			@Override
//			public void readEvent(XMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
//				System.out.println("Branch end");
//			}
//		});
		
		map.put(new XMLElementReaderKey(null, null, XMLStreamConstants.COMMENT), new AbstractXTGElementReader() {			
			@Override
			public void readEvent(XMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
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

	
	protected Queue<JPhyloIOEvent> getUpcomingEvents() {
		return super.getUpcomingEvents();
	}
	
	
	protected XMLEventReader getXMLReader() {
		return super.getXMLReader();
	}
	
	
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

			AbstractXTGElementReader elementReader = getElementReader(parentTag, elementTag, xmlEvent.getEventType());
			if (elementReader != null) {
				elementReader.readEvent(getStreamDataProvider(), xmlEvent);
			}			
		}		
	}
	
	
	protected AbstractXTGElementReader getElementReader(QName parentTag, QName elementTag, int eventType) {
		AbstractXTGElementReader result = ELEMENT_READER_MAP.get(new XMLElementReaderKey(parentTag, elementTag, eventType));
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
	
	
	@Override
	public void close() throws Exception {
		super.close();
		getXMLReader().close();
	}
}
