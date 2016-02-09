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


import java.util.ArrayList;
import java.util.List;

import info.bioinfweb.commons.bio.CharacterStateType;
import info.bioinfweb.commons.io.XMLUtils;
import info.bioinfweb.jphyloio.formats.xml.AbstractXMLElementReader;
import info.bioinfweb.jphyloio.formats.xml.XMLElementReader;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;



/**
 * Reads the contents of a NeXML tag, including attributes of possible sub tags.
 * 
 * @author Sarah Wiechers
 */
public abstract class AbstractNeXMLElementReader extends AbstractXMLElementReader<NeXMLStreamDataProvider> 
		implements XMLElementReader<NeXMLStreamDataProvider>, NeXMLConstants {
	
	protected static class LabeledIDEventInformation {
		public String id;
		public String label;
	}
	
	
	protected static class OTUEventInformation extends LabeledIDEventInformation {
		public String otuID;
	}
	
	
	protected List<String> readSequence(NeXMLStreamDataProvider streamDataProvider, String sequence, TranslateTokens translateTokens) {		
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
					XMLEvent nextEvent = streamDataProvider.getEventReader().getXMLReader().peek();
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

			if (streamDataProvider.getCharacterSetType().equals(CharacterStateType.DISCRETE) && !translateTokens.equals(TranslateTokens.NEVER)) { //standard data
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
	
	
	protected LabeledIDEventInformation getLabeledIDEventInformation(NeXMLStreamDataProvider streamDataProvider, 
			StartElement element) {
		LabeledIDEventInformation labeledIDEventInformation = new LabeledIDEventInformation();
		labeledIDEventInformation.id = XMLUtils.readStringAttr(element, ATTR_ID, null);
		labeledIDEventInformation.label = XMLUtils.readStringAttr(element, ATTR_LABEL, null);
		
		return labeledIDEventInformation;
	}
	
	
	protected OTUEventInformation getOTUEventInformation(NeXMLStreamDataProvider streamDataProvider, StartElement element) {
		LabeledIDEventInformation labeledIDEventInformation = getLabeledIDEventInformation(streamDataProvider, element);
		OTUEventInformation otuEventInformation = new OTUEventInformation();
		
		otuEventInformation.id = labeledIDEventInformation.id;
		otuEventInformation.label = labeledIDEventInformation.label;
		otuEventInformation.otuID = XMLUtils.readStringAttr(element, ATTR_OTU, null);
		
		if ((otuEventInformation.label == null) && (otuEventInformation.otuID != null)) {
			otuEventInformation.label = streamDataProvider.getOtuIDToLabelMap().get(otuEventInformation.otuID);
			if (otuEventInformation.label == null) {
				otuEventInformation.label = otuEventInformation.id;	
			}
		}
		
		return otuEventInformation;
	}
}