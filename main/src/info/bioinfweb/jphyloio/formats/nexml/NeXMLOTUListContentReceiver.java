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


import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.dataadapters.implementations.receivers.AbstractEventReceiver;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataContentEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.ResourceMetadataEvent;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;



public class NeXMLOTUListContentReceiver extends AbstractEventReceiver<XMLStreamWriter> implements NeXMLConstants {
	
	
	
	public NeXMLOTUListContentReceiver(XMLStreamWriter writer, ReadWriteParameterMap parameterMap) {
		super(writer, parameterMap);
	}
	
	
	private void writeResourceMeta(ResourceMetadataEvent event) throws XMLStreamException {
		getWriter().writeEmptyElement(TAG_META.getLocalPart());
		
		
		getWriter().writeAttribute(ATTR_REL.getLocalPart(), event.getRel().getLocalPart());
		if (event.getRel() != null) {
			getWriter().writeAttribute(ATTR_HREF.getLocalPart(), event.getHRef().toString());
		}
		
		getWriter().writeAttribute(ATTR_TYPE.getLocalPart(), event.getRel().getLocalPart());
	}
	
	
	private void writeLiteralMeta(LiteralMetadataEvent event) {
		
	}
	
	
	private void writeLiteralMetaContent(LiteralMetadataContentEvent event) {
		
	}
	
	
	private void writeOTUTag(LabeledIDEvent event) throws XMLStreamException { //TODO use method from NeXMLEventWriter to write attributes
		getWriter().writeEmptyElement(TAG_OTU.getLocalPart());
		
//		writeLabeledIDAttributes(event);
		
		getWriter().writeAttribute(ATTR_ID.getLocalPart(), event.getID());  //TODO Add ID to set to ensure all IDs are unique.
		getWriter().writeAttribute(ATTR_ABOUT.getLocalPart(), "#" + event.getID());
		if (event.hasLabel()) {
			getWriter().writeAttribute(ATTR_LABEL.getLocalPart(), event.getLabel());
		}
	}
	

	@Override
	public boolean add(JPhyloIOEvent event) throws Exception {
		switch (event.getType().getContentType()) {
			case OTU:
				if (event.getType().getTopologyType().equals(EventTopologyType.START)) {
					writeOTUTag(event.asLabeledIDEvent());
				}  // End events can be ignored.
				break;
			case META_RESOURCE:
				if (event.getType().getTopologyType().equals(EventTopologyType.START)) {
					writeResourceMeta(event.asResourceMetadataEvent());
				}				
				break;
			case META_LITERAL:
				if (event.getType().getTopologyType().equals(EventTopologyType.START)) {
					writeLiteralMeta(event.asLiteralMetadataEvent());
				}		
				break;
			case META_LITERAL_CONTENT:				
				writeLiteralMetaContent(event.asLiteralMetadataContentEvent());				
				break;
			default:
				break;
		}
		return true;
	}
}
