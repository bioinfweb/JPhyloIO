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


import info.bioinfweb.commons.collections.ParameterMap;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.events.type.EventType;



/**
 * Implements basic functionality for {@link JPhyloIOEvent}s.
 * 
 * @author Ben St&ouml;ver
 */
public class ConcreteJPhyloIOEvent implements JPhyloIOEvent {
	private EventType type;
	private ParameterMap metaInformationMap = new ParameterMap();
	
	
	public ConcreteJPhyloIOEvent(EventContentType contentType, EventTopologyType topologyType) {
		super();
		this.type = new EventType(contentType, topologyType);
	}


	@Override
	public EventType getType() {
		return type;
	}


	@Override
	public ParameterMap getMetaInformationMap() {
		return metaInformationMap;
	}


	@Override
	public MetaInformationEvent asMetaInformationEvent() throws ClassCastException {
		return (MetaInformationEvent)this;
	}


	@Override
	public UnknownCommandEvent asUnknownCommandEvent() throws ClassCastException {
		return (UnknownCommandEvent)this;
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


	@Override
	public NodeEvent asNodeEvent() throws ClassCastException {
		return (NodeEvent)this;
	}


	@Override
	public EdgeEvent asEdgeEvent() throws ClassCastException {
		return (EdgeEvent)this;
	}
}
