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
package info.bioinfweb.phyloio;


import java.util.NoSuchElementException;

import info.bioinfweb.phyloio.events.PhyloIOEvent;



/**
 * The main PhyloIO interface to be implemented by all format specific event readers.
 * 
 * @author Ben St&ouml;ver
 */
public interface PhyloIOEventReader {
	/**
	 * Checks if another event could be parsed from the underlying document.
	 * 
	 * @return {@code true} if another event is waiting, {@code false} if the end of the underlying document was reached
	 */
	public boolean hasNextEvent() throws Exception;
	
	/**
	 * Returns the next event from the underlying document and moves one step forward.
	 * 
	 * @return the next event object
	 * @throws NoSuchElementException if the end of the document has been reached with the previous call of this method
	 * @throws Exception Implementing classes might throw additional exceptions
	 */
	public PhyloIOEvent next() throws Exception;

	/**
	 * Returns the event from the underlying document that will be returned in the next call of {@link #next()}
	 * without moving forward.
	 * 
	 * @return the next event object
	 * @throws NoSuchElementException if the end of the document has been reached with the previous call of this method
	 * @throws Exception Implementing classes might throw additional exceptions
	 */
	public PhyloIOEvent peek() throws Exception;
	
	/**
	 * Closes the underlying document source (usually a stream).
	 * 
	 * @throws Exception Implementing classes might throw different types of exceptions if the stream cannot 
	 *         be closed properly
	 */
	public void close() throws Exception;
	
	/**
	 * Closes the underlying document source (usually a stream). The next call of {@link #next()} will start at the 
	 * beginning of the document again.
	 * 
	 * @throws Exception Implementing classes might throw different types of exceptions if the underlying source cannot 
	 *         reseted
	 */
	public void reset() throws Exception;
}