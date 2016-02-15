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
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.EdgeEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.MetaInformationEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.formats.NodeEdgeInfo;
import info.bioinfweb.jphyloio.formats.xml.AbstractXMLEventReader;
import info.bioinfweb.jphyloio.formats.xml.CommentElementReader;
import info.bioinfweb.jphyloio.formats.xml.UnknownMetaElementEndReader;
import info.bioinfweb.jphyloio.formats.xml.UnknownMetaElementStartReader;
import info.bioinfweb.jphyloio.formats.xml.XMLElementReader;
import info.bioinfweb.jphyloio.formats.xml.XMLElementReaderKey;
import info.bioinfweb.jphyloio.formats.xml.XMLStreamDataProvider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
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
	
	
	@SuppressWarnings("unchecked")
	@Override
	protected void fillMap() {
		Map<XMLElementReaderKey, XMLElementReader<XMLStreamDataProvider<XTGEventReader>>> map = getElementReaderMap();
		
		XMLElementReader<XMLStreamDataProvider<XTGEventReader>> nodeStartReader = new XMLElementReader<XMLStreamDataProvider<XTGEventReader>>() {			
			@Override
			public void readEvent(XMLStreamDataProvider<XTGEventReader> streamDataProvider, XMLEvent event) throws Exception {
				StartElement element = event.asStartElement();
				String id = getID(XMLUtils.readStringAttr(element, ATTR_UNIQUE_NAME, null), EventContentType.NODE);
				String label = XMLUtils.readStringAttr(element, ATTR_TEXT, null);
				
				streamDataProvider.getCurrentEventCollection().add(new LabeledIDEvent(EventContentType.NODE, id, label));
				streamDataProvider.setCurrentNodeEdgeInfo(new NodeEdgeInfo(DEFAULT_EDGE_ID_PREFIX + streamDataProvider.getIDManager().createNewID(), Double.NaN));
				if (!streamDataProvider.getSourceNode().isEmpty()) {
					streamDataProvider.getCurrentNodeEdgeInfo().setSource(streamDataProvider.getSourceNode().peek());
				}
				else {
					streamDataProvider.getCurrentNodeEdgeInfo().setSource(null);
				}
				streamDataProvider.getCurrentNodeEdgeInfo().setTarget(id);
				streamDataProvider.getSourceNode().add(id);
				
				readAttributes(element, XTG + "." + streamDataProvider.getParentName() + "." + element.getName().getLocalPart());
			}
		};
		
		XMLElementReader<XMLStreamDataProvider<XTGEventReader>> nodeEndReader = new XMLElementReader<XMLStreamDataProvider<XTGEventReader>>() {			
			@Override
			public void readEvent(XMLStreamDataProvider<XTGEventReader> streamDataProvider, XMLEvent event) throws Exception {
				streamDataProvider.getSourceNode().pop();
				streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.NODE));
			}
		};
		
		XMLElementReader<XMLStreamDataProvider<XTGEventReader>> createMetaEnd = new XMLElementReader<XMLStreamDataProvider<XTGEventReader>>() {			
			@Override
			public void readEvent(XMLStreamDataProvider<XTGEventReader> streamDataProvider, XMLEvent event) throws Exception {
				streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_INFORMATION));
			}
		};
		
		XMLElementReader<XMLStreamDataProvider<XTGEventReader>> emptyReader = new XMLElementReader<XMLStreamDataProvider<XTGEventReader>>() {			
			@Override
			public void readEvent(XMLStreamDataProvider<XTGEventReader> streamDataProvider, XMLEvent event) throws Exception {}
		};
		
		map.put(new XMLElementReaderKey(null, null, XMLStreamConstants.START_DOCUMENT), new XMLElementReader<XMLStreamDataProvider<XTGEventReader>>() {
			@Override
			public void readEvent(XMLStreamDataProvider<XTGEventReader> streamDataProvider, XMLEvent event) throws Exception {
				streamDataProvider.getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(EventContentType.DOCUMENT, EventTopologyType.START));
				streamDataProvider.setFormat(XTG);
			}
		});
		
		map.put(new XMLElementReaderKey(null, null, XMLStreamConstants.END_DOCUMENT), new XMLElementReader<XMLStreamDataProvider<XTGEventReader>>() {
			@Override
			public void readEvent(XMLStreamDataProvider<XTGEventReader> streamDataProvider, XMLEvent event) throws Exception {
				streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.DOCUMENT));
			}
		});
		
		map.put(new XMLElementReaderKey(null, TAG_DOCUMENT, XMLStreamConstants.START_ELEMENT), emptyReader);
		
		map.put(new XMLElementReaderKey(null, TAG_DOCUMENT, XMLStreamConstants.END_ELEMENT), emptyReader);
		
		map.put(new XMLElementReaderKey(TAG_DOCUMENT, TAG_TREE, XMLStreamConstants.START_ELEMENT), new XMLElementReader<XMLStreamDataProvider<XTGEventReader>>() {			
			@Override
			public void readEvent(XMLStreamDataProvider<XTGEventReader> streamDataProvider, XMLEvent event) throws Exception {
				streamDataProvider.setSourceNode(new Stack<String>());
				String treeID = getID(null, EventContentType.TREE);							
				streamDataProvider.getCurrentEventCollection().add(new LabeledIDEvent(EventContentType.TREE, treeID, null));		
			}
		});
		
		map.put(new XMLElementReaderKey(TAG_DOCUMENT, TAG_TREE, XMLStreamConstants.END_ELEMENT), new XMLElementReader<XMLStreamDataProvider<XTGEventReader>>() {			
			@Override
			public void readEvent(XMLStreamDataProvider<XTGEventReader> streamDataProvider, XMLEvent event) throws Exception {
				streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.TREE));
			}
		});
		
		map.put(new XMLElementReaderKey(TAG_TREE, TAG_NODE, XMLStreamConstants.START_ELEMENT), nodeStartReader);
		
		map.put(new XMLElementReaderKey(TAG_TREE, TAG_NODE, XMLStreamConstants.END_ELEMENT), nodeEndReader);
		
		map.put(new XMLElementReaderKey(TAG_NODE, TAG_NODE, XMLStreamConstants.START_ELEMENT), nodeStartReader);
		
		map.put(new XMLElementReaderKey(TAG_NODE, TAG_NODE, XMLStreamConstants.END_ELEMENT), nodeEndReader);
		
		map.put(new XMLElementReaderKey(TAG_NODE, TAG_BRANCH, XMLStreamConstants.START_ELEMENT), new XMLElementReader<XMLStreamDataProvider<XTGEventReader>>() {			
			@Override
			public void readEvent(XMLStreamDataProvider<XTGEventReader> streamDataProvider, XMLEvent event) throws Exception {
				StartElement element = event.asStartElement();				
				double length = XMLUtils.readDoubleAttr(element, ATTR_BRANCH_LENGTH, Double.NaN);
				
				streamDataProvider.getCurrentNodeEdgeInfo().setLength(length);
				streamDataProvider.setCurrentEventCollection(new ArrayList<JPhyloIOEvent>());
				
				readAttributes(element, XTG + "." + streamDataProvider.getParentName() + "." + element.getName().getLocalPart());
			}
		});
		
		map.put(new XMLElementReaderKey(TAG_NODE, TAG_BRANCH, XMLStreamConstants.END_ELEMENT), new XMLElementReader<XMLStreamDataProvider<XTGEventReader>>() {			
			@Override
			public void readEvent(XMLStreamDataProvider<XTGEventReader> streamDataProvider, XMLEvent event) throws Exception {
				Collection<JPhyloIOEvent> nestedEvents = streamDataProvider.resetCurrentEventCollection();
				NodeEdgeInfo info = streamDataProvider.getCurrentNodeEdgeInfo();
				
				streamDataProvider.getCurrentEventCollection().add(new EdgeEvent(info.getID(), info.getLabel(), info.getSource(), 
						info.getTarget(), info.getLength()));
				for (JPhyloIOEvent nextEvent : nestedEvents) {
					streamDataProvider.getCurrentEventCollection().add(nextEvent);
				}
				streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.EDGE));				
			}
		});
		
		map.put(new XMLElementReaderKey(TAG_BRANCH, TAG_TEXT_LABEL, XMLStreamConstants.START_ELEMENT), new XMLElementReader<XMLStreamDataProvider<XTGEventReader>>() {			
			@Override
			public void readEvent(XMLStreamDataProvider<XTGEventReader> streamDataProvider, XMLEvent event) throws Exception {
				StartElement element = event.asStartElement();
				String label = XMLUtils.readStringAttr(element, ATTR_TEXT, null);
				streamDataProvider.getCurrentNodeEdgeInfo().setLabel(label);
				
				streamDataProvider.getCurrentEventCollection().add(new MetaInformationEvent(XTG + "." + streamDataProvider.getParentName() 
						+ "." + element.getName().getLocalPart(), "String", null));
			}
		});
		
		map.put(new XMLElementReaderKey(TAG_BRANCH, TAG_TEXT_LABEL, XMLStreamConstants.END_ELEMENT), createMetaEnd);
		
		map.put(new XMLElementReaderKey(TAG_TREE, TAG_SCALE_BAR, XMLStreamConstants.START_ELEMENT), emptyReader);
		
		map.put(new XMLElementReaderKey(TAG_TREE, TAG_SCALE_BAR, XMLStreamConstants.END_ELEMENT), emptyReader);
		
		map.put(new XMLElementReaderKey(TAG_TREE, TAG_LEGEND, XMLStreamConstants.START_ELEMENT), emptyReader);
		
		map.put(new XMLElementReaderKey(TAG_TREE, TAG_LEGEND, XMLStreamConstants.END_ELEMENT), emptyReader);
		
		map.put(new XMLElementReaderKey(TAG_TREE, TAG_LEGEND_MARGIN, XMLStreamConstants.START_ELEMENT), emptyReader);
		
		map.put(new XMLElementReaderKey(TAG_TREE, TAG_LEGEND_MARGIN, XMLStreamConstants.END_ELEMENT), emptyReader);
		
		map.put(new XMLElementReaderKey(null, null, XMLStreamConstants.START_ELEMENT), new UnknownMetaElementStartReader());
		
		map.put(new XMLElementReaderKey(null, null, XMLStreamConstants.END_ELEMENT), new UnknownMetaElementEndReader());
		
		map.put(new XMLElementReaderKey(null, null, XMLStreamConstants.COMMENT), new CommentElementReader());
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
