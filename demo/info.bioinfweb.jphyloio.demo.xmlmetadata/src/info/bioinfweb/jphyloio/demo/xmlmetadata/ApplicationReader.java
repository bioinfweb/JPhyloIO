/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015-2016  Ben Stöver, Sarah Wiechers
 * <http://bioinfweb.info/JPhyloIO>
 * 
 * This file is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This file is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package info.bioinfweb.jphyloio.demo.xmlmetadata;


import info.bioinfweb.jphyloio.JPhyloIOEventReader;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralContentSequenceType;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataEvent;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.utils.JPhyloIOReadingUtils;

import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;



public abstract class ApplicationReader implements IOConstants {
	protected abstract RelatedResource readRelatedResource(JPhyloIOEventReader reader) throws IOException, XMLStreamException;
	
	
	public void read(JPhyloIOEventReader reader, List<RelatedResource> resources) throws IOException, XMLStreamException {
		while (reader.hasNextEvent()) {
			JPhyloIOEvent event = reader.next();	     
			
			if (event.getType().getTopologyType().equals(EventTopologyType.START)) {
				switch (event.getType().getContentType()) { 
					case DOCUMENT:
						if (event.getType().getTopologyType().equals(EventTopologyType.START)) {
							resources.clear();  // Remove possible previous data from the model instance.
						}
						break;
	
					case LITERAL_META:
						if (event.getType().getTopologyType().equals(EventTopologyType.START)) {  // There can be a document start and a document end event.
							LiteralMetadataEvent literalEvent = event.asLiteralMetadataEvent();
							if (PREDICATE_RELATED_REFERENCE.equals(literalEvent.getPredicate().getURI()) 
									&& literalEvent.getSequenceType().equals(LiteralContentSequenceType.XML)) {
								
								resources.add(readRelatedResource(reader));
							}
						}
						break;
	
					default:
						JPhyloIOReadingUtils.reachElementEnd(reader);
						break;
				}
			}
		}
	}
}
