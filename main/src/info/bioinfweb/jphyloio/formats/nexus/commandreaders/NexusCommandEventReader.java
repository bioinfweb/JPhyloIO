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

import java.util.Collection;



/**
 * Interface to be implement by all classes and parse a single Nexus command and convert its contents
 * into JPhyloIO events.
 * <p>
 * Implementing instances should take information about the reader and the Nexus document as constructor
 * parameters. A new instance should be created for parsing each command. The reader should be positioned
 * at the first character behind the terminal semicolon after the implementing instance parsed all data
 * and {@link #isAllDataProcessed()} should return {@code true} then.
 * 
 * @author Ben St&ouml;ver
 */
public interface NexusCommandEventReader {  //TODO Define a constructor signature in the doc that must be offered by all implementations 
	/**
	 * Returns the name of the Nexus command that can be parsed by this instance.
	 * 
	 * @return a non-empty string
	 */
	public String getCommandName();
	
	/**
	 * Returns a collection with the names of the Nexus blocks the command parsed by the implementing 
	 * class may be contained in.
	 * 
	 * @return a collection with the block names
	 */
	public Collection<String> getValidBlocks();
	
	/**
	 * Returns the next event generated from the parsed Nexus command.
	 * 
	 * @return the next event or {@code null} if no more events can be generated from the current command data
	 */
	public JPhyloIOEvent readNextEvent() throws Exception;
}
