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
 * Indicates the end of a sequence or subsequence.
 * <p>
 * Sequences may be split across several sequences of sequence token events (each surrounded by a 
 * {@link BasicOTUEvent} and a {@link SequenceEndEvent} event), especially when interleaved formats 
 * are read. {@link #isSequenceTerminated()} determines whether another part of the same 
 * sequence may follow.
 * 
 * @author Ben St&ouml;ver
 */
public class SequenceEndEvent extends ConcreteJPhyloIOEvent {
	private boolean sequenceTerminated;

	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param sequenceTerminated Specifies whether another part of the same sequence may follow.
	 */
	public SequenceEndEvent(boolean sequenceTerminated) {
		super(EventContentType.SEQUENCE, EventTopologyType.END);
		this.sequenceTerminated = sequenceTerminated;
	}


	/**
	 * Specifies whether another part of the same sequence may follow.
	 * <p>
	 * Sequences may be split across several sequences of sequence token events (each surrounded by a 
	 * {@link BasicOTUEvent} and a {@link SequenceEndEvent} event), especially when interleaved formats
	 * are read. 
	 * 
	 * @return {@code true} if this sequence is definitely terminated or {@code false} if another part may (or may not) follow 
	 */
	public boolean isSequenceTerminated() {
		return sequenceTerminated;
	}
}
