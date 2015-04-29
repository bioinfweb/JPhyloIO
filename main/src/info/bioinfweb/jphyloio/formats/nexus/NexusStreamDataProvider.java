/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015  Ben Stöver
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


import java.io.IOException;
import java.util.Queue;

import info.bioinfweb.commons.collections.ParameterMap;
import info.bioinfweb.commons.io.PeekReader;
import info.bioinfweb.jphyloio.SequenceTokensEventManager;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.MetaInformationEvent;
import info.bioinfweb.jphyloio.formats.nexus.commandreaders.NexusCommandEventReader;



/**
 * Stores data that shall be shared among different implementations of {@link NexusCommandEventReader}
 * reading from the same document.
 * 
 * @author Ben St&ouml;ver
 */
public class NexusStreamDataProvider {
	private NexusEventReader nexusReader;
	private PeekReader dataReader;
	private ParameterMap sharedInformationMap = new ParameterMap();
	
	
	public NexusStreamDataProvider(NexusEventReader nexusReader, PeekReader dataReader) {
		super();
		this.nexusReader = nexusReader;
		this.dataReader = dataReader;
	}


	public NexusEventReader getNexusReader() {
		return nexusReader;
	}


	public void consumeWhiteSpaceAndComments() throws IOException {
		nexusReader.consumeWhiteSpaceAndComments();
	}
	
	
	public String readNexusWord() throws IOException {
		return nexusReader.readNexusWord();
	}
	
	
	public void readComment() throws IOException {
		nexusReader.readComment();
	}
	
	
	public MetaInformationEvent readKeyValueMetaInformation(String keyPrefix) throws IOException {
		return nexusReader.readKeyValueMetaInformation(keyPrefix);
	}
	
	
	public Queue<JPhyloIOEvent> getUpcomingEvents() {
		return nexusReader.getUpcomingEvents();
	}
	
	
	/**
	 * The reader from the associated {@link NexusEventReader}.
	 * 
	 * @return the reader to read the command content data from
	 */
	public PeekReader getDataReader() {
		return dataReader;
	}
	
	
	public SequenceTokensEventManager getSequenceTokensEventManager() {
		return nexusReader.getSequenceTokensEventManager();
	}
	
	
	/**
	 * This map can be used to store objects to be shared between different instances of 
	 * {@link NexusCommandEventReader}.
	 * 
	 * @return a map providing access to shared data objects
	 */
	public ParameterMap getSharedInformationMap() {
		return sharedInformationMap;
	}
}
