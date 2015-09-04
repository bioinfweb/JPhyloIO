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
package info.bioinfweb.jphyloio.formats.nexus.commandreaders;


import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.formats.nexus.NexusStreamDataProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;



/**
 * Implements shared functionality for classes implementing {@link NexusCommandEventReader}.
 * 
 * @author Ben St&ouml;ver
 */
public abstract class AbstractNexusCommandEventReader implements NexusCommandEventReader {
	private String commandName;
	private Collection<String> validBlocks;
	private boolean allDataProcessed = false;
	private NexusStreamDataProvider streamDataProvider;

	
	/**
	 * Creates a new instance of this class.
	 * <p>
	 * Note that non-abstract inherited classes need to specify a constructor matching the requirements defined by 
	 * {@link NexusCommandEventReader}.
	 * 
	 * @param validBlocks an array with the names of the Nexus blocks the command parsed by the implementing
	 *        class may be contained in
	 */
	public AbstractNexusCommandEventReader(String commandName, String[] validBlocks, NexusStreamDataProvider nexusDocument) {
		super();
		
		this.commandName = commandName.toUpperCase();
		ArrayList<String> list = new ArrayList<String>(validBlocks.length);
		for (int i = 0; i < validBlocks.length; i++) {
			list.add(validBlocks[i].toUpperCase());
		}
		this.validBlocks = Collections.unmodifiableCollection(list);
		this.streamDataProvider = nexusDocument;
	}


	@Override
	public String getCommandName() {
		return commandName;
	}


	@Override
	public Collection<String> getValidBlocks() {
		return validBlocks;
	}


	/**
	 * Returns {@code true} if the whole command this instance was created on has been parsed or {@code false}
	 * if another events can be generated by the next call of {@link #readNextEvent()}. 
	 * 
	 * @return whether more events are available
	 */
	protected boolean isAllDataProcessed() {
		return allDataProcessed;
	}


	protected void setAllDataProcessed(boolean allDataProcessed) {
		this.allDataProcessed = allDataProcessed;
	}


	protected NexusStreamDataProvider getStreamDataProvider() {
		return streamDataProvider;
	}


	protected abstract JPhyloIOEvent doReadNextEvent() throws Exception;
	
	
	@Override
	public JPhyloIOEvent readNextEvent() throws Exception {
		if (isAllDataProcessed()) {
			return null;
		}
		else {
			return doReadNextEvent();
		}
	}
}
