/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015  Ben Stöver
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
package info.bioinfweb.jphyloio.events;


import info.bioinfweb.commons.collections.ParameterMap;
import info.bioinfweb.jphyloio.JPhyloIOEventReader;



/**
 * This interface is implemented by all events triggered by implementations of {@link JPhyloIOEventReader}.
 * 
 * @author Ben St&ouml;ver
 */
public interface JPhyloIOEvent {
	/**
	 * Returns the type of this event.
	 * 
	 * @return the event type
	 */
	public EventType getEventType();
	
	/**
	 * Returns a map containing additional (possibly format specific) information about this event that
	 * are not covered by properties of the implementing event object.
	 * 
	 * @return a (possibly empty) map object
	 */
	public ParameterMap getMetaInformationMap();
	
	/**
	 * Casts this event to a meta information event.
	 * 
	 * @return a reference to this event as a meta information event
	 * @throws ClassCastException if this event is not an instance of {@link MetaInformationEvent}
	 */
	public MetaInformationEvent asMetaInformationEvent() throws ClassCastException;
	
	/**
	 * Casts this event to a comment event.
	 * 
	 * @return a reference to this event as a tokens event
	 * @throws ClassCastException if this event is not an instance of {@link CommentEvent}
	 */
	public CommentEvent asCommentEvent() throws ClassCastException;
	
	/**
	 * Casts this event to a tokens event.
	 * 
	 * @return a reference to this event as a tokens event
	 * @throws ClassCastException if this event is not an instance of {@link SequenceTokensEvent}
	 */
	public SequenceTokensEvent asSequenceTokensEvent() throws ClassCastException;
	
	/**
	 * Casts this event to a character set event.
	 * 
	 * @return a reference to this event as a tokens event
	 * @throws ClassCastException if this event is not an instance of {@link CharacterSetEvent}
	 */
	public CharacterSetEvent asCharacterSetEvent() throws ClassCastException;	
}
