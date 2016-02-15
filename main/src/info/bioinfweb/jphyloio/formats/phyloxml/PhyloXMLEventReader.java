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
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.MetaInformationEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.formats.NodeEdgeInfo;
import info.bioinfweb.jphyloio.formats.xml.AbstractXMLElementReader;
import info.bioinfweb.jphyloio.formats.xml.AbstractXMLEventReader;
import info.bioinfweb.jphyloio.formats.xml.CommentElementReader;
import info.bioinfweb.jphyloio.formats.xml.UnknownMetaElementEndReader;
import info.bioinfweb.jphyloio.formats.xml.UnknownMetaElementStartReader;
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
		
		XMLElementReader<PhyloXMLStreamDataProvider> cladeEndReader = 
			new AbstractXMLElementReader<PhyloXMLStreamDataProvider>() {
				@Override
				public void readEvent(PhyloXMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
					NodeEdgeInfo info = streamDataProvider.getCurrentNodeEdgeInfo();
					
					if (info != null) { //node has no children
						Collection<JPhyloIOEvent> nestedEvents = streamDataProvider.resetCurrentEventCollection();
						
						info.setID(getID(info.getID(), EventContentType.NODE)); //make sure node has valid ID
						streamDataProvider.setCurrentNodeEdgeInfo(null);
						
						createNodeEvents(info, nestedEvents);
						createEdgeEvents(info, streamDataProvider.getCurrentParentNodeID());
					}
				}
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
		
		map.put(new XMLElementReaderKey(null, TAG_PHYLOXML, XMLStreamConstants.START_ELEMENT), 
				new AbstractXMLElementReader<PhyloXMLStreamDataProvider>() {
					@Override
					public void readEvent(PhyloXMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {} //no meta events belonging to tag phyloxml are read
			});
		
		map.put(new XMLElementReaderKey(null, TAG_PHYLOXML, XMLStreamConstants.END_ELEMENT), 
				new AbstractXMLElementReader<PhyloXMLStreamDataProvider>() {
					@Override
					public void readEvent(PhyloXMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {}
			});
				
		map.put(new XMLElementReaderKey(TAG_PHYLOXML, TAG_PHYLOGENY, XMLStreamConstants.START_ELEMENT), 
			new AbstractXMLElementReader<PhyloXMLStreamDataProvider>() {
				@Override
				public void readEvent(PhyloXMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
					boolean rooted = XMLUtils.readBooleanAttr(event.asStartElement(), ATTR_ROOTED, false);
					streamDataProvider.setRooted(rooted);
				}
		});
		
		map.put(new XMLElementReaderKey(TAG_PHYLOXML, TAG_PHYLOGENY, XMLStreamConstants.END_ELEMENT), 
			new AbstractXMLElementReader<PhyloXMLStreamDataProvider>() {
				@Override
				public void readEvent(PhyloXMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
					streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.TREE));			}
		});
		
		map.put(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_CLADE, XMLStreamConstants.START_ELEMENT),
			new AbstractXMLElementReader<PhyloXMLStreamDataProvider>() {
				@Override
				public void readEvent(PhyloXMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
					String treeID = getID(streamDataProvider.getTreeID(), EventContentType.TREE);
					String treeLabel = streamDataProvider.getTreeLabel();		
								
					streamDataProvider.getCurrentEventCollection().add(new LabeledIDEvent(EventContentType.TREE, treeID, treeLabel));		

					
					streamDataProvider.setCurrentEventCollection(new ArrayList<JPhyloIOEvent>());
					
					double branchLength = XMLUtils.readDoubleAttr(event.asStartElement(), TAG_BRANCH_LENGTH, Double.NaN);					
					streamDataProvider.setCurrentNodeEdgeInfo(new NodeEdgeInfo("", branchLength));										
				}
			});
		
		map.put(new XMLElementReaderKey(TAG_CLADE, TAG_CLADE, XMLStreamConstants.START_ELEMENT),
				new AbstractXMLElementReader<PhyloXMLStreamDataProvider>() {
					@Override
					public void readEvent(PhyloXMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
						NodeEdgeInfo info = streamDataProvider.getCurrentNodeEdgeInfo();
						
						if (info != null) { //node has children
							Collection<JPhyloIOEvent> nestedEvents = streamDataProvider.resetCurrentEventCollection();
							String parentNodeID = streamDataProvider.getCurrentParentNodeID();
							
							info.setID(getID(info.getID(), EventContentType.NODE)); //make sure node has valid ID
							streamDataProvider.setCurrentParentNodeID(info.getID());
							
							createNodeEvents(info, nestedEvents);							
							createEdgeEvents(info, parentNodeID);
						}						
						
						streamDataProvider.setCurrentEventCollection(new ArrayList<JPhyloIOEvent>());
						
						double branchLength = XMLUtils.readDoubleAttr(event.asStartElement(), TAG_BRANCH_LENGTH, Double.NaN);			
						streamDataProvider.setCurrentNodeEdgeInfo(new NodeEdgeInfo("", branchLength));												
					}
				});
		
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
								streamDataProvider.setTreeLabel(value);
							}
							else if (elementName.equals(TAG_ID.getLocalPart())) {
								streamDataProvider.setTreeID(value);
							}
						}
						
						else if (streamDataProvider.getCurrentNodeEdgeInfo() != null) {
							String currentNodeLabel = streamDataProvider.getCurrentNodeEdgeInfo().getLabel();
							String currentNodeID = streamDataProvider.getCurrentNodeEdgeInfo().getID();
							double currentNodeLength = streamDataProvider.getCurrentNodeEdgeInfo().getLength();
							
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
							
							streamDataProvider.getCurrentNodeEdgeInfo().setLabel(currentNodeLabel);
							streamDataProvider.getCurrentNodeEdgeInfo().setID(currentNodeID);
							streamDataProvider.getCurrentNodeEdgeInfo().setLength(currentNodeLength);							
						}
					}
				}
		});
		
		map.put(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_CLADE, XMLStreamConstants.END_ELEMENT), cladeEndReader);
		
		map.put(new XMLElementReaderKey(TAG_CLADE, TAG_CLADE, XMLStreamConstants.END_ELEMENT), cladeEndReader);
		
		map.put(new XMLElementReaderKey(null, null, XMLStreamConstants.START_ELEMENT), new UnknownMetaElementStartReader());
		
		map.put(new XMLElementReaderKey(null, null, XMLStreamConstants.END_ELEMENT), new UnknownMetaElementEndReader());
		
		map.put(new XMLElementReaderKey(null, null, XMLStreamConstants.COMMENT), new CommentElementReader());
	}
	
	
	private void createNodeEvents(NodeEdgeInfo info, Collection<JPhyloIOEvent> nestedEvents) {
		getStreamDataProvider().getCurrentEventCollection().add(new LabeledIDEvent(EventContentType.NODE, info.getID(), info.getLabel()));
		for (JPhyloIOEvent nextEvent : nestedEvents) {
			getStreamDataProvider().getCurrentEventCollection().add(nextEvent);
		}							
		getStreamDataProvider().getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.NODE));
	}
	
	
	private void createEdgeEvents(NodeEdgeInfo info, String parentNodeID) { //TODO possibly give some nested meta events here
		if (parentNodeID == null) {
			if (getStreamDataProvider().isRooted()) {
				parentNodeID = TAG_ROOT.getLocalPart();									
			}
		}
		if (parentNodeID != null) {
			getStreamDataProvider().getCurrentEventCollection().add(new EdgeEvent(getID(null, EventContentType.EDGE), null, 
					parentNodeID, info.getID(), info.getLength()));
			getStreamDataProvider().getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.EDGE));
		}
	}
	
	
	@Override
	protected PhyloXMLStreamDataProvider createStreamDataProvider() {
		return new PhyloXMLStreamDataProvider(this);
	}
	
	
	@Override
	public int getMaxCommentLength() {
		return 0;
	}
	

	@Override
	public void setMaxCommentLength(int maxCommentLength) {}
}
