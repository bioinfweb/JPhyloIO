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
import info.bioinfweb.commons.bio.CharacterSymbolMeaning;
import info.bioinfweb.commons.bio.CharacterSymbolType;
import info.bioinfweb.commons.io.XMLUtils;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.CharacterSetIntervalEvent;
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
import info.bioinfweb.jphyloio.formats.JPhyloIOFormatIDs;
import info.bioinfweb.jphyloio.formats.xml.AbstractXMLEventReader;
import info.bioinfweb.jphyloio.formats.xml.CommentElementReader;
import info.bioinfweb.jphyloio.formats.xml.XMLElementReaderKey;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.namespace.QName;
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
		AbstractNeXMLElementReader readMetaStart = new AbstractNeXMLElementReader() {
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
	    	StartElement element = event.asStartElement();
	    	LabeledIDEventInformation info = getLabeledIDEventInformation(streamDataProvider, element);
	    	String typeWithPrefix = XMLUtils.readStringAttr(element, ATTR_XSI_TYPE, null);
	    	URIOrStringIdentifier predicate;
					
	    	if ((info.id != null) && !info.id.equals("")) {
	    		if (typeWithPrefix != null) {
	    			String type = typeWithPrefix.split(":")[typeWithPrefix.split(":").length - 1];
			  		if (type.equals(TYPE_LITERAL_META)) {
			  			streamDataProvider.setMetaType(EventContentType.META_LITERAL);
			  			predicate = new URIOrStringIdentifier(null, new QName(XMLUtils.readStringAttr(element, ATTR_PROPERTY, null)));
			  			String content = XMLUtils.readStringAttr(element, ATTR_CONTENT, null);
			  			streamDataProvider.setAlternativeStringRepresentation(content);
			  			
			  			streamDataProvider.getCurrentEventCollection().add(new LiteralMetadataEvent(info.id, info.label, predicate, LiteralContentSequenceType.XML));
	
	//		  			String[] dataType = XMLUtils.readStringAttr(element, ATTR_DATATYPE, null).split(":");
	//		  			UriOrStringIdentifier originalType = new UriOrStringIdentifier(null, new QName(streamDataProvider.getNamespaceMap().get(dataType[0]), dataType[1], dataType[0]));	  			
			  			
	//		  			LiteralContentSequenceType contentType = null;
	//		  			if (streamDataProvider.getNamespaceMap().get(dataType[0]).equals(XMLConstants.W3C_XML_SCHEMA_NS_URI) 
	//		  					|| streamDataProvider.getNamespaceMap().get(dataType[0]).equals(XMLConstants.W3C_XML_SCHEMA_NS_URI + "#")) { //TODO is this okay? What about the '#'?
	//		  				contentType = LiteralContentSequenceType.SIMPLE; //TODO Delegate java object construction to ontology definition instance, which is able to convert a QName to a Java class.		
	//		  			}
	//		  			else {
	//		  				contentType = LiteralContentSequenceType.XML;
	//		  				//TODO Create XMLStreamReader instance here, if nested XML is present.
	//			  		}
			  		}
			  		else if (type.equals(TYPE_RESOURCE_META)) {
			  			streamDataProvider.setMetaType(EventContentType.META_RESOURCE);
			  			predicate = new URIOrStringIdentifier(null, new QName(XMLUtils.readStringAttr(element, ATTR_REL, null)));
			  			String about = XMLUtils.readStringAttr(element, ATTR_ABOUT, null);
			  			URI href = null;
			  			try {
			  				href = new URI(XMLUtils.readStringAttr(element, ATTR_HREF, null));
			  			} 
			  			catch (URISyntaxException e) {}
			  			
			  			streamDataProvider.getCurrentEventCollection().add(new ResourceMetadataEvent(info.id, info.label, predicate, href, about));	  			
			  		}
			  		else {
			  			throw new JPhyloIOReaderException("Meta annotations can only be of type \"" + TYPE_LITERAL_META + "\" or \"" + 
			  					TYPE_RESOURCE_META + "\".", element.getLocation());
			  		}
		    	}
	    		else {
						throw new JPhyloIOReaderException("Meta tag must have an attribute called \"" + ATTR_XSI_TYPE + "\".", element.getLocation());
					}
	    	}
			}
		};
		
		AbstractNeXMLElementReader readMetaEnd = new AbstractNeXMLElementReader() {
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				EventContentType type = streamDataProvider.getMetaType();
				
				if (type != null) {
					if (type.equals(EventContentType.META_LITERAL)) {
						streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
					}
					else if (type.equals(EventContentType.META_RESOURCE)) {
						streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_RESOURCE));
					}					
					streamDataProvider.setMetaType(null);
				}
			}
		};
		
		AbstractNeXMLElementReader readNodeStart = new AbstractNeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				StartElement element = event.asStartElement();
				OTUorOTUSEventInformation info = getOTUorOTUSEventInformation(streamDataProvider, element);
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
					String edgeID = XMLUtils.readStringAttr(element, ATTR_ID, null);
					String targetID = XMLUtils.readStringAttr(element, ATTR_TARGET, null);
					double length = XMLUtils.readDoubleAttr(element, ATTR_LENGTH, Double.NaN);	// It is not a problem for JPhyloIO, if floating point values are specified for IntTrees.

					if (edgeID == null) {
						throw new JPhyloIOReaderException("The \"id\" attribute of an edge or rootedge definition in NeXML must not be omitted.", 
								element.getLocation());
					}
					else if (targetID == null) {
						throw new JPhyloIOReaderException("The \"target\" attribute of an edge or rootedge definition in NeXML must not be omitted.", 
								element.getLocation());
					}
					else {
						streamDataProvider.getCurrentEventCollection().add(new EdgeEvent(edgeID, XMLUtils.readStringAttr(element, ATTR_LABEL, null), 
								XMLUtils.readStringAttr(element, ATTR_SOURCE, null), targetID, length));  // The source ID will be null for rootedges, which is valid.
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
		
		AbstractNeXMLElementReader readStateStart = new AbstractNeXMLElementReader() {		
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				StartElement element = event.asStartElement();
				LabeledIDEventInformation info = getLabeledIDEventInformation(streamDataProvider, element);
				String symbol = XMLUtils.readStringAttr(element, ATTR_SYMBOL, null);
				String translation = symbol;
				
				if (!streamDataProvider.getEventReader().getTranslateTokens().equals(TokenTranslationStrategy.NEVER)) {
	   			if (streamDataProvider.getEventReader().getTranslateTokens().equals(TokenTranslationStrategy.SYMBOL_TO_LABEL) 
	   					&& (info.label != null)) {
	   				translation = info.label;
	   			}
	   			else { //SYMBOL_TO_ID or label was null
	   				translation = info.id;
	   			}
	   		}
				
	  		if (symbol != null) {
	  			streamDataProvider.setSymbol(symbol);
	  			streamDataProvider.getTokenSets().get(streamDataProvider.getTokenSetID()).getSymbolTranslationMap().put(symbol, translation);
	  			streamDataProvider.getTokenDefinitionIDToSymbolMap().put(info.id, symbol);
	  		}
	  		else {
	  			throw new JPhyloIOReaderException("State tag must have an attribute called \"" + ATTR_SYMBOL + "\".", element.getLocation());
	  		}
	  		
	  		List<String> currentConstitituents = new ArrayList<String>();
	  		streamDataProvider.setTokenDefinitionInfo(info);
				streamDataProvider.setConstituents(currentConstitituents);
				streamDataProvider.setCurrentEventCollection(new ArrayList<JPhyloIOEvent>());
			}
		};
		
		AbstractNeXMLElementReader readStateEnd = new AbstractNeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				String symbol = streamDataProvider.getSymbol();
				CharacterSymbolMeaning meaning;
				String parent = streamDataProvider.getParentName();
				CharacterSymbolType tokenType;
				
				if (symbol.equals("?")) {
					meaning = CharacterSymbolMeaning.MISSING;
				}
				else if (symbol.equals("-")) {
					meaning = CharacterSymbolMeaning.GAP;
				}
				else {
					meaning = CharacterSymbolMeaning.CHARACTER_STATE;
				}
				
				if (parent.equals(TAG_POLYMORPHIC.getLocalPart())) {
					tokenType = CharacterSymbolType.POLYMORPHIC;
				}
				else if (parent.equals(TAG_UNCERTAIN.getLocalPart())) {
					tokenType = CharacterSymbolType.UNCERTAIN;
				}
				else {
					tokenType = CharacterSymbolType.ATOMIC_STATE;
				}
				
				Collection<JPhyloIOEvent> currentEventCollection = streamDataProvider.resetCurrentEventCollection();				
				LabeledIDEventInformation info = streamDataProvider.getTokenDefinitionInfo();
				
				streamDataProvider.getCurrentEventCollection().add(new SingleTokenDefinitionEvent(info.id, info.label, symbol, meaning, 
						tokenType, streamDataProvider.getConstituents()));
  			Iterator<JPhyloIOEvent> subEventIterator = currentEventCollection.iterator();
  			while (subEventIterator.hasNext()) {
  				streamDataProvider.getCurrentEventCollection().add(subEventIterator.next());
  			}
				streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.SINGLE_TOKEN_DEFINITION));
			}
		};
		
		AbstractNeXMLElementReader readMemberStart = new AbstractNeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				StartElement element = event.asStartElement();
				String state = XMLUtils.readStringAttr(element, ATTR_STATE, null);
				streamDataProvider.getConstituents().add(streamDataProvider.getTokenDefinitionIDToSymbolMap().get(state));
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
		putElementReader(new XMLElementReaderKey(TAG_FORMAT, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart);
		putElementReader(new XMLElementReaderKey(TAG_FORMAT, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		putElementReader(new XMLElementReaderKey(TAG_STATES, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart);
		putElementReader(new XMLElementReaderKey(TAG_STATES, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		putElementReader(new XMLElementReaderKey(TAG_STATE, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart);
		putElementReader(new XMLElementReaderKey(TAG_STATE, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		putElementReader(new XMLElementReaderKey(TAG_UNCERTAIN, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart);
		putElementReader(new XMLElementReaderKey(TAG_UNCERTAIN, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		putElementReader(new XMLElementReaderKey(TAG_POLYMORPHIC, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart);
		putElementReader(new XMLElementReaderKey(TAG_POLYMORPHIC, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		putElementReader(new XMLElementReaderKey(TAG_CHAR, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart);
		putElementReader(new XMLElementReaderKey(TAG_CHAR, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		putElementReader(new XMLElementReaderKey(TAG_MATRIX, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart);
		putElementReader(new XMLElementReaderKey(TAG_MATRIX, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		putElementReader(new XMLElementReaderKey(TAG_ROW, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart);
		putElementReader(new XMLElementReaderKey(TAG_ROW, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		putElementReader(new XMLElementReaderKey(TAG_TREE, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart);
		putElementReader(new XMLElementReaderKey(TAG_TREE, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		putElementReader(new XMLElementReaderKey(TAG_NETWORK, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart);
		putElementReader(new XMLElementReaderKey(TAG_NETWORK, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		putElementReader(new XMLElementReaderKey(TAG_NODE, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart);
		putElementReader(new XMLElementReaderKey(TAG_NODE, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		putElementReader(new XMLElementReaderKey(TAG_EDGE, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart);
		putElementReader(new XMLElementReaderKey(TAG_EDGE, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		
		putElementReader(new XMLElementReaderKey(null, null, XMLStreamConstants.START_ELEMENT), new AbstractNeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				if ((streamDataProvider.getMetaType() != null) && streamDataProvider.getMetaType().equals(EventContentType.META_LITERAL)) { //content events are only allowed under literal meta events					
					streamDataProvider.getCurrentEventCollection().add(
							new LiteralMetadataContentEvent(event.asStartElement(), false, streamDataProvider.getAlternativeStringRepresentation()));
				}
			}
		});
		
		putElementReader(new XMLElementReaderKey(null, null, XMLStreamConstants.CHARACTERS), new AbstractNeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {				
				if ((streamDataProvider.getMetaType() != null) && streamDataProvider.getMetaType().equals(EventContentType.META_LITERAL)) { //content events are only allowed under literal meta events
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
				if ((streamDataProvider.getMetaType() != null) && streamDataProvider.getMetaType().equals(EventContentType.META_LITERAL)) { //content events are only allowed under literal meta events					
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
						info.label = "DNA";
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
			}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_ROOT, TAG_CHARACTERS, XMLStreamConstants.END_ELEMENT), new AbstractNeXMLElementReader() {		
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.ALIGNMENT));
				streamDataProvider.getTokenSets().clear();
			}
		});		
		
		putElementReader(new XMLElementReaderKey(TAG_CHARACTERS, TAG_FORMAT, XMLStreamConstants.START_ELEMENT), new AbstractNeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				List<String> charIDs = new ArrayList<String>();
				Map<String, String> charIDToStatesMap = new HashMap<String, String>();
				Set<String> directCharSetIDs = new TreeSet<String>();
				
				streamDataProvider.setCharIDs(charIDs);
				streamDataProvider.setCharIDToStatesMap(charIDToStatesMap);
				streamDataProvider.setDirectCharSetIDs(directCharSetIDs);				
			}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_CHARACTERS, TAG_FORMAT, XMLStreamConstants.END_ELEMENT), new AbstractNeXMLElementReader() {		
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {				
				//Indirect char sets
				if (!streamDataProvider.getTokenSets().isEmpty()) { //there are no token sets for continuous data and therefore no indirect char sets
					if (streamDataProvider.getTokenSets().size() == 1) {
						streamDataProvider.getTokenSets().get(streamDataProvider.getTokenSetID()).getCharSetIntervals()
								.add(new CharacterSetIntervalEvent(0, streamDataProvider.getCharIDs().size() - 1));
					}					 
					else {
						String currentStates = null;
						String previousStates = null;
						int startChar = 0;
						for (int i = 0; i < streamDataProvider.getCharIDs().size(); i++) {
							previousStates = currentStates;
							currentStates = streamDataProvider.getCharIDToStatesMap().get(streamDataProvider.getCharIDs().get(i));
							if ((previousStates != null) && !currentStates.equals(previousStates)) {						
								streamDataProvider.getTokenSets().get(previousStates).getCharSetIntervals()
										.add(new CharacterSetIntervalEvent(startChar, i - 1));
								startChar = i;
							}						
						}
						streamDataProvider.getTokenSets().get(currentStates).getCharSetIntervals()
								.add(new CharacterSetIntervalEvent(startChar, streamDataProvider.getCharIDs().size() - 1));
					}
								
					Iterator<String> tokenSetIDIterator = streamDataProvider.getTokenSets().keySet().iterator();
					while (tokenSetIDIterator.hasNext()) {
						String tokenSetID = tokenSetIDIterator.next();
						NeXMLTokenSetInformation info = streamDataProvider.getTokenSets().get(tokenSetID);
						String charSetID;
						if (!info.getCharSetIntervals().isEmpty()) {						
							do {
								charSetID = DEFAULT_CHAR_SET_ID_PREFIX + streamDataProvider.getIDManager().createNewID();
							} 
							while (!streamDataProvider.getDirectCharSetIDs().add(charSetID));
							info.setCharacterSetID(charSetID);
							streamDataProvider.getCurrentEventCollection().add(new LabeledIDEvent(EventContentType.CHARACTER_SET, charSetID, null));
							
							Iterator<JPhyloIOEvent> charSetEventsIterator = info.getCharSetIntervals().iterator();					
							while (charSetEventsIterator.hasNext()) {
								streamDataProvider.getCurrentEventCollection().add(charSetEventsIterator.next());
							}
	
							streamDataProvider.getCurrentEventCollection().add(new PartEndEvent(EventContentType.CHARACTER_SET, true));
						}
					}
				}
				
				//Token set definitions
				Iterator<String> indirectTokenSetIDIterator = streamDataProvider.getTokenSets().keySet().iterator();
				while (indirectTokenSetIDIterator.hasNext()) {
					String tokenSetID = indirectTokenSetIDIterator.next();
					NeXMLTokenSetInformation info = streamDataProvider.getTokenSets().get(tokenSetID);
					CharacterStateSetType type = info.getSetType();
					String id = info.getID();
					String label = info.getLabel();
					String charSetID = info.getCharacterSetID();
					if (!info.getSingleTokens().isEmpty()) {
						streamDataProvider.getCurrentEventCollection().add(new TokenSetDefinitionEvent(type, id, label, charSetID));
						Iterator<JPhyloIOEvent> tokenEventsIterator = info.getSingleTokens().iterator();					
						while (tokenEventsIterator.hasNext()) {
							streamDataProvider.getCurrentEventCollection().add(tokenEventsIterator.next());
						}
					}
				}
			}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_FORMAT, TAG_SET, XMLStreamConstants.START_ELEMENT), new AbstractNeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				StartElement element = event.asStartElement();
				LabeledIDEventInformation info = getLabeledIDEventInformation(streamDataProvider, element);
				
				String charIDs = XMLUtils.readStringAttr(element, ATTR_CHAR, null);
				
				String[] charIDArray = charIDs.split(" "); //IDs are not allowed to contain spaces
				streamDataProvider.getDirectCharSetIDs().add(info.id);
						
				streamDataProvider.getCurrentEventCollection().add(new LabeledIDEvent(EventContentType.CHARACTER_SET, info.id, info.label));

				boolean[] charSets = new boolean[streamDataProvider.getCharIDs().size()];
				for (String charID: charIDArray) {	
					charSets[streamDataProvider.getCharIDs().indexOf(charID)] = true;					
				}
				
				int currentIndex = -1;
				int startIndex = -1;
				
				for (int i = 0; i < charSets.length; i++) {
					if (charSets[i]) {
						currentIndex = i;
					}
					
					if (charSets[i] && !charSets[i - 1]) {
						startIndex = i;
					}
					else if (charSets[i] && ((i + 1 == charSets.length) || !charSets[i + 1])) {
						streamDataProvider.getCurrentEventCollection().add(new CharacterSetIntervalEvent(startIndex, currentIndex));
					}
				}
			}				
		});
		
		putElementReader(new XMLElementReaderKey(TAG_FORMAT, TAG_SET, XMLStreamConstants.END_ELEMENT), new AbstractNeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				streamDataProvider.getCurrentEventCollection().add(new PartEndEvent(EventContentType.CHARACTER_SET, true));				
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
				
				List<JPhyloIOEvent> singleTokens = new ArrayList<JPhyloIOEvent>();
				List<JPhyloIOEvent> charSetIntervals = new ArrayList<JPhyloIOEvent>();
				Map<String, String> symbolToLabelMap = new HashMap<String, String>();
				Map<String, String> idToSymbolMap = new HashMap<String, String>();
				
				streamDataProvider.setTokenSetID(info.id);				
				streamDataProvider.getTokenSets().put(info.id, new NeXMLTokenSetInformation(info.id, info.label, streamDataProvider.getCharacterSetType()));
				
				streamDataProvider.getTokenSets().get(info.id).setSingleTokens(singleTokens);
				streamDataProvider.getTokenSets().get(info.id).setCharSetIntervals(charSetIntervals);
				streamDataProvider.getTokenSets().get(info.id).setSymbolTranslationMap(symbolToLabelMap);
				streamDataProvider.setTokenDefinitionIDToSymbolMap(idToSymbolMap);
				
				streamDataProvider.setCurrentEventCollection(streamDataProvider.getTokenSets().get(info.id).getSingleTokens());
			}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_FORMAT, TAG_STATES, XMLStreamConstants.END_ELEMENT), new AbstractNeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.TOKEN_SET_DEFINITION));
				streamDataProvider.resetCurrentEventCollection();
			}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_STATES, TAG_STATE, XMLStreamConstants.START_ELEMENT), readStateStart);
		putElementReader(new XMLElementReaderKey(TAG_STATES, TAG_STATE, XMLStreamConstants.END_ELEMENT), readStateEnd);
		putElementReader(new XMLElementReaderKey(TAG_STATES, TAG_UNCERTAIN, XMLStreamConstants.START_ELEMENT), readStateStart);
		putElementReader(new XMLElementReaderKey(TAG_STATES, TAG_UNCERTAIN, XMLStreamConstants.END_ELEMENT), readStateEnd);
		putElementReader(new XMLElementReaderKey(TAG_STATES, TAG_POLYMORPHIC, XMLStreamConstants.START_ELEMENT), readStateStart);
		putElementReader(new XMLElementReaderKey(TAG_STATES, TAG_POLYMORPHIC, XMLStreamConstants.END_ELEMENT), readStateEnd);
		
		putElementReader(new XMLElementReaderKey(TAG_UNCERTAIN, TAG_MEMBER, XMLStreamConstants.START_ELEMENT), readMemberStart);
		putElementReader(new XMLElementReaderKey(TAG_POLYMORPHIC, TAG_MEMBER, XMLStreamConstants.START_ELEMENT), readMemberStart);

		putElementReader(new XMLElementReaderKey(TAG_FORMAT, TAG_CHAR, XMLStreamConstants.START_ELEMENT), new AbstractNeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				StartElement element = event.asStartElement();
				LabeledIDEventInformation info = getLabeledIDEventInformation(streamDataProvider, element);
				String states =	XMLUtils.readStringAttr(element, ATTR_STATES, null);
				
				streamDataProvider.getCharIDs().add(info.id);
				streamDataProvider.getCharIDToStatesMap().put(info.id, states);
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
			}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_ROW, TAG_CELL, XMLStreamConstants.START_ELEMENT), new AbstractNeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				StartElement element = event.asStartElement();
				
				String label = XMLUtils.readStringAttr(element, ATTR_LABEL, null);
				String token = XMLUtils.readStringAttr(element, ATTR_STATE, null);
				String character = XMLUtils.readStringAttr(element, ATTR_CHAR, null);
				
				if (streamDataProvider.getCharacterSetType().equals(CharacterStateSetType.DISCRETE) 
						&& !streamDataProvider.getEventReader().getTranslateTokens().equals(TokenTranslationStrategy.NEVER)) {
					
					String currentStates = streamDataProvider.getCharIDToStatesMap().get(character);
		 	 		String translatedToken = streamDataProvider.getTokenSets().get(currentStates).getSymbolTranslationMap().get(token);
		 	 		if (translatedToken != null) {
		 	 			token = translatedToken;
		 	 		}
		 		}
				
				streamDataProvider.getCurrentEventCollection().add(new SingleSequenceTokenEvent(label, token));
			}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_ROW, TAG_CELL, XMLStreamConstants.END_ELEMENT), new AbstractNeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.SINGLE_SEQUENCE_TOKEN));
			}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_SEQ, null, XMLStreamConstants.CHARACTERS), new AbstractNeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				String tokens = event.asCharacters().getData(); // TODO Influence read length of character events, may not be  possible
		   	streamDataProvider.getCurrentEventCollection().add(new SequenceTokensEvent(readSequence(streamDataProvider, tokens, streamDataProvider.getEventReader().getTranslateTokens())));				
			}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_TREES, TAG_TREE, XMLStreamConstants.START_ELEMENT), new AbstractNeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				StartElement element = event.asStartElement();				
				OTUorOTUSEventInformation info = getOTUorOTUSEventInformation(streamDataProvider, element);
				
				streamDataProvider.getCurrentEventCollection().add(new LinkedLabeledIDEvent(EventContentType.TREE, info.id,	info.label, info.otuOrOtusID));
					
	  		String branchLengthsFormatWithPrefix = XMLUtils.readStringAttr(element, ATTR_XSI_TYPE, null);
	  		
				if (branchLengthsFormatWithPrefix == null) {
					throw new JPhyloIOReaderException("Tree tag must have an attribute called \"" + ATTR_XSI_TYPE + "\".", element.getLocation());
				}
				else {
					String branchLengthsFormat = branchLengthsFormatWithPrefix.split(":")[branchLengthsFormatWithPrefix.split(":").length - 1];
					if (branchLengthsFormat.equals(TYPE_FLOAT_TREE)) {
						streamDataProvider.setBranchLengthsFormat(TYPE_FLOAT_TREE);
					}
					else if (branchLengthsFormat.equals(TYPE_INT_TREE)) {
						streamDataProvider.setBranchLengthsFormat(TYPE_INT_TREE);
					}
				}
			}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_TREES, TAG_TREE, XMLStreamConstants.END_ELEMENT), new AbstractNeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.TREE));
			}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_TREES, TAG_NETWORK, XMLStreamConstants.START_ELEMENT), new AbstractNeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				StartElement element = event.asStartElement();
				OTUorOTUSEventInformation otuInfo = getOTUorOTUSEventInformation(streamDataProvider, element);
				
	  		streamDataProvider.getCurrentEventCollection().add(new LinkedLabeledIDEvent(EventContentType.NETWORK, otuInfo.id, otuInfo.label, otuInfo.otuOrOtusID));
			}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_TREES, TAG_NETWORK, XMLStreamConstants.END_ELEMENT), new AbstractNeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.NETWORK));
			}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_TREE, TAG_NODE, XMLStreamConstants.START_ELEMENT), readNodeStart);
		putElementReader(new XMLElementReaderKey(TAG_TREE, TAG_NODE, XMLStreamConstants.END_ELEMENT), readNodeEnd);
		putElementReader(new XMLElementReaderKey(TAG_NETWORK, TAG_NODE, XMLStreamConstants.START_ELEMENT), readNodeStart);
		putElementReader(new XMLElementReaderKey(TAG_NETWORK, TAG_NODE, XMLStreamConstants.END_ELEMENT), readNodeEnd);

		putElementReader(new XMLElementReaderKey(TAG_TREE, TAG_EDGE, XMLStreamConstants.START_ELEMENT), readEdgeStart);
		putElementReader(new XMLElementReaderKey(TAG_TREE, TAG_EDGE, XMLStreamConstants.END_ELEMENT), readEdgeEnd);
		putElementReader(new XMLElementReaderKey(TAG_NETWORK, TAG_EDGE, XMLStreamConstants.START_ELEMENT), readEdgeStart);
		putElementReader(new XMLElementReaderKey(TAG_NETWORK, TAG_EDGE, XMLStreamConstants.END_ELEMENT), readEdgeEnd);
		
		putElementReader(new XMLElementReaderKey(null, null, XMLStreamConstants.COMMENT), new CommentElementReader());
	}
	

	@Override
	protected NeXMLReaderStreamDataProvider createStreamDataProvider() {
		return new NeXMLReaderStreamDataProvider(this);
	}
}
