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


import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

import info.bioinfweb.jphyloio.AbstractEventWriter;
import info.bioinfweb.jphyloio.dataadapters.DataAdapter;
import info.bioinfweb.jphyloio.dataadapters.JPhyloIOEventReceiver;
import info.bioinfweb.jphyloio.dataadapters.ObjectListDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.implementations.receivers.BasicEventReceiver;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.LinkedLabeledIDEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.formats.nexus.receivers.AbstractNexusEventReceiver;



/**
 * Customizable implementation for writing different kinds of Nexus sets.
 * 
 * @author Ben St&ouml;ver
 * @since 0.0.0
 */
public abstract class AbstractNexusSetWriter implements NexusConstants {
	private NexusWriterStreamDataProvider streamDataProvider;
	private String commandName;
	private EventContentType linkedContentType;
	private Iterator<? extends DataAdapter<? extends LabeledIDEvent>> dataSourceIterator;
	private AbstractNexusEventReceiver receiver;
	private boolean executed = false;
	
	
	public AbstractNexusSetWriter(NexusWriterStreamDataProvider streamDataProvider, String commandName,	EventContentType linkedContentType,	
			Iterator<? extends DataAdapter<? extends LabeledIDEvent>> dataSourceIterator,	AbstractNexusEventReceiver receiver) {
		
		super();
		this.streamDataProvider = streamDataProvider;
		this.commandName = commandName;
		this.linkedContentType = linkedContentType;
		this.dataSourceIterator = dataSourceIterator;
		this.receiver = receiver;
	}


	protected abstract ObjectListDataAdapter<LinkedLabeledIDEvent> getSets(DataAdapter<? extends LabeledIDEvent> dataSource);
	
	protected abstract String getLinkedBlockName(DataAdapter<? extends LabeledIDEvent> dataSource);
	
	
	private void logIgnoredMetadata(BasicEventReceiver<Writer> receiver, String setName) {
		if (receiver.didIgnoreMetadata()) {
			streamDataProvider.getParameters().getLogger().addMessage("One or more " + setName + " elements contained metadata. " + 
					receiver.getIgnoredMetadata() +	
					" metadata items were not written, because the Nexus format does not support metadata at this position.");
		}
	}
	
	
	public void write() throws IOException {
		if (executed) {
			throw new IllegalStateException("This set writer did already run and cannot be invoked multiple times.");
		}
		else {
			executed = true;
		}
		
		while (dataSourceIterator.hasNext()) {
			DataAdapter<? extends LabeledIDEvent> dataSource = dataSourceIterator.next();
			ObjectListDataAdapter<LinkedLabeledIDEvent> sets = getSets(dataSource);
			Iterator<String> charSetIDIterator = sets.getIDIterator();
			if (charSetIDIterator.hasNext()) {
				streamDataProvider.writeBlockStart(BLOCK_NAME_SETS);
				String dataSourceID = dataSource.getStartEvent(streamDataProvider.getParameters()).getID();
				streamDataProvider.writeLinkCommand(dataSourceID, getLinkedBlockName(dataSource), linkedContentType);
				
				while (charSetIDIterator.hasNext()) {
					String charSetID = charSetIDIterator.next();
					
					streamDataProvider.writeLineStart(commandName);
					streamDataProvider.getDataWriter().write(' ');
					streamDataProvider.getDataWriter().write(NexusEventWriter.formatToken(AbstractEventWriter.createUniqueLabel(
							streamDataProvider.getParameters(), sets.getObjectStartEvent(charSetID))));
					streamDataProvider.getDataWriter().write(' ');
					streamDataProvider.getDataWriter().write(KEY_VALUE_SEPARATOR);  // Next space will be written by receiver.
					sets.writeContentData(receiver, charSetID);  // Nothing will be written for empty character sets.
					streamDataProvider.writeCommandEnd();
					//TODO Log ignored metadata?
				}
				
				streamDataProvider.writeBlockEnd();
			}
		}
		
		logIgnoredMetadata(receiver, commandName);
	}
}
