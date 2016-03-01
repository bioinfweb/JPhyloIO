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
package info.bioinfweb.jphyloio.formats.nexus;


import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.dataadapters.JPhyloIOEventReceiver;
import info.bioinfweb.jphyloio.dataadapters.implementations.receivers.CertainStartEventReceiver;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.SingleTokenDefinitionEvent;
import info.bioinfweb.jphyloio.events.TokenSetDefinitionEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;



public class TokenSetEventReceiver extends CertainStartEventReceiver<TokenSetDefinitionEvent> 
		implements JPhyloIOEventReceiver, NexusConstants {

	private StringBuilder singleTokens = new StringBuilder();
	
	
	public TokenSetEventReceiver(Writer writer,	ReadWriteParameterMap parameterMap) {
		super(writer, parameterMap, EventContentType.TOKEN_SET_DEFINITION);
	}

	
	public String getSingleTokens() {
		if (singleTokens.length() > 0) {
			return singleTokens.toString();
		}
		else {
			return null;
		}
	}


	@Override
	public void clear() {
		super.clear();
		singleTokens.delete(0, singleTokens.length());
	}


	@Override
	protected void processStartEvent(TokenSetDefinitionEvent startEvent) throws IOException {
		String dataType;
		switch (startEvent.getSetType()) {
			case DISCRETE:
				dataType = FORMAT_VALUE_STANDARD_DATA_TYPE;
				break;
			case NUCLEOTIDE:
				dataType = FORMAT_VALUE_NUCLEOTIDE_DATA_TYPE;
				break;
			case DNA:
				dataType = FORMAT_VALUE_DNA_DATA_TYPE;
				break;
			case RNA:
				dataType = FORMAT_VALUE_RNA_DATA_TYPE;
				break;
			case AMINO_ACID:
				dataType = FORMAT_VALUE_PROTEIN_DATA_TYPE;
				break;
			case CONTINUOUS:
				dataType = FORMAT_VALUE_CONTINUOUS_DATA_TYPE;
				break;
			default:  // UNKNOWN
				dataType = null;
				break;
		}
		
		if (dataType != null) {
			getWriter().write(' ');
			NexusEventWriter.writeKeyValueExpression(getWriter(), FORMAT_SUBCOMMAND_DATA_TYPE, dataType);
		}
	}
	
	
	private void writeSingleTokenDefinition(String key, SingleTokenDefinitionEvent singleTokenEvent) throws IOException {
		getWriter().write(' ');
		NexusEventWriter.writeKeyValueExpression(getWriter(), key, 
				NexusEventWriter.formatToken(singleTokenEvent.getTokenName()));  //TODO Token names that need to be delimited would anyway not be valid in Nexus. => An according exception should be thrown or the token should be replaced somehow.
	}


	@Override
	protected boolean doAdd(JPhyloIOEvent event) throws IllegalArgumentException,	IOException {
		switch (event.getType().getContentType()) {
			case SINGLE_TOKEN_DEFINITION:
				SingleTokenDefinitionEvent singleTokenEvent = event.asSingleTokenDefinitionEvent();
				switch (singleTokenEvent.getMeaning()) {
					case CHARACTER_STATE:
						if (singleTokens.length() > 0) {
							singleTokens.append(" ");
						}
						singleTokens.append(singleTokenEvent.getTokenName());  // TODO Check token name for invalid characters
						break;
					case GAP:
						writeSingleTokenDefinition(FORMAT_SUBCOMMAND_GAP_CHAR, singleTokenEvent);
						break;
					case MISSING:
						writeSingleTokenDefinition(FORMAT_SUBCOMMAND_MISSING_CHAR, singleTokenEvent);
						break;
					case MATCH:
						writeSingleTokenDefinition(FORMAT_SUBCOMMAND_MATCH_CHAR, singleTokenEvent);
						break;
					default:  // OTHER
						break;  // Nothing to do.
				}
				break;
			case META_INFORMATION:
				if (event.getType().getTopologyType().equals(EventTopologyType.START)) {
					addIgnoredMetadata(1);
				}
				break;
			default:
				break;
		}
		return true;
	}
}
