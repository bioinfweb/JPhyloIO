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
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;



public class NeXMLPredicateMetaReceiver extends NeXMLMetaDataReceiver {
	private List<QName> predicates = new ArrayList<QName>();
	private boolean writePredicateMetadata;
	private boolean isUnderPredicate;
	private long metaLevel = 0;

	
	public NeXMLPredicateMetaReceiver(XMLStreamWriter writer, ReadWriteParameterMap parameterMap, NeXMLWriterStreamDataProvider streamDataProvider, 
			boolean writePredicateMetadata, QName... predicates) {
		
		super(writer, parameterMap, streamDataProvider);
		
		for (int i = 0; i < predicates.length; i++) {
			this.predicates.add(predicates[i]);
		}
		
		this.writePredicateMetadata = writePredicateMetadata;
	}


	@Override
	protected void handleLiteralMetaStart(LiteralMetadataEvent event) throws IOException, XMLStreamException {
		if (isUnderPredicate) {
			metaLevel++;
		}
		
		if ((isUnderPredicate && writePredicateMetadata) || (!isUnderPredicate && !writePredicateMetadata)) {
			super.handleLiteralMetaStart(event);
		}		
	}


	@Override
	protected void handleLiteralContentMeta(LiteralMetadataContentEvent event) throws IOException, XMLStreamException {
		if ((isUnderPredicate && writePredicateMetadata) || (!isUnderPredicate && !writePredicateMetadata)) {
			super.handleLiteralContentMeta(event);
		}
	}


	@Override
	protected void handleResourceMetaStart(ResourceMetadataEvent event) throws IOException, XMLStreamException {
		if (!isUnderPredicate) {
			if (event.getRel().getURI() != null) {				
				isUnderPredicate = predicates.contains(event.getRel().getURI());
			}
		}
		
		if (isUnderPredicate) {
			metaLevel++;
			if (writePredicateMetadata) {
				super.handleResourceMetaStart(event);
			}
		}
		else if (!isUnderPredicate && !writePredicateMetadata) {
			super.handleResourceMetaStart(event);
		}
	}


	@Override
	protected void handleComment(CommentEvent event) throws IOException, XMLStreamException {
		if ((isUnderPredicate && writePredicateMetadata) || (!isUnderPredicate && !writePredicateMetadata)) {
			super.handleComment(event);
		}		
	}


	@Override
	protected void handleMetaEndEvent(JPhyloIOEvent event) throws IOException, XMLStreamException {
		if ((isUnderPredicate && writePredicateMetadata) || (!isUnderPredicate && !writePredicateMetadata)) {			
			super.handleMetaEndEvent(event);
		}		
		
		if (isUnderPredicate) {
			metaLevel--;
			if (metaLevel == 0) {
				isUnderPredicate = false;
			}
		}
	}
}
