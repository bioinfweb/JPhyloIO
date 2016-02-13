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
import info.bioinfweb.jphyloio.dataadapters.MatrixDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.OTUListDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.ObjectListDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.implementations.receivers.IgnoreObjectListMetadataReceiver;
import info.bioinfweb.jphyloio.dataadapters.implementations.receivers.SequenceContentReceiver;
import info.bioinfweb.jphyloio.formats.newick.NewickStringWriter;



public class NexusEventWriter extends AbstractEventWriter implements NexusConstants {
	private static enum MatrixWriteResult {
		CHARACTERS,	UNALIGNED, NONE;
	}
	
	
	private Writer writer;
	private EventWriterParameterMap parameters;
	private ApplicationLogger logger;

	
	private void writeInitialLines() throws IOException {
		writer.write(FIRST_LINE);
		writeLineBreak(writer, parameters);
		
		String applicationComment = parameters.getString(EventWriterParameterMap.KEY_APPLICATION_COMMENT);
		if (applicationComment != null) {
			writer.write(COMMENT_START);
			writer.write(applicationComment);
			writer.write(COMMENT_END);
			writeLineBreak(writer, parameters);
		}
		
		writer.write(COMMENT_START);
		writer.write("This file was generated by an application using ");
		writer.write(JPhyloIO.getInstance().getLibraryNameAndVersion());
		writer.write(". <");
		writer.write(JPhyloIO.getInstance().getProjectURL().toString());
		writer.write(">");
		writer.write(COMMENT_END);
		writeLineBreak(writer, parameters);
	}
	
	
	private void logIgnoredMetadata(AnnotatedDataAdapter adapter, String objectName) {
		if (adapter.hasMetadata()) {
			logger.addWarning(objectName + 
					" is annotated directly with metadata, which have been ignored, since the Nexus format does not support this.");
		}
	}
	
	
	private void logMultipleBlocksWarning(String objectName, String blockName) {
		logger.addWarning("This document contains more than one " + objectName + ". Therefore multiple " + blockName 
				+ " blocks have been written into the Nexus output. Not all programs may be able to process Nexus files "
				+ "with multiple blocks of the same type.");
	}
	
	
	public static String formatToken(String token) {
		return NewickStringWriter.formatToken(token, WORD_DELIMITER);
	}
	
	
	private void writeCommandEnd() throws IOException {
		writer.write(COMMAND_END);
		writeLineBreak(writer, parameters);
	}
	
	
	private void writeBlockStart(String name) throws IOException {
		writeLineBreak(writer, parameters);  // Add one empty line before each block.
		writeLineStart(writer, BEGIN_COMMAND);
		writer.write(' ');
		writer.write(name);
		writeCommandEnd();
	}
	
	
	private void writeBlockEnd() throws IOException {
		writeLineStart(writer, END_COMMAND);
		writeCommandEnd();
	}
	
	
	private void writeKeyValueExpression(String key, String value) throws IOException {
		writeKeyValueExpression(writer, key, value);
	}
	
	
	public static void writeKeyValueExpression(Writer writer, String key, String value) throws IOException {
		writer.write(key);
		writer.write(KEY_VALUE_SEPARATOR);
		writer.write(value);
	}
	
	
	private void writeTaxaBlock(OTUListDataAdapter otuList) throws IOException {
		logIgnoredMetadata(otuList, "An OTU list");
		if (otuList.getCount() > 0) {
			writeBlockStart(BLOCK_NAME_TAXA);
			increaseIndention();
			
			writeLineStart(writer, COMMAND_NAME_DIMENSIONS);
			writer.write(' ');
			writeKeyValueExpression(DIMENSIONS_SUBCOMMAND_NTAX, Long.toString(otuList.getCount()));
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
				logMultipleBlocksWarning("OTU list", "TAXA");
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
	
	
	private void writeFormatCommand(ObjectListDataAdapter tokenSets) throws IOException {
		if (tokenSets.getCount() > 0) {
			writeLineStart(writer, COMMAND_NAME_FORMAT);
			Iterator<String> iterator = tokenSets.getIDIterator();
			if (tokenSets.getCount() == 1) {
				TokenSetEventReceiver receiver = new TokenSetEventReceiver(writer, parameters);
				tokenSets.writeData(receiver, iterator.next());
				
				if (receiver.getSingleTokens() != null) {
					writer.write(' ');
					writeKeyValueExpression(FORMAT_SUBCOMMAND_TOKENS, VALUE_DELIMITER + receiver.getSingleTokens() + VALUE_DELIMITER);
				}
				if (receiver.getIgnoredMetadata() > 0) {
					logger.addWarning("A token definition of a character matrix contained metadata which has been ignored, "
							+ "since the Nexus format does not support writing such data.");
				}
			}
			else {  // MrBayes extension (or exception if according parameter is set?)
				
			}
			writeCommandEnd();
		}
	}
	
	
	private void writeMatrixCommand(DocumentDataAdapter document, MatrixDataAdapter matrix, boolean unaligned) 
				throws IOException {
		
		writeLineStart(writer, COMMAND_NAME_MATRIX);
		writeLineBreak(writer, parameters);
		
		increaseIndention();
		increaseIndention();
		Iterator<String> iterator = matrix.getSequenceIDIterator();
		while (iterator.hasNext()) {
			String id = iterator.next();
			
			OTUListDataAdapter otuList = null;
			String otuListID = matrix.getLinkedOTUListID();
			if (otuListID != null) {
				otuList = document.getOTUList(otuListID);
			}
			writeLineStart(writer, formatToken(getLinkedOTUName(matrix.getSequenceStartEvent(id), otuList)));
			writer.write(' ');
			
			SequenceContentReceiver receiver = new SequenceContentReceiver(
					writer, parameters, "" + COMMENT_START, "" + COMMENT_END, matrix.containsLongTokens());
			matrix.writeSequencePartContentData(receiver, id, 0, matrix.getSequenceLength(id));
			
			if (iterator.hasNext()) {
				if (unaligned) {
					writer.write(ELEMENT_SEPARATOR);
				}
				writeLineBreak(writer, parameters);
			}
			else {
				writeCommandEnd();
			}
		}
		
		decreaseIndention();
		decreaseIndention();
	}
	
	
	/**
	 * Writes a Nexus {@code CHARACTERS} or {@code UNALIGNED} block. 
	 * 
	 * @param matrix the character data to be written
	 * @return a value indicating if and which Nexus block was written
	 * @throws IOException
	 */
	private MatrixWriteResult writeCharactersUnalignedBlock(DocumentDataAdapter document, MatrixDataAdapter matrix) throws IOException {
		logIgnoredMetadata(matrix, "A character matrix");
		if (matrix.getSequenceCount() > 0) {
			long columnCount = matrix.getColumnCount();
			if ((columnCount == -1) && parameters.getBoolean(EventWriterParameterMap.KEY_EXTEND_SEQUENCE_WITH_GAPS, false)) {
			  // Determine maximal sequence length:
				Iterator<String> iterator = matrix.getSequenceIDIterator();
				while (iterator.hasNext()) {  // columnCount will be set, since it was already checked, that at least one sequence is contained.
					columnCount = Math.max(columnCount, matrix.getSequenceLength(iterator.next()));
				}
			}
			
			MatrixWriteResult result;
			if (columnCount == -1) {
				result = MatrixWriteResult.UNALIGNED;
				writeBlockStart(BLOCK_NAME_UNALIGNED);
			}
			else {
				result = MatrixWriteResult.CHARACTERS;
				writeBlockStart(BLOCK_NAME_CHARACTERS);
			}
			increaseIndention();
			
			writeLineStart(writer, COMMAND_NAME_DIMENSIONS);
			writer.write(' ');
			writeKeyValueExpression(DIMENSIONS_SUBCOMMAND_NTAX, Long.toString(matrix.getSequenceCount()));
			if (columnCount != -1) {
				writer.write(' ');
				writeKeyValueExpression(DIMENSIONS_SUBCOMMAND_NCHAR, Long.toString(columnCount));
			}
			writeCommandEnd();
			
			writeFormatCommand(matrix.getTokenSets());  //TODO Write "newTokens" if necessary.
			writeMatrixCommand(document, matrix, columnCount == -1);
			
			decreaseIndention();
			writeBlockEnd();
			
			return result;
		}
		else {
			logger.addWarning("The document contained an emtpty character matrix, which has been ignored.");
			return MatrixWriteResult.NONE;
		}
	}
	
	
	private void writeCharactersUnalignedBlocks(DocumentDataAdapter document) throws IOException {
		boolean charactersWritten = false;
		boolean unalignedWritten = false;
		
		Iterator<MatrixDataAdapter> matrixIterator = document.getMatrixIterator();
		while (matrixIterator.hasNext()) {
			switch (writeCharactersUnalignedBlock(document, matrixIterator.next())) {
				case CHARACTERS:
					charactersWritten = true;
					break;
				case UNALIGNED:
					unalignedWritten = true;
					break;
				default:  // Nothing to do.
					break;
			}
		}
		
		if (charactersWritten) {
			logMultipleBlocksWarning("aligned matrix", BLOCK_NAME_CHARACTERS);
		}
		if (unalignedWritten) {
			logMultipleBlocksWarning("unaligned matrix", BLOCK_NAME_UNALIGNED);
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
		writeCharactersUnalignedBlocks(document);
		//TODO Write TREES
		//TODO Write SETS
	}
}
