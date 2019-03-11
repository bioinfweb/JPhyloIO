/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015-2019  Ben Stöver, Sarah Wiechers
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
package info.bioinfweb.jphyloio.demo.xmlmetadata;


import info.bioinfweb.commons.io.XMLUtils;
import info.bioinfweb.jphyloio.JPhyloIOEventReader;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.ReadWriteParameterNames;
import info.bioinfweb.jphyloio.dataadapters.JPhyloIOEventReceiver;
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



/**
 * This is the main class of one of two example applications contained in this demo project. It inherits from
 * {@link AbstractApplication} and implements the abstract methods {@link #readMetadata(JPhyloIOEventReader)} and
 * {@link #writeMetadata(ReadWriteParameterMap, JPhyloIOEventReceiver, RelatedResource)}, which are used to read and write
 * an <i>XML</i> representation of the contents of a {@link RelatedResource} instance. This class does this using
 * the iterator-based <i>StAX</i> approach with {@link XMLEventReader} and {@link XMLEventWriter}, while
 * {@link CursorApplication} uses the cursor-based approach.
 * 
 * @author Ben St&ouml;ver
 * @see AbstractApplication
 * @see IteratorApplication
 */
public class IteratorApplication extends AbstractApplication implements IOConstants {
	public IteratorApplication() {
		super("iterator");
	}


	/**
	 * Used internally by {@link #readMetadata(JPhyloIOEventReader)}.
	 */
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
	
	
	/**
	 * Reads the <i>XML</i> representation of an instance of {@link RelatedResource} using an {@link XMLEventReader} obtained
	 * from the <i>JPhyloIO</i> writer.
	 */
	@Override
	protected RelatedResource readMetadata(JPhyloIOEventReader reader) throws IOException, XMLStreamException {
		RelatedResource result = null;
		
		if (reader instanceof JPhyloIOXMLEventReader) {
  		XMLEventReader xmlReader = ((JPhyloIOXMLEventReader)reader).createMetaXMLEventReader();
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
		}
		
		return result;
	}

	
	/**
	 * Writes the <i>XML</i> representation of an instance of {@link RelatedResource} using an {@link XMLEventWriter} obtained
	 * from the <i>JPhyloIO</i> writer.
	 */
	@Override
	protected void writeMetadata(ReadWriteParameterMap parameters, JPhyloIOEventReceiver receiver, 
			RelatedResource resource) throws IOException, XMLStreamException {
		
		if (parameters.get(ReadWriteParameterNames.KEY_WRITER_INSTANCE) instanceof JPhyloIOXMLEventWriter) {  // XML metadata can only be written to XML formats.
  		XMLEventWriter writer = parameters.getObject(ReadWriteParameterNames.KEY_WRITER_INSTANCE, null, 
  				JPhyloIOXMLEventWriter.class).createMetaXMLEventWriter(receiver);  
  		XMLEventFactory factory = XMLEventFactory.newInstance();
  		
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
	}
	
	
	public static void main(String[] args) {
		new IteratorApplication().run();
	}
}
