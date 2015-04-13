/*
 * PhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015  Ben Stöver
 * <http://bioinfweb.info/PhyloIO>
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
package info.bioinfweb.phyloio.events;


import info.bioinfweb.commons.collections.ParameterMap;



/**
 * Implements basic functionality for {@link PhyloIOEvent}s.
 * 
 * @author Ben St&ouml;ver
 */
public class ConcretePhyloIOEvent implements PhyloIOEvent {
	private EventType eventType;
	private ParameterMap metaInformationMap = new ParameterMap();
	
	
	public ConcretePhyloIOEvent(EventType eventType) {
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
	public MetaInformationEvent asMetaInformationEvent() throws ClassCastException {
		return (MetaInformationEvent)this;
	}


	@Override
	public SequenceStartEvent asSequenceStartEvent() throws ClassCastException {
		return (SequenceStartEvent)this;
	}


	@Override
	public TokensEvent asTokensEvent() throws ClassCastException {
		return (TokensEvent)this;
	}
}
