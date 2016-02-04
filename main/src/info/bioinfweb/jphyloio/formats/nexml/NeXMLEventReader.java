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


import info.bioinfweb.commons.bio.CharacterStateMeaning;
import info.bioinfweb.commons.bio.CharacterStateType;
import info.bioinfweb.commons.io.XMLUtils;
import info.bioinfweb.jphyloio.JPhyloIOReaderException;
import info.bioinfweb.jphyloio.events.CharacterSetIntervalEvent;
import info.bioinfweb.jphyloio.events.CommentEvent;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.EdgeEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.LinkedOTUEvent;
import info.bioinfweb.jphyloio.events.MetaInformationEvent;
import info.bioinfweb.jphyloio.events.PartEndEvent;
import info.bioinfweb.jphyloio.events.SequenceTokensEvent;
import info.bioinfweb.jphyloio.events.SingleSequenceTokenEvent;
import info.bioinfweb.jphyloio.events.SingleTokenDefinitionEvent;
import info.bioinfweb.jphyloio.events.TokenSetDefinitionEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.formats.xml.AbstractXMLEventReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Comment;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;



public class NeXMLEventReader extends AbstractXMLEventReader implements NeXMLConstants {
	private static final Map<XMLElementReaderKey, NeXMLElementReader> ELEMENT_READER_MAP = createMap();
	
	private XMLEventReader xmlReader;
	private Stack<QName> encounteredTags = new Stack<QName>();
	private TranslateTokens translateTokens;

	
	public NeXMLEventReader(File file, TranslateTokens translateTokens) throws IOException, XMLStreamException {
		this(new FileReader(file), translateTokens);
	}

	
	public NeXMLEventReader(InputStream stream, TranslateTokens translateTokens) throws IOException, XMLStreamException {
		this(new InputStreamReader(stream), translateTokens);
	}

	
	public NeXMLEventReader(XMLEventReader reader, TranslateTokens translateTokens) {
		super(true);
		this.xmlReader = reader;
		this.translateTokens = translateTokens;
	}

	
	public NeXMLEventReader(Reader reader, TranslateTokens translateTokens) throws IOException, XMLStreamException {
		super(true);
		if (!(reader instanceof BufferedReader)) {
			reader = new BufferedReader(reader);
		}
		this.xmlReader = XMLInputFactory.newInstance().createXMLEventReader(reader);
		this.translateTokens = translateTokens;
	}
	
	
	private static Map<XMLElementReaderKey, NeXMLElementReader> createMap() {
		Map<XMLElementReaderKey, NeXMLElementReader> map = new HashMap<XMLElementReaderKey, NeXMLElementReader>();
		
		NeXMLElementReader readMetaStart = new NeXMLElementReader() {
			@Override
			public void readEvent(NeXMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
	    	StartElement element = event.asStartElement();
	    	String type = XMLUtils.readStringAttr(element, ATTR_TYPE, null);
	  		String key = null;	
	  		String stringValue = null;
	  		Object objectValue = null;
	  		String dataType = null;
	  		
	  		if (type.equals(TYPE_LITERAL_META)) {
	  			key = XMLUtils.readStringAttr(element, ATTR_PROPERTY, null);
	  			stringValue = XMLUtils.readStringAttr(element, ATTR_CONTENT, null);
	  			dataType = XMLUtils.readStringAttr(element, ATTR_DATATYPE, null);
	  			//TODO Delegate java object construction to ontology definition instance, which is able to convert a QName to a Java class.
	  		}
	  		else if (type.equals(TYPE_RESOURCE_META)) {
	  			key = XMLUtils.readStringAttr(element, ATTR_REL, null);
	  			stringValue = XMLUtils.readStringAttr(element, ATTR_HREF, null);
	  			//TODO Create XMLStreamReader instance here, if nested XML is present. (In this case there will be no href attribute.)
	  			try {
	  				objectValue = new URL(stringValue);
	  			} 
	  			catch (MalformedURLException e) {}
	  			dataType = type;
	  		}
	  		else {
	  			throw new JPhyloIOReaderException("Meta annotations can only be of type \"" + TYPE_LITERAL_META + "\" or \"" + 
	  					TYPE_RESOURCE_META + "\".", element.getLocation());
	  		}
	   		
	   		if (stringValue != null && objectValue != null) {
	   			streamDataProvider.getCurrentEventCollection().add(new MetaInformationEvent(key, dataType, stringValue, objectValue));
	   		}
	   		else if (stringValue != null) {
	   			streamDataProvider.getCurrentEventCollection().add(new MetaInformationEvent(key, dataType, stringValue));
	   		}
	   		else {
	   			throw new JPhyloIOReaderException("Meta tag must either have an attribute called \"" + ATTR_CONTENT + "\" or \"" + 
	   					ATTR_HREF + "\".", element.getLocation());
	   		}
	   		
	   		streamDataProvider.getEventReader().readID(element);
			}
		};
		
		NeXMLElementReader readMetaEnd = new NeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
				streamDataProvider.getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(EventContentType.META_INFORMATION, EventTopologyType.END));
			}
		};
		
		NeXMLElementReader readNodeStart = new NeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
				StartElement element = event.asStartElement();				
				OTUEventInformation info = getOTUEventInformation(streamDataProvider, element);
				streamDataProvider.getCurrentEventCollection().add(new LinkedOTUEvent(EventContentType.NODE, info.id,	info.label, info.otuID));
			}
		};
		
		NeXMLElementReader readNodeEnd = new NeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
				streamDataProvider.getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(EventContentType.NODE, EventTopologyType.END));
			}
		};
		
		NeXMLElementReader readEdgeStart = new NeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
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
		
		NeXMLElementReader readEdgeEnd = new NeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
				streamDataProvider.getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(EventContentType.EDGE, EventTopologyType.END));
			}
		};
		
		NeXMLElementReader readStateStart = new NeXMLElementReader() {		
			@Override
			public void readEvent(NeXMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
				StartElement element = event.asStartElement();
				String id = XMLUtils.readStringAttr(element, ATTR_ID, null);
				String label = XMLUtils.readStringAttr(element, ATTR_LABEL, null);
				String symbol = XMLUtils.readStringAttr(element, ATTR_SYMBOL, null);
				String translation = symbol;
				
				if (!streamDataProvider.getEventReader().getTranslateTokens().equals(TranslateTokens.NEVER)) {
	   			if (streamDataProvider.getEventReader().getTranslateTokens().equals(TranslateTokens.SYMBOL_TO_LABEL) 
	   					&& (label != null)) {
	   				translation = label;
	   			}
	   			else { //SYMBOL_TO_ID or label was null
	   				translation = id;
	   			}
	   		}
				
	  		if (symbol != null) {
	  			streamDataProvider.setSymbol(symbol);
	  			streamDataProvider.getTokenSets().get(streamDataProvider.getTokenSetID()).getSymbolTranslationMap().put(symbol, translation);
	  			streamDataProvider.getTokenDefinitionIDToSymbolMap().put(id, symbol);
	  		}
	  		else {
	  			throw new JPhyloIOReaderException("State tag must have an attribute called \"" + ATTR_SYMBOL + "\".", element.getLocation());
	  		}
	  		
	  		List<String> currentConstitituents = new ArrayList<String>();		
				streamDataProvider.setConstituents(currentConstitituents);
			}
		};
		
		NeXMLElementReader readStateEnd = new NeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
				String symbol = streamDataProvider.getSymbol();
				CharacterStateMeaning meaning;
				
				if (symbol.equals("?")) {
					meaning = CharacterStateMeaning.MISSING;
				}
				else if (symbol.equals("-")) {
					meaning = CharacterStateMeaning.GAP;
				}
				else {
					meaning = CharacterStateMeaning.CHARACTER_STATE;
				}
				
				//TODO reset current event collection
				streamDataProvider.getCurrentEventCollection().add(new SingleTokenDefinitionEvent(symbol, meaning, streamDataProvider.getConstituents()));
  			//TODO Add meta and comment events from current event collection
				streamDataProvider.getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(EventContentType.SINGLE_TOKEN_DEFINITION, EventTopologyType.END)); //TODO probably give end event some place else, so meta events can be given in between
			}
		};
		
		NeXMLElementReader readMemberStart = new NeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
				StartElement element = event.asStartElement();				
				String state = XMLUtils.readStringAttr(element, ATTR_STATE, null);
				streamDataProvider.getConstituents().add(streamDataProvider.getTokenDefinitionIDToSymbolMap().get(state));
			}
		};
		
		map.put(new XMLElementReaderKey(TAG_NEXML, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart);
		
		map.put(new XMLElementReaderKey(TAG_NEXML, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		
		map.put(new XMLElementReaderKey(TAG_META, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart);
		
		map.put(new XMLElementReaderKey(TAG_META, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		
		map.put(new XMLElementReaderKey(TAG_CHARACTERS, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart);
		
		map.put(new XMLElementReaderKey(TAG_CHARACTERS, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		
		map.put(new XMLElementReaderKey(TAG_OTUS, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart);
		
		map.put(new XMLElementReaderKey(TAG_OTUS, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		
		map.put(new XMLElementReaderKey(TAG_OTU, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart);
		
		map.put(new XMLElementReaderKey(TAG_OTU, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		
		map.put(new XMLElementReaderKey(TAG_FORMAT, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart);
		
		map.put(new XMLElementReaderKey(TAG_FORMAT, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		
		map.put(new XMLElementReaderKey(TAG_STATES, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart);
		
		map.put(new XMLElementReaderKey(TAG_STATES, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		
		map.put(new XMLElementReaderKey(TAG_STATE, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart);
		
		map.put(new XMLElementReaderKey(TAG_STATE, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		
		map.put(new XMLElementReaderKey(TAG_UNCERTAIN, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart);
		
		map.put(new XMLElementReaderKey(TAG_UNCERTAIN, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		
		map.put(new XMLElementReaderKey(TAG_POLYMORPHIC, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart);
		
		map.put(new XMLElementReaderKey(TAG_POLYMORPHIC, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		
		map.put(new XMLElementReaderKey(TAG_CHAR, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart);
		
		map.put(new XMLElementReaderKey(TAG_CHAR, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		
		map.put(new XMLElementReaderKey(TAG_MATRIX, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart);
		
		map.put(new XMLElementReaderKey(TAG_MATRIX, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		
		map.put(new XMLElementReaderKey(TAG_ROW, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart);
		
		map.put(new XMLElementReaderKey(TAG_ROW, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		
		map.put(new XMLElementReaderKey(TAG_TREE, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart);
		
		map.put(new XMLElementReaderKey(TAG_TREE, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		
		map.put(new XMLElementReaderKey(TAG_NETWORK, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart);
		
		map.put(new XMLElementReaderKey(TAG_NETWORK, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		
		map.put(new XMLElementReaderKey(TAG_NODE, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart);
		
		map.put(new XMLElementReaderKey(TAG_NODE, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		
		map.put(new XMLElementReaderKey(TAG_EDGE, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart);
		
		map.put(new XMLElementReaderKey(TAG_EDGE, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		
		map.put(new XMLElementReaderKey(null, null, XMLStreamConstants.START_DOCUMENT), new NeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
				streamDataProvider.getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(EventContentType.DOCUMENT, EventTopologyType.START));
			}
		});
		
		map.put(new XMLElementReaderKey(null, null, XMLStreamConstants.END_DOCUMENT), new NeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
				streamDataProvider.getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(EventContentType.DOCUMENT, EventTopologyType.END));
			}
		});
		
		map.put(new XMLElementReaderKey(TAG_NEXML, TAG_OTUS, XMLStreamConstants.START_ELEMENT), new NeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
				StartElement element = event.asStartElement();
				OTUEventInformation info = getOTUEventInformation(streamDataProvider, element);
				streamDataProvider.getCurrentEventCollection().add(new LabeledIDEvent(EventContentType.OTU_LIST, info.id,	info.label));
			}
		});
		
		map.put(new XMLElementReaderKey(TAG_NEXML, TAG_OTUS, XMLStreamConstants.END_ELEMENT), new NeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
				streamDataProvider.getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(EventContentType.OTU_LIST, EventTopologyType.END));
			}
		});
		
		map.put(new XMLElementReaderKey(TAG_OTUS, TAG_OTU, XMLStreamConstants.START_ELEMENT), new NeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
				StartElement element = event.asStartElement();
				String id = XMLUtils.readStringAttr(element, ATTR_ID, null);
				String label = XMLUtils.readStringAttr(element, ATTR_LABEL, null);			
				
				streamDataProvider.getOtuIDToLabelMap().put(id, label);
				streamDataProvider.getCurrentEventCollection().add(new LabeledIDEvent(EventContentType.OTU, id, label));
			}
		});
		
		map.put(new XMLElementReaderKey(TAG_OTUS, TAG_OTU, XMLStreamConstants.END_ELEMENT), new NeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
				streamDataProvider.getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(EventContentType.OTU, EventTopologyType.END));
			}
		});
		
		map.put(new XMLElementReaderKey(TAG_NEXML, TAG_CHARACTERS, XMLStreamConstants.START_ELEMENT), new NeXMLElementReader() {		
			@Override
			public void readEvent(NeXMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
				StartElement element = event.asStartElement();
				String id = XMLUtils.readStringAttr(element, ATTR_ID, null);
				String label = XMLUtils.readStringAttr(element, ATTR_LABEL, null);
				
				String tokenSetType = XMLUtils.readStringAttr(element, ATTR_TYPE, null);			
				if (tokenSetType == null) {
					throw new JPhyloIOReaderException("Character tag must have an attribute called \"" + ATTR_TYPE + "\".", element.getLocation());
				}
				else {
					CharacterStateType setType = null;				
					if (tokenSetType.equals(TYPE_DNA_SEQS) || tokenSetType.equals(TYPE_DNA_CELLS)) {
						label = "DNA";
						setType = CharacterStateType.DNA; //standard IUPAC nucleotide symbols
						streamDataProvider.setAllowLongTokens(false);
					}
					else if (tokenSetType.equals(TYPE_RNA_SEQS) || tokenSetType.equals(TYPE_RNA_CELLS)) {
						label = "RNA";
						setType = CharacterStateType.RNA;  //standard IUPAC nucleotide symbols
						streamDataProvider.setAllowLongTokens(false);
					}
					else if (tokenSetType.equals(TYPE_PROTEIN_SEQS) || tokenSetType.equals(TYPE_PROTEIN_CELLS)) {
						label = "AminoAcid";
						setType = CharacterStateType.AMINO_ACID;  //standard IUPAC amino acid symbols
						streamDataProvider.setAllowLongTokens(false);
					}
					else if (tokenSetType.equals(TYPE_CONTIN_SEQ) || tokenSetType.equals(TYPE_CONTIN_CELLS)) {
						label = "ContinuousData";
						setType = CharacterStateType.CONTINUOUS; 
						streamDataProvider.setAllowLongTokens(true);
					}
					else if (tokenSetType.equals(TYPE_RESTRICTION_SEQS) || tokenSetType.equals(TYPE_RESTRICTION_CELLS)) {
						label = "RestrictionSiteData";
						setType = CharacterStateType.DISCRETE; 
						streamDataProvider.setAllowLongTokens(false);
					}
					else { // type of character block is StandardSeqs or StandardCells
						label = "StandardData";
						setType = CharacterStateType.DISCRETE;
						streamDataProvider.setAllowLongTokens(true);
					}
					streamDataProvider.setCharacterSetType(setType);
				}
				
				streamDataProvider.getCurrentEventCollection().add(new LabeledIDEvent(EventContentType.ALIGNMENT, id,	label));				
			}
		});
		
		map.put(new XMLElementReaderKey(TAG_NEXML, TAG_CHARACTERS, XMLStreamConstants.END_ELEMENT), new NeXMLElementReader() {		
			@Override
			public void readEvent(NeXMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
				streamDataProvider.getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(EventContentType.ALIGNMENT, EventTopologyType.END));
				streamDataProvider.getTokenSets().clear();
			}
		});		
		
		map.put(new XMLElementReaderKey(TAG_CHARACTERS, TAG_FORMAT, XMLStreamConstants.START_ELEMENT), new NeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
				List<String> charIDs = new ArrayList<String>();
				Map<String, String> charIDToStatesMap = new HashMap<String, String>();
				Set<String> directCharSetIDs = new TreeSet<String>();
				
				streamDataProvider.setCharIDs(charIDs);
				streamDataProvider.setCharIDToStatesMap(charIDToStatesMap);
				streamDataProvider.setDirectCharSetIDs(directCharSetIDs);				
			}
		});
		
		map.put(new XMLElementReaderKey(TAG_CHARACTERS, TAG_FORMAT, XMLStreamConstants.END_ELEMENT), new NeXMLElementReader() {		
			@Override
			public void readEvent(NeXMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {				
				//Indirect char sets
				if (!streamDataProvider.getTokenSets().isEmpty()) { //there are no token sets for continuous data and therefore no indirect char sets
					if (streamDataProvider.getTokenSets().size() == 1) {
						streamDataProvider.getTokenSets().get(streamDataProvider.getTokenSetID()).getCharSetIntervals()
								.add(new CharacterSetIntervalEvent(0, streamDataProvider.getCharIDs().size()));
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
					CharacterStateType type = info.getSetType();
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
		
		map.put(new XMLElementReaderKey(TAG_FORMAT, TAG_SET, XMLStreamConstants.START_ELEMENT), new NeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
				StartElement element = event.asStartElement();
				String id = XMLUtils.readStringAttr(element, ATTR_ID, null);
				String label = XMLUtils.readStringAttr(element, ATTR_LABEL, null);
				String charIDs = XMLUtils.readStringAttr(element, ATTR_CHAR, null);
				System.out.println("Direct: " + id);
				String[] charIDArray = charIDs.split(" "); //IDs are not allowed to contain spaces
				streamDataProvider.getDirectCharSetIDs().add(id);
						
				streamDataProvider.getCurrentEventCollection().add(new LabeledIDEvent(EventContentType.CHARACTER_SET, id, label));
				
				List<Integer> indexList = new ArrayList<Integer>();
				for (String charID: charIDArray) {
					indexList.add(streamDataProvider.getCharIDs().indexOf(charID));
				}
				Collections.sort(indexList); //TODO eventuell hier boolean Feld verwenden statt Liste zu sortieren
				
				if ((indexList.get(indexList.size() - 1) - indexList.get(0)) == (indexList.size() - 1)) {
					streamDataProvider.getCurrentEventCollection().add(new CharacterSetIntervalEvent(indexList.get(0), indexList.get(indexList.size() - 1)));
				}
				else {
					int currentIndex = -1;
					int previousIndex = -1;
					int startIndex = indexList.get(0);
					
					for (int index: indexList) {						
						previousIndex = currentIndex;
						currentIndex = index;
						if (((previousIndex != -1) && (currentIndex != (previousIndex + 1)))) {						
							streamDataProvider.getCurrentEventCollection().add(new CharacterSetIntervalEvent(startIndex, previousIndex));
							startIndex = currentIndex;
						}
					}
					streamDataProvider.getCurrentEventCollection().add(new CharacterSetIntervalEvent(startIndex, currentIndex));
				}				
			}				
		});
		
		map.put(new XMLElementReaderKey(TAG_FORMAT, TAG_SET, XMLStreamConstants.END_ELEMENT), new NeXMLElementReader() {			
			@Override
			protected void readEvent(NeXMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
				streamDataProvider.getCurrentEventCollection().add(new PartEndEvent(EventContentType.CHARACTER_SET, true));				
			}
		});
		
		map.put(new XMLElementReaderKey(TAG_FORMAT, TAG_STATES, XMLStreamConstants.START_ELEMENT), new NeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
				StartElement element = event.asStartElement();
				String id = XMLUtils.readStringAttr(element, ATTR_ID, null);
				String label = XMLUtils.readStringAttr(element, ATTR_LABEL, streamDataProvider.getCharacterSetType().toString());
				
				List<JPhyloIOEvent> singleTokens = new ArrayList<JPhyloIOEvent>();
				List<JPhyloIOEvent> charSetIntervals = new ArrayList<JPhyloIOEvent>();
				Map<String, String> symbolToLabelMap = new HashMap<String, String>();
				Map<String, String> idToSymbolMap = new HashMap<String, String>();
				
				streamDataProvider.setTokenSetID(id);				
				streamDataProvider.getTokenSets().put(id, new NeXMLTokenSetInformation(id, label, streamDataProvider.getCharacterSetType()));
				
				streamDataProvider.getTokenSets().get(id).setSingleTokens(singleTokens);
				streamDataProvider.getTokenSets().get(id).setCharSetIntervals(charSetIntervals);
				streamDataProvider.getTokenSets().get(id).setSymbolTranslationMap(symbolToLabelMap);
				streamDataProvider.setTokenDefinitionIDToSymbolMap(idToSymbolMap);
				
				streamDataProvider.setCurrentEventCollection(streamDataProvider.getTokenSets().get(id).getSingleTokens());
			}
		});
		
		map.put(new XMLElementReaderKey(TAG_FORMAT, TAG_STATES, XMLStreamConstants.END_ELEMENT), new NeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
				streamDataProvider.getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(EventContentType.TOKEN_SET_DEFINITION, EventTopologyType.END));
				streamDataProvider.resetCurrentEventCollection();
			}
		});
		
		map.put(new XMLElementReaderKey(TAG_STATES, TAG_STATE, XMLStreamConstants.START_ELEMENT), readStateStart);
		
		map.put(new XMLElementReaderKey(TAG_STATES, TAG_STATE, XMLStreamConstants.END_ELEMENT), readStateEnd);
		
		map.put(new XMLElementReaderKey(TAG_STATES, TAG_UNCERTAIN, XMLStreamConstants.START_ELEMENT), readStateStart);
		
		map.put(new XMLElementReaderKey(TAG_STATES, TAG_UNCERTAIN, XMLStreamConstants.END_ELEMENT), readStateEnd);
		
		map.put(new XMLElementReaderKey(TAG_STATES, TAG_POLYMORPHIC, XMLStreamConstants.START_ELEMENT), readStateStart);
		
		map.put(new XMLElementReaderKey(TAG_STATES, TAG_POLYMORPHIC, XMLStreamConstants.END_ELEMENT), readStateEnd);
		
		map.put(new XMLElementReaderKey(TAG_UNCERTAIN, TAG_MEMBER, XMLStreamConstants.START_ELEMENT), readMemberStart);
		
		map.put(new XMLElementReaderKey(TAG_POLYMORPHIC, TAG_MEMBER, XMLStreamConstants.START_ELEMENT), readMemberStart);
		
		map.put(new XMLElementReaderKey(TAG_FORMAT, TAG_CHAR, XMLStreamConstants.START_ELEMENT), new NeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
				StartElement element = event.asStartElement();
				String id =	XMLUtils.readStringAttr(element, ATTR_ID, null);
				String states =	XMLUtils.readStringAttr(element, ATTR_STATES, null);
				
				streamDataProvider.getCharIDs().add(id);
				streamDataProvider.getCharIDToStatesMap().put(id, states);
			}
		});
		
		map.put(new XMLElementReaderKey(TAG_MATRIX, TAG_ROW, XMLStreamConstants.START_ELEMENT), new NeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
				StartElement element = event.asStartElement();
				OTUEventInformation otuInfo = getOTUEventInformation(streamDataProvider, element);
	  		streamDataProvider.getCurrentEventCollection().add(new LinkedOTUEvent(EventContentType.SEQUENCE, otuInfo.id, otuInfo.label, otuInfo.otuID));
			}
		});
		
		map.put(new XMLElementReaderKey(TAG_MATRIX, TAG_ROW, XMLStreamConstants.END_ELEMENT), new NeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
				streamDataProvider.getCurrentEventCollection().add(new PartEndEvent(EventContentType.SEQUENCE, true));
			}
		});
		
		map.put(new XMLElementReaderKey(TAG_ROW, TAG_CELL, XMLStreamConstants.START_ELEMENT), new NeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
				StartElement element = event.asStartElement();				
				String token = XMLUtils.readStringAttr(element, ATTR_STATE, null);
				String character = XMLUtils.readStringAttr(element, ATTR_CHAR, null);
				
				if (streamDataProvider.getCharacterSetType().equals(CharacterStateType.DISCRETE) && !streamDataProvider.getEventReader().getTranslateTokens().equals(TranslateTokens.NEVER)) {		 			
					String currentStates = streamDataProvider.getCharIDToStatesMap().get(character);
		 	 		String translatedToken = streamDataProvider.getTokenSets().get(currentStates).getSymbolTranslationMap().get(token);
		 	 		if (translatedToken != null) {
		 	 			token = translatedToken;
		 	 		}
		 		}
				
				streamDataProvider.getCurrentEventCollection().add(new SingleSequenceTokenEvent(token));
			}
		});
		
		map.put(new XMLElementReaderKey(TAG_ROW, TAG_CELL, XMLStreamConstants.END_ELEMENT), new NeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
				streamDataProvider.getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(EventContentType.SEQUENCE_TOKENS, EventTopologyType.END));
			}
		});
		
		map.put(new XMLElementReaderKey(TAG_SEQ, null, XMLStreamConstants.CHARACTERS), new NeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
				String tokens = event.asCharacters().getData(); // TODO Influence read length of character events		
		   	streamDataProvider.getCurrentEventCollection().add(new SequenceTokensEvent(readSequence(streamDataProvider, tokens, streamDataProvider.getEventReader().getTranslateTokens())));				
			}
		});
		
		map.put(new XMLElementReaderKey(TAG_TREES, TAG_TREE, XMLStreamConstants.START_ELEMENT), new NeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
				StartElement element = event.asStartElement();
				
				OTUEventInformation info = getOTUEventInformation(streamDataProvider, element);
				streamDataProvider.getCurrentEventCollection().add(new LabeledIDEvent(EventContentType.TREE, info.id,	info.label));
				
	  		String branchLengthsFormat = XMLUtils.readStringAttr(element, ATTR_TYPE, null);
				if (branchLengthsFormat.equals(null)) {}
				else if (branchLengthsFormat.equals(TYPE_FLOAT_TREE)) {
					streamDataProvider.setBranchLengthsFormat(TYPE_FLOAT_TREE);
				}
				else if (branchLengthsFormat.equals(TYPE_INT_TREE)) {
					streamDataProvider.setBranchLengthsFormat(TYPE_INT_TREE);
				}
			}
		});
		
		map.put(new XMLElementReaderKey(TAG_TREES, TAG_TREE, XMLStreamConstants.END_ELEMENT), new NeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
				streamDataProvider.getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(EventContentType.TREE, EventTopologyType.END));
			}
		});
		
		map.put(new XMLElementReaderKey(TAG_TREES, TAG_NETWORK, XMLStreamConstants.START_ELEMENT), new NeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
				StartElement element = event.asStartElement();
				OTUEventInformation otuInfo = getOTUEventInformation(streamDataProvider, element);
	  		streamDataProvider.getCurrentEventCollection().add(new LabeledIDEvent(EventContentType.NETWORK, otuInfo.id, otuInfo.label));
			}
		});
		
		map.put(new XMLElementReaderKey(TAG_TREES, TAG_NETWORK, XMLStreamConstants.END_ELEMENT), new NeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
				streamDataProvider.getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(EventContentType.NETWORK, EventTopologyType.END));
			}
		});
		
		map.put(new XMLElementReaderKey(TAG_TREE, TAG_NODE, XMLStreamConstants.START_ELEMENT), readNodeStart);
		
		map.put(new XMLElementReaderKey(TAG_TREE, TAG_NODE, XMLStreamConstants.END_ELEMENT), readNodeEnd);
		
		map.put(new XMLElementReaderKey(TAG_NETWORK, TAG_NODE, XMLStreamConstants.START_ELEMENT), readNodeStart);
		
		map.put(new XMLElementReaderKey(TAG_NETWORK, TAG_NODE, XMLStreamConstants.END_ELEMENT), readNodeEnd);
		
		map.put(new XMLElementReaderKey(TAG_TREE, TAG_EDGE, XMLStreamConstants.START_ELEMENT), readEdgeStart);
		
		map.put(new XMLElementReaderKey(TAG_TREE, TAG_EDGE, XMLStreamConstants.END_ELEMENT), readEdgeEnd);
		
		map.put(new XMLElementReaderKey(TAG_NETWORK, TAG_EDGE, XMLStreamConstants.START_ELEMENT), readEdgeStart);
		
		map.put(new XMLElementReaderKey(TAG_NETWORK, TAG_EDGE, XMLStreamConstants.END_ELEMENT), readEdgeEnd);
		
		map.put(new XMLElementReaderKey(null, null, XMLStreamConstants.COMMENT), new NeXMLElementReader() {			
			@Override
			protected void readEvent(NeXMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
				String comment = ((Comment)event).getText();
				streamDataProvider.getCurrentEventCollection().add(new CommentEvent(comment, false));
			}
		});
		
		return map; 
	}
	

	@Override
	protected NeXMLStreamDataProvider createStreamDataProvider() {
		return new NeXMLStreamDataProvider(this);
	}


	@Override
	protected NeXMLStreamDataProvider getStreamDataProvider() {
		return (NeXMLStreamDataProvider)super.getStreamDataProvider();
	}


	protected XMLEventReader getXMLReader() {
		return xmlReader;
	}

	protected Stack<QName> getEncounteredTags() {
		return encounteredTags;
	}


	public TranslateTokens getTranslateTokens() {
		return translateTokens;
	}


	@Override
	public int getMaxCommentLength() {
		return 0;
	}


	@Override
	public void setMaxCommentLength(int maxCommentLength) {}
	
	
	protected Queue<JPhyloIOEvent> getUpcomingEvents() {
		return super.getUpcomingEvents();
	}


	@Override
	protected void readNextEvent() throws Exception {
		while (xmlReader.hasNext() && getUpcomingEvents().isEmpty()) {
			XMLEvent xmlEvent = getXMLReader().nextEvent();
			QName parentTag = null;
			
			QName elementTag = null;
			switch (xmlEvent.getEventType()) {
				case XMLStreamConstants.START_DOCUMENT:
					elementTag = null;
					break;
				case XMLStreamConstants.END_DOCUMENT:
					elementTag = null;
					break;
				case XMLStreamConstants.START_ELEMENT:
					elementTag = xmlEvent.asStartElement().getName();
					break;
				case XMLStreamConstants.END_ELEMENT:
					encounteredTags.pop();
					elementTag = xmlEvent.asEndElement().getName();
					break;
				default: 
					break;  // Nothing to do.
			}

			if (!encounteredTags.isEmpty()) {
				parentTag = encounteredTags.peek();
			}
			else {
				parentTag = TAG_ROOT;
			}		
			
			if (xmlEvent.isStartElement()) {
				encounteredTags.push(xmlEvent.asStartElement().getName());
			}

			NeXMLElementReader elementReader = getElementReader(parentTag, elementTag, xmlEvent.getEventType());
			if (elementReader != null) {
				elementReader.readEvent(getStreamDataProvider(), xmlEvent);
			}			
		}
	}
	
	
	protected NeXMLElementReader getElementReader(QName parentTag, QName elementTag, int eventType) {
		NeXMLElementReader result = ELEMENT_READER_MAP.get(new XMLElementReaderKey(parentTag, elementTag, eventType));
		if (result == null) {
			result = ELEMENT_READER_MAP.get(new XMLElementReaderKey(null, elementTag, eventType));
			if (result == null) {
				result = ELEMENT_READER_MAP.get(new XMLElementReaderKey(parentTag, null, eventType));
				if (result == null) {
					result = ELEMENT_READER_MAP.get(new XMLElementReaderKey(null, null, eventType));
				}
			}
		}
		return result;
	}
	

	protected void readID(StartElement element) {
		String id = XMLUtils.readStringAttr(element, ATTR_ID, null);
		if (id != null) {
			getCurrentEventCollection().add(new MetaInformationEvent("id", "string", id));
			getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(EventContentType.META_INFORMATION, EventTopologyType.END));
		}
	}
	
	
	@Override
	public void close() throws Exception {
		super.close();
		xmlReader.close();
	}	
}
