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


import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;



public class SingleSequenceTokenEvent extends ConcreteJPhyloIOEvent {
	private String token;
	//TODO should there be a property for the character this token belongs to? => No, but events in NeXML need to fired in the correct order (which may differ from the order in the file in rare cases.)

	
	public SingleSequenceTokenEvent(String token) {
		super(EventContentType.SINGLE_SEQUENCE_TOKEN, EventTopologyType.START);
		this.token = token;
	}


	public String getToken() {
		return token;
	}
}
