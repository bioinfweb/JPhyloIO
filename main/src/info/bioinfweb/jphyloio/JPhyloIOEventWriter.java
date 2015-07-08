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
package info.bioinfweb.jphyloio;


import java.io.Closeable;
import java.io.Flushable;

import info.bioinfweb.jphyloio.events.JPhyloIOEvent;



public interface JPhyloIOEventWriter extends Closeable, Flushable {
	/**
	 * Writes the specified event to the stream.
	 * 
	 * @param event the event to written to the current position of the stream
	 * @return the result of this write operation (Events can be written, not written or queued to be written later.)
	 */
	public EventWriteResult writeEvent(JPhyloIOEvent event) throws Exception;  
	//TODO Throw container exception instead (also to be used in the reader classes)? Alternatively, implementing methods can restrict the exception type(s)
}
