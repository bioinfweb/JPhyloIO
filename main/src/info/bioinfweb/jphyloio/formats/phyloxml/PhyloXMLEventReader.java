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
import info.bioinfweb.jphyloio.formats.phyloxml.elementreader.PhyloXMLNoCharactersAllowedElementReader;
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
		
		AbstractXMLElementReader<PhyloXMLReaderStreamDataProvider> nodeLabelReader = new AbstractXMLElementReader<PhyloXMLReaderStreamDataProvider>() {
			@Override
			public void readEvent(PhyloXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				String value = event.asCharacters().getData();
				
				if (!streamDataProvider.getSourceNode().isEmpty()) {
					NodeEdgeInfo currentNode = streamDataProvider.getSourceNode().peek();
					String currentNodeLabel = currentNode.getLabel();
					
					if (currentNodeLabel == null) {
						currentNode.setLabel(value);
					}
				}				
				
				boolean isContinued = streamDataProvider.getEventReader().peek().getType().equals(XMLStreamConstants.CHARACTERS);
				streamDataProvider.getCurrentEventCollection().add(new LiteralMetadataContentEvent(
						new UriOrStringIdentifier(null, DATA_TYPE_TOKEN), event.asCharacters().getData(), isContinued));
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
					
					streamDataProvider.setCurrentEventCollection(new ArrayList<JPhyloIOEvent>());
					
					readAttributes(streamDataProvider, element, ATTR_ROOTED, ReadWriteConstants.PREDICATE_DISPLAY_TREE_ROOTED, ATTR_REROOTABLE, 
							PREDICATE_PHYLOGENY_ATTR_REROOTABLE, ATTR_BRANCH_LENGTH_UNIT, PREDICATE_PHYLOGENY_ATTR_BRANCH_LENGTH_UNIT, ATTR_TYPE, PREDICATE_PHYLOGENY_ATTR_TYPE);
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
					
					for (JPhyloIOEvent nextEvent : nestedEvents) {
						streamDataProvider.getCurrentEventCollection().add(nextEvent);
					}
					
					double branchLength = XMLUtils.readDoubleAttr(event.asStartElement(), TAG_BRANCH_LENGTH, Double.NaN);	
					streamDataProvider.getSourceNode().add(new NodeEdgeInfo(null, Double.NaN, new ArrayList<JPhyloIOEvent>()));
					streamDataProvider.getEdgeInfos().add(new NodeEdgeInfo(null, branchLength, new ArrayList<JPhyloIOEvent>()));
					streamDataProvider.getEdgeInfos().peek().setSource(null);
					
					streamDataProvider.setCurrentEventCollection(streamDataProvider.getSourceNode().peek().getNestedEvents());
					
					readAttributes(streamDataProvider, event.asStartElement(), ATTR_ID_SOURCE, PREDICATE_CLADE_ATTR_ID_SOURCE);
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
					
					readAttributes(streamDataProvider, event.asStartElement(), ATTR_ID_SOURCE, PREDICATE_CLADE_ATTR_ID_SOURCE);
				}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_CLADE, XMLStreamConstants.END_ELEMENT), cladeEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_CLADE, XMLStreamConstants.END_ELEMENT), cladeEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_NAME, null, XMLStreamConstants.CHARACTERS),
				new AbstractXMLElementReader<PhyloXMLReaderStreamDataProvider>() {
					@Override
					public void readEvent(PhyloXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
						String value = event.asCharacters().getData();
						String parentName = streamDataProvider.getParentName();
						boolean useAsMeta = true;
						
						if (parentName.equals(TAG_PHYLOGENY.getLocalPart())) {
							streamDataProvider.setTreeLabel(value);
							useAsMeta = false;
						}
						else if (!streamDataProvider.getSourceNode().isEmpty()) {
							NodeEdgeInfo currentNode = streamDataProvider.getSourceNode().peek();
							String currentNodeLabel = currentNode.getLabel();
							
							if (parentName.equals(TAG_CLADE.getLocalPart())) {
								currentNode.setLabel(value);
								useAsMeta = false;
							}
							else if (parentName.equals(TAG_SEQUENCE.getLocalPart())) {
								useAsMeta = false;
								if (currentNodeLabel == null) {
									currentNode.setLabel(value);
								}
							}							
						}
						
						if (useAsMeta) {
							boolean isContinued = streamDataProvider.getEventReader().peek().getType().equals(XMLStreamConstants.CHARACTERS);
							streamDataProvider.getCurrentEventCollection().add(new LiteralMetadataContentEvent(
									new UriOrStringIdentifier(null, DATA_TYPE_TOKEN), event.asCharacters().getData(), isContinued));
						}
					}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_BRANCH_LENGTH, null, XMLStreamConstants.CHARACTERS),
				new AbstractXMLElementReader<PhyloXMLReaderStreamDataProvider>() {
					@Override
					public void readEvent(PhyloXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
						String value = event.asCharacters().getData();
						
						if (!streamDataProvider.getEdgeInfos().isEmpty()) {
							NodeEdgeInfo currentEdge = streamDataProvider.getEdgeInfos().peek();
							double currentEdgeLength = currentEdge.getLength();
							double newEdgeLength;
							
							try {
								newEdgeLength = Double.parseDouble(value);
							}
							catch (NumberFormatException e) {
								throw new JPhyloIOReaderException("The branch length must be of type double.", event.getLocation());
							}
							
							if (currentEdgeLength == Double.NaN) {								
								currentEdge.setLength(newEdgeLength);								
							}
							else if (newEdgeLength != currentEdgeLength) {
								getParameters().getLogger().addWarning("Two different branch lengths of \"" + currentEdgeLength + "\" and \"" + newEdgeLength 
										+ "\" are present for the same branch in the document.");
							}
						}
					}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_ID, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(PREDICATE_PHYLOGENY_ID_VALUE, PREDICATE_PHYLOGENY_ID, false, ATTR_ID_PROVIDER, PREDICATE_PHYLOGENY_ID_ATTR_PROVIDER));
		putElementReader(new XMLElementReaderKey(TAG_ID, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_ID, XMLStreamConstants.END_ELEMENT), new PhyloXMLEndElementReader(true, true, false));
		
		putElementReader(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_DESCRIPTION, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_PHYLOGENY_DESCRIPTION, null, false));
		putElementReader(new XMLElementReaderKey(TAG_DESCRIPTION, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_DESCRIPTION, XMLStreamConstants.END_ELEMENT), new PhyloXMLEndElementReader(true, false, false));
		
		putElementReader(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_DATE, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_PHYLOGENY_DATE, null, false));
		putElementReader(new XMLElementReaderKey(TAG_DATE, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(DATA_TYPE_TOKEN)); //TODO type xs:dateTime
		putElementReader(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_DATE, XMLStreamConstants.END_ELEMENT), new PhyloXMLEndElementReader(true, false, false));
		
		putElementReader(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_CONFIDENCE, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(PREDICATE_PHYLOGENY_CONFIDENCE_VALUE, PREDICATE_PHYLOGENY_CONFIDENCE, false, ATTR_TYPE, PREDICATE_PHYLOGENY_CONFIDENCE_ATTR_TYPE));
		putElementReader(new XMLElementReaderKey(TAG_CONFIDENCE, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(DATA_TYPE_TOKEN)); //TODO type xs:double
		putElementReader(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_CONFIDENCE, XMLStreamConstants.END_ELEMENT), new PhyloXMLEndElementReader(true, true, false));
		
		putElementReader(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_CLADE_RELATION, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(null, PREDICATE_CLADE_REL, false, ATTR_ID_REF_0, PREDICATE_CLADE_REL_ATTR_IDREF0, ATTR_ID_REF_1, PREDICATE_CLADE_REL_ATTR_IDREF1, 
						ATTR_DISTANCE, PREDICATE_CLADE_REL_ATTR_DISTANCE, ATTR_TYPE, PREDICATE_CLADE_REL_ATTR_TYPE));
		putElementReader(new XMLElementReaderKey(TAG_CLADE_RELATION, null, XMLStreamConstants.CHARACTERS), new PhyloXMLNoCharactersAllowedElementReader());
		
		putElementReader(new XMLElementReaderKey(TAG_CLADE_RELATION, TAG_CONFIDENCE, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(PREDICATE_CLADE_REL_CONFIDENCE_VALUE, PREDICATE_CLADE_REL_CONFIDENCE, false, ATTR_TYPE, PREDICATE_CLADE_REL_CONFIDENCE_ATTR_TYPE));		
		putElementReader(new XMLElementReaderKey(TAG_CLADE_RELATION, TAG_CONFIDENCE, XMLStreamConstants.END_ELEMENT), new PhyloXMLEndElementReader(true, true, false));
		
		putElementReader(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_CLADE_RELATION, XMLStreamConstants.END_ELEMENT), new PhyloXMLEndElementReader(false, true, false));
		
		putElementReader(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_SEQUENCE_RELATION, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(null, PREDICATE_SEQ_REL, false, ATTR_ID_REF_0, PREDICATE_SEQ_REL_ATTR_IDREF0, ATTR_ID_REF_1, PREDICATE_SEQ_REL_ATTR_IDREF1, 
						ATTR_DISTANCE, PREDICATE_SEQ_REL_ATTR_DISTANCE, ATTR_TYPE, PREDICATE_SEQ_REL_ATTR_TYPE));
		putElementReader(new XMLElementReaderKey(TAG_SEQUENCE_RELATION, null, XMLStreamConstants.CHARACTERS), new PhyloXMLNoCharactersAllowedElementReader());
		
		putElementReader(new XMLElementReaderKey(TAG_SEQUENCE_RELATION, TAG_CONFIDENCE, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(PREDICATE_SEQ_REL_CONFIDENCE_VALUE, PREDICATE_SEQ_REL_CONFIDENCE, false, ATTR_TYPE, PREDICATE_SEQ_REL_CONFIDENCE_ATTR_TYPE));		
		putElementReader(new XMLElementReaderKey(TAG_SEQUENCE_RELATION, TAG_CONFIDENCE, XMLStreamConstants.END_ELEMENT), new PhyloXMLEndElementReader(true, true, false));
		
		putElementReader(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_SEQUENCE_RELATION, XMLStreamConstants.END_ELEMENT), new PhyloXMLEndElementReader(false, true, false));
		
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_CONFIDENCE, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(PREDICATE_CLADE_CONFIDENCE_VALUE, PREDICATE_CLADE_CONFIDENCE, true, ATTR_TYPE, PREDICATE_CLADE_CONFIDENCE_ATTR_TYPE));		
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_CONFIDENCE, XMLStreamConstants.END_ELEMENT), new PhyloXMLEndElementReader(true, true, true));
		
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_BRANCH_WIDTH, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_CLADE_WIDTH, null, true));
		putElementReader(new XMLElementReaderKey(TAG_BRANCH_WIDTH, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(DATA_TYPE_TOKEN)); //TODO type xs:double
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_BRANCH_WIDTH, XMLStreamConstants.END_ELEMENT), new PhyloXMLEndElementReader(true, false, true));
		
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_BRANCH_COLOR, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(null, PREDICATE_COLOR, true));
		putElementReader(new XMLElementReaderKey(TAG_BRANCH_COLOR, null, XMLStreamConstants.CHARACTERS), new PhyloXMLNoCharactersAllowedElementReader());
		
		putElementReader(new XMLElementReaderKey(TAG_BRANCH_COLOR, TAG_RED, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_COLOR_RED, null, true));
		putElementReader(new XMLElementReaderKey(TAG_RED, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_BRANCH_COLOR, TAG_RED, XMLStreamConstants.END_ELEMENT), new PhyloXMLEndElementReader(true, false, true));
		
		putElementReader(new XMLElementReaderKey(TAG_BRANCH_COLOR, TAG_GREEN, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_COLOR_GREEN, null, true));
		putElementReader(new XMLElementReaderKey(TAG_GREEN, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_BRANCH_COLOR, TAG_GREEN, XMLStreamConstants.END_ELEMENT), new PhyloXMLEndElementReader(true, false, true));
		
		putElementReader(new XMLElementReaderKey(TAG_BRANCH_COLOR, TAG_BLUE, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_COLOR_BLUE, null, true));
		putElementReader(new XMLElementReaderKey(TAG_BLUE, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_BRANCH_COLOR, TAG_BLUE, XMLStreamConstants.END_ELEMENT), new PhyloXMLEndElementReader(true, false, true));
		
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_BRANCH_COLOR, XMLStreamConstants.END_ELEMENT), new PhyloXMLEndElementReader(false, true, true));
		
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_NODE_ID, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(PREDICATE_CLADE_NODE_ID_VALUE, PREDICATE_CLADE_NODE_ID, false, ATTR_ID_PROVIDER, PREDICATE_CLADE_NODE_ID_ATTR_PROVIDER));
		putElementReader(new XMLElementReaderKey(TAG_NODE_ID, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_NODE_ID, XMLStreamConstants.END_ELEMENT), new PhyloXMLEndElementReader(true, true, false));		
		
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_TAXONOMY, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(null, PREDICATE_TAXONOMY, false, ATTR_ID_SOURCE, PREDICATE_TAXONOMY_ATTR_ID_SOURCE));
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, null, XMLStreamConstants.CHARACTERS), new PhyloXMLNoCharactersAllowedElementReader());
		
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_ID, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(PREDICATE_TAXONOMY_ID_VALUE, PREDICATE_TAXONOMY_ID, false, ATTR_ID_PROVIDER, PREDICATE_TAXONOMY_ID_ATTR_PROVIDER));
		putElementReader(new XMLElementReaderKey(TAG_ID, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_ID, XMLStreamConstants.END_ELEMENT), new PhyloXMLEndElementReader(true, true, false));
		
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_CODE, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_TAXONOMY_CODE, null, false));
		putElementReader(new XMLElementReaderKey(TAG_CODE, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_CODE, XMLStreamConstants.END_ELEMENT), new PhyloXMLEndElementReader(true, false, false));
		
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_SCI_NAME, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(PREDICATE_TAXONOMY_SCI_NAME, null, false));
		putElementReader(new XMLElementReaderKey(TAG_SCI_NAME, null, XMLStreamConstants.CHARACTERS), nodeLabelReader);
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_SCI_NAME, XMLStreamConstants.END_ELEMENT), new PhyloXMLEndElementReader(true, false, false));
		
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_AUTHORITY, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(PREDICATE_TAXONOMY_AUTHORITY, null, false));
		putElementReader(new XMLElementReaderKey(TAG_AUTHORITY, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_AUTHORITY, XMLStreamConstants.END_ELEMENT), new PhyloXMLEndElementReader(true, false, false));
		
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_COMMON_NAME, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(PREDICATE_TAXONOMY_COMMON_NAME, null, false));
		putElementReader(new XMLElementReaderKey(TAG_COMMON_NAME, null, XMLStreamConstants.CHARACTERS), nodeLabelReader);
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_COMMON_NAME, XMLStreamConstants.END_ELEMENT), new PhyloXMLEndElementReader(true, false, false));
		
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_SYNONYM, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(PREDICATE_TAXONOMY_SYNONYM, null, false));
		putElementReader(new XMLElementReaderKey(TAG_SYNONYM, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_SYNONYM, XMLStreamConstants.END_ELEMENT), new PhyloXMLEndElementReader(true, false, false));
		
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_RANK, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(PREDICATE_TAXONOMY_RANK, null, false));
		putElementReader(new XMLElementReaderKey(TAG_RANK, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_RANK, XMLStreamConstants.END_ELEMENT), new PhyloXMLEndElementReader(true, false, false));
		
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_URI, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(PREDICATE_TAXONOMY_URI_VALUE, PREDICATE_TAXONOMY_URI, false, ATTR_DESC, PREDICATE_TAXONOMY_URI_ATTR_DESC, ATTR_TYPE, PREDICATE_TAXONOMY_URI_ATTR_TYPE));		
		putElementReader(new XMLElementReaderKey(TAG_URI, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_URI, XMLStreamConstants.END_ELEMENT), new PhyloXMLEndElementReader(false, true, false));
		
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_TAXONOMY, XMLStreamConstants.END_ELEMENT), new PhyloXMLEndElementReader(false, true, false));
		
		
		
		//TODO implement customXML reading
		
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
