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
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.EdgeEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.LinkedOTUOrOTUsEvent;
import info.bioinfweb.jphyloio.events.MetaInformationEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.formats.JPhyloIOFormatIDs;
import info.bioinfweb.jphyloio.formats.NodeEdgeInfo;
import info.bioinfweb.jphyloio.formats.xml.AbstractXMLElementReader;
import info.bioinfweb.jphyloio.formats.xml.AbstractXMLEventReader;
import info.bioinfweb.jphyloio.formats.xml.CommentElementReader;
import info.bioinfweb.jphyloio.formats.xml.XMLToMetaElementStartReader;
import info.bioinfweb.jphyloio.formats.xml.XMLElementReaderKey;
import info.bioinfweb.jphyloio.formats.xml.XMLReaderStreamDataProvider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;



/**
 * Event reader for the <a href="http://bioinfweb.info/xmlns/xtg">extensible TreeGraph 2 format (XTG)</a> used by the 
 * phylogenetic tree editor <a href="http://treegraph.bioinfweb.info/">TreeGraph 2</a>.
 * 
 * <h3><a name="parameters"></a>Recognized parameters</h3> 
 * <ul>
 *   <li>{@link ReadWriteParameterMap#KEY_ALLOW_DEFAULT_NAMESPACE}</li>
 *   <li>{@link ReadWriteParameterMap#KEY_LOGGER}</li>
 * </ul>
 * 
 * @author Sarah Wiechers
 * @author Ben St&ouml;ver
 * @since 0.0.0
 */
public class XTGEventReader extends AbstractXMLEventReader<XMLReaderStreamDataProvider<XTGEventReader>> implements XTGConstants {	
	public XTGEventReader(File file, ReadWriteParameterMap parameters) throws IOException, XMLStreamException {
		super(file, parameters);
	}


	public XTGEventReader(InputStream stream, ReadWriteParameterMap parameters)	throws IOException, XMLStreamException {
		super(stream, parameters);
	}


	public XTGEventReader(Reader reader, ReadWriteParameterMap parameters) throws IOException, XMLStreamException {
		super(reader, parameters);
	}


	public XTGEventReader(XMLEventReader xmlReader,	ReadWriteParameterMap parameters) {
		super(xmlReader, parameters);
	}


	@Override
	public String getFormatID() {
		return JPhyloIOFormatIDs.XTG_FORMAT_ID;
	}


	@SuppressWarnings("unchecked")
	@Override
	protected void fillMap() {
		AbstractXMLElementReader<XMLReaderStreamDataProvider<XTGEventReader>> nodeStartReader = new AbstractXMLElementReader<XMLReaderStreamDataProvider<XTGEventReader>>() {			
			@Override
			public void readEvent(XMLReaderStreamDataProvider<XTGEventReader> streamDataProvider, XMLEvent event) throws Exception {
				StartElement element = event.asStartElement();
				String id = getID(null, EventContentType.NODE);
				String label = XMLUtils.readStringAttr(element, ATTR_TEXT, null);
				
				streamDataProvider.getCurrentEventCollection().add(new LinkedOTUOrOTUsEvent(EventContentType.NODE, id, label, null));
				NodeEdgeInfo branchInfo = new NodeEdgeInfo(DEFAULT_EDGE_ID_PREFIX + streamDataProvider.getIDManager().createNewID(), Double.NaN, null);

				if (!streamDataProvider.getSourceNode().isEmpty()) {
					branchInfo.setSource(streamDataProvider.getSourceNode().peek().getID());
				}
				else {
					branchInfo.setSource(null);
				}
				branchInfo.setTarget(id);
				
				streamDataProvider.getSourceNode().add(new NodeEdgeInfo(id, Double.NaN, new ArrayList<JPhyloIOEvent>()));
				streamDataProvider.getEdgeInfos().add(branchInfo);
				
				readAttributes(streamDataProvider, element);
			}
		};
		
		AbstractXMLElementReader<XMLReaderStreamDataProvider<XTGEventReader>> nodeEndReader = new AbstractXMLElementReader<XMLReaderStreamDataProvider<XTGEventReader>>() {			
			@Override
			public void readEvent(XMLReaderStreamDataProvider<XTGEventReader> streamDataProvider, XMLEvent event) throws Exception {
				streamDataProvider.resetCurrentEventCollection();
				Collection<JPhyloIOEvent> nestedEdgeEvents = streamDataProvider.getSourceNode().pop().getNestedEvents();
				streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.NODE));
				for (JPhyloIOEvent nextEvent : nestedEdgeEvents) {
					streamDataProvider.getCurrentEventCollection().add(nextEvent);
				}
			}
		};
		
		AbstractXMLElementReader<XMLReaderStreamDataProvider<XTGEventReader>> createMetaEnd = new AbstractXMLElementReader<XMLReaderStreamDataProvider<XTGEventReader>>() {			
			@Override
			public void readEvent(XMLReaderStreamDataProvider<XTGEventReader> streamDataProvider, XMLEvent event) throws Exception {
				streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_INFORMATION));
			}
		};
		
//		AbstractXMLElementReader<XMLStreamDataProvider<XTGEventReader>> idDataReader = new AbstractXMLElementReader<XMLStreamDataProvider<XTGEventReader>>() {			
//			@Override
//			public void readEvent(XMLStreamDataProvider<XTGEventReader> streamDataProvider, XMLEvent event) throws Exception {
//				  
//			}
//		};
		
		AbstractXMLElementReader<XMLReaderStreamDataProvider<XTGEventReader>> emptyReader = new AbstractXMLElementReader<XMLReaderStreamDataProvider<XTGEventReader>>() {			
			@Override
			public void readEvent(XMLReaderStreamDataProvider<XTGEventReader> streamDataProvider, XMLEvent event) throws Exception {}
		};
		
		putElementReader(new XMLElementReaderKey(null, null, XMLStreamConstants.START_DOCUMENT), new AbstractXMLElementReader<XMLReaderStreamDataProvider<XTGEventReader>>() {
			@Override
			public void readEvent(XMLReaderStreamDataProvider<XTGEventReader> streamDataProvider, XMLEvent event) throws Exception {
				streamDataProvider.getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(EventContentType.DOCUMENT, EventTopologyType.START));
				streamDataProvider.setFormat(XTG);
			}
		});
		
		putElementReader(new XMLElementReaderKey(null, null, XMLStreamConstants.END_DOCUMENT), new AbstractXMLElementReader<XMLReaderStreamDataProvider<XTGEventReader>>() {
			@Override
			public void readEvent(XMLReaderStreamDataProvider<XTGEventReader> streamDataProvider, XMLEvent event) throws Exception {
				streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.DOCUMENT));
			}
		});
		
		putElementReader(new XMLElementReaderKey(null, TAG_ROOT, XMLStreamConstants.START_ELEMENT), emptyReader);
		putElementReader(new XMLElementReaderKey(null, TAG_ROOT, XMLStreamConstants.END_ELEMENT), emptyReader);
		
		putElementReader(new XMLElementReaderKey(TAG_ROOT, TAG_TREE, XMLStreamConstants.START_ELEMENT), new AbstractXMLElementReader<XMLReaderStreamDataProvider<XTGEventReader>>() {			
			@Override
			public void readEvent(XMLReaderStreamDataProvider<XTGEventReader> streamDataProvider, XMLEvent event) throws Exception {
				String treeID = getID(null, EventContentType.TREE);							
				streamDataProvider.getCurrentEventCollection().add(new LinkedOTUOrOTUsEvent(EventContentType.TREE, treeID, null, null));		
			}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_ROOT, TAG_TREE, XMLStreamConstants.END_ELEMENT), new AbstractXMLElementReader<XMLReaderStreamDataProvider<XTGEventReader>>() {			
			@Override
			public void readEvent(XMLReaderStreamDataProvider<XTGEventReader> streamDataProvider, XMLEvent event) throws Exception {
				streamDataProvider.getSourceNode().clear();
				streamDataProvider.getEdgeInfos().clear();
				streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.TREE));
			}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_TREE, TAG_NODE, XMLStreamConstants.START_ELEMENT), nodeStartReader);
		putElementReader(new XMLElementReaderKey(TAG_TREE, TAG_NODE, XMLStreamConstants.END_ELEMENT), nodeEndReader);
		putElementReader(new XMLElementReaderKey(TAG_NODE, TAG_NODE, XMLStreamConstants.START_ELEMENT), nodeStartReader);
		putElementReader(new XMLElementReaderKey(TAG_NODE, TAG_NODE, XMLStreamConstants.END_ELEMENT), nodeEndReader);

		putElementReader(new XMLElementReaderKey(TAG_NODE, TAG_BRANCH, XMLStreamConstants.START_ELEMENT), new AbstractXMLElementReader<XMLReaderStreamDataProvider<XTGEventReader>>() {			
			@Override
			public void readEvent(XMLReaderStreamDataProvider<XTGEventReader> streamDataProvider, XMLEvent event) throws Exception {
				StartElement element = event.asStartElement();				
				double length = XMLUtils.readDoubleAttr(element, ATTR_BRANCH_LENGTH, Double.NaN);
				
				streamDataProvider.getEdgeInfos().peek().setLength(length);
				streamDataProvider.setCurrentEventCollection(new ArrayList<JPhyloIOEvent>());
				
				readAttributes(streamDataProvider, element);
			}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_NODE, TAG_BRANCH, XMLStreamConstants.END_ELEMENT), new AbstractXMLElementReader<XMLReaderStreamDataProvider<XTGEventReader>>() {			
			@Override
			public void readEvent(XMLReaderStreamDataProvider<XTGEventReader> streamDataProvider, XMLEvent event) throws Exception {
				Collection<JPhyloIOEvent> nestedEvents = streamDataProvider.resetCurrentEventCollection();
				NodeEdgeInfo info = streamDataProvider.getEdgeInfos().pop();
				
				streamDataProvider.setCurrentEventCollection(streamDataProvider.getSourceNode().peek().getNestedEvents());
				
				streamDataProvider.getCurrentEventCollection().add(new EdgeEvent(info.getID(), info.getLabel(), info.getSource(), 
						info.getTarget(), info.getLength()));
				for (JPhyloIOEvent nextEvent : nestedEvents) {
					streamDataProvider.getCurrentEventCollection().add(nextEvent);
				}
				streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.EDGE));				
			}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_BRANCH, TAG_TEXT_LABEL, XMLStreamConstants.START_ELEMENT), new AbstractXMLElementReader<XMLReaderStreamDataProvider<XTGEventReader>>() {			
			@Override
			public void readEvent(XMLReaderStreamDataProvider<XTGEventReader> streamDataProvider, XMLEvent event) throws Exception {
				StartElement element = event.asStartElement();
				String label = XMLUtils.readStringAttr(element, ATTR_TEXT, null);
				streamDataProvider.getEdgeInfos().peek().setLabel(label); //TODO which text is used as a label if more than one text labels exist?
				
				streamDataProvider.getCurrentEventCollection().add(new MetaInformationEvent(streamDataProvider.getFormat() + "." + streamDataProvider.getParentName() 
						+ "." + element.getName().getLocalPart(), null, null));
			}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_BRANCH, TAG_TEXT_LABEL, XMLStreamConstants.END_ELEMENT), createMetaEnd);		
		
		putElementReader(new XMLElementReaderKey(TAG_TREE, TAG_SCALE_BAR, XMLStreamConstants.START_ELEMENT), emptyReader);
		putElementReader(new XMLElementReaderKey(TAG_TREE, TAG_SCALE_BAR, XMLStreamConstants.END_ELEMENT), emptyReader);
		putElementReader(new XMLElementReaderKey(TAG_TREE, TAG_LEGEND, XMLStreamConstants.START_ELEMENT), emptyReader);
		putElementReader(new XMLElementReaderKey(TAG_TREE, TAG_LEGEND, XMLStreamConstants.END_ELEMENT), emptyReader);
		putElementReader(new XMLElementReaderKey(TAG_TREE, TAG_LEGEND_MARGIN, XMLStreamConstants.START_ELEMENT), emptyReader);
		putElementReader(new XMLElementReaderKey(TAG_TREE, TAG_LEGEND_MARGIN, XMLStreamConstants.END_ELEMENT), emptyReader);
		
		putElementReader(new XMLElementReaderKey(null, null, XMLStreamConstants.START_ELEMENT), new XMLToMetaElementStartReader());
		putElementReader(new XMLElementReaderKey(null, null, XMLStreamConstants.END_ELEMENT), createMetaEnd);
		putElementReader(new XMLElementReaderKey(null, null, XMLStreamConstants.COMMENT), new CommentElementReader());
	}
}
