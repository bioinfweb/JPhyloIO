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
package info.bioinfweb.jphyloio.formats.nexml;


import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import info.bioinfweb.commons.io.XMLUtils;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.EventContentType;
import info.bioinfweb.jphyloio.events.EventTopologyType;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.MetaInformationEvent;



public abstract class NeXMLTagReader implements NeXMLConstants {
//	private String about; //stores information from the "about"-attribute of NeXML tags with meta subtags (usually an id)
	
	public JPhyloIOEvent readEvent(NeXMLEventReader reader) throws Exception {
		if (reader.getXMLReader().hasNext()) {
			XMLEvent xmlEvent = reader.getXMLReader().nextEvent();
			if (xmlEvent.isEndElement()) {
				if (reader.getEncounteredTags().peek().equals(TAG_NEXML)) {
					reader.getEncounteredTags().pop();
					return new ConcreteJPhyloIOEvent(EventContentType.DOCUMENT, EventTopologyType.END);
				}
				else if (reader.getEncounteredTags().peek().equals(TAG_CHARACTERS)) {
					reader.getEncounteredTags().pop();
					return new ConcreteJPhyloIOEvent(EventContentType.ALIGNMENT, EventTopologyType.END);
				}
				reader.getEncounteredTags().pop();
			}
			else {
				return readEventCore(reader, xmlEvent);
			}
		}
		return null;
	}
	
	
	protected abstract JPhyloIOEvent readEventCore(NeXMLEventReader reader, XMLEvent event) throws Exception;
	
	
	public JPhyloIOEvent readMeta(NeXMLEventReader reader, StartElement element) {		
		String type = XMLUtils.readStringAttr(element, ATTR_TYPE, null);
		String key = null;	
		String stringValue = null;
		Object objectValue = null;
		String dataType = null;
		
		if (type.equals(TYPE_LITERAL_META)) {
			key = XMLUtils.readStringAttr(element, ATTR_PROPERTY, null);
			stringValue = XMLUtils.readStringAttr(element, ATTR_CONTENT, null);
			dataType = XMLUtils.readStringAttr(element, ATTR_DATATYPE, null);
			//TODO Delegate java object construction to ontology definition instance, which is able to convert a QName to a Java class.
		}
		else if (type.equals(TYPE_RESOURCE_META)) {
			key = XMLUtils.readStringAttr(element, ATTR_REL, null);
			stringValue = XMLUtils.readStringAttr(element, ATTR_HREF, null);
			try {
				objectValue = new URL(stringValue);
			} 
			catch (MalformedURLException e) {}
			dataType = type;
		}
		else {}
 		
 		if (stringValue != null && objectValue != null) {
 			return new MetaInformationEvent(key, dataType, stringValue, objectValue);
 		}
 		else if (stringValue != null) {
 			return new MetaInformationEvent(key, dataType, stringValue);
 		}
 		else {  //TODO Possibly throw exception or write to warning log, if necessary NeXML attributes are missing.
 			return null;
 		}
	}


//	public String getAbout() {
//		return about;
//	}


//	public void setAbout(String about) {
//		this.about = about;
//	}
}
