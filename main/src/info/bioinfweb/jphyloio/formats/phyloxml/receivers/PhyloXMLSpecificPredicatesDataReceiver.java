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
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataContentEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.ResourceMetadataEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.formats.phyloxml.PhyloXMLWriterStreamDataProvider;
import info.bioinfweb.jphyloio.formats.phyloxml.PropertyOwner;

import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;



public class PhyloXMLSpecificPredicatesDataReceiver extends PhyloXMLMetaDataReceiver {
	private QName parentPredicate;
//	private int runCount;
	private QName currentElementPredicate;
	private QName currentLiteralPredicate;
	private boolean writeLiteralEnd = false;
	private boolean writeResourceEnd = false;
	

	public PhyloXMLSpecificPredicatesDataReceiver(PhyloXMLWriterStreamDataProvider streamDataProvider,
			ReadWriteParameterMap parameterMap, PropertyOwner propertyOwner, QName parentPredicate) {
		super(streamDataProvider, parameterMap, propertyOwner);
		this.parentPredicate = parentPredicate;
//		runCount = 0;
		this.currentElementPredicate = parentPredicate;
	}


	@Override
	protected void handleLiteralMetaStart(LiteralMetadataEvent event) throws IOException, XMLStreamException {
		if (getStreamDataProvider().getPredicateInfoMap().get(currentElementPredicate).getAllowedChildren().contains(event.getPredicate().getURI())) { //TODO check correct order
			getStreamDataProvider().getMetaIDs().remove(event.getID());		
			
			switch (getStreamDataProvider().getPredicateInfoMap().get(event.getPredicate().getURI()).getTreatment()) {
				case TAG_AND_VALUE:
					QName tagName = getStreamDataProvider().getPredicateInfoMap().get(event.getPredicate().getURI()).getTranslation();
					getStreamDataProvider().getWriter().writeStartElement(tagName.getNamespaceURI(), tagName.getLocalPart());
					currentElementPredicate = event.getPredicate().getURI();
					writeLiteralEnd = true;
				case ATTRIBUTE:					
				case VALUE:
					currentLiteralPredicate = event.getPredicate().getURI();
					break;
				default:
					break;
			}
		}
	}


	@Override
	protected void handleLiteralContentMeta(LiteralMetadataContentEvent event) throws IOException, XMLStreamException {
		if (currentLiteralPredicate != null) {
			switch (getStreamDataProvider().getPredicateInfoMap().get(currentLiteralPredicate).getTreatment()) {
				case TAG_AND_VALUE:					
				case VALUE:
					getStreamDataProvider().getWriter().writeCharacters(event.getStringValue()); //TODO maybe use object value instead?
					break;
				case ATTRIBUTE:					
					QName attribute = getStreamDataProvider().getPredicateInfoMap().get(currentLiteralPredicate).getTranslation();
					getStreamDataProvider().getWriter().writeAttribute(attribute.getPrefix(), attribute.getNamespaceURI(), attribute.getLocalPart(), event.getStringValue());
					break;
				default:
					break;
			}
		}
	}


	@Override
	protected void handleResourceMetaStart(ResourceMetadataEvent event) throws IOException, XMLStreamException {
		if (getStreamDataProvider().getPredicateInfoMap().get(currentElementPredicate).getAllowedChildren().contains(event.getRel().getURI())) { //TODO check correct order
			getStreamDataProvider().getMetaIDs().remove(event.getID());
			
			switch (getStreamDataProvider().getPredicateInfoMap().get(event.getRel().getURI()).getTreatment()) {
				case TAG:
					QName tagName = getStreamDataProvider().getPredicateInfoMap().get(event.getRel().getURI()).getTranslation();
					getStreamDataProvider().getWriter().writeStartElement(tagName.getNamespaceURI(), tagName.getLocalPart());
					currentElementPredicate = event.getRel().getURI();
					writeResourceEnd = true;
					break;
				default:
					break;
			}
		}
	}


	@Override
	protected void handleMetaEndEvent(JPhyloIOEvent event) throws IOException, XMLStreamException {
		if (writeLiteralEnd && event.getType().getContentType().equals(EventContentType.META_LITERAL)) {
			getStreamDataProvider().getWriter().writeEndElement();
			writeLiteralEnd = false;
		}
		else if (writeResourceEnd && event.getType().getContentType().equals(EventContentType.META_RESOURCE)) {
			getStreamDataProvider().getWriter().writeEndElement();
			writeLiteralEnd = false;
		}
		
		currentLiteralPredicate = null;
	}
	
	
//	public void increaseRunCount() {
//		runCount++;
//		currentElementPredicate = parentPredicate;
//	}
}
