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


import info.bioinfweb.commons.text.StringUtils;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.MetaInformationEvent;
import info.bioinfweb.jphyloio.formats.nexus.NexusCommandReaderFactory;
import info.bioinfweb.jphyloio.formats.nexus.NexusConstants;
import info.bioinfweb.jphyloio.formats.nexus.NexusEventReader;
import info.bioinfweb.jphyloio.formats.nexus.NexusStreamDataProvider;



/**
 * Default Nexus command reader for all unsupported Nexus commands.
 * 
 * @author Ben St&ouml;ver
 */
public class DefaultCommandReader extends AbstractNexusCommandEventReader implements NexusConstants {
	private static final String[] TERMINATION_SEQUENCES = {Character.toString(COMMAND_END), Character.toString(COMMENT_START)};
	
	
	/**
	 * Creates a new instance of this class.
	 * <p>
	 * This class does not provide a constructor according to the requirements defined in {@link NexusCommandEventReader},
	 * because it is not meant to be used with {@link NexusCommandReaderFactory} but shall be directly created in
	 * {@link NexusEventReader}.
	 * 
	 * @param commandName the name of the command to be parsed
	 * @param streamDataProvider the provider to access the shared Nexus data and the input stream
	 */
	public DefaultCommandReader(String commandName, NexusStreamDataProvider streamDataProvider) {
		super(commandName, new String[0], streamDataProvider);
	}

	
	@Override
	protected JPhyloIOEvent doReadNextEvent() throws Exception {
		StringBuilder result = new StringBuilder();
		CharSequence sequence;
		do {
			sequence = getStreamDataProvider().getDataReader().readUntil(TERMINATION_SEQUENCES).getSequence();
			result.append(StringUtils.cutEnd(sequence, 1));
			if (StringUtils.endsWith(sequence, COMMENT_START)) {
				getStreamDataProvider().readComment();
			}
		} while (!StringUtils.endsWith(sequence, COMMAND_END));
		return new MetaInformationEvent(getCommandName(), result.toString());  //TODO Use other event type (e.g. special Nexus command version or an UnparsedDataEvent)?
	}
}