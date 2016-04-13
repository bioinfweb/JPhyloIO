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
package info.bioinfweb.jphyloio.formats.nexml.nexmlreceivers;


import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.CommentEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataContentEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.ResourceMetadataEvent;
import info.bioinfweb.jphyloio.formats.nexml.NeXMLWriterStreamDataProvider;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;



public class NeXMLSequenceTokensReceiver extends NeXMLHandleSequenceDataReceiver {


	public NeXMLSequenceTokensReceiver(XMLStreamWriter writer, ReadWriteParameterMap parameterMap, boolean longTokens,
			NeXMLWriterStreamDataProvider streamDataProvider) {
		super(writer, parameterMap, longTokens, streamDataProvider);
	}


	@Override
	protected void handleLiteralMetaStart(LiteralMetadataEvent event) throws IOException, XMLStreamException {
		if (isNestedUnderSingleToken()) {
			AbstractNeXMLDataReceiverMixin.handleLiteralMeta(getStreamDataProvider(), event);
		}
	}


	@Override
	protected void handleLiteralContentMeta(LiteralMetadataContentEvent event) throws IOException, XMLStreamException {
		if (isNestedUnderSingleToken()) {
			AbstractNeXMLDataReceiverMixin.handleLiteralContentMeta(getStreamDataProvider(), event);
		}
	}


	@Override
	protected void handleResourceMetaStart(ResourceMetadataEvent event) throws IOException, XMLStreamException {
		if (isNestedUnderSingleToken()) {
			AbstractNeXMLDataReceiverMixin.handleResourceMeta(getStreamDataProvider(), event);
		}
	}
	
	
	@Override
	protected void handleMetaEndEvent(JPhyloIOEvent event) throws IOException, XMLStreamException {
		if (isNestedUnderSingleToken()) {
			AbstractNeXMLDataReceiverMixin.handleMetaEndEvent(getStreamDataProvider(), event);
		}
	}


	@Override
	protected void handleComment(CommentEvent event) throws IOException, XMLStreamException { //TODO write if parent was null?
		if (!isNestedUnderSingleToken()) {
			AbstractNeXMLDataReceiverMixin.handleComment(getStreamDataProvider(), event);
		}
	}


	@Override
	protected void handleToken(String token, String label) throws XMLStreamException {
		String translatedToken = getStreamDataProvider().getTokenTranslationMap().get(token);
		
		if (getStreamDataProvider().isWriteCellsTags()) {
			getWriter().writeStartElement(TAG_CELL.getLocalPart());
			if (label != null) {
				getWriter().writeAttribute(ATTR_LABEL.getLocalPart(), label);
			}
			getStreamDataProvider().setSingleToken(translatedToken);
		}		
		else {
			getWriter().writeCharacters(translatedToken);
			if (isLongTokens()) {
				getWriter().writeCharacters(" ");
			}
		}			
	}


	@Override
	protected void handleTokenEnd() throws XMLStreamException {
		if (getStreamDataProvider().isWriteCellsTags()) {
			String token = getStreamDataProvider().getSingleToken();			
			if (token != null) {
				getWriter().writeCharacters(token);
				getStreamDataProvider().setSingleToken(null);
			}			
			getWriter().writeEndElement();
		}		
	}
}
