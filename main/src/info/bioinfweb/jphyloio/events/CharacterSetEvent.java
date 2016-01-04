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



/**
 * Event that indicates that new information on a character set has been parsed.
 * <p>
 * Note that one or more events with the same character set name may occur any time between 
 * the {@link EventType#ALIGNMENT} and {@link EventType#ALIGNMENT_END} events.   
 * 
 * @author Ben St&ouml;ver
 */
public class CharacterSetEvent extends ConcreteJPhyloIOEvent {
	private String name;
	private long start;
	private long end;
	
	
	/**
	 * Creates a new instance of this class.
	 * <p>
	 * A segment ranges from {@link #getStart()} to {@link #getEnd()} {@code - 1}.
	 * 
	 * @param name the name of the character set that shall be extended
	 * @param start the index of the first position of the sequence segment to be added to the specified character set
	 * @param end the first index after the end of the sequence segment to be added to the specified character set
	 */
	public CharacterSetEvent(String name, long start,	long end) {
		super(EventType.CHARACTER_SET);
		this.name = name;
		this.start = start;
		this.end = end;
	}


	/**
	 * The name of the character set. Depending on the parsed format the name might have a special form.
	 * 
	 * @return the parsed name
	 */
	public String getName() {
		return name;
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
