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


import java.io.IOException;

import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.CommentEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataContentEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.ResourceMetadataEvent;
import info.bioinfweb.jphyloio.formats.nexml.NeXMLEventReader;
import info.bioinfweb.jphyloio.formats.nexml.NeXMLEventWriter;
import info.bioinfweb.jphyloio.formats.nexml.NeXMLWriterStreamDataProvider;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;



public class NeXMLIgnoreCertainMetadataReceiver extends NeXMLPredicateMetaReceiver {
	private boolean writePredicateMetadata;
	
	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param writer the XML writer of the calling {@link NeXMLEventWriter}
	 * @param parameterMap the parameter map of the calling {@link NeXMLEventWriter}
	 * @param streamDataProvider the stream data provider of the calling {@link NeXMLEventReader}
	 * @param writePredicateMetadata specify {code true} here if only metadata nested under the specified predicates should be written 
	 * or {@code false} if metadata under these predicates should be ignored
	 * @param predicates the predicates to be processed in a special way
	 */
	public NeXMLIgnoreCertainMetadataReceiver(XMLStreamWriter writer, ReadWriteParameterMap parameterMap,
			NeXMLWriterStreamDataProvider streamDataProvider, boolean writePredicateMetadata, QName... predicates) {
		
		super(writer, parameterMap, streamDataProvider, predicates);
		this.writePredicateMetadata = writePredicateMetadata;		
	}
	
	
	@Override
	protected void handleLiteralMetaStart(LiteralMetadataEvent event) throws IOException, XMLStreamException {
		if (isUnderPredicate()) {
			changeMetaLevel(1);
		}
		
		if ((isUnderPredicate() && writePredicateMetadata) || (!isUnderPredicate() && !writePredicateMetadata)) {
			super.handleLiteralMetaStart(event);
		}
	}


	@Override
	protected void handleLiteralContentMeta(LiteralMetadataContentEvent event) throws IOException, XMLStreamException {
		if ((isUnderPredicate() && writePredicateMetadata) || (!isUnderPredicate() && !writePredicateMetadata)) {
			super.handleLiteralContentMeta(event);
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
			if (writePredicateMetadata) {
				super.handleResourceMetaStart(event);
			}
		}
		else if (!isUnderPredicate() && !writePredicateMetadata) {
			super.handleResourceMetaStart(event);
		}
	}


	@Override
	protected void handleComment(CommentEvent event) throws IOException, XMLStreamException {
		if ((isUnderPredicate() && writePredicateMetadata) || (!isUnderPredicate() && !writePredicateMetadata)) {
			super.handleComment(event);
		}		
	}


	@Override
	protected void handleMetaEndEvent(JPhyloIOEvent event) throws IOException, XMLStreamException {
		if ((isUnderPredicate() && writePredicateMetadata) || (!isUnderPredicate() && !writePredicateMetadata)) {			
			super.handleMetaEndEvent(event);
		}		
		
		if (isUnderPredicate()) {
			changeMetaLevel(-1);
			if (getMetaLevel() == 0) {
				setUnderPredicate(false);
			}
		}
	}
}
