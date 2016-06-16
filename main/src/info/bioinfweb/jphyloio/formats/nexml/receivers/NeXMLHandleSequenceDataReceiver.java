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
import info.bioinfweb.jphyloio.dataadapters.implementations.receivers.AbstractSequenceContentReceiver;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.SingleSequenceTokenEvent;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.formats.nexml.NeXMLConstants;
import info.bioinfweb.jphyloio.formats.nexml.NeXMLWriterAlignmentInformation;
import info.bioinfweb.jphyloio.formats.nexml.NeXMLWriterStreamDataProvider;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;



public abstract class NeXMLHandleSequenceDataReceiver extends AbstractSequenceContentReceiver<XMLStreamWriter> implements NeXMLConstants {
	private boolean nestedUnderSingleToken = false;
	private NeXMLWriterStreamDataProvider streamDataProvider;
	NeXMLWriterAlignmentInformation alignmentInfo;


	public NeXMLHandleSequenceDataReceiver(XMLStreamWriter writer, ReadWriteParameterMap parameterMap,
			boolean longTokens, NeXMLWriterStreamDataProvider streamDataProvider) {
		super(writer, parameterMap, longTokens);
		
		this.streamDataProvider = streamDataProvider;
		this.alignmentInfo = streamDataProvider.getCurrentAlignmentInfo();
	}


	public boolean isNestedUnderSingleToken() {
		return nestedUnderSingleToken;
	}


	public void setNestedUnderSingleToken(boolean nestedUnderSingleToken) {
		this.nestedUnderSingleToken = nestedUnderSingleToken;
	}
	
	
	public NeXMLWriterStreamDataProvider getStreamDataProvider() {
		return streamDataProvider;
	}
	
	
	protected void handleTokenEnd() throws XMLStreamException {}
	
	
	@Override
	protected boolean doAdd(JPhyloIOEvent event) throws IOException, XMLStreamException {
		switch (event.getType().getContentType()) {
			case SINGLE_SEQUENCE_TOKEN:
				if (event.getType().getTopologyType().equals(EventTopologyType.START)) {
					SingleSequenceTokenEvent tokenEvent = event.asSingleSequenceTokenEvent();
					handleToken(tokenEvent.getToken(), tokenEvent.getLabel());		
					
					setNestedUnderSingleToken(true);
					
					if (tokenEvent.getLabel() != null) {
						alignmentInfo.setWriteCellsTags(true);
					}
				}
				else {
					handleTokenEnd();
					setNestedUnderSingleToken(false);
				}
				break;
			case SEQUENCE_TOKENS:
				for (String token : event.asSequenceTokensEvent().getCharacterValues()) {
					handleToken(token, null);
					handleTokenEnd();
				}
				break;
			default:
				break;
		}
		return true;
	}
}
