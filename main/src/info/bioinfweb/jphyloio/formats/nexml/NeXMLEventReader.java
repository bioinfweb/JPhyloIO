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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;
import java.util.TreeMap;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import info.bioinfweb.commons.bio.CharacterStateType;
import info.bioinfweb.commons.io.XMLUtils;
import info.bioinfweb.jphyloio.AbstractEventReader;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.TokenSetDefinitionEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.events.type.EventType;



public class NeXMLEventReader extends AbstractEventReader implements NeXMLConstants {
	private static final Map<QName, NeXMLTagReader> METHOD_MAP = createMap();
	
	private Map<String, String> idToLabelMap = new TreeMap<String, String>();
	private XMLEventReader xmlReader;
	private Stack<QName> encounteredTags = new Stack<QName>();
	private String currentSequenceName;
//	private boolean parseStates = false;
	private Queue<JPhyloIOEvent> upcomingEvents = new LinkedList<JPhyloIOEvent>();

	
	private static Map<QName, NeXMLTagReader> createMap() {
		Map<QName, NeXMLTagReader> map = new HashMap<QName, NeXMLTagReader>();
		
		map.put(TAG_SEQ, new NeXMLTagReader() {			
			@Override
			public JPhyloIOEvent readEventCore(NeXMLEventReader reader, XMLEvent event) throws Exception {
				List<String> tokenList = new ArrayList<String>();
				if (event.isCharacters()) {
					String tokens = event.asCharacters().toString(); // TODO influence read length of character events
		   	  for (int i = 0; i < tokens.length(); i++) {
		   	  	tokenList.add(Character.toString(tokens.charAt(i))); //TODO handle tokens longer than one character
		   	  }		   	  
		   	  return reader.getSequenceTokensEventManager().createEvent(reader.getCurrentSequenceName(), tokenList);
				}
				else if (event.isStartElement()) {
	      	StartElement element = event.asStartElement();
	      	reader.getEncounteredTags().push(element.getName());
	      	if (element.getName().equals(TAG_META)) {
	      		reader.getUpcomingEvents().add(readMeta(reader, element));
	      		return new ConcreteJPhyloIOEvent(EventContentType.META_INFORMATION, EventTopologyType.END);
	      	}        
	      	else {
	      		XMLUtils.reachElementEnd(reader.getXMLReader());
	      		reader.getEncounteredTags().pop();
	      		return null;
	        }
				}
				else {
					return null;
				}
			}
		});
		
		map.put(TAG_ROW, new NeXMLTagReader() {			
			@Override
			protected JPhyloIOEvent readEventCore(NeXMLEventReader reader, XMLEvent event) throws Exception {
				if (event.isStartElement()) {
	      	StartElement element = event.asStartElement();
	      	reader.getEncounteredTags().push(element.getName());
	      	if (element.getName().equals(TAG_SEQ)) {
	      		return METHOD_MAP.get(TAG_SEQ).readEvent(reader);
	      	}
	      	else if (element.getName().equals(TAG_META)) {
	      		reader.getUpcomingEvents().add(readMeta(reader, element));
	      		return new ConcreteJPhyloIOEvent(EventContentType.META_INFORMATION, EventTopologyType.END);
	      	}        
	      	else {
	      		XMLUtils.reachElementEnd(reader.getXMLReader());
	      		reader.getEncounteredTags().pop();
	      		return null;
	        }	      	
	      }
				else {
					return null;
				}
			}
		});
		
		map.put(TAG_MATRIX, new NeXMLTagReader() {			
			@Override
			protected JPhyloIOEvent readEventCore(NeXMLEventReader reader, XMLEvent event) throws Exception {
	      if (event.isStartElement()) {
	      	StartElement element = event.asStartElement();
	      	reader.getEncounteredTags().push(element.getName());
	      	if (element.getName().equals(TAG_ROW)) {
	      		String sequenceName = XMLUtils.readStringAttr(element, ATTR_OTU, null);
	      		if (sequenceName != null) {
      				reader.setCurrentSequenceName(sequenceName);
	      		}
	      		return METHOD_MAP.get(TAG_ROW).readEvent(reader);
	      	}
	      	else if (element.getName().equals(TAG_META)) {
	      		reader.getUpcomingEvents().add(readMeta(reader, element));
	      		return new ConcreteJPhyloIOEvent(EventContentType.META_INFORMATION, EventTopologyType.END);
	      	}        
	      	else {
	      		XMLUtils.reachElementEnd(reader.getXMLReader());
	      		reader.getEncounteredTags().pop();
	      		return null;
	        }	      	
	      }	     
	      else {
	      	return null;
	      }
			}
		});
		
//		map.put(TAG_STATES, new NeXMLTagReader() {
//			@Override
//			protected JPhyloIOEvent readEventCore(NeXMLEventReader reader, XMLEvent event) throws Exception {
//				if (event.isStartElement()) {
//	      	StartElement element = event.asStartElement();
//	      	reader.getEncounteredTags().push(element.getName());
//	      	if (element.getName().equals(TAG_STATE) || element.getName().equals(TAG_POLYMORPHIC) || element.getName().equals(TAG_UNCERTAIN)) {
//	      		String id = null;
//	      		String symbol = null;
//	      		Iterator<Attribute> attributes = element.getAttributes();
//	      		while (attributes.hasNext()) {      			
//	      			Attribute attribute = attributes.next();	
////	      			if (attribute.getName().equals(ATTR_ID)) { //TODO eventuell per Abfrage in der Implementierung klären ob id auch geparsed werden soll
////	      				id = attribute.getValue(); 
////	      			}
//	      			if (attribute.getName().equals(ATTR_SYMBOL)) {
//	      				symbol = attribute.getValue(); //symbol is always an integer
//	      			}
//	      		}
//	      		return new SingleTokenDefinitionEvent(symbol, ChracterStateMeaning.OTHER); //since this method should only be used in standard character blocks the meaning is always other (non nucleotide or aa)
//	      	}        
//	      	else {
//	      		XMLUtils.reachElementEnd(reader.getXMLReader());
//	      		reader.getEncounteredTags().pop();
//	      		return null;
//	        }	      	
//	      }	     
//	      else {
//	      	return null;
//	      }
//			}
//		});
//		
//		map.put(TAG_FORMAT, new NeXMLTagReader() {			
//			@Override
//			protected JPhyloIOEvent readEventCore(NeXMLEventReader reader, XMLEvent event) throws Exception {
//				if (event.isStartElement()) {
//	      	StartElement element = event.asStartElement();
//	      	reader.getEncounteredTags().push(element.getName());
//	      	if (element.getName().equals(TAG_STATES)) {
//	      		return METHOD_MAP.get(TAG_STATES).readEvent(reader);
//	      	}        
//	      	else {
//	      		XMLUtils.reachElementEnd(reader.getXMLReader());
//	      		reader.getEncounteredTags().pop();
//	      		return null;
//	        }	      	
//	      }
//				else {
//					return null;
//				}
//			}
//		});
		
		map.put(TAG_CHARACTERS, new NeXMLTagReader() {			
			@Override
			protected JPhyloIOEvent readEventCore(NeXMLEventReader reader, XMLEvent event) throws Exception {
	      if (event.isStartElement()) {
	      	StartElement element = event.asStartElement();
	      	reader.getEncounteredTags().push(element.getName());
//	      	if (element.getName().equals(TAG_FORMAT) && reader.getParseStates()) {
//	      		return METHOD_MAP.get(TAG_FORMAT).readEvent(reader);
//	      	}	      	
	      	if (element.getName().equals(TAG_MATRIX)) {
	      		return METHOD_MAP.get(TAG_MATRIX).readEvent(reader);
	      	}
	      	else if (element.getName().equals(TAG_META)) {
	      		reader.getUpcomingEvents().add(readMeta(reader, element));
	      		return new ConcreteJPhyloIOEvent(EventContentType.META_INFORMATION, EventTopologyType.END);
	      	}        
	      	else {
	      		XMLUtils.reachElementEnd(reader.getXMLReader());
	      		reader.getEncounteredTags().pop();
	      		return null;
	        }	      	
	      }
	      else {
	      	return null;				
	      }
			}
		});
		
		map.put(TAG_OTU, new NeXMLTagReader() {			
			@Override
			protected JPhyloIOEvent readEventCore(NeXMLEventReader reader, XMLEvent event) throws Exception {
				if (event.isStartElement()) {
	      	StartElement element = event.asStartElement();
	      	reader.getEncounteredTags().push(element.getName());
	      	if (element.getName().equals(TAG_META)) {
	      		reader.getUpcomingEvents().add(readMeta(reader, element));
	      		return new ConcreteJPhyloIOEvent(EventContentType.META_INFORMATION, EventTopologyType.END);
	      	}        
	      	else {
	      		XMLUtils.reachElementEnd(reader.getXMLReader());
	      		reader.getEncounteredTags().pop();
	      		return null;
	        }	      	
	      }
				else {
					return null;
				}
			}
		});
		
		map.put(TAG_OTUS, new NeXMLTagReader() {			
			@Override
			protected JPhyloIOEvent readEventCore(NeXMLEventReader reader, XMLEvent event) throws Exception {	     
				if (event.isStartElement()) {
	      	StartElement element = event.asStartElement();
	      	reader.getEncounteredTags().push(element.getName());
	      	
	      	if (element.getName().equals(TAG_OTU)) {
	      		String id = XMLUtils.readStringAttr(element, ATTR_ID, null);
		  			String label = XMLUtils.readStringAttr(element, ATTR_LABEL, null);	    			
	    		
	    			if (label != null && id != null) {
	    				reader.getIDToLabelMap().put(id, label);
	    			}		    			
		    		return METHOD_MAP.get(TAG_OTU).readEvent(reader);
	        }
	      	else if (element.getName().equals(TAG_META)) {
	      		reader.getUpcomingEvents().add(readMeta(reader, element));
	      		return new ConcreteJPhyloIOEvent(EventContentType.META_INFORMATION, EventTopologyType.END);
	      	}
	      	else {
	      		XMLUtils.reachElementEnd(reader.getXMLReader());
	      		reader.getEncounteredTags().pop();
	      		return null;
	      	}
	      }
				else  {
					return null;
				}
			}
		});
		
		map.put(TAG_META, new NeXMLTagReader() {			
			@Override
			protected JPhyloIOEvent readEventCore(NeXMLEventReader reader, XMLEvent event) throws Exception {
				if (event.isStartElement()) {
	      	StartElement element = event.asStartElement();
	      	reader.getEncounteredTags().push(element.getName());
	      	if (element.getName().equals(TAG_META)) {
	      		reader.getUpcomingEvents().add(readMeta(reader, element));
	      		return new ConcreteJPhyloIOEvent(EventContentType.META_INFORMATION, EventTopologyType.END);
	      	}        
	      	else {
	      		XMLUtils.reachElementEnd(reader.getXMLReader());
	      		reader.getEncounteredTags().pop();
	      		return null;
	        }	      	
	      }
				else {
					return null;
				}				
			}
		});
		
		map.put(TAG_NEXML, new NeXMLTagReader() {			
			@Override
			protected JPhyloIOEvent readEventCore(NeXMLEventReader reader, XMLEvent event) throws Exception {
				if (event.isStartElement()) {					
	      	StartElement element = event.asStartElement();
	      	reader.getEncounteredTags().push(element.getName());
//	      	Map<String, String> attributeMap = readAttributes(reader, element);
	      	
	      	if (element.getName().equals(TAG_CHARACTERS)) {
	      		if (!reader.getPreviousEvent().getType().equals(
	      				new EventType(EventContentType.ALIGNMENT, EventTopologyType.START)) 
	      				|| !reader.getPreviousEvent().getType().getContentType().equals(EventContentType.SEQUENCE_CHARACTERS)) {
	      				//TODO Additionally check for single token event, when this is implemented! 
	      			
	  					reader.getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.ALIGNMENT, EventTopologyType.START));
	  				}

    				String tokenSetType = XMLUtils.readStringAttr(element, ATTR_TYPE, null);
    				TokenSetDefinitionEvent tokenSetEvent;
    				if (tokenSetType.equals(null)) {
    					return null;
    				}
    				else if (tokenSetType.equals(TYPE_DNA_SEQS) || tokenSetType.equals(TYPE_DNA_CELLS)) {
    					tokenSetEvent = new TokenSetDefinitionEvent(CharacterStateType.DNA, "DNA"); //standard IUPAC nucleotide symbols
    				}
    				else if (tokenSetType.equals(TYPE_RNA_SEQS) || tokenSetType.equals(TYPE_RNA_CELLS)) {
    					tokenSetEvent = new TokenSetDefinitionEvent(CharacterStateType.RNA, "RNA"); //standard IUPAC nucleotide symbols
    				}
    				else if (tokenSetType.equals(TYPE_PROTEIN_SEQS) || tokenSetType.equals(TYPE_PROTEIN_CELLS)) {
    					tokenSetEvent = new TokenSetDefinitionEvent(CharacterStateType.AMINO_ACID, "AminoAcids"); //standard IUPAC amino acid symbols
    				}
    				else if (tokenSetType.equals(TYPE_CONTIN_SEQ) || tokenSetType.equals(TYPE_CONTIN_CELLS)) {
    					tokenSetEvent = new TokenSetDefinitionEvent(CharacterStateType.CONTINUOUS, "ContinuousData");
    				}
    				else if (tokenSetType.equals(TYPE_RESTRICTION_SEQS) || tokenSetType.equals(TYPE_RESTRICTION_CELLS)) {
//	      					reader.setParseStates(true);
    					tokenSetEvent = new TokenSetDefinitionEvent(CharacterStateType.DISCRETE, "RestrictionSiteData");
    				}
    				else { // type of character block is StandardSeqs or StandardCells
//	      					reader.setParseStates(true);
    					tokenSetEvent = new TokenSetDefinitionEvent(CharacterStateType.DISCRETE, "StandardData");
    				}
    				reader.getUpcomingEvents().add(tokenSetEvent);
      		
	      		return METHOD_MAP.get(TAG_CHARACTERS).readEvent(reader);
	      	}
	      	
	      	else if (element.getName().equals(TAG_OTUS)) {
	      		return METHOD_MAP.get(TAG_OTUS).readEvent(reader);
	      	}
	      	
	      	else if (element.getName().equals(TAG_META)) {
	      		reader.getUpcomingEvents().add(readMeta(reader, element));
	      		return new ConcreteJPhyloIOEvent(EventContentType.META_INFORMATION, EventTopologyType.END);
	      	}
	      	else {
	      		XMLUtils.reachElementEnd(reader.getXMLReader());
	      		reader.getEncounteredTags().pop();
	      		return null;
	        }	      	
	      }
	      else {
	      	return null;		
	      }
			}
		});
		
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


	public String getCurrentSequenceName() {
		return currentSequenceName;
	}
	
	
	public void setCurrentSequenceName(String currentSequenceName) {
		this.currentSequenceName = currentSequenceName;
	}


//	public boolean getParseStates() {
//		return parseStates;
//	}
//
//
//	public void setParseStates(boolean isStandardData) {
//		this.parseStates = isStandardData;
//	}


	public Queue<JPhyloIOEvent> getUpcomingEvents() {
		return upcomingEvents;
	}


	public void setUpcomingEvents(Queue<JPhyloIOEvent> upcomingEvents) {
		this.upcomingEvents = upcomingEvents;
	}


	@Override
	protected JPhyloIOEvent readNextEvent() throws Exception {
		XMLEvent xmlEvent;
		JPhyloIOEvent result = null;
		NeXMLTagReader tagReader = null;
    
		while (xmlReader.hasNext() && result == null) {	
			if (encounteredTags.isEmpty())  {	
				xmlEvent = xmlReader.nextEvent();
	      if (xmlEvent.isStartElement()) {
        	StartElement element = xmlEvent.asStartElement();
        	if (element.getName().equals(TAG_NEXML)) {
        		encounteredTags.push(element.getName());
        		
//        		String about = XMLUtils.readStringAttr(element, ATTR_ABOUT, null);
//        		if (about != null) {
//        			METHOD_MAP.get(TAG_NEXML).setAbout(about);
//        		}
		    		
        		result = new ConcreteJPhyloIOEvent(EventContentType.DOCUMENT, EventTopologyType.START);
        	}
        	else {
        		XMLUtils.reachElementEnd(xmlReader);
        		result = new ConcreteJPhyloIOEvent(EventContentType.DOCUMENT, EventTopologyType.END);
        	}
	      }
			}		
		
			else {
				QName currentTag = encounteredTags.peek();
	      tagReader = METHOD_MAP.get(currentTag);	      
	      if (tagReader != null) {
	      	result = METHOD_MAP.get(currentTag).readEvent(this);
	      }
	      else {
      		XMLUtils.reachElementEnd(xmlReader);
      		encounteredTags.pop();
      	}	      
	    }
		}
		if (!upcomingEvents.isEmpty()) {
			upcomingEvents.add(result);
			return upcomingEvents.poll();
		}
		else {
			return result;
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
	
	
	@Override
	public void close() throws Exception {
		super.close();
		xmlReader.close();
	}	
}
