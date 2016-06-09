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
import info.bioinfweb.commons.io.XMLUtils;
import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.events.CharacterSetIntervalEvent;
import info.bioinfweb.jphyloio.exception.JPhyloIOReaderException;
import info.bioinfweb.jphyloio.formats.xml.AbstractXMLElementReader;
import info.bioinfweb.jphyloio.formats.xml.XMLElementReader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;



/**
 * Reads the contents of a NeXML tag, including attributes of possible sub tags.
 * 
 * @author Sarah Wiechers
 */
public abstract class AbstractNeXMLElementReader extends AbstractXMLElementReader<NeXMLReaderStreamDataProvider> 
		implements XMLElementReader<NeXMLReaderStreamDataProvider>, NeXMLConstants, ReadWriteConstants {
	
	protected static class LabeledIDEventInformation {
		public String id;
		public String label;
	}
	
	
	protected static class OTUorOTUSEventInformation extends LabeledIDEventInformation {
		public String otuOrOtusID;
	}
	
	
	protected List<String> readSequence(NeXMLReaderStreamDataProvider streamDataProvider, String sequence, TokenTranslationStrategy translateTokens) {		
		List<String> tokenList = new ArrayList<String>();
		String lastToken = "";
   	String currentToken = "";
		Character currentChar;
		
		if (streamDataProvider.isAllowLongTokens()) { //continuous and standard data
			if (streamDataProvider.getIncompleteToken() != null) {
				currentToken = streamDataProvider.getIncompleteToken();
				streamDataProvider.setIncompleteToken(null);
			}
			
			for (int i = 0; i < sequence.length(); i++) {
	 			currentChar = sequence.charAt(i);	 			
	 			if (!Character.isWhitespace(currentChar)) {
	 				currentToken += currentChar;
	 			}
	 			else {
	 				if (!currentToken.isEmpty()) {
	 					tokenList.add(currentToken);
					}		 				
	 				currentToken = "";
	 			}	   		
	 		}
			lastToken = currentToken;
			
			if (!Character.isWhitespace(sequence.charAt(sequence.length() - 1))) {
				try {
					XMLEvent nextEvent = streamDataProvider.getXMLReader().peek();
					if (nextEvent.getEventType() == XMLStreamConstants.CHARACTERS) {
						String nextSequence = nextEvent.asCharacters().getData();
						if (!Character.isWhitespace(nextSequence.charAt(0))) {
							streamDataProvider.setIncompleteToken(lastToken);
						}
					}
					else if (!currentToken.isEmpty()) {
						tokenList.add(currentToken);
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}			

			if (streamDataProvider.getCharacterSetType().equals(CharacterStateSetType.DISCRETE) && !translateTokens.equals(TokenTranslationStrategy.NEVER)) { //standard data
	 			for (int i = 0; i < tokenList.size(); i++) {	 				
		 			String currentStates = streamDataProvider.getCharIDToStatesMap().get(streamDataProvider.getCharIDs().get(i));
	 	 			tokenList.set(i, streamDataProvider.getTokenSets().get(currentStates).getSymbolTranslationMap().get(tokenList.get(i)));
				}		 		
			}
		}
		
		else { //DNA, RNA, AA & restriction data
			for (int i = 0; i < sequence.length(); i++) {
				currentChar = sequence.charAt(i);
				if (!Character.isWhitespace(currentChar)) {
					tokenList.add(currentChar.toString());
				}
	 		}
		}
		
   	return tokenList;
	}
	
	
	protected void createIntervalEvents(NeXMLReaderStreamDataProvider streamDataProvider, String[] charIDs) throws JPhyloIOReaderException, XMLStreamException {
		boolean[] charSets = new boolean[streamDataProvider.getCharIDs().size()];
		for (String charID: charIDs) {
			if (streamDataProvider.getCharIDToIndexMap().containsKey(charID)) {
				charSets[streamDataProvider.getCharIDToIndexMap().get(charID)] = true;
			}
			else {
				throw new JPhyloIOReaderException("A character set referenced the ID \"" + charID + "\" of a character that was not specified before.", streamDataProvider.getXMLReader().peek().getLocation());
			}
		}
		
		int currentIndex = -1;
		int startIndex = -1;
		
		for (int i = 0; i < charSets.length; i++) {
			if (charSets[i]) {
				currentIndex = i;
			}
			
			if ((i == 0) || (charSets[i] && !charSets[i - 1])) {
				startIndex = i;
			}
			
			if (charSets[i] && ((i + 1 == charSets.length) || !charSets[i + 1])) {
				streamDataProvider.getCurrentEventCollection().add(new CharacterSetIntervalEvent(startIndex, currentIndex + 1)); //the end of a character set interval is specified as the first index after the end of the sequence segment to be added to the specified character set
			}
		}
	}
	
	
	protected LabeledIDEventInformation getLabeledIDEventInformation(NeXMLReaderStreamDataProvider streamDataProvider, StartElement element) throws JPhyloIOReaderException {
		LabeledIDEventInformation labeledIDEventInformation = new LabeledIDEventInformation();
		labeledIDEventInformation.id = XMLUtils.readStringAttr(element, ATTR_ID, null);
		labeledIDEventInformation.label = XMLUtils.readStringAttr(element, ATTR_LABEL, null);
		
		if (labeledIDEventInformation.id != null) {
			return labeledIDEventInformation;
		}
		else {
			throw new JPhyloIOReaderException("The element \"" + element.getName().getLocalPart() + "\" must specify an ID.", element.getLocation());
		}
	}
	
	
	protected OTUorOTUSEventInformation getOTUorOTUSEventInformation(NeXMLReaderStreamDataProvider streamDataProvider, StartElement element) throws JPhyloIOReaderException {
		LabeledIDEventInformation labeledIDEventInformation = getLabeledIDEventInformation(streamDataProvider, element);
		OTUorOTUSEventInformation otuEventInformation = new OTUorOTUSEventInformation();
		
		otuEventInformation.id = labeledIDEventInformation.id;
		otuEventInformation.label = labeledIDEventInformation.label;
		otuEventInformation.otuOrOtusID = XMLUtils.readStringAttr(element, ATTR_OTU, null);
		if (otuEventInformation.otuOrOtusID == null) {
			otuEventInformation.otuOrOtusID = XMLUtils.readStringAttr(element, ATTR_OTUS, null);
		}
		
		if ((otuEventInformation.label == null) && (otuEventInformation.otuOrOtusID != null)) {
			otuEventInformation.label = streamDataProvider.getOtuIDToLabelMap().get(otuEventInformation.otuOrOtusID);
			if (otuEventInformation.label == null) {
				otuEventInformation.label = otuEventInformation.id;	
			}
		}
		
		return otuEventInformation;
	}
}
