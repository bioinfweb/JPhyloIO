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
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataContentEvent;
import info.bioinfweb.jphyloio.events.meta.ResourceMetadataEvent;
import info.bioinfweb.jphyloio.exception.JPhyloIOWriterException;
import info.bioinfweb.jphyloio.formats.phyloxml.PhyloXMLWriterStreamDataProvider;
import info.bioinfweb.jphyloio.formats.phyloxml.PropertyOwner;
import info.bioinfweb.jphyloio.objecttranslation.ObjectTranslator;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;



public class PhyloXMLDocumentMetaDataReceiver extends PhyloXMLMetaDataReceiver {

	
	public PhyloXMLDocumentMetaDataReceiver(PhyloXMLWriterStreamDataProvider streamDataProvider,
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
				ObjectTranslator<?> translator = getParameterMap().getObjectTranslatorFactory()
						.getDefaultTranslatorWithPossiblyInvalidNamespace(getOriginalType().getURI());
				String value = null;
				
				if ((event.getObjectValue() != null)) {
					if ((translator != null)) {
						if (translator.hasStringRepresentation()) {
							try {
								value = translator.javaToRepresentation(event.getObjectValue(), getStreamDataProvider());
							}
							catch (ClassCastException e) {
								throw new JPhyloIOWriterException("The original type of the object declared in this event did not match the actual object type. "
										+ "Therefore it could not be parsed.");
							}							
						}
						else {
							translator.writeXMLRepresentation(getStreamDataProvider().getWriter(), event.getObjectValue(), getStreamDataProvider());
						}
					}
					else if (event.getStringValue() != null) {
						value = event.getStringValue();
					}
					else {
						value = event.getObjectValue().toString();
					}
				}
				else {
					value = event.getStringValue();
				}
				
				if (value != null) {
					getStreamDataProvider().getWriter().writeCharacters(value); //TODO nest under according tag (meta?)
				}
				
				getStreamDataProvider().setLiteralContentIsContinued(event.isContinuedInNextEvent());				
			}
		}
	}
	

	@Override
	protected void handleResourceMetaStart(ResourceMetadataEvent event) throws IOException, XMLStreamException {
		if (determineWriteMeta(event.getID(), event.getRel())) {
			if (event.getHRef() != null) {
				getStreamDataProvider().getWriter().writeCharacters(event.getHRef().toString());				
			}
		}
	}
}
