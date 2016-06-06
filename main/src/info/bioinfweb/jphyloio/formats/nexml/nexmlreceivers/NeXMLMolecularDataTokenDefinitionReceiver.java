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
import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.CommentEvent;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.SingleTokenDefinitionEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataContentEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.ResourceMetadataEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.formats.nexml.NeXMLWriterStreamDataProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;



public class NeXMLMolecularDataTokenDefinitionReceiver extends AbstractNeXMLDataReceiver {
	private NeXMLTokenSetEventReceiver receiver;
	private Set<Character> tokens = new HashSet<Character>();


	public NeXMLMolecularDataTokenDefinitionReceiver(XMLStreamWriter writer, ReadWriteParameterMap parameterMap,
			NeXMLWriterStreamDataProvider streamDataProvider) {
		super(writer, parameterMap, streamDataProvider);
		receiver = new NeXMLTokenSetEventReceiver(writer, parameterMap, streamDataProvider);
	}


	private void writeDNATokenDefinitions(NeXMLTokenSetEventReceiver receiver) throws IOException, XMLStreamException {
		Set<Character> remainingTokens = SequenceUtils.getNucleotideCharacters();
		remainingTokens.removeAll(tokens);
		remainingTokens.remove('U');

		List<String> states = new ArrayList<String>();
		for (int i = 0; i < SequenceUtils.DNA_CHARS.length(); i++) {
			states.add(Character.toString(SequenceUtils.DNA_CHARS.charAt(i)));
		}  //TODO Default tokens should be added to the set in the stream data provider instead of just writing them to the file here.

		writeTokenDefinitionEvents(receiver, remainingTokens, CharacterStateSetType.DNA, states);
	}


	private void writeRNATokenDefinitions(NeXMLTokenSetEventReceiver receiver) throws IOException, XMLStreamException {
		Set<Character> remainingTokens = SequenceUtils.getNucleotideCharacters();
		remainingTokens.removeAll(tokens);
		remainingTokens.remove('T');

		List<String> states = new ArrayList<String>();
		for (int i = 0; i < SequenceUtils.RNA_CHARS.length(); i++) {
			states.add(Character.toString(SequenceUtils.RNA_CHARS.charAt(i)));
		}

		writeTokenDefinitionEvents(receiver, remainingTokens, CharacterStateSetType.RNA, states);
	}


	private void writeAminoAcidTokenDefinitions(NeXMLTokenSetEventReceiver receiver) throws IOException, XMLStreamException {
		Set<Character> remainingTokens = SequenceUtils.getAminoAcidOneLetterCodes(true);
		remainingTokens.removeAll(tokens);
		remainingTokens.remove('J');

		List<String> states = new ArrayList<String>();
		for (Character state : SequenceUtils.getAminoAcidOneLetterCodes(false)) {
			states.add(Character.toString(state));
		}

		writeTokenDefinitionEvents(receiver, remainingTokens, CharacterStateSetType.AMINO_ACID, states);
	}


	private void writeTokenDefinitionEvents(NeXMLTokenSetEventReceiver receiver, Set<Character> remainingTokens, CharacterStateSetType alignmentType,
			Collection<String> atomicStates) throws IOException, XMLStreamException {
		List<String> constituents;
		CharacterSymbolType type;

		for (Character token : remainingTokens) {
			constituents = null;
			type = CharacterSymbolType.ATOMIC_STATE;
			char[] constituentChars = new char[0];

			if (alignmentType.equals(CharacterStateSetType.DNA)) {
				constituentChars = SequenceUtils.nucleotideConstituents(token);
			}
			else if (alignmentType.equals(CharacterStateSetType.RNA)) {
				constituentChars = SequenceUtils.rnaConstituents(token);
			}
			else if (alignmentType.equals(CharacterStateSetType.AMINO_ACID)) {
				constituentChars = SequenceUtils.oneLetterAminoAcidConstituents(Character.toString(token));
			}

			if (constituentChars.length > 1) {
				constituents = new ArrayList<String>();
				type = CharacterSymbolType.UNCERTAIN;
				for (int i = 0; i < constituentChars.length; i++) {
					constituents.add(Character.toString(constituentChars[i]));
				}
			}
			receiver.doAdd(new SingleTokenDefinitionEvent(getStreamDataProvider().createNewID(ReadWriteConstants.DEFAULT_TOKEN_DEFINITION_ID_PREFIX),
					null, Character.toString(token), CharacterSymbolMeaning.CHARACTER_STATE, type, constituents));
			receiver.doAdd(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.SINGLE_TOKEN_DEFINITION));
		}

		receiver.doAdd(new SingleTokenDefinitionEvent(getStreamDataProvider().createNewID(ReadWriteConstants.DEFAULT_TOKEN_DEFINITION_ID_PREFIX),
				"gap", Character.toString(SequenceUtils.GAP_CHAR), CharacterSymbolMeaning.GAP, CharacterSymbolType.UNCERTAIN, null));
		receiver.doAdd(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.SINGLE_TOKEN_DEFINITION));

		receiver.doAdd(new SingleTokenDefinitionEvent(getStreamDataProvider().createNewID(ReadWriteConstants.DEFAULT_TOKEN_DEFINITION_ID_PREFIX),
				"missing data", Character.toString(SequenceUtils.MISSING_DATA_CHAR), CharacterSymbolMeaning.MISSING, CharacterSymbolType.UNCERTAIN, atomicStates));
		receiver.doAdd(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.SINGLE_TOKEN_DEFINITION));

		if (alignmentType.equals(CharacterStateSetType.AMINO_ACID)) {
			receiver.doAdd(new SingleTokenDefinitionEvent(getStreamDataProvider().createNewID(ReadWriteConstants.DEFAULT_TOKEN_DEFINITION_ID_PREFIX),
					"stop codon", Character.toString(SequenceUtils.STOP_CODON_CHAR), CharacterSymbolMeaning.OTHER, CharacterSymbolType.ATOMIC_STATE, null));
			receiver.doAdd(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.SINGLE_TOKEN_DEFINITION));
		}
	}


	public void addRemainingEvents(CharacterStateSetType type) throws IOException, XMLStreamException {
		switch (type) {
			case NUCLEOTIDE:
			case DNA:
				writeDNATokenDefinitions(receiver);
				break;
			case RNA:
				writeRNATokenDefinitions(receiver);
				break;
			case AMINO_ACID:
				writeAminoAcidTokenDefinitions(receiver);
				break;
			default:
				break;
		}
	}


	@Override
	protected void handleLiteralMetaStart(LiteralMetadataEvent event) throws IOException, XMLStreamException {
		receiver.add(event);
	}


	@Override
	protected void handleLiteralContentMeta(LiteralMetadataContentEvent event) throws IOException, XMLStreamException {
		receiver.add(event);
	}


	@Override
	protected void handleResourceMetaStart(ResourceMetadataEvent event) throws IOException, XMLStreamException {
		receiver.add(event);
	}


	@Override
	protected void handleMetaEndEvent(JPhyloIOEvent event) throws IOException, XMLStreamException {
		receiver.add(event);
	}


	@Override
	protected void handleComment(CommentEvent event) throws IOException, XMLStreamException {
		receiver.add(event);
	}


	@Override
	protected boolean doAdd(JPhyloIOEvent event) throws IOException, XMLStreamException {
		switch (event.getType().getContentType()) {
			case SINGLE_TOKEN_DEFINITION:
				if (event.getType().getTopologyType().equals(EventTopologyType.START)) {
					SingleTokenDefinitionEvent tokenDefinitionEvent = event.asSingleTokenDefinitionEvent();

					if (getStreamDataProvider().getAlignmentType().equals(CharacterStateSetType.AMINO_ACID)) {
						if (tokenDefinitionEvent.getTokenName().length() > 1) {  //only one letter codes can be written to NeXML
							tokens.add(SequenceUtils.oneLetterAminoAcidByThreeLetter(tokenDefinitionEvent.getTokenName()));  //Token must be a valid three letter code which was already checked in NeXMLCollectTokenSetDefinitionDataReceiver
						}
					}
					else {
						tokens.add(tokenDefinitionEvent.getTokenName().charAt(0));
					}
				}
				receiver.doAdd(event);
			default:
				break;
		}
		return true;
	}
}
