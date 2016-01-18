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
import info.bioinfweb.jphyloio.events.LinkedOTUEvent;
import info.bioinfweb.jphyloio.events.MetaInformationEvent;
import info.bioinfweb.jphyloio.events.PartEndEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;



public abstract class NeXMLTagReader implements NeXMLConstants {
	protected static class OTUEventInformation {
		public String id;
		public String label;	
		public String otuID;
	}
	
	
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
				else if (reader.getEncounteredTags().peek().equals(TAG_ROW)) {
					reader.getUpcomingEvents().add(new PartEndEvent(EventContentType.SEQUENCE, true));
				}
				else if (reader.getEncounteredTags().peek().equals(TAG_TREE)) {
					reader.getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.TREE, EventTopologyType.END));
				}
				else if (reader.getEncounteredTags().peek().equals(TAG_NETWORK)) {
					reader.getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.NETWORK, EventTopologyType.END));
				}
				else if (reader.getEncounteredTags().peek().equals(TAG_OTU)) {
					reader.getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.OTU, EventTopologyType.END));
				}
				else if (reader.getEncounteredTags().peek().equals(TAG_OTUS)) {
					reader.getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.OTU_LIST, EventTopologyType.END));
				}
				reader.getEncounteredTags().pop();
			}
			else {
				readEventCore(reader, xmlEvent);
			}
		}
	}
	
	
	protected abstract void readEventCore(NeXMLEventReader reader, XMLEvent event) throws Exception;
	
	
	protected void readMeta(NeXMLEventReader reader, StartElement element) {		
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
		OTUEventInformation info = getOTUEventInformation(reader, element);
		reader.getUpcomingEvents().add(new LinkedOTUEvent(EventContentType.NODE, info.id,	info.label, info.otuID));
		reader.getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.NODE, EventTopologyType.END));
	}
	
	
	public void readEdge(NeXMLEventReader reader, StartElement element) {
		String sourceID = null;
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
		
		if (element.getName().equals(TAG_EDGE)) {
			sourceID = XMLUtils.readStringAttr(element, ATTR_SOURCE, null);
		}
		reader.getUpcomingEvents().add(new EdgeEvent(XMLUtils.readStringAttr(element, ATTR_ID, null), 
				XMLUtils.readStringAttr(element, ATTR_LABEL, null), sourceID, XMLUtils.readStringAttr(element, ATTR_TARGET, null), 
				length));  // Throws a NullPointerException, if any needed parameter is null. //TODO Throw special parse exception instead 
		reader.readID(reader, element);
		reader.getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.EDGE, EventTopologyType.END));
	}
	
	
	protected OTUEventInformation getOTUEventInformation(NeXMLEventReader reader, StartElement element) {
		OTUEventInformation otuEventInformation = new OTUEventInformation();
		otuEventInformation.id = XMLUtils.readStringAttr(element, ATTR_ID, null);
		otuEventInformation.label = XMLUtils.readStringAttr(element, ATTR_LABEL, null);
		otuEventInformation.otuID = XMLUtils.readStringAttr(element, ATTR_OTU, null);
		if ((otuEventInformation.label == null) && (otuEventInformation.otuID != null)) {
			otuEventInformation.label = reader.getIDToLabelMap().get(otuEventInformation.otuID);
		}
		if (otuEventInformation.label == null) {
			otuEventInformation.label = otuEventInformation.id;	
		}
		return otuEventInformation;
	}
}
