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
package info.bioinfweb.jphyloio.events.meta;


import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;

import javax.xml.stream.events.XMLEvent;



/**
 * Instances of this class act as a wrapper for {@link XMLEvent}s and are nested within 
 * {@link EventContentType#META_INFORMATION}, if the content of this metainformation is
 * XML.  
 * 
 * @author Ben St&ouml;ver
 */
public class LiteralMetadataContentEvent extends ConcreteJPhyloIOEvent {
	private String stringValue;
	private Object objectValue;
	private String originalType;

	public LiteralMetadataContentEvent(String originalType, String stringValue) {
		this(originalType, stringValue, stringValue);
	}
	
	
	/**
	 * Creates a new instance of MetaInformationEvent.
	 * 
	 * @param originalType The original type of meta information or null. 
	 * @param stringValue The string value of the meta information.
	 * @param objectValue The object value of the meta information.
	 * 
	 * @return a new instance of MetaInformationEvent
	 */
	public LiteralMetadataContentEvent(String originalType, String stringValue, Object objectValue) {
		super(EventContentType.META_LITERAL_CONTENT, EventTopologyType.SOLE);
		
		this.stringValue = stringValue;  //TODO Should an NPE be thrown if both values are null?
		this.objectValue = objectValue;
		this.originalType = originalType;
	}
	

	public LiteralMetadataContentEvent(XMLEvent xmlEvent) {
		super(EventContentType.META_LITERAL_CONTENT, EventTopologyType.SOLE);
		
		this.stringValue = null;
		this.objectValue = xmlEvent;
		this.originalType = null;  //TODO Should something else be stored here (e.g. the XML event type)?
	}
	

	/**
	 * Returns the string value of the meta information.
	 * 
	 * @return the string representation of the meta information or {@code null} if this metaevent carries no value (e.g.
	 *         when nested metaevent will follow)
	 */
	public String getStringValue() {
		if ((stringValue == null) && (getObjectValue() != null)) {
			return getObjectValue().toString();
		}
		else {
			return stringValue;
		}
	}


	public Object getObjectValue() {
		return objectValue;
	}
	
	
	public boolean hasValue() {
		return (getStringValue() != null) || (getObjectValue() != null);
	}


	public String getOriginalType() {
		return originalType;
	}
	
	
	public boolean hasXMLEventValue() {
		return getObjectValue() instanceof XMLEvent;
	}
	
	
	public XMLEvent getXMLEvent() {  //TODO Should simple strings also be converted to a characters event here or in the constructor?
		return (XMLEvent)getObjectValue();
	}
}
