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
package info.bioinfweb.jphyloio.formats.phylip;


import info.bioinfweb.jphyloio.JPhyloIOEventWriter;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.formats.AbstractSingleReaderWriterFactory;
import info.bioinfweb.jphyloio.formats.JPhyloIOFormatIDs;
import info.bioinfweb.jphyloio.formats.SingleReaderWriterFactory;

import java.io.IOException;
import java.io.Reader;



/**
 * Implements shared functionality for Phylip factories.
 * 
 * @author Ben St&ouml;ver
 * @since 0.0.0
 */
public abstract class AbstractPhylipFactory extends AbstractSingleReaderWriterFactory 
		implements SingleReaderWriterFactory, PhylipConstants, JPhyloIOFormatIDs {
	
	/**
	 * Defines the maximum number of digits in a column or line count definition of a Phylip file. Using this
	 * constant avoids parsing files completely that are not Phylip and just contain many digits in {@code checkFormat()}.
	 */
	public static final int MAXIMUM_EXPECTED_DIGITS_OR_WHITESPACE = 128;
	
	
	@Override
	public boolean checkFormat(Reader reader, ReadWriteParameterMap parameters)	throws IOException {
		int c;
		
		for (int i = 0; i < 2; i++) {
			// Check first whitespace:
			int count = 0;
			do {
				c = reader.read();
				count++;
			} while ((c != -1) && Character.isWhitespace(c) && (count < MAXIMUM_EXPECTED_DIGITS_OR_WHITESPACE));
			if ((c == -1) || !Character.isDigit(c)) {
				return false;
			}
			
			// Check first whitespace:
			count = 0;
			do {
				c = reader.read();
				count++;
			} while ((c != -1) && Character.isDigit(c) && (count < MAXIMUM_EXPECTED_DIGITS_OR_WHITESPACE));
			if ((c == -1) || !Character.isWhitespace(c)) {
				return false;
			}
		}
		//TODO This implementation would return false for empty files that contain only " 0 0". To check this, the numeric values would have to be parsed.
		//TODO Also allow files that do not start with a whitespace?
		
		return true;
	}


	@Override
	public JPhyloIOEventWriter getWriter() {
		return new PhylipEventWriter();  // The writer is the same for both formats, since no line breaks are written within sequences.
	}
	

	@Override
	public boolean hasReader() {
		return true;
	}
	

	@Override
	public boolean hasWriter() {
		return true;
	}
}
