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



/**
 * Event that indicates that new information on a character set has been read.
 * <p>
 * Note that one or more events with the same character set name may occur any time between 
 * the {@link EventContentType#ALIGNMENT} and {@link EventContentType#ALIGNMENT_END} events.
 * <p>
 * After the first event with a certain name was fired, that name can be used a reference in
 * {@link TokenSetDefinitionEvent#getCharacterSetName()}, even if additional 
 * {@link CharacterSetIntervalEvent}s with the same name will follow. 
 * 
 * @author Ben St&ouml;ver
 */
public class CharacterSetIntervalEvent extends ConcreteJPhyloIOEvent {
	private long start;
	private long end;
	
	
	/**
	 * Creates a new instance of this class.
	 * <p>
	 * A segment ranges from {@link #getStart()} to {@link #getEnd()} {@code - 1}.
	 * 
	 * @param start the index of the first position of the sequence segment to be added to the specified character set
	 * @param end the first index after the end of the sequence segment to be added to the specified character set
	 */
	public CharacterSetIntervalEvent(long start,	long end) {
		super(EventContentType.CHARACTER_SET_INTERVAL, EventTopologyType.SOLE);
		this.start = start;
		this.end = end;
	}


	/**
	 * The first position of the new segment to be added to the character set with the specified name. 
	 * <p>
	 * A segment ranges from {@link #getStart()} to {@code getEnd() - 1}.
	 * 
	 * @return the first position to be included in the character set
	 */
	public long getStart() {
		return start;
	}


	/**
	 * The first index after the new segment to be added to the character set with the specified name.
	 * <p>
	 * A segment ranges from {@link #getStart()} to {@code getEnd() - 1}.
	 * 
	 * @return the first index after the segment to be added
	 */
	public long getEnd() {
		return end;
	}
}
