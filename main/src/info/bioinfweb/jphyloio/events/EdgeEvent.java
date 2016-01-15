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



public class EdgeEvent extends ConcreteJPhyloIOEvent {
	private String sourceID;
	private String targetID;
	private double length;
	
	
	public EdgeEvent(String sourceID, String targetID, double length) {
		super(EventContentType.EDGE, EventTopologyType.START);

		if (targetID == null) {
			throw new NullPointerException("The target node ID must not be null.");
		}
		else {
			this.sourceID = sourceID; //can be null if edge is a root edge
			this.targetID = targetID;
			this.length = length;
		}
	}


	public String getSourceID() {
		return sourceID;
	}


	public String getTargetID() {
		return targetID;
	}


	public double getLength() {
		return length;
	}


	@Override
	public String toString() {
		return getType() + " (" + getSourceID() + " -> " + getTargetID() + "):" + getLength();
	}	
}