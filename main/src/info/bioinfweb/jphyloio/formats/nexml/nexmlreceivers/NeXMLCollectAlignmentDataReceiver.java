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


import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import info.bioinfweb.commons.bio.CharacterStateSetType;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.exception.JPhyloIOWriterException;
import info.bioinfweb.jphyloio.formats.nexml.DocumentInformation;



public class NeXMLCollectAlignmentDataReceiver extends NeXMLCollectDocumentDataReceiver {
	
	
	public NeXMLCollectAlignmentDataReceiver(XMLStreamWriter writer, ReadWriteParameterMap parameterMap,
			DocumentInformation documentInformation) {
		super(writer, parameterMap, documentInformation);
	}
	

	@Override
	protected boolean doAdd(JPhyloIOEvent event) throws IOException, XMLStreamException {
		switch (event.getType().getContentType()) {
			case SINGLE_SEQUENCE_TOKEN:
				if (event.getType().getTopologyType().equals(EventTopologyType.START)) {
					if (event.asSingleSequenceTokenEvent().getLabel() != null) { //TODO also check for nested meta
						getDocumentInformation().setWriteCellsTags(true);
					}
				}
				break;
			case SEQUENCE:
				if (event.getType().getTopologyType().equals(EventTopologyType.START)) {
					if (event.asLinkedOTUOrOTUsEvent().getOTUOrOTUsID().isEmpty()) {
						getDocumentInformation().setWriteUndefinedOTU(true);
					}
				}
				break;
			case SEQUENCE_TOKENS:
				List<String> tokens = event.asSequenceTokensEvent().getCharacterValues();
				if (getDocumentInformation().getAlignmentType().equals(CharacterStateSetType.DISCRETE)) {
					//TODO check for previously undefined states
				}
				else if (getDocumentInformation().getAlignmentType().equals(CharacterStateSetType.CONTINUOUS)) {
					for (String token : tokens) {
						try {
							Double.parseDouble(token);
						}
						catch (NumberFormatException e) {
							throw new JPhyloIOWriterException(""); //TODO give exception message
						}
					}
				}
				break;
			default:
				break;
		}
		return true;
	}
	
}
