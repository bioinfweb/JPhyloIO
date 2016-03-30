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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.dataadapters.implementations.receivers.AbstractEventReceiver;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.formats.nexml.NeXMLConstants;
import info.bioinfweb.jphyloio.formats.nexml.NeXMLWriterStreamDataProvider;



public abstract class AbstractNeXMLDataReceiver extends AbstractEventReceiver<XMLStreamWriter> implements NeXMLConstants {
	private NeXMLWriterStreamDataProvider streamDataProvider;
	

	public AbstractNeXMLDataReceiver(XMLStreamWriter writer, ReadWriteParameterMap parameterMap, NeXMLWriterStreamDataProvider streamDataProvider) {
		super(writer, parameterMap);
		this.streamDataProvider = streamDataProvider;
	}

	
	@Override
	protected abstract boolean doAdd(JPhyloIOEvent event) throws IOException, XMLStreamException; //TODO handle metadata name space collecting here?


	public NeXMLWriterStreamDataProvider getStreamDataProvider() {
		return streamDataProvider;
	}
}
