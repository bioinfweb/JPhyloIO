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
package info.bioinfweb.jphyloio.utils;


import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.dataadapters.ObjectListDataAdapter;

import java.util.Iterator;
import java.util.NoSuchElementException;



/**
 * Implementation of {@link Iterator} that returns strings with defined prefix followed by an integer from a specified range.
 * <p>
 * This tool class is helpful for implementing data adapters. It can be returned as an ID iterator if the application
 * uses numbered IDs as a certain prefix e.g. by 
 * {@link ObjectListDataAdapter#getIDIterator(info.bioinfweb.jphyloio.ReadWriteParameterMap)}.
 * <p>
 * The <a href="http://bioinfweb.info/JPhyloIO/Documentation/Demos">example applications</a> of <i>JPhyloIO</i> demonstrate 
 * the usage of this class. 
 * 
 * @author Ben St&ouml;ver
 * @since 0.1.0
 */
public class NumberedIDsIterator implements Iterator<String> {
	private String prefix;
	private long startIndex;
	private long endIndex;
	

	/**
	 * Creates a new instance of this class.
	 * 
	 * @param prefix the prefix all returned IDs (strings) shall have
	 * @param startIndex the index of the first ID (string) to be returned
	 * @param count the number of elements to be returned by this iterator
	 */
	public NumberedIDsIterator(String prefix, long startIndex, long count) {
		super();
		this.prefix = prefix;
		this.startIndex = startIndex;
		this.endIndex = startIndex + count;
	}


	/**
	 * Creates a new instance of this class starting with the index 0.
	 * 
	 * @param prefix the prefix all returned IDs (strings) shall have
	 * @param count the number of elements to be returned by this iterator
	 */
	public NumberedIDsIterator(String prefix, long count) {
		this(prefix, 0, count);
	}
	
	
	@Override
	public boolean hasNext() {
		return startIndex < endIndex;
	}

	
	@Override
	public String next() {
		if (hasNext()) {
			startIndex++;
			return prefix + (startIndex - 1);
		}
		else {
			throw new NoSuchElementException("This iterator does not have more elements.");
		}
	}


	@Override
	public void remove() {
		throw new UnsupportedOperationException("Removing IDs from this iterator is not legal.");
	}
	
	
	/**
	 * Extracts an index from a numbered sequence ID. (Sequence IDs are assumed to start with a common prefix 
	 * followed by an integer index.
	 * 
	 * @param sequenceID the event ID
	 * @return the extracted index
	 * @throws IllegalArgumentException if no index could be extracted from the specified ID
	 */
	public static long extractIndexFromID(String id, String prefix) {
		if (id.startsWith(prefix)) {
			try {
				return Long.parseLong(id.substring(prefix.length()));
			}
			catch (NumberFormatException e) {}
		}
		throw new IllegalArgumentException("The ID \"" + id + "\" is not valid at this position.");
	}
}
