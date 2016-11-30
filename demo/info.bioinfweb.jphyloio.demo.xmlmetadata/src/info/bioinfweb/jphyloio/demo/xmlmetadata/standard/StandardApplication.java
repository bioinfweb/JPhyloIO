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


import java.io.IOException;

import javax.xml.stream.events.StartElement;

import info.bioinfweb.jphyloio.JPhyloIOEventReader;
import info.bioinfweb.jphyloio.JPhyloIOEventWriter;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.dataadapters.JPhyloIOEventReceiver;
import info.bioinfweb.jphyloio.demo.xmlmetadata.AbstractApplication;
import info.bioinfweb.jphyloio.demo.xmlmetadata.IOConstants;
import info.bioinfweb.jphyloio.demo.xmlmetadata.RelatedResource;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataContentEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.formats.xml.JPhyloIOXMLEventReader;



public class StandardApplication extends AbstractApplication implements IOConstants {
	private RelatedResource readRelatedResource(StartElement parentEvent, JPhyloIOEventReader reader) throws IOException {
		RelatedResource result = new RelatedResource();
		result.setType(RelatedResource.Type.valueOf(parentEvent.getAttributeByName(ATTR_TYPE).getValue()));
		
		JPhyloIOEvent event = reader.next();
		while (reader.hasNextEvent() && !event.getType().getContentType().equals(EventContentType.LITERAL_META_CONTENT)) {
			//TODO Are comments producing comment events or content events as well? (JPhyloIO Comment events would possibly have to be handled here.)
			
			LiteralMetadataContentEvent contentEvent = event.asLiteralMetadataContentEvent();
			if (contentEvent.getXMLEvent().isStartElement()) {
				StartElement startEvent = contentEvent.getXMLEvent().asStartElement();
				if (startEvent.getName().equals(TAG_TITLE)) {
					//result.setTitle(title);  //TODO Use tool method to read title
				}
				else if (startEvent.getName().equals(TAG_URL)) {
					//result.setURL(url);  //TODO Use tool method to read url
				}
				else {
					//TODO Consume other tag and subelements (with new tool method).
				}
			}
			event = reader.next();
		}
		
		return result;
	}
	
	
	@Override
	protected RelatedResource readMetadata(JPhyloIOXMLEventReader reader) throws IOException {
		RelatedResource result = null;
		
		JPhyloIOEvent event = reader.next();
		while (reader.hasNextEvent() && !event.getType().getTopologyType().equals(EventTopologyType.END)) {
			if (event.getType().getContentType().equals(EventContentType.LITERAL_META_CONTENT)) { 
				LiteralMetadataContentEvent contentEvent = event.asLiteralMetadataContentEvent();
				if (contentEvent.getXMLEvent().isStartElement()) {
					StartElement startEvent = contentEvent.getXMLEvent().asStartElement();
					if (startEvent.getName().equals(TAG_RELATED_RESOURCE)) {
						result = readRelatedResource(startEvent, reader);
					}
					else {
						//TODO Consume other tag and subelements (with new tool method).
					}
				}
			}
			event = reader.next();
		}
		
		return result;
	}

	
	@Override
	protected void writeMetadata(ReadWriteParameterMap parameters, JPhyloIOEventReceiver receiver, RelatedResource resource) {
		// TODO Auto-generated method stub
		
	}
}
