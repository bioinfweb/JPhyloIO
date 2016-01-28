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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import info.bioinfweb.commons.bio.CharacterStateMeaning;
import info.bioinfweb.commons.bio.CharacterStateType;
import info.bioinfweb.commons.io.XMLUtils;
import info.bioinfweb.jphyloio.AbstractEventReader;
import info.bioinfweb.jphyloio.JPhyloIOReaderException;
import info.bioinfweb.jphyloio.events.CharacterSetIntervalEvent;
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



public class NeXMLEventReader extends AbstractEventReader implements NeXMLConstants {
	private static final Map<XMLElementType, NeXMLElementReader> ELEMENT_READER_MAP = createMap();
	
	private NeXMLStreamDataProvider streamDataProvider;
	private XMLEventReader xmlReader;
	private Stack<QName> encounteredTags = new Stack<QName>();

	
	private static Map<XMLElementType, NeXMLElementReader> createMap() {
		Map<XMLElementType, NeXMLElementReader> map = new HashMap<XMLElementType, NeXMLElementReader>();
		
		NeXMLElementReader readMetaStart = new NeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLEventReader reader, XMLEvent event) throws Exception {
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
	  			try {
	  				objectValue = new URL(stringValue);
	  			} 
	  			catch (MalformedURLException e) {}
	  			dataType = type;
	  		}
	  		else {} //TODO Possibly throw exception or write to warning log, if invalid types are encountered
	   		
	   		if (stringValue != null && objectValue != null) {
	   			reader.getUpcomingEvents().add(new MetaInformationEvent(key, dataType, stringValue, objectValue));
	   		}
	   		else if (stringValue != null) {
	   			reader.getUpcomingEvents().add(new MetaInformationEvent(key, dataType, stringValue));
	   		}
	   		//TODO Possibly throw exception or write to warning log, if necessary NeXML attributes are missing. 		
	   		reader.readID(reader, element);
			}
		};
		
		NeXMLElementReader readMetaEnd = new NeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLEventReader reader, XMLEvent event) throws Exception {
				reader.getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.META_INFORMATION, EventTopologyType.END));
			}
		};
		
		NeXMLElementReader readNodeStart = new NeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLEventReader reader, XMLEvent event) throws Exception {
				StartElement element = event.asStartElement();				
				OTUEventInformation info = getOTUEventInformation(reader, element);
				reader.getUpcomingEvents().add(new LinkedOTUEvent(EventContentType.NODE, info.id,	info.label, info.otuID));
			}
		};
		
		NeXMLElementReader readNodeEnd = new NeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLEventReader reader, XMLEvent event) throws Exception {
				reader.getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.NODE, EventTopologyType.END));
			}
		};
		
		NeXMLElementReader readEdgeStart = new NeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLEventReader reader, XMLEvent event) throws Exception {
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
						reader.getUpcomingEvents().add(new EdgeEvent(edgeID, XMLUtils.readStringAttr(element, ATTR_LABEL, null), 
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
			public void readEvent(NeXMLEventReader reader, XMLEvent event) throws Exception {
				reader.getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.EDGE, EventTopologyType.END));
			}
		};
		
		NeXMLElementReader readStateStart = new NeXMLElementReader() {		
			@Override
			public void readEvent(NeXMLEventReader reader, XMLEvent event) throws Exception {
				StartElement element = event.asStartElement();
				String id = XMLUtils.readStringAttr(element, ATTR_ID, null);
				String label = XMLUtils.readStringAttr(element, ATTR_LABEL, null);
				String symbol = XMLUtils.readStringAttr(element, ATTR_SYMBOL, null);
				
				reader.getStreamDataProvider().setCurrentSingleTokenDefinitionID(id);
				
	  		if (symbol != null) { //symbol should never be null in a valid NeXML file
	  			reader.getStreamDataProvider().getTokenDefinitionIDToSymbolMap().put(id, symbol);
	  		}
	  		
	  		reader.getStreamDataProvider().getTokenSets().get(reader.getStreamDataProvider().getCurrentTokenSetID()).getSingleTokens()
	  				.add(new MetaInformationEvent("id", "string", id));
	  		reader.getStreamDataProvider().getTokenSets().get(reader.getStreamDataProvider().getCurrentTokenSetID()).getSingleTokens()
	  				.add(new ConcreteJPhyloIOEvent(EventContentType.META_INFORMATION, EventTopologyType.END));
	  		reader.getStreamDataProvider().getTokenSets().get(reader.getStreamDataProvider().getCurrentTokenSetID()).getSingleTokens()
	  				.add(new MetaInformationEvent("label", "string", label));
	  		reader.getStreamDataProvider().getTokenSets().get(reader.getStreamDataProvider().getCurrentTokenSetID()).getSingleTokens()
	  				.add(new ConcreteJPhyloIOEvent(EventContentType.META_INFORMATION, EventTopologyType.END));
	  		
	  		List<String> currentConstitituents = new ArrayList<String>();		
				reader.getStreamDataProvider().setConstituents(currentConstitituents);
			}
		};
		
		NeXMLElementReader readStateEnd = new NeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLEventReader reader, XMLEvent event) throws Exception {
				String symbol = reader.getStreamDataProvider().getTokenDefinitionIDToSymbolMap().get(reader.getStreamDataProvider().getCurrentSingleTokenDefinitionID());
				CharacterStateMeaning meaning;
				
				if (symbol.equals("?")) {
					meaning = CharacterStateMeaning.MISSING;
				}
				else if (symbol.equals("-")) {
					meaning = CharacterStateMeaning.GAP;
				}
				else {
					meaning = CharacterStateMeaning.CHARACTER_STATE; //TODO handle case restriction data
				}
				
				reader.getStreamDataProvider().getTokenSets().get(reader.getStreamDataProvider().getCurrentTokenSetID()).getSingleTokens()
						.add(new SingleTokenDefinitionEvent(symbol, meaning, reader.getStreamDataProvider().getConstituents()));				
				reader.getStreamDataProvider().getTokenSets().get(reader.getStreamDataProvider().getCurrentTokenSetID()).getSingleTokens()
						.add(new ConcreteJPhyloIOEvent(EventContentType.SINGLE_TOKEN_DEFINITION, EventTopologyType.END));
			}
		};
		
		NeXMLElementReader readMemberStart = new NeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLEventReader reader, XMLEvent event) throws Exception {
				StartElement element = event.asStartElement();				
				String state = XMLUtils.readStringAttr(element, ATTR_STATE, null);
				reader.getStreamDataProvider().getConstituents().add(reader.getStreamDataProvider().getTokenDefinitionIDToSymbolMap().get(state));
			}
		};
		
		map.put(new XMLElementType(TAG_NEXML, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart);
		
		map.put(new XMLElementType(TAG_NEXML, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		
		map.put(new XMLElementType(TAG_META, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart);
		
		map.put(new XMLElementType(TAG_META, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		
		map.put(new XMLElementType(TAG_CHARACTERS, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart);
		
		map.put(new XMLElementType(TAG_CHARACTERS, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		
		map.put(new XMLElementType(TAG_OTUS, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart);
		
		map.put(new XMLElementType(TAG_OTUS, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		
		map.put(new XMLElementType(TAG_OTU, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart);
		
		map.put(new XMLElementType(TAG_OTU, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		
		map.put(new XMLElementType(TAG_MATRIX, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart);
		
		map.put(new XMLElementType(TAG_MATRIX, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		
		map.put(new XMLElementType(TAG_ROW, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart);
		
		map.put(new XMLElementType(TAG_ROW, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		
		map.put(new XMLElementType(TAG_TREE, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart);
		
		map.put(new XMLElementType(TAG_TREE, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		
		map.put(new XMLElementType(TAG_NETWORK, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart);
		
		map.put(new XMLElementType(TAG_NETWORK, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		
		map.put(new XMLElementType(TAG_NODE, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart);
		
		map.put(new XMLElementType(TAG_NODE, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		
		map.put(new XMLElementType(TAG_EDGE, TAG_META, XMLStreamConstants.START_ELEMENT), readMetaStart);
		
		map.put(new XMLElementType(TAG_EDGE, TAG_META, XMLStreamConstants.END_ELEMENT), readMetaEnd);
		
		map.put(new XMLElementType(TAG_NEXML, TAG_OTUS, XMLStreamConstants.START_ELEMENT), new NeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLEventReader reader, XMLEvent event) throws Exception {
				StartElement element = event.asStartElement();
				OTUEventInformation info = getOTUEventInformation(reader, element);
				reader.getUpcomingEvents().add(new LabeledIDEvent(EventContentType.OTU_LIST, info.id,	info.label));
			}
		});
		
		map.put(new XMLElementType(TAG_NEXML, TAG_OTUS, XMLStreamConstants.END_ELEMENT), new NeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLEventReader reader, XMLEvent event) throws Exception {
				reader.getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.OTU_LIST, EventTopologyType.END));
			}
		});
		
		map.put(new XMLElementType(TAG_OTUS, TAG_OTU, XMLStreamConstants.START_ELEMENT), new NeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLEventReader reader, XMLEvent event) throws Exception {
				StartElement element = event.asStartElement();
				String id = XMLUtils.readStringAttr(element, ATTR_ID, null);
				String label = XMLUtils.readStringAttr(element, ATTR_LABEL, null);			
				
				reader.getStreamDataProvider().getOtuIDToLabelMap().put(id, label);
				reader.getUpcomingEvents().add(new LabeledIDEvent(EventContentType.OTU, id, label));
			}
		});
		
		map.put(new XMLElementType(TAG_OTUS, TAG_OTU, XMLStreamConstants.END_ELEMENT), new NeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLEventReader reader, XMLEvent event) throws Exception {
				reader.getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.OTU, EventTopologyType.END));
			}
		});
		
		map.put(new XMLElementType(TAG_NEXML, TAG_CHARACTERS, XMLStreamConstants.START_ELEMENT), new NeXMLElementReader() {		
			@Override
			public void readEvent(NeXMLEventReader reader, XMLEvent event) throws Exception {
				StartElement element = event.asStartElement();
				String id = XMLUtils.readStringAttr(element, ATTR_ID, null);
				String label = XMLUtils.readStringAttr(element, ATTR_LABEL, null);
				
				reader.getStreamDataProvider().setCurrentTokenSetType(XMLUtils.readStringAttr(element, ATTR_TYPE, ""));
				reader.getUpcomingEvents().add(new LabeledIDEvent(EventContentType.ALIGNMENT, id,	label));				
			}
		});
		
		map.put(new XMLElementType(TAG_NEXML, TAG_CHARACTERS, XMLStreamConstants.END_ELEMENT), new NeXMLElementReader() {		
			@Override
			public void readEvent(NeXMLEventReader reader, XMLEvent event) throws Exception {
				reader.getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.ALIGNMENT, EventTopologyType.END));
			}
		});		
		
		map.put(new XMLElementType(TAG_CHARACTERS, TAG_FORMAT, XMLStreamConstants.START_ELEMENT), new NeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLEventReader reader, XMLEvent event) throws Exception {
				//TODO generate character sets, if there is only one token set, there can be only one character set
			}
		});
		
		map.put(new XMLElementType(TAG_CHARACTERS, TAG_FORMAT, XMLStreamConstants.END_ELEMENT), new NeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLEventReader reader, XMLEvent event) throws Exception {
				NeXMLTokenSetInformation info = reader.getStreamDataProvider().getTokenSets().get(reader.getStreamDataProvider().getCurrentTokenSetID());
				CharacterStateType type = info.getSetType();
				String id = info.getId();
				String label = info.getLabel();
				String charSetID = info.getCharacterSetID();
				
				//direct char sets
				
				
				//indirect char sets
//			reader.getUpcomingEvents().add(new LabeledIDEvent(EventContentType.CHARACTER_SET, "ID", "label")); //TODO set correct id and label
//			reader.getUpcomingEvents().add(new CharacterSetIntervalEvent(reader.getStreamDataProvider().getStartChar(), reader.getStreamDataProvider().getCurrentChar()));
//			reader.getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.CHARACTER_SET, EventTopologyType.END));
				
				//token sets
				reader.getUpcomingEvents().add(new TokenSetDefinitionEvent(type, id, label, charSetID));
				Collection<JPhyloIOEvent> tokenEvents = reader.getStreamDataProvider().getTokenSets().get(reader.getStreamDataProvider().getCurrentTokenSetID()).getSingleTokens();
				for (Iterator<JPhyloIOEvent> iterator = tokenEvents.iterator(); iterator.hasNext();) {
					JPhyloIOEvent nextEvent = iterator.next();
					reader.getUpcomingEvents().add(nextEvent);
				}
			}
		});
		
		map.put(new XMLElementType(TAG_FORMAT, TAG_STATES, XMLStreamConstants.START_ELEMENT), new NeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLEventReader reader, XMLEvent event) throws Exception {
				StartElement element = event.asStartElement();
				String id = XMLUtils.readStringAttr(element, ATTR_ID, null);
				String label = null;
				CharacterStateType setType = null;
				List<JPhyloIOEvent> singleTokens = new ArrayList<JPhyloIOEvent>();
				List<JPhyloIOEvent> charSetIntervals = new ArrayList<JPhyloIOEvent>();
				
				String tokenSetType = reader.getStreamDataProvider().getCurrentTokenSetType();
				if (tokenSetType.equals("")) {}
				else if (tokenSetType.equals(TYPE_DNA_SEQS) || tokenSetType.equals(TYPE_DNA_CELLS)) {
					label = "DNA";
					setType = CharacterStateType.DNA; //standard IUPAC nucleotide symbols
				}
				else if (tokenSetType.equals(TYPE_RNA_SEQS) || tokenSetType.equals(TYPE_RNA_CELLS)) {
					label = "RNA";
					setType = CharacterStateType.RNA;  //standard IUPAC nucleotide symbols
				}
				else if (tokenSetType.equals(TYPE_PROTEIN_SEQS) || tokenSetType.equals(TYPE_PROTEIN_CELLS)) {
					label = "AminoAcid";
					setType = CharacterStateType.AMINO_ACID;  //standard IUPAC amino acid symbols
				}
				else if (tokenSetType.equals(TYPE_CONTIN_SEQ) || tokenSetType.equals(TYPE_CONTIN_CELLS)) {
					label = "ContinuousData";
					setType = CharacterStateType.CONTINUOUS; 
				}
				else if (tokenSetType.equals(TYPE_RESTRICTION_SEQS) || tokenSetType.equals(TYPE_RESTRICTION_CELLS)) {
					label = "RestrictionSiteData";
					setType = CharacterStateType.DISCRETE; 
				}
				else { // type of character block is StandardSeqs or StandardCells
					label = "StandardData";
					setType = CharacterStateType.DISCRETE; 
				}
				
				reader.getStreamDataProvider().setCurrentTokenSetID(id);
				reader.getStreamDataProvider().getTokenSets().put(id, new NeXMLTokenSetInformation(id, label, setType));
				reader.getStreamDataProvider().getTokenSets().get(id).setSingleTokens(singleTokens);
				reader.getStreamDataProvider().getTokenSets().get(id).setCharSetIntervals(charSetIntervals);
			}
		});
		
		map.put(new XMLElementType(TAG_FORMAT, TAG_STATES, XMLStreamConstants.END_ELEMENT), new NeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLEventReader reader, XMLEvent event) throws Exception {
				reader.getStreamDataProvider().getTokenSets().get(reader.getStreamDataProvider().getCurrentTokenSetID()).getSingleTokens()
						.add(new ConcreteJPhyloIOEvent(EventContentType.TOKEN_SET_DEFINITION, EventTopologyType.END));
			}
		});
		
		map.put(new XMLElementType(TAG_STATES, TAG_STATE, XMLStreamConstants.START_ELEMENT), readStateStart);
		
		map.put(new XMLElementType(TAG_STATES, TAG_STATE, XMLStreamConstants.END_ELEMENT), readStateEnd);
		
		map.put(new XMLElementType(TAG_STATES, TAG_UNCERTAIN, XMLStreamConstants.START_ELEMENT), readStateStart);
		
		map.put(new XMLElementType(TAG_STATES, TAG_UNCERTAIN, XMLStreamConstants.END_ELEMENT), readStateEnd);
		
		map.put(new XMLElementType(TAG_STATES, TAG_POLYMORPHIC, XMLStreamConstants.START_ELEMENT), readStateStart);
		
		map.put(new XMLElementType(TAG_STATES, TAG_POLYMORPHIC, XMLStreamConstants.END_ELEMENT), readStateEnd);
		
		map.put(new XMLElementType(TAG_UNCERTAIN, TAG_MEMBER, XMLStreamConstants.START_ELEMENT), readMemberStart);
		
		map.put(new XMLElementType(TAG_POLYMORPHIC, TAG_MEMBER, XMLStreamConstants.START_ELEMENT), readMemberStart);
		
		map.put(new XMLElementType(TAG_FORMAT, TAG_CHAR, XMLStreamConstants.START_ELEMENT), new NeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLEventReader reader, XMLEvent event) throws Exception {
				StartElement element = event.asStartElement();
				String states =	XMLUtils.readStringAttr(element, ATTR_STATES, null);
				String previousStates = reader.getStreamDataProvider().getCurrentStates();
				if ((previousStates != null) && !reader.getStreamDataProvider().getCurrentStates().equals(states)) {
					reader.getUpcomingEvents().add(new CharacterSetIntervalEvent(reader.getStreamDataProvider().getStartChar(), reader.getStreamDataProvider().getCurrentChar()));
					reader.getStreamDataProvider().setStartChar(reader.getStreamDataProvider().getCurrentChar() + 1);
				}
				reader.getStreamDataProvider().setCurrentChar(reader.getStreamDataProvider().getCurrentChar() + 1);
			}
		});
		
		map.put(new XMLElementType(TAG_MATRIX, TAG_ROW, XMLStreamConstants.START_ELEMENT), new NeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLEventReader reader, XMLEvent event) throws Exception {
				StartElement element = event.asStartElement();
				OTUEventInformation otuInfo = getOTUEventInformation(reader, element);
	  		reader.getUpcomingEvents().add(new LinkedOTUEvent(EventContentType.SEQUENCE, otuInfo.id, otuInfo.label, otuInfo.otuID));
			}
		});
		
		map.put(new XMLElementType(TAG_MATRIX, TAG_ROW, XMLStreamConstants.END_ELEMENT), new NeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLEventReader reader, XMLEvent event) throws Exception {
				reader.getUpcomingEvents().add(new PartEndEvent(EventContentType.SEQUENCE, true));
			}
		});
		
		map.put(new XMLElementType(TAG_ROW, TAG_CELL, XMLStreamConstants.START_ELEMENT), new NeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLEventReader reader, XMLEvent event) throws Exception {
				StartElement element = event.asStartElement();				
				String token = XMLUtils.readStringAttr(element, ATTR_STATE, "-");	      		
				reader.getUpcomingEvents().add(new SingleSequenceTokenEvent(token));
			}
		});
		
		map.put(new XMLElementType(TAG_ROW, TAG_CELL, XMLStreamConstants.END_ELEMENT), new NeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLEventReader reader, XMLEvent event) throws Exception {
				reader.getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.SEQUENCE_TOKENS, EventTopologyType.END));
			}
		});
		
		map.put(new XMLElementType(TAG_SEQ, null, XMLStreamConstants.CHARACTERS), new NeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLEventReader reader, XMLEvent event) throws Exception {
				String tokens = event.asCharacters().toString(); // TODO Influence read length of character events
				List<String> tokenList = new ArrayList<String>();				
		   	for (int i = 0; i < tokens.length(); i++) {
	   	  	tokenList.add(Character.toString(tokens.charAt(i))); //TODO Handle tokens longer than one character
	   	  }
		   	reader.getUpcomingEvents().add(new SequenceTokensEvent(tokenList));				
			}
		});
		
		map.put(new XMLElementType(TAG_TREES, TAG_TREE, XMLStreamConstants.START_ELEMENT), new NeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLEventReader reader, XMLEvent event) throws Exception {
				StartElement element = event.asStartElement();
				
				OTUEventInformation info = getOTUEventInformation(reader, element);
				reader.getUpcomingEvents().add(new LabeledIDEvent(EventContentType.TREE, info.id,	info.label));
				
	  		String branchLengthsFormat = XMLUtils.readStringAttr(element, ATTR_TYPE, null);
				if (branchLengthsFormat.equals(null)) {}
				else if (branchLengthsFormat.equals(TYPE_FLOAT_TREE)) {
					reader.getStreamDataProvider().setCurrentBranchLengthsFormat(TYPE_FLOAT_TREE);
				}
				else if (branchLengthsFormat.equals(TYPE_INT_TREE)) {
					reader.getStreamDataProvider().setCurrentBranchLengthsFormat(TYPE_INT_TREE);
				}
			}
		});
		
		map.put(new XMLElementType(TAG_TREES, TAG_TREE, XMLStreamConstants.END_ELEMENT), new NeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLEventReader reader, XMLEvent event) throws Exception {
				reader.getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.TREE, EventTopologyType.END));
			}
		});
		
		map.put(new XMLElementType(TAG_TREES, TAG_NETWORK, XMLStreamConstants.START_ELEMENT), new NeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLEventReader reader, XMLEvent event) throws Exception {
				StartElement element = event.asStartElement();
				OTUEventInformation otuInfo = getOTUEventInformation(reader, element);
	  		reader.getUpcomingEvents().add(new LabeledIDEvent(EventContentType.NETWORK, otuInfo.id, otuInfo.label));
			}
		});
		
		map.put(new XMLElementType(TAG_TREES, TAG_NETWORK, XMLStreamConstants.END_ELEMENT), new NeXMLElementReader() {			
			@Override
			public void readEvent(NeXMLEventReader reader, XMLEvent event) throws Exception {
				reader.getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.NETWORK, EventTopologyType.END));
			}
		});
		
		map.put(new XMLElementType(TAG_TREE, TAG_NODE, XMLStreamConstants.START_ELEMENT), readNodeStart);
		
		map.put(new XMLElementType(TAG_TREE, TAG_NODE, XMLStreamConstants.END_ELEMENT), readNodeEnd);
		
		map.put(new XMLElementType(TAG_NETWORK, TAG_NODE, XMLStreamConstants.START_ELEMENT), readNodeStart);
		
		map.put(new XMLElementType(TAG_NETWORK, TAG_NODE, XMLStreamConstants.END_ELEMENT), readNodeEnd);
		
		map.put(new XMLElementType(TAG_TREE, TAG_EDGE, XMLStreamConstants.START_ELEMENT), readEdgeStart);
		
		map.put(new XMLElementType(TAG_TREE, TAG_EDGE, XMLStreamConstants.END_ELEMENT), readEdgeEnd);
		
		map.put(new XMLElementType(TAG_NETWORK, TAG_EDGE, XMLStreamConstants.START_ELEMENT), readEdgeStart);
		
		map.put(new XMLElementType(TAG_NETWORK, TAG_EDGE, XMLStreamConstants.END_ELEMENT), readEdgeEnd);
		
		return map; 
	}
	

	public NeXMLEventReader(File file, boolean translateMatchToken) throws IOException, XMLStreamException {
		this(new FileReader(file), translateMatchToken);
	}

	
	public NeXMLEventReader(InputStream stream, boolean translateMatchToken) throws IOException, XMLStreamException {
		this(new InputStreamReader(stream), translateMatchToken);
	}

	
	public NeXMLEventReader(XMLEventReader reader, boolean translateMatchToken) {
		super(translateMatchToken);
		this.xmlReader = reader;
		this.streamDataProvider = new NeXMLStreamDataProvider(this);
	}

	
	public NeXMLEventReader(Reader reader, boolean translateMatchToken) throws IOException, XMLStreamException {
		super(translateMatchToken);
		if (!(reader instanceof BufferedReader)) {
			reader = new BufferedReader(reader);
		}
		this.xmlReader = XMLInputFactory.newInstance().createXMLEventReader(reader);
		this.streamDataProvider = new NeXMLStreamDataProvider(this);
	}
	
	
	protected XMLEventReader getXMLReader() {
		return xmlReader;
	}

	protected Stack<QName> getEncounteredTags() {
		return encounteredTags;
	}


	public NeXMLStreamDataProvider getStreamDataProvider() {
		return streamDataProvider;
	}


	public void setStreamDataProvider(NeXMLStreamDataProvider streamDataProvider) {
		this.streamDataProvider = streamDataProvider;
	}


	@Override
	public int getMaxCommentLength() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public void setMaxCommentLength(int maxCommentLength) {
		// TODO Auto-generated method stub		
	}
	
	
	protected Queue<JPhyloIOEvent> getUpcomingEvents() {
		return super.getUpcomingEvents();
	}
	

	@Override
	protected void readNextEvent() throws Exception {
		NeXMLElementReader tagReader = null;
		
		while (xmlReader.hasNext() && getUpcomingEvents().isEmpty()) {
			XMLEvent xmlEvent = getXMLReader().nextEvent();			
			QName parentTag;
			
			if (xmlEvent.isEndElement()) {
				encounteredTags.pop();
			}
			if (!encounteredTags.isEmpty()) {
				parentTag = encounteredTags.peek();
			}
			else {
				parentTag = null;
			}
			
			int xmlEventType = xmlEvent.getEventType();
			switch (xmlEventType) {
				case 7: 
					getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.DOCUMENT, EventTopologyType.START));
					break;
				case 8:
					getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.DOCUMENT, EventTopologyType.END));
					break;
				case 1:
					StartElement startElement = xmlEvent.asStartElement();
					QName elementName = startElement.getName();				
					tagReader = ELEMENT_READER_MAP.get(new XMLElementType(parentTag, elementName, XMLStreamConstants.START_ELEMENT));
					encounteredTags.push(elementName);
					break;
				case 2:
					EndElement endElement = xmlEvent.asEndElement();			
					tagReader = ELEMENT_READER_MAP.get(new XMLElementType(parentTag, endElement.getName(), XMLStreamConstants.END_ELEMENT));
					break;
				case 4:				
					tagReader = ELEMENT_READER_MAP.get(new XMLElementType(parentTag, null, XMLStreamConstants.CHARACTERS));
					break;
				case 5:
					System.out.println("Comment");
					break;
				default: 
					XMLUtils.reachElementEnd(xmlReader);
			}
			
			if (tagReader != null) {
				tagReader.readEvent(this, xmlEvent);
			}			
		}
	}
	

	protected void readID(NeXMLEventReader reader, StartElement element) {
		String id = XMLUtils.readStringAttr(element, ATTR_ID, null);
		if (id != null) {
			reader.getUpcomingEvents().add(new MetaInformationEvent("id", "string", id));
			reader.getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.META_INFORMATION, EventTopologyType.END));
		}
	}
	
	
	@Override
	public void close() throws Exception {
		super.close();
		xmlReader.close();
	}	
}
