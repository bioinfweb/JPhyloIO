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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import info.bioinfweb.commons.log.ApplicationLogger;
import info.bioinfweb.jphyloio.AbstractEventWriter;
import info.bioinfweb.jphyloio.EventWriterParameterMap;
import info.bioinfweb.jphyloio.JPhyloIO;
import info.bioinfweb.jphyloio.dataadapters.AnnotatedDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.DocumentDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.MatrixDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.OTUListDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.ObjectListDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.TreeNetworkDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.implementations.receivers.IgnoreObjectListMetadataReceiver;
import info.bioinfweb.jphyloio.dataadapters.implementations.receivers.SequenceContentReceiver;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.formats.newick.NewickStringWriter;



/**
 * Event based writer for the Nexus format.
 * <p>
 * This write is able to write OTU, sequence and tree data to Nexus formatted streams. It will ignore any data for 
 * phylogenetic networks that are provided by {@link DocumentDataAdapter#getTreeNetworkIterator()}, because the Nexus 
 * format only supports trees.
 * 
 * <h3><a name="commentsMeta"></a>Comments and metadata</h3> 
 * <p>
 * Comments nested in any of the supported elements will usually be written. Metadata is only supported nested in
 * tree node or edge definitions and is written into hot comments as they are supported by {@link NewickStringWriter}.
 * Metadata nested into other elements will be ignored.
 * 
 * <h3><a name="labelsIDs"></a>Labels and IDs</h3> 
 * <p>
 * Note that the Nexus format does not differentiate between labels and IDs and because of this, labels of OTUs and 
 * labels of sequences or nodes linked to them must have identical names in Nexus. Therefore this writer will always use
 * the OTU label of the linked OTU, also for writing sequences and nodes. Labels of sequences and nodes will be ignored
 * (which does not make a difference, of they are identical with the labels of their linked OTUs).
 * <p>
 * Sequence labels will though be used, if no OTU is linked. In such cases the {@code NEWTAXA} subcommand will be specified
 * in the {@code DIMENSIONS} command.
 * <p>
 * If an OTU event without a defined label is provided by the data adapter, this writer will use the OTU ID as the taxon
 * name in Nexus instead. If two OTUs with identical labels are provided, the Nexus name of the second will be a combination 
 * of the ID and the label, while the first will be represented as the unchanged label as usual. If additional conflicts 
 * occur (e.g. if an ID of an OTU without a label is equal to the label of a previous OTU) an index will be added to the end 
 * of the label, until it is unique labels. The elements (ID, label, index) of edited Nexus names will be separated by
 * {@link #EDITED_LABEL_SEPARATOR}.
 * <p>
 * The Nexus names that have actually be used in the output can be obtained from 
 *  
 * <h3><a name="parameters"></a>Recognized parameters</h3> 
 * <ul>
 *   <li>{@link EventWriterParameterMap#KEY_APPLICATION_COMMENT}</li>
 *   <li>{@link EventWriterParameterMap#KEY_EXTEND_SEQUENCE_WITH_GAPS}</li>
 *   <li>{@link EventWriterParameterMap#KEY_GENERATE_TRANSLATION_TABLE}</li>
 *   <li>{@link EventWriterParameterMap#KEY_LOGGER}</li>
 *   <li>{@link EventWriterParameterMap#KEY_GENERATED_LABELS_MAP}</li>
 *   <li>{@link EventWriterParameterMap#KEY_GENERATED_LABELS_MAP_ID_TYPE}</li>
 * </ul>
 * 
 * @author Ben St&ouml;ver
 */
public class NexusEventWriter extends AbstractEventWriter implements NexusConstants {
	public static final String EDITED_LABEL_SEPARATOR = "_";
	
	
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
	
	
	private String createOTULabel(LabeledIDEvent otuEvent, Set<String> taxonLabels) {
		String result = getLabeledIDName(otuEvent);
		if (taxonLabels.contains(result)) {
			if (otuEvent.hasLabel()) {
				result = otuEvent.getID() + EDITED_LABEL_SEPARATOR + otuEvent.getLabel();
			}
			
			if (taxonLabels.contains(result)) {
				long suffix = 2;
				String editedResult;
				do {
					editedResult = result + EDITED_LABEL_SEPARATOR + suffix;
					suffix++;
				}	while (taxonLabels.contains(editedResult));
				result = editedResult;
			}
		}
		
		taxonLabels.add(result);
		if (!result.equals(otuEvent.getLabel())) {  //TODO Also add unchanged labels as mappings from their IDs?
			parameters.getGeneratedLabelsMap().put(otuEvent.getID(), result);
		}
		return result;
	}
	
	
	private void writeTaxaBlock(OTUListDataAdapter otuList) throws IOException {
		logIgnoredMetadata(otuList, "Metadata attached to an OTU list have been ignored.");
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
			Set<String> taxonLabels = new HashSet<String>();
			Iterator<String> iterator = otuList.getIDIterator();
			while (iterator.hasNext()) {
				String id = iterator.next();
				writeLineStart(writer, formatToken(createOTULabel(otuList.getOTUStartEvent(id), taxonLabels)));
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
		parameters.getGeneratedLabelsMap().clear();
		parameters.put(EventWriterParameterMap.KEY_GENERATED_LABELS_MAP_ID_TYPE, EventContentType.OTU);
		
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
	
	
	private void writeFormatCommand(MatrixDataAdapter matrix) throws IOException {
		ObjectListDataAdapter tokenSets = matrix.getTokenSets();
		if (tokenSets.getCount() > 0) {
			writeLineStart(writer, COMMAND_NAME_FORMAT);
			Iterator<String> iterator = tokenSets.getIDIterator();
			if (tokenSets.getCount() == 1) {
				TokenSetEventReceiver receiver = new TokenSetEventReceiver(writer, parameters);
				tokenSets.writeData(receiver, iterator.next());
				
				if (receiver.getSingleTokens() != null) {
					writer.write(' ');
					writeKeyValueExpression(FORMAT_SUBCOMMAND_SYMBOLS, VALUE_DELIMITER + receiver.getSingleTokens() + VALUE_DELIMITER);
				}
				if (receiver.getIgnoredMetadata() > 0) {
					logger.addWarning("A token definition of a character matrix contained metadata which has been ignored, "
							+ "since the Nexus format does not support writing such data.");
				}
				
				writer.write(' ');
				if (matrix.containsLongTokens()) {
					writer.write(FORMAT_SUBCOMMAND_TOKENS);
				}
				else {
					writer.write(FORMAT_SUBCOMMAND_NOTOKENS);
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
			String sequenceName = getLinkedOTUNameOTUFirst(matrix.getSequenceStartEvent(id), getReferencedOTUList(document, matrix));
			writeLineStart(writer, formatToken(sequenceName));
			writer.write(' ');
			
			SequenceContentReceiver receiver = new SequenceContentReceiver(
					writer, parameters, "" + COMMENT_START, "" + COMMENT_END, matrix.containsLongTokens());
			matrix.writeSequencePartContentData(receiver, id, 0, matrix.getSequenceLength(id));
			if (receiver.didIgnoreMetadata()) {
				logger.addWarning(receiver.getIgnoredMetadata() + " metadata events nested inside the sequence \"" + sequenceName + 
						"\" have been ignored, since the Nexus format does not supprt such data.");
			}
			
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
			
			writeFormatCommand(matrix);  //TODO Write "newTokens" if necessary.
			writeMatrixCommand(document, matrix, columnCount == -1);
			//TODO Write TAXLABELS if necessary (e.g. if sequences without linked OTUs are present).
			
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
	
	
	private void writeTreesBlock(DocumentDataAdapter document) throws IOException {
		long skippedNetworks = 0;
		boolean treeWritten = false;
		Iterator<TreeNetworkDataAdapter> iterator = document.getTreeNetworkIterator();
		while (iterator.hasNext()) {
			TreeNetworkDataAdapter treeNetwork = iterator.next();
			if (treeNetwork.isTree()) {
				if (!treeWritten) {
					writeBlockStart(BLOCK_NAME_TREES);
					increaseIndention();
					
					//TODO Write NEWTAXA and TAXLABELS if necessary (e.g. if nodes without linked OTUs are present).
					boolean translate = parameters.getBoolean(EventWriterParameterMap.KEY_GENERATE_TRANSLATION_TABLE, true);
					if (translate) {
						//TODO Write translate command (Determine necessary taxa.)
					}
				}
				
				writeLineStart(writer, COMMAND_NAME_TREE);
				writer.write(' ');
				writer.write("someTree");  //TODO Replace by tree name read from adapter, when according event getter is available.
				writer.write(' ');
				writer.write(KEY_VALUE_SEPARATOR);
				writer.write(' ');
				new NewickStringWriter(writer, treeNetwork, getReferencedOTUList(document, treeNetwork), true, parameters).write();  // Also writes line break.
				
				treeWritten = true;
			}
			else {
				skippedNetworks += 1;
			}
		}
		
		if (skippedNetworks > 0) {
			//TODO Log warning
		}
		if (treeWritten) {
			decreaseIndention();
			writeBlockEnd();
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
		writeTreesBlock(document);
		//TODO Write SETS
	}
}
