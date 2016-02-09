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
package info.bioinfweb.jphyloio.formats.xtg;


import info.bioinfweb.commons.io.XMLUtils;
import info.bioinfweb.jphyloio.events.CommentEvent;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.formats.phyloxml.PhyloXMLEventReader;
import info.bioinfweb.jphyloio.formats.xml.AbstractXMLEventReader;
import info.bioinfweb.jphyloio.formats.xml.XMLElementReader;
import info.bioinfweb.jphyloio.formats.xml.XMLElementReaderKey;
import info.bioinfweb.jphyloio.formats.xml.XMLStreamDataProvider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Comment;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;



public class XTGEventReader extends AbstractXMLEventReader<XMLStreamDataProvider<XTGEventReader>> implements XTGConstants {	
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
	
	
	@Override
	protected void fillMap() {
		Map<XMLElementReaderKey, XMLElementReader<XMLStreamDataProvider<XTGEventReader>>> map = getElementReaderMap();
		
		XMLElementReader<XMLStreamDataProvider<XTGEventReader>> nodeStartReader = new XMLElementReader<XMLStreamDataProvider<XTGEventReader>>() {			
			@Override
			public void readEvent(XMLStreamDataProvider<XTGEventReader> streamDataProvider, XMLEvent event) throws Exception {
				StartElement element = event.asStartElement();
				String id = DEFAULT_NODE_ID_PREFIX + streamDataProvider.getIdManager().createNewID();
				String label = XMLUtils.readStringAttr(element, ATTR_TEXT, null);
				streamDataProvider.getCurrentEventCollection().add(new LabeledIDEvent(EventContentType.NODE, id, label));		
			}
		};
		
		XMLElementReader<XMLStreamDataProvider<XTGEventReader>> nodeEndReader = new XMLElementReader<XMLStreamDataProvider<XTGEventReader>>() {			
			@Override
			public void readEvent(XMLStreamDataProvider<XTGEventReader> streamDataProvider, XMLEvent event) throws Exception {
				streamDataProvider.getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(EventContentType.NODE, EventTopologyType.END));
			}
		};
		
		map.put(new XMLElementReaderKey(null, null, XMLStreamConstants.START_DOCUMENT), new XMLElementReader<XMLStreamDataProvider<XTGEventReader>>() {
			@Override
			public void readEvent(XMLStreamDataProvider<XTGEventReader> streamDataProvider, XMLEvent event) throws Exception {
				streamDataProvider.getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(EventContentType.DOCUMENT, EventTopologyType.START));
			}
		});
		
		map.put(new XMLElementReaderKey(null, null, XMLStreamConstants.END_DOCUMENT), new XMLElementReader<XMLStreamDataProvider<XTGEventReader>>() {
			@Override
			public void readEvent(XMLStreamDataProvider<XTGEventReader> streamDataProvider, XMLEvent event) throws Exception {
				streamDataProvider.getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(EventContentType.DOCUMENT, EventTopologyType.END));
			}
		});
		
		map.put(new XMLElementReaderKey(TAG_TREE, TAG_NODE, XMLStreamConstants.START_ELEMENT), nodeStartReader);
		
		map.put(new XMLElementReaderKey(TAG_TREE, TAG_NODE, XMLStreamConstants.END_ELEMENT), nodeEndReader);
		
		map.put(new XMLElementReaderKey(TAG_NODE, TAG_NODE, XMLStreamConstants.START_ELEMENT), nodeStartReader);
		
		map.put(new XMLElementReaderKey(TAG_NODE, TAG_NODE, XMLStreamConstants.END_ELEMENT), nodeEndReader);
		
//		map.put(new XMLElementReaderKey(TAG_NODE, TAG_BRANCH, XMLStreamConstants.START_ELEMENT), new XMLElementReader<XMLStreamDataProvider<XTGEventReader>>() {			
//			@Override
//			public void readEvent(XMLStreamDataProvider<XTGEventReader> streamDataProvider, XMLEvent event) throws Exception {
//				System.out.println("Branch start");
//			}
//		});
//		
//		map.put(new XMLElementReaderKey(TAG_NODE, TAG_BRANCH, XMLStreamConstants.END_ELEMENT), new XMLElementReader<XMLStreamDataProvider<XTGEventReader>>() {			
//			@Override
//			public void readEvent(XMLStreamDataProvider<XTGEventReader> streamDataProvider, XMLEvent event) throws Exception {
//				System.out.println("Branch end");
//			}
//		});
		
		map.put(new XMLElementReaderKey(null, null, XMLStreamConstants.COMMENT), new XMLElementReader<XMLStreamDataProvider<XTGEventReader>>() {			
			@Override
			public void readEvent(XMLStreamDataProvider<XTGEventReader> streamDataProvider, XMLEvent event) throws Exception {
				String comment = ((Comment)event).getText();
				streamDataProvider.getCurrentEventCollection().add(new CommentEvent(comment, false));
			}
		});
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
	public void close() throws Exception {
		super.close();
		getXMLReader().close();
	}
}
