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


import info.bioinfweb.jphyloio.JPhyloIOEventReader;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.formats.JPhyloIOFormatInfo;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;



/**
 * Reader and writer factory for the sequential Phylip format. Note that the {@code checkFormat()} methods of this factory
 * cannot determine the difference between sequential and non-sequential Phylip.
 * 
 * @author Ben St&ouml;ver
 * @since 0.0.0
 * @see PhylipFactory
 */
public class SequentialPhylipFactory extends AbstractPhylipFactory {
	@Override
	public JPhyloIOEventReader getReader(InputStream stream, ReadWriteParameterMap parameters) throws IOException {
		return new SequentialPhylipEventReader(stream, parameters);
	}

	
	@Override
	public JPhyloIOEventReader getReader(Reader reader, ReadWriteParameterMap parameters) throws IOException {
		return new SequentialPhylipEventReader(reader, parameters);
	}

	
	@Override
	public JPhyloIOFormatInfo getFormatInfo() {
		return getFormatInfo(SEQUENTIAL_PHYLIP_FORMAT_ID, SEQUENTIAL_PHYLIP_FORMAT_NAME, "Sequential Phylip format");
	}
}
