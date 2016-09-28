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
package info.bioinfweb.jphyloio.formats.nexml;


import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import info.bioinfweb.jphyloio.JPhyloIOEventReader;
import info.bioinfweb.jphyloio.JPhyloIOEventWriter;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.type.EventContentType;
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
public class NeXMLFactory extends AbstractXMLFactory implements NeXMLConstants, JPhyloIOFormatIDs {
	public NeXMLFactory() {
		super(TAG_ROOT);
	}


	@Override
	public JPhyloIOEventReader getReader(InputStream stream, ReadWriteParameterMap parameters) throws IOException, XMLStreamException {
		return new NeXMLEventReader(stream, parameters);
	}

	
	@Override
	public JPhyloIOEventReader getReader(Reader reader, ReadWriteParameterMap parameters) throws IOException, XMLStreamException {
		return new NeXMLEventReader(reader, parameters);
	}

	
	@Override
	public JPhyloIOEventWriter getWriter() {
		return new NeXMLEventWriter();
	}

	
	@Override
	public boolean hasReader() {
		return true;
	}

	
	@Override
	public boolean hasWriter() {
		return true;
	}

	
	@Override
	protected JPhyloIOFormatInfo createFormatInfo() {
		Set<EventContentType> supportedContentTypes = EnumSet.of(EventContentType.DOCUMENT, EventContentType.META_RESOURCE, 
				EventContentType.META_LITERAL, EventContentType.META_LITERAL_CONTENT, EventContentType.COMMENT, 
				EventContentType.OTU_LIST, EventContentType.OTU, EventContentType.OTU_SET, EventContentType.ALIGNMENT, 
				EventContentType.CHARACTER_DEFINITION, EventContentType.SEQUENCE,	EventContentType.SEQUENCE_TOKENS, 
				EventContentType.SINGLE_SEQUENCE_TOKEN, EventContentType.TREE_NETWORK_GROUP, EventContentType.TREE, 
				EventContentType.NETWORK, EventContentType.NODE, EventContentType.EDGE, EventContentType.ROOT_EDGE, 
				EventContentType.TOKEN_SET_DEFINITION, EventContentType.SINGLE_TOKEN_DEFINITION, EventContentType.CHARACTER_SET, 
				EventContentType.CHARACTER_SET_INTERVAL, EventContentType.SET_ELEMENT, EventContentType.OTU_SET, 
				EventContentType.SEQUENCE_SET, EventContentType.TREE_NETWORK_SET, EventContentType.NODE_EDGE_SET);
		
		return new DefaultFormatInfo(this, NEXML_FORMAT_ID, NEXML_FORMAT_NAME, 
				supportedContentTypes, supportedContentTypes,	EnumSet.noneOf(EventContentType.class),
				Collections.<String>emptySet(), Collections.<String>emptySet(),
				new ReadWriteParameterMap(),	"NeXML", "nexml",  "xml");
	}
}
