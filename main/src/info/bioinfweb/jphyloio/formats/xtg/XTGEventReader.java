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
import info.bioinfweb.jphyloio.events.LinkedLabeledIDEvent;
import info.bioinfweb.jphyloio.events.NodeEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.formats.JPhyloIOFormatIDs;
import info.bioinfweb.jphyloio.formats.NodeEdgeInfo;
import info.bioinfweb.jphyloio.formats.xml.AbstractXMLEventReader;
import info.bioinfweb.jphyloio.formats.xml.XMLReaderStreamDataProvider;
import info.bioinfweb.jphyloio.formats.xml.elementreaders.AbstractXMLElementReader;
import info.bioinfweb.jphyloio.formats.xml.elementreaders.CommentElementReader;
import info.bioinfweb.jphyloio.formats.xml.elementreaders.XMLElementReaderKey;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;

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
			public void readEvent(XMLReaderStreamDataProvider<XTGEventReader> streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				StartElement element = event.asStartElement();				
				String id = DEFAULT_NODE_ID_PREFIX + streamDataProvider.getIDManager().createNewID();
				String label = XMLUtils.readStringAttr(element, ATTR_TEXT, null);
				
				createNodeEvents(streamDataProvider);  // Create node events for previous node
				
				// Add node info for this node
				NodeEdgeInfo nodeInfo = new NodeEdgeInfo(id, Double.NaN, new ArrayList<JPhyloIOEvent>(), new ArrayList<JPhyloIOEvent>());
				nodeInfo.setLabel(label); //TODO add rooted information
				streamDataProvider.getSourceNode().add(nodeInfo);
				streamDataProvider.setCreateNodeStart(true);
				
				// Add edge info for this level
				streamDataProvider.getEdgeInfos().add(new ArrayDeque<NodeEdgeInfo>());
				
				streamDataProvider.setCurrentEventCollection(streamDataProvider.getSourceNode().peek().getNestedNodeEvents());
				
				readAttributes(streamDataProvider, element, null); //TODO read node attributes
			}
		};
		
		AbstractXMLElementReader<XMLReaderStreamDataProvider<XTGEventReader>> nodeEndReader = new AbstractXMLElementReader<XMLReaderStreamDataProvider<XTGEventReader>>() {			
			@Override
			public void readEvent(XMLReaderStreamDataProvider<XTGEventReader> streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				createNodeEvents(streamDataProvider);
				streamDataProvider.setCreateNodeStart(false);
				
				createEdgeEvents(streamDataProvider);
				
				streamDataProvider.getSourceNode().pop();
			}
		};
		
		AbstractXMLElementReader<XMLReaderStreamDataProvider<XTGEventReader>> emptyReader = new AbstractXMLElementReader<XMLReaderStreamDataProvider<XTGEventReader>>() {			
			@Override
			public void readEvent(XMLReaderStreamDataProvider<XTGEventReader> streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {}
		};
		
		putElementReader(new XMLElementReaderKey(null, null, XMLStreamConstants.START_DOCUMENT), new AbstractXMLElementReader<XMLReaderStreamDataProvider<XTGEventReader>>() {
			@Override
			public void readEvent(XMLReaderStreamDataProvider<XTGEventReader> streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				streamDataProvider.getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(EventContentType.DOCUMENT, EventTopologyType.START));
			}
		});
		
		putElementReader(new XMLElementReaderKey(null, null, XMLStreamConstants.END_DOCUMENT), new AbstractXMLElementReader<XMLReaderStreamDataProvider<XTGEventReader>>() {
			@Override
			public void readEvent(XMLReaderStreamDataProvider<XTGEventReader> streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.DOCUMENT));
			}
		});
		
		putElementReader(new XMLElementReaderKey(null, TAG_ROOT, XMLStreamConstants.START_ELEMENT), emptyReader);
		putElementReader(new XMLElementReaderKey(null, TAG_ROOT, XMLStreamConstants.END_ELEMENT), emptyReader);
		
		putElementReader(new XMLElementReaderKey(TAG_ROOT, TAG_TREE, XMLStreamConstants.START_ELEMENT), new AbstractXMLElementReader<XMLReaderStreamDataProvider<XTGEventReader>>() {			
			@Override
			public void readEvent(XMLReaderStreamDataProvider<XTGEventReader> streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				String treeID = DEFAULT_TREE_ID_PREFIX + streamDataProvider.getIDManager().createNewID();
				
				streamDataProvider.getEdgeInfos().add(new ArrayDeque<NodeEdgeInfo>());
				streamDataProvider.setCreateNodeStart(false); // Do not create node events at start of root node
				streamDataProvider.getCurrentEventCollection().add(new LinkedLabeledIDEvent(EventContentType.TREE, treeID, null, null));	
			}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_ROOT, TAG_TREE, XMLStreamConstants.END_ELEMENT), new AbstractXMLElementReader<XMLReaderStreamDataProvider<XTGEventReader>>() {			
			@Override
			public void readEvent(XMLReaderStreamDataProvider<XTGEventReader> streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				createEdgeEvents(streamDataProvider);
				
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
			public void readEvent(XMLReaderStreamDataProvider<XTGEventReader> streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				StartElement element = event.asStartElement();	
				
				streamDataProvider.getSourceNode().peek().setLength(XMLUtils.readDoubleAttr(element, ATTR_BRANCH_LENGTH, Double.NaN));
				streamDataProvider.setCurrentEventCollection(streamDataProvider.getSourceNode().peek().getNestedEdgeEvents());
				
				readAttributes(streamDataProvider, element, ""); //TODO read branch attributes
			}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_NODE, TAG_BRANCH, XMLStreamConstants.END_ELEMENT), new AbstractXMLElementReader<XMLReaderStreamDataProvider<XTGEventReader>>() {			
			@Override
			public void readEvent(XMLReaderStreamDataProvider<XTGEventReader> streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {				
				if (streamDataProvider.hasSpecialEventCollection()) {
					streamDataProvider.resetCurrentEventCollection();
				}
			}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_BRANCH, TAG_TEXT_LABEL, XMLStreamConstants.START_ELEMENT), new AbstractXMLElementReader<XMLReaderStreamDataProvider<XTGEventReader>>() {			
			@Override
			public void readEvent(XMLReaderStreamDataProvider<XTGEventReader> streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				StartElement element = event.asStartElement();
				String label = XMLUtils.readStringAttr(element, ATTR_TEXT, null); //TODO maybe use as branch label?
			}
		});
		
//		putElementReader(new XMLElementReaderKey(TAG_BRANCH, TAG_TEXT_LABEL, XMLStreamConstants.END_ELEMENT), createMetaEnd);		
		
		putElementReader(new XMLElementReaderKey(TAG_TREE, TAG_SCALE_BAR, XMLStreamConstants.START_ELEMENT), emptyReader);
		putElementReader(new XMLElementReaderKey(TAG_TREE, TAG_SCALE_BAR, XMLStreamConstants.END_ELEMENT), emptyReader);
		putElementReader(new XMLElementReaderKey(TAG_TREE, TAG_LEGEND, XMLStreamConstants.START_ELEMENT), emptyReader);
		putElementReader(new XMLElementReaderKey(TAG_TREE, TAG_LEGEND, XMLStreamConstants.END_ELEMENT), emptyReader);
		putElementReader(new XMLElementReaderKey(TAG_TREE, TAG_LEGEND_MARGIN, XMLStreamConstants.START_ELEMENT), emptyReader);
		putElementReader(new XMLElementReaderKey(TAG_TREE, TAG_LEGEND_MARGIN, XMLStreamConstants.END_ELEMENT), emptyReader);
		
//		putElementReader(new XMLElementReaderKey(null, null, XMLStreamConstants.START_ELEMENT), new XMLToMetaElementStartReader());
//		putElementReader(new XMLElementReaderKey(null, null, XMLStreamConstants.END_ELEMENT), createMetaEnd);
		putElementReader(new XMLElementReaderKey(null, null, XMLStreamConstants.COMMENT), new CommentElementReader());
	}
	
	
	private void createNodeEvents(XMLReaderStreamDataProvider<XTGEventReader> streamDataProvider) {
		if (streamDataProvider.hasSpecialEventCollection()) {
			streamDataProvider.resetCurrentEventCollection();
		}
		
		if (streamDataProvider.isCreateNodeStart()) {
			NodeEdgeInfo nodeInfo = streamDataProvider.getSourceNode().peek();
			
			getStreamDataProvider().getCurrentEventCollection().add(new NodeEvent(nodeInfo.getID(), nodeInfo.getLabel(), null, nodeInfo.isRoot()));
			for (JPhyloIOEvent nextEvent : nodeInfo.getNestedNodeEvents()) {
				getStreamDataProvider().getCurrentEventCollection().add(nextEvent);  // Might lead to an exception, if nodeInfo.getNestedEvents() is the currentEventCollection at the time this method is called
			}
			
			streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.NODE));
		}
	}
	
	
	private void createEdgeEvents(XMLReaderStreamDataProvider<XTGEventReader> streamDataProvider) { //TODO move to superclass?
		Queue<NodeEdgeInfo> edgeInfos = streamDataProvider.getEdgeInfos().pop(); // All edges leading to children of this node
		String sourceID = null;
		NodeEdgeInfo edgeInfo;
		
		if (!streamDataProvider.getEdgeInfos().isEmpty()) {
			streamDataProvider.getEdgeInfos().peek().add(streamDataProvider.getSourceNode().peek()); // Add info for this node to top level queue
		}
		
		if (!streamDataProvider.getSourceNode().isEmpty()) {
			sourceID = streamDataProvider.getSourceNode().peek().getID();
		}
		
		while (!edgeInfos.isEmpty()) {
			edgeInfo = edgeInfos.poll();
			
			if (!((sourceID == null) && Double.isNaN(edgeInfo.getLength()) && edgeInfo.getNestedEdgeEvents().isEmpty())) { // Do not add root edge if no information about it is present
				getStreamDataProvider().getCurrentEventCollection().add(new EdgeEvent(DEFAULT_EDGE_ID_PREFIX + streamDataProvider.getIDManager().createNewID(), null, 
						sourceID, edgeInfo.getID(), edgeInfo.getLength()));
				
				for (JPhyloIOEvent nextEvent : edgeInfo.getNestedEdgeEvents()) {
					getStreamDataProvider().getCurrentEventCollection().add(nextEvent);
				}
				
				streamDataProvider.getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(
						sourceID == null ? EventContentType.ROOT_EDGE : EventContentType.EDGE, EventTopologyType.END));
			}
		}
	}
}
