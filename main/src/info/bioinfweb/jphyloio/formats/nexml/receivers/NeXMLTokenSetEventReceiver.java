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
package info.bioinfweb.jphyloio.formats.nexml.receivers;


import info.bioinfweb.commons.bio.CharacterStateSetType;
import info.bioinfweb.commons.bio.CharacterSymbolMeaning;
import info.bioinfweb.commons.bio.CharacterSymbolType;
import info.bioinfweb.commons.bio.SequenceUtils;
import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.SingleTokenDefinitionEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralContentSequenceType;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataContentEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.URIOrStringIdentifier;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.exception.InconsistentAdapterDataException;
import info.bioinfweb.jphyloio.formats.nexml.NeXMLWriterAlignmentInformation;
import info.bioinfweb.jphyloio.formats.nexml.NeXMLWriterStreamDataProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;



/**
 * Receiver that is used to write token sets.
 * 
 * @author Sarah Wiechers
 */
public class NeXMLTokenSetEventReceiver extends NeXMLMetaDataReceiver {
	String tokenSetID;
	private Map<String, String> tokenNameToIDMap = new HashMap<String, String>();
	private int tokenDefinitionIndex = 0;
	
	
	public NeXMLTokenSetEventReceiver(NeXMLWriterStreamDataProvider streamDataProvider,
			ReadWriteParameterMap parameterMap, String tokenSetID) {
		super(streamDataProvider, parameterMap);
		this.tokenSetID = tokenSetID;
	}


	private void writeState(SingleTokenDefinitionEvent event) throws XMLStreamException, IOException {
		getStreamDataProvider().getWriter().writeStartElement(TAG_STATE.getLocalPart());
		writeTokenDefinitionAttributes(event);
	}
	
	
	private void writeStateSet(SingleTokenDefinitionEvent event, boolean isPolymorphic) throws XMLStreamException, IOException {
		NeXMLWriterAlignmentInformation alignmentInfo = getStreamDataProvider().getCurrentAlignmentInfo();
		String memberID = null;
		String tokenName = event.getTokenName();
		
		if (isPolymorphic) {
			getStreamDataProvider().getWriter().writeStartElement(TAG_POLYMORPHIC.getLocalPart());
		}
		else {
			getStreamDataProvider().getWriter().writeStartElement(TAG_UNCERTAIN.getLocalPart());
		}
		
		writeTokenDefinitionAttributes(event);
		
		if (!event.getMeaning().equals(CharacterSymbolMeaning.GAP)) {
			Collection<String> constituents = new ArrayList<String>();
			
			if (event.getConstituents() == null || event.getConstituents().isEmpty()) {
				switch (alignmentInfo.getAlignmentType()) {
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
						else if (SequenceUtils.getAminoAcidThreeLetterCodes(true).contains(tokenName)) {
							tokenName = Character.toString(SequenceUtils.oneLetterAminoAcidByThreeLetter(tokenName));
							constituents = addConstituents(SequenceUtils.oneLetterAminoAcidConstituents(tokenName));
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
				getStreamDataProvider().getWriter().writeEmptyElement(TAG_MEMBER.getLocalPart());
				memberID = tokenNameToIDMap.get(tokenDefinition);
				if ((memberID != null) && getStreamDataProvider().getDocumentIDs().contains(memberID)) {
					getStreamDataProvider().getWriter().writeAttribute(ATTR_SINGLE_STATE_LINK.getLocalPart(), memberID);
				}
				else {
					throw new InconsistentAdapterDataException("The token definition set with the ID \"" + event.getID() 
							+ "\" linked to the token definition ID \"" + memberID + "\" of the token \"" + tokenDefinition + "\" which does not exist in the adapter data.");
				}
			}
		}
	}
	
	
	private void writeTokenDefinitionAttributes(SingleTokenDefinitionEvent event) throws XMLStreamException, IOException {
		NeXMLWriterAlignmentInformation alignmentInfo = getStreamDataProvider().getCurrentAlignmentInfo();
		
		String tokenName = event.getTokenName();
		String tokenSymbol = tokenName;
		String label = event.getLabel();
		
		if (alignmentInfo.getAlignmentType().equals(CharacterStateSetType.DISCRETE)) {
			if (event.getMeaning().equals(CharacterSymbolMeaning.GAP)) {
				if (!((tokenName.length() == 1) && (tokenName.charAt(0) == SequenceUtils.GAP_CHAR))) {
					tokenSymbol = "" + tokenDefinitionIndex;
					tokenDefinitionIndex++;
					label = tokenName;
				}
			}
			else if (event.getMeaning().equals(CharacterSymbolMeaning.MISSING)) {
				if (!((tokenName.length() == 1) && (tokenName.charAt(0) == SequenceUtils.MISSING_DATA_CHAR))) {
					tokenSymbol = "" + tokenDefinitionIndex;
					tokenDefinitionIndex++;
					label = tokenName;
				}
			}
			else {
				tokenSymbol = "" + tokenDefinitionIndex;
				tokenDefinitionIndex++;
				label = tokenName;
			}
		}
		else if (alignmentInfo.getAlignmentType().equals(CharacterStateSetType.AMINO_ACID)) {
			if (tokenName.length() == 3) {
				tokenSymbol = Character.toString(SequenceUtils.oneLetterAminoAcidByThreeLetter(tokenName));
				label = tokenName;
			}
		}
		
		alignmentInfo.getIDToTokenSetInfoMap().get(tokenSetID).getTokenTranslationMap().put(event.getTokenName(), tokenSymbol);
		tokenNameToIDMap.put(tokenName, event.getID());
		
		getStreamDataProvider().getWriter().writeAttribute(ATTR_ID.getLocalPart(), event.getID());
		getStreamDataProvider().getWriter().writeAttribute(ATTR_ABOUT.getLocalPart(), "#" + event.getID());
		
		if (label != null) {
			if (getParameterMap().getBoolean(ReadWriteParameterMap.KEY_NEXML_TOKEN_DEFINITION_LABEL, false)) {
				getStreamDataProvider().getWriter().writeAttribute(ATTR_LABEL.getLocalPart(), event.getLabel());
			}
			else {
				getStreamDataProvider().getWriter().writeAttribute(ATTR_LABEL.getLocalPart(), label);
			}
		}
		
		getStreamDataProvider().getWriter().writeAttribute(ATTR_SYMBOL.getLocalPart(), tokenSymbol);
		
		switch (getParameterMap().getLabelHandling()) {
			case NEITHER:
				break;
			case BOTH:
				if ((event.getLabel() != null) && !event.getLabel().isEmpty()) {
					writeMetaElement(PREDICATE_ORIGINAL_LABEL, event.getLabel());
				}
				
				if (!event.getTokenName().isEmpty()) {
					writeMetaElement(PREDICATE_ORIGINAL_TOKEN_NAME, event.getTokenName());
				}
				break;
			case DISCARDED:
				if (((event.getLabel() != null) && !event.getLabel().isEmpty()) && !label.equals(event.getLabel())) {
					writeMetaElement(PREDICATE_ORIGINAL_LABEL, event.getLabel());
				}
				
				if (!tokenSymbol.equals(event.getTokenName()) && !label.equals(event.getTokenName()) && !event.getTokenName().isEmpty()) {
					writeMetaElement(PREDICATE_ORIGINAL_TOKEN_NAME, event.getTokenName());
				}				
				break;
		}		
	}
	
	
	private void writeMetaElement(QName predicate, String content) throws XMLStreamException, IOException {
		AbstractNeXMLDataReceiverMixin.handleLiteralMeta(getStreamDataProvider(), 
				new LiteralMetadataEvent(getStreamDataProvider().createNewID(ReadWriteConstants.DEFAULT_META_ID_PREFIX), null, new URIOrStringIdentifier(null, predicate), 
						LiteralContentSequenceType.SIMPLE));
		AbstractNeXMLDataReceiverMixin.handleLiteralContentMeta(getStreamDataProvider(), getParameterMap(), 
				new LiteralMetadataContentEvent(content, content));
		AbstractNeXMLDataReceiverMixin.handleMetaEndEvent(getStreamDataProvider(), ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
	}
	
	
	private Collection<String> addConstituents(char[] molecularConstituents) {
		Collection<String> constituents = new ArrayList<String>();
		
		for (int i = 0; i < molecularConstituents.length; i++) {
			constituents.add(Character.toString(molecularConstituents[i]));
		}	
		
		return constituents;
	}
	
	
	public void writeRemainingStandardTokenDefinitions() throws IOException, XMLStreamException {
		Set<String> tokenNames = getStreamDataProvider().getCurrentAlignmentInfo().getIDToTokenSetInfoMap().get(tokenSetID).getOccuringTokens();
		
		for (String token : tokenNames) {
			String tokenDefinitionID = getStreamDataProvider().createNewID(ReadWriteConstants.DEFAULT_TOKEN_DEFINITION_ID_PREFIX);
			getStreamDataProvider().addToDocumentIDs(tokenDefinitionID);
			doAdd(new SingleTokenDefinitionEvent(tokenDefinitionID, null, token, CharacterSymbolMeaning.CHARACTER_STATE, CharacterSymbolType.ATOMIC_STATE, null));
			doAdd(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.SINGLE_TOKEN_DEFINITION));
		}
	}
	

	@Override
	protected boolean doAdd(JPhyloIOEvent event) throws IOException, XMLStreamException {
		switch (event.getType().getContentType()) {
			case SINGLE_TOKEN_DEFINITION:
				if (event.getType().getTopologyType().equals(EventTopologyType.START)) {
					SingleTokenDefinitionEvent tokenDefinitionEvent = event.asSingleTokenDefinitionEvent();
					if (!tokenDefinitionEvent.getMeaning().equals(CharacterSymbolMeaning.MATCH) 
							&& !tokenDefinitionEvent.getMeaning().equals(CharacterSymbolMeaning.OTHER)) {
						
						switch (tokenDefinitionEvent.getTokenType()) {
							case ATOMIC_STATE:
								if (tokenDefinitionEvent.getMeaning().equals(CharacterSymbolMeaning.GAP)) {
									writeStateSet(tokenDefinitionEvent, false);
								}
								else {
									writeState(tokenDefinitionEvent);
								}
								break;
							case POLYMORPHIC:
								writeStateSet(tokenDefinitionEvent, true);
								break;
							case UNCERTAIN:
								writeStateSet(tokenDefinitionEvent, false);
								break;
						}
						break;
					}
				}
				else {
					getStreamDataProvider().getWriter().writeEndElement();
				}
				break;
			default:
				break;
		}
		return true;
	}	
}
