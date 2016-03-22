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


import java.io.IOException;

import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.dataadapters.implementations.receivers.AbstractSequenceContentReceiver;
import info.bioinfweb.jphyloio.events.CommentEvent;
import info.bioinfweb.jphyloio.events.MetaInformationEvent;
import info.bioinfweb.jphyloio.events.SequenceTokensEvent;
import info.bioinfweb.jphyloio.formats.nexml.NeXMLConstants;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;



public class NeXMLSequenceContentReceiver extends AbstractSequenceContentReceiver<XMLStreamWriter> implements NeXMLConstants {	
	public NeXMLSequenceContentReceiver(XMLStreamWriter writer, ReadWriteParameterMap parameterMap, boolean longTokens) {
		super(writer, parameterMap, longTokens);
	}


	@Override
	protected void writeToken(String token) throws XMLStreamException {
		//TODO decide if cell or seq tag is to be used
		getWriter().writeStartElement(TAG_CELL.getLocalPart());
		getWriter().writeCharacters(token);
		getWriter().writeEndElement();
	}


//	@Override
//	protected void writeTokens(SequenceTokensEvent event) throws XMLStreamException {
//		getWriter().writeStartElement(TAG_SEQ.getLocalPart());
//		
//		for (String token : event.getCharacterValues()) {
//			getWriter().writeCharacters(token);
//		}
//		
//		getWriter().writeEndElement();
//	}

	
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