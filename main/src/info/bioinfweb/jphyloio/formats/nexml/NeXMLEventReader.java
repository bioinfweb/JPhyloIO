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
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;
import java.util.TreeMap;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import info.bioinfweb.commons.bio.CharacterStateType;
import info.bioinfweb.commons.io.XMLUtils;
import info.bioinfweb.jphyloio.AbstractEventReader;
import info.bioinfweb.jphyloio.JPhyloIOReaderException;
import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.EdgeEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.LinkedOTUEvent;
import info.bioinfweb.jphyloio.events.MetaInformationEvent;
import info.bioinfweb.jphyloio.events.PartEndEvent;
import info.bioinfweb.jphyloio.events.SequenceTokensEvent;
import info.bioinfweb.jphyloio.events.SingleSequenceTokenEvent;
import info.bioinfweb.jphyloio.events.TokenSetDefinitionEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.events.type.EventType;



public class NeXMLEventReader extends AbstractEventReader implements NeXMLConstants {
	private static final Map<XMLElementType, NeXMLTagReader> ELEMENT_READER_MAP = createMap();
	
	private Map<String, String> idToLabelMap = new TreeMap<String, String>();
	private XMLEventReader xmlReader;
	private Stack<QName> encounteredTags = new Stack<QName>();
	private String currentBranchLengthsFormat;
//	private boolean parseStates = false;

	
	private static Map<XMLElementType, NeXMLTagReader> createMap() {
		Map<XMLElementType, NeXMLTagReader> map = new HashMap<XMLElementType, NeXMLTagReader>();
		
		NeXMLTagReader readMetaStart = new NeXMLTagReader() {			
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
		
		NeXMLTagReader readMetaEnd = new NeXMLTagReader() {			
			@Override
			public void readEvent(NeXMLEventReader reader, XMLEvent event) throws Exception {
				reader.getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.META_INFORMATION, EventTopologyType.END));
			}
		};
		
		NeXMLTagReader readNodeStart = new NeXMLTagReader() {			
			@Override
			public void readEvent(NeXMLEventReader reader, XMLEvent event) throws Exception {
				StartElement element = event.asStartElement();				
				OTUEventInformation info = getOTUEventInformation(reader, element);
				reader.getUpcomingEvents().add(new LinkedOTUEvent(EventContentType.NODE, info.id,	info.label, info.otuID));
			}
		};
		
		NeXMLTagReader readNodeEnd = new NeXMLTagReader() {			
			@Override
			public void readEvent(NeXMLEventReader reader, XMLEvent event) throws Exception {
				reader.getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.NODE, EventTopologyType.END));
			}
		};
		
		NeXMLTagReader readEdgeStart = new NeXMLTagReader() {			
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
		
		NeXMLTagReader readEdgeEnd = new NeXMLTagReader() {			
			@Override
			public void readEvent(NeXMLEventReader reader, XMLEvent event) throws Exception {
				reader.getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.EDGE, EventTopologyType.END));
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
		
		map.put(new XMLElementType(TAG_NEXML, TAG_OTUS, XMLStreamConstants.START_ELEMENT), new NeXMLTagReader() {			
			@Override
			public void readEvent(NeXMLEventReader reader, XMLEvent event) throws Exception {
				StartElement element = event.asStartElement();
				OTUEventInformation info = getOTUEventInformation(reader, element);
				reader.getUpcomingEvents().add(new LabeledIDEvent(EventContentType.OTU_LIST, info.id,	info.label));
			}
		});
		
		map.put(new XMLElementType(TAG_NEXML, TAG_OTUS, XMLStreamConstants.END_ELEMENT), new NeXMLTagReader() {			
			@Override
			public void readEvent(NeXMLEventReader reader, XMLEvent event) throws Exception {
				reader.getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.OTU_LIST, EventTopologyType.END));
			}
		});
		
		map.put(new XMLElementType(TAG_OTUS, TAG_OTU, XMLStreamConstants.START_ELEMENT), new NeXMLTagReader() {			
			@Override
			public void readEvent(NeXMLEventReader reader, XMLEvent event) throws Exception {
				StartElement element = event.asStartElement();
				String id = XMLUtils.readStringAttr(element, ATTR_ID, null);
				String label = XMLUtils.readStringAttr(element, ATTR_LABEL, null);			
				
				reader.getIDToLabelMap().put(id, label);
				reader.getUpcomingEvents().add(new LabeledIDEvent(EventContentType.OTU, id, label));
			}
		});
		
		map.put(new XMLElementType(TAG_OTUS, TAG_OTU, XMLStreamConstants.END_ELEMENT), new NeXMLTagReader() {			
			@Override
			public void readEvent(NeXMLEventReader reader, XMLEvent event) throws Exception {
				reader.getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.OTU, EventTopologyType.END));
			}
		});
		
		map.put(new XMLElementType(TAG_NEXML, TAG_CHARACTERS, XMLStreamConstants.START_ELEMENT), new NeXMLTagReader() {		
			@Override
			public void readEvent(NeXMLEventReader reader, XMLEvent event) throws Exception {
				StartElement element = event.asStartElement();
				OTUEventInformation info = getOTUEventInformation(reader, element);				
				
				if (!reader.getPreviousEvent().getType().equals(new EventType(EventContentType.ALIGNMENT, EventTopologyType.START)) 
	  				|| !reader.getPreviousEvent().getType().getContentType().equals(EventContentType.SEQUENCE)) {
	  				//TODO Additionally check for single token event, when this is implemented!
					reader.getUpcomingEvents().add(new LabeledIDEvent(EventContentType.ALIGNMENT, info.id,	info.label));
				}
	
				String tokenSetType = XMLUtils.readStringAttr(element, ATTR_TYPE, "");
				TokenSetDefinitionEvent tokenSetEvent = null;
				if (tokenSetType.equals("")) {}
				else if (tokenSetType.equals(TYPE_DNA_SEQS) || tokenSetType.equals(TYPE_DNA_CELLS)) {
					tokenSetEvent = new TokenSetDefinitionEvent(CharacterStateType.DNA, 
							ReadWriteConstants.DEFAULT_TOKEN_SET_ID_PREFIX + reader.getIDManager().createNewID(), "DNA"); //standard IUPAC nucleotide symbols
				}
				else if (tokenSetType.equals(TYPE_RNA_SEQS) || tokenSetType.equals(TYPE_RNA_CELLS)) {
					tokenSetEvent = new TokenSetDefinitionEvent(CharacterStateType.RNA, 
							ReadWriteConstants.DEFAULT_TOKEN_SET_ID_PREFIX + reader.getIDManager().createNewID(), "RNA"); //standard IUPAC nucleotide symbols
				}
				else if (tokenSetType.equals(TYPE_PROTEIN_SEQS) || tokenSetType.equals(TYPE_PROTEIN_CELLS)) {
					tokenSetEvent = new TokenSetDefinitionEvent(CharacterStateType.AMINO_ACID, 
							ReadWriteConstants.DEFAULT_TOKEN_SET_ID_PREFIX + reader.getIDManager().createNewID(), "AminoAcids"); //standard IUPAC amino acid symbols
				}
				else if (tokenSetType.equals(TYPE_CONTIN_SEQ) || tokenSetType.equals(TYPE_CONTIN_CELLS)) {
					tokenSetEvent = new TokenSetDefinitionEvent(CharacterStateType.CONTINUOUS, 
							ReadWriteConstants.DEFAULT_TOKEN_SET_ID_PREFIX + reader.getIDManager().createNewID(), "ContinuousData");
				}
				else if (tokenSetType.equals(TYPE_RESTRICTION_SEQS) || tokenSetType.equals(TYPE_RESTRICTION_CELLS)) {
	//  					reader.setParseStates(true);
					tokenSetEvent = new TokenSetDefinitionEvent(CharacterStateType.DISCRETE, 
							ReadWriteConstants.DEFAULT_TOKEN_SET_ID_PREFIX + reader.getIDManager().createNewID(), "RestrictionSiteData");
				}
				else { // type of character block is StandardSeqs or StandardCells
	//  					reader.setParseStates(true);
					tokenSetEvent = new TokenSetDefinitionEvent(CharacterStateType.DISCRETE, 
							ReadWriteConstants.DEFAULT_TOKEN_SET_ID_PREFIX + reader.getIDManager().createNewID(), "StandardData");
				}
				reader.getUpcomingEvents().add(tokenSetEvent);
				reader.getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.TOKEN_SET_DEFINITION, EventTopologyType.END)); //TODO move generating end event to a different element reader when single token definitions are allowed
			}
		});
		
		map.put(new XMLElementType(TAG_NEXML, TAG_CHARACTERS, XMLStreamConstants.END_ELEMENT), new NeXMLTagReader() {			
			@Override
			public void readEvent(NeXMLEventReader reader, XMLEvent event) throws Exception {
				reader.getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.ALIGNMENT, EventTopologyType.END));
			}
		});
		
		map.put(new XMLElementType(TAG_MATRIX, TAG_ROW, XMLStreamConstants.START_ELEMENT), new NeXMLTagReader() {			
			@Override
			public void readEvent(NeXMLEventReader reader, XMLEvent event) throws Exception {
				StartElement element = event.asStartElement();
				OTUEventInformation otuInfo = getOTUEventInformation(reader, element);
	  		reader.getUpcomingEvents().add(new LinkedOTUEvent(EventContentType.SEQUENCE, otuInfo.id, otuInfo.label, otuInfo.otuID));
			}
		});
		
		map.put(new XMLElementType(TAG_MATRIX, TAG_ROW, XMLStreamConstants.END_ELEMENT), new NeXMLTagReader() {			
			@Override
			public void readEvent(NeXMLEventReader reader, XMLEvent event) throws Exception {
				reader.getUpcomingEvents().add(new PartEndEvent(EventContentType.SEQUENCE, true));
			}
		});
		
		map.put(new XMLElementType(TAG_ROW, TAG_CELL, XMLStreamConstants.START_ELEMENT), new NeXMLTagReader() {			
			@Override
			public void readEvent(NeXMLEventReader reader, XMLEvent event) throws Exception {
				StartElement element = event.asStartElement();				
				String token = XMLUtils.readStringAttr(element, ATTR_STATE, "-");	      		
				reader.getUpcomingEvents().add(new SingleSequenceTokenEvent(token));
			}
		});
		
		map.put(new XMLElementType(TAG_ROW, TAG_CELL, XMLStreamConstants.END_ELEMENT), new NeXMLTagReader() {			
			@Override
			public void readEvent(NeXMLEventReader reader, XMLEvent event) throws Exception {
				reader.getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.SEQUENCE_TOKENS, EventTopologyType.END));
			}
		});
		
		map.put(new XMLElementType(TAG_SEQ, null, XMLStreamConstants.CHARACTERS), new NeXMLTagReader() {			
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
		
		map.put(new XMLElementType(TAG_TREES, TAG_TREE, XMLStreamConstants.START_ELEMENT), new NeXMLTagReader() {			
			@Override
			public void readEvent(NeXMLEventReader reader, XMLEvent event) throws Exception {
				StartElement element = event.asStartElement();
				
				OTUEventInformation info = getOTUEventInformation(reader, element);
				reader.getUpcomingEvents().add(new LabeledIDEvent(EventContentType.TREE, info.id,	info.label));
				
	  		String branchLengthsFormat = XMLUtils.readStringAttr(element, ATTR_TYPE, null);
				if (branchLengthsFormat.equals(null)) {}
				else if (branchLengthsFormat.equals(TYPE_FLOAT_TREE)) {
					reader.setCurrentBranchLengthsFormat(TYPE_FLOAT_TREE);
				}
				else if (branchLengthsFormat.equals(TYPE_INT_TREE)) {
					reader.setCurrentBranchLengthsFormat(TYPE_INT_TREE);
				}
			}
		});
		
		map.put(new XMLElementType(TAG_TREES, TAG_TREE, XMLStreamConstants.END_ELEMENT), new NeXMLTagReader() {			
			@Override
			public void readEvent(NeXMLEventReader reader, XMLEvent event) throws Exception {
				reader.getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.TREE, EventTopologyType.END));
			}
		});
		
		map.put(new XMLElementType(TAG_TREES, TAG_NETWORK, XMLStreamConstants.START_ELEMENT), new NeXMLTagReader() {			
			@Override
			public void readEvent(NeXMLEventReader reader, XMLEvent event) throws Exception {
				StartElement element = event.asStartElement();
				OTUEventInformation otuInfo = getOTUEventInformation(reader, element);
	  		reader.getUpcomingEvents().add(new LabeledIDEvent(EventContentType.NETWORK, otuInfo.id, otuInfo.label));
			}
		});
		
		map.put(new XMLElementType(TAG_TREES, TAG_NETWORK, XMLStreamConstants.END_ELEMENT), new NeXMLTagReader() {			
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

//		
//		map.put(TAG_STATES, new NeXMLTagReader() {
//			@Override
//			protected void readEventCore(NeXMLEventReader reader, XMLEvent event) throws Exception {
//				if (event.isStartElement()) {
//	      	StartElement element = event.asStartElement();
//	      	reader.getEncounteredTags().push(element.getName());
//	      	if (element.getName().equals(TAG_STATE) || element.getName().equals(TAG_POLYMORPHIC) || element.getName().equals(TAG_UNCERTAIN)) {
//	      		String id = XMLUtils.readStringAttr(element, ATTR_ID, null);
//	      		String label = XMLUtils.readStringAttr(element, ATTR_LABEL, null);
//	      		Integer symbol = XMLUtils.readIntAttr(element, ATTR_SYMBOL, 0);	//since this symbol is used in the alignment it should be parsed somehow   		
//	      		
//	      		if (label != null) {
//	      			reader.getUpcomingEvents().add(new SingleTokenDefinitionEvent(label, CharacterStateMeaning.OTHER)); //since this method should only be used in standard character blocks the meaning is always other (non nucleotide or aa)
//	      		}
//	      		else if (id != null) { // ID should never be null in a valid NeXML file
//	      			reader.getUpcomingEvents().add(new SingleTokenDefinitionEvent(id, CharacterStateMeaning.OTHER));
//	      		}
//	      	}        
//	      	else {
//	      		XMLUtils.reachElementEnd(reader.getXMLReader());
//	      		reader.getEncounteredTags().pop();
//	        }	      	
//	      }
//			}
//		});
//		
//		map.put(TAG_CHARACTERS, new NeXMLTagReader() {			
//			@Override
//			protected void readEventCore(NeXMLEventReader reader, XMLEvent event) throws Exception {
//	      if (event.isStartElement()) {
//	      	StartElement element = event.asStartElement();
//	      	reader.getEncounteredTags().push(element.getName());
////	      	if (element.getName().equals(TAG_FORMAT) && reader.getParseStates()) { //TODO is there any necessary information in the format block that should be parsed?
////	      		return METHOD_MAP.get(TAG_FORMAT).readEvent(reader);
////	      	}	      	
//	      	if (element.getName().equals(TAG_MATRIX)) {}
//	      	else if (element.getName().equals(TAG_META)) {
//	      		readMeta(reader, element);
//	      	}        
//	      	else {
//	      		XMLUtils.reachElementEnd(reader.getXMLReader());
//	      		reader.getEncounteredTags().pop();
//	        }	      	
//	      }
//			}
//		});
		
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
	}

	
	public NeXMLEventReader(Reader reader, boolean translateMatchToken) throws IOException, XMLStreamException {
		super(translateMatchToken);
		if (!(reader instanceof BufferedReader)) {
			reader = new BufferedReader(reader);
		}
		this.xmlReader = XMLInputFactory.newInstance().createXMLEventReader(reader);
	}
	
	
	protected XMLEventReader getXMLReader() {
		return xmlReader;
	}


	protected Map<String, String> getIDToLabelMap() {
		return idToLabelMap;
	}


	protected Stack<QName> getEncounteredTags() {
		return encounteredTags;
	}


//	public boolean getParseStates() {
//		return parseStates;
//	}
//
//
//	public void setParseStates(boolean isStandardData) {
//		this.parseStates = isStandardData;
//	}


	public String getCurrentBranchLengthsFormat() {
		return currentBranchLengthsFormat;
	}


	public void setCurrentBranchLengthsFormat(String currentBranchLengthsFormat) {
		this.currentBranchLengthsFormat = currentBranchLengthsFormat;
	}


	@Override
	protected void readNextEvent() throws Exception {
		NeXMLTagReader tagReader = null;
		
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
	public void close() throws Exception {
		super.close();
		xmlReader.close();
	}	
}
