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
package info.bioinfweb.jphyloio.formats.nexus.commandreaders.all;


import java.io.IOException;

import info.bioinfweb.jphyloio.JPhyloIOReaderException;
import info.bioinfweb.jphyloio.formats.nexus.NexusConstants;
import info.bioinfweb.jphyloio.formats.nexus.NexusStreamDataProvider;
import info.bioinfweb.jphyloio.formats.nexus.commandreaders.AbstractKeyValueCommandReader;
import info.bioinfweb.jphyloio.formats.text.AbstractTextEventReader.KeyValueInformation;



/**
 * Reads the {@code LINK} command of a Nexus block and stores the links in the shared information map of the
 * stream data provider under the key {@link NexusStreamDataProvider#INFO_KEY_BLOCK_LINKS}.
 * <p>
 * Note that the {@code LINK} command is not part of the initial Nexus definition, but was used by Mesquite
 * as a custom command to allow references between blocks using the {@code TITLE} command.
 * <p>
 * This reader is valid for all blocks, therefore {@link #getValidBlocks()} returns an empty collection.
 * <p>
 * <b>Example usage:</b>
 * <pre>
 * #NEXUS
 * 
 * BEGIN TAXA;
 *   <b>TITLE</b> 'taxon list 1';
 *   DIMENSIONS NTAX = 3;
 *   TAXLABELS A B C;
 * END;
 * BEGIN TAXA;
 *   <b>TITLE</b> TaxonList2;
 *   DIMENSIONS NTAX = 3;
 *   TAXLABELS D E F;
 * END;
 * 
 * BEGIN TREES;
 *   <b>LINK</b> TAXA = 'taxon list 1';
 *   TREE someTree = (A, (B, C));
 * END;
 * BEGIN TREES;
 *   <b>LINK</b> TAXA = TaxonList2;
 *   TREE someTree = (D, (E, F));
 * END;
 * </pre>
 * The {@code LINK} command may also be used to link other blocks than the {@code TAXA} block, but <i>JPhyloIO</i> will
 * not use this information.
 * 
 * @author Ben St&ouml;ver
 * @see TitleReader
 */
public class LinkReader extends AbstractKeyValueCommandReader implements NexusConstants {
	public LinkReader(NexusStreamDataProvider nexusDocument) {
		super(COMMAND_NAME_LINK, new String[0], nexusDocument, "");
	}

	
	@Override
	protected boolean processSubcommand(KeyValueInformation info)	throws IOException {
		String value = info.getValue();
		if (info.getKey().toUpperCase().equals(BLOCK_NAME_TAXA.toUpperCase())) {
			value = getStreamDataProvider().getOTUsLabelToIDMap().get(info.getValue());
			if (value == null) {
				throw new JPhyloIOReaderException("The linked Nexus TAXA block with the label \"" + info.getValue() + 
						"\" was not previously declared unsing a TITLE command.", getStreamDataProvider().getDataReader());
			}
		}
		getStreamDataProvider().getBlockLinks().put(info.getKey().toUpperCase(), value);
		return false;
	}

	
	@Override
	protected boolean addStoredEvents() {
		return false;
	}
}
