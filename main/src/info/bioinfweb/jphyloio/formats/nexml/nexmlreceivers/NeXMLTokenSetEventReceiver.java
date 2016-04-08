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
package info.bioinfweb.jphyloio.formats.nexml.nexmlreceivers;


import info.bioinfweb.commons.bio.CharacterStateSetType;
import info.bioinfweb.commons.bio.CharacterSymbolMeaning;
import info.bioinfweb.commons.bio.SequenceUtils;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.SingleTokenDefinitionEvent;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.exception.JPhyloIOWriterException;
import info.bioinfweb.jphyloio.formats.nexml.NeXMLWriterStreamDataProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;



public class NeXMLTokenSetEventReceiver extends NeXMLMetaDataReceiver {
	private Map<String, String> tokenNameToIDMap = new HashMap<String, String>();
	private int tokenDefinitionIndex = 0;
	
	
	public NeXMLTokenSetEventReceiver(XMLStreamWriter writer, ReadWriteParameterMap parameterMap,
			NeXMLWriterStreamDataProvider streamDataProvider) {
		super(writer, parameterMap, streamDataProvider);
	}
	
	
	private void writeTokenDefinitionAttributes(SingleTokenDefinitionEvent event) throws XMLStreamException, JPhyloIOWriterException {
		String tokenName = event.getTokenName();
		String tokenSymbol = tokenName;
		
		if (getStreamDataProvider().getAlignmentType().equals(CharacterStateSetType.DISCRETE)) {
			tokenSymbol = "" + tokenDefinitionIndex;
			tokenDefinitionIndex++;
		}
		
		getStreamDataProvider().getTokenTranslationMap().put(event.getTokenName(), tokenSymbol);
		tokenNameToIDMap.put(tokenName, event.getID());
		
		getStreamDataProvider().writeLabeledIDAttributes(event);
		getWriter().writeAttribute(ATTR_SYMBOL.getLocalPart(), tokenSymbol);
	}
	
	
	private void writeState(SingleTokenDefinitionEvent event) throws XMLStreamException, JPhyloIOWriterException {
		getWriter().writeStartElement(TAG_STATE.getLocalPart());
		writeTokenDefinitionAttributes(event);
	}
	
	
	private void writeStateSet(SingleTokenDefinitionEvent event, boolean isPolymorphic) throws XMLStreamException, JPhyloIOWriterException {
		String memberID = null;
		String tokenName = event.getTokenName();
		
		if (isPolymorphic) {
			getWriter().writeStartElement(TAG_POLYMORPHIC.getLocalPart());
		}
		else {
			getWriter().writeStartElement(TAG_UNCERTAIN.getLocalPart());
		}
		
		writeTokenDefinitionAttributes(event);
		
		if (!event.getMeaning().equals(CharacterSymbolMeaning.GAP)) {
			Collection<String> constituents = new ArrayList<String>();
			
			if (event.getConstituents() == null || event.getConstituents().isEmpty()) {
				switch (getStreamDataProvider().getAlignmentType()) {
					case DNA:
						constituents = addConstituents(SequenceUtils.nucleotideConstituents(tokenName.charAt(0)));
						break;
					case RNA:
						constituents = addConstituents(SequenceUtils.rnaConstituents(tokenName.charAt(0)));
						break;
					case AMINO_ACID:
						if (SequenceUtils.getAminoAcidOneLetterCodes(true).contains(tokenName)) {
							constituents = addConstituents(SequenceUtils.oneLetterAminoAcidConstituents(tokenName));
						}
						else if (SequenceUtils.getAminoAcidThreeLetterCodes(true).contains(tokenName)) { //TODO do not write events with 3 letter codes
							String[] threLetterCodeConstituents = SequenceUtils.threeLetterAminoAcidConstituents(tokenName);
							for (int i = 0; i < threLetterCodeConstituents.length; i++) {
								constituents.add(threLetterCodeConstituents[i]);
							}
						}		
						break;
					default:
						break;
				}						
			}
			else {
				constituents = event.getConstituents();
			}
			
			for (String tokenDefinition : constituents) {
				getWriter().writeEmptyElement(TAG_MEMBER.getLocalPart());
				memberID = tokenNameToIDMap.get(tokenDefinition);
				if ((memberID != null) && getStreamDataProvider().getDocumentIDs().contains(memberID)) { //TODO check this in collect data receiver?
					getWriter().writeAttribute(ATTR_STATE.getLocalPart(), memberID);
				}
			}			
		}
	}
	
	
	private Collection<String> addConstituents(char[] molecularConstituents) {
		Collection<String> constituents = new ArrayList<String>();
		
		for (int i = 0; i < molecularConstituents.length; i++) {
			constituents.add(Character.toString(molecularConstituents[i]));
		}	
		
		return constituents;
	}
	

	@Override
	protected boolean doAdd(JPhyloIOEvent event) throws IOException, XMLStreamException {
		switch (event.getType().getContentType()) {
			case SINGLE_TOKEN_DEFINITION:
				if (event.getType().getTopologyType().equals(EventTopologyType.START)) {
					SingleTokenDefinitionEvent singleTokenEvent = event.asSingleTokenDefinitionEvent();
					if (!singleTokenEvent.getMeaning().equals(CharacterSymbolMeaning.MATCH) 
							&& !singleTokenEvent.getMeaning().equals(CharacterSymbolMeaning.OTHER)) {
						switch (singleTokenEvent.getTokenType()) {
							case ATOMIC_STATE:
								if (singleTokenEvent.getMeaning().equals(CharacterSymbolMeaning.GAP)) {
									writeStateSet(singleTokenEvent, false);
								}
								else {
									writeState(singleTokenEvent);
								}
								break;
							case POLYMORPHIC:
								writeStateSet(singleTokenEvent, true);
								break;
							case UNCERTAIN:
								writeStateSet(singleTokenEvent, false);
								break;
							default:
								break; // Nothing to do.
						}
						break;
					}
				}
				else {
					getWriter().writeEndElement();
				}
				break;
			default:
				break;
		}
		return true;
	}	
}
