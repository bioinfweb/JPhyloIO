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
import info.bioinfweb.commons.bio.CharacterSymbolType;
import info.bioinfweb.commons.bio.SequenceUtils;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.SingleTokenDefinitionEvent;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.exception.JPhyloIOWriterException;
import info.bioinfweb.jphyloio.formats.nexml.NeXMLWriterStreamDataProvider;

import java.io.IOException;
import java.util.Collection;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;



public class NeXMLCollectTokenSetDefinitionDataReceiver extends NeXMLCollectNamespaceReceiver {
	
	
	public NeXMLCollectTokenSetDefinitionDataReceiver(XMLStreamWriter writer,
			ReadWriteParameterMap parameterMap, NeXMLWriterStreamDataProvider streamDataProvider) {
		super(writer, parameterMap, streamDataProvider);
	}
	
	
	private void checkSingleTokenDefinition(SingleTokenDefinitionEvent event) throws JPhyloIOWriterException {
		switch (getStreamDataProvider().getAlignmentType()) {							
			case DNA:
				if (!isDNAToken(event)) {
					if (isRNAToken(event)) {
						getStreamDataProvider().setAlignmentType(CharacterStateSetType.RNA);
					}
					else {
						getStreamDataProvider().setAlignmentType(CharacterStateSetType.DISCRETE);
					}
				}
				break;
			case RNA:
				if (!isRNAToken(event)) {					
					getStreamDataProvider().setAlignmentType(CharacterStateSetType.DISCRETE);					
				}
				break;
			case NUCLEOTIDE:
				if (isDNAToken(event)) {
					getStreamDataProvider().setAlignmentType(CharacterStateSetType.DNA);
				}
				else if (isRNAToken(event)) {
					getStreamDataProvider().setAlignmentType(CharacterStateSetType.RNA);
				}			
				else {
					getStreamDataProvider().setAlignmentType(CharacterStateSetType.DISCRETE);
				}
				break;
			case AMINO_ACID:
				if (!isAAToken(event)) {
					getStreamDataProvider().setAlignmentType(CharacterStateSetType.DISCRETE);
				}
				break;				
			case DISCRETE:
				getStreamDataProvider().getTokenDefinitions().add(event.getTokenName());
				break;
			default:
				break;
		}		
	}
	
	
	private boolean isDNAToken(SingleTokenDefinitionEvent event) {
		if (event.getTokenName().length() == 1) {
			char token = event.getTokenName().charAt(0);
			if (SequenceUtils.isDNAChar(token)) {
				if (event.getTokenType().equals(CharacterSymbolType.ATOMIC_STATE)) {
					if (SequenceUtils.isNonAmbiguityNucleotide(token)) {
						return true;
					}
					else if (isMissingChar(event) || isGapChar(event)) {
						return true;
					}
				}
				else if (event.getTokenType().equals(CharacterSymbolType.UNCERTAIN)) {	
					if (SequenceUtils.isNucleotideAmbuguityCode(token)) {
						return checkConstituents(event.getConstituents(), SequenceUtils.nucleotideConstituents(token));
					}
					else if (isGapChar(event)) {
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	
	private boolean isRNAToken(SingleTokenDefinitionEvent event) {
		if (event.getTokenName().length() == 1) {
			char token = event.getTokenName().charAt(0);
			if (SequenceUtils.isRNAChar(token)) {
				if (event.getTokenType().equals(CharacterSymbolType.ATOMIC_STATE)) {
					if (SequenceUtils.isNonAmbiguityNucleotide(token)) {
						return true;
					}
					else if (isMissingChar(event) || isGapChar(event)) {
						return true;
					}
				}
				else if (event.getTokenType().equals(CharacterSymbolType.UNCERTAIN)) {	
					if (SequenceUtils.isNucleotideAmbuguityCode(token)) {		
						return checkConstituents(event.getConstituents(), SequenceUtils.rnaConstituents(token));
					}
					else if (isGapChar(event)) {
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	
	private boolean isAAToken(SingleTokenDefinitionEvent event) {
		String token = event.getTokenName();

		if (event.getTokenType().equals(CharacterSymbolType.ATOMIC_STATE)) {
			if (SequenceUtils.isNonAmbiguityAminoAcid(token)) {
				return true;
			}				
			else if (isMissingChar(event) || isGapChar(event)) {
				return true;
			}
			else if (event.getMeaning().equals(CharacterSymbolMeaning.OTHER) && token.equals(SequenceUtils.STOP_CODON_CHAR)) { //TODO maybe create new CharacterSymbolMeaning?
				return true;
			}
		}		
		else if (event.getTokenType().equals(CharacterSymbolType.UNCERTAIN)) {	
			if (SequenceUtils.isAminoAcidAmbiguityCode(token)) {
				if (!(token.equals("J") || token.equals("Xle"))) {
					Collection<String> constituents = event.getConstituents();
					
					if (SequenceUtils.getAminoAcidOneLetterCodes(true).contains(token.charAt(0))) {
						checkConstituents(constituents, SequenceUtils.oneLetterAminoAcidConstituents(token));
					}
					else if (SequenceUtils.getAminoAcidThreeLetterCodes(true).contains(token)) {
						checkConstituents(constituents, SequenceUtils.threeLetterAminoAcidConstituents(token));
					}
					return true;
				}
			}
			else if (isGapChar(event)) {
				return true;
			}
		}		
		
		return false;
	}
	
	
	private boolean isGapChar(SingleTokenDefinitionEvent event) {
		if (event.getMeaning().equals(CharacterSymbolMeaning.GAP) && event.getTokenName().equals(SequenceUtils.GAP_CHAR)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	
	private boolean isMissingChar(SingleTokenDefinitionEvent event) {
		if (event.getMeaning().equals(CharacterSymbolMeaning.MISSING) && event.getTokenName().equals(SequenceUtils.MISSING_DATA_CHAR)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	
	private boolean checkConstituents(Collection<String> constituents, char[] expectedConstituents) {
		String[] expectedConstituentsString = new String[expectedConstituents.length];
		
		for (int i = 0; i < expectedConstituents.length; i++) {
			expectedConstituentsString[i] = Character.toString(expectedConstituents[i]);
		}
		
		return checkConstituents(constituents, expectedConstituentsString);
	}
	
	
	private boolean checkConstituents(Collection<String> constituents, String[] expectedConstituents) {
		if (!constituents.isEmpty()) {
			if (constituents.size() == expectedConstituents.length) {
				boolean isContained = true;
				for (int i = 0; i < expectedConstituents.length; i++) {
					isContained = constituents.contains(expectedConstituents[i]);
					if (!isContained) {
						return false;
					}
				}
				if (isContained) {
					return true;
				}
			}
		}
		else {
			return true;
		}
		
		return false;
	}
	

	@Override
	protected boolean doAdd(JPhyloIOEvent event) throws IOException, XMLStreamException {		
		switch (event.getType().getContentType()) {
			case SINGLE_TOKEN_DEFINITION:
				if (event.getType().getTopologyType().equals(EventTopologyType.START)) {
					SingleTokenDefinitionEvent tokenDefinitionEvent = event.asSingleTokenDefinitionEvent();
					if (!tokenDefinitionEvent.getMeaning().equals(CharacterSymbolMeaning.MATCH)) {
						checkSingleTokenDefinition(tokenDefinitionEvent);
					}
				}
				break;
			default:
				break;
		}
		return true;
	}
}
