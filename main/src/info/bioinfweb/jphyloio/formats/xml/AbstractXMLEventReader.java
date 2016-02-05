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
package info.bioinfweb.jphyloio.formats.xml;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Stack;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import info.bioinfweb.jphyloio.AbstractEventReader;



/**
 * Implements shared functionality for reading XML formats.
 * 
 * @author Ben St&ouml;ver
 */
public abstract class AbstractXMLEventReader extends AbstractEventReader {	
	private XMLEventReader xmlReader;
	private Stack<QName> encounteredTags = new Stack<QName>();
	
	
	public AbstractXMLEventReader(boolean translateMatchToken, File file) throws IOException, XMLStreamException {
		this(translateMatchToken, new FileReader(file));
	}

	
	public AbstractXMLEventReader(boolean translateMatchToken, InputStream stream) throws IOException, XMLStreamException {
		this(translateMatchToken, new InputStreamReader(stream));
	}

	
	public AbstractXMLEventReader(boolean translateMatchToken, XMLEventReader xmlReader) {
		super(translateMatchToken);
		this.xmlReader = xmlReader;
	}
	
	
	public AbstractXMLEventReader(boolean translateMatchToken, int maxTokensToRead, XMLEventReader xmlReader) {
		super(translateMatchToken, maxTokensToRead);
		this.xmlReader = xmlReader;
	}

	
	public AbstractXMLEventReader(boolean translateMatchToken, Reader reader) throws IOException, XMLStreamException {
		super(translateMatchToken);
		if (!(reader instanceof BufferedReader)) {
			reader = new BufferedReader(reader);
		}
		this.xmlReader = XMLInputFactory.newInstance().createXMLEventReader(reader);		
	}
	
	
	@Override
	protected XMLStreamDataProvider createStreamDataProvider() {
		return new XMLStreamDataProvider(this);
	}
	
	
	protected XMLEventReader getXMLReader() {
		return xmlReader;
	}
	

	protected Stack<QName> getEncounteredTags() {
		return encounteredTags;
	}
}
