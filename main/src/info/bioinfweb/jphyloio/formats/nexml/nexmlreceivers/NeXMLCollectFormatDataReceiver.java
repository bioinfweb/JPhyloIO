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


import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.formats.nexml.DocumentInformation;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;



public class NeXMLCollectFormatDataReceiver extends NeXMLCollectDocumentDataReceiver {
	
	
	public NeXMLCollectFormatDataReceiver(XMLStreamWriter writer, ReadWriteParameterMap parameterMap,
			DocumentInformation documentInformation) {
		super(writer, parameterMap, documentInformation);
	}

	
	@Override
	protected boolean doAdd(JPhyloIOEvent event) throws IOException, XMLStreamException {
		switch (event.getType().getContentType()) {
			case TOKEN_SET_DEFINITION:
				if (event.getType().getTopologyType().equals(EventTopologyType.START)) {					
					getDocumentInformation().setAlignmentType(event.asTokenSetDefinitionEvent().getSetType()); //Woher bekomme ich diese Info wenn getSetType() == null?
				}
				break;
			case SINGLE_TOKEN_DEFINITION:
				if (event.getType().getTopologyType().equals(EventTopologyType.START)) {
					//TODO check here if all definitions for DNA, RNA and AA are according to the IUPAC standard
				}
				break;
			default:
				break;
		}
		return true;
	}	
}
