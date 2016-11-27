/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015-2016  Ben Stöver, Sarah Wiechers
 * <http://bioinfweb.info/JPhyloIO>
 * 
 * This file is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This file is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package info.bioinfweb.jphyloio.demo.xmlmetadata.standard;


import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.stream.events.StartElement;

import info.bioinfweb.jphyloio.JPhyloIOEventReader;
import info.bioinfweb.jphyloio.demo.xmlmetadata.AbstractApplication;
import info.bioinfweb.jphyloio.demo.xmlmetadata.IOConstants;
import info.bioinfweb.jphyloio.demo.xmlmetadata.RelatedResource;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataContentEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;



public class StandardApplication extends AbstractApplication implements IOConstants {
	@Override
	protected RelatedResource readMetadata(JPhyloIOEventReader reader) throws IOException {
		String title = null;
		String url = null;
		RelatedResource.Type type = null;
		
		JPhyloIOEvent event = reader.next();
		while (reader.hasNextEvent() && !event.getType().getTopologyType().equals(EventTopologyType.END)) {
			if (event.getType().getContentType().equals(EventContentType.LITERAL_META_CONTENT)) { 
				LiteralMetadataContentEvent contentEvent = event.asLiteralMetadataContentEvent();
				if (contentEvent.getXMLEvent().isStartElement()) {
					StartElement startEvent = contentEvent.getXMLEvent().asStartElement();
					if (startEvent.getName().equals(TAG_RELATED_RESOURCE)) {
						
					}
				}
				
				//TODO Read (nested) XML events. (Possibly in multiple loops/methods.)
			}
			event = reader.next();
		}
		
		return null;
	}

	
	@Override
	protected void writeMetadata(File file, String formatID, List<RelatedResource> resources) {
		// TODO Auto-generated method stub
		
	}
}
