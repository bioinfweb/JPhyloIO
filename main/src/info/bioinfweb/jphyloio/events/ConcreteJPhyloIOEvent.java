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



/**
 * Implements basic functionality for {@link JPhyloIOEvent}s.
 * 
 * @author Ben St&ouml;ver
 */
public class ConcreteJPhyloIOEvent implements JPhyloIOEvent {
	private EventType eventType;
	private ParameterMap metaInformationMap = new ParameterMap();
	
	
	public ConcreteJPhyloIOEvent(EventType eventType) {
		super();
		this.eventType = eventType;
	}


	@Override
	public EventType getEventType() {
		return eventType;
	}


	@Override
	public ParameterMap getMetaInformationMap() {
		return metaInformationMap;
	}


	@Override
	public BlockEndEvent asBlockEndEvent() throws ClassCastException {
		return (BlockEndEvent)this;
	}


	@Override
	public MetaInformationEvent asMetaInformationEvent() throws ClassCastException {
		return (MetaInformationEvent)this;
	}


	@Override
	public CommentEvent asCommentEvent() throws ClassCastException {
		return (CommentEvent)this;
	}


	@Override
	public SequenceTokensEvent asSequenceTokensEvent() throws ClassCastException {
		return (SequenceTokensEvent)this;
	}


	@Override
	public CharacterSetEvent asCharacterSetEvent() throws ClassCastException {
		return (CharacterSetEvent)this;
	}


	@Override
	public TokenSetDefinitionEvent asTokenSetDefinitionEvent() throws ClassCastException {
		return (TokenSetDefinitionEvent)this;
	}


	@Override
	public SingleTokenDefinitionEvent asSingleTokenDefinitionEvent() throws ClassCastException {
		return (SingleTokenDefinitionEvent)this;
	}
}
