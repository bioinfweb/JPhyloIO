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
package info.bioinfweb.jphyloio.formats.nexus.commandreaders.trees;


import java.io.EOFException;
import java.io.IOException;

import info.bioinfweb.commons.io.PeekReader;
import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.exception.JPhyloIOReaderException;
import info.bioinfweb.jphyloio.formats.newick.NewickStringReader;
import info.bioinfweb.jphyloio.formats.nexus.NexusConstants;
import info.bioinfweb.jphyloio.formats.nexus.NexusReaderStreamDataProvider;
import info.bioinfweb.jphyloio.formats.nexus.commandreaders.AbstractNexusCommandEventReader;



public class TreeReader extends AbstractNexusCommandEventReader implements NexusConstants, ReadWriteConstants {
	private NewickStringReader newickStringReader = null;
	
	
	public TreeReader(NexusReaderStreamDataProvider nexusDocument) {
		super(COMMAND_NAME_TREE, new String[]{BLOCK_NAME_TREES}, nexusDocument);
	}

	
	@Override
	protected boolean doReadNextEvent() throws IOException {
		PeekReader reader = getStreamDataProvider().getDataReader();
		
		try {
			if (newickStringReader == null) {  // First call
				getStreamDataProvider().consumeWhiteSpaceAndComments();
				String treeLabel = getStreamDataProvider().readNexusWord();
				getStreamDataProvider().consumeWhiteSpaceAndComments();
				
				String treeGroupID = getStreamDataProvider().getSharedInformationMap().getString(
						NexusReaderStreamDataProvider.INFO_KEY_CURRENT_BLOCK_ID);
				getStreamDataProvider().getElementList(EventContentType.TREE, treeGroupID).add(treeLabel);
				String treeID = DEFAULT_TREE_ID_PREFIX + getStreamDataProvider().getIDManager().createNewID();
				getStreamDataProvider().getNexusNameToIDMap(EventContentType.TREE, treeGroupID).put(treeLabel, treeID);
				
				if (reader.peekChar() == KEY_VALUE_SEPARATOR) {
					reader.read();  // Skip KEY_VALUE_SEPARATOR.
					newickStringReader = new NewickStringReader(getStreamDataProvider(), treeID, treeLabel, 
							new NexusNewickReaderNodeLabelProcessor(getStreamDataProvider()));
				}
				else {
					throw new JPhyloIOReaderException("Expected \"" + KEY_VALUE_SEPARATOR + 
							"\" behind the tree label in the TREE command, but found \"" + reader.peekChar() + "\".", reader);
				}
			}
			return newickStringReader.addNextEvents();
		}
		catch (EOFException e) {
			throw new JPhyloIOReaderException("Unexpected end of file inside a Nexus TREE command.", reader, e);
		}
	}
}
