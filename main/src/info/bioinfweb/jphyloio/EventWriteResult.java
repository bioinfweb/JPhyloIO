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
package info.bioinfweb.jphyloio;



/**
 * Describes the result of a call of {@link JPhyloIOEventWriter#writeEvent(info.bioinfweb.jphyloio.events.JPhyloIOEvent)}.
 * 
 * @author Ben St&ouml;ver
 */
public enum EventWriteResult {
	/** 
	 * Indicates that this event is supported by the writer instance and was written at the current position of the 
	 * underlying stream, if necessary.
	 * <p>
	 * Note that some supported events (e.g. {@link info.bioinfweb.jphyloio.events.EventType#DOCUMENT_START} do not 
	 * require any concrete write operation in some formats. 
	 */
	WRITTEN,
	
	/** 
	 * Indicates that the event was stored to be written later because the target format does not 
	 * support this element at the current position, but in general this event is supported by the writer.  
	 */
	QUEUED,
	
	/** Indicates that the specified event is not supported by the writer instance and was ignored. */
	NOT_WRITTEN;
}
