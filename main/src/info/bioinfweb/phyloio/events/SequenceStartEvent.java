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



/**
 * Indicates that a new sequence has been encountered at the current position of the document.  
 * 
 * @author Ben St&ouml;ver
 */
public class SequenceStartEvent extends ConcretePhyloIOEvent {
	private String name;

	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param name the name of encountered sequence as read from the document
	 */
	public SequenceStartEvent(String name) {
		super(EventType.SEQUENCE_START);
		this.name = name;
	}


	/**
	 * Returns the name of the new sequence as read from the document.
	 * 
	 * @return the name of encountered sequence
	 */
	public String getName() {
		return name;
	}
}
