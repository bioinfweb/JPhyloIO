/*
* JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015-2016  Ben Stöver, Sarah Wiechers
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
package info.bioinfweb.jphyloio.formats.nexml;


import info.bioinfweb.commons.bio.CharacterStateSetType;
import info.bioinfweb.commons.bio.CharacterSymbolType;
import info.bioinfweb.commons.io.XMLUtils;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.EdgeEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.LinkedLabeledIDEvent;
import info.bioinfweb.jphyloio.events.PartEndEvent;
import info.bioinfweb.jphyloio.events.SequenceTokensEvent;
import info.bioinfweb.jphyloio.events.SingleSequenceTokenEvent;
import info.bioinfweb.jphyloio.events.SingleTokenDefinitionEvent;
import info.bioinfweb.jphyloio.events.TokenSetDefinitionEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralContentSequenceType;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataContentEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.ResourceMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.URIOrStringIdentifier;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.exception.JPhyloIOReaderException;
import info.bioinfweb.jphyloio.formats.BufferedEventInfo;
import info.bioinfweb.jphyloio.formats.JPhyloIOFormatIDs;
import info.bioinfweb.jphyloio.formats.nexml.elementreader.AbstractNeXMLElementReader;
import info.bioinfweb.jphyloio.formats.nexml.elementreader.NeXMLMetaEndElementReader;
import info.bioinfweb.jphyloio.formats.nexml.elementreader.NeXMLMetaStartElementReader;
import info.bioinfweb.jphyloio.formats.xml.AbstractXMLEventReader;
import info.bioinfweb.jphyloio.formats.xml.CommentElementReader;
import info.bioinfweb.jphyloio.formats.xml.XMLElementReaderKey;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;



/**
 * An event reader for the <a href="http://nexml.org/">NeXML format</a>.
 * 
 * <h3><a name="parameters"></a>Recognized parameters</h3> 
 * <ul>
 *   <li>{@link ReadWriteParameterMap#KEY_ALLOW_DEFAULT_NAMESPACE}</li>
 *   <li>{@link ReadWriteParameterMap#KEY_NEXML_TOKEN_TRANSLATION_STRATEGY}</li>
 *   <li>{@link ReadWriteParameterMap#KEY_LOGGER}</li>
 * </ul>
 * 
 * @author Sarah Wiechers
 * @author Ben St&ouml;ver
 * @since 0.0.0
 */
public class NeXMLEventReader extends AbstractXMLEventReader<NeXMLReaderStreamDataProvider> implements NeXMLConstants {
	public NeXMLEventReader(InputStream stream, ReadWriteParameterMap parameters)	throws IOException, XMLStreamException {
		super(stream, parameters);
	}


	public NeXMLEventReader(Reader reader, ReadWriteParameterMap parameters) throws IOException, XMLStreamException {
		super(reader, parameters);
	}


	public NeXMLEventReader(XMLEventReader xmlReader,	ReadWriteParameterMap parameters) {
		super(xmlReader, parameters);
	}


	public NeXMLEventReader(File file, ReadWriteParameterMap parameters) throws IOException, XMLStreamException {
		super(file, parameters);
	}
	
	
	@Override
	public String getFormatID() {
		return JPhyloIOFormatIDs.NEXML_FORMAT_ID;
	}
	
	
	public TokenTranslationStrategy getTranslateTokens() {
		return getParameters().getTranslateTokens();
	}


	@SuppressWarnings("unchecked")
	@Override
	protected void fillMap() {
		AbstractNeXMLElementReader readMetaStart = new NeXMLMetaStartElementReader();
		
		AbstractNeXMLElementReader readMetaEnd = new NeXMLMetaEndElementReader();
		
		AbstractNeXMLElementReader readMetaWithPredicateStart = new NeXMLMetaStartElementReader() {
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				URIOrStringIdentifier predicate = null;
				
				if (streamDataProvider.getParentName().equals(TAG_FORMAT.getLocalPart())) {
					predicate = new URIOrStringIdentifier(null, PREDICATE_FORMAT);
				}
				else if (streamDataProvider.getParentName().equals(TAG_CHAR.getLocalPart())) {
					predicate = new URIOrStringIdentifier(null, PREDICATE_CHAR);
				}
				else if (streamDataProvider.getParentName().equals(TAG_MATRIX.getLocalPart())) {
					predicate = new URIOrStringIdentifier(null, PREDICATE_MATRIX);
				}
				//add cases if element reader is registered for more parent tags				
				
	    	streamDataProvider.getCurrentEventCollection().add(new ResourceMetadataEvent(RESERVED_ID_PREFIX + DEFAULT_META_ID_PREFIX + streamDataProvider.getIDManager().createNewID(), 
	    			null, predicate, null, null)); // ID conflict theoretically possible
				super.readEvent(streamDataProvider, event);
			}
		};
		
		AbstractNeXMLElementReader readMetaWithPredicateEnd = new NeXMLMetaEndElementReader() {
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				super.readEvent(streamDataProvider, event);
				streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_RESOURCE));
			}			
		};
		
		AbstractNeXMLElementReader readNodeStart = new AbstractNeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				StartElement element = event.asStartElement();
				OTUorOTUSEventInformation info = getOTUorOTUSEventInformation(streamDataProvider, element);
				boolean isRoot = XMLUtils.readBooleanAttr(element, ATTR_ROOT, false);
				
				if (isRoot) {
					streamDataProvider.getRootNodeIDs().add(info.id);
					streamDataProvider.setTrulyRooted(isRoot);
				}
				
				streamDataProvider.getCurrentEventCollection().add(new LinkedLabeledIDEvent(EventContentType.NODE, info.id,	info.label, info.otuOrOtusID));
			}
		};
		
		AbstractNeXMLElementReader readNodeEnd = new AbstractNeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.NODE));
			}
		};
		
		AbstractNeXMLElementReader readEdgeStart = new AbstractNeXMLElementReader() {
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				StartElement element = event.asStartElement();
				try {
					LabeledIDEventInformation info = getLabeledIDEventInformation(streamDataProvider, element);
					String targetID = XMLUtils.readStringAttr(element, ATTR_TARGET, null);
					double length = XMLUtils.readDoubleAttr(element, ATTR_LENGTH, Double.NaN); // It is not a problem for JPhyloIO, if floating point values are specified for IntTrees.

					if (targetID == null) {
						throw new JPhyloIOReaderException("The \"target\" attribute of an edge or rootedge definition in NeXML must not be omitted.", 
								element.getLocation());
					}
					else {
						streamDataProvider.getCurrentEventCollection().add(new EdgeEvent(info.id, info.label, 
								XMLUtils.readStringAttr(element, ATTR_SOURCE, null), targetID, length)); // The source ID will be null for rootedges, which is valid.
						
						if (streamDataProvider.getRootNodeIDs().contains(targetID)) {
							streamDataProvider.getCurrentEventCollection().add(new LiteralMetadataEvent(RESERVED_ID_PREFIX + DEFAULT_META_ID_PREFIX + streamDataProvider.getIDManager().createNewID(),
									null, new URIOrStringIdentifier(null, PREDICATE_TRUE_ROOT), LiteralContentSequenceType.SIMPLE)); //ID conflict theoretically possible
							streamDataProvider.getCurrentEventCollection().add(new LiteralMetadataContentEvent(null, Boolean.toString(true), 
									true, null)); //TODO also create meta event for edges that are not true roots?
							streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
							
							streamDataProvider.getRootNodeIDs().remove(targetID);
						}
					}
				}
				catch (NumberFormatException e) {
					throw new JPhyloIOReaderException("The attribute value \"" + element.getAttributeByName(ATTR_LENGTH).getValue() + 
							"\" is not a valid branch length.", element.getLocation(), e);
				}
			}
		};
		
		AbstractNeXMLElementReader readEdgeEnd = new AbstractNeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.EDGE));
			}
		};
		
		AbstractNeXMLElementReader readStateSetStart = new AbstractNeXMLElementReader() {		
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				StartElement element = event.asStartElement();
				LabeledIDEventInformation info = getLabeledIDEventInformation(streamDataProvider, element);
				String symbol = XMLUtils.readStringAttr(element, ATTR_SYMBOL, null);
				
	  		if (symbol == null) {
	  			throw new JPhyloIOReaderException("State tag must have an attribute called \"" + ATTR_SYMBOL + "\".", element.getLocation());
	  		}
	  		
	  		streamDataProvider.setCurrentSingleTokenDefinition(new NeXMLSingleTokenDefinitionInformation(info.id, info.label, symbol));
	  		streamDataProvider.getCurrentSingleTokenDefinition().setConstituents(new ArrayList<String>());
				streamDataProvider.setCurrentEventCollection(new ArrayList<JPhyloIOEvent>()); //meta events nested under this state set are buffered here
			}
		};
		
		AbstractNeXMLElementReader readStateSetEnd = new AbstractNeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				NeXMLSingleTokenDefinitionInformation info = streamDataProvider.getCurrentSingleTokenDefinition();
				String symbol = info.getSymbol();
				String translation = symbol;
				
				if (streamDataProvider.getCharacterSetType().equals(CharacterStateSetType.DISCRETE)) {					
					if (!streamDataProvider.getEventReader().getTranslateTokens().equals(TokenTranslationStrategy.NEVER)) {
		   			if (streamDataProvider.getEventReader().getTranslateTokens().equals(TokenTranslationStrategy.SYMBOL_TO_LABEL) 
		   					&& (info.getLabel() != null)) {
		   				translation = info.getLabel();
		   			}
		   			else {  // SYMBOL_TO_ID or label was null
		   				translation = info.getId();
		   			}
		   		}
					
					try {
						streamDataProvider.getTokenSets().get(streamDataProvider.getCurrentTokenSetID()).getSymbolTranslationMap().put(Integer.parseInt(symbol), translation);
					}
					catch (NumberFormatException e) {
						throw new JPhyloIOReaderException("The symbol of a standard data token definition must be of type Integer.", event.getLocation());
					}	  			
				}				
				
	  		if (symbol != null) {
					CharacterSymbolType tokenType;				
	  			if (streamDataProvider.getElementName().equals(TAG_POLYMORPHIC.getLocalPart())) {
						tokenType = CharacterSymbolType.POLYMORPHIC;
					}
					else {
						tokenType = CharacterSymbolType.UNCERTAIN;
					}
	  			
	  			streamDataProvider.getTokenDefinitionIDToSymbolMap().put(info.getId(), symbol);
	  			streamDataProvider.getCurrentEventCollection().add(new SingleTokenDefinitionEvent(info.getId(), info.getLabel(), symbol, 
	  					parseStateMeaning(symbol), tokenType, info.getConstituents()));
	  		
	  			Collection<JPhyloIOEvent> nestedEvents = streamDataProvider.resetCurrentEventCollection();
	  			for (JPhyloIOEvent nestedEvent : nestedEvents) {
	  				streamDataProvider.getCurrentEventCollection().add(nestedEvent);
	  			}
	  			
	  			streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.SINGLE_TOKEN_DEFINITION));
	  		}				
			}
		};
		
		AbstractNeXMLElementReader readMemberStart = new AbstractNeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				StartElement element = event.asStartElement();
				String state = XMLUtils.readStringAttr(element, ATTR_STATE, null);
				
				if (streamDataProvider.getTokenDefinitionIDToSymbolMap().containsKey(state)) {
					streamDataProvider.getCurrentSingleTokenDefinition().getConstituents().add(streamDataProvider.getTokenDefinitionIDToSymbolMap().get(state));
				}
				else {
					throw new JPhyloIOReaderException("A single token definition referenced the ID \"" + state + "\" of a state that was not specified before.", element.getLocation()); 
				}
			}
		};
		
		AbstractNeXMLElementReader emptyElementReader = new AbstractNeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {}
		};
		
		putElementReader(new XMLElementReaderKey(TAG_ROOT, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart);
		putElementReader(new XMLElementReaderKey(TAG_ROOT, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		putElementReader(new XMLElementReaderKey(TAG_META, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart);
		putElementReader(new XMLElementReaderKey(TAG_META, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		putElementReader(new XMLElementReaderKey(TAG_CHARACTERS, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart);
		putElementReader(new XMLElementReaderKey(TAG_CHARACTERS, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		putElementReader(new XMLElementReaderKey(TAG_OTUS, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart);
		putElementReader(new XMLElementReaderKey(TAG_OTUS, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		putElementReader(new XMLElementReaderKey(TAG_OTU, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart);
		putElementReader(new XMLElementReaderKey(TAG_OTU, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		putElementReader(new XMLElementReaderKey(TAG_FORMAT, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaWithPredicateStart);
		putElementReader(new XMLElementReaderKey(TAG_FORMAT, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaWithPredicateEnd);
		putElementReader(new XMLElementReaderKey(TAG_SET, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart); //TODO only charset meta should be read
		putElementReader(new XMLElementReaderKey(TAG_SET, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		putElementReader(new XMLElementReaderKey(TAG_STATES, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart);
		putElementReader(new XMLElementReaderKey(TAG_STATES, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		putElementReader(new XMLElementReaderKey(TAG_STATE, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart);
		putElementReader(new XMLElementReaderKey(TAG_STATE, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		putElementReader(new XMLElementReaderKey(TAG_UNCERTAIN, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart);
		putElementReader(new XMLElementReaderKey(TAG_UNCERTAIN, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		putElementReader(new XMLElementReaderKey(TAG_POLYMORPHIC, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart);
		putElementReader(new XMLElementReaderKey(TAG_POLYMORPHIC, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		putElementReader(new XMLElementReaderKey(TAG_CHAR, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaWithPredicateStart);
		putElementReader(new XMLElementReaderKey(TAG_CHAR, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaWithPredicateEnd);
		putElementReader(new XMLElementReaderKey(TAG_MATRIX, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaWithPredicateStart);
		putElementReader(new XMLElementReaderKey(TAG_MATRIX, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaWithPredicateEnd);
		putElementReader(new XMLElementReaderKey(TAG_ROW, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart);
		putElementReader(new XMLElementReaderKey(TAG_ROW, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		putElementReader(new XMLElementReaderKey(TAG_CELL, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart);
		putElementReader(new XMLElementReaderKey(TAG_CELL, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		putElementReader(new XMLElementReaderKey(TAG_TREES, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart);
		putElementReader(new XMLElementReaderKey(TAG_TREES, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		putElementReader(new XMLElementReaderKey(TAG_TREE, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart);
		putElementReader(new XMLElementReaderKey(TAG_TREE, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		putElementReader(new XMLElementReaderKey(TAG_NETWORK, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart);
		putElementReader(new XMLElementReaderKey(TAG_NETWORK, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		putElementReader(new XMLElementReaderKey(TAG_NODE, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart);
		putElementReader(new XMLElementReaderKey(TAG_NODE, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		putElementReader(new XMLElementReaderKey(TAG_EDGE, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart);
		putElementReader(new XMLElementReaderKey(TAG_EDGE, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		putElementReader(new XMLElementReaderKey(TAG_ROOTEDGE, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart);
		putElementReader(new XMLElementReaderKey(TAG_ROOTEDGE, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		
		putElementReader(new XMLElementReaderKey(TAG_META, null, XMLStreamConstants.CHARACTERS), new AbstractNeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {				
				if ((streamDataProvider.getMetaType() != null) && streamDataProvider.getMetaType().equals(EventContentType.META_LITERAL)) { //content events are only allowed under literal meta events
					String content = event.asCharacters().getData();
					
					if (!content.matches("\\s+")) {
						boolean isContinued = streamDataProvider.getXMLReader().peek().equals(XMLStreamConstants.CHARACTERS);
						streamDataProvider.getCurrentEventCollection().add(
								new LiteralMetadataContentEvent(event.asCharacters(), isContinued, streamDataProvider.getAlternativeStringRepresentation()));
						
						streamDataProvider.getCurrentEventCollection().add(new LiteralMetadataContentEvent(new URIOrStringIdentifier(null, streamDataProvider.getNestedMetaType()), 
								content, isContinued));
					}
				}
			}
		});
		
		putElementReader(new XMLElementReaderKey(null, null, XMLStreamConstants.START_ELEMENT), new AbstractNeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				if (!streamDataProvider.getMetaType().isEmpty() && streamDataProvider.getMetaType().peek().equals(EventContentType.META_LITERAL)) { //content events are only allowed under literal meta events					
					streamDataProvider.getCurrentEventCollection().add(
							new LiteralMetadataContentEvent(event.asStartElement(), false, streamDataProvider.getAlternativeStringRepresentation()));
				}
			}
		});
		
		putElementReader(new XMLElementReaderKey(null, null, XMLStreamConstants.CHARACTERS), new AbstractNeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {				
				if (!streamDataProvider.getMetaType().isEmpty() && streamDataProvider.getMetaType().peek().equals(EventContentType.META_LITERAL)) { //content events are only allowed under literal meta events
					String content = event.asCharacters().getData();
					if (!content.matches("\\s+")) {
						boolean isContinued = streamDataProvider.getXMLReader().peek().equals(XMLStreamConstants.CHARACTERS);
						streamDataProvider.getCurrentEventCollection().add(
								new LiteralMetadataContentEvent(event.asCharacters(), isContinued, streamDataProvider.getAlternativeStringRepresentation()));
					}
				}
			}
		});
		
		putElementReader(new XMLElementReaderKey(null, null, XMLStreamConstants.END_ELEMENT), new AbstractNeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				if (!streamDataProvider.getMetaType().isEmpty() && streamDataProvider.getMetaType().peek().equals(EventContentType.META_LITERAL)) { //content events are only allowed under literal meta events					
					streamDataProvider.getCurrentEventCollection().add(
							new LiteralMetadataContentEvent(event.asEndElement(), false, streamDataProvider.getAlternativeStringRepresentation()));
				}
			}
		});

		putElementReader(new XMLElementReaderKey(null, null, XMLStreamConstants.START_DOCUMENT), new AbstractNeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				streamDataProvider.getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(EventContentType.DOCUMENT, EventTopologyType.START));
			}
		});
		
		putElementReader(new XMLElementReaderKey(null, null, XMLStreamConstants.END_DOCUMENT), new AbstractNeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.DOCUMENT));
			}
		});
		
		putElementReader(new XMLElementReaderKey(null, TAG_ROOT, XMLStreamConstants.START_ELEMENT), emptyElementReader);
		
		putElementReader(new XMLElementReaderKey(TAG_ROOT, TAG_OTUS, XMLStreamConstants.START_ELEMENT), new AbstractNeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				StartElement element = event.asStartElement();
				LabeledIDEventInformation info = getLabeledIDEventInformation(streamDataProvider, element);
				streamDataProvider.getCurrentEventCollection().add(new LabeledIDEvent(EventContentType.OTU_LIST, info.id,	info.label));
			}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_ROOT, TAG_OTUS, XMLStreamConstants.END_ELEMENT), new AbstractNeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.OTU_LIST));
			}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_OTUS, TAG_OTU, XMLStreamConstants.START_ELEMENT), new AbstractNeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				StartElement element = event.asStartElement();
				LabeledIDEventInformation info = getLabeledIDEventInformation(streamDataProvider, element);
				
				streamDataProvider.getOtuIDToLabelMap().put(info.id, info.label);
				streamDataProvider.getCurrentEventCollection().add(new LabeledIDEvent(EventContentType.OTU, info.id, info.label));
			}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_OTUS, TAG_OTU, XMLStreamConstants.END_ELEMENT), new AbstractNeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.OTU));
			}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_ROOT, TAG_CHARACTERS, XMLStreamConstants.START_ELEMENT), new AbstractNeXMLElementReader() {		
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				StartElement element = event.asStartElement();
				OTUorOTUSEventInformation info = getOTUorOTUSEventInformation(streamDataProvider, element);
				
				String tokenSetTypeWithPrefix = XMLUtils.readStringAttr(element, ATTR_XSI_TYPE, null);			
				
				if (tokenSetTypeWithPrefix == null) {
					throw new JPhyloIOReaderException("Character tag must have an attribute called \"" + ATTR_XSI_TYPE + "\".", element.getLocation());
				}
				else {
					String tokenSetType = tokenSetTypeWithPrefix.split(":")[tokenSetTypeWithPrefix.split(":").length - 1];
					CharacterStateSetType setType = null;
					
					if (tokenSetType.equals(TYPE_DNA_SEQS) || tokenSetType.equals(TYPE_DNA_CELLS)) {
						info.label = "DNA";  //TODO Create constants (that may also contain spaces?)
						setType = CharacterStateSetType.DNA; //standard IUPAC nucleotide symbols
						streamDataProvider.setAllowLongTokens(false);
					}
					else if (tokenSetType.equals(TYPE_RNA_SEQS) || tokenSetType.equals(TYPE_RNA_CELLS)) {
						info.label = "RNA";
						setType = CharacterStateSetType.RNA;  //standard IUPAC nucleotide symbols
						streamDataProvider.setAllowLongTokens(false);
					}
					else if (tokenSetType.equals(TYPE_PROTEIN_SEQS) || tokenSetType.equals(TYPE_PROTEIN_CELLS)) {
						info.label = "AminoAcid";
						setType = CharacterStateSetType.AMINO_ACID;  //standard IUPAC amino acid symbols
						streamDataProvider.setAllowLongTokens(false);
					}
					else if (tokenSetType.equals(TYPE_CONTIN_SEQ) || tokenSetType.equals(TYPE_CONTIN_CELLS)) {
						info.label = "ContinuousData";
						setType = CharacterStateSetType.CONTINUOUS; 
						streamDataProvider.setAllowLongTokens(true);
					}
					else if (tokenSetType.equals(TYPE_RESTRICTION_SEQS) || tokenSetType.equals(TYPE_RESTRICTION_CELLS)) {
						info.label = "RestrictionSiteData";
						setType = CharacterStateSetType.DISCRETE; 
						streamDataProvider.setAllowLongTokens(false);
					}
					else { // type of character block is StandardSeqs or StandardCells
						info.label = "StandardData";
						setType = CharacterStateSetType.DISCRETE;
						streamDataProvider.setAllowLongTokens(true);
					}
					streamDataProvider.setCharacterSetType(setType);
				}
				
				streamDataProvider.getCurrentEventCollection().add(new LinkedLabeledIDEvent(EventContentType.ALIGNMENT, info.id, info.label, info.otuOrOtusID));
				streamDataProvider.setCurrentAlignmentID(info.id);
			}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_ROOT, TAG_CHARACTERS, XMLStreamConstants.END_ELEMENT), new AbstractNeXMLElementReader() {		
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.ALIGNMENT));
				streamDataProvider.getTokenSets().clear();
				streamDataProvider.getCharIDs().clear();
				streamDataProvider.getCharIDToIndexMap().clear();
				streamDataProvider.getTokenDefinitionIDToSymbolMap().clear();
			}
		});		
		
		putElementReader(new XMLElementReaderKey(TAG_CHARACTERS, TAG_FORMAT, XMLStreamConstants.START_ELEMENT), emptyElementReader);
		
		putElementReader(new XMLElementReaderKey(TAG_CHARACTERS, TAG_FORMAT, XMLStreamConstants.END_ELEMENT), new AbstractNeXMLElementReader() {		
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {								
				//Token set definitions
				Iterator<String> tokenSetIDIterator = streamDataProvider.getTokenSets().keySet().iterator();
				while (tokenSetIDIterator.hasNext()) {
					String tokenSetID = tokenSetIDIterator.next();
					NeXMLTokenSetInformation info = streamDataProvider.getTokenSets().get(tokenSetID);
					String[] columnIDs; 
					
					if (!info.getNestedEvents().isEmpty()) {						
						streamDataProvider.getCurrentEventCollection().add(new TokenSetDefinitionEvent(info.getSetType(), info.getID(), info.getLabel()));						
						
						for (JPhyloIOEvent nestedEvent : info.getNestedEvents()) {
							streamDataProvider.getCurrentEventCollection().add(nestedEvent);
						}
						
						columnIDs = streamDataProvider.getTokenSetIDtoColumnsMap().get(tokenSetID).toArray(new String[streamDataProvider.getTokenSetIDtoColumnsMap().get(tokenSetID).size()]);
						createIntervalEvents(streamDataProvider, columnIDs);
						
						streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.TOKEN_SET_DEFINITION));
					}
				}
			}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_FORMAT, TAG_STATES, XMLStreamConstants.START_ELEMENT), new AbstractNeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				StartElement element = event.asStartElement();
				LabeledIDEventInformation info = getLabeledIDEventInformation(streamDataProvider, element);
				
				if (info.label == null) {
					info.label = streamDataProvider.getCharacterSetType().toString();
				}

				streamDataProvider.setCurrentTokenSetID(info.id);
				streamDataProvider.getTokenSetIDtoColumnsMap().put(info.id, new ArrayList<String>());
				streamDataProvider.getTokenSets().put(info.id, new NeXMLTokenSetInformation(info.id, info.label, streamDataProvider.getCharacterSetType()));
				streamDataProvider.setCurrentEventCollection(streamDataProvider.getTokenSets().get(info.id).getNestedEvents());
			}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_FORMAT, TAG_STATES, XMLStreamConstants.END_ELEMENT), new AbstractNeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				streamDataProvider.resetCurrentEventCollection();
			}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_STATES, TAG_STATE, XMLStreamConstants.START_ELEMENT), new AbstractNeXMLElementReader() {		
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				StartElement element = event.asStartElement();
				LabeledIDEventInformation info = getLabeledIDEventInformation(streamDataProvider, element);
				String symbol = XMLUtils.readStringAttr(element, ATTR_SYMBOL, null);
				String translation = symbol;
				
				if (streamDataProvider.getCharacterSetType().equals(CharacterStateSetType.DISCRETE)) {					
					if (!streamDataProvider.getEventReader().getTranslateTokens().equals(TokenTranslationStrategy.NEVER)) {
		   			if (streamDataProvider.getEventReader().getTranslateTokens().equals(TokenTranslationStrategy.SYMBOL_TO_LABEL) 
		   					&& (info.label != null)) {
		   				translation = info.label;
		   			}
		   			else {  // SYMBOL_TO_ID or label was null
		   				translation = info.id;
		   			}
		   		}
					
					try {
						streamDataProvider.getTokenSets().get(streamDataProvider.getCurrentTokenSetID()).getSymbolTranslationMap().put(Integer.parseInt(symbol), translation);
					}
					catch (NumberFormatException e) {
						throw new JPhyloIOReaderException("The symbol of a standard data token definition must be of type Integer.", event.getLocation());
					}	  			
				}
				
	  		if (symbol != null) {	  			
	  			streamDataProvider.getTokenDefinitionIDToSymbolMap().put(info.id, symbol);
	  			streamDataProvider.getCurrentEventCollection().add(new SingleTokenDefinitionEvent(info.id, info.label, symbol, parseStateMeaning(symbol), CharacterSymbolType.ATOMIC_STATE));
	  		}
	  		else {
	  			throw new JPhyloIOReaderException("State tag must have an attribute called \"" + ATTR_SYMBOL + "\".", element.getLocation());
	  		}
			}
		});		
		
		putElementReader(new XMLElementReaderKey(TAG_STATES, TAG_STATE, XMLStreamConstants.END_ELEMENT), new AbstractNeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.SINGLE_TOKEN_DEFINITION));
			}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_STATES, TAG_UNCERTAIN, XMLStreamConstants.START_ELEMENT), readStateSetStart);
		putElementReader(new XMLElementReaderKey(TAG_STATES, TAG_UNCERTAIN, XMLStreamConstants.END_ELEMENT), readStateSetEnd);
		putElementReader(new XMLElementReaderKey(TAG_STATES, TAG_POLYMORPHIC, XMLStreamConstants.START_ELEMENT), readStateSetStart);
		putElementReader(new XMLElementReaderKey(TAG_STATES, TAG_POLYMORPHIC, XMLStreamConstants.END_ELEMENT), readStateSetEnd);
		
		putElementReader(new XMLElementReaderKey(TAG_UNCERTAIN, TAG_MEMBER, XMLStreamConstants.START_ELEMENT), readMemberStart);
		putElementReader(new XMLElementReaderKey(TAG_POLYMORPHIC, TAG_MEMBER, XMLStreamConstants.START_ELEMENT), readMemberStart);

		putElementReader(new XMLElementReaderKey(TAG_FORMAT, TAG_CHAR, XMLStreamConstants.START_ELEMENT), new AbstractNeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				StartElement element = event.asStartElement();
				LabeledIDEventInformation info = getLabeledIDEventInformation(streamDataProvider, element);
				String states =	XMLUtils.readStringAttr(element, ATTR_STATES, null);
				
				streamDataProvider.getCharIDToIndexMap().put(info.id, streamDataProvider.getCharIDs().size());
				streamDataProvider.getCharIDs().add(info.id);
				streamDataProvider.getCharIDToStatesMap().put(info.id, states);
				
				if (streamDataProvider.getTokenSetIDtoColumnsMap().get(states) != null) {
					streamDataProvider.getTokenSetIDtoColumnsMap().get(states).add(info.id);
				}
				else {
					throw new JPhyloIOReaderException("A character referenced the ID \"" + states + "\" of a token set that was not specified before.", element.getLocation()); 
				}				
				
				if ((element.getAttributeByName(ATTR_TOKENS) != null) || (element.getAttributeByName(ATTR_CODON_POSITION) != null)) {
					streamDataProvider.getCurrentEventCollection().add(new ResourceMetadataEvent(RESERVED_ID_PREFIX + DEFAULT_META_ID_PREFIX + streamDataProvider.getIDManager().createNewID(),
							null, new URIOrStringIdentifier(null, PREDICATE_CHAR), null, null));  // ID conflict theoretically possible
					readAttributes(streamDataProvider, element, RESERVED_ID_PREFIX, ATTR_TOKENS, PREDICATE_CHAR_ATTR_TOKENS, ATTR_CODON_POSITION, PREDICATE_CHAR_ATTR_CODON_POSITION);
					streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_RESOURCE));
				}	    			
			}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_FORMAT, TAG_CHAR, XMLStreamConstants.END_ELEMENT), emptyElementReader);
		
		putElementReader(new XMLElementReaderKey(TAG_FORMAT, TAG_SET, XMLStreamConstants.START_ELEMENT), new AbstractNeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				StartElement element = event.asStartElement();
				LabeledIDEventInformation info = getLabeledIDEventInformation(streamDataProvider, element);
				
				String charIDs = XMLUtils.readStringAttr(element, ATTR_CHAR, null);
				
				String[] charIDArray = charIDs.split(" "); //IDs are not allowed to contain spaces
						
				streamDataProvider.getCurrentEventCollection().add(new LinkedLabeledIDEvent(EventContentType.CHARACTER_SET, info.id, info.label, 
						streamDataProvider.getCurrentAlignmentID()));

				createIntervalEvents(streamDataProvider, charIDArray);
			}				
		});
		
		putElementReader(new XMLElementReaderKey(TAG_FORMAT, TAG_SET, XMLStreamConstants.END_ELEMENT), new AbstractNeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				streamDataProvider.getCurrentEventCollection().add(new PartEndEvent(EventContentType.CHARACTER_SET, true));				
			}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_MATRIX, TAG_ROW, XMLStreamConstants.START_ELEMENT), new AbstractNeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				StartElement element = event.asStartElement();
				OTUorOTUSEventInformation otuInfo = getOTUorOTUSEventInformation(streamDataProvider, element);
				
	  		streamDataProvider.getCurrentEventCollection().add(new LinkedLabeledIDEvent(EventContentType.SEQUENCE, otuInfo.id, otuInfo.label, otuInfo.otuOrOtusID));
			}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_MATRIX, TAG_ROW, XMLStreamConstants.END_ELEMENT), new AbstractNeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				streamDataProvider.getCurrentEventCollection().add(new PartEndEvent(EventContentType.SEQUENCE, true));
				
				// Related to handling cell tags that were not in the order of the columns:
				if (!streamDataProvider.getCurrentCellsBuffer().isEmpty()) {
					// All waiting events should have been consumed in the last call of the cell tag element reader.
					throw new JPhyloIOReaderException(streamDataProvider.getCurrentCellsBuffer().size() + 
							" cell tag(s) referencing an undeclared column ID was/were found.", event.getLocation());
				}
				
				streamDataProvider.clearCurrentRowInformation();
			}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_ROW, TAG_CELL, XMLStreamConstants.START_ELEMENT), new AbstractNeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				StartElement element = event.asStartElement();				
				String label = XMLUtils.readStringAttr(element, ATTR_LABEL, null);
				String tokenState = XMLUtils.readStringAttr(element, ATTR_STATE, null);
				String columnID = XMLUtils.readStringAttr(element, ATTR_CHAR, null);				
				String token = null;
				
				if (!streamDataProvider.getCharacterSetType().equals(CharacterStateSetType.CONTINUOUS)) {
					if (streamDataProvider.getTokenDefinitionIDToSymbolMap().containsKey(tokenState)) {
						token = streamDataProvider.getTokenDefinitionIDToSymbolMap().get(tokenState);
					}
					else {
						throw new JPhyloIOReaderException("A cell referenced the ID \"" + tokenState + "\" of a token definition that was not specified before.", event.getLocation());
					}
				}

				if (streamDataProvider.getCharacterSetType().equals(CharacterStateSetType.DISCRETE) 
						&& !streamDataProvider.getEventReader().getTranslateTokens().equals(TokenTranslationStrategy.NEVER)) {
					
					String currentStates = streamDataProvider.getCharIDToStatesMap().get(columnID);
		 	 		String translatedToken = streamDataProvider.getTokenSets().get(currentStates).getSymbolTranslationMap().get(token);
		 	 		if (translatedToken != null) {
		 	 			token = translatedToken;
		 	 		}
		 		}
				
				SingleSequenceTokenEvent currentTokenEvent = new SingleSequenceTokenEvent(label, token);
				
				// Handle cell tags that are not in the order of the columns:
				String expectedID = streamDataProvider.getCurrentExpectedCharID();
				if (expectedID == null) {
					throw new JPhyloIOReaderException("A row contained more cell tags than previously declared columns.", event.getLocation());
				}
				else if (expectedID.equals(columnID)) {  // Fire the current event:
					streamDataProvider.getCurrentEventCollection().add(currentTokenEvent);
					expectedID = streamDataProvider.nextCharID();  // Move iterator forward for next call of this method.
					streamDataProvider.setCurrentCellBuffered(false);
				}
				else {  // Buffer current events for later use:
					BufferedEventInfo<SingleSequenceTokenEvent> info = new BufferedEventInfo<SingleSequenceTokenEvent>(currentTokenEvent);
					streamDataProvider.getCurrentCellsBuffer().put(columnID, info);
					streamDataProvider.setCurrentEventCollection(info.getNestedEvents());
					streamDataProvider.setCurrentCellBuffered(true);
				}
			}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_ROW, TAG_CELL, XMLStreamConstants.END_ELEMENT), new AbstractNeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				if (streamDataProvider.isCurrentCellBuffered()) {
					streamDataProvider.resetCurrentEventCollection();  // Remove current buffer list.
				}
				else {
					streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.SINGLE_SEQUENCE_TOKEN));
				}
				
				// Fire all waiting events from the buffer that fit to the current position:
				BufferedEventInfo<SingleSequenceTokenEvent> info = streamDataProvider.getCurrentCellsBuffer().get(streamDataProvider.getCurrentExpectedCharID());
				while (info != null) {
					streamDataProvider.getCurrentEventCollection().add(info.getStartEvent());
					streamDataProvider.getCurrentEventCollection().addAll(info.getNestedEvents());
					streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.SINGLE_SEQUENCE_TOKEN));
					streamDataProvider.getCurrentCellsBuffer().remove(streamDataProvider.getCurrentExpectedCharID());
					
					info = streamDataProvider.getCurrentCellsBuffer().get(streamDataProvider.nextCharID());  // Move iterator forward for next iteration or next call of this method.
				}
			}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_SEQ, null, XMLStreamConstants.CHARACTERS), new AbstractNeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				String tokens = event.asCharacters().getData();
		   	streamDataProvider.getCurrentEventCollection().add(new SequenceTokensEvent(readSequence(streamDataProvider, tokens, streamDataProvider.getEventReader().getTranslateTokens())));				
			}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_ROOT, TAG_TREES, XMLStreamConstants.START_ELEMENT), new AbstractNeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				StartElement element = event.asStartElement();
				OTUorOTUSEventInformation info = getOTUorOTUSEventInformation(streamDataProvider, element);				
				
				streamDataProvider.getCurrentEventCollection().add(new LinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, info.id, info.label, info.otuOrOtusID));
			}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_ROOT, TAG_TREES, XMLStreamConstants.END_ELEMENT), new AbstractNeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.TREE_NETWORK_GROUP));
			}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_TREES, TAG_TREE, XMLStreamConstants.START_ELEMENT), new AbstractNeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				StartElement element = event.asStartElement();				
				OTUorOTUSEventInformation info = getOTUorOTUSEventInformation(streamDataProvider, element);
				String treeType = XMLUtils.readStringAttr(element, ATTR_XSI_TYPE, null);
				
				streamDataProvider.getCurrentEventCollection().add(new LinkedLabeledIDEvent(EventContentType.TREE, info.id,	info.label, info.otuOrOtusID));	  		
	  		streamDataProvider.setRootNodeIDs(new HashSet<String>());
				
				if (treeType == null) {
					throw new JPhyloIOReaderException("Tree tag must have an attribute called \"" + ATTR_XSI_TYPE + "\".", element.getLocation());
				}
			}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_TREES, TAG_TREE, XMLStreamConstants.END_ELEMENT), new AbstractNeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				if (!streamDataProvider.getRootNodeIDs().isEmpty()) {
					for (String rootNodeID : streamDataProvider.getRootNodeIDs()) {
						streamDataProvider.getCurrentEventCollection().add(new EdgeEvent(RESERVED_ID_PREFIX + DEFAULT_EDGE_ID_PREFIX + streamDataProvider.getIDManager().createNewID(),
								null, null, rootNodeID, Double.NaN)); //ID conflict theoretically possible
	
						streamDataProvider.getCurrentEventCollection().add(new LiteralMetadataEvent(RESERVED_ID_PREFIX + DEFAULT_META_ID_PREFIX + streamDataProvider.getIDManager().createNewID(),
								null, new URIOrStringIdentifier(null, PREDICATE_TRUE_ROOT), LiteralContentSequenceType.SIMPLE)); //ID conflict theoretically possible
						streamDataProvider.getCurrentEventCollection().add(new LiteralMetadataContentEvent(null, Boolean.toString(true), true, null));
						streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));				
						
						streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.EDGE));
					}
				}
				
				streamDataProvider.getCurrentEventCollection().add(new LiteralMetadataEvent(RESERVED_ID_PREFIX + DEFAULT_META_ID_PREFIX + streamDataProvider.getIDManager().createNewID(),
						null, new URIOrStringIdentifier(null, PREDICATE_DISPLAY_TREE_ROOTED), LiteralContentSequenceType.SIMPLE)); //ID conflict theoretically possible
				streamDataProvider.getCurrentEventCollection().add(new LiteralMetadataContentEvent(null, Boolean.toString(streamDataProvider.isTrulyRooted()), 
						streamDataProvider.isTrulyRooted(), null));
				streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
				
				streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.TREE));
				
				streamDataProvider.getRootNodeIDs().clear();
			}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_TREES, TAG_NETWORK, XMLStreamConstants.START_ELEMENT), new AbstractNeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				StartElement element = event.asStartElement();
				OTUorOTUSEventInformation otuInfo = getOTUorOTUSEventInformation(streamDataProvider, element);
				
	  		streamDataProvider.getCurrentEventCollection().add(new LinkedLabeledIDEvent(EventContentType.NETWORK, otuInfo.id, otuInfo.label, otuInfo.otuOrOtusID));
	  		streamDataProvider.setRootNodeIDs(new HashSet<String>());
			}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_TREES, TAG_NETWORK, XMLStreamConstants.END_ELEMENT), new AbstractNeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.NETWORK));
				streamDataProvider.getRootNodeIDs().clear();
			}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_TREE, TAG_NODE, XMLStreamConstants.START_ELEMENT), readNodeStart);
		putElementReader(new XMLElementReaderKey(TAG_TREE, TAG_NODE, XMLStreamConstants.END_ELEMENT), readNodeEnd);
		putElementReader(new XMLElementReaderKey(TAG_NETWORK, TAG_NODE, XMLStreamConstants.START_ELEMENT), readNodeStart);
		putElementReader(new XMLElementReaderKey(TAG_NETWORK, TAG_NODE, XMLStreamConstants.END_ELEMENT), readNodeEnd);

		putElementReader(new XMLElementReaderKey(TAG_TREE, TAG_EDGE, XMLStreamConstants.START_ELEMENT), readEdgeStart);
		putElementReader(new XMLElementReaderKey(TAG_TREE, TAG_EDGE, XMLStreamConstants.END_ELEMENT), readEdgeEnd);
		putElementReader(new XMLElementReaderKey(TAG_TREE, TAG_ROOTEDGE, XMLStreamConstants.START_ELEMENT), readEdgeStart);
		putElementReader(new XMLElementReaderKey(TAG_TREE, TAG_ROOTEDGE, XMLStreamConstants.END_ELEMENT), readEdgeEnd);
		putElementReader(new XMLElementReaderKey(TAG_NETWORK, TAG_EDGE, XMLStreamConstants.START_ELEMENT), readEdgeStart);
		putElementReader(new XMLElementReaderKey(TAG_NETWORK, TAG_EDGE, XMLStreamConstants.END_ELEMENT), readEdgeEnd);
		
		putElementReader(new XMLElementReaderKey(null, null, XMLStreamConstants.COMMENT), new CommentElementReader());
	}
	

	@Override
	protected NeXMLReaderStreamDataProvider createStreamDataProvider() {
		return new NeXMLReaderStreamDataProvider(this);
	}
}
