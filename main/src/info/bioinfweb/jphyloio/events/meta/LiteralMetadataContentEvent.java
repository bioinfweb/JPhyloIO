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


import java.util.List;

import info.bioinfweb.jphyloio.events.ContinuedEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;

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
 * Note that large string values may additionally be separated along subsequent events (indicated by 
 * {@link #isContinuedInNextEvent()}) in all three cases. This is only possible if the object value is identical with its
 * string representation. This technique is not meant to split any other value (e.g. {@link List} instances) among separate
 * events.
 * 
 * @author Ben St&ouml;ver
 * @since 0.0.0
 */
public class LiteralMetadataContentEvent extends ContinuedEvent {
	private String stringValue;
	private Object objectValue;
	private String alternativeStringValue = null;
	private UriOrStringIdentifier originalType;

	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param originalType the original type of meta information or null. 
	 * @param stringValue the string value of the meta information.
	 * @param continuedInNextEvent Specify {@code true} here if this event does not contain the final part of 
	 *        its value and more events are ahead or {@code false otherwise}.
	 */
	public LiteralMetadataContentEvent(UriOrStringIdentifier originalType, String stringValue, boolean continuedInNextEvent) {
		this(originalType, stringValue, stringValue, null, continuedInNextEvent);
	}
	
	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param originalType the original type of meta information or null. 
	 * @param stringValue the string value of the meta information.
	 * @param objectValue the object value of the meta information.
	 */
	public LiteralMetadataContentEvent(UriOrStringIdentifier originalType, String stringValue, Object objectValue) {
		this(originalType, stringValue, objectValue, null, false);
	}
	
	
	public LiteralMetadataContentEvent(UriOrStringIdentifier originalType, String stringValue, Object objectValue, String alternativeStringValue) {
		this(originalType, stringValue, objectValue, alternativeStringValue, false);
	}
	
	
	public LiteralMetadataContentEvent(UriOrStringIdentifier originalType, String stringValue, String alternativeStringValue) {
		this(originalType, stringValue, stringValue, alternativeStringValue, false);
	}
	
	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param originalType the original type of meta information or null. 
	 * @param stringValue the string value of the meta information.
	 * @param objectValue the object value of the meta information.
	 * @param alternativeStringValue an alternative string representation of the value (Some formats may provide alternative
	 *        representations of a value, e.g. a human and a machine readable one.)
	 * @param continuedInNextEvent Specify {@code true} here if this event does not contain the final part of 
	 *        its value and more events are ahead or {@code false otherwise}.
	 * @throws NullPointerException if {@code stringValue} is {@code null} and {@code alternativeStringValue} is not
	 */
	private LiteralMetadataContentEvent(UriOrStringIdentifier originalType, String stringValue, Object objectValue, String alternativeStringValue, 
			boolean continuedInNextEvent) {
		
		super(EventContentType.META_LITERAL_CONTENT, continuedInNextEvent);
		
		this.stringValue = stringValue;  //TODO Should an NPE be thrown if stringValue and objectValue are null?
		if (!(objectValue instanceof String) && isContinuedInNextEvent()) {
			throw new IllegalArgumentException("Only string values may be separated amoung continued events.");
		}
		else {
			this.objectValue = objectValue;
			this.originalType = originalType;
			if ((stringValue == null) && (alternativeStringValue != null)) {
				throw new NullPointerException("stringValue must not be null, if alternativeStringValue is provided.");
			}
			else {
				this.alternativeStringValue = alternativeStringValue;
			}
		}
	}
	

	/**
	 * Creates a new instance of this class wrapping an {@link XMLEvent}.
	 * 
	 * @param xmlEvent the XML event object to be wrapped
	 * @param continuedInNextEvent Specify {@code true} here if this event does not contain the final part of 
	 *        its value and more events are ahead or {@code false otherwise}. (Note that {@code true} this is only allowed
	 *        for character XML events which contain only a part of the represented string.)
	 * @throws IllegalArgumentException if {@code continuedInNextEvent} in set to {@code true}, but the specified XML event
	 *         was not a characters event 
	 */
	public LiteralMetadataContentEvent(XMLEvent xmlEvent, boolean continuedInNextEvent) {
		this(xmlEvent, continuedInNextEvent, null);
	}
	
	
	public LiteralMetadataContentEvent(XMLEvent xmlEvent, boolean continuedInNextEvent, String alternativeStringValue) {
		super(EventContentType.META_LITERAL_CONTENT, continuedInNextEvent);
		
		if (!xmlEvent.isCharacters() && continuedInNextEvent) {
			throw new IllegalArgumentException("Only character XML events may be continued in the next event. "
					+ "The specified event had the type " + xmlEvent.getEventType() + ".");
		}
		else {
			this.stringValue = null;
			this.alternativeStringValue = alternativeStringValue;
			this.objectValue = xmlEvent;
			this.originalType = null;  //TODO Should something else be stored here (e.g. the XML event type)?
		}
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


	public UriOrStringIdentifier getOriginalType() {
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
