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
package info.bioinfweb.jphyloio.formats.nexml.receivers;


import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.CommentEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataContentEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.ResourceMetadataEvent;
import info.bioinfweb.jphyloio.formats.nexml.NeXMLWriterStreamDataProvider;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;



public class NeXMLAttributeMetadataReceiver extends NeXMLPredicateMetaReceiver {
	private Map<QName, String> attributeToValueMap = new HashMap<QName, String>();
	private QName currentAttributeName;

	
	public NeXMLAttributeMetadataReceiver(XMLStreamWriter writer, ReadWriteParameterMap parameterMap,
			NeXMLWriterStreamDataProvider streamDataProvider, QName... predicates) {
		super(writer, parameterMap, streamDataProvider, predicates);
	}
	
	
	public Map<QName, String> getAttributeToValueMap() {
		return attributeToValueMap;
	}


	@Override
	protected void handleLiteralMetaStart(LiteralMetadataEvent event) throws IOException, XMLStreamException {
		if (isUnderPredicate()) {
			changeMetaLevel(1);
			currentAttributeName = event.getPredicate().getURI();
		}		
	}


	@Override
	protected void handleLiteralContentMeta(LiteralMetadataContentEvent event) throws IOException, XMLStreamException {
		if (isUnderPredicate()) {
			attributeToValueMap.put(currentAttributeName, event.getStringValue());
		}
	}


	@Override
	protected void handleResourceMetaStart(ResourceMetadataEvent event) throws IOException, XMLStreamException {
		if (!isUnderPredicate()) {
			if (event.getRel().getURI() != null) {				
				setUnderPredicate(getPredicates().contains(event.getRel().getURI()));
			}
		}
		
		if (isUnderPredicate()) {
			changeMetaLevel(1);			
		}		
	}


	@Override
	protected void handleComment(CommentEvent event) throws IOException, XMLStreamException {}


	@Override
	protected void handleMetaEndEvent(JPhyloIOEvent event) throws IOException, XMLStreamException {		
		if (isUnderPredicate()) {
			changeMetaLevel(-1);
			if (getMetaLevel() == 0) {
				setUnderPredicate(false);
			}
		}
	}
}
