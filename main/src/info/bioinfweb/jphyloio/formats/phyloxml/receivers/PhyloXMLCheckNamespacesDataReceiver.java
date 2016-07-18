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


import java.io.IOException;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;

import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.CommentEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataContentEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.ResourceMetadataEvent;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.formats.phyloxml.PhyloXMLConstants;
import info.bioinfweb.jphyloio.formats.phyloxml.PhyloXMLWriterStreamDataProvider;
import info.bioinfweb.jphyloio.formats.xml.XMLReadWriteUtils;
import info.bioinfweb.jphyloio.formats.xml.receivers.AbstractXMLDataReceiver;



public class PhyloXMLCheckNamespacesDataReceiver extends AbstractXMLDataReceiver<PhyloXMLWriterStreamDataProvider> implements PhyloXMLConstants {

	
	public PhyloXMLCheckNamespacesDataReceiver(PhyloXMLWriterStreamDataProvider streamDataProvider,
			ReadWriteParameterMap parameterMap) {
		super(streamDataProvider, parameterMap);
	}
	
	
	//TODO move shared code to some kind of superclass shared with NeXML?
	@Override
	protected void handleLiteralMetaStart(LiteralMetadataEvent event) throws IOException, XMLStreamException {
		if (event.getType().getTopologyType().equals(EventTopologyType.START)) {
			QName resourceIdentifier;
			
			if (event.getPredicate().getURI() != null) {
				resourceIdentifier = event.getPredicate().getURI();
				getStreamDataProvider().setNamespacePrefix(XMLReadWriteUtils.getNamespacePrefix(getStreamDataProvider().getWriter(), resourceIdentifier.getPrefix(), 
						resourceIdentifier.getNamespaceURI()), resourceIdentifier.getNamespaceURI());
			}
			else {
				resourceIdentifier = ReadWriteConstants.PREDICATE_HAS_LITERAL_METADATA;
				getStreamDataProvider().setNamespacePrefix(XMLReadWriteUtils.getNamespacePrefix(getStreamDataProvider().getWriter(), resourceIdentifier.getPrefix(), 
						resourceIdentifier.getNamespaceURI()), resourceIdentifier.getNamespaceURI());
			}
			
			//TODO how should the string key be processed? additional attributes may not be allowed
			
			// Original type namespace does not need to be added, since only XSD types are allowed
		}
	}
	

	@Override
	protected void handleLiteralContentMeta(LiteralMetadataContentEvent event) throws IOException, XMLStreamException {
		QName resourceIdentifier;
		
		if (event.hasXMLEventValue()) {
			if (event.getXMLEvent().getEventType() == XMLStreamConstants.START_ELEMENT) {
				StartElement element = event.getXMLEvent().asStartElement();
				resourceIdentifier = element.getName();
				
				getStreamDataProvider().setNamespacePrefix(XMLReadWriteUtils.getNamespacePrefix(getStreamDataProvider().getWriter(), resourceIdentifier.getPrefix(), 
						resourceIdentifier.getNamespaceURI()), resourceIdentifier.getNamespaceURI());
				
				@SuppressWarnings("unchecked")
				Iterator<Attribute> attributesIterator = element.getAttributes();
				while (attributesIterator.hasNext()) {
					Attribute attribute = attributesIterator.next();
					resourceIdentifier = attribute.getName();
					
					getStreamDataProvider().setNamespacePrefix(XMLReadWriteUtils.getNamespacePrefix(getStreamDataProvider().getWriter(), resourceIdentifier.getPrefix(), 
							resourceIdentifier.getNamespaceURI()), resourceIdentifier.getNamespaceURI());
				}				
			}
		}
		
		if ((event.getObjectValue() != null) && (event.getObjectValue() instanceof QName)) {
			QName objectValue = (QName)event.getObjectValue();
			getStreamDataProvider().setNamespacePrefix(XMLReadWriteUtils.getNamespacePrefix(getStreamDataProvider().getWriter(), objectValue.getPrefix(), 
					objectValue.getNamespaceURI()), objectValue.getNamespaceURI());
		}
	}

	
	@Override
	protected void handleResourceMetaStart(ResourceMetadataEvent event) throws IOException, XMLStreamException {
		if (event.getType().getTopologyType().equals(EventTopologyType.START)) {
			QName resourceIdentifier;
			
			if (event.getRel().getURI() != null) {
				resourceIdentifier = event.getRel().getURI();
				getStreamDataProvider().setNamespacePrefix(XMLReadWriteUtils.getNamespacePrefix(getStreamDataProvider().getWriter(), resourceIdentifier.getPrefix(), 
						resourceIdentifier.getNamespaceURI()), resourceIdentifier.getNamespaceURI());
			}
			else {
				resourceIdentifier = ReadWriteConstants.PREDICATE_HAS_LITERAL_METADATA;
				getStreamDataProvider().setNamespacePrefix(XMLReadWriteUtils.getNamespacePrefix(getStreamDataProvider().getWriter(), resourceIdentifier.getPrefix(), 
						resourceIdentifier.getNamespaceURI()), resourceIdentifier.getNamespaceURI());				
			}
			
			//TODO how should the string key be processed? additional attributes may not be allowed
		}
	}

	
	@Override
	protected void handleMetaEndEvent(JPhyloIOEvent event) throws IOException, XMLStreamException {}

	
	@Override
	protected void handleComment(CommentEvent event) throws IOException, XMLStreamException {}	
}
