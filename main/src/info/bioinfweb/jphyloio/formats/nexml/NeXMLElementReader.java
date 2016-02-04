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

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;



/**
 * Reads the contents of a NeXML tag, including attributes of possible sub tags.
 * 
 * @author Sarah Wiechers
 */
public abstract class NeXMLElementReader implements NeXMLConstants {
	protected static class OTUEventInformation {
		public String id;
		public String label;	
		public String otuID;
	}
	
	
	protected abstract void readEvent(NeXMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception;

	
	protected List<String> readSequence(NeXMLStreamDataProvider streamDataProvider, String sequence, TranslateTokens translateTokens) {		
		List<String> tokenList = new ArrayList<String>();
		String previousToken = "";
   	String currentToken = "";
		Character currentChar;
		
		if (streamDataProvider.isAllowLongTokens()) {
			if (streamDataProvider.getCharacterSetType().equals(CharacterStateType.CONTINUOUS)) { //continuous data
				for (String token: sequence.split("\\s+")) {
					if (!token.isEmpty()) {
						tokenList.add(token);
						previousToken = token;
					}
				}
			}
			else if (streamDataProvider.getCharacterSetType().equals(CharacterStateType.DISCRETE)) { //standard data
				for (int i = 0; i < sequence.length(); i++) {
		 			currentChar = sequence.charAt(i);	 			
		 			if (!Character.isWhitespace(currentChar)) {
		 				currentToken += currentChar;
		 			}
		 			else {
		 				if (!currentToken.isEmpty()) {
		 					tokenList.add(currentToken);
		 					previousToken = currentToken;
						}		 				
		 				currentToken = "";
		 			}	   		
		 		}
				if (!currentToken.isEmpty()) {
 					tokenList.add(currentToken);
 					previousToken = currentToken;
				}
		 		if (!translateTokens.equals(TranslateTokens.NEVER)) {
		 			for (int i = 0; i < tokenList.size(); i++) {
		 				String currentStates = "standardstateset1";
//		 				String currentStates = streamDataProvider.getCharIDToStatesMap().get(streamDataProvider.getCharIDs().get(i));
		 	 			tokenList.set(i, streamDataProvider.getTokenSets().get(currentStates).getSymbolTranslationMap().get(tokenList.get(i)));
					} 			   			
		 		}
			}
			try {
//				System.out.println("Type: " + streamDataProvider.getEventReader().getXMLReader().peek().getEventType() + " Token: " + previousToken);				
			}
			catch (Exception e) {
				e.printStackTrace();
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
	
	
	protected OTUEventInformation getOTUEventInformation(NeXMLStreamDataProvider streamDataProvider, StartElement element) {
		OTUEventInformation otuEventInformation = new OTUEventInformation();
		otuEventInformation.id = XMLUtils.readStringAttr(element, ATTR_ID, null);
		otuEventInformation.label = XMLUtils.readStringAttr(element, ATTR_LABEL, null);
		otuEventInformation.otuID = XMLUtils.readStringAttr(element, ATTR_OTU, null);
		if ((otuEventInformation.label == null) && (otuEventInformation.otuID != null)) {
			otuEventInformation.label = streamDataProvider.getOtuIDToLabelMap().get(otuEventInformation.otuID);
		}
		if (otuEventInformation.label == null) {
			otuEventInformation.label = otuEventInformation.id;	
		}
		return otuEventInformation;
	}
}
