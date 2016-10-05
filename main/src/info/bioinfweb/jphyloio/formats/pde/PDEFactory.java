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


import info.bioinfweb.jphyloio.JPhyloIOEventReader;
import info.bioinfweb.jphyloio.JPhyloIOEventWriter;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.ReadWriteParameterNames;
import info.bioinfweb.jphyloio.events.meta.LiteralContentSequenceType;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.formatinfo.DefaultFormatInfo;
import info.bioinfweb.jphyloio.formatinfo.JPhyloIOFormatInfo;
import info.bioinfweb.jphyloio.formatinfo.MetadataModeling;
import info.bioinfweb.jphyloio.formatinfo.MetadataTopologyType;
import info.bioinfweb.jphyloio.formats.JPhyloIOFormatIDs;
import info.bioinfweb.jphyloio.formats.xml.AbstractXMLFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamException;



/**
 * Reader and writer factory for the <a href="http://bioinfweb.info/xmlns/xtg">XTG</a> format.
 * 
 * @author Ben St&ouml;ver
 * @since 0.0.0
 */
public class PDEFactory extends AbstractXMLFactory implements PDEConstants, JPhyloIOFormatIDs {
	public PDEFactory() {
		super(TAG_ROOT);
	}


	@Override
	public JPhyloIOEventReader getReader(InputStream stream, ReadWriteParameterMap parameters) throws IOException, XMLStreamException {
		return new PDEEventReader(stream, parameters);
	}

	
	@Override
	public JPhyloIOEventReader getReader(Reader reader, ReadWriteParameterMap parameters) throws IOException, XMLStreamException {
		return new PDEEventReader(reader, parameters);
	}

	
	@Override
	public JPhyloIOEventWriter getWriter() {
		return null;
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
	protected JPhyloIOFormatInfo createFormatInfo() {
		Map<EventContentType, MetadataModeling> supportedMetadataModeling = new EnumMap<EventContentType, MetadataModeling>(EventContentType.class);
		supportedMetadataModeling.put(EventContentType.DOCUMENT, new MetadataModeling(MetadataTopologyType.FULL_TREE, 
				EnumSet.of(LiteralContentSequenceType.SIMPLE)));
		supportedMetadataModeling.put(EventContentType.META_RESOURCE, new MetadataModeling(MetadataTopologyType.NONE, 
				Collections.<LiteralContentSequenceType>emptySet()));
		supportedMetadataModeling.put(EventContentType.META_LITERAL, new MetadataModeling(MetadataTopologyType.NONE, 
				Collections.<LiteralContentSequenceType>emptySet()));
		supportedMetadataModeling.put(EventContentType.META_LITERAL_CONTENT, new MetadataModeling(MetadataTopologyType.NONE, 
				Collections.<LiteralContentSequenceType>emptySet()));
		supportedMetadataModeling.put(EventContentType.COMMENT, new MetadataModeling(MetadataTopologyType.NONE, 
				Collections.<LiteralContentSequenceType>emptySet()));
		supportedMetadataModeling.put(EventContentType.OTU_LIST, new MetadataModeling(MetadataTopologyType.NONE, 
				Collections.<LiteralContentSequenceType>emptySet()));
		supportedMetadataModeling.put(EventContentType.OTU, new MetadataModeling(MetadataTopologyType.NONE, 
				Collections.<LiteralContentSequenceType>emptySet()));
		supportedMetadataModeling.put(EventContentType.ALIGNMENT, new MetadataModeling(MetadataTopologyType.NONE, 
				Collections.<LiteralContentSequenceType>emptySet()));
		supportedMetadataModeling.put(EventContentType.SEQUENCE, new MetadataModeling(MetadataTopologyType.FULL_TREE, 
				EnumSet.of(LiteralContentSequenceType.SIMPLE)));
		supportedMetadataModeling.put(EventContentType.SEQUENCE_TOKENS, new MetadataModeling(MetadataTopologyType.NONE, 
				Collections.<LiteralContentSequenceType>emptySet()));
		supportedMetadataModeling.put(EventContentType.TOKEN_SET_DEFINITION, new MetadataModeling(MetadataTopologyType.NONE, 
				Collections.<LiteralContentSequenceType>emptySet()));
		supportedMetadataModeling.put(EventContentType.CHARACTER_SET, new MetadataModeling(MetadataTopologyType.FULL_TREE, 
				EnumSet.of(LiteralContentSequenceType.SIMPLE)));
		supportedMetadataModeling.put(EventContentType.CHARACTER_SET_INTERVAL, new MetadataModeling(MetadataTopologyType.NONE, 
				Collections.<LiteralContentSequenceType>emptySet()));
		
		Set<String> supportedParameters = new HashSet<String>();
		supportedParameters.add(ReadWriteParameterNames.KEY_ALLOW_DEFAULT_NAMESPACE);
		//TODO match tokens?
		
		return new DefaultFormatInfo(this, PDE_FORMAT_ID, PDE_FORMAT_NAME, 
				EnumSet.of(EventContentType.DOCUMENT, EventContentType.META_RESOURCE, EventContentType.META_LITERAL, 
						EventContentType.META_LITERAL_CONTENT, EventContentType.COMMENT, EventContentType.OTU_LIST, 
						EventContentType.OTU, EventContentType.ALIGNMENT, EventContentType.SEQUENCE,
						EventContentType.SEQUENCE_TOKENS, EventContentType.TOKEN_SET_DEFINITION, EventContentType.CHARACTER_SET, 
						EventContentType.CHARACTER_SET_INTERVAL),
				null, supportedMetadataModeling, null,
				supportedParameters, null,
				new ReadWriteParameterMap(),	"PDE format of PhyDE", "pde");
	}
}
