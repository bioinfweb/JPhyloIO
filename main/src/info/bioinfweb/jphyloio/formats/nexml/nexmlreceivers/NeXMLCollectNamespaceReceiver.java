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

import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.formats.nexml.NeXMLWriterStreamDataProvider;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;



public class NeXMLCollectNamespaceReceiver extends AbstractNeXMLDataReceiver {
	

	public NeXMLCollectNamespaceReceiver(XMLStreamWriter writer, ReadWriteParameterMap parameterMap,
			NeXMLWriterStreamDataProvider streamDataProvider) {
		super(writer, parameterMap, streamDataProvider);
	}

	
	@Override
	protected boolean doAdd(JPhyloIOEvent event) throws IOException, XMLStreamException {
		switch (event.getType().getContentType()) {
			case META_RESOURCE:
				if (event.getType().getTopologyType().equals(EventTopologyType.START)) {
					getStreamDataProvider().getMetaDataNameSpaces().add(event.asResourceMetadataEvent().getRel().getNamespaceURI());
				}				
				break;
			case META_LITERAL:
				if (event.getType().getTopologyType().equals(EventTopologyType.START)) {
					getStreamDataProvider().getMetaDataNameSpaces().add(event.asLiteralMetadataEvent().getPredicate().getNamespaceURI());
				}
				break;
			default:
				break;
		}
		return true;
	}
}
