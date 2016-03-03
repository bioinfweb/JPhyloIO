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
package info.bioinfweb.jphyloio.formats.xtg;


import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

import info.bioinfweb.jphyloio.JPhyloIOEventReader;
import info.bioinfweb.jphyloio.JPhyloIOEventWriter;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.formats.JPhyloIOFormatInfo;
import info.bioinfweb.jphyloio.formats.xml.AbstractXMLFactory;



/**
 * Reader and writer factory for the <a href="http://bioinfweb.info/xmlns/xtg">XTG</a> format.
 * 
 * @author Ben St&ouml;ver
 * @since 0.0.0
 */
public class XTGFactory extends AbstractXMLFactory implements XTGConstants {
	@Override
	protected boolean checkRootTag(StartElement startElement) {
		return startElement.getName().getLocalPart().equals(TAG_DOCUMENT.getLocalPart());
		// Additional information like namespace or XSD is not checked, because early versions of TreeGraph do not specify it and files from other sources may not have it.
	}


	@Override
	public JPhyloIOEventReader getReader(InputStream stream, ReadWriteParameterMap parameters) throws IOException, XMLStreamException {
		return new XTGEventReader(stream, parameters);
	}

	
	@Override
	public JPhyloIOEventReader getReader(Reader reader, ReadWriteParameterMap parameters) throws IOException, XMLStreamException {
		return new XTGEventReader(reader, parameters);
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
		// TODO Auto-generated method stub
		return null;
	}
}
