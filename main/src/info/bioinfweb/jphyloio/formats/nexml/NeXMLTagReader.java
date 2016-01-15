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
import info.bioinfweb.jphyloio.events.EdgeEvent;
import info.bioinfweb.jphyloio.events.MetaInformationEvent;
import info.bioinfweb.jphyloio.events.NodeEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;



public abstract class NeXMLTagReader implements NeXMLConstants {	
	public void readEvent(NeXMLEventReader reader) throws Exception {
		if (reader.getXMLReader().hasNext()) {
			XMLEvent xmlEvent = reader.getXMLReader().nextEvent();
			if (xmlEvent.isEndElement()) {
				if (reader.getEncounteredTags().peek().equals(TAG_NEXML)) {
					reader.getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.DOCUMENT, EventTopologyType.END));
				}
				else if (reader.getEncounteredTags().peek().equals(TAG_CHARACTERS)) {
					reader.getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.ALIGNMENT, EventTopologyType.END));
				}
				reader.getEncounteredTags().pop();
			}
			else {
				readEventCore(reader, xmlEvent);
			}
		}
	}
	
	
	protected abstract void readEventCore(NeXMLEventReader reader, XMLEvent event) throws Exception;
	
	
	public void readMeta(NeXMLEventReader reader, StartElement element) {		
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
		else {} //TODO Possibly throw exception or write to warning log, if invalid types are encountered
 		
 		if (stringValue != null && objectValue != null) {
 			reader.getUpcomingEvents().add(new MetaInformationEvent(key, dataType, stringValue, objectValue));
 		}
 		else if (stringValue != null) {
 			reader.getUpcomingEvents().add(new MetaInformationEvent(key, dataType, stringValue));
 		}
 		//TODO Possibly throw exception or write to warning log, if necessary NeXML attributes are missing. 
 		
 		reader.readID(reader, element);
		
		reader.getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.META_INFORMATION, EventTopologyType.END));
	}
	
	
	public void readNode(NeXMLEventReader reader, StartElement element) {
		String nodeID = XMLUtils.readStringAttr(element, ATTR_ID, null);
		String label = XMLUtils.readStringAttr(element, ATTR_LABEL, null);
		String otuID = XMLUtils.readStringAttr(element, ATTR_OTU, null);
		if (nodeID != null) {
			reader.getUpcomingEvents().add(new NodeEvent(label, otuID, nodeID));
			reader.getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.NODE, EventTopologyType.END));
		}
	}
	
	
	public void readEdge(NeXMLEventReader reader, StartElement element) {
		String sourceID = null;
		String targetID = XMLUtils.readStringAttr(element, ATTR_TARGET, null);	      		
		double length = Double.NaN;	 
		
		if (reader.getCurrentBranchLengthsFormat().equals(TYPE_FLOAT_TREE)) { 
			length = XMLUtils.readDoubleAttr(element, ATTR_LENGTH, Double.NaN); //TODO possibly check if value is really a double and throw exception if not?
		}
		else { //Type IntTree
			try {
				length = Integer.parseInt(element.getAttributeByName(ATTR_LENGTH).getValue());
			}
			catch (NumberFormatException e) {
				throw new NumberFormatException("The branch length in an IntTree must be an Integer.");
			}
			catch (NullPointerException e) {}
		}
		
		if (targetID != null) {
			if (element.getName().equals(TAG_EDGE)) {
				sourceID = XMLUtils.readStringAttr(element, ATTR_SOURCE, null);
			}
			reader.getUpcomingEvents().add(new EdgeEvent(sourceID, targetID, length));
			reader.getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.EDGE, EventTopologyType.END));
		}
	}
}
