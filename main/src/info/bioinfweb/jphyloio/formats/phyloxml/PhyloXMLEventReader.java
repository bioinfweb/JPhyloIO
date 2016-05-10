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


import info.bioinfweb.commons.io.XMLUtils;
import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.EdgeEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.LinkedLabeledIDEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralContentSequenceType;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataContentEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.UriOrStringIdentifier;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.exception.JPhyloIOReaderException;
import info.bioinfweb.jphyloio.formats.JPhyloIOFormatIDs;
import info.bioinfweb.jphyloio.formats.NodeEdgeInfo;
import info.bioinfweb.jphyloio.formats.phyloxml.elementreader.PhyloXMLCharactersElementReader;
import info.bioinfweb.jphyloio.formats.phyloxml.elementreader.PhyloXMLEndElementReader;
import info.bioinfweb.jphyloio.formats.phyloxml.elementreader.PhyloXMLStartElementReader;
import info.bioinfweb.jphyloio.formats.xml.AbstractXMLElementReader;
import info.bioinfweb.jphyloio.formats.xml.AbstractXMLEventReader;
import info.bioinfweb.jphyloio.formats.xml.CommentElementReader;
import info.bioinfweb.jphyloio.formats.xml.XMLElementReader;
import info.bioinfweb.jphyloio.formats.xml.XMLElementReaderKey;
import info.bioinfweb.jphyloio.formats.xml.XMLToMetaElementStartReader;

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
			public void readEvent(PhyloXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
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
			public void readEvent(PhyloXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				StartElement element = event.asStartElement();					
				streamDataProvider.setCurrentEventCollection(streamDataProvider.getEdgeInfos().peek().getNestedEvents());
				
				streamDataProvider.getCurrentEventCollection().add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + streamDataProvider.getIDManager().createNewID(), 
						null, new UriOrStringIdentifier(null, element.getName()), null, LiteralContentSequenceType.XML));
				
				streamDataProvider.getCurrentEventCollection().add(new LiteralMetadataContentEvent(event, false));
				
				streamDataProvider.getNestedMetaNames().add(element.getName().getLocalPart());
			}
		};
			
		XMLElementReader<PhyloXMLReaderStreamDataProvider> branchMetaEndReader = new AbstractXMLElementReader<PhyloXMLReaderStreamDataProvider>() {
			@Override
			public void readEvent(PhyloXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				streamDataProvider.getCurrentEventCollection().add(new LiteralMetadataContentEvent(event, false));
				streamDataProvider.getNestedMetaNames().pop();
				
				if (streamDataProvider.getNestedMetaNames().isEmpty()) {
					streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
				}
				
				streamDataProvider.resetCurrentEventCollection();
			}
		};
		
		XMLElementReader<PhyloXMLReaderStreamDataProvider> emptyReader = new AbstractXMLElementReader<PhyloXMLReaderStreamDataProvider>() {			
			@Override
			public void readEvent(PhyloXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {}
		};
		
		putElementReader(new XMLElementReaderKey(null, null, XMLStreamConstants.START_DOCUMENT), 
			new AbstractXMLElementReader<PhyloXMLReaderStreamDataProvider>() {
				@Override
				public void readEvent(PhyloXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
					streamDataProvider.getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(EventContentType.DOCUMENT, EventTopologyType.START));
				}
		});
		
		putElementReader(new XMLElementReaderKey(null, null, XMLStreamConstants.END_DOCUMENT), 
			new AbstractXMLElementReader<PhyloXMLReaderStreamDataProvider>() {
				@Override
				public void readEvent(PhyloXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
					streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.DOCUMENT));
				}
		});
		
		putElementReader(new XMLElementReaderKey(null, TAG_ROOT, XMLStreamConstants.START_ELEMENT), emptyReader); //no meta events belonging to tag phyloXML are read
		
		putElementReader(new XMLElementReaderKey(null, TAG_ROOT, XMLStreamConstants.END_ELEMENT), emptyReader); //no meta events belonging to tag phyloXML are read
				
		putElementReader(new XMLElementReaderKey(TAG_ROOT, TAG_PHYLOGENY, XMLStreamConstants.START_ELEMENT), 
			new AbstractXMLElementReader<PhyloXMLReaderStreamDataProvider>() {
				@Override
				public void readEvent(PhyloXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
					StartElement element = event.asStartElement();					
					boolean rooted = XMLUtils.readBooleanAttr(event.asStartElement(), ATTR_ROOTED, false);					
					
					streamDataProvider.setRooted(rooted);
					streamDataProvider.setCurrentEventCollection(new ArrayList<JPhyloIOEvent>());
//					readAttributes(streamDataProvider, element); //TODO do not read root attribute again here
				}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_ROOT, TAG_PHYLOGENY, XMLStreamConstants.END_ELEMENT), 
			new AbstractXMLElementReader<PhyloXMLReaderStreamDataProvider>() {
				@Override
				public void readEvent(PhyloXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
					streamDataProvider.getSourceNode().clear();
					streamDataProvider.getEdgeInfos().clear();
					streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.TREE));			
				}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_CLADE, XMLStreamConstants.START_ELEMENT),
			new AbstractXMLElementReader<PhyloXMLReaderStreamDataProvider>() {
				@Override
				public void readEvent(PhyloXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {					
					String treeID = getID(null, EventContentType.TREE);
					String treeLabel = streamDataProvider.getTreeLabel();					
					Collection<JPhyloIOEvent> nestedEvents = streamDataProvider.resetCurrentEventCollection();
					
					streamDataProvider.getCurrentEventCollection().add(new LinkedLabeledIDEvent(EventContentType.TREE, treeID, treeLabel, null));
					
//					streamDataProvider.getCurrentEventCollection().add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + streamDataProvider.getIDManager().createNewID(), 
//							null, new UriOrStringIdentifier(null, new QName(META_KEY_DISPLAY_TREE_ROOTED)), null, LiteralContentSequenceType.SIMPLE));
//					streamDataProvider.getCurrentEventCollection().add(new LiteralMetadataContentEvent(new UriOrStringIdentifier("boolean", new QName(NAMESPACE_XSI, "boolean", XSD_PRE)), 
//							Boolean.toString(streamDataProvider.isRooted()), ((Boolean)streamDataProvider.isRooted())));
//					streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL)); 
					
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
					public void readEvent(PhyloXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
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
				public void readEvent(PhyloXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {					
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
						
						if (!streamDataProvider.getNestedMetaNames().isEmpty()) {
							boolean isContinued = false;
							if (streamDataProvider.getXMLReader().peek().getEventType() == XMLStreamConstants.CHARACTERS) {
								isContinued = true;
							}
							
							streamDataProvider.getCurrentEventCollection().add(new LiteralMetadataContentEvent(event, isContinued));
						}
					}
				}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_CLADE, XMLStreamConstants.END_ELEMENT), cladeEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_CLADE, XMLStreamConstants.END_ELEMENT), cladeEndReader);
		
//		putElementReader(new XMLElementReaderKey(null, null, XMLStreamConstants.START_ELEMENT), new XMLToMetaElementStartReader());
		
//		putElementReader(new XMLElementReaderKey(null, null, XMLStreamConstants.END_ELEMENT), 
//				new AbstractXMLElementReader<PhyloXMLReaderStreamDataProvider>() {
//					@Override
//					public void readEvent(PhyloXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
//						streamDataProvider.getCurrentEventCollection().add(new LiteralMetadataContentEvent(event, false));
//						streamDataProvider.getNestedMetaNames().pop();
//						
//						if (streamDataProvider.getNestedMetaNames().isEmpty()) {
//							streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
//						}
//					}
//			});
		
		putElementReader(new XMLElementReaderKey(null, null, XMLStreamConstants.COMMENT), new CommentElementReader());
		
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_TAXONOMY, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(PREDICATE_TAXONOMY, false, true, ATTR_ID_SOURCE, PREDICATE_TAXONOMY_ATTR_ID_SOURCE));
		
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_ID, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(PREDICATE_TAXONOMY_ID, true, true, ATTR_ID_PROVIDER, PREDICATE_TAXONOMY_ID_ATTR_PROVIDER));
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_ID, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_ID, XMLStreamConstants.END_ELEMENT), new PhyloXMLEndElementReader(false, true));
		
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_CODE, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_TAXONOMY_CODE, true, false));
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_CODE, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_CODE, XMLStreamConstants.END_ELEMENT), new PhyloXMLEndElementReader(true, false));
		
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_SCI_NAME, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(PREDICATE_TAXONOMY_SCI_NAME, true, false));
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_SCI_NAME, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_SCI_NAME, XMLStreamConstants.END_ELEMENT), new PhyloXMLEndElementReader(true, false));
		
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_AUTHORITY, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(PREDICATE_TAXONOMY_AUTHORITY, true, false));
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_AUTHORITY, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_AUTHORITY, XMLStreamConstants.END_ELEMENT), new PhyloXMLEndElementReader(true, false));
		
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_COMMON_NAME, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(PREDICATE_TAXONOMY_COMMON_NAME, true, false));
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_COMMON_NAME, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_COMMON_NAME, XMLStreamConstants.END_ELEMENT), new PhyloXMLEndElementReader(true, false));
		
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_SYNONYM, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(PREDICATE_TAXONOMY_SYNONYM, true, false));
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_SYNONYM, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_SYNONYM, XMLStreamConstants.END_ELEMENT), new PhyloXMLEndElementReader(true, false));
		
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_RANK, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(PREDICATE_TAXONOMY_RANK, true, false));
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_RANK, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_RANK, XMLStreamConstants.END_ELEMENT), new PhyloXMLEndElementReader(true, false));
		
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_URI, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(PREDICATE_TAXONOMY_URI, true, true, ATTR_DESC, PREDICATE_TAXONOMY_URI_ATTR_DESC, ATTR_TYPE, PREDICATE_TAXONOMY_URI_ATTR_TYPE));		
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_URI, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_URI, XMLStreamConstants.END_ELEMENT), new PhyloXMLEndElementReader(false, true));
		
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_TAXONOMY, XMLStreamConstants.END_ELEMENT), new PhyloXMLEndElementReader(false, true));
	}
	
	
	private void createNodeEvents(NodeEdgeInfo info) {		
		getStreamDataProvider().getCurrentEventCollection().add(new LinkedLabeledIDEvent(EventContentType.NODE, info.getID(), info.getLabel(), null));
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
