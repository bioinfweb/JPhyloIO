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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.events.Attribute;

import info.bioinfweb.commons.io.XMLUtils;
import info.bioinfweb.jphyloio.JPhyloIOReaderException;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.MetaInformationEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.formats.NodeInfo;
import info.bioinfweb.jphyloio.formats.xml.AbstractXMLEventReader;
import info.bioinfweb.jphyloio.formats.xml.CommentElementReader;
import info.bioinfweb.jphyloio.formats.xml.XMLElementReader;
import info.bioinfweb.jphyloio.formats.xml.XMLElementReaderKey;
import info.bioinfweb.jphyloio.formats.xml.XMLStreamDataProvider;



public class PhyloXMLEventReader extends AbstractXMLEventReader<XMLStreamDataProvider<PhyloXMLEventReader>> 
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
		Map<XMLElementReaderKey, XMLElementReader<XMLStreamDataProvider<PhyloXMLEventReader>>> map = getElementReaderMap();
		
		XMLElementReader<XMLStreamDataProvider<PhyloXMLEventReader>> cladeEndReader = 
			new XMLElementReader<XMLStreamDataProvider<PhyloXMLEventReader>>() {
				@Override
				public void readEvent(XMLStreamDataProvider<PhyloXMLEventReader> streamDataProvider, XMLEvent event) throws Exception {
					System.out.println(streamDataProvider.getCurrentEventCollection().size());
					Collection<JPhyloIOEvent> nestedEvents = streamDataProvider.resetCurrentEventCollection();
					
//					Queue<NodeInfo> currentCollection = streamDataProvider.getPassedSubnodes().pop();		
////					Collection<JPhyloIOEvent> nestedEvents = new ArrayList<JPhyloIOEvent>();
//					
//					if (currentCollection.isEmpty()) {
//						streamDataProvider.getPassedSubnodes().peek().add(streamDataProvider.getCurrentNodeInfo());
//						streamDataProvider.setCurrentNodeInfo(null);
//					}
								
//					System.out.println("Child nodes: " + currentCollection.size());
					
//					for (NodeInfo info : currentCollection) {						
//						String id = info.getID();
//						if (id.equals("") || (id == null)) {
//							id = DEFAULT_NODE_ID_PREFIX + streamDataProvider.getIDManager().createNewID();
//						}
////						System.out.println("Child ID:" + id);
						
//						streamDataProvider.getCurrentEventCollection().add(new LabeledIDEvent(EventContentType.NODE, info.getID(), info.getLabel()));#
						streamDataProvider.getCurrentEventCollection().add(new LabeledIDEvent(EventContentType.NODE, "id", "label"));
						
//						System.out.println("Nested number: " + nestedEvents.size());
						for (JPhyloIOEvent nextEvent : nestedEvents) {
							streamDataProvider.getCurrentEventCollection().add(nextEvent);
						}						
						streamDataProvider.getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(EventContentType.NODE, EventTopologyType.END));
//					}
					//TODO create according edge events
				}
			};
		
		map.put(new XMLElementReaderKey(null, null, XMLStreamConstants.START_DOCUMENT), 
			new XMLElementReader<XMLStreamDataProvider<PhyloXMLEventReader>>() {
				@Override
				public void readEvent(XMLStreamDataProvider<PhyloXMLEventReader> streamDataProvider, XMLEvent event) throws Exception {
					streamDataProvider.getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(EventContentType.DOCUMENT, EventTopologyType.START));
				}
		});
		
		map.put(new XMLElementReaderKey(null, null, XMLStreamConstants.END_DOCUMENT), 
			new XMLElementReader<XMLStreamDataProvider<PhyloXMLEventReader>>() {
				@Override
				public void readEvent(XMLStreamDataProvider<PhyloXMLEventReader> streamDataProvider, XMLEvent event) throws Exception {
					streamDataProvider.getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(EventContentType.DOCUMENT, EventTopologyType.END));
				}
		});
		
		map.put(new XMLElementReaderKey(null, TAG_PHYLOXML, XMLStreamConstants.START_ELEMENT), 
				new XMLElementReader<XMLStreamDataProvider<PhyloXMLEventReader>>() {
					@Override
					public void readEvent(XMLStreamDataProvider<PhyloXMLEventReader> streamDataProvider, XMLEvent event) throws Exception {} //no meta events belonging to tag phyloxml are read
			});
		
		map.put(new XMLElementReaderKey(null, TAG_PHYLOXML, XMLStreamConstants.END_ELEMENT), 
				new XMLElementReader<XMLStreamDataProvider<PhyloXMLEventReader>>() {
					@Override
					public void readEvent(XMLStreamDataProvider<PhyloXMLEventReader> streamDataProvider, XMLEvent event) throws Exception {}
			});
				
		map.put(new XMLElementReaderKey(TAG_PHYLOXML, TAG_PHYLOGENY, XMLStreamConstants.START_ELEMENT), 
			new XMLElementReader<XMLStreamDataProvider<PhyloXMLEventReader>>() {
				@Override
				public void readEvent(XMLStreamDataProvider<PhyloXMLEventReader> streamDataProvider, XMLEvent event) throws Exception {
					streamDataProvider.setPassedSubnodes(new Stack<Queue<NodeInfo>>());					
				}
		});
		
		map.put(new XMLElementReaderKey(TAG_PHYLOXML, TAG_PHYLOGENY, XMLStreamConstants.END_ELEMENT), 
			new XMLElementReader<XMLStreamDataProvider<PhyloXMLEventReader>>() {
				@Override
				public void readEvent(XMLStreamDataProvider<PhyloXMLEventReader> streamDataProvider, XMLEvent event) throws Exception {
					streamDataProvider.getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(EventContentType.TREE, EventTopologyType.END));			}
		});
		
		map.put(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_CLADE, XMLStreamConstants.START_ELEMENT),
			new XMLElementReader<XMLStreamDataProvider<PhyloXMLEventReader>>() {
				@Override
				public void readEvent(XMLStreamDataProvider<PhyloXMLEventReader> streamDataProvider, XMLEvent event) throws Exception {
					String treeID = streamDataProvider.getTreeID();
					String treeLabel = streamDataProvider.getTreeLabel();		
					
					if (treeID == null) {
						treeID = DEFAULT_TREE_ID_PREFIX + streamDataProvider.getIDManager().createNewID(); 
					}					
					streamDataProvider.getCurrentEventCollection().add(new LabeledIDEvent(EventContentType.TREE, treeID, treeLabel));		
					
					
					streamDataProvider.setCurrentEventCollection(new ArrayList<JPhyloIOEvent>());
					System.out.println("First: " + streamDataProvider.getCurrentEventCollection().size());
					
//					streamDataProvider.getPassedSubnodes().add(new ArrayDeque<NodeInfo>()); // Add queue for top level.
					
					double branchLength = XMLUtils.readDoubleAttr(event.asStartElement(), TAG_BRANCH_LENGTH, Double.NaN);					
					streamDataProvider.setCurrentNodeInfo(new NodeInfo("", branchLength));										
				}
			});
		
		map.put(new XMLElementReaderKey(TAG_CLADE, TAG_CLADE, XMLStreamConstants.START_ELEMENT),
				new XMLElementReader<XMLStreamDataProvider<PhyloXMLEventReader>>() {
					@Override
					public void readEvent(XMLStreamDataProvider<PhyloXMLEventReader> streamDataProvider, XMLEvent event) throws Exception {
						streamDataProvider.setCurrentEventCollection(new ArrayList<JPhyloIOEvent>());
						System.out.println("Next: " + streamDataProvider.getCurrentEventCollection().size());
//						if (streamDataProvider.getCurrentNodeInfo() != null) {
//							streamDataProvider.getPassedSubnodes().peek().add(streamDataProvider.getCurrentNodeInfo()); // Add previous NodeInfo to the current queue.
//						}
//						
						double branchLength = XMLUtils.readDoubleAttr(event.asStartElement(), TAG_BRANCH_LENGTH, Double.NaN);			
						streamDataProvider.setCurrentNodeInfo(new NodeInfo("", branchLength));
//						
//						streamDataProvider.getPassedSubnodes().add(new ArrayDeque<NodeInfo>()); // Add queue for new level.						
					}
				});
		
		map.put(new XMLElementReaderKey(null, null, XMLStreamConstants.CHARACTERS), 
			new XMLElementReader<XMLStreamDataProvider<PhyloXMLEventReader>>() {			
				@Override
				public void readEvent(XMLStreamDataProvider<PhyloXMLEventReader> streamDataProvider, XMLEvent event) throws Exception {					
					String value = event.asCharacters().getData();
					
					if (!value.matches("\\s+")) { //space characters after tag ends are excluded this way					
						String parentName = streamDataProvider.getParentName();
						String elementName = streamDataProvider.getElementName();							
						
						if (parentName.equals(TAG_PHYLOGENY.getLocalPart())) {
							if (elementName.equals(TAG_NAME.getLocalPart())) {
								streamDataProvider.setTreeLabel(value);
							}
							else if (elementName.equals(TAG_ID.getLocalPart())) {
								streamDataProvider.setTreeID(value);
							}
						}
						
						else if (streamDataProvider.getCurrentNodeInfo() != null) {
							String currentNodeLabel = streamDataProvider.getCurrentNodeInfo().getLabel();
							String currentNodeID = streamDataProvider.getCurrentNodeInfo().getID();
							double currentNodeLength = streamDataProvider.getCurrentNodeInfo().getLength();
							
							if (parentName.equals(TAG_CLADE.getLocalPart())) {
								if (elementName.equals(TAG_NAME.getLocalPart())) {								
									currentNodeLabel = value;
								}
								else if (elementName.equals(TAG_BRANCH_LENGTH.getLocalPart()) && (currentNodeLength == Double.NaN)) {
									try {
										currentNodeLength = Double.parseDouble(value);
									}
									catch (NumberFormatException e) {
										throw new JPhyloIOReaderException("The branch length must be of type double.", event.getLocation());
									}
								}
								else if (elementName.equals(TAG_ID.getLocalPart())) {								
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
								else if (elementName.equals(TAG_ID.getLocalPart()) && (currentNodeID.equals(""))) {								
									currentNodeID = value;
								}
							}							
							else if (parentName.equals(TAG_SEQUENCE.getLocalPart())) {
								if (elementName.equals(TAG_NAME.getLocalPart()) && (currentNodeLabel == null)) {									
									currentNodeLabel = value;																	
								}
							}							
							
							streamDataProvider.getCurrentNodeInfo().setLabel(currentNodeLabel);
							streamDataProvider.getCurrentNodeInfo().setID(currentNodeID);
							streamDataProvider.getCurrentNodeInfo().setLength(currentNodeLength);							
						}
					}
				}
		});
		
		map.put(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_CLADE, XMLStreamConstants.END_ELEMENT), cladeEndReader);
		
		map.put(new XMLElementReaderKey(TAG_CLADE, TAG_CLADE, XMLStreamConstants.END_ELEMENT), cladeEndReader);
		
		map.put(new XMLElementReaderKey(null, null, XMLStreamConstants.START_ELEMENT), 
				new XMLElementReader<XMLStreamDataProvider<PhyloXMLEventReader>>() {			
					@Override
					public void readEvent(XMLStreamDataProvider<PhyloXMLEventReader> streamDataProvider, XMLEvent event) throws Exception {
						StartElement element = event.asStartElement();
						String elementName = element.getName().getLocalPart();
						XMLEvent nextEvent = streamDataProvider.getEventReader().getXMLReader().peek();
						String value = null;
								
						if (nextEvent.getEventType() == XMLStreamConstants.CHARACTERS) {
							String characterData = nextEvent.asCharacters().getData();
							if (!characterData.matches("\\s+")) {
								value = characterData;
							}
						}
						
						streamDataProvider.getCurrentEventCollection().add(new MetaInformationEvent(PHYLO_XML + "." + streamDataProvider.getParentName() + "." + elementName, "String", value));
						
						streamDataProvider.setMetaWithAttributes(element);
					}
			});
		
		map.put(new XMLElementReaderKey(null, null, XMLStreamConstants.END_ELEMENT), 
				new XMLElementReader<XMLStreamDataProvider<PhyloXMLEventReader>>() {			
					@Override
					public void readEvent(XMLStreamDataProvider<PhyloXMLEventReader> streamDataProvider, XMLEvent event) throws Exception {
						String attributeKey = PHYLO_XML + "." + streamDataProvider.getParentName() + "." + streamDataProvider.getMetaWithAttributes().getName().getLocalPart();
						Iterator<Attribute> attributes = streamDataProvider.getMetaWithAttributes().getAttributes();
						while (attributes.hasNext()) {
							streamDataProvider.getCurrentEventCollection().add(new MetaInformationEvent(attributeKey, "String", attributes.next().getValue()));
							streamDataProvider.getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(EventContentType.META_INFORMATION, EventTopologyType.END));
						}
						streamDataProvider.getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(EventContentType.META_INFORMATION, EventTopologyType.END));
					}
			});
		
		map.put(new XMLElementReaderKey(null, null, XMLStreamConstants.COMMENT), new CommentElementReader());
	}

	
	@Override
	public int getMaxCommentLength() {
		return 0;
	}
	

	@Override
	public void setMaxCommentLength(int maxCommentLength) {}
}
