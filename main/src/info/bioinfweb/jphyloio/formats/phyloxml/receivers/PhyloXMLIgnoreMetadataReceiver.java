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
package info.bioinfweb.jphyloio.formats.phyloxml.receivers;


import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataContentEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.ResourceMetadataEvent;
import info.bioinfweb.jphyloio.formats.phyloxml.PhyloXMLWriterStreamDataProvider;
import info.bioinfweb.jphyloio.formats.phyloxml.PropertyOwner;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;



public class PhyloXMLIgnoreMetadataReceiver extends PhyloXMLMetaDataReceiver {
	private boolean ignoreCustomXML;
	private boolean hasMetadata = false;
	
	
	public PhyloXMLIgnoreMetadataReceiver(PhyloXMLWriterStreamDataProvider streamDataProvider,
			ReadWriteParameterMap parameterMap, PropertyOwner propertyOwner, boolean ignoreCustomXML) {
		super(streamDataProvider, parameterMap, propertyOwner);
		
		this.ignoreCustomXML = ignoreCustomXML;
	}


	public boolean hasMetadata() {
		return hasMetadata;
	}


	@Override
	protected void handleLiteralMetaStart(LiteralMetadataEvent event) throws IOException, XMLStreamException {
		hasMetadata = true;
		
		if (!ignoreCustomXML) {
			super.handleLiteralMetaStart(event);
		}
	}

	
	@Override
	protected void handleLiteralContentMeta(LiteralMetadataContentEvent event) throws IOException, XMLStreamException {
		hasMetadata = true;
		
		if (!ignoreCustomXML && !hasSimpleContent() && event.hasXMLEventValue()) {			
			writeCustomXMLTag(event.getXMLEvent());			
		}
	}
	

	@Override
	protected void handleResourceMetaStart(ResourceMetadataEvent event) throws IOException, XMLStreamException {
		hasMetadata = true;
	}

	
	@Override
	protected void handleMetaEndEvent(JPhyloIOEvent event) throws IOException, XMLStreamException {
		hasMetadata = true;
	}
}
