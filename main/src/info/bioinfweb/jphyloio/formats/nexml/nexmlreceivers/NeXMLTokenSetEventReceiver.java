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


import info.bioinfweb.commons.bio.CharacterSymbolMeaning;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.SingleTokenDefinitionEvent;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.exception.JPhyloIOWriterException;
import info.bioinfweb.jphyloio.formats.nexml.NeXMLWriterStreamDataProvider;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;



public class NeXMLTokenSetEventReceiver extends NeXMLMetaDataReceiver {
	private int tokenDefinitionIndex = 0;
	private Map<String, String> tokenNametoIDMap = new TreeMap<String, String>();
	
	public NeXMLTokenSetEventReceiver(XMLStreamWriter writer, ReadWriteParameterMap parameterMap,
			NeXMLWriterStreamDataProvider streamDataProvider) {
		super(writer, parameterMap, streamDataProvider);
	}
	
	
	private void writeTokenDefinitionAttributes(SingleTokenDefinitionEvent event) throws XMLStreamException, JPhyloIOWriterException {
		getStreamDataProvider().writeLabeledIDAttributes(event);
		getWriter().writeAttribute(ATTR_SYMBOL.getLocalPart(), "" + tokenDefinitionIndex);
		
		tokenDefinitionIndex++;		
		tokenNametoIDMap.put(event.getTokenName(), event.getID());
	}
	
	
	private void writeState(SingleTokenDefinitionEvent event) throws XMLStreamException, JPhyloIOWriterException {
		getWriter().writeEmptyElement(TAG_STATE.getLocalPart());
		writeTokenDefinitionAttributes(event);
	}
	
	
	private void writeStateSet(SingleTokenDefinitionEvent event, boolean isPolymorphic) throws XMLStreamException, JPhyloIOWriterException {
		if (isPolymorphic) {
			getWriter().writeStartElement(TAG_POLYMORPHIC.getLocalPart());
		}
		else {
			getWriter().writeStartElement(TAG_UNCERTAIN.getLocalPart());
		}
		
		writeTokenDefinitionAttributes(event);
		
		for (String tokenDefinition : event.getConstituents()) {
			getWriter().writeEmptyElement(TAG_MEMBER.getLocalPart());
			getWriter().writeAttribute(ATTR_STATE.getLocalPart(), tokenNametoIDMap.get(tokenDefinition)); //TODO make sure this is a valid state ID
		}
		
		getWriter().writeEndElement();
	}
	

	@Override
	protected boolean doAdd(JPhyloIOEvent event) throws IOException, XMLStreamException {
		switch (event.getType().getContentType()) {
			case SINGLE_TOKEN_DEFINITION:
				if (event.getType().getTopologyType().equals(EventTopologyType.START)) {
					SingleTokenDefinitionEvent singleTokenEvent = event.asSingleTokenDefinitionEvent();
					if (!singleTokenEvent.getMeaning().equals(CharacterSymbolMeaning.MATCH) 
							&& !singleTokenEvent.getMeaning().equals(CharacterSymbolMeaning.OTHER)) {
						switch (singleTokenEvent.getTokenType()) {
							case ATOMIC_STATE:
								writeState(singleTokenEvent); //TODO if its a gap character write uncertain set
								break;
							case POLYMORPHIC:
								writeStateSet(singleTokenEvent, true);
								break;
							case UNCERTAIN:
								writeStateSet(singleTokenEvent, false);
								break;
							default:
								break; // Nothing to do.
						}
						break;
					}
				}
			default:
				break;
		}
		return true;
	}	
}
