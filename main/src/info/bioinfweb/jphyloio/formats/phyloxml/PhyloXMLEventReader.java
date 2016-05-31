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


import info.bioinfweb.commons.io.W3CXSConstants;
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
import info.bioinfweb.jphyloio.events.meta.ResourceMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.URIOrStringIdentifier;
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
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
				createNodeEvents(streamDataProvider);				
				getStreamDataProvider().getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.NODE));
				
				createEdgeEvents(streamDataProvider);
				
				if (!streamDataProvider.getPropertyEvents().isEmpty()) {
					for (JPhyloIOEvent nextEvent : streamDataProvider.getPropertyEvents()) {
						streamDataProvider.getCurrentEventCollection().add(nextEvent);
					}
					streamDataProvider.getPropertyEvents().clear();
				}
				
				streamDataProvider.getSourceNode().pop();
				streamDataProvider.setLastNodeID(null);
				streamDataProvider.setCreateNodeStart(true);
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
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), event.asCharacters().getData(), isContinued));
			}
		};
		
		AbstractXMLElementReader<PhyloXMLReaderStreamDataProvider> propertyStartReader = new AbstractXMLElementReader<PhyloXMLReaderStreamDataProvider>() {
			@Override
			public void readEvent(PhyloXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				StartElement element = event.asStartElement();
				String parentTag = streamDataProvider.getParentName();
				
				String appliesTo = XMLUtils.readStringAttr(element, ATTR_APPLIES_TO, null);
				URIOrStringIdentifier predicate = new URIOrStringIdentifier(null, qNameFromCURIE(XMLUtils.readStringAttr(element, ATTR_REF, null), streamDataProvider));
				
				boolean appliesToAsAttribute = true;
				boolean resetEventCollection = false;				
				
				if (parentTag.equals(TAG_CLADE.getLocalPart())) {
					if (appliesTo.equals("parent_branch")) { //TODO create constants for AppliesTo values and use switch
						streamDataProvider.setCurrentEventCollection(streamDataProvider.getEdgeInfos().peek().getNestedEvents());
						resetEventCollection = true;
						appliesToAsAttribute = false;
					}
					else if (appliesTo.equals("phylogeny")) {
						streamDataProvider.setCurrentEventCollection(streamDataProvider.getPropertyEvents());
						resetEventCollection = true;
						appliesToAsAttribute = false;
					}
					else if (appliesTo.equals("node")) {
						appliesToAsAttribute = false;
					}
				}
				else if (parentTag.equals(TAG_PHYLOGENY.getLocalPart())) {
					if (appliesTo.equals("phylogeny")) {
						appliesToAsAttribute = false;
					}
				}
				else if (parentTag.equals(TAG_ANNOTATION.getLocalPart())) {
					if (appliesTo.equals("annotation")) {
						appliesToAsAttribute = false;
					}
				}
				
				if ((XMLUtils.readStringAttr(element, ATTR_UNIT, null) != null) || (XMLUtils.readStringAttr(element, ATTR_ID_REF, null) != null) || appliesToAsAttribute == true) {
					streamDataProvider.getCurrentEventCollection().add(
							new ResourceMetadataEvent(streamDataProvider.getEventReader().getID(EventContentType.META_RESOURCE), null, predicate, null, null));					
					streamDataProvider.setPropertyHasResource(true);
					
					if (appliesToAsAttribute) {
						readAttributes(streamDataProvider, element, ATTR_APPLIES_TO, PREDICATE_PROPERTY_ATTR_APPLIES_TO, ATTR_UNIT, PREDICATE_PROPERTY_ATTR_UNIT, 
								ATTR_ID_REF, PREDICATE_PROPERTY_ATTR_ID_REF);
					}
					else {
						readAttributes(streamDataProvider, element, ATTR_UNIT, PREDICATE_PROPERTY_ATTR_UNIT, ATTR_ID_REF, PREDICATE_PROPERTY_ATTR_ID_REF);
					}
				}
				else {
					streamDataProvider.setPropertyHasResource(false);
				}
				
				streamDataProvider.getCurrentEventCollection().add(
						new LiteralMetadataEvent(streamDataProvider.getEventReader().getID(EventContentType.META_LITERAL), null, predicate, LiteralContentSequenceType.SIMPLE));
				
				streamDataProvider.setCurrentPropertyDatatype(XMLUtils.readStringAttr(element, ATTR_DATATYPE, null));
				
				if (resetEventCollection && streamDataProvider.hasSpecialEventCollection()) {
					streamDataProvider.resetCurrentEventCollection();
				}
			}
		};
		
		AbstractXMLElementReader<PhyloXMLReaderStreamDataProvider> propertyEndReader = new AbstractXMLElementReader<PhyloXMLReaderStreamDataProvider>() {
			@Override
			public void readEvent(PhyloXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
				
				if (streamDataProvider.isPropertyHasResource()) {
					streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_RESOURCE));
				}
			}
		};
		
		PhyloXMLEndElementReader literalEndReader = new PhyloXMLEndElementReader(true, false, false);
		
		PhyloXMLEndElementReader resourceEndReader = new PhyloXMLEndElementReader(false, true, false);
		
		PhyloXMLEndElementReader resourceAndLiteralEndReader = new PhyloXMLEndElementReader(true, true, false);
		
		XMLElementReader<PhyloXMLReaderStreamDataProvider> emptyReader = new AbstractXMLElementReader<PhyloXMLReaderStreamDataProvider>() {			
			@Override
			public void readEvent(PhyloXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {}  //is used if no meta events should be read from a tag
		};
		
		putElementReader(new XMLElementReaderKey(null, null, XMLStreamConstants.START_DOCUMENT), 
			new AbstractXMLElementReader<PhyloXMLReaderStreamDataProvider>() {
				@Override
				public void readEvent(PhyloXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
					streamDataProvider.getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(EventContentType.DOCUMENT, EventTopologyType.START));
					streamDataProvider.getCurrentEventCollection().add(new LinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, 
							getID(EventContentType.TREE_NETWORK_GROUP), null, null));
				}
		});
		
		putElementReader(new XMLElementReaderKey(null, null, XMLStreamConstants.END_DOCUMENT), 
			new AbstractXMLElementReader<PhyloXMLReaderStreamDataProvider>() {
				@Override
				public void readEvent(PhyloXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
					streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.DOCUMENT));
				}
		});
		
		putElementReader(new XMLElementReaderKey(null, TAG_ROOT, XMLStreamConstants.START_ELEMENT), emptyReader);
		
		putElementReader(new XMLElementReaderKey(TAG_ROOT, null, XMLStreamConstants.CHARACTERS), new PhyloXMLNoCharactersAllowedElementReader());
		
		putElementReader(new XMLElementReaderKey(null, TAG_ROOT, XMLStreamConstants.END_ELEMENT), emptyReader);
		
		//PhyloXML.Phylogeny	
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
		
		putElementReader(new XMLElementReaderKey(TAG_PHYLOGENY, null, XMLStreamConstants.CHARACTERS), new PhyloXMLNoCharactersAllowedElementReader());
		
		putElementReader(new XMLElementReaderKey(TAG_ROOT, TAG_PHYLOGENY, XMLStreamConstants.END_ELEMENT), 
			new AbstractXMLElementReader<PhyloXMLReaderStreamDataProvider>() {
				@Override
				public void readEvent(PhyloXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
					streamDataProvider.getSourceNode().clear();
					streamDataProvider.getEdgeInfos().clear();
					streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.TREE));			
				}
		});
		
		//Phylogeny.Clade		
		putElementReader(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_CLADE, XMLStreamConstants.START_ELEMENT),
			new AbstractXMLElementReader<PhyloXMLReaderStreamDataProvider>() {
				@Override
				public void readEvent(PhyloXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {					
					String treeID = getID(EventContentType.TREE);
					String treeLabel = streamDataProvider.getTreeLabel();
					
					Collection<JPhyloIOEvent> nestedEvents = streamDataProvider.resetCurrentEventCollection();
					streamDataProvider.getCurrentEventCollection().add(new LinkedLabeledIDEvent(EventContentType.TREE, treeID, treeLabel, null));
					
					for (JPhyloIOEvent nextEvent : nestedEvents) {
						streamDataProvider.getCurrentEventCollection().add(nextEvent);
					}
					
					streamDataProvider.getSourceNode().add(new NodeEdgeInfo(getID(EventContentType.NODE), Double.NaN, new ArrayList<JPhyloIOEvent>()));
					streamDataProvider.setCreateNodeStart(true);
					
					streamDataProvider.getEdgeInfos().add(new NodeEdgeInfo(getID(EventContentType.EDGE), 
							XMLUtils.readDoubleAttr(event.asStartElement(), ATTR_BRANCH_LENGTH, Double.NaN), new ArrayList<JPhyloIOEvent>()));
					streamDataProvider.getEdgeInfos().peek().setSource(null);
					
					streamDataProvider.setCurrentEventCollection(streamDataProvider.getSourceNode().peek().getNestedEvents());
					
					readAttributes(streamDataProvider, event.asStartElement(), ATTR_ID_SOURCE, PREDICATE_ATTR_ID_SOURCE);
				}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_CLADE, null, XMLStreamConstants.CHARACTERS), new PhyloXMLNoCharactersAllowedElementReader());
		
		putElementReader(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_CLADE, XMLStreamConstants.END_ELEMENT), cladeEndReader);
		
		//Clade.Clade
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_CLADE, XMLStreamConstants.START_ELEMENT),
			new AbstractXMLElementReader<PhyloXMLReaderStreamDataProvider>() {
				@Override
				public void readEvent(PhyloXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
					String parentID = streamDataProvider.getSourceNode().peek().getID();
					
					if (streamDataProvider.hasSpecialEventCollection()) {
						streamDataProvider.resetCurrentEventCollection();
					}

					streamDataProvider.getSourceNode().add(new NodeEdgeInfo(getID(EventContentType.NODE), Double.NaN, new ArrayList<JPhyloIOEvent>()));
					streamDataProvider.setCreateNodeStart(true);
					streamDataProvider.getEdgeInfos().add(new NodeEdgeInfo(getID(EventContentType.EDGE), 
							XMLUtils.readDoubleAttr(event.asStartElement(), ATTR_BRANCH_LENGTH, Double.NaN), new ArrayList<JPhyloIOEvent>()));
					streamDataProvider.getEdgeInfos().peek().setSource(parentID);
					
					streamDataProvider.setCurrentEventCollection(streamDataProvider.getSourceNode().peek().getNestedEvents());
					
					readAttributes(streamDataProvider, event.asStartElement(), ATTR_ID_SOURCE, PREDICATE_ATTR_ID_SOURCE);
				}
		});
		
		//Element reader for character content of clade tag was registered before
		
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_CLADE, XMLStreamConstants.END_ELEMENT), cladeEndReader);
		
		//Phylogeny.Name
		putElementReader(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_NAME, XMLStreamConstants.START_ELEMENT), emptyReader);
		
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
								useAsMeta = true;
								if (currentNodeLabel == null) {
									currentNode.setLabel(value);
								}
							}
						}
						
						if (useAsMeta) {							
							boolean isContinued = streamDataProvider.getEventReader().peek().getType().equals(XMLStreamConstants.CHARACTERS);
							streamDataProvider.getCurrentEventCollection().add(new LiteralMetadataContentEvent(
									new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), event.asCharacters().getData(), isContinued));
						}
					}
			});		
		
		putElementReader(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_NAME, XMLStreamConstants.END_ELEMENT), emptyReader);
		
		//Phylogeny.ID
		putElementReader(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_ID, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(PREDICATE_PHYLOGENY_ID_VALUE, PREDICATE_PHYLOGENY_ID, false, ATTR_ID_PROVIDER, PREDICATE_PHYLOGENY_ID_ATTR_PROVIDER));
		putElementReader(new XMLElementReaderKey(TAG_ID, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_ID, XMLStreamConstants.END_ELEMENT), resourceAndLiteralEndReader);
		
		//Phylogeny.Description
		putElementReader(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_DESCRIPTION, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_PHYLOGENY_DESCRIPTION, null, false));
		putElementReader(new XMLElementReaderKey(TAG_DESCRIPTION, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_DESCRIPTION, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		//Phylogeny.Date
		putElementReader(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_DATE, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_PHYLOGENY_DATE, null, false));
		putElementReader(new XMLElementReaderKey(TAG_DATE, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_TOKEN)); //TODO use constant for dateTime data type
		putElementReader(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_DATE, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		//Phylogeny.Confidence
		putElementReader(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_CONFIDENCE, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(PREDICATE_CONFIDENCE_VALUE, PREDICATE_CONFIDENCE, false, ATTR_TYPE, PREDICATE_CONFIDENCE_ATTR_TYPE));
		putElementReader(new XMLElementReaderKey(TAG_CONFIDENCE, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_TOKEN)); //TODO use constant for double data type
		putElementReader(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_CONFIDENCE, XMLStreamConstants.END_ELEMENT), resourceAndLiteralEndReader);		
		
		//Phylogeny.CladeRelation
		putElementReader(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_CLADE_RELATION, XMLStreamConstants.START_ELEMENT),
			new AbstractXMLElementReader<PhyloXMLReaderStreamDataProvider>() {
				@Override
				public void readEvent(PhyloXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
					StartElement element = event.asStartElement();					
					String cladeID0 = XMLUtils.readStringAttr(element, ATTR_ID_REF_0, null);
					String cladeID1 = XMLUtils.readStringAttr(element, ATTR_ID_REF_1, null);
					
					if ((cladeID0 != null) && (cladeID1 != null)) {
						String eventID0 = streamDataProvider.getCladeIDToNodeEventIDMap().get(cladeID0);
						String eventID1 = streamDataProvider.getCladeIDToNodeEventIDMap().get(cladeID1);
						
						if ((eventID0 != null) && (eventID1 != null)) {
							getStreamDataProvider().getCurrentEventCollection().add(new EdgeEvent(getID(EventContentType.EDGE), null, 
									eventID0, eventID1, Double.NaN));
							getStreamDataProvider().getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.EDGE));
						}
					}
					else {
						throw new JPhyloIOReaderException("No valid edge was referenced by a clade relation element. Both the source and target node must not be null.", event.getLocation());
					}
					
					streamDataProvider.getCurrentEventCollection().add(
							new ResourceMetadataEvent(streamDataProvider.getEventReader().getID(EventContentType.META_RESOURCE), null, new URIOrStringIdentifier(null, PREDICATE_CLADE_REL), null, null));
					
					readAttributes(streamDataProvider, event.asStartElement(), ATTR_DISTANCE, PREDICATE_CLADE_REL_ATTR_DISTANCE, ATTR_TYPE, PREDICATE_CLADE_REL_ATTR_TYPE);					
				}
		});		
		
		putElementReader(new XMLElementReaderKey(TAG_CLADE_RELATION, TAG_CONFIDENCE, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(PREDICATE_CONFIDENCE_VALUE, PREDICATE_CONFIDENCE, false, ATTR_TYPE, PREDICATE_CONFIDENCE_ATTR_TYPE));
		//Element reader for character content of confidence tag was registered before
		putElementReader(new XMLElementReaderKey(TAG_CLADE_RELATION, TAG_CONFIDENCE, XMLStreamConstants.END_ELEMENT), resourceAndLiteralEndReader);		
		
		putElementReader(new XMLElementReaderKey(TAG_CLADE_RELATION, null, XMLStreamConstants.CHARACTERS), new PhyloXMLNoCharactersAllowedElementReader());
		
		putElementReader(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_CLADE_RELATION, XMLStreamConstants.END_ELEMENT), resourceEndReader);
		
		//Phylogeny.SequenceRelation
		putElementReader(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_SEQUENCE_RELATION, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(null, PREDICATE_SEQ_REL, false, ATTR_ID_REF_0, PREDICATE_SEQ_REL_ATTR_IDREF0, ATTR_ID_REF_1, PREDICATE_SEQ_REL_ATTR_IDREF1, 
						ATTR_DISTANCE, PREDICATE_SEQ_REL_ATTR_DISTANCE, ATTR_TYPE, PREDICATE_SEQ_REL_ATTR_TYPE));
		putElementReader(new XMLElementReaderKey(TAG_SEQUENCE_RELATION, null, XMLStreamConstants.CHARACTERS), new PhyloXMLNoCharactersAllowedElementReader());
		
		putElementReader(new XMLElementReaderKey(TAG_SEQUENCE_RELATION, TAG_CONFIDENCE, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(PREDICATE_SEQ_REL_CONFIDENCE_VALUE, PREDICATE_SEQ_REL_CONFIDENCE, false, ATTR_TYPE, PREDICATE_SEQ_REL_CONFIDENCE_ATTR_TYPE));
		//Element reader for character content of confidence tag was registered before
		putElementReader(new XMLElementReaderKey(TAG_SEQUENCE_RELATION, TAG_CONFIDENCE, XMLStreamConstants.END_ELEMENT), resourceAndLiteralEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_SEQUENCE_RELATION, XMLStreamConstants.END_ELEMENT), resourceEndReader);
		
		//Phylogeny.Property
		putElementReader(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_PROPERTY, XMLStreamConstants.START_ELEMENT), propertyStartReader);
		
		putElementReader(new XMLElementReaderKey(TAG_PROPERTY, null, XMLStreamConstants.CHARACTERS),
				new AbstractXMLElementReader<PhyloXMLReaderStreamDataProvider>() {
					@Override
					public void readEvent(PhyloXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {					
						boolean isContinued = streamDataProvider.getEventReader().peek().getType().equals(XMLStreamConstants.CHARACTERS);
						URIOrStringIdentifier datatype = new URIOrStringIdentifier(null, qNameFromCURIE(streamDataProvider.getCurrentPropertyDatatype(), streamDataProvider));
						
						streamDataProvider.getCurrentEventCollection().add(new LiteralMetadataContentEvent(datatype, event.asCharacters().getData(), isContinued));
					}
			});
		
		putElementReader(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_PROPERTY, XMLStreamConstants.END_ELEMENT), propertyEndReader);
		
		//Clade.Name
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_NAME, XMLStreamConstants.START_ELEMENT), emptyReader);
		//Element reader for character content of name tag was registered before
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_NAME, XMLStreamConstants.END_ELEMENT), emptyReader);
		
		//Clade.BranchLength
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_BRANCH_LENGTH, XMLStreamConstants.START_ELEMENT), emptyReader);		
		
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
						
						if (Double.isNaN(currentEdgeLength)) {								
							currentEdge.setLength(newEdgeLength);
						}
						else if (Double.compare(newEdgeLength, currentEdgeLength) != 0) {
							getParameters().getLogger().addWarning("Two different branch lengths of \"" + currentEdgeLength + "\" and \"" + newEdgeLength 
									+ "\" are present for the same branch in the document.");
						}
					}
				}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_BRANCH_LENGTH, XMLStreamConstants.END_ELEMENT), emptyReader);
		
		//Clade.Confidence
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_CONFIDENCE, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(PREDICATE_CONFIDENCE_VALUE, PREDICATE_CONFIDENCE, true, ATTR_TYPE, PREDICATE_CONFIDENCE_ATTR_TYPE));
		//Element reader for character content of confidence tag was registered before
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_CONFIDENCE, XMLStreamConstants.END_ELEMENT), new PhyloXMLEndElementReader(true, true, true));
		
		//Clade.Width
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_BRANCH_WIDTH, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_WIDTH, null, true));
		putElementReader(new XMLElementReaderKey(TAG_BRANCH_WIDTH, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_TOKEN)); //TODO use constant for double data type
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_BRANCH_WIDTH, XMLStreamConstants.END_ELEMENT), new PhyloXMLEndElementReader(true, false, true));
		
		//Clade.Color
		//TODO Read single Color object (Possibly use PhyloXML specific object translator.)
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_BRANCH_COLOR, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(null, PREDICATE_COLOR, true));
		putElementReader(new XMLElementReaderKey(TAG_BRANCH_COLOR, null, XMLStreamConstants.CHARACTERS), new PhyloXMLNoCharactersAllowedElementReader());
		
		putElementReader(new XMLElementReaderKey(TAG_BRANCH_COLOR, TAG_RED, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_COLOR_RED, null, true));
		putElementReader(new XMLElementReaderKey(TAG_RED, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_BRANCH_COLOR, TAG_RED, XMLStreamConstants.END_ELEMENT), new PhyloXMLEndElementReader(true, false, true));
		
		putElementReader(new XMLElementReaderKey(TAG_BRANCH_COLOR, TAG_GREEN, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_COLOR_GREEN, null, true));
		putElementReader(new XMLElementReaderKey(TAG_GREEN, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_BRANCH_COLOR, TAG_GREEN, XMLStreamConstants.END_ELEMENT), new PhyloXMLEndElementReader(true, false, true));
		
		putElementReader(new XMLElementReaderKey(TAG_BRANCH_COLOR, TAG_BLUE, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_COLOR_BLUE, null, true));
		putElementReader(new XMLElementReaderKey(TAG_BLUE, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_BRANCH_COLOR, TAG_BLUE, XMLStreamConstants.END_ELEMENT), new PhyloXMLEndElementReader(true, false, true));
		
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_BRANCH_COLOR, XMLStreamConstants.END_ELEMENT), new PhyloXMLEndElementReader(false, true, true));
		
		//Clade.NodeID
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_NODE_ID, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(PREDICATE_NODE_ID_VALUE, PREDICATE_NODE_ID, false, ATTR_ID_PROVIDER, PREDICATE_NODE_ID_ATTR_PROVIDER));		
		
		putElementReader(new XMLElementReaderKey(TAG_NODE_ID, null, XMLStreamConstants.CHARACTERS),
			new AbstractXMLElementReader<PhyloXMLReaderStreamDataProvider>() {
				@Override
				public void readEvent(PhyloXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
					String value = event.asCharacters().getData();
					
					streamDataProvider.setLastNodeID(value);
					
					boolean isContinued = streamDataProvider.getEventReader().peek().getType().equals(XMLStreamConstants.CHARACTERS);
					streamDataProvider.getCurrentEventCollection().add(new LiteralMetadataContentEvent(
							new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), event.asCharacters().getData(), isContinued));
				}
			});
		
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_NODE_ID, XMLStreamConstants.END_ELEMENT), resourceAndLiteralEndReader);	
		
		//Clade.Taxonomy
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_TAXONOMY, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(null, PREDICATE_TAXONOMY, false, ATTR_ID_SOURCE, PREDICATE_TAXONOMY_ATTR_ID_SOURCE));
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, null, XMLStreamConstants.CHARACTERS), new PhyloXMLNoCharactersAllowedElementReader());
		
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_ID, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(PREDICATE_TAXONOMY_ID_VALUE, PREDICATE_TAXONOMY_ID, false, ATTR_ID_PROVIDER, PREDICATE_TAXONOMY_ID_ATTR_PROVIDER));
		putElementReader(new XMLElementReaderKey(TAG_ID, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_ID, XMLStreamConstants.END_ELEMENT), resourceAndLiteralEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_CODE, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_TAXONOMY_CODE, null, false));
		putElementReader(new XMLElementReaderKey(TAG_CODE, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_CODE, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_SCI_NAME, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(PREDICATE_TAXONOMY_SCIENTIFIC_NAME, null, false));
		putElementReader(new XMLElementReaderKey(TAG_SCI_NAME, null, XMLStreamConstants.CHARACTERS), nodeLabelReader);
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_SCI_NAME, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_AUTHORITY, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(PREDICATE_TAXONOMY_AUTHORITY, null, false));
		putElementReader(new XMLElementReaderKey(TAG_AUTHORITY, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_AUTHORITY, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_COMMON_NAME, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(PREDICATE_TAXONOMY_COMMON_NAME, null, false));
		putElementReader(new XMLElementReaderKey(TAG_COMMON_NAME, null, XMLStreamConstants.CHARACTERS), nodeLabelReader);
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_COMMON_NAME, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_SYNONYM, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(PREDICATE_TAXONOMY_SYNONYM, null, false));
		putElementReader(new XMLElementReaderKey(TAG_SYNONYM, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_SYNONYM, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_RANK, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(PREDICATE_TAXONOMY_RANK, null, false));
		putElementReader(new XMLElementReaderKey(TAG_RANK, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_RANK, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_URI, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(null, PREDICATE_TAXONOMY_URI, false, ATTR_DESC, PREDICATE_TAXONOMY_URI_ATTR_DESC, ATTR_TYPE, PREDICATE_TAXONOMY_URI_ATTR_TYPE));
		
		putElementReader(new XMLElementReaderKey(TAG_URI, null, XMLStreamConstants.CHARACTERS),
			new AbstractXMLElementReader<PhyloXMLReaderStreamDataProvider>() {
				@Override
				public void readEvent(PhyloXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
					String uri = event.asCharacters().getData();
					boolean isContinued = streamDataProvider.getEventReader().peek().getType().equals(XMLStreamConstants.CHARACTERS);
					URI externalResource = null;
					
					if (streamDataProvider.getIncompleteToken() != null) {
						uri = streamDataProvider.getIncompleteToken() + uri;
					}
					
					if (!isContinued) {
						try {
							externalResource = new URI(uri);
							streamDataProvider.getCurrentEventCollection().add(new ResourceMetadataEvent(getID(EventContentType.META_RESOURCE), null, 
									new URIOrStringIdentifier(null, PREDICATE_TAXONOMY_URI_VALUE), externalResource, null));
							streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_RESOURCE));
		  			}
		  			catch (URISyntaxException e) {
		  				throw new JPhyloIOReaderException("A URI element must specify a valid URI. Instead the string\"" + uri + "\" was given.", event.getLocation());
		  			}
					}
					else {					
						streamDataProvider.setIncompleteToken(uri);
					}
				}
			});
		
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_URI, XMLStreamConstants.END_ELEMENT), resourceAndLiteralEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_TAXONOMY, XMLStreamConstants.END_ELEMENT), resourceEndReader);		
		
		//Clade.Sequence
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_SEQUENCE, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(null, PREDICATE_SEQUENCE, false, ATTR_TYPE, PREDICATE_SEQUENCE_ATTR_TYPE, ATTR_ID_SOURCE, PREDICATE_SEQUENCE_ATTR_ID_SOURCE, 
						ATTR_ID_REF, PREDICATE_SEQUENCE_ATTR_ID_REF));
		putElementReader(new XMLElementReaderKey(TAG_SEQUENCE, null, XMLStreamConstants.CHARACTERS), new PhyloXMLNoCharactersAllowedElementReader());
		
		putElementReader(new XMLElementReaderKey(TAG_SEQUENCE, TAG_SYMBOL, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_SEQUENCE_SYMBOL, null, false));
		putElementReader(new XMLElementReaderKey(TAG_SYMBOL, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_SEQUENCE, TAG_SYMBOL, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_SEQUENCE, TAG_ACCESSION, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(PREDICATE_SEQUENCE_ACCESSION_VALUE, PREDICATE_SEQUENCE_ACCESSION, false, ATTR_SOURCE, PREDICATE_SEQUENCE_ACCESSION_ATTR_SOURCE));		
		putElementReader(new XMLElementReaderKey(TAG_ACCESSION, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_SEQUENCE, TAG_ACCESSION, XMLStreamConstants.END_ELEMENT), resourceAndLiteralEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_SEQUENCE, TAG_LOCATION, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_SEQUENCE_LOCATION, null, false));
		putElementReader(new XMLElementReaderKey(TAG_LOCATION, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_SEQUENCE, TAG_LOCATION, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_SEQUENCE, TAG_NAME, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_SEQUENCE_NAME, null, false));
		//Element reader for character content of name tag was registered before
		putElementReader(new XMLElementReaderKey(TAG_SEQUENCE, TAG_NAME, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_SEQUENCE, TAG_MOL_SEQ, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(PREDICATE_SEQUENCE_MOL_SEQ_VALUE, PREDICATE_SEQUENCE_MOL_SEQ, false, ATTR_IS_ALIGNED, PREDICATE_SEQUENCE_MOL_SEQ_ATTR_IS_ALIGNED));		
		putElementReader(new XMLElementReaderKey(TAG_MOL_SEQ, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_SEQUENCE, TAG_MOL_SEQ, XMLStreamConstants.END_ELEMENT), resourceAndLiteralEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_SEQUENCE, TAG_URI, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(PREDICATE_SEQUENCE_URI_VALUE, PREDICATE_SEQUENCE_URI, false, ATTR_DESC, PREDICATE_SEQUENCE_URI_ATTR_DESC, 
						ATTR_TYPE, PREDICATE_SEQUENCE_URI_ATTR_TYPE));
		//Element reader for character content of URI tag was registered before
		putElementReader(new XMLElementReaderKey(TAG_SEQUENCE, TAG_URI, XMLStreamConstants.END_ELEMENT), resourceAndLiteralEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_SEQUENCE, TAG_ANNOTATION, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(null, PREDICATE_ANNOTATION, false, ATTR_REF, PREDICATE_ANNOTATION_ATTR_REF, ATTR_SOURCE, PREDICATE_ANNOTATION_ATTR_SOURCE, 
						ATTR_EVIDENCE, PREDICATE_ANNOTATION_ATTR_EVIDENCE, ATTR_TYPE, PREDICATE_ANNOTATION_ATTR_TYPE));
		putElementReader(new XMLElementReaderKey(TAG_ANNOTATION, null, XMLStreamConstants.CHARACTERS), new PhyloXMLNoCharactersAllowedElementReader());
		
		putElementReader(new XMLElementReaderKey(TAG_ANNOTATION, TAG_DESC, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_ANNOTATION_DESC, null, false));
		putElementReader(new XMLElementReaderKey(TAG_DESC, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_ANNOTATION, TAG_DESC, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_ANNOTATION, TAG_CONFIDENCE, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(PREDICATE_ANNOTATION_CONFIDENCE_VALUE, PREDICATE_ANNOTATION_CONFIDENCE, false, ATTR_TYPE, PREDICATE_ANNOTATION_CONFIDENCE_ATTR_TYPE));
		//Element reader for character content of confidence tag was registered before
		putElementReader(new XMLElementReaderKey(TAG_ANNOTATION, TAG_CONFIDENCE, XMLStreamConstants.END_ELEMENT), resourceAndLiteralEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_ANNOTATION, TAG_PROPERTY, XMLStreamConstants.START_ELEMENT), propertyStartReader);
		//Element reader for character content of property tag was registered before
		putElementReader(new XMLElementReaderKey(TAG_ANNOTATION, TAG_PROPERTY, XMLStreamConstants.END_ELEMENT), propertyEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_ANNOTATION, TAG_URI, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(PREDICATE_ANNOTATION_URI_VALUE, PREDICATE_ANNOTATION_URI, false, ATTR_DESC, PREDICATE_ANNOTATION_URI_ATTR_DESC, 
						ATTR_TYPE, PREDICATE_ANNOTATION_URI_ATTR_TYPE));
		//Element reader for character content of URI tag was registered before
		putElementReader(new XMLElementReaderKey(TAG_ANNOTATION, TAG_URI, XMLStreamConstants.END_ELEMENT), resourceAndLiteralEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_SEQUENCE, TAG_ANNOTATION, XMLStreamConstants.END_ELEMENT), resourceEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_SEQUENCE, TAG_DOMAIN_ARCHITECTURE, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(null, PREDICATE_DOMAIN_ARCHITECTURE, false, ATTR_LENGTH, PREDICATE_DOMAIN_ARCHITECTURE_ATTR_LENGTH));
		putElementReader(new XMLElementReaderKey(TAG_DOMAIN_ARCHITECTURE, null, XMLStreamConstants.CHARACTERS), new PhyloXMLNoCharactersAllowedElementReader());
		
		putElementReader(new XMLElementReaderKey(TAG_DOMAIN_ARCHITECTURE, TAG_DOMAIN, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(PREDICATE_DOMAIN_ARCHITECTURE_DOMAIN_VALUE, PREDICATE_DOMAIN_ARCHITECTURE_DOMAIN, false, ATTR_FROM, PREDICATE_DOMAIN_ARCHITECTURE_DOMAIN_ATTR_FROM,
						ATTR_TO, PREDICATE_DOMAIN_ARCHITECTURE_DOMAIN_ATTR_TO, ATTR_CONFIDENCE, PREDICATE_DOMAIN_ARCHITECTURE_DOMAIN_ATTR_CONFIDENCE, ATTR_ID, PREDICATE_DOMAIN_ARCHITECTURE_DOMAIN_ATTR_ID));
		putElementReader(new XMLElementReaderKey(TAG_DOMAIN, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_DOMAIN_ARCHITECTURE, TAG_DOMAIN, XMLStreamConstants.END_ELEMENT), resourceAndLiteralEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_SEQUENCE, TAG_DOMAIN_ARCHITECTURE, XMLStreamConstants.END_ELEMENT), resourceEndReader);		
		
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_SEQUENCE, XMLStreamConstants.END_ELEMENT), resourceEndReader);
		
		//Clade.Events
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_EVENTS, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(null, PREDICATE_EVENTS, false));
		putElementReader(new XMLElementReaderKey(TAG_EVENTS, null, XMLStreamConstants.CHARACTERS), new PhyloXMLNoCharactersAllowedElementReader());
		
		putElementReader(new XMLElementReaderKey(TAG_EVENTS, TAG_TYPE, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_EVENTS_TYPE, null, false));
		putElementReader(new XMLElementReaderKey(TAG_TYPE, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(DATA_TYPE_EVENTTYPE));
		putElementReader(new XMLElementReaderKey(TAG_EVENTS, TAG_TYPE, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_EVENTS, TAG_DUPLICATIONS, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_EVENTS_DUPLICATIONS, null, false));
		putElementReader(new XMLElementReaderKey(TAG_DUPLICATIONS, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_EVENTS, TAG_DUPLICATIONS, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_EVENTS, TAG_SPECIATIONS, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_EVENTS_SPECIATIONS, null, false));
		putElementReader(new XMLElementReaderKey(TAG_SPECIATIONS, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_EVENTS, TAG_SPECIATIONS, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_EVENTS, TAG_LOSSES, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_EVENTS_LOSSES, null, false));
		putElementReader(new XMLElementReaderKey(TAG_LOSSES, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_EVENTS, TAG_LOSSES, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_EVENTS, TAG_CONFIDENCE, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(PREDICATE_EVENTS_CONFIDENCE_VALUE, PREDICATE_EVENTS_CONFIDENCE, false, ATTR_TYPE, PREDICATE_EVENTS_CONFIDENCE_ATTR_TYPE));
		//Element reader for character content of confidence tag was registered before
		putElementReader(new XMLElementReaderKey(TAG_EVENTS, TAG_CONFIDENCE, XMLStreamConstants.END_ELEMENT), resourceAndLiteralEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_EVENTS, XMLStreamConstants.END_ELEMENT), resourceEndReader);
		
		//Clade.BinaryCharacters
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_BINARY_CHARACTERS, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(null, PREDICATE_BINARY_CHARACTERS, false, ATTR_TYPE, PREDICATE_BINARY_CHARACTERS_ATTR_TYPE, ATTR_GAINED_COUNT, PREDICATE_BINARY_CHARACTERS_ATTR_GAINED_COUNT,
						ATTR_LOST_COUNT, PREDICATE_BINARY_CHARACTERS_ATTR_LOST_COUNT, ATTR_PRESENT_COUNT, PREDICATE_BINARY_CHARACTERS_ATTR_PRESENT_COUNT, ATTR_ABSENT_COUNT, PREDICATE_BINARY_CHARACTERS_ATTR_ABSENT_COUNT));
		putElementReader(new XMLElementReaderKey(TAG_EVENTS, null, XMLStreamConstants.CHARACTERS), new PhyloXMLNoCharactersAllowedElementReader());
		
		putElementReader(new XMLElementReaderKey(TAG_BINARY_CHARACTERS, TAG_GAINED, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(null, PREDICATE_BINARY_CHARACTERS_GAINED, false));
		putElementReader(new XMLElementReaderKey(TAG_GAINED, null, XMLStreamConstants.CHARACTERS), new PhyloXMLNoCharactersAllowedElementReader());
		
		putElementReader(new XMLElementReaderKey(TAG_GAINED, TAG_BC, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_BINARY_CHARACTERS_GAINED_BC, null, false));
		putElementReader(new XMLElementReaderKey(TAG_BC, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(DATA_TYPE_EVENTTYPE));
		putElementReader(new XMLElementReaderKey(TAG_GAINED, TAG_BC, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_BINARY_CHARACTERS, TAG_GAINED, XMLStreamConstants.END_ELEMENT), resourceEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_BINARY_CHARACTERS, TAG_LOST, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(null, PREDICATE_BINARY_CHARACTERS_LOST, false));
		putElementReader(new XMLElementReaderKey(TAG_LOST, null, XMLStreamConstants.CHARACTERS), new PhyloXMLNoCharactersAllowedElementReader());
		
		putElementReader(new XMLElementReaderKey(TAG_LOST, TAG_BC, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_BINARY_CHARACTERS_LOST_BC, null, false));
		//Element reader for character content of BC tag was registered before
		putElementReader(new XMLElementReaderKey(TAG_LOST, TAG_BC, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_BINARY_CHARACTERS, TAG_LOST, XMLStreamConstants.END_ELEMENT), resourceEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_BINARY_CHARACTERS, TAG_PRESENT, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(null, PREDICATE_BINARY_CHARACTERS_PRESENT, false));
		putElementReader(new XMLElementReaderKey(TAG_PRESENT, null, XMLStreamConstants.CHARACTERS), new PhyloXMLNoCharactersAllowedElementReader());
		
		putElementReader(new XMLElementReaderKey(TAG_PRESENT, TAG_BC, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_BINARY_CHARACTERS_PRESENT_BC, null, false));
		//Element reader for character content of BC tag was registered before
		putElementReader(new XMLElementReaderKey(TAG_PRESENT, TAG_BC, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_BINARY_CHARACTERS, TAG_PRESENT, XMLStreamConstants.END_ELEMENT), resourceEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_BINARY_CHARACTERS, TAG_ABSENT, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(null, PREDICATE_BINARY_CHARACTERS_ABSENT, false));
		putElementReader(new XMLElementReaderKey(TAG_ABSENT, null, XMLStreamConstants.CHARACTERS), new PhyloXMLNoCharactersAllowedElementReader());
		
		putElementReader(new XMLElementReaderKey(TAG_ABSENT, TAG_BC, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_BINARY_CHARACTERS_ABSENT_BC, null, false));
		//Element reader for character content of BC tag was registered before
		putElementReader(new XMLElementReaderKey(TAG_ABSENT, TAG_BC, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_BINARY_CHARACTERS, TAG_ABSENT, XMLStreamConstants.END_ELEMENT), resourceEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_BINARY_CHARACTERS, XMLStreamConstants.END_ELEMENT), resourceEndReader);
		
		//Clade.Distribution
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_DISTRIBUTION, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(null, PREDICATE_DISTRIBUTION, false));
		putElementReader(new XMLElementReaderKey(TAG_DISTRIBUTION, null, XMLStreamConstants.CHARACTERS), new PhyloXMLNoCharactersAllowedElementReader());
		
		putElementReader(new XMLElementReaderKey(TAG_DISTRIBUTION, TAG_DESC, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_DISTRIBUTION_DESC, null, false));
		putElementReader(new XMLElementReaderKey(TAG_DESC, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_DISTRIBUTION, TAG_DESC, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_DISTRIBUTION, TAG_POINT, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(null, PREDICATE_DISTRIBUTION_POINT, false, ATTR_GEO_DATUM, PREDICATE_DISTRIBUTION_POINT_GEODETIC_DATUM, 
						ATTR_ALT_UNIT, PREDICATE_DISTRIBUTION_POINT_ALT_UNIT));
		putElementReader(new XMLElementReaderKey(TAG_POINT, null, XMLStreamConstants.CHARACTERS), new PhyloXMLNoCharactersAllowedElementReader());
		
		putElementReader(new XMLElementReaderKey(TAG_POINT, TAG_LAT, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_DISTRIBUTION_POINT_LAT, null, false));
		putElementReader(new XMLElementReaderKey(TAG_LAT, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_POINT, TAG_LAT, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_POINT, TAG_LONG, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_DISTRIBUTION_POINT_LONG, null, false));
		putElementReader(new XMLElementReaderKey(TAG_LONG, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_POINT, TAG_LONG, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_POINT, TAG_ALT, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_DISTRIBUTION_POINT_ALT, null, false));
		putElementReader(new XMLElementReaderKey(TAG_ALT, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_POINT, TAG_ALT, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_DISTRIBUTION, TAG_POINT, XMLStreamConstants.END_ELEMENT), resourceEndReader);		

		putElementReader(new XMLElementReaderKey(TAG_DISTRIBUTION, TAG_POLYGON, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(null, PREDICATE_DISTRIBUTION_POLYGON, false));
		putElementReader(new XMLElementReaderKey(TAG_POLYGON, null, XMLStreamConstants.CHARACTERS), new PhyloXMLNoCharactersAllowedElementReader());
		
		putElementReader(new XMLElementReaderKey(TAG_POLYGON, TAG_POINT, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(null, PREDICATE_DISTRIBUTION_POLYGON_POINT, false, ATTR_GEO_DATUM, PREDICATE_DISTRIBUTION_POLYGON_POINT_GEODETIC_DATUM, 
						ATTR_ALT_UNIT, PREDICATE_DISTRIBUTION_POLYGON_POINT_ALT_UNIT));
		//Element reader for character content of point tag was registered before
		
		putElementReader(new XMLElementReaderKey(TAG_POINT, TAG_LAT, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_DISTRIBUTION_POLYGON_POINT_LAT, null, false));
		putElementReader(new XMLElementReaderKey(TAG_LAT, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_POINT, TAG_LAT, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_POINT, TAG_LONG, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_DISTRIBUTION_POLYGON_POINT_LONG, null, false));
		putElementReader(new XMLElementReaderKey(TAG_LONG, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_POINT, TAG_LONG, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_POINT, TAG_ALT, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_DISTRIBUTION_POLYGON_POINT_ALT, null, false));
		putElementReader(new XMLElementReaderKey(TAG_ALT, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_POINT, TAG_ALT, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_POLYGON, TAG_POINT, XMLStreamConstants.END_ELEMENT), resourceEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_DISTRIBUTION, XMLStreamConstants.END_ELEMENT), resourceEndReader);		
		
		//Clade.Date
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_DATE, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(null, PREDICATE_DATE, false, ATTR_UNIT, PREDICATE_DATE_ATTR_UNIT));
		putElementReader(new XMLElementReaderKey(TAG_DATE, null, XMLStreamConstants.CHARACTERS), new PhyloXMLNoCharactersAllowedElementReader());
		
		putElementReader(new XMLElementReaderKey(TAG_DATE, TAG_DESC, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_DATE_DESC, null, false));
		putElementReader(new XMLElementReaderKey(TAG_DESC, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_DATE, TAG_DESC, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_DATE, TAG_VALUE, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_DATE_VALUE, null, false));
		putElementReader(new XMLElementReaderKey(TAG_VALUE, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_DATE, TAG_VALUE, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_DATE, TAG_MINIMUM, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_DATE_MINIMUM, null, false));
		putElementReader(new XMLElementReaderKey(TAG_MINIMUM, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_DATE, TAG_MINIMUM, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_DATE, TAG_MAXIMUM, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_DATE_MAXIMUM, null, false));
		putElementReader(new XMLElementReaderKey(TAG_MAXIMUM, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_DATE, TAG_MAXIMUM, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_DATE, XMLStreamConstants.END_ELEMENT), resourceEndReader);
		
		//Clade.Reference
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_REFERENCE, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(PREDICATE_REFERENCE_VALUE, PREDICATE_REFERENCE, false, ATTR_DOI, PREDICATE_REFERENCE_ATTR_DOI));
		putElementReader(new XMLElementReaderKey(TAG_REFERENCE, null, XMLStreamConstants.CHARACTERS), new PhyloXMLNoCharactersAllowedElementReader());
		
		putElementReader(new XMLElementReaderKey(TAG_REFERENCE, TAG_DESC, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_REFERENCE_DESC, null, false));
		putElementReader(new XMLElementReaderKey(TAG_DESC, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(DATA_TYPE_EVENTTYPE));
		putElementReader(new XMLElementReaderKey(TAG_REFERENCE, TAG_DESC, XMLStreamConstants.END_ELEMENT), literalEndReader);	
		
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_REFERENCE, XMLStreamConstants.END_ELEMENT), resourceEndReader);
		
		//Clade.Property
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_PROPERTY, XMLStreamConstants.START_ELEMENT), propertyStartReader);
		//Element reader for character content of property tag was registered before
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_PROPERTY, XMLStreamConstants.END_ELEMENT), propertyEndReader);
		
		//CustomXML
		putElementReader(new XMLElementReaderKey(null, null, XMLStreamConstants.START_ELEMENT), 
			new AbstractXMLElementReader<PhyloXMLReaderStreamDataProvider>() {
				@Override
				public void readEvent(PhyloXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
					StartElement element = event.asStartElement();
	
					if (streamDataProvider.getParentName().equals(TAG_CLADE)) {
						streamDataProvider.getEventReader().createNodeEvents(streamDataProvider);
						streamDataProvider.setCreateNodeStart(false);
					}
					
					if (streamDataProvider.getNestedMetaNames().isEmpty()) {
						streamDataProvider.getCurrentEventCollection().add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + streamDataProvider.getIDManager().createNewID(), 
								null, new URIOrStringIdentifier(null, element.getName()), LiteralContentSequenceType.XML));
					}
					
					streamDataProvider.getCurrentEventCollection().add(new LiteralMetadataContentEvent(event, false));
					
					streamDataProvider.getNestedMetaNames().add(element.getName().getLocalPart());
				}
		});
		
		putElementReader(new XMLElementReaderKey(null, null, XMLStreamConstants.CHARACTERS), 
				new AbstractXMLElementReader<PhyloXMLReaderStreamDataProvider>() {
					@Override
					public void readEvent(PhyloXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
						String value = event.asCharacters().getData();
						
						if (!value.matches("\\s+")) {
							boolean isContinued = streamDataProvider.getEventReader().peek().getType().equals(XMLStreamConstants.CHARACTERS);
							streamDataProvider.getCurrentEventCollection().add(new LiteralMetadataContentEvent(event, isContinued));
						}
					}
			});
	
		putElementReader(new XMLElementReaderKey(null, null, XMLStreamConstants.END_ELEMENT), 
			new AbstractXMLElementReader<PhyloXMLReaderStreamDataProvider>() {
				@Override
				public void readEvent(PhyloXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
					streamDataProvider.getCurrentEventCollection().add(new LiteralMetadataContentEvent(event, false));
					streamDataProvider.getNestedMetaNames().pop();
					
					if (streamDataProvider.getNestedMetaNames().isEmpty()) {
						streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
					}
				}
		});
	
		//Comments
		putElementReader(new XMLElementReaderKey(null, null, XMLStreamConstants.COMMENT), new CommentElementReader());
	}
	
	
	public void createNodeEvents(PhyloXMLReaderStreamDataProvider streamDataProvider) {
		if (streamDataProvider.hasSpecialEventCollection()) {
			streamDataProvider.resetCurrentEventCollection();
		}
		
		if (streamDataProvider.isCreateNodeStart()) {
			NodeEdgeInfo nodeInfo = streamDataProvider.getSourceNode().peek();
			
			streamDataProvider.getCladeIDToNodeEventIDMap().put(streamDataProvider.getLastNodeID(), nodeInfo.getID());
			
			getStreamDataProvider().getCurrentEventCollection().add(new LinkedLabeledIDEvent(EventContentType.NODE, nodeInfo.getID(), nodeInfo.getLabel(), null));
			for (JPhyloIOEvent nextEvent : nodeInfo.getNestedEvents()) {
				getStreamDataProvider().getCurrentEventCollection().add(nextEvent);
			}
		}		
	}
	
	
	private void createEdgeEvents(PhyloXMLReaderStreamDataProvider streamDataProvider) {
		NodeEdgeInfo edgeInfo = streamDataProvider.getEdgeInfos().pop();
		edgeInfo.setTarget(streamDataProvider.getSourceNode().peek().getID());
		
		getStreamDataProvider().getCurrentEventCollection().add(new EdgeEvent(edgeInfo.getID(), edgeInfo.getLabel(), edgeInfo.getSource(), 
				edgeInfo.getTarget(), edgeInfo.getLength()));
		for (JPhyloIOEvent nextEvent : edgeInfo.getNestedEvents()) {
			getStreamDataProvider().getCurrentEventCollection().add(nextEvent);
		}
		getStreamDataProvider().getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.EDGE));
	}
	
	
	@Override
	protected PhyloXMLReaderStreamDataProvider createStreamDataProvider() {
		return new PhyloXMLReaderStreamDataProvider(this);
	}
}
