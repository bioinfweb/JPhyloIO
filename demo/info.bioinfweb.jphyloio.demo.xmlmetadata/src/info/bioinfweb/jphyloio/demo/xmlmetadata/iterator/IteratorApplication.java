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
package info.bioinfweb.jphyloio.demo.xmlmetadata.iterator;


import info.bioinfweb.commons.io.XMLUtils;
import info.bioinfweb.jphyloio.demo.xmlmetadata.AbstractApplication;
import info.bioinfweb.jphyloio.demo.xmlmetadata.IOConstants;
import info.bioinfweb.jphyloio.demo.xmlmetadata.RelatedResource;
import info.bioinfweb.jphyloio.formats.xml.JPhyloIOXMLEventReader;
import info.bioinfweb.jphyloio.formats.xml.MetaXMLEventReader;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;



public class IteratorApplication extends AbstractApplication implements IOConstants {
	private RelatedResource readRelatedResource(StartElement parentEvent, MetaXMLEventReader xmlReader) 
			throws IOException, XMLStreamException {
		
		RelatedResource result = new RelatedResource();
		result.
		setType(RelatedResource.Type.valueOf(
				parentEvent.
				getAttributeByName(ATTR_TYPE).
				getValue()));
		
    XMLEvent event = xmlReader.nextEvent();
    while (event.getEventType() != XMLStreamConstants.END_ELEMENT) {
      if (event.getEventType() == XMLStreamConstants.START_ELEMENT) {
      	StartElement element = event.asStartElement();
      	if (element.getName().equals(TAG_TITLE)) {
      		result.setTitle(XMLUtils.readCharactersAsString(xmlReader));
      	}
      	else if (element.getName().equals(TAG_URL)) {
      		result.setURL(new URL(XMLUtils.readCharactersAsString(xmlReader)));
        }
       	XMLUtils.reachElementEnd(xmlReader);
      }
      event = xmlReader.nextEvent();
    }
		
		return result;
	}
	
	
	@Override
	protected RelatedResource readMetadata(JPhyloIOXMLEventReader reader) throws IOException, XMLStreamException {
		RelatedResource result = null;
		
		MetaXMLEventReader xmlReader = reader.createMetaXMLEventReader();
		XMLEvent event;
		while (xmlReader.hasNext()) {
      event = xmlReader.nextEvent();
      if (event.isStartElement()) {
      	StartElement element = event.asStartElement();
        if (element.getName().equals(TAG_RELATED_RESOURCE)) {
        	result = readRelatedResource(element, xmlReader);
        }
        else {
        	XMLUtils.reachElementEnd(xmlReader);
        }
      }
    }
		
		return result;
	}

	
	@Override
	protected void writeMetadata(File file, String formatID, List<RelatedResource> resources) {
		// TODO Auto-generated method stub
		
	}
	
	
	public static void main(String[] args) {
		new IteratorApplication().run();
	}
}
