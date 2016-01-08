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


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.EventContentType;
import info.bioinfweb.jphyloio.events.EventTopologyType;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.MetaEventParameterMap;
import info.bioinfweb.jphyloio.events.MetaInformationEvent;



public abstract class NeXMLTagReader implements NeXMLConstants {
	private String about;
	
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
	
	
	public JPhyloIOEvent readMeta(NeXMLEventReader reader, StartElement element, String annotatedBlock) {
 		Iterator<Attribute> attributes = element.getAttributes();
 		String id = null;
		MetaEventParameterMap parameters = new MetaEventParameterMap();
		parameters.put(MetaEventParameterMap.KEY_ANNOTATED_BLOCK, annotatedBlock);
		
 		while (attributes.hasNext()) {      			
 			Attribute attribute = attributes.next();
 			if (attribute.getName().equals(ATTR_ID)) {
 				id = attribute.getValue();
 			}
 			else {
 				parameters.put(attribute.getName().getLocalPart(), attribute.getValue());
 			}
 		}
 		
 		if (id != null) {
 			return new MetaInformationEvent(id, "null"); //TODO give a correct content value
 		}
 		else {
 			return null;
 		}
	}
	
	
	public Map<String, String> readAttributes(NeXMLEventReader reader, StartElement element) {
		Map<String, String> attributeMap = new HashMap<String, String>();
		Iterator<Attribute> attributes = element.getAttributes();
		while (attributes.hasNext()) {      			
			Attribute attribute = attributes.next();
			attributeMap.put(attribute.getName().getLocalPart(), attribute.getValue());			
		}
		return attributeMap;
	}


	public String getAbout() {
		return about;
	}


	public void setAbout(String about) {
		this.about = about;
	}
}
