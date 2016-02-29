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
package info.bioinfweb.jphyloio.formats.phyloxml;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Stack;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import info.bioinfweb.commons.io.XMLUtils;
import info.bioinfweb.jphyloio.JPhyloIOReaderException;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.EdgeEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.LinkedOTUOrOTUsEvent;
import info.bioinfweb.jphyloio.events.MetaInformationEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.formats.NodeEdgeInfo;
import info.bioinfweb.jphyloio.formats.xml.AbstractXMLElementReader;
import info.bioinfweb.jphyloio.formats.xml.AbstractXMLEventReader;
import info.bioinfweb.jphyloio.formats.xml.CommentElementReader;
import info.bioinfweb.jphyloio.formats.xml.XMLToMetaElementStartReader;
import info.bioinfweb.jphyloio.formats.xml.XMLElementReader;
import info.bioinfweb.jphyloio.formats.xml.XMLElementReaderKey;



public class PhyloXMLEventReader extends AbstractXMLEventReader<PhyloXMLStreamDataProvider> 
		implements PhyloXMLConstants {
	
	
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
	
	
	@SuppressWarnings("unchecked")
	protected void fillMap() {
		Map<XMLElementReaderKey, XMLElementReader<PhyloXMLStreamDataProvider>> map = getElementReaderMap();
		
		XMLElementReader<PhyloXMLStreamDataProvider> cladeEndReader = new AbstractXMLElementReader<PhyloXMLStreamDataProvider>() {
				@Override
				public void readEvent(PhyloXMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
					if (streamDataProvider.hasSpecialEventCollection()) {
						streamDataProvider.resetCurrentEventCollection();
					}
					
					createNodeEvents(streamDataProvider.getSourceNode().pop());
					createEdgeEvents(streamDataProvider.getEdges().pop());					
				}
			};
			
		XMLElementReader<PhyloXMLStreamDataProvider> branchMetaStartReader = new AbstractXMLElementReader<PhyloXMLStreamDataProvider>() {
				@Override
				public void readEvent(PhyloXMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
					StartElement element = event.asStartElement();						
					XMLEvent nextEvent = streamDataProvider.getXMLReader().peek();
					String key = streamDataProvider.getFormat() + "." + streamDataProvider.getParentName() + "." + element.getName();
					String value = null;
					
					if (nextEvent.getEventType() == XMLStreamConstants.CHARACTERS) {
						String characterData = nextEvent.asCharacters().getData();
						if (!characterData.matches("\\s+")) {
							value = characterData;
						}
					}
					streamDataProvider.setCurrentEventCollection(streamDataProvider.getEdges().peek().getNestedEvents());
					
					streamDataProvider.getCurrentEventCollection().add(new MetaInformationEvent(key, null, value));
					readAttributes(streamDataProvider, element);	
				}
			};
			
		XMLElementReader<PhyloXMLStreamDataProvider> branchMetaEndReader = new AbstractXMLElementReader<PhyloXMLStreamDataProvider>() {
			@Override
			public void readEvent(PhyloXMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
				streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_INFORMATION));
				streamDataProvider.resetCurrentEventCollection();
			}
		};
		
		XMLElementReader<PhyloXMLStreamDataProvider> emptyReader = new AbstractXMLElementReader<PhyloXMLStreamDataProvider>() {			
			@Override
			public void readEvent(PhyloXMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {}
		};
		
		map.put(new XMLElementReaderKey(null, null, XMLStreamConstants.START_DOCUMENT), 
			new AbstractXMLElementReader<PhyloXMLStreamDataProvider>() {
				@Override
				public void readEvent(PhyloXMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
					streamDataProvider.getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(EventContentType.DOCUMENT, EventTopologyType.START));
					streamDataProvider.setFormat(PHYLO_XML);
				}
		});
		
		map.put(new XMLElementReaderKey(null, null, XMLStreamConstants.END_DOCUMENT), 
			new AbstractXMLElementReader<PhyloXMLStreamDataProvider>() {
				@Override
				public void readEvent(PhyloXMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
					streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.DOCUMENT));
				}
		});
		
		map.put(new XMLElementReaderKey(null, TAG_PHYLOXML, XMLStreamConstants.START_ELEMENT), emptyReader); //no meta events belonging to tag phyloXML are read
		
		map.put(new XMLElementReaderKey(null, TAG_PHYLOXML, XMLStreamConstants.END_ELEMENT), emptyReader); //no meta events belonging to tag phyloXML are read
				
		map.put(new XMLElementReaderKey(TAG_PHYLOXML, TAG_PHYLOGENY, XMLStreamConstants.START_ELEMENT), 
			new AbstractXMLElementReader<PhyloXMLStreamDataProvider>() {
				@Override
				public void readEvent(PhyloXMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
					StartElement element = event.asStartElement();					
					boolean rooted = XMLUtils.readBooleanAttr(event.asStartElement(), ATTR_ROOTED, false);
					streamDataProvider.getCurrentEventCollection().add(new MetaInformationEvent(META_KEY_DISPLAY_TREE_ROOTED, null, Boolean.toString(rooted)));
					streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_INFORMATION));
					
					streamDataProvider.setTreeInfo(new NodeEdgeInfo("", Double.NaN, null));
					streamDataProvider.setSourceNode(new Stack<NodeEdgeInfo>());
					streamDataProvider.setEdges(new Stack<NodeEdgeInfo>());
					streamDataProvider.setCurrentEventCollection(new ArrayList<JPhyloIOEvent>());
					readAttributes(streamDataProvider, element); //TODO do not read root tag
				}
		});
		
		map.put(new XMLElementReaderKey(TAG_PHYLOXML, TAG_PHYLOGENY, XMLStreamConstants.END_ELEMENT), 
			new AbstractXMLElementReader<PhyloXMLStreamDataProvider>() {
				@Override
				public void readEvent(PhyloXMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
					streamDataProvider.getSourceNode().clear();
					streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.TREE));			
				}
		});
		
		map.put(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_CLADE, XMLStreamConstants.START_ELEMENT),
			new AbstractXMLElementReader<PhyloXMLStreamDataProvider>() {
				@Override
				public void readEvent(PhyloXMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {					
					String treeID = getID(streamDataProvider.getTreeInfo().getID(), EventContentType.TREE);
					String treeLabel = streamDataProvider.getTreeInfo().getLabel();					
					Collection<JPhyloIOEvent> nestedEvents = streamDataProvider.resetCurrentEventCollection();					
					
					streamDataProvider.getCurrentEventCollection().add(new LinkedOTUOrOTUsEvent(EventContentType.TREE, treeID, treeLabel, null));		
					for (JPhyloIOEvent nextEvent : nestedEvents) {
						streamDataProvider.getCurrentEventCollection().add(nextEvent);
					}
					
					
					streamDataProvider.getSourceNode().add(new NodeEdgeInfo(null, Double.NaN, new ArrayList<JPhyloIOEvent>()));
					streamDataProvider.setCurrentEventCollection(streamDataProvider.getSourceNode().peek().getNestedEvents());
					
					double branchLength = XMLUtils.readDoubleAttr(event.asStartElement(), TAG_BRANCH_LENGTH, Double.NaN);		
					streamDataProvider.getEdges().add(new NodeEdgeInfo(null, branchLength, new ArrayList<JPhyloIOEvent>()));
					streamDataProvider.getEdges().peek().setSource(null);
				}
			});
		
		map.put(new XMLElementReaderKey(TAG_CLADE, TAG_CLADE, XMLStreamConstants.START_ELEMENT),
				new AbstractXMLElementReader<PhyloXMLStreamDataProvider>() {
					@Override
					public void readEvent(PhyloXMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
						String parentID = streamDataProvider.getSourceNode().peek().getID();
						if (streamDataProvider.hasSpecialEventCollection()) {
							streamDataProvider.resetCurrentEventCollection();
						}						
						
						streamDataProvider.getSourceNode().add(new NodeEdgeInfo(null, Double.NaN, new ArrayList<JPhyloIOEvent>()));
						streamDataProvider.setCurrentEventCollection(streamDataProvider.getSourceNode().peek().getNestedEvents());
						
						double branchLength = XMLUtils.readDoubleAttr(event.asStartElement(), TAG_BRANCH_LENGTH, Double.NaN);		
						streamDataProvider.getEdges().add(new NodeEdgeInfo(null, branchLength, new ArrayList<JPhyloIOEvent>()));
						streamDataProvider.getEdges().peek().setSource(parentID);				
					}
				});
		
		map.put(new XMLElementReaderKey(TAG_CLADE, TAG_CONFIDENCE, XMLStreamConstants.START_ELEMENT), branchMetaStartReader);
			
		map.put(new XMLElementReaderKey(TAG_CLADE, TAG_CONFIDENCE, XMLStreamConstants.END_ELEMENT), branchMetaEndReader);
 
		map.put(new XMLElementReaderKey(TAG_CLADE, TAG_BRANCH_WIDTH, XMLStreamConstants.START_ELEMENT), branchMetaStartReader);
		
		map.put(new XMLElementReaderKey(TAG_CLADE, TAG_BRANCH_WIDTH, XMLStreamConstants.END_ELEMENT), branchMetaEndReader);
		
		map.put(new XMLElementReaderKey(TAG_CLADE, TAG_BRANCH_COLOR, XMLStreamConstants.START_ELEMENT), branchMetaStartReader);
		
		map.put(new XMLElementReaderKey(TAG_CLADE, TAG_BRANCH_COLOR, XMLStreamConstants.END_ELEMENT), branchMetaEndReader);
		
		map.put(new XMLElementReaderKey(null, null, XMLStreamConstants.CHARACTERS), 
			new AbstractXMLElementReader<PhyloXMLStreamDataProvider>() {			
				@Override
				public void readEvent(PhyloXMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {					
					String value = event.asCharacters().getData();
					
					if (!value.matches("\\s+")) { //space characters after tag ends are excluded this way					
						String parentName = streamDataProvider.getParentName();
						String elementName = streamDataProvider.getElementName();							
						
						if (parentName.equals(TAG_PHYLOGENY.getLocalPart())) {
							if (elementName.equals(TAG_NAME.getLocalPart())) {
								streamDataProvider.getTreeInfo().setLabel(value);
							}
							else if (elementName.equals(TAG_ID.getLocalPart())) {
								streamDataProvider.getTreeInfo().setID(value);
							}
						}
						
						else if (!streamDataProvider.getSourceNode().isEmpty() && !streamDataProvider.getEdges().isEmpty()) {
							NodeEdgeInfo currentNode = streamDataProvider.getSourceNode().peek();							
							String currentNodeLabel = currentNode.getLabel();
							String currentNodeID = currentNode.getID();
							
							NodeEdgeInfo currentEdge = streamDataProvider.getEdges().peek();
							double currentEdgeLength = currentEdge.getLength();
							
							if (parentName.equals(TAG_CLADE.getLocalPart())) {
								if (elementName.equals(TAG_NAME.getLocalPart())) {								
									currentNodeLabel = value;
								}
								else if (elementName.equals(TAG_BRANCH_LENGTH.getLocalPart()) && (currentEdgeLength == Double.NaN)) {
									try {
										currentEdgeLength = Double.parseDouble(value);
									}
									catch (NumberFormatException e) {
										throw new JPhyloIOReaderException("The branch length must be of type double.", event.getLocation());
									}
								}
								else if (elementName.equals(TAG_NODE_ID.getLocalPart())) {								
									currentNodeID = value;
								}
							}
							else if (parentName.equals(TAG_TAXONOMY.getLocalPart())) {
								if (elementName.equals(TAG_SCI_NAME.getLocalPart()) && (currentNodeLabel == null)) {								
									currentNodeLabel = value;
								}
								else if (elementName.equals(TAG_COMMON_NAME.getLocalPart()) && (currentNodeLabel == null)) {								
									currentNodeLabel = value;
								}
								else if (elementName.equals(TAG_ID.getLocalPart()) && (currentNodeID == null)) {								
									currentNodeID = value;
								}
							}							
							else if (parentName.equals(TAG_SEQUENCE.getLocalPart())) {
								if (elementName.equals(TAG_NAME.getLocalPart()) && (currentNodeLabel == null)) {									
									currentNodeLabel = value;																	
								}
							}
							
							currentNodeID = getID(currentNodeID, EventContentType.NODE);
							
							currentNode.setLabel(currentNodeLabel);
							currentNode.setID(currentNodeID);
							
							currentEdge.setTarget(currentNodeID);
							currentEdge.setLength(currentEdgeLength);						
						}
					}
				}
		});
		
		map.put(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_CLADE, XMLStreamConstants.END_ELEMENT), cladeEndReader);
		
		map.put(new XMLElementReaderKey(TAG_CLADE, TAG_CLADE, XMLStreamConstants.END_ELEMENT), cladeEndReader);
		
		map.put(new XMLElementReaderKey(null, null, XMLStreamConstants.START_ELEMENT), new XMLToMetaElementStartReader());
		
		map.put(new XMLElementReaderKey(null, null, XMLStreamConstants.END_ELEMENT), 
				new AbstractXMLElementReader<PhyloXMLStreamDataProvider>() {
					@Override
					public void readEvent(PhyloXMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {						
						streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_INFORMATION));			
					}
			});
		
		map.put(new XMLElementReaderKey(null, null, XMLStreamConstants.COMMENT), new CommentElementReader());
	}
	
	
	private void createNodeEvents(NodeEdgeInfo info) {
		getStreamDataProvider().getCurrentEventCollection().add(new LinkedOTUOrOTUsEvent(EventContentType.NODE, info.getID(), info.getLabel(), null));
		for (JPhyloIOEvent nextEvent : info.getNestedEvents()) {
			getStreamDataProvider().getCurrentEventCollection().add(nextEvent);
		}							
		getStreamDataProvider().getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.NODE));
	}
	
	
	private void createEdgeEvents(NodeEdgeInfo info) {				
		if (info.getSource() == null) {
			info.setSource(TAG_ROOT.getLocalPart());	
		}		
		
		getStreamDataProvider().getCurrentEventCollection().add(new EdgeEvent(getID(null, EventContentType.EDGE), null, info.getSource(), info.getTarget(), info.getLength()));
		for (JPhyloIOEvent nextEvent : info.getNestedEvents()) {
			getStreamDataProvider().getCurrentEventCollection().add(nextEvent);
		}
		getStreamDataProvider().getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.EDGE));		
	}
	
	
	@Override
	protected PhyloXMLStreamDataProvider createStreamDataProvider() {
		return new PhyloXMLStreamDataProvider(this);
	}
}
