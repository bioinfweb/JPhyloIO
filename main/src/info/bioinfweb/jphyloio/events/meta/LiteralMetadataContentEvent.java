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
 * This event represents a value of a literal metadata and is therefore always nested inside start and end events of the type
 * {@link EventContentType#META_LITERAL}.
 * <p>
 * Values can be represented in the following ways:
 * <ul>
 *   <li>An instance can represent a single object value directly.</li>
 *   <li>A sequence of events can represent values of an array (e.g. read from a Newick hot comment).</li>
 *   <li>A sequence of events can represent a more complex XML representation of the literal value. In such cases one event 
 *       instance will represent each {@link XMLEvent} created from the encountered XML.</li>
 * </ul>
 * 
 * @author Ben St&ouml;ver
 * @since 0.0.0
 */
public class LiteralMetadataContentEvent extends ConcreteJPhyloIOEvent {
	private String stringValue;
	private Object objectValue;
	private String alternativeStringValue = null;
	private String originalType;

	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param originalType the original type of meta information or null. 
	 * @param stringValue the string value of the meta information.
	 */
	public LiteralMetadataContentEvent(String originalType, String stringValue) {
		this(originalType, stringValue, stringValue);
	}
	
	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param originalType the original type of meta information or null. 
	 * @param stringValue the string value of the meta information.
	 * @param objectValue the object value of the meta information.
	 */
	public LiteralMetadataContentEvent(String originalType, String stringValue, Object objectValue) {
		this(originalType, stringValue, objectValue, null);
	}
	
	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param originalType the original type of meta information or null. 
	 * @param stringValue the string value of the meta information.
	 * @param objectValue the object value of the meta information.
	 * @param alternativeStringValue an alternative string representation of the value (Some formats may provide alternative
	 *        representations of a value, e.g. a human and a machine readable one.)
	 * @throws NullPointerException if {@code stringValue} is {@code null} and {@code alternativeStringValue} is not
	 */
	public LiteralMetadataContentEvent(String originalType, String stringValue, Object objectValue, String alternativeStringValue) {
		super(EventContentType.META_LITERAL_CONTENT, EventTopologyType.SOLE);
		
		this.stringValue = stringValue;  //TODO Should an NPE be thrown if stringValue and objectValue are null?
		this.objectValue = objectValue;
		this.originalType = originalType;
		if ((stringValue == null) && (alternativeStringValue != null)) {
			throw new NullPointerException("stringValue must not be null, if alternativeStringValue is provided.");
		}
		else {
			this.alternativeStringValue = alternativeStringValue;
		}
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


	/**
	 * Determines whether an alternative string representation is available for the value modeled by this event.
	 * 
	 * @return {@code true} if an alternative representation is available or {@code false} otherwise
	 */
	public boolean hasAlternativeStringValue() {
		return getAlternativeStringValue() != null;
	}
	
	
	/**
	 * Returns the alternative string representation of the literal value modeled by this event.
	 * <p>
	 * Some formats may provide alternative representations of a value, e.g. a human and a machine readable one.
	 * 
	 * @return the alternative representation or {@code null} if there is none
	 */
	public String getAlternativeStringValue() {
		return alternativeStringValue;
	}
}
