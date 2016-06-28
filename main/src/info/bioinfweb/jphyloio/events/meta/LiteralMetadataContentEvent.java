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


import info.bioinfweb.jphyloio.events.ContinuedEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.objecttranslation.ObjectTranslatorFactory;

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
 * When readers create instances of this event, they try to create object values from their string representations using a
 * translator returned by {@link ObjectTranslatorFactory}. If no according translator is available, the object value will be
 * {@code null}.
 * <p>
 * If the declared data type is mapped to an instance of {@link String} (e.g. {@code xsd:string} or {@code xsd:token}) the object
 * value and its string representation are the same {@link String} instance. Large strings may be separated among several events
 * for performance reasons, while {@link #isContinuedInNextEvent()} will be {@code true} in all but the last event of such a 
 * sequence. If a string is separated among multiple events, the object value in all of these events will be {@code null}. 
 * (That is because only the whole string is considered as the object and not its parts.)
 * 
 * @author Ben St&ouml;ver
 * @since 0.0.0
 */
public class LiteralMetadataContentEvent extends ContinuedEvent {
	private String stringValue;
	private Object objectValue;
	@Deprecated
	private String alternativeStringValue = null;
	@Deprecated
	private URIOrStringIdentifier originalType;

	
	@Deprecated
	public LiteralMetadataContentEvent(URIOrStringIdentifier originalType, String stringValue, boolean continuedInNextEvent) {
		this(originalType, stringValue, null, null, continuedInNextEvent);
	}
	

	@Deprecated
	public LiteralMetadataContentEvent(URIOrStringIdentifier originalType, String stringValue, Object objectValue) {
		this(originalType, stringValue, objectValue, null, false);
	}
	
	
	@Deprecated
	public LiteralMetadataContentEvent(URIOrStringIdentifier originalType, String stringValue, Object objectValue, String alternativeStringValue) {
		this(originalType, stringValue, objectValue, alternativeStringValue, false);
	}
	
	
	@Deprecated
	public LiteralMetadataContentEvent(URIOrStringIdentifier originalType, String stringValue, String alternativeStringValue) {
		this(originalType, stringValue, null, alternativeStringValue, false);
	}

	
	@Deprecated
	private LiteralMetadataContentEvent(URIOrStringIdentifier originalType, String stringValue, Object objectValue, String alternativeStringValue, 
			boolean continuedInNextEvent) {
		
		super(EventContentType.META_LITERAL_CONTENT, continuedInNextEvent);
		
		if ((stringValue == null) && (objectValue == null)) {
			throw new NullPointerException("Either stringValue or objectValue must be specified. If a literal meta event has no content, the content event should be omitted.");
		}
		else {
			this.stringValue = stringValue;
			if ((objectValue != null) && isContinuedInNextEvent()) {
				throw new IllegalArgumentException("If a value is separated among several events no object value may be specified.");
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
	}
	
	
	@Deprecated
	public LiteralMetadataContentEvent(XMLEvent xmlEvent, boolean continuedInNextEvent, String alternativeStringValue) {
		super(EventContentType.META_LITERAL_CONTENT, continuedInNextEvent);
		
		if (!xmlEvent.isCharacters() && continuedInNextEvent) {
			throw new IllegalArgumentException("Only character XML events may be continued in the next event. "
					+ "The specified event had the type " + xmlEvent.getEventType() + ".");
		}
		else {
			if (xmlEvent.isCharacters()) {
				this.stringValue = xmlEvent.asCharacters().getData();  //TODO Also store tags as string representations?
			}
			this.alternativeStringValue = alternativeStringValue;
			this.objectValue = xmlEvent;
			this.originalType = null;  //TODO Should something else be stored here (e.g. the XML event type)?
		}
	}
	
	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param stringValue the string value of the meta information.
	 * @param continuedInNextEvent Specify {@code true} here if this event does not contain the final part of 
	 *        its value and more events are ahead or {@code false otherwise}.
	 */
	public LiteralMetadataContentEvent(String stringValue, boolean continuedInNextEvent) {
		this(stringValue, null, continuedInNextEvent);
	}
	
	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param stringValue the string value of the meta information.
	 * @param objectValue the object value of the meta information.
	 */
	public LiteralMetadataContentEvent(String stringValue, Object objectValue) {
		this(stringValue, objectValue, false);
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
	private LiteralMetadataContentEvent(String stringValue, Object objectValue, boolean continuedInNextEvent) {		
		super(EventContentType.META_LITERAL_CONTENT, continuedInNextEvent);
		
		if ((stringValue == null) && (objectValue == null)) {
			throw new NullPointerException("Either stringValue or objectValue must be specified. If a literal meta event has no content, the content event should be omitted.");
		}
		else {
			this.stringValue = stringValue;
			if ((objectValue != null) && isContinuedInNextEvent()) {
				throw new IllegalArgumentException("If a value is separated among several events no object value may be specified.");
			}
			else {
				this.objectValue = objectValue;
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
		super(EventContentType.META_LITERAL_CONTENT, continuedInNextEvent);
		
		if (!xmlEvent.isCharacters() && continuedInNextEvent) {
			throw new IllegalArgumentException("Only character XML events may be continued in the next event. "
					+ "The specified event had the type " + xmlEvent.getEventType() + ".");
		}
		else {
			if (xmlEvent.isCharacters()) {
				this.stringValue = xmlEvent.asCharacters().getData();  //TODO Also store tags as string representations?
			}			
			this.objectValue = xmlEvent;
		}
	}
	

	/**
	 * Returns the string value of the meta information.
	 * 
	 * @return the string representation of the meta information or {@code null} if this metaevent carries no value (e.g.
	 *         when nested metaevent will follow)
	 */
	public String getStringValue() {
		return stringValue;
	}


	public Object getObjectValue() {
		return objectValue;
	}
	
	
	public boolean hasValue() {
		return (getStringValue() != null) || (getObjectValue() != null);
	}


	@Deprecated
	public URIOrStringIdentifier getOriginalType() {
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
	@Deprecated
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
	@Deprecated
	public String getAlternativeStringValue() {
		return alternativeStringValue;
	}
}
