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
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.LinkedLabeledIDEvent;
import info.bioinfweb.jphyloio.events.NodeEvent;
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
import info.bioinfweb.jphyloio.formats.xml.AbstractXMLEventReader;
import info.bioinfweb.jphyloio.formats.xml.XMLReadWriteUtils;
import info.bioinfweb.jphyloio.formats.xml.elementreaders.AbstractXMLElementReader;
import info.bioinfweb.jphyloio.formats.xml.elementreaders.CommentElementReader;
import info.bioinfweb.jphyloio.formats.xml.elementreaders.XMLElementReader;
import info.bioinfweb.jphyloio.formats.xml.elementreaders.XMLElementReaderKey;
import info.bioinfweb.jphyloio.objecttranslation.InvalidObjectSourceDataException;
import info.bioinfweb.jphyloio.objecttranslation.ObjectTranslator;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Queue;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
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
	
	/*
	 * Custom XML is read in all positions, where no other element reader is registered. This includes custom XML elements nested under elements where this is illegal.
	 * Only registering custom XML element readers under tags, where this is valid, would require to buffer the whole custom XML contents.
	 */
	
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
				
				if (!streamDataProvider.getPropertyEvents().isEmpty()) {
					for (JPhyloIOEvent nextEvent : streamDataProvider.getPropertyEvents()) {
						streamDataProvider.getCurrentEventCollection().add(nextEvent);
					}
					streamDataProvider.getPropertyEvents().clear();
				}
				
				createEdgeEvents(streamDataProvider);			
				
				streamDataProvider.getSourceNode().pop();
				streamDataProvider.setLastNodeID(null);
				streamDataProvider.setCreateNodeStart(true);
			}
		};
		
		AbstractXMLElementReader<PhyloXMLReaderStreamDataProvider> nodeLabelReader = new AbstractXMLElementReader<PhyloXMLReaderStreamDataProvider>() {
			@Override
			public void readEvent(PhyloXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				String value = event.asCharacters().getData() + XMLUtils.readCharactersAsString(getXMLReader());
				
				if (!streamDataProvider.getSourceNode().isEmpty()) {
					NodeEdgeInfo currentNode = streamDataProvider.getSourceNode().peek();
					
					if (currentNode.getLabel() == null) {
						currentNode.setLabel(value);
					}
				}
				
				streamDataProvider.getCurrentEventCollection().add(new LiteralMetadataContentEvent(value, value));
			}
		};
		
		AbstractXMLElementReader<PhyloXMLReaderStreamDataProvider> propertyStartReader = new AbstractXMLElementReader<PhyloXMLReaderStreamDataProvider>() {
			@Override
			public void readEvent(PhyloXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				StartElement element = event.asStartElement();
				String parentTag = streamDataProvider.getParentName();
				
				String appliesTo = XMLUtils.readStringAttr(element, ATTR_APPLIES_TO, null);
				URIOrStringIdentifier predicate = new URIOrStringIdentifier(null, parseQName(XMLUtils.readStringAttr(element, ATTR_REF, null), element));
				
				boolean appliesToAsAttribute = true;
				boolean resetEventCollection = false;				
				
				if (parentTag.equals(TAG_CLADE.getLocalPart())) {
					if (appliesTo.equals(APPLIES_TO_PARENT_BRANCH)) {
						streamDataProvider.setCurrentEventCollection(streamDataProvider.getSourceNode().peek().getNestedEdgeEvents());
						resetEventCollection = true;
						appliesToAsAttribute = false;
					}
					else if (appliesTo.equals(APPLIES_TO_PHYLOGENY)) {
						streamDataProvider.setCurrentEventCollection(streamDataProvider.getPropertyEvents());
						resetEventCollection = true;
						appliesToAsAttribute = false;
					}
					else if (appliesTo.equals(APPLIES_TO_NODE)) {
						appliesToAsAttribute = false;
					}
				}
				else if (parentTag.equals(TAG_PHYLOGENY.getLocalPart())) {
					if (appliesTo.equals(APPLIES_TO_PHYLOGENY)) {
						appliesToAsAttribute = false;
					}
				}
				else if (parentTag.equals(TAG_ANNOTATION.getLocalPart())) {
					if (appliesTo.equals(APPLIES_TO_ANNOTATION)) {
						appliesToAsAttribute = false;
					}
				}
				
				if ((XMLUtils.readStringAttr(element, ATTR_UNIT, null) != null) || (XMLUtils.readStringAttr(element, ATTR_ID_REF, null) != null) || appliesToAsAttribute == true) {
					streamDataProvider.getCurrentEventCollection().add(
							new ResourceMetadataEvent(DEFAULT_META_ID_PREFIX + streamDataProvider.getIDManager().createNewID(), null, 
							new URIOrStringIdentifier(null, PREDICATE_PROPERTY), null, null));
					streamDataProvider.setPropertyHasResource(true);
					
					if (appliesToAsAttribute) {
						readAttributes(streamDataProvider, element, "", ATTR_APPLIES_TO, PREDICATE_PROPERTY_ATTR_APPLIES_TO);
					}
					
					readAttributes(streamDataProvider, element, "", ATTR_UNIT, PREDICATE_PROPERTY_ATTR_UNIT, ATTR_ID_REF, PREDICATE_PROPERTY_ATTR_ID_REF);					
				}
				else {
					streamDataProvider.setPropertyHasResource(false);
				}
				
				QName datatype = readDatatypeAttributeValue(XMLUtils.readStringAttr(element, ATTR_DATATYPE, null), element);
				ObjectTranslator<?> translator = getParameters().getObjectTranslatorFactory().getDefaultTranslator(datatype);
				String propertyValue;
				
				
				if (datatype.equals(W3CXSConstants.DATA_TYPE_ANY_URI) && translator.getObjectClass().equals(URI.class)) {
					streamDataProvider.setPropertyIsURI(true);
					propertyValue = XMLUtils.readCharactersAsString(getXMLReader());
					
					try {						
						URI uri;
						if ((propertyValue != null) && !propertyValue.isEmpty()) {
							uri = (URI)translator.representationToJava(propertyValue, streamDataProvider);
						}
						else {
							uri = null;
						}
						
						streamDataProvider.getCurrentEventCollection().add(new ResourceMetadataEvent(DEFAULT_META_ID_PREFIX + streamDataProvider.getIDManager().createNewID(), null, 
								predicate, uri, null));
						streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_RESOURCE));
					}
					catch (InvalidObjectSourceDataException e) {
						throw new JPhyloIOReaderException("The content of this property tag could not be parsed to a URI.", event.getLocation());
					}
				}
				else {
					streamDataProvider.setPropertyIsURI(false);
					
					streamDataProvider.getCurrentEventCollection().add(
							new LiteralMetadataEvent(DEFAULT_META_ID_PREFIX + streamDataProvider.getIDManager().createNewID(), null, predicate, 
							new URIOrStringIdentifier(null, datatype), LiteralContentSequenceType.SIMPLE));

					streamDataProvider.setResetEventCollection(resetEventCollection);
					
					if (!datatype.equals(W3CXSConstants.DATA_TYPE_TOKEN) && !datatype.equals(W3CXSConstants.DATA_TYPE_STRING) && (translator != null)) {
						Object objectValue = null;
						propertyValue = XMLUtils.readCharactersAsString(getXMLReader());
						
						if (propertyValue != null) {
							try {
								objectValue = translator.representationToJava(propertyValue, streamDataProvider);
								streamDataProvider.getCurrentEventCollection().add(new LiteralMetadataContentEvent(objectValue, propertyValue));
							}
							catch (InvalidObjectSourceDataException e) {
								throw new JPhyloIOReaderException("The content of this property tag could not be parsed to class " + translator.getObjectClass().getSimpleName() + ".", event.getLocation());
							}
						}
					}
					else {
						streamDataProvider.setFirstContentEvent(true);
					}
				}
			}
		};
		
		putElementReader(new XMLElementReaderKey(TAG_PROPERTY, null, XMLStreamConstants.CHARACTERS),
				new AbstractXMLElementReader<PhyloXMLReaderStreamDataProvider>() {
					@Override
					public void readEvent(PhyloXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {						
						boolean isContinued = streamDataProvider.getXMLReader().peek().isCharacters();
						String data = event.asCharacters().getData();
					
						if (streamDataProvider.isFirstContentEvent() && !isContinued) {
							streamDataProvider.getCurrentEventCollection().add(new LiteralMetadataContentEvent(data, data));
						}
						else {
							streamDataProvider.getCurrentEventCollection().add(new LiteralMetadataContentEvent(data, isContinued));
							streamDataProvider.setFirstContentEvent(false);
						}
					}
			});
		
		AbstractXMLElementReader<PhyloXMLReaderStreamDataProvider> propertyEndReader = new AbstractXMLElementReader<PhyloXMLReaderStreamDataProvider>() {
			@Override
			public void readEvent(PhyloXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				if (!streamDataProvider.isPropertyIsURI()) {
					streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
				}
				
				if (streamDataProvider.isPropertyHasResource()) {
					streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_RESOURCE));
				}
				
				if (streamDataProvider.isResetEventCollection()) {
					streamDataProvider.resetCurrentEventCollection();
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
					streamDataProvider.setCreateTreeGroupStart(true);
				}
		});
		
		putElementReader(new XMLElementReaderKey(null, null, XMLStreamConstants.END_DOCUMENT), 
			new AbstractXMLElementReader<PhyloXMLReaderStreamDataProvider>() {
				@Override
				public void readEvent(PhyloXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
					if (streamDataProvider.isCreateTreeGroupEnd()) {
						streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.TREE_NETWORK_GROUP));
						streamDataProvider.setCreateTreeGroupEnd(false);
					}
					
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
				
					streamDataProvider.getEdgeInfos().add(new ArrayDeque<NodeEdgeInfo>());	// Add edge info queue for root level
					
					if (streamDataProvider.isCreateTreeGroupStart()) {
						streamDataProvider.getCurrentEventCollection().add(new LinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, 
									DEFAULT_TREE_NETWORK_GROUP_ID_PREFIX + streamDataProvider.getIDManager().createNewID(), null, null));
						streamDataProvider.setCreateTreeGroupEnd(true);
						streamDataProvider.setCreateTreeGroupStart(false);
					}
					
					streamDataProvider.setCreatePhylogenyStart(true);
					streamDataProvider.setCurrentEventCollection(new ArrayList<JPhyloIOEvent>());
					streamDataProvider.setRootedPhylogeny(XMLUtils.readBooleanAttr(element, ATTR_ROOTED, false));
					
					readAttributes(streamDataProvider, element, "", ATTR_REROOTABLE, PREDICATE_PHYLOGENY_ATTR_REROOTABLE, ATTR_BRANCH_LENGTH_UNIT, 
							PREDICATE_PHYLOGENY_ATTR_BRANCH_LENGTH_UNIT, ATTR_TYPE, PREDICATE_PHYLOGENY_ATTR_TYPE);
				}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_PHYLOGENY, null, XMLStreamConstants.CHARACTERS), new PhyloXMLNoCharactersAllowedElementReader());
		
		putElementReader(new XMLElementReaderKey(TAG_ROOT, TAG_PHYLOGENY, XMLStreamConstants.END_ELEMENT), 
			new AbstractXMLElementReader<PhyloXMLReaderStreamDataProvider>() {
				@Override
				public void readEvent(PhyloXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
					if (streamDataProvider.isCreatePhylogenyStart()) {
						createPhylogenyStart(streamDataProvider);
					}
					
					createEdgeEvents(streamDataProvider); // Add root edge event
					
					streamDataProvider.getSourceNode().clear();
					streamDataProvider.getEdgeInfos().clear();
					
					streamDataProvider.getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(
							getParameters().getBoolean(ReadWriteParameterMap.KEY_PHYLOXML_CONSIDER_PHYLOGENY_AS_TREE, false) ? EventContentType.TREE : EventContentType.NETWORK, 
							EventTopologyType.END));
				}
		});
		
		//Phylogeny.Clade		
		putElementReader(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_CLADE, XMLStreamConstants.START_ELEMENT),
			new AbstractXMLElementReader<PhyloXMLReaderStreamDataProvider>() {
				@Override
				public void readEvent(PhyloXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
					StartElement element = event.asStartElement();
					
					if (streamDataProvider.isCreatePhylogenyStart()) {
						createPhylogenyStart(streamDataProvider);
					}
					
					// Add node info for root node
					NodeEdgeInfo nodeInfo = new NodeEdgeInfo(DEFAULT_NODE_ID_PREFIX + streamDataProvider.getIDManager().createNewID(), 
							XMLUtils.readDoubleAttr(element, ATTR_BRANCH_LENGTH, Double.NaN), new ArrayList<JPhyloIOEvent>(), new ArrayList<JPhyloIOEvent>());
					nodeInfo.setIsRoot(streamDataProvider.isRootedPhylogeny());
					streamDataProvider.getSourceNode().add(nodeInfo);
					streamDataProvider.setCreateNodeStart(true);
					
					streamDataProvider.getEdgeInfos().add(new ArrayDeque<NodeEdgeInfo>());	// Add edge info for this level
					
					streamDataProvider.setCurrentEventCollection(streamDataProvider.getSourceNode().peek().getNestedNodeEvents());
					
					streamDataProvider.setLastNodeID(XMLUtils.readStringAttr(element, ATTR_ID_SOURCE, null));
				}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_CLADE, null, XMLStreamConstants.CHARACTERS), new PhyloXMLNoCharactersAllowedElementReader());
		
		putElementReader(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_CLADE, XMLStreamConstants.END_ELEMENT), cladeEndReader);
		
		//Clade.Clade
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_CLADE, XMLStreamConstants.START_ELEMENT),
			new AbstractXMLElementReader<PhyloXMLReaderStreamDataProvider>() {
				@Override
				public void readEvent(PhyloXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {					
					if (streamDataProvider.hasSpecialEventCollection()) {
						streamDataProvider.resetCurrentEventCollection();
					}

					// Add node info for this node
					streamDataProvider.getSourceNode().add(new NodeEdgeInfo(DEFAULT_NODE_ID_PREFIX + streamDataProvider.getIDManager().createNewID(), 
							XMLUtils.readDoubleAttr(event.asStartElement(), ATTR_BRANCH_LENGTH, Double.NaN), new ArrayList<JPhyloIOEvent>(), new ArrayList<JPhyloIOEvent>()));
					streamDataProvider.setCreateNodeStart(true);
					
					streamDataProvider.getEdgeInfos().add(new ArrayDeque<NodeEdgeInfo>());  // Add edge info for this level
					
					streamDataProvider.setCurrentEventCollection(streamDataProvider.getSourceNode().peek().getNestedNodeEvents());					
					
					streamDataProvider.setLastNodeID(XMLUtils.readStringAttr(event.asStartElement(), ATTR_ID_SOURCE, null));					
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
						String value = event.asCharacters().getData() + XMLUtils.readCharactersAsString(getXMLReader());
						
						if (!value.matches("\\s+")) {
							String parentName = streamDataProvider.getParentName();
							
							if (parentName.equals(TAG_PHYLOGENY.getLocalPart())) {
								streamDataProvider.setTreeLabel(value);
								if (streamDataProvider.isCreatePhylogenyStart()) {
									createPhylogenyStart(streamDataProvider);
								}
							}
							else if (!streamDataProvider.getSourceNode().isEmpty()) {
								NodeEdgeInfo currentNode = streamDataProvider.getSourceNode().peek();
								String currentNodeLabel = currentNode.getLabel();
								
								if (parentName.equals(TAG_CLADE.getLocalPart())) {
									currentNode.setLabel(value);
								}
								else if (parentName.equals(TAG_SEQUENCE.getLocalPart())) {
									if (currentNodeLabel == null) {
										currentNode.setLabel(value);
									}
									
									streamDataProvider.getCurrentEventCollection().add(new LiteralMetadataContentEvent(value, value));
								}
							}
						}
					}
			});		
		
		putElementReader(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_NAME, XMLStreamConstants.END_ELEMENT), emptyReader);
		
		//Phylogeny.ID
		putElementReader(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_ID, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(PREDICATE_PHYLOGENY_ID_VALUE, PREDICATE_PHYLOGENY_ID, new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN),
				false, ATTR_ID_PROVIDER, PREDICATE_PHYLOGENY_ID_ATTR_PROVIDER));
		putElementReader(new XMLElementReaderKey(TAG_ID, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_ID, XMLStreamConstants.END_ELEMENT), resourceAndLiteralEndReader);
		
		//Phylogeny.Description
		putElementReader(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_DESCRIPTION, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_PHYLOGENY_DESCRIPTION, null, new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), false));
		putElementReader(new XMLElementReaderKey(TAG_DESCRIPTION, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_DESCRIPTION, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		//Phylogeny.Date
		putElementReader(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_DATE, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_PHYLOGENY_DATE, null, new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_DATE_TIME), false));
		putElementReader(new XMLElementReaderKey(TAG_DATE, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_DATE_TIME)); //TODO might lead to unspecific exceptions if an illegal characters event is encountered under Clade.Date
		putElementReader(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_DATE, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		//Phylogeny.Confidence
		putElementReader(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_CONFIDENCE, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(PREDICATE_CONFIDENCE_VALUE, PREDICATE_CONFIDENCE, new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_DOUBLE), 
				false, ATTR_TYPE, PREDICATE_CONFIDENCE_ATTR_TYPE)); //TODO maybe only create one literal meta event with the "type"-attributes value as an additional part of the predicate, depending on the implementation in the writer
		putElementReader(new XMLElementReaderKey(TAG_CONFIDENCE, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_DOUBLE));
		putElementReader(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_CONFIDENCE, XMLStreamConstants.END_ELEMENT), resourceAndLiteralEndReader);		
		
		//Phylogeny.CladeRelation
		putElementReader(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_CLADE_RELATION, XMLStreamConstants.START_ELEMENT),
			new AbstractXMLElementReader<PhyloXMLReaderStreamDataProvider>() {
				@Override
				public void readEvent(PhyloXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
					StartElement element = event.asStartElement();
					String cladeID0 = XMLUtils.readStringAttr(element, ATTR_ID_REF_0, null);
					String cladeID1 = XMLUtils.readStringAttr(element, ATTR_ID_REF_1, null);
					double branchLength = XMLUtils.readDoubleAttr(element, ATTR_DISTANCE, Double.NaN);
					
					if (getParameters().getBoolean(ReadWriteParameterMap.KEY_PHYLOXML_CONSIDER_PHYLOGENY_AS_TREE, false)) {
						getStreamDataProvider().getCurrentEventCollection().add(new ResourceMetadataEvent(DEFAULT_META_ID_PREFIX + streamDataProvider.getIDManager().createNewID(), 
								null, new URIOrStringIdentifier(null, PREDICATE_CLADE_REL), null, null));
						
						streamDataProvider.getCurrentEventCollection().add(
								new LiteralMetadataEvent(DEFAULT_META_ID_PREFIX + streamDataProvider.getIDManager().createNewID(), null, 
								new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_EDGE_SOURCE_NODE), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), 
								LiteralContentSequenceType.SIMPLE));
		
						streamDataProvider.getCurrentEventCollection().add(new LiteralMetadataContentEvent(cladeID0, cladeID0));
								
						streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
						
						streamDataProvider.getCurrentEventCollection().add(
								new LiteralMetadataEvent(DEFAULT_META_ID_PREFIX + streamDataProvider.getIDManager().createNewID(), null, 
								new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_EDGE_TARGET_NODE), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN),
								LiteralContentSequenceType.SIMPLE));
		
						streamDataProvider.getCurrentEventCollection().add(new LiteralMetadataContentEvent(cladeID1, cladeID1));
								
						streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
						
						if (Double.compare(branchLength, Double.NaN) != 0) {
							streamDataProvider.getCurrentEventCollection().add(
									new LiteralMetadataEvent(DEFAULT_META_ID_PREFIX + streamDataProvider.getIDManager().createNewID(), null, 
									new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_EDGE_LENGTH), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_DOUBLE), 
									LiteralContentSequenceType.SIMPLE));
			
							streamDataProvider.getCurrentEventCollection().add(new LiteralMetadataContentEvent(branchLength, Double.toString(branchLength)));
									
							streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
						}
						
						readAttributes(streamDataProvider, event.asStartElement(), "", ATTR_ID_REF_0, PREDICATE_CLADE_REL_ATTR_IDREF0, ATTR_ID_REF_1, PREDICATE_CLADE_REL_ATTR_IDREF1,
								ATTR_DISTANCE, PREDICATE_CLADE_REL_ATTR_DISTANCE, ATTR_TYPE, PREDICATE_CLADE_REL_ATTR_TYPE);
					}
					else {  // The phylogeny is considered as a network
						if ((cladeID0 != null) && (cladeID1 != null)) {
							String eventID0 = streamDataProvider.getCladeIDToNodeEventIDMap().get(cladeID0);
							String eventID1 = streamDataProvider.getCladeIDToNodeEventIDMap().get(cladeID1);
							
							if ((eventID0 != null) && (eventID1 != null)) {								
								getStreamDataProvider().getCurrentEventCollection().add(new EdgeEvent(DEFAULT_EDGE_ID_PREFIX + streamDataProvider.getIDManager().createNewID(), null, 
										eventID0, eventID1, branchLength));
								
								streamDataProvider.getCurrentEventCollection().add(
										new LiteralMetadataEvent(DEFAULT_META_ID_PREFIX + streamDataProvider.getIDManager().createNewID(), null, 
										new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_IS_CROSSLINK), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_BOOLEAN), 
										LiteralContentSequenceType.SIMPLE));
				
								streamDataProvider.getCurrentEventCollection().add(new LiteralMetadataContentEvent(true, Boolean.toString(true)));
										
								streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
								
								readAttributes(streamDataProvider, event.asStartElement(), "", ATTR_TYPE, PREDICATE_CLADE_REL_ATTR_TYPE);
							}
							else {
								throw new JPhyloIOReaderException("A node ID was referenced by a clade relation element, that was not defined before.", event.getLocation());
							}
						}
						else {
							throw new JPhyloIOReaderException("No valid edge was referenced by a clade relation element. Both the source and target node must not be null.", event.getLocation());
						}						
					}									
				}
		});		
		
		putElementReader(new XMLElementReaderKey(TAG_CLADE_RELATION, TAG_CONFIDENCE, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(PREDICATE_CONFIDENCE_VALUE, PREDICATE_CONFIDENCE, new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_DOUBLE), 
				false, ATTR_TYPE, PREDICATE_CONFIDENCE_ATTR_TYPE));
		//Element reader for character content of confidence tag was registered before
		putElementReader(new XMLElementReaderKey(TAG_CLADE_RELATION, TAG_CONFIDENCE, XMLStreamConstants.END_ELEMENT), resourceAndLiteralEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_CLADE_RELATION, null, XMLStreamConstants.CHARACTERS), new PhyloXMLNoCharactersAllowedElementReader());
		
		putElementReader(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_CLADE_RELATION, XMLStreamConstants.END_ELEMENT), 
			new AbstractXMLElementReader<PhyloXMLReaderStreamDataProvider>() {
				@Override
				public void readEvent(PhyloXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
					if (getParameters().getBoolean(ReadWriteParameterMap.KEY_PHYLOXML_CONSIDER_PHYLOGENY_AS_TREE, false)) {
						streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_RESOURCE));
					}
					else {
						getStreamDataProvider().getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.EDGE));
					}
				}
		});		
		
		//Phylogeny.SequenceRelation
		putElementReader(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_SEQUENCE_RELATION, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(null, PREDICATE_SEQ_REL, null, false, ATTR_ID_REF_0, PREDICATE_SEQ_REL_ATTR_IDREF0, 
				ATTR_ID_REF_1, PREDICATE_SEQ_REL_ATTR_IDREF1, ATTR_DISTANCE, PREDICATE_SEQ_REL_ATTR_DISTANCE, 
				ATTR_TYPE, PREDICATE_SEQ_REL_ATTR_TYPE));
		putElementReader(new XMLElementReaderKey(TAG_SEQUENCE_RELATION, null, XMLStreamConstants.CHARACTERS), new PhyloXMLNoCharactersAllowedElementReader());
		
		putElementReader(new XMLElementReaderKey(TAG_SEQUENCE_RELATION, TAG_CONFIDENCE, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(PREDICATE_SEQ_REL_CONFIDENCE_VALUE, PREDICATE_SEQ_REL_CONFIDENCE, new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_DOUBLE), 
				false, ATTR_TYPE, PREDICATE_SEQ_REL_CONFIDENCE_ATTR_TYPE));
		//Element reader for character content of confidence tag was registered before
		putElementReader(new XMLElementReaderKey(TAG_SEQUENCE_RELATION, TAG_CONFIDENCE, XMLStreamConstants.END_ELEMENT), resourceAndLiteralEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_SEQUENCE_RELATION, XMLStreamConstants.END_ELEMENT), resourceEndReader);
		
		//Phylogeny.Property
		putElementReader(new XMLElementReaderKey(TAG_PHYLOGENY, TAG_PROPERTY, XMLStreamConstants.START_ELEMENT), propertyStartReader);		
		//Element reader for character content of property tag was registered before		
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
						NodeEdgeInfo currentEdge = streamDataProvider.getSourceNode().peek();
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
				new PhyloXMLStartElementReader(PREDICATE_CONFIDENCE_VALUE, PREDICATE_CONFIDENCE, new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_DOUBLE),
				true, ATTR_TYPE, PREDICATE_CONFIDENCE_ATTR_TYPE));
		//Element reader for character content of confidence tag was registered before
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_CONFIDENCE, XMLStreamConstants.END_ELEMENT), new PhyloXMLEndElementReader(true, true, true));
		
		//Clade.Width
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_BRANCH_WIDTH, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_WIDTH, null, new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_DOUBLE), true));
		putElementReader(new XMLElementReaderKey(TAG_BRANCH_WIDTH, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_DOUBLE));
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_BRANCH_WIDTH, XMLStreamConstants.END_ELEMENT), new PhyloXMLEndElementReader(true, false, true));
		
		//Clade.Color
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_BRANCH_COLOR, XMLStreamConstants.START_ELEMENT), 
			new AbstractXMLElementReader<PhyloXMLReaderStreamDataProvider>() {
				@Override
				public void readEvent(PhyloXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {					
					ObjectTranslator<Color> translator = new PhyloXMLColorTranslator();
					Color color = null;
					
					streamDataProvider.setCurrentEventCollection(streamDataProvider.getSourceNode().peek().getNestedEdgeEvents());
					
					streamDataProvider.getCurrentEventCollection().add(
							new LiteralMetadataEvent(DEFAULT_META_ID_PREFIX + streamDataProvider.getIDManager().createNewID(), null, new URIOrStringIdentifier(null, PREDICATE_COLOR), 
									new URIOrStringIdentifier(null, DATA_TYPE_BRANCH_COLOR), LiteralContentSequenceType.SIMPLE));
					
					try {
						color = translator.readXMLRepresentation(getXMLReader(), streamDataProvider);
						streamDataProvider.getCurrentEventCollection().add(new LiteralMetadataContentEvent(color, null));
					}
					catch (InvalidObjectSourceDataException e) {
						throw new JPhyloIOReaderException("The content of this property tag could not be parsed to class color.", event.getLocation());
					}
				}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_BRANCH_COLOR, XMLStreamConstants.END_ELEMENT), new PhyloXMLEndElementReader(true, false, true));
		
		//Clade.NodeID
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_NODE_ID, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(PREDICATE_NODE_ID_VALUE, PREDICATE_NODE_ID, new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN),
				false, ATTR_ID_PROVIDER, PREDICATE_NODE_ID_ATTR_PROVIDER));
		putElementReader(new XMLElementReaderKey(TAG_NODE_ID, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_NODE_ID, XMLStreamConstants.END_ELEMENT), resourceAndLiteralEndReader);
		
		//Clade.Taxonomy
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_TAXONOMY, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(null, PREDICATE_TAXONOMY, null, false, ATTR_ID_SOURCE, PREDICATE_TAXONOMY_ATTR_ID_SOURCE));
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, null, XMLStreamConstants.CHARACTERS), new PhyloXMLNoCharactersAllowedElementReader());
		
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_ID, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(PREDICATE_TAXONOMY_ID_VALUE, PREDICATE_TAXONOMY_ID, new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), 
				false, ATTR_ID_PROVIDER, PREDICATE_TAXONOMY_ID_ATTR_PROVIDER));
		putElementReader(new XMLElementReaderKey(TAG_ID, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_ID, XMLStreamConstants.END_ELEMENT), resourceAndLiteralEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_CODE, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_TAXONOMY_CODE, null, new URIOrStringIdentifier(null, DATA_TYPE_TAXONOMY_CODE), false));
		putElementReader(new XMLElementReaderKey(TAG_CODE, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(DATA_TYPE_TAXONOMY_CODE));
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_CODE, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_SCI_NAME, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(PREDICATE_TAXONOMY_SCIENTIFIC_NAME, null, new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), false));
		putElementReader(new XMLElementReaderKey(TAG_SCI_NAME, null, XMLStreamConstants.CHARACTERS), nodeLabelReader);
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_SCI_NAME, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_AUTHORITY, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(PREDICATE_TAXONOMY_AUTHORITY, null, new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), false));
		putElementReader(new XMLElementReaderKey(TAG_AUTHORITY, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_AUTHORITY, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_COMMON_NAME, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(PREDICATE_TAXONOMY_COMMON_NAME, null, new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), false));
		putElementReader(new XMLElementReaderKey(TAG_COMMON_NAME, null, XMLStreamConstants.CHARACTERS), nodeLabelReader);
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_COMMON_NAME, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_SYNONYM, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(PREDICATE_TAXONOMY_SYNONYM, null, new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), false));
		putElementReader(new XMLElementReaderKey(TAG_SYNONYM, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_SYNONYM, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_RANK, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(PREDICATE_TAXONOMY_RANK, null, new URIOrStringIdentifier(null, DATA_TYPE_RANK), false));
		putElementReader(new XMLElementReaderKey(TAG_RANK, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(DATA_TYPE_RANK));
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_RANK, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_URI, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(null, PREDICATE_TAXONOMY_URI, null, false, ATTR_DESC, PREDICATE_TAXONOMY_URI_ATTR_DESC, ATTR_TYPE, PREDICATE_TAXONOMY_URI_ATTR_TYPE));
		
		putElementReader(new XMLElementReaderKey(TAG_URI, null, XMLStreamConstants.CHARACTERS),
			new AbstractXMLElementReader<PhyloXMLReaderStreamDataProvider>() {
				@Override
				public void readEvent(PhyloXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
					String uri = event.asCharacters().getData();
					boolean isContinued = streamDataProvider.getXMLReader().peek().isCharacters();
					URI externalResource = null;
					
					if (streamDataProvider.getIncompleteToken() != null) {
						uri = streamDataProvider.getIncompleteToken() + uri;
					}
					
					if (!isContinued) {
						try {
							externalResource = new URI(uri);
							streamDataProvider.getCurrentEventCollection().add(new ResourceMetadataEvent(DEFAULT_META_ID_PREFIX + streamDataProvider.getIDManager().createNewID(), null, 
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
		
		putElementReader(new XMLElementReaderKey(TAG_TAXONOMY, TAG_URI, XMLStreamConstants.END_ELEMENT), resourceEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_TAXONOMY, XMLStreamConstants.END_ELEMENT), resourceEndReader);		
		
		//Clade.Sequence
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_SEQUENCE, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(null, PREDICATE_SEQUENCE, null, false, ATTR_TYPE, PREDICATE_SEQUENCE_ATTR_TYPE, ATTR_ID_SOURCE, PREDICATE_SEQUENCE_ATTR_ID_SOURCE, 
						ATTR_ID_REF, PREDICATE_SEQUENCE_ATTR_ID_REF));
		putElementReader(new XMLElementReaderKey(TAG_SEQUENCE, null, XMLStreamConstants.CHARACTERS), new PhyloXMLNoCharactersAllowedElementReader());
		
		putElementReader(new XMLElementReaderKey(TAG_SEQUENCE, TAG_SYMBOL, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_SEQUENCE_SYMBOL, null, new URIOrStringIdentifier(null, DATA_TYPE_SEQUENCE_SYMBOL), false));
		putElementReader(new XMLElementReaderKey(TAG_SYMBOL, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(DATA_TYPE_SEQUENCE_SYMBOL));
		putElementReader(new XMLElementReaderKey(TAG_SEQUENCE, TAG_SYMBOL, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_SEQUENCE, TAG_ACCESSION, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(PREDICATE_SEQUENCE_ACCESSION_VALUE, PREDICATE_SEQUENCE_ACCESSION, new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), 
				false, ATTR_SOURCE, PREDICATE_SEQUENCE_ACCESSION_ATTR_SOURCE));		
		putElementReader(new XMLElementReaderKey(TAG_ACCESSION, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_SEQUENCE, TAG_ACCESSION, XMLStreamConstants.END_ELEMENT), resourceAndLiteralEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_SEQUENCE, TAG_LOCATION, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_SEQUENCE_LOCATION, null, new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), false));
		putElementReader(new XMLElementReaderKey(TAG_LOCATION, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_SEQUENCE, TAG_LOCATION, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_SEQUENCE, TAG_NAME, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_SEQUENCE_NAME, null, new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), false));
		//Element reader for character content of name tag was registered before
		putElementReader(new XMLElementReaderKey(TAG_SEQUENCE, TAG_NAME, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_SEQUENCE, TAG_MOL_SEQ, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(PREDICATE_SEQUENCE_MOL_SEQ_VALUE, PREDICATE_SEQUENCE_MOL_SEQ, new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), 
				false, ATTR_IS_ALIGNED, PREDICATE_SEQUENCE_MOL_SEQ_ATTR_IS_ALIGNED)); //TODO possibly create parameter in parameter map to prevent reading large sequences		
		putElementReader(new XMLElementReaderKey(TAG_MOL_SEQ, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_SEQUENCE, TAG_MOL_SEQ, XMLStreamConstants.END_ELEMENT), resourceAndLiteralEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_SEQUENCE, TAG_URI, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(null, PREDICATE_SEQUENCE_URI, null, false, ATTR_DESC, PREDICATE_SEQUENCE_URI_ATTR_DESC, 
						ATTR_TYPE, PREDICATE_SEQUENCE_URI_ATTR_TYPE));
		//Element reader for character content of URI tag was registered before
		putElementReader(new XMLElementReaderKey(TAG_SEQUENCE, TAG_URI, XMLStreamConstants.END_ELEMENT), resourceEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_SEQUENCE, TAG_ANNOTATION, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(null, PREDICATE_ANNOTATION, null, false, ATTR_REF, PREDICATE_ANNOTATION_ATTR_REF, ATTR_SOURCE, PREDICATE_ANNOTATION_ATTR_SOURCE, 
						ATTR_EVIDENCE, PREDICATE_ANNOTATION_ATTR_EVIDENCE, ATTR_TYPE, PREDICATE_ANNOTATION_ATTR_TYPE));
		putElementReader(new XMLElementReaderKey(TAG_ANNOTATION, null, XMLStreamConstants.CHARACTERS), new PhyloXMLNoCharactersAllowedElementReader());
		
		putElementReader(new XMLElementReaderKey(TAG_ANNOTATION, TAG_DESC, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_ANNOTATION_DESC, null, new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), false));
		putElementReader(new XMLElementReaderKey(TAG_DESC, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_ANNOTATION, TAG_DESC, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_ANNOTATION, TAG_CONFIDENCE, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(PREDICATE_ANNOTATION_CONFIDENCE_VALUE, PREDICATE_ANNOTATION_CONFIDENCE, new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_DOUBLE), 
				false, ATTR_TYPE, PREDICATE_ANNOTATION_CONFIDENCE_ATTR_TYPE));
		//Element reader for character content of confidence tag was registered before
		putElementReader(new XMLElementReaderKey(TAG_ANNOTATION, TAG_CONFIDENCE, XMLStreamConstants.END_ELEMENT), resourceAndLiteralEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_ANNOTATION, TAG_PROPERTY, XMLStreamConstants.START_ELEMENT), propertyStartReader);
		//Element reader for character content of property tag was registered before
		putElementReader(new XMLElementReaderKey(TAG_ANNOTATION, TAG_PROPERTY, XMLStreamConstants.END_ELEMENT), propertyEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_ANNOTATION, TAG_URI, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(null, PREDICATE_ANNOTATION_URI, null, false, ATTR_DESC, PREDICATE_ANNOTATION_URI_ATTR_DESC, 
						ATTR_TYPE, PREDICATE_ANNOTATION_URI_ATTR_TYPE));
		//Element reader for character content of URI tag was registered before
		putElementReader(new XMLElementReaderKey(TAG_ANNOTATION, TAG_URI, XMLStreamConstants.END_ELEMENT), resourceEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_SEQUENCE, TAG_ANNOTATION, XMLStreamConstants.END_ELEMENT), resourceEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_SEQUENCE, TAG_DOMAIN_ARCHITECTURE, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(null, PREDICATE_DOMAIN_ARCHITECTURE, null, false, ATTR_LENGTH, PREDICATE_DOMAIN_ARCHITECTURE_ATTR_LENGTH));
		putElementReader(new XMLElementReaderKey(TAG_DOMAIN_ARCHITECTURE, null, XMLStreamConstants.CHARACTERS), new PhyloXMLNoCharactersAllowedElementReader());
		
		putElementReader(new XMLElementReaderKey(TAG_DOMAIN_ARCHITECTURE, TAG_DOMAIN, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(PREDICATE_DOMAIN_ARCHITECTURE_DOMAIN_VALUE, PREDICATE_DOMAIN_ARCHITECTURE_DOMAIN, 
				new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), false, ATTR_FROM, PREDICATE_DOMAIN_ARCHITECTURE_DOMAIN_ATTR_FROM,
				ATTR_TO, PREDICATE_DOMAIN_ARCHITECTURE_DOMAIN_ATTR_TO, ATTR_CONFIDENCE, PREDICATE_DOMAIN_ARCHITECTURE_DOMAIN_ATTR_CONFIDENCE, 
				ATTR_ID, PREDICATE_DOMAIN_ARCHITECTURE_DOMAIN_ATTR_ID));
		putElementReader(new XMLElementReaderKey(TAG_DOMAIN, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_DOMAIN_ARCHITECTURE, TAG_DOMAIN, XMLStreamConstants.END_ELEMENT), resourceAndLiteralEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_SEQUENCE, TAG_DOMAIN_ARCHITECTURE, XMLStreamConstants.END_ELEMENT), resourceEndReader);		
		
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_SEQUENCE, XMLStreamConstants.END_ELEMENT), resourceEndReader);
		
		//Clade.Events
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_EVENTS, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(null, PREDICATE_EVENTS, null, false));
		putElementReader(new XMLElementReaderKey(TAG_EVENTS, null, XMLStreamConstants.CHARACTERS), new PhyloXMLNoCharactersAllowedElementReader());
		
		putElementReader(new XMLElementReaderKey(TAG_EVENTS, TAG_TYPE, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_EVENTS_TYPE, null, new URIOrStringIdentifier(null, DATA_TYPE_EVENTTYPE), false));
		putElementReader(new XMLElementReaderKey(TAG_TYPE, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(DATA_TYPE_EVENTTYPE));
		putElementReader(new XMLElementReaderKey(TAG_EVENTS, TAG_TYPE, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_EVENTS, TAG_DUPLICATIONS, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_EVENTS_DUPLICATIONS, null, new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_NON_NEGATIVE_INTEGER), false));
		putElementReader(new XMLElementReaderKey(TAG_DUPLICATIONS, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_NON_NEGATIVE_INTEGER));
		putElementReader(new XMLElementReaderKey(TAG_EVENTS, TAG_DUPLICATIONS, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_EVENTS, TAG_SPECIATIONS, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_EVENTS_SPECIATIONS, null, new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_NON_NEGATIVE_INTEGER), false));
		putElementReader(new XMLElementReaderKey(TAG_SPECIATIONS, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_NON_NEGATIVE_INTEGER));
		putElementReader(new XMLElementReaderKey(TAG_EVENTS, TAG_SPECIATIONS, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_EVENTS, TAG_LOSSES, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_EVENTS_LOSSES, null, new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_NON_NEGATIVE_INTEGER), false));
		putElementReader(new XMLElementReaderKey(TAG_LOSSES, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_NON_NEGATIVE_INTEGER));
		putElementReader(new XMLElementReaderKey(TAG_EVENTS, TAG_LOSSES, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_EVENTS, TAG_CONFIDENCE, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(PREDICATE_EVENTS_CONFIDENCE_VALUE, PREDICATE_EVENTS_CONFIDENCE, 
				new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_DOUBLE), false, ATTR_TYPE, PREDICATE_EVENTS_CONFIDENCE_ATTR_TYPE));
		//Element reader for character content of confidence tag was registered before
		putElementReader(new XMLElementReaderKey(TAG_EVENTS, TAG_CONFIDENCE, XMLStreamConstants.END_ELEMENT), resourceAndLiteralEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_EVENTS, XMLStreamConstants.END_ELEMENT), resourceEndReader);
		
		//Clade.BinaryCharacters
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_BINARY_CHARACTERS, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(null, PREDICATE_BINARY_CHARACTERS, null, false, ATTR_TYPE, PREDICATE_BINARY_CHARACTERS_ATTR_TYPE, ATTR_GAINED_COUNT, PREDICATE_BINARY_CHARACTERS_ATTR_GAINED_COUNT,
						ATTR_LOST_COUNT, PREDICATE_BINARY_CHARACTERS_ATTR_LOST_COUNT, ATTR_PRESENT_COUNT, PREDICATE_BINARY_CHARACTERS_ATTR_PRESENT_COUNT, ATTR_ABSENT_COUNT, PREDICATE_BINARY_CHARACTERS_ATTR_ABSENT_COUNT));
		putElementReader(new XMLElementReaderKey(TAG_EVENTS, null, XMLStreamConstants.CHARACTERS), new PhyloXMLNoCharactersAllowedElementReader());
		
		putElementReader(new XMLElementReaderKey(TAG_BINARY_CHARACTERS, TAG_GAINED, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(null, PREDICATE_BINARY_CHARACTERS_GAINED, null, false));
		putElementReader(new XMLElementReaderKey(TAG_GAINED, null, XMLStreamConstants.CHARACTERS), new PhyloXMLNoCharactersAllowedElementReader());
		
		putElementReader(new XMLElementReaderKey(TAG_GAINED, TAG_BC, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_BINARY_CHARACTERS_GAINED_BC, null, new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), false));
		putElementReader(new XMLElementReaderKey(TAG_BC, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_GAINED, TAG_BC, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_BINARY_CHARACTERS, TAG_GAINED, XMLStreamConstants.END_ELEMENT), resourceEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_BINARY_CHARACTERS, TAG_LOST, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(null, PREDICATE_BINARY_CHARACTERS_LOST, null, false));
		putElementReader(new XMLElementReaderKey(TAG_LOST, null, XMLStreamConstants.CHARACTERS), new PhyloXMLNoCharactersAllowedElementReader());
		
		putElementReader(new XMLElementReaderKey(TAG_LOST, TAG_BC, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_BINARY_CHARACTERS_LOST_BC, null, new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), false));
		//Element reader for character content of BC tag was registered before
		putElementReader(new XMLElementReaderKey(TAG_LOST, TAG_BC, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_BINARY_CHARACTERS, TAG_LOST, XMLStreamConstants.END_ELEMENT), resourceEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_BINARY_CHARACTERS, TAG_PRESENT, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(null, PREDICATE_BINARY_CHARACTERS_PRESENT, null, false));
		putElementReader(new XMLElementReaderKey(TAG_PRESENT, null, XMLStreamConstants.CHARACTERS), new PhyloXMLNoCharactersAllowedElementReader());
		
		putElementReader(new XMLElementReaderKey(TAG_PRESENT, TAG_BC, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_BINARY_CHARACTERS_PRESENT_BC, null, new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), false));
		//Element reader for character content of BC tag was registered before
		putElementReader(new XMLElementReaderKey(TAG_PRESENT, TAG_BC, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_BINARY_CHARACTERS, TAG_PRESENT, XMLStreamConstants.END_ELEMENT), resourceEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_BINARY_CHARACTERS, TAG_ABSENT, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(null, PREDICATE_BINARY_CHARACTERS_ABSENT, null, false));
		putElementReader(new XMLElementReaderKey(TAG_ABSENT, null, XMLStreamConstants.CHARACTERS), new PhyloXMLNoCharactersAllowedElementReader());
		
		putElementReader(new XMLElementReaderKey(TAG_ABSENT, TAG_BC, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_BINARY_CHARACTERS_ABSENT_BC, null, new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), false));
		//Element reader for character content of BC tag was registered before
		putElementReader(new XMLElementReaderKey(TAG_ABSENT, TAG_BC, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_BINARY_CHARACTERS, TAG_ABSENT, XMLStreamConstants.END_ELEMENT), resourceEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_BINARY_CHARACTERS, XMLStreamConstants.END_ELEMENT), resourceEndReader);
		
		//Clade.Distribution
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_DISTRIBUTION, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(null, PREDICATE_DISTRIBUTION, null, false));
		putElementReader(new XMLElementReaderKey(TAG_DISTRIBUTION, null, XMLStreamConstants.CHARACTERS), new PhyloXMLNoCharactersAllowedElementReader());
		
		putElementReader(new XMLElementReaderKey(TAG_DISTRIBUTION, TAG_DESC, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_DISTRIBUTION_DESC, null, new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), false));
		putElementReader(new XMLElementReaderKey(TAG_DESC, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_DISTRIBUTION, TAG_DESC, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_DISTRIBUTION, TAG_POINT, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(null, PREDICATE_DISTRIBUTION_POINT, null, false, ATTR_GEO_DATUM, PREDICATE_DISTRIBUTION_POINT_ATTR_GEODETIC_DATUM, 
						ATTR_ALT_UNIT, PREDICATE_DISTRIBUTION_POINT_ATTR_ALT_UNIT)); //TODO is there a java class similar to Point to parse the latitude/longitude/altitude values to?
		putElementReader(new XMLElementReaderKey(TAG_POINT, null, XMLStreamConstants.CHARACTERS), new PhyloXMLNoCharactersAllowedElementReader());
		
		putElementReader(new XMLElementReaderKey(TAG_POINT, TAG_LAT, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_DISTRIBUTION_POINT_LAT, null, new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_DECIMAL), false));
		putElementReader(new XMLElementReaderKey(TAG_LAT, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_DECIMAL));
		putElementReader(new XMLElementReaderKey(TAG_POINT, TAG_LAT, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_POINT, TAG_LONG, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_DISTRIBUTION_POINT_LONG, null, new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_DECIMAL), false));
		putElementReader(new XMLElementReaderKey(TAG_LONG, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_DECIMAL));
		putElementReader(new XMLElementReaderKey(TAG_POINT, TAG_LONG, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_POINT, TAG_ALT, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_DISTRIBUTION_POINT_ALT, null, new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_DECIMAL), false));
		putElementReader(new XMLElementReaderKey(TAG_ALT, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_DECIMAL));
		putElementReader(new XMLElementReaderKey(TAG_POINT, TAG_ALT, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_DISTRIBUTION, TAG_POINT, XMLStreamConstants.END_ELEMENT), resourceEndReader);		

		putElementReader(new XMLElementReaderKey(TAG_DISTRIBUTION, TAG_POLYGON, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(null, PREDICATE_DISTRIBUTION_POLYGON, null, false));
		putElementReader(new XMLElementReaderKey(TAG_POLYGON, null, XMLStreamConstants.CHARACTERS), new PhyloXMLNoCharactersAllowedElementReader());
		
		putElementReader(new XMLElementReaderKey(TAG_POLYGON, TAG_POINT, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(null, PREDICATE_DISTRIBUTION_POLYGON_POINT, null, false, ATTR_GEO_DATUM, PREDICATE_DISTRIBUTION_POLYGON_POINT_ATTR_GEODETIC_DATUM, 
						ATTR_ALT_UNIT, PREDICATE_DISTRIBUTION_POLYGON_POINT_ATTR_ALT_UNIT));
		//Element reader for character content of point tag was registered before
		
		putElementReader(new XMLElementReaderKey(TAG_POINT, TAG_LAT, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_DISTRIBUTION_POLYGON_POINT_LAT, null, new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_DECIMAL), false));
		putElementReader(new XMLElementReaderKey(TAG_LAT, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_DECIMAL));
		putElementReader(new XMLElementReaderKey(TAG_POINT, TAG_LAT, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_POINT, TAG_LONG, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_DISTRIBUTION_POLYGON_POINT_LONG, null, new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_DECIMAL), false));
		putElementReader(new XMLElementReaderKey(TAG_LONG, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_DECIMAL));
		putElementReader(new XMLElementReaderKey(TAG_POINT, TAG_LONG, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_POINT, TAG_ALT, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_DISTRIBUTION_POLYGON_POINT_ALT, null, new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_DECIMAL), false));
		putElementReader(new XMLElementReaderKey(TAG_ALT, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_DECIMAL));
		putElementReader(new XMLElementReaderKey(TAG_POINT, TAG_ALT, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_POLYGON, TAG_POINT, XMLStreamConstants.END_ELEMENT), resourceEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_DISTRIBUTION, XMLStreamConstants.END_ELEMENT), resourceEndReader);		
		
		//Clade.Date
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_DATE, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(null, PREDICATE_DATE, null, false, ATTR_UNIT, PREDICATE_DATE_ATTR_UNIT));
		//Element reader for character content of Clade.Date tag can not be registered here, because the character reader for Phylogeny.Date would be overwritten
		
		putElementReader(new XMLElementReaderKey(TAG_DATE, TAG_DESC, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_DATE_DESC, null, new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), false));
		putElementReader(new XMLElementReaderKey(TAG_DESC, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_TOKEN));
		putElementReader(new XMLElementReaderKey(TAG_DATE, TAG_DESC, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_DATE, TAG_VALUE, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_DATE_VALUE, null, new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_DECIMAL), false));
		putElementReader(new XMLElementReaderKey(TAG_VALUE, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_DECIMAL));
		putElementReader(new XMLElementReaderKey(TAG_DATE, TAG_VALUE, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_DATE, TAG_MINIMUM, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_DATE_MINIMUM, null, new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_DECIMAL), false));
		putElementReader(new XMLElementReaderKey(TAG_MINIMUM, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_DECIMAL));
		putElementReader(new XMLElementReaderKey(TAG_DATE, TAG_MINIMUM, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_DATE, TAG_MAXIMUM, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_DATE_MAXIMUM, null, new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_DECIMAL), false));
		putElementReader(new XMLElementReaderKey(TAG_MAXIMUM, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_DECIMAL));
		putElementReader(new XMLElementReaderKey(TAG_DATE, TAG_MAXIMUM, XMLStreamConstants.END_ELEMENT), literalEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_DATE, XMLStreamConstants.END_ELEMENT), resourceEndReader);
		
		//Clade.Reference
		putElementReader(new XMLElementReaderKey(TAG_CLADE, TAG_REFERENCE, XMLStreamConstants.START_ELEMENT), 
				new PhyloXMLStartElementReader(null, PREDICATE_REFERENCE, null, false, ATTR_DOI, PREDICATE_REFERENCE_ATTR_DOI));
		putElementReader(new XMLElementReaderKey(TAG_REFERENCE, null, XMLStreamConstants.CHARACTERS), new PhyloXMLNoCharactersAllowedElementReader());
		
		putElementReader(new XMLElementReaderKey(TAG_REFERENCE, TAG_DESC, XMLStreamConstants.START_ELEMENT),
				new PhyloXMLStartElementReader(PREDICATE_REFERENCE_DESC, null, new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_DECIMAL), false));
		putElementReader(new XMLElementReaderKey(TAG_DESC, null, XMLStreamConstants.CHARACTERS), new PhyloXMLCharactersElementReader(W3CXSConstants.DATA_TYPE_TOKEN));
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
	
					if (streamDataProvider.getParentName().equals(TAG_CLADE.getLocalPart())) {
						createNodeEvents(streamDataProvider);
						streamDataProvider.setCreateNodeStart(false);
					}
					else if (streamDataProvider.getParentName().equals(TAG_ROOT.getLocalPart()) && streamDataProvider.isCreateTreeGroupEnd()) {
						streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.TREE_NETWORK_GROUP));
						streamDataProvider.setCreateTreeGroupEnd(false);
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
							boolean isContinued = streamDataProvider.getXMLReader().peek().isCharacters();
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
	
	
	private void createPhylogenyStart(PhyloXMLReaderStreamDataProvider streamDataProvider) {
		String phylogenyLabel = streamDataProvider.getTreeLabel();
		String phylogenyID;
		EventContentType phylogenyType;
		
		if (getParameters().getBoolean(ReadWriteParameterMap.KEY_PHYLOXML_CONSIDER_PHYLOGENY_AS_TREE, false)) {
			phylogenyID = DEFAULT_TREE_ID_PREFIX + streamDataProvider.getIDManager().createNewID();
			phylogenyType = EventContentType.TREE;
		}
		else {
			phylogenyID = DEFAULT_NETWORK_ID_PREFIX + streamDataProvider.getIDManager().createNewID();
			phylogenyType = EventContentType.NETWORK;
		}
		
		Collection<JPhyloIOEvent> nestedEvents = streamDataProvider.resetCurrentEventCollection();
		streamDataProvider.getCurrentEventCollection().add(new LabeledIDEvent(phylogenyType, phylogenyID, phylogenyLabel));
		
		for (JPhyloIOEvent nextEvent : nestedEvents) {
			streamDataProvider.getCurrentEventCollection().add(nextEvent);
		}
		
		streamDataProvider.setCreatePhylogenyStart(false);
	}
	
	
	private void createNodeEvents(PhyloXMLReaderStreamDataProvider streamDataProvider) {
		if (streamDataProvider.hasSpecialEventCollection()) {
			streamDataProvider.resetCurrentEventCollection();
		}
		
		if (streamDataProvider.isCreateNodeStart()) {
			NodeEdgeInfo nodeInfo = streamDataProvider.getSourceNode().peek();
			
			if (streamDataProvider.getLastNodeID() != null) {
				streamDataProvider.getCladeIDToNodeEventIDMap().put(streamDataProvider.getLastNodeID(), nodeInfo.getID());
			}
			
			getStreamDataProvider().getCurrentEventCollection().add(new NodeEvent(nodeInfo.getID(), nodeInfo.getLabel(), null, nodeInfo.isRoot()));
			for (JPhyloIOEvent nextEvent : nodeInfo.getNestedNodeEvents()) {
				getStreamDataProvider().getCurrentEventCollection().add(nextEvent);  // Might lead to an exception, if nodeInfo.getNestedEvents() is the currentEventCollection at the time this method is called
			}
		}		
	}
	
	
	private void createEdgeEvents(PhyloXMLReaderStreamDataProvider streamDataProvider) {
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
				
				if (!getParameters().getBoolean(ReadWriteParameterMap.KEY_PHYLOXML_CONSIDER_PHYLOGENY_AS_TREE, false)) {
					streamDataProvider.getCurrentEventCollection().add(
							new LiteralMetadataEvent(DEFAULT_META_ID_PREFIX + streamDataProvider.getIDManager().createNewID(), null, 
							new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_IS_CROSSLINK), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_BOOLEAN), 
							LiteralContentSequenceType.SIMPLE));
			
					streamDataProvider.getCurrentEventCollection().add(new LiteralMetadataContentEvent(false, Boolean.toString(false)));
							
					streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
				}
				
				for (JPhyloIOEvent nextEvent : edgeInfo.getNestedEdgeEvents()) {
					getStreamDataProvider().getCurrentEventCollection().add(nextEvent);
				}
				
				streamDataProvider.getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(
						sourceID == null ? EventContentType.ROOT_EDGE : EventContentType.EDGE, EventTopologyType.END));
			}
		}
	}
	
	
	@Override
	protected PhyloXMLReaderStreamDataProvider createStreamDataProvider() {
		return new PhyloXMLReaderStreamDataProvider(this);
	}
	
	
	public static QName readDatatypeAttributeValue(String datatype, StartElement element) throws JPhyloIOReaderException {
		String prefix = null;
		String localPart = null;
		String namespaceURI = null;
		
		if (datatype != null) {
			if (datatype.contains(":")) {
				prefix = datatype.substring(0, datatype.indexOf(':'));
				localPart = datatype.substring(datatype.indexOf(':') + 1);				

				if (prefix.equals(XMLReadWriteUtils.XSD_DEFAULT_PRE)) {
					namespaceURI =  element.getNamespaceContext().getNamespaceURI(prefix);
					if (namespaceURI == null) { // no namespace was defined for xsd prefix						
						namespaceURI = XMLConstants.W3C_XML_SCHEMA_NS_URI;
					}	
				}
				else {
					throw new JPhyloIOReaderException("The \"datatype\" value of a property element must specify \"xsd\" as a prefix, instead the prefix was \"" + prefix +  "\".", element.getLocation());
				}						
			}
			else {
				throw new JPhyloIOReaderException("The \"datatype\" value of a property element must specify a prefix.", element.getLocation());
			}			
		}
		
		if ((prefix != null) && (localPart != null) && (namespaceURI != null)) {
			return new QName(namespaceURI, localPart, prefix);
		}
		else {
			throw new JPhyloIOReaderException("The \"datatype\" attribute of a property element contained the invalid value \"" + datatype + "\".", element.getLocation());
		}
	}
}
