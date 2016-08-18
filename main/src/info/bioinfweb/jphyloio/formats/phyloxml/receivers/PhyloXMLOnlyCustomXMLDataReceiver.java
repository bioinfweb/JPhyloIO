/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015-2016  Ben St�ver, Sarah Wiechers
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
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataContentEvent;
import info.bioinfweb.jphyloio.events.meta.ResourceMetadataEvent;
import info.bioinfweb.jphyloio.formats.phyloxml.PhyloXMLWriterStreamDataProvider;
import info.bioinfweb.jphyloio.formats.phyloxml.PropertyOwner;
import info.bioinfweb.jphyloio.objecttranslation.ObjectTranslator;

import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;



/**
 * Receiver, that  writes only custom XML, e.g nested under document.
 * If an event has XML content, this is written directly to the file. Simple content is also written in form of custom XML.
 * 
 * @author Sarah Wiechers
 *
 */
public class PhyloXMLOnlyCustomXMLDataReceiver extends PhyloXMLMetaDataReceiver {
	
	
	public PhyloXMLOnlyCustomXMLDataReceiver(PhyloXMLWriterStreamDataProvider streamDataProvider,
			ReadWriteParameterMap parameterMap, PropertyOwner propertyOwner) {
		
		super(streamDataProvider, parameterMap, propertyOwner);
	}
	
	
	@Override
	protected void handleLiteralContentMeta(LiteralMetadataContentEvent event) throws IOException, XMLStreamException {		
		if (isWriteContent()) {
			if (!hasSimpleContent() && event.hasXMLEventValue()) {
				writeCustomXMLTag(event.getXMLEvent());
			}
			else {
				QName datatype = null;
				if (getOriginalType() != null) {
					datatype = getOriginalType().getURI();
				}
				
				ObjectTranslator<?> translator = getParameterMap().getObjectTranslatorFactory().getDefaultTranslatorWithPossiblyInvalidNamespace(datatype);
//				String value = processLiteralContent(event, translator, datatype); 
//				
//				if (value != null) {
//					getStreamDataProvider().getWriter().writeCharacters(value);  // Could be written nested under special meta tag
//				}
				if ((translator != null) && !translator.hasStringRepresentation()) { // Make sure no single character events are written
					translator.writeXMLRepresentation(getStreamDataProvider().getWriter(), event.getObjectValue(), getStreamDataProvider());
				}
				
				getStreamDataProvider().setLiteralContentIsContinued(event.isContinuedInNextEvent());				
			}
		}
	}
	

	@Override
	protected void handleResourceMetaStart(ResourceMetadataEvent event) throws IOException, XMLStreamException {
//		if (determineWriteMeta(event.getID(), event.getRel())) {
//			if (event.getHRef() != null) {
//				getStreamDataProvider().getWriter().writeCharacters(event.getHRef().toString());  // Could be written nested under special meta tag				
//			}
//		}
	}
}
