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
import info.bioinfweb.jphyloio.dataadapters.implementations.receivers.AbstractSequenceContentReceiver;
import info.bioinfweb.jphyloio.events.CommentEvent;
import info.bioinfweb.jphyloio.events.MetaInformationEvent;
import info.bioinfweb.jphyloio.formats.nexml.NeXMLConstants;
import info.bioinfweb.jphyloio.formats.nexml.NeXMLWriterStreamDataProvider;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;



public class NeXMLSequenceContentReceiver extends AbstractSequenceContentReceiver<XMLStreamWriter> implements NeXMLConstants {
	private NeXMLWriterStreamDataProvider streamDataProvider; //TODO Doppelvererbungsproblem: müsste eigentlich auch von AbstractNeXMLDataReceiver erben um dort MetaDaten zu behandeln
	
	
	public NeXMLSequenceContentReceiver(XMLStreamWriter writer, ReadWriteParameterMap parameterMap, boolean longTokens, 
			NeXMLWriterStreamDataProvider streamDataProvider) {
		super(writer, parameterMap, longTokens);
		
		this.streamDataProvider = streamDataProvider;
	}


	@Override
	protected void writeToken(String token, String label) throws XMLStreamException {		
		if (streamDataProvider.isWriteCellsTags()) {
			getWriter().writeStartElement(TAG_CELL.getLocalPart());
			if (label != null) {
				getWriter().writeAttribute(ATTR_LABEL.getLocalPart(), label);
			}
			getWriter().writeCharacters(token);
			getWriter().writeEndElement();
		}		
		else {
			getWriter().writeCharacters(token);
			if (isLongTokens()) {
				getWriter().writeCharacters(" ");
			}
		}			
	}

	
	@Override
	protected void writeComment(CommentEvent event) throws XMLStreamException {
		String comment = event.getContent();
		if (!comment.isEmpty()) {
			getWriter().writeComment(comment);			
		}
	}
	

	@Override
	protected void writeMetaData(MetaInformationEvent event) throws XMLStreamException {
		// TODO Auto-generated method stub		
	}	
}
