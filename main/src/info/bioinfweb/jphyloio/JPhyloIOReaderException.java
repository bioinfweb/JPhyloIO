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


import javax.xml.stream.Location;



public class JPhyloIOReaderException extends Exception {
	private long charIndex;
	private long line;
	private long column;
	
	
	public JPhyloIOReaderException(String message, long charIndex, long line, long column, Throwable cause) {
		super(message, cause);
		this.charIndex = charIndex;
		this.line = line;
		this.column = column;
	}


	public JPhyloIOReaderException(String message, long charIndex, long line, long column) {
		this(message, charIndex, line, column, null);
	}


	public JPhyloIOReaderException(String message, Location location, Throwable cause) {
		this(message, location.getCharacterOffset(), location.getLineNumber(), location.getColumnNumber(), cause);
	}


	public JPhyloIOReaderException(String message, Location location) {
		this(message, location.getCharacterOffset(), location.getLineNumber(), location.getColumnNumber(), null);
	}


	/**
	 * Returns the index of the character in the underlying data source, where the error occurred.
	 * The first character in the stream would have the index 0.
	 * 
	 * @return the character index or -1 if the index is unknown
	 */
	public long getCharIndex() {
		return charIndex;
	}


	/**
	 * Returns the index of the line in the underlying data source, where the error occurred.
	 * The first line would have the index 0.
	 * 
	 * @return the line index or -1 if the index is unknown
	 */
	public long getLine() {
		return line;
	}


	/**
	 * Returns the index of the column in the underlying data source, where the error occurred.
	 * The first column in each line would have the index 0.
	 * 
	 * @return the column index or -1 if the index is unknown
	 */
	public long getColumn() {
		return column;
	}
}
