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
package info.bioinfweb.jphyloio.formats.phyloxml;


import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import javax.xml.stream.XMLStreamException;

import info.bioinfweb.commons.io.ExtensionFileFilter;
import info.bioinfweb.jphyloio.JPhyloIOEventReader;
import info.bioinfweb.jphyloio.JPhyloIOEventWriter;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.formats.DefaultFormatInfo;
import info.bioinfweb.jphyloio.formats.JPhyloIOFormatIDs;
import info.bioinfweb.jphyloio.formats.JPhyloIOFormatInfo;
import info.bioinfweb.jphyloio.formats.xml.AbstractXMLFactory;



/**
 * Reader and writer factory for the <a href="http://bioinfweb.info/xmlns/xtg">XTG</a> format.
 * 
 * @author Ben St&ouml;ver
 * @since 0.0.0
 */
public class PhyloXMLFactory extends AbstractXMLFactory implements PhyloXMLConstants, JPhyloIOFormatIDs {
	public PhyloXMLFactory() {
		super(TAG_ROOT);
	}


	@Override
	public JPhyloIOEventReader getReader(InputStream stream, ReadWriteParameterMap parameters) throws IOException, XMLStreamException {
		return new PhyloXMLEventReader(stream, parameters);
	}

	
	@Override
	public JPhyloIOEventReader getReader(Reader reader, ReadWriteParameterMap parameters) throws IOException, XMLStreamException {
		return new PhyloXMLEventReader(reader, parameters);
	}

	
	@Override
	public JPhyloIOEventWriter getWriter() {
		return null;  //TODO Add reader when implemented.
	}

	
	@Override
	public boolean hasReader() {
		return true;
	}

	
	@Override
	public boolean hasWriter() {
		return false;
	}

	
	@Override
	public JPhyloIOFormatInfo getFormatInfo() {
		return new DefaultFormatInfo(PHYLOXML_FORMAT_ID, PHYLOXML_FORMAT_NAME, new ExtensionFileFilter(
				"PhylXML", "phyloxml", true, "phylo.xml", "pxml", "xml"));
	}
}
