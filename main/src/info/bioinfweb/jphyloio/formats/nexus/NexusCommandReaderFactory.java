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


import info.bioinfweb.jphyloio.formats.nexus.commandreaders.NexusCommandEventReader;
import info.bioinfweb.jphyloio.formats.nexus.commandreaders.sets.CharSetReader;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;



public class NexusCommandReaderFactory {
	public static final char BLOCK_COMMAND_CONNECTOR = '.';
	
	private Map<String, Class<? extends NexusCommandEventReader>> readers = 
			new TreeMap<String, Class<? extends NexusCommandEventReader>>();

	
	public void addJPhyloIOReaders() {
		addReaderClass(CharSetReader.class);
		// add new classes here
	}
	
	
	private NexusCommandEventReader createReaderInstance(Class<? extends NexusCommandEventReader> readerClass, 
			NexusStreamDataProvider streamDataProvider) throws IllegalArgumentException {
		
		try {
			return readerClass.getConstructor(NexusStreamDataProvider.class).newInstance(streamDataProvider);
		}
		catch (Exception e) {
			throw new IllegalArgumentException("Unable to create instance with single " + NexusStreamDataProvider.class.getName() + 
					" argument.", e);
		}
	}
	
	
	public void addReaderClass(Class<? extends NexusCommandEventReader> readerClass) throws IllegalArgumentException {
		NexusCommandEventReader reader = createReaderInstance(readerClass, null);
		for (String blockName : reader.getValidBlocks()) {
			readers.put(blockName.toUpperCase() + BLOCK_COMMAND_CONNECTOR + reader.getCommandName().toUpperCase(), readerClass);
		}
	}
	
	
	/**
	 * Creates a new reader instance that is able to parse the specified command in the specified block,
	 * 
	 * @param blockName the name of the block the command to parse is contained in
	 * @param commandName the name of the command to be parsed
	 * @param streamDataProvider the stream and data provider to be used by the returned reader
	 * @return the reader or {@code null} if no according reader was found
	 */
	public NexusCommandEventReader createReader(String blockName, String commandName, NexusStreamDataProvider streamDataProvider) {
		Class<? extends NexusCommandEventReader> readerClass = readers.get(blockName.toUpperCase() + BLOCK_COMMAND_CONNECTOR + 
				commandName.toUpperCase());
		if (readerClass == null) {
			return null;
		}
		else {
			try {
				return createReaderInstance(readerClass, streamDataProvider);
			}
			catch (IllegalArgumentException e) {
				throw new InternalError(e.getCause());
			}
		}
	}
}
