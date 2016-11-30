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
import info.bioinfweb.jphyloio.JPhyloIOEventWriter;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.ReadWriteParameterNames;
import info.bioinfweb.jphyloio.dataadapters.JPhyloIOEventReceiver;
import info.bioinfweb.jphyloio.demo.xmlmetadata.AbstractApplication;
import info.bioinfweb.jphyloio.demo.xmlmetadata.IOConstants;
import info.bioinfweb.jphyloio.demo.xmlmetadata.RelatedResource;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralContentSequenceType;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.URIOrStringIdentifier;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.formats.xml.JPhyloIOXMLEventReader;
import info.bioinfweb.jphyloio.formats.xml.JPhyloIOXMLEventWriter;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;



public class IteratorApplication extends AbstractApplication implements IOConstants {
	private RelatedResource readRelatedResource(StartElement parentEvent, XMLEventReader xmlReader) 
			throws IOException, XMLStreamException {
		
		RelatedResource result = new RelatedResource();
		result.setType(RelatedResource.Type.valueOf(parentEvent.getAttributeByName(ATTR_TYPE).getValue()));
		
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
		
		XMLEventReader xmlReader = reader.createMetaXMLEventReader();
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
	protected void writeMetadata(ReadWriteParameterMap parameters, JPhyloIOEventReceiver receiver, 
			RelatedResource resource) throws IOException, XMLStreamException {
		
		XMLEventWriter writer = parameters.getObject(ReadWriteParameterNames.KEY_WRITER_INSTANCE, null, JPhyloIOXMLEventWriter.class).
				createMetaXMLEventWriter(receiver);  
				// This will cause a NullPointerException, if the writer does not implement JPhyloIOXMLEventWriter (e.g. writers for text 
				// formats like Nexus). Real-world applications should handle this case. 
		
		XMLEventFactory factory = XMLEventFactory.newInstance();
		writer.add(factory.createNamespace(CUSTOM_XML_NAMESPACE_PREFIX, CUSTOM_XML_NAMESPACE_URI));
		
		String prefix = writer.getNamespaceContext().getPrefix(CUSTOM_XML_NAMESPACE_URI);  //TODO Use this prefix, since it might have been changed.
		//TODO Discuss why automatic namespace collecting was not implemeted/possible here.
		writer.add(factory.createStartElement(TAG_RELATED_RESOURCE, Collections.emptyIterator(), Collections.emptyIterator()));
		if (resource.getType() != null) {
			writer.add(factory.createAttribute(ATTR_TYPE, resource.getType().toString()));
		}
		
		if (resource.getTitle() != null) {
			writer.add(factory.createStartElement(TAG_TITLE, Collections.emptyIterator(), Collections.emptyIterator()));
			writer.add(factory.createCharacters(resource.getTitle()));
			writer.add(factory.createEndElement(TAG_TITLE, Collections.emptyIterator()));
		}
		
		if (resource.getURL() != null) {
			writer.add(factory.createStartElement(TAG_URL, Collections.emptyIterator(), Collections.emptyIterator()));
			writer.add(factory.createCharacters(resource.getURL().toExternalForm()));
			writer.add(factory.createEndElement(TAG_URL, Collections.emptyIterator()));
		}
		
		writer.add(factory.createEndElement(TAG_RELATED_RESOURCE, Collections.emptyIterator()));
	}
	
	
	public static void main(String[] args) {
		new IteratorApplication().run();
	}
}
