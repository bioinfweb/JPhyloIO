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
package info.bioinfweb.jphyloio.formats.pde;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import info.bioinfweb.jphyloio.JPhyloIOReaderException;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.formats.JPhyloIOFormatIDs;
import info.bioinfweb.jphyloio.formats.xml.AbstractXMLElementReader;
import info.bioinfweb.jphyloio.formats.xml.AbstractXMLEventReader;
import info.bioinfweb.jphyloio.formats.xml.XMLElementReader;
import info.bioinfweb.jphyloio.formats.xml.XMLElementReaderKey;



/**
 * Event reader for the PDE format used by the alignment editor <a href="http://phyde.de/">PhyDE</a>.
 * 
 * @author Ben St&ouml;ver
 * @since 0.0.0
 */
public class PDEEventReader extends AbstractXMLEventReader<PDEStreamDataProvider> implements PDEConstants {
	private static final Pattern META_DEFINITION_PATTERN = Pattern.compile("(\\d+)\\s+\\\"([^\\\"]*)\\\"\\s+(\\w+)\\s*");
	
	
//	public static void main(String[] args) {
//		String test = "32 \"testNumber\" NUMBER\n33 \"test File\" FILE\n34 \"testString\" STRING 35 \"start ";
//		Matcher matcher = META_DEFINITION_PATTERN.matcher(test);
//		int offset = 0;
//		while (matcher.find(offset)) {
//			System.out.println(matcher.group());
//			for (int i = 1; i <= matcher.groupCount(); i++) {
//				System.out.println("  '" + matcher.group(i) + "'");
//			}
//			offset = matcher.end();
//		}
//		System.out.println("'" + test.substring(offset) + "'");
//	}
	
	
	public PDEEventReader(File file, ReadWriteParameterMap parameters) throws IOException, XMLStreamException {
		super(file, parameters);
	}

	
	public PDEEventReader(InputStream stream, ReadWriteParameterMap parameters)	throws IOException, XMLStreamException {
		super(stream, parameters);
	}


	public PDEEventReader(Reader reader, ReadWriteParameterMap parameters) throws IOException, XMLStreamException {
		super(reader, parameters);
	}


	public PDEEventReader(XMLEventReader xmlReader,	ReadWriteParameterMap parameters) {
		super(xmlReader, parameters);
	}
	

	@Override
	public String getFormatID() {
		return JPhyloIOFormatIDs.PDE_FORMAT_ID;
	}


	@Override
	protected void fillMap() {
		Map<XMLElementReaderKey, XMLElementReader<PDEStreamDataProvider>> map = getElementReaderMap();
		map.put(new XMLElementReaderKey(TAG_HEADER, TAG_META_TYPE_DEFINITIONS, XMLStreamConstants.CHARACTERS), 
			new AbstractXMLElementReader<PDEStreamDataProvider>() {
				@Override
				public void readEvent(PDEStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
					String data = event.asCharacters().getData();
					if (streamDataProvider.hasLastMetaDefinitionCharacters()) {
						data += streamDataProvider.getLastMetaDefinitionCharacters();
					}
					Matcher matcher = META_DEFINITION_PATTERN.matcher(data);
					int offset = 0;
					while (matcher.find(offset)) {
						long index = Long.parseLong(matcher.group(1));
						streamDataProvider.getMetaColumns().put(index, new PDEMetaColumnDefintion(
								index, matcher.group(2), PDEMetaColumnType.parseColumnType(matcher.group(3))));
						offset = matcher.end();
					}
					
					data = data.substring(offset);  // Can an IndexOutOfBoundsException happen?
					
					if (data.length() > 0) {
						if (streamDataProvider.getXMLReader().peek().isCharacters()) {
							streamDataProvider.setLastMetaDefinitionCharacters(data);
						}
						else {
							throw new JPhyloIOReaderException("Invalid meta column definition in " + TAG_META_TYPE_DEFINITIONS.getLocalPart() 
									+ " tag ending with \"" + data + "\" found.", event.getLocation());  //TODO Shorten data strings which are too long?
						}
					}
					else {
						streamDataProvider.setLastMetaDefinitionCharacters(null);
					}
				}
		});
	}
}
