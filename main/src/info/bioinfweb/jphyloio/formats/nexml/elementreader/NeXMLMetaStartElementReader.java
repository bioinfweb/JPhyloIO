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
package info.bioinfweb.jphyloio.formats.nexml.elementreader;


import info.bioinfweb.commons.io.W3CXSConstants;
import info.bioinfweb.commons.io.XMLUtils;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralContentSequenceType;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataContentEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.ResourceMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.URIOrStringIdentifier;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.exception.JPhyloIOReaderException;
import info.bioinfweb.jphyloio.formats.nexml.NeXMLReaderStreamDataProvider;
import info.bioinfweb.jphyloio.formats.nexml.elementreader.AbstractNeXMLElementReader.LabeledIDEventInformation;
import info.bioinfweb.jphyloio.objecttranslation.InvalidObjectSourceDataException;
import info.bioinfweb.jphyloio.objecttranslation.ObjectTranslator;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;



public class NeXMLMetaStartElementReader extends AbstractNeXMLElementReader {
	@Override
	public void readEvent(NeXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException,
			XMLStreamException {
		StartElement element = event.asStartElement();
  	LabeledIDEventInformation info = getLabeledIDEventInformation(streamDataProvider, element);
  	QName type = streamDataProvider.getEventReader().qNameFromCURIE(XMLUtils.readStringAttr(element, ATTR_XSI_TYPE, null), element);	    	
  	URIOrStringIdentifier predicate;	    	
			
  	if ((info.id != null) && !info.id.equals("")) {
  		if (type.getLocalPart().equals(TYPE_LITERAL_META)) {
  			streamDataProvider.getMetaType().push(EventContentType.META_LITERAL);
  			
  			predicate = new URIOrStringIdentifier(null, 
  					streamDataProvider.getEventReader().qNameFromCURIE(XMLUtils.readStringAttr(element, ATTR_PROPERTY, null), element));
  			QName datatype = streamDataProvider.getEventReader().qNameFromCURIE(XMLUtils.readStringAttr(element, ATTR_DATATYPE, null), element);		  			
  			String content = XMLUtils.readStringAttr(element, ATTR_CONTENT, null);
  			
  			streamDataProvider.setNestedMetaType(datatype);
  			streamDataProvider.setAlternativeStringRepresentation(content);		  			
  			
  			LiteralContentSequenceType contentType = LiteralContentSequenceType.XML;
  			
  			ObjectTranslator<?> translator = streamDataProvider.getParameters().getObjectTranslatorFactory().getDefaultTranslator(datatype);
  			if (translator != null) {
  				contentType = LiteralContentSequenceType.SIMPLE;
  			}
  			
  			streamDataProvider.getCurrentEventCollection().add(new LiteralMetadataEvent(info.id, info.label, predicate, contentType));
  			
  			if ((streamDataProvider.getXMLReader().peek().getEventType() == XMLStreamConstants.END_ELEMENT) && (content != null)) { //if no character data or custom XML is nested under this literal meta event the value of the content-attribute is used to create a LiteralMetadataContentEvent
  				Object objectValue;
  				if (translator != null) {
	  				try {
							objectValue = translator.representationToJava(content, streamDataProvider);
							streamDataProvider.getCurrentEventCollection().add(new LiteralMetadataContentEvent(new URIOrStringIdentifier(null, datatype), content, objectValue, null));
						}
						catch (InvalidObjectSourceDataException e) {
							throw new JPhyloIOReaderException("The content of this meta tag could not be parsed to class " + translator.getObjectClass().getSimpleName() + ".", event.getLocation());
						}
  				}
  				else {
  					streamDataProvider.getCurrentEventCollection().add(new LiteralMetadataContentEvent(new URIOrStringIdentifier(null, datatype), content, false));
  				}
  			}
  			
  			if (!datatype.equals(W3CXSConstants.DATA_TYPE_TOKEN) && !datatype.equals(W3CXSConstants.DATA_TYPE_STRING) && (translator != null)) {
  				Object objectValue = null;
  				String nestedContent = streamDataProvider.getXMLReader().getElementText();
					
					if (nestedContent != null) {
						try {
							objectValue = translator.representationToJava(nestedContent, streamDataProvider);
							streamDataProvider.getCurrentEventCollection().add(new LiteralMetadataContentEvent(new URIOrStringIdentifier(null, datatype), nestedContent, objectValue, null));
							streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL)); //getElementText() already consumed the literal meta end event
						}
						catch (InvalidObjectSourceDataException e) {
							throw new JPhyloIOReaderException("The nested content of this meta tag could not be parsed to class " + translator.getObjectClass().getSimpleName() + ".", event.getLocation());
						}
					}
  			}
  			//TODO Create XMLStreamReader instance here, if nested XML is present.
  		}
  		else if (type.getLocalPart().equals(TYPE_RESOURCE_META)) {
  			streamDataProvider.getMetaType().push(EventContentType.META_RESOURCE);
  			predicate = new URIOrStringIdentifier(null, 
  					streamDataProvider.getEventReader().qNameFromCURIE(XMLUtils.readStringAttr(element, ATTR_REL, null), element));
  			String about = XMLUtils.readStringAttr(element, ATTR_ABOUT, null);
  			String uri = XMLUtils.readStringAttr(element, ATTR_HREF, null);
  			URI href = null;
  			try {
  				href = new URI(uri);
  			}
  			catch (URISyntaxException e) {
  				throw new JPhyloIOReaderException("An \"href\"-attribute element must specify a valid URI. Instead the string\"" + uri + "\" was given.", event.getLocation());
  			}
  			
  			streamDataProvider.getCurrentEventCollection().add(new ResourceMetadataEvent(info.id, info.label, predicate, href, about));	  			
  		}
  		else {
  			throw new JPhyloIOReaderException("Meta annotations can only be of type \"" + TYPE_LITERAL_META + "\" or \"" + 
  					TYPE_RESOURCE_META + "\".", element.getLocation());
  		}
  	}
  	else {
			throw new JPhyloIOReaderException("NeXML meta elements must specifiy an ID.", element.getLocation());
		}		
	}
}
