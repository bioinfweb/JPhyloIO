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
package info.bioinfweb.jphyloio.events;


import info.bioinfweb.jphyloio.JPhyloIOEventReader;
import info.bioinfweb.jphyloio.events.type.EventType;



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
	public EventType getType();
	
	/**
	 * Casts this event to a meta information event.
	 * 
	 * @return a reference to this event as a meta information event
	 * @throws ClassCastException if this event is not an instance of {@link MetaInformationEvent}
	 */
	public MetaInformationEvent asMetaInformationEvent() throws ClassCastException;
	
	/**
	 * Casts this event to an unknown command event.
	 * 
	 * @return a reference to this event as an unknown command event
	 * @throws ClassCastException if this event is not an instance of {@link UnknownCommandEvent}
	 */
	public UnknownCommandEvent asUnknownCommandEvent() throws ClassCastException;
	
	/**
	 * Casts this event to a comment event.
	 * 
	 * @return a reference to this event as a tokens event
	 * @throws ClassCastException if this event is not an instance of {@link CommentEvent}
	 */
	public CommentEvent asCommentEvent() throws ClassCastException;
	
	/**
	 * Casts this event to a part end event.
	 * 
	 * @return a reference to this event as a part end event
	 * @throws ClassCastException if this event is not an instance of {@link PartEndEvent}
	 */
	public PartEndEvent asPartEndEvent() throws ClassCastException;
	
	/**
	 * Casts this event to a basic OTU event.
	 * 
	 * @return a reference to this event as a basic OTU event
	 * @throws ClassCastException if this event is not an instance of {@link BasicOTUEvent}
	 */
	public BasicOTUEvent asBasicOTUEvent() throws ClassCastException;
	
	/**
	 * Casts this event to a tokens event.
	 * 
	 * @return a reference to this event as a tokens event
	 * @throws ClassCastException if this event is not an instance of {@link SequenceTokensEvent}
	 */
	public SequenceTokensEvent asSequenceTokensEvent() throws ClassCastException;
	
	/**
	 * Casts this event to a single token event.
	 * 
	 * @return a reference to this event as a single token event
	 * @throws ClassCastException if this event is not an instance of {@link SingleSequenceTokenEvent}
	 */
	public SingleSequenceTokenEvent asSingleSequenceTokenEvent() throws ClassCastException;
	
	/**
	 * Casts this event to a character set event.
	 * 
	 * @return a reference to this event as a tokens event
	 * @throws ClassCastException if this event is not an instance of {@link CharacterSetEvent}
	 */
	public CharacterSetEvent asCharacterSetEvent() throws ClassCastException;	
	
	/**
	 * Casts this event to a character state set definition event.
	 * 
	 * @return a reference to this event as a token set definition event
	 * @throws ClassCastException if this event is not an instance of {@link TokenSetDefinitionEvent}
	 */
	public TokenSetDefinitionEvent asTokenSetDefinitionEvent() throws ClassCastException;	
	
	/**
	 * Casts this event to a single character state symbol definition event.
	 * 
	 * @return a reference to this event as a token definition event
	 * @throws ClassCastException if this event is not an instance of {@link SingleTokenDefinitionEvent}
	 */
	public SingleTokenDefinitionEvent asSingleTokenDefinitionEvent() throws ClassCastException;	
	
	/**
	 * Casts this event to a tree or graph node event.
	 * 
	 * @return a reference to this event as a node event
	 * @throws ClassCastException if this event is not an instance of {@link SingleTokenDefinitionEvent}
	 */
	public NodeEvent asNodeEvent() throws ClassCastException;	
	
	/**
	 * Casts this event to a tree or graph edge event.
	 * 
	 * @return a reference to this event as an edge event
	 * @throws ClassCastException if this event is not an instance of {@link SingleTokenDefinitionEvent}
	 */
	public EdgeEvent asEdgeEvent() throws ClassCastException;	
}
