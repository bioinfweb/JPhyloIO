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

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import info.bioinfweb.commons.io.XMLUtils;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.EdgeEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.LinkedOTUOrOTUsEvent;
import info.bioinfweb.jphyloio.events.MetaInformationEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.exception.JPhyloIOReaderException;
import info.bioinfweb.jphyloio.formats.JPhyloIOFormatIDs;
import info.bioinfweb.jphyloio.formats.NodeEdgeInfo;
import info.bioinfweb.jphyloio.formats.xml.AbstractXMLElementReader;
import info.bioinfweb.jphyloio.formats.xml.AbstractXMLEventReader;
import info.bioinfweb.jphyloio.formats.xml.CommentElementReader;
import info.bioinfweb.jphyloio.formats.xml.XMLToMetaElementStartReader;
import info.bioinfweb.jphyloio.formats.xml.XMLElementReader;
import info.bioinfweb.jphyloio.formats.xml.XMLElementReaderKey;



/**
 * Event reader for the <a href="http://phyloxml.org/"PhyloXML</a> format.
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
public class PhyloXMLEventReader extends AbstractXMLEventReader<PhyloXMLReaderStreamDataProvider> 
		implements PhyloXMLConstants {
	
	
	public PhyloXMLEventReader(File file, ReadWriteParameterMap parameters) throws IOException, XMLStreamException {
		super(file, parameters);
	}


	public PhyloXMLEventReader(InputStream stream, ReadWriteParameterMap parameters) throws IOException, XMLStreamException {
		super(stream, parameters);
	}


	public PhyloXMLEventReader(Reader reader, ReadWriteParameterMap parameters) throws IOException, XMLStreamException {
		super(reader, parameters);
	}


	public PhyloXMLEventReader(XMLEventReader xmlReader, ReadWriteParameterMap parameters) {
		super(xmlReader, parameters);
	}


	@Override
	public String getFormatID() {
		return JPhyloIOFormatIDs.PHYLOXML_FORMAT_ID;
	}


	@SuppressWarnings("unchecked")
	protected void fillMap() {
		XMLElementReader<PhyloXMLReaderStreamDataProvider> cladeEndReader = new AbstractXMLElementReader<PhyloXMLReaderStreamDataProvider>() {
				@Override
				public void readEvent(PhyloXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
					if (streamDataProvider.hasSpecialEventCollection()) {
						streamDataProvider.resetCurrentEventCollection();
					}
					NodeEdgeInfo nodeInfo = streamDataProvider.getSourceNode().pop();
					NodeEdgeInfo edgeInfo = streamDataProvider.getEdgeInfos().pop();
					
					String nodeID = getID(nodeInfo.getID(), EventContentType.NODE);
					nodeInfo.setID(nodeID);
					edgeInfo.setTarget(nodeID);					
					
					createNodeEvents(nodeInfo);
					createEdgeEvents(edgeInfo);		
				}
			};
			
		XMLElementReader<PhyloXMLReaderStreamDataProvider> branchMetaStartReader = new AbstractXMLElementReader<PhyloXMLReaderStreamDataProvider>() {
				@Override
				public void readEvent(PhyloXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
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
					streamDataProvider.setCurrentEventCollection(streamDataProvider.getEdgeInfos().peek().getNestedEvents());
					
					streamDataProvider.getCurrentEventCollection().add(new MetaInformationEvent(key, null, value));
					readAttributes(streamDataProvider, element);	
				}
			};
			
		XMLElementReader<PhyloXMLReaderStreamDataProvider> branchMetaEndReader = new AbstractXMLElementReader<PhyloXMLReaderStreamDataProvider>() {
			@Override
			public void readEvent(PhyloXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
				streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_INFORMATION));
				streamDataProvider.resetCurrentEventCollection();
			}
		};
		
		XMLElementReader<PhyloXMLReaderStreamDataProvider> emptyReader = new AbstractXMLElementReader<PhyloXMLReaderStreamDataProvider>() {			
			@Override
			public void readEvent(PhyloXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {}
		};
		
		putElementReader(new XMLElementReaderKey(null, null, XMLStreamConstants.START_DOCUMENT), 
			new AbstractXMLElementReader<PhyloXMLReaderStreamDataProvider>() {
				@Override
				public void readEvent(PhyloXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
					streamDataProvider.getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(EventContentType.DOCUMENT, EventTopologyType.START));
					streamDataProvider.setFormat(PHYLO_XML);
				}
		});
		
		putElementReader(new XMLElementReaderKey(null, null, XMLStreamConstants.END_DOCUMENT), 
			new AbstractXMLElementReader<PhyloXMLReaderStreamDataProvider>() {
				@Override
				public void readEvent(PhyloXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
					streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.DOCUMENT));
				}
		});
		
		putElementReader(new XMLElementReaderKey(null, TAG_ROOT, XMLStreamConstants.START_ELEMENT), emptyReader); //no meta events belonging to tag phyloXML are read
		
		putElementReader(new XMLElementReaderKey(null, TAG_ROOT, XMLStreamConstants.END_ELEMENT), emptyReader); //no meta events belonging to tag phyloXML are read
				
		putElementReader(new XMLElementReaderKey(TAG_ROOT, TAG_PHYLOGENY, XMLStreamConstants.START_ELEMENT), 
			new AbstractXMLElementReader<PhyloXMLReaderStreamDataProvider>() {
				@Override
				public void readEvent(PhyloXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
					StartElement element = event.asStartElement();					
					boolean rooted = XMLUtils.readBooleanAttr(event.asStartElement(), ATTR_ROOTED, false);					
					
					streamDataProvider.setRooted(rooted);
					streamDataProvider.setCurrentEventCollection(new ArrayList<JPhyloIOEvent>());
					readAttributes(streamDataProvider, element); //TODO do not read root tag again here
				}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_ROOT, TAG_PHYLOGENY, XMLStreamConstants.END_ELEMENT), 
			new AbstractXMLElementReader<PhyloXMLReaderStreamDataProvider>() {
				@Override
				public void readEvent(PhyloXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
					streamDataProvider.getSourceNode().clear();
					streamDataProvider.getEdgeInfos().clear();
					streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.TREE));			
				}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_CLADE, XMLStreamConstants.START_ELEMENT),
			new AbstractXMLElementReader<PhyloXMLReaderStreamDataProvider>() {
				@Override
				public void readEvent(PhyloXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {					
					String treeID = getID(null, EventContentType.TREE);
					String treeLabel = streamDataProvider.getTreeLabel();					
					Collection<JPhyloIOEvent> nestedEvents = streamDataProvider.resetCurrentEventCollection();					
					
					streamDataProvider.getCurrentEventCollection().add(new LinkedOTUOrOTUsEvent(EventContentType.TREE, treeID, treeLabel, null));
					
					streamDataProvider.getCurrentEventCollection().add(new MetaInformationEvent(META_KEY_DISPLAY_TREE_ROOTED, "boolean", 
							Boolean.toString(streamDataProvider.isRooted()), streamDataProvider.isRooted()));					
					streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_INFORMATION));
					
					for (JPhyloIOEvent nextEvent : nestedEvents) {
						streamDataProvider.getCurrentEventCollection().add(nextEvent);
					}
					
					double branchLength = XMLUtils.readDoubleAttr(event.asStartElement(), TAG_BRANCH_LENGTH, Double.NaN);	
					streamDataProvider.getSourceNode().add(new NodeEdgeInfo(null, Double.NaN, new ArrayList<JPhyloIOEvent>()));
					streamDataProvider.getEdgeInfos().add(new NodeEdgeInfo(null, branchLength, new ArrayList<JPhyloIOEvent>()));
					streamDataProvider.getEdgeInfos().peek().setSource(null);
					
					streamDataProvider.setCurrentEventCollection(streamDataProvider.getSourceNode().peek().getNestedEvents());	
				}
			});
		
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_CLADE, XMLStreamConstants.START_ELEMENT),
				new AbstractXMLElementReader<PhyloXMLReaderStreamDataProvider>() {
					@Override
					public void readEvent(PhyloXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
						String parentID = streamDataProvider.getSourceNode().peek().getID();
						if (streamDataProvider.hasSpecialEventCollection()) {
							streamDataProvider.resetCurrentEventCollection();
						}						
						
						double branchLength = XMLUtils.readDoubleAttr(event.asStartElement(), TAG_BRANCH_LENGTH, Double.NaN);	
						streamDataProvider.getSourceNode().add(new NodeEdgeInfo(null, Double.NaN, new ArrayList<JPhyloIOEvent>()));
						streamDataProvider.getEdgeInfos().add(new NodeEdgeInfo(null, branchLength, new ArrayList<JPhyloIOEvent>()));
						streamDataProvider.getEdgeInfos().peek().setSource(parentID);
						
						streamDataProvider.setCurrentEventCollection(streamDataProvider.getSourceNode().peek().getNestedEvents());									
					}
				});
		
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_CONFIDENCE, XMLStreamConstants.START_ELEMENT), branchMetaStartReader);
			
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_CONFIDENCE, XMLStreamConstants.END_ELEMENT), branchMetaEndReader);
 
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_BRANCH_WIDTH, XMLStreamConstants.START_ELEMENT), branchMetaStartReader);
		
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_BRANCH_WIDTH, XMLStreamConstants.END_ELEMENT), branchMetaEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_BRANCH_COLOR, XMLStreamConstants.START_ELEMENT), branchMetaStartReader);
		
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_BRANCH_COLOR, XMLStreamConstants.END_ELEMENT), branchMetaEndReader);
		
		putElementReader(new XMLElementReaderKey(null, null, XMLStreamConstants.CHARACTERS), 
			new AbstractXMLElementReader<PhyloXMLReaderStreamDataProvider>() {			
				@Override
				public void readEvent(PhyloXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {					
					String value = event.asCharacters().getData();
					
					if (!value.matches("\\s+")) { //space characters after tag ends are excluded this way					
						String parentName = streamDataProvider.getParentName();
						String elementName = streamDataProvider.getElementName();							
						
						if (parentName.equals(TAG_PHYLOGENY.getLocalPart())) {
							if (elementName.equals(TAG_NAME.getLocalPart())) {
								streamDataProvider.setTreeLabel(value);
							}							
						}						
						else if (!streamDataProvider.getSourceNode().isEmpty() && !streamDataProvider.getEdgeInfos().isEmpty()) {
							NodeEdgeInfo currentNode = streamDataProvider.getSourceNode().peek();							
							String currentNodeLabel = currentNode.getLabel();							
							
							NodeEdgeInfo currentEdge = streamDataProvider.getEdgeInfos().peek();
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
							}
							else if (parentName.equals(TAG_TAXONOMY.getLocalPart())) {
								if (elementName.equals(TAG_SCI_NAME.getLocalPart()) && (currentNodeLabel == null)) {								
									currentNodeLabel = value;
								}
								else if (elementName.equals(TAG_COMMON_NAME.getLocalPart()) && (currentNodeLabel == null)) {								
									currentNodeLabel = value;
								}								
							}							
							else if (parentName.equals(TAG_SEQUENCE.getLocalPart())) {
								if (elementName.equals(TAG_NAME.getLocalPart()) && (currentNodeLabel == null)) {									
									currentNodeLabel = value;																	
								}
							}						
							
							currentNode.setLabel(currentNodeLabel);							
							currentEdge.setLength(currentEdgeLength);							
						}
					}
				}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_CLADE, XMLStreamConstants.END_ELEMENT), cladeEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_CLADE, XMLStreamConstants.END_ELEMENT), cladeEndReader);
		
		putElementReader(new XMLElementReaderKey(null, null, XMLStreamConstants.START_ELEMENT), new XMLToMetaElementStartReader());
		
		putElementReader(new XMLElementReaderKey(null, null, XMLStreamConstants.END_ELEMENT), 
				new AbstractXMLElementReader<PhyloXMLReaderStreamDataProvider>() {
					@Override
					public void readEvent(PhyloXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {						
						streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_INFORMATION));			
					}
			});
		
		putElementReader(new XMLElementReaderKey(null, null, XMLStreamConstants.COMMENT), new CommentElementReader());
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
			info.setSource(TAG_PARENT_OF_ROOT.getLocalPart());	
		}		
		
		getStreamDataProvider().getCurrentEventCollection().add(new EdgeEvent(getID(info.getID(), EventContentType.EDGE), info.getLabel(), info.getSource(), 
				info.getTarget(), info.getLength()));
		for (JPhyloIOEvent nextEvent : info.getNestedEvents()) {
			getStreamDataProvider().getCurrentEventCollection().add(nextEvent);
		}
		getStreamDataProvider().getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.EDGE));		
	}
	
	
	@Override
	protected PhyloXMLReaderStreamDataProvider createStreamDataProvider() {
		return new PhyloXMLReaderStreamDataProvider(this);
	}
}
