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


import java.io.Writer;

import info.bioinfweb.commons.log.ApplicationLogger;
import info.bioinfweb.jphyloio.EventWriterParameterMap;
import info.bioinfweb.jphyloio.dataadapters.JPhyloIOEventReceiver;



public abstract class AbstractEventReceiver implements JPhyloIOEventReceiver {
	private Writer writer;
	private EventWriterParameterMap parameterMap;
	private long ignoredComments = 0;
	private long ignoredMetadata = 0;
	
	
	public AbstractEventReceiver(Writer writer, EventWriterParameterMap parameterMap) {
		super();
		this.writer = writer;
		this.parameterMap = parameterMap;
	}


	protected Writer getWriter() {
		return writer;
	}


	protected EventWriterParameterMap getParameterMap() {
		return parameterMap;
	}
	
	
	protected ApplicationLogger getLogger() {
		return getParameterMap().getApplicationLogger(EventWriterParameterMap.KEY_LOGGER);
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
}
