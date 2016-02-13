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

import info.bioinfweb.commons.log.ApplicationLogger;
import info.bioinfweb.jphyloio.AbstractEventWriter;
import info.bioinfweb.jphyloio.EventWriterParameterMap;
import info.bioinfweb.jphyloio.JPhyloIO;
import info.bioinfweb.jphyloio.dataadapters.AnnotatedDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.DocumentDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.OTUListDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.implementations.receivers.IgnoreObjectListMetadataReceiver;
import info.bioinfweb.jphyloio.formats.newick.NewickStringWriter;



public class NexusEventWriter extends AbstractEventWriter implements NexusConstants {
	private Writer writer;
	private EventWriterParameterMap parameters;
	private ApplicationLogger logger;

	
	private void writeInitialLines() throws IOException {
		writer.write(FIRST_LINE);
		writeLineBreak(writer, parameters);
		writer.write(COMMENT_START);
		writer.write("This file was generated using ");
		writer.write(JPhyloIO.getInstance().getLibraryNameAndVersion());
		writer.write(". <");
		writer.write(JPhyloIO.getInstance().getProjectURL().toString());
		writer.write(">");
		writer.write(COMMENT_END);
		writeLineBreak(writer, parameters);
		writeLineBreak(writer, parameters);
	}
	
	
	private void logIgnoredMetadata(AnnotatedDataAdapter adapter, String objectName) {
		if (adapter.hasMetadata()) {
			logger.addWarning(objectName + 
					" is annotated directly with metadata, which have been ignored, since the Nexus format does not support this.");
		}
	}
	
	
	private String formatToken(String token) {
		return NewickStringWriter.formatToken(token, WORD_DELIMITER);
	}
	
	
	private void writeCommandEnd() throws IOException {
		writer.write(COMMAND_END);
		writeLineBreak(writer, parameters);
	}
	
	
	private void writeBlockStart(String name) throws IOException {
		writeLineStart(writer, BEGIN_COMMAND);
		writer.write(' ');
		writer.write(name);
		writeCommandEnd();
	}
	
	
	private void writeBlockEnd() throws IOException {
		writeLineStart(writer, END_COMMAND);
		writeCommandEnd();
	}
	
	
	private void writeTaxaBlock(OTUListDataAdapter otuList) throws IOException {
		logIgnoredMetadata(otuList, "An OTU list");
		if (otuList.getCount() > 0) {
			writeBlockStart(BLOCK_NAME_TAXA);
			increaseIndention();
			
			writeLineStart(writer, COMMAND_NAME_DIMENSIONS);
			writer.write(' ');
			writer.write(DIMENSIONS_SUBCOMMAND_NTAX);
			writer.write(' ');
			writer.write(KEY_VALUE_SEPARATOR);
			writer.write(' ');
			writer.write(Long.toString(otuList.getCount()));
			writeCommandEnd();
			
			writeLineStart(writer, COMMAND_NAME_TAX_LABELS);
			writeLineBreak(writer, parameters);
			increaseIndention();
			increaseIndention();
			IgnoreObjectListMetadataReceiver receiver = new IgnoreObjectListMetadataReceiver(logger, "an OTU", "Nexus");
			Iterator<String> iterator = otuList.getIDIterator();
			while (iterator.hasNext()) {
				String id = iterator.next();
				writeLineStart(writer, formatToken(getLabeledIDName(otuList.getOTUStartEvent(id))));
				if (iterator.hasNext()) {
					writeLineBreak(writer, parameters);
				}
				else {
					writeCommandEnd();
				}
				otuList.writeData(receiver, id);
				receiver.reset();
			}
			decreaseIndention();
			decreaseIndention();
			
			decreaseIndention();
			writeBlockEnd();
		}
		else {
			logger.addWarning("The document contained an emtpty OTU list, which has been ignored.");
		}
	}
	
	
	private void writeTaxaBlocks(DocumentDataAdapter document) throws IOException {
		Iterator<OTUListDataAdapter> otusIterator = document.getOTUListIterator();
		if (otusIterator.hasNext()) {
			writeTaxaBlock(otusIterator.next());
			if (otusIterator.hasNext()) {
				logger.addWarning("This document contains more than one OTU list. Therefore multiple TAXA blocks have been written "
						+ "into the Nexus output. Not all programs may be able to process Nexus files with multiple TAXA blocks.");
				do {
					writeTaxaBlock(otusIterator.next());
				}	while (otusIterator.hasNext());
			}
		}
		else {
			logger.addWarning("This document contains no OTU list. Therefore no TAXA block can be written to the Nexus document. "
					+ "Some programs may not be able to read Nexus files without a TAXA block.");
		}
	}
	
	
	@Override
	public void writeDocument(DocumentDataAdapter document, Writer writer, EventWriterParameterMap parameters) throws Exception {
		this.writer = writer;
		this.parameters = parameters;
		logger = getLogger(parameters);
		
		writeInitialLines();
		logIgnoredMetadata(document, "The document");
		writeTaxaBlocks(document);
		
		//TODO Write n * (CHARACTERS or UNALIGNED)
		//TODO Write TREES
		//TODO Write SETS
	}
}
