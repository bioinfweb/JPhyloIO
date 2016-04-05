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
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.SingleTokenDefinitionEvent;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.formats.nexml.NeXMLWriterStreamDataProvider;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;



public class NeXMLCollectTokenSetDefinitionDataReceiver extends NeXMLCollectNamespaceReceiver {
	private static final String DNA_TOKEN = "ABCDGHKMNRSTVWXY-?";
	private static final String RNA_TOKEN = "-?ABCDGHKMNRSUVWXY";
	private static final String AA_TOKEN = "*-?ABCDEFGHIKLMNPQRSTUVWXYZ";

	
	public NeXMLCollectTokenSetDefinitionDataReceiver(XMLStreamWriter writer,
			ReadWriteParameterMap parameterMap, NeXMLWriterStreamDataProvider streamDataProvider) {
		super(writer, parameterMap, streamDataProvider);
	}
	

	@Override
	protected boolean doAdd(JPhyloIOEvent event) throws IOException, XMLStreamException {
		if (event.getType().getTopologyType().equals(EventTopologyType.START)) {
			switch (event.getType().getContentType()) {
				case SINGLE_TOKEN_DEFINITION:
						SingleTokenDefinitionEvent tokenDefinitionEvent = event.asSingleTokenDefinitionEvent();
						
						if (!tokenDefinitionEvent.getMeaning().equals(CharacterSymbolMeaning.MATCH)) {
							String tokenName = tokenDefinitionEvent.getTokenName();
							String tokenSymbol = null;							
							switch (getStreamDataProvider().getEventWriter().getParameters().getTranslateTokens()) {
								case NEVER:
									tokenSymbol = tokenName;
									break;
								case SYMBOL_TO_LABEL:
									if (tokenDefinitionEvent.getLabel() != null) {
										tokenSymbol = tokenDefinitionEvent.getLabel();
										break;
									}
								case SYMBOL_TO_ID:
									tokenSymbol = tokenDefinitionEvent.getID();
									break;
							}
							
							switch (getStreamDataProvider().getAlignmentType()) {							
								case DNA:
									if (!((tokenName.length() == 1) && DNA_TOKEN.contains(tokenName))) {
										getStreamDataProvider().setAlignmentType(CharacterStateSetType.DISCRETE);
										getStreamDataProvider().getTokenTranslationMap().put(tokenSymbol, tokenName);
									}
									break;
								case RNA:
									if (!((tokenName.length() == 1) && RNA_TOKEN.contains(tokenName))) {
										getStreamDataProvider().setAlignmentType(CharacterStateSetType.DISCRETE);
										getStreamDataProvider().getTokenTranslationMap().put(tokenSymbol, tokenName);
									}
									break;
								case NUCLEOTIDE:
									if (!((tokenName.length() == 1) && (RNA_TOKEN.contains(tokenName) || DNA_TOKEN.contains(tokenName)))) {
										getStreamDataProvider().setAlignmentType(CharacterStateSetType.DISCRETE);
										getStreamDataProvider().getTokenTranslationMap().put(tokenSymbol, tokenName);
									}
									break;
								case AMINO_ACID:
									if (!((tokenName.length() == 1) && AA_TOKEN.contains(tokenName))) {
										getStreamDataProvider().setAlignmentType(CharacterStateSetType.DISCRETE);
										getStreamDataProvider().getTokenTranslationMap().put(tokenSymbol, tokenName);
									}
									break;
								case DISCRETE:
									getStreamDataProvider().getTokenTranslationMap().put(tokenSymbol, tokenName);
									break;							
								default:
									break;
							}
						}
					break;
				default:
					break;
			}
		}
		return true;
	}
}
