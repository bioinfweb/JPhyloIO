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
package info.bioinfweb.jphyloio.demo.xmlmetadata.cursor;


import info.bioinfweb.commons.io.XMLUtils;
import info.bioinfweb.jphyloio.JPhyloIOEventReader;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.ReadWriteParameterNames;
import info.bioinfweb.jphyloio.dataadapters.JPhyloIOEventReceiver;
import info.bioinfweb.jphyloio.demo.xmlmetadata.AbstractApplication;
import info.bioinfweb.jphyloio.demo.xmlmetadata.IOConstants;
import info.bioinfweb.jphyloio.demo.xmlmetadata.RelatedResource;
import info.bioinfweb.jphyloio.formats.xml.JPhyloIOXMLEventReader;
import info.bioinfweb.jphyloio.formats.xml.JPhyloIOXMLEventWriter;

import java.io.IOException;
import java.net.URL;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;



public class CursorApplication extends AbstractApplication implements IOConstants {
	public CursorApplication() {
		super("cursor");
	}


	private RelatedResource readRelatedResource(XMLStreamReader xmlReader) throws IOException, XMLStreamException {
		RelatedResource result = new RelatedResource();
		result.setType(RelatedResource.Type.valueOf(
				xmlReader.getAttributeValue(ATTR_TYPE.getNamespaceURI(), ATTR_TYPE.getLocalPart())));
		
    while (xmlReader.next() != XMLStreamConstants.END_ELEMENT) {
      if (xmlReader.getEventType() == XMLStreamConstants.START_ELEMENT) {
      	if (xmlReader.getName().equals(TAG_TITLE)) {
      		xmlReader.next();
      		result.setTitle(XMLUtils.readCharactersAsString(xmlReader));
      	}
      	else if (xmlReader.getName().equals(TAG_URL)) {
      		xmlReader.next();
      		result.setURL(new URL(XMLUtils.readCharactersAsString(xmlReader)));
        }
      	else {
      		XMLUtils.reachElementEnd(xmlReader);
      	}
      }
    }
		
		return result;
	}
	
	
	@Override
	protected RelatedResource readMetadata(JPhyloIOEventReader reader) throws IOException, XMLStreamException {
		RelatedResource result = null;
		
		if (reader instanceof JPhyloIOXMLEventReader) {
  		XMLStreamReader xmlReader = ((JPhyloIOXMLEventReader)reader).createMetaXMLStreamReader();
    	while (xmlReader.hasNext()) {
    		if (xmlReader.next() == XMLStreamConstants.START_ELEMENT) {
    			if (xmlReader.getName().equals(TAG_RELATED_RESOURCE)) {
          	result = readRelatedResource(xmlReader);
    			}
    			else {
    				XMLUtils.reachElementEnd(xmlReader);
    			}
    		}
    	}
		}
		
		return result;
	}

	
	@Override
	protected void writeMetadata(ReadWriteParameterMap parameters, JPhyloIOEventReceiver receiver, 
			RelatedResource resource) throws IOException, XMLStreamException {
		
		if (parameters.get(ReadWriteParameterNames.KEY_WRITER_INSTANCE) instanceof JPhyloIOXMLEventWriter) {  // XML metadata can only be written to XML formats.
  		XMLStreamWriter writer = parameters.getObject(ReadWriteParameterNames.KEY_WRITER_INSTANCE, null, 
  				JPhyloIOXMLEventWriter.class).createMetaXMLStreamWriter(receiver);  
  
  		writer.writeStartElement(TAG_RELATED_RESOURCE.getPrefix(), TAG_RELATED_RESOURCE.getLocalPart(), TAG_RELATED_RESOURCE.getNamespaceURI());
  		if (resource.getType() != null) {
  			writer.writeAttribute(ATTR_TYPE.getPrefix(), ATTR_TYPE.getNamespaceURI(), ATTR_TYPE.getLocalPart(), resource.getType().toString());
  		}
  		
  		if (resource.getTitle() != null) {
  			writer.writeStartElement(TAG_TITLE.getPrefix(), TAG_TITLE.getLocalPart(), TAG_TITLE.getNamespaceURI());
  			writer.writeCharacters(resource.getTitle());
  			writer.writeEndElement();
  		}
  		
  		if (resource.getURL() != null) {
  			writer.writeStartElement(TAG_URL.getPrefix(), TAG_URL.getLocalPart(), TAG_URL.getNamespaceURI());
  			writer.writeCharacters(resource.getURL().toExternalForm());
  			writer.writeEndElement();
  		}
  		
  		writer.writeEndElement();
		}
	}
	
	
	public static void main(String[] args) {
		new CursorApplication().run();
	}
}
