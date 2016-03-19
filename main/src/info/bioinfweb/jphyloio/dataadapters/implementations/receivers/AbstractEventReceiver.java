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
package info.bioinfweb.jphyloio.dataadapters.implementations.receivers;


import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import info.bioinfweb.commons.log.ApplicationLogger;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.dataadapters.JPhyloIOEventReceiver;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.exception.JPhyloIOWriterException;



public abstract class AbstractEventReceiver<W extends Object> implements JPhyloIOEventReceiver {
	private W writer;
	private ReadWriteParameterMap parameterMap;
	private long ignoredComments = 0;
	private long ignoredMetadata = 0;
	
	
	public AbstractEventReceiver(W writer, ReadWriteParameterMap parameterMap) {
		super();
		this.writer = writer;
		this.parameterMap = parameterMap;
	}


	protected W getWriter() {
		return writer;
	}


	protected ReadWriteParameterMap getParameterMap() {
		return parameterMap;
	}
	
	
	protected ApplicationLogger getLogger() {
		return getParameterMap().getApplicationLogger(ReadWriteParameterMap.KEY_LOGGER);
	}


	public long getIgnoredComments() {
		return ignoredComments;
	}
	
	
	public boolean didIgnoreComments() {
		return getIgnoredComments() > 0;
	}


	protected void addIgnoredComments(long addend) {
		ignoredComments += addend;
	}


	public long getIgnoredMetadata() {
		return ignoredMetadata;
	}


	public boolean didIgnoreMetadata() {
		return getIgnoredMetadata() > 0;
	}


	protected void addIgnoredMetadata(long addend) {
		ignoredMetadata += addend;
	}

	
	protected abstract boolean doAdd(JPhyloIOEvent event) throws IOException, XMLStreamException;
	
	
	@Override
	public boolean add(JPhyloIOEvent event) throws IOException {
		try {
			return doAdd(event);
		}
		catch (XMLStreamException e) {
			throw new JPhyloIOWriterException("An XMLStream exception with the message \"" + e.getMessage() + 
					"\" occured when trying to add an event to this receiver.", e);
		}
	}
}
