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
package info.bioinfweb.jphyloio.formats.nexus;


import info.bioinfweb.commons.log.ApplicationLogger;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.type.EventContentType;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;



public class NexusWriterStreamDataProvider {
	private NexusEventWriter writer;

	
	public NexusWriterStreamDataProvider(NexusEventWriter writer) {
		super();
		this.writer = writer;
	}


	public NexusEventWriter getNexusWriter() {
		return writer;
	}


	public Writer getDataWriter() {
		return writer.getWriter();
	}


	public ReadWriteParameterMap getParameters() {
		return writer.getParameters();
	}


	public ApplicationLogger getLogger() {
		return writer.getLogger();
	}


	public Map<String, NexusMatrixWriteResult> getMatrixIDToBlockTypeMap() {
		return writer.getMatrixIDToBlockTypeMap();
	}
	
	
	public void writeBlockStart(String name) throws IOException {
		writer.writeBlockStart(name);
	}

	
	public void writeLinkCommand(String linkedID, String linkedBlockName, EventContentType linkedContentType) throws IOException {
		writer.writeLinkCommand(linkedID, linkedBlockName, linkedContentType);
	}
	
	
	public void writeLineStart(String text) throws IOException {
		writer.writeLineStart(getDataWriter(), text);
	}
	
	
	public void writeCommandEnd() throws IOException {
		writer.writeCommandEnd();
	}
	
	
	public void writeBlockEnd() throws IOException {
		writer.writeBlockEnd();
	}
}
