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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import info.bioinfweb.commons.log.ApplicationLogger;
import info.bioinfweb.jphyloio.AbstractEventWriter;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.JPhyloIO;
import info.bioinfweb.jphyloio.LabelEditingReporter;
import info.bioinfweb.jphyloio.dataadapters.AnnotatedDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.DataAdapter;
import info.bioinfweb.jphyloio.dataadapters.DocumentDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.MatrixDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.OTUListDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.ObjectListDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.TreeNetworkDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.TreeNetworkGroupDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.implementations.receivers.BasicEventReceiver;
import info.bioinfweb.jphyloio.events.CharacterDefinitionEvent;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.LinkedLabeledIDEvent;
import info.bioinfweb.jphyloio.events.TokenSetDefinitionEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.exception.InconsistentAdapterDataException;
import info.bioinfweb.jphyloio.formats.JPhyloIOFormatIDs;
import info.bioinfweb.jphyloio.formats.newick.NewickStringWriter;
import info.bioinfweb.jphyloio.formats.nexus.receivers.CharacterSetEventReceiver;
import info.bioinfweb.jphyloio.formats.nexus.receivers.ReferenceOnlySetReceiver;
import info.bioinfweb.jphyloio.formats.nexus.receivers.TokenSetEventReceiver;
import info.bioinfweb.jphyloio.formats.text.TextSequenceContentReceiver;



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
 * The Nexus names that have actually been used in the output can be obtained from the {@link LabelEditingReporter} added
 * to the parameter map using {@link ReadWriteParameterMap#getLabelEditingReporter()}. (A reference to the parameter map 
 * passed to one of the {@code #writeDocument()} methods must be kept by the application code, in order to access the
 * label editing reporter after the document has been written.)
 *  
 * <h3><a name="parameters"></a>Recognized parameters</h3> 
 * <ul>
 *   <li>{@link ReadWriteParameterMap#KEY_APPLICATION_COMMENT}</li>
 *   <li>{@link ReadWriteParameterMap#KEY_SEQUENCE_EXTENSION_TOKEN}</li>
 *   <li>{@link ReadWriteParameterMap#KEY_ALWAYS_WRITE_NEXUS_NODE_LABELS}</li>
 *   <li>{@link ReadWriteParameterMap#KEY_GENERATE_NEXUS_TRANSLATION_TABLE}</li>
 *   <li>{@link ReadWriteParameterMap#KEY_LOGGER}</li>
 *   <li>{@link ReadWriteParameterMap#KEY_GENERATED_LABELS_MAP}</li>
 *   <li>{@link ReadWriteParameterMap#KEY_GENERATED_LABELS_MAP_ID_TYPE}</li>
 * </ul>
 * 
 * @author Ben St&ouml;ver
 * @since 0.0.0
 */
public class NexusEventWriter extends AbstractEventWriter implements NexusConstants {
	private static final String UNDEFINED_OTUS_ID = "\n";  // Should not occur as a real ID;
	
	
	private Writer writer;
	private ReadWriteParameterMap parameters;
	private ApplicationLogger logger;
	private Map<String, NexusMatrixWriteResult> matrixIDToBlockTypeMap = new HashMap<String, NexusMatrixWriteResult>(8);
	private NexusWriterStreamDataProvider streamDataProvider = new NexusWriterStreamDataProvider(this);

	
	@Override
	public String getFormatID() {
		return JPhyloIOFormatIDs.NEXUS_FORMAT_ID;
	}


	protected Writer getWriter() {
		return writer;
	}


	protected ReadWriteParameterMap getParameters() {
		return parameters;
	}


	protected ApplicationLogger getLogger() {
		return logger;
	}


	protected Map<String, NexusMatrixWriteResult> getMatrixIDToBlockTypeMap() {
		return matrixIDToBlockTypeMap;
	}


	protected NexusWriterStreamDataProvider getStreamDataProvider() {
		return streamDataProvider;
	}


	@Override
	protected void writeLineStart(Writer writer, String text) throws IOException {
		super.writeLineStart(writer, text);
	}
	
	
	private void writeInitialLines() throws IOException {
		writer.write(FIRST_LINE);
		writeLineBreak(writer, parameters);
		
		String applicationComment = parameters.getString(ReadWriteParameterMap.KEY_APPLICATION_COMMENT);
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
	
	
	private void logIgnoredMetadata(AnnotatedDataAdapter<?> adapter, String objectName) {
		//TODO Replace by receiver logging ignored metadata
//		if (adapter.getMetadataAdapter() != null) {
//			logger.addWarning(objectName + 
//					" is annotated directly with metadata, which have been ignored, since the Nexus format does not support this.");
//		}
	}
	
	
	private void logMultipleBlocksWarning(String objectName, String blockName) {
		logger.addWarning("This document contains more than one " + objectName + ". Therefore multiple " + blockName 
				+ " blocks have been written into the Nexus output. Not all programs may be able to process Nexus files "
				+ "with multiple blocks of the same type.");
	}
	
	
	public static String formatToken(String token) {
		return NewickStringWriter.formatToken(token, WORD_DELIMITER);
	}
	
	
	protected void writeCommandEnd() throws IOException {
		writer.write(COMMAND_END);
		writeLineBreak(writer, parameters);
	}
	
	
	protected void writeBlockStart(String name) throws IOException {
		writeLineBreak(writer, parameters);  // Add one empty line before each block.
		writeLineStart(writer, BEGIN_COMMAND);
		writer.write(' ');
		writer.write(name);
		writeCommandEnd();
		increaseIndention();
	}
	
	
	protected void writeBlockEnd() throws IOException {
		decreaseIndention();
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
	
	
	private void writeTitleCommand(String label) throws IOException {
		writeLineStart(writer, COMMAND_NAME_TITLE);
		writer.write(' ');
		writer.write(formatToken(label));
		writeCommandEnd();
	}
	
	
	private void writeTitleCommand(LabeledIDEvent startEvent) throws IOException {
		String label = createUniqueLabel(parameters, startEvent);
		writeTitleCommand(label);
		parameters.getLabelEditingReporter().addEdit(startEvent, label);
	}
	
	
	protected void writeLinkCommand(String linkedID, String linkedBlockName, EventContentType linkedContentType) throws IOException {
		writeLineStart(writer, COMMAND_NAME_LINK);
		writer.write(' ');
		writer.write(linkedBlockName);
		writer.write(KEY_VALUE_SEPARATOR);
		writer.write(formatToken(parameters.getLabelEditingReporter().getEditedLabel(linkedContentType, linkedID)));
		writeCommandEnd();
	}
	
	
	private void writeLinkCommand(LinkedLabeledIDEvent startEvent, String linkedBlockName, EventContentType linkedContentType) throws IOException {
		if (startEvent.hasLink()) {
			writeLinkCommand(startEvent.getLinkedID(), linkedBlockName, linkedContentType);
		}
	}
	
	
	private void writeTaxaBlock(OTUListDataAdapter otuList) throws IOException {
		logIgnoredMetadata(otuList, "Metadata attached to an OTU list have been ignored.");
		if (otuList.getCount() > 0) {
			writeBlockStart(BLOCK_NAME_TAXA);
			
			writeTitleCommand(otuList.getStartEvent(parameters));
			
			writeLineStart(writer, COMMAND_NAME_DIMENSIONS);
			writer.write(' ');
			writeKeyValueExpression(DIMENSIONS_SUBCOMMAND_NTAX, Long.toString(otuList.getCount()));
			writeCommandEnd();
			
			writeLineStart(writer, COMMAND_NAME_TAX_LABELS);
			writeLineBreak(writer, parameters);
			increaseIndention();
			increaseIndention();
			BasicEventReceiver<Writer> receiver = new BasicEventReceiver<Writer>(writer, parameters);
			Iterator<String> iterator = otuList.getIDIterator();
			while (iterator.hasNext()) {
				String id = iterator.next();
				writeLineStart(writer, formatToken(createUniqueLabel(parameters, otuList.getObjectStartEvent(id))));
				if (iterator.hasNext()) {
					writeLineBreak(writer, parameters);
				}
				else {
					writeCommandEnd();
				}
				otuList.writeContentData(receiver, id);
			}
			receiver.addIgnoreLogMessage(logger, "one or more OTUs", "Nexus");
			decreaseIndention();
			decreaseIndention();
			
			writeBlockEnd();
		}
		else {
			logger.addWarning("The document contained an emtpty OTU list, which has been ignored.");
		}
	}
	
	
	private void writeTaxaBlocks(DocumentDataAdapter document) throws IOException {
		Iterator<OTUListDataAdapter> otusIterator = document.getOTUListIterator(getParameters());
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
	
	
	private boolean containsInvalidOTULinks(MatrixDataAdapter matrix) {
		Set<String> encounteredOTUs = new HashSet<String>();
		Iterator<String> iterator = matrix.getSequenceIDIterator();
		while (iterator.hasNext()) {
			LinkedLabeledIDEvent event = matrix.getSequenceStartEvent(iterator.next());
			if (event.hasLink()) {
				if (encounteredOTUs.contains(event.getLinkedID())) {
					return true;
				}
				else {
					encounteredOTUs.add(event.getLinkedID());
				}
			}
			else {
				return true;
			}
		}
		return false;
	}
	
	
	private void writeMatrixDimensionsCommand(MatrixDataAdapter matrix, long columnCount) throws IOException {
		writeLineStart(writer, COMMAND_NAME_DIMENSIONS);
		writer.write(' ');
		if (containsInvalidOTULinks(matrix)) {
			writer.write(DIMENSIONS_SUBCOMMAND_NEW_TAXA);
			writer.write(' ');
		}
		writeKeyValueExpression(DIMENSIONS_SUBCOMMAND_NTAX, Long.toString(matrix.getSequenceCount(getParameters())));
		if (columnCount != -1) {
			writer.write(' ');
			writeKeyValueExpression(DIMENSIONS_SUBCOMMAND_NCHAR, Long.toString(columnCount));
		}
		writeCommandEnd();
	}

	
	
	private void writeFormatCommand(MatrixDataAdapter matrix) throws IOException {
		ObjectListDataAdapter<TokenSetDefinitionEvent> tokenSets = matrix.getTokenSets();
		if (tokenSets.getCount() > 0) {
			writeLineStart(writer, COMMAND_NAME_FORMAT);
			Iterator<String> iterator = tokenSets.getIDIterator();
			if (tokenSets.getCount() == 1) {
				TokenSetEventReceiver receiver = new TokenSetEventReceiver(getStreamDataProvider());
				
				String dataType;
				String tokenSetID = iterator.next();
				switch (tokenSets.getObjectStartEvent(tokenSetID).asTokenSetDefinitionEvent().getSetType()) {
					case DISCRETE:
						dataType = FORMAT_VALUE_STANDARD_DATA_TYPE;
						break;
					case NUCLEOTIDE:
						dataType = FORMAT_VALUE_NUCLEOTIDE_DATA_TYPE;
						break;
					case DNA:
						dataType = FORMAT_VALUE_DNA_DATA_TYPE;
						break;
					case RNA:
						dataType = FORMAT_VALUE_RNA_DATA_TYPE;
						break;
					case AMINO_ACID:
						dataType = FORMAT_VALUE_PROTEIN_DATA_TYPE;
						break;
					case CONTINUOUS:
						dataType = FORMAT_VALUE_CONTINUOUS_DATA_TYPE;
						break;
					default:  // UNKNOWN
						dataType = null;
						break;
				}
				
				if (dataType != null) {
					writer.write(' ');
					NexusEventWriter.writeKeyValueExpression(writer, FORMAT_SUBCOMMAND_DATA_TYPE, dataType);
				}			
				
				tokenSets.writeContentData(receiver, tokenSetID);
				
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
					writer.write(FORMAT_SUBCOMMAND_NO_TOKENS);
				}
			}
			else {  // MrBayes extension (or exception if according parameter is set?)
				
			}
			writeCommandEnd();
		}
	}
	
	
	/**
	 * Writes the TAXLABELS command inside a CHARACTERS or UNALIGNED block. New taxa added here are created either for
	 * sequences without a linked OTU or if more than one sequence is linked to the same OTU (which is valid in JPhyloIO
	 * but not in Nexus). 
	 * 
	 * @param matrix the matrix to be writte
	 * @throws IOException
	 */
	private void writeMatrixTaxLabelsCommand(MatrixDataAdapter matrix) throws IOException {
		final LabelEditingReporter reporter = parameters.getLabelEditingReporter(); 
		boolean beforeFirst = true;
		boolean anyWritten = false;
		Set<String> encounteredOTUs = new HashSet<String>();
		Iterator<String> iterator = matrix.getSequenceIDIterator();
		while (iterator.hasNext()) {
			LinkedLabeledIDEvent event = matrix.getSequenceStartEvent(iterator.next());
			boolean createNewLabel = false;
			if (event.hasLink()) {
				if (encounteredOTUs.contains(event.getLinkedID())) {
					createNewLabel = true;
				}
				else {
					encounteredOTUs.add(event.getLinkedID());
				}
			}
			else {
				createNewLabel = true;
			}
			
			String label;
			if (createNewLabel) {
				if (beforeFirst) {
					writeLineStart(writer, COMMAND_NAME_TAX_LABELS);
					writeLineBreak(writer, parameters);
					increaseIndention();
					increaseIndention();
					anyWritten = true;
				}
				else {
					writeLineBreak(writer, parameters);
					beforeFirst = false;
				}
				label = createUniqueLabel(
						parameters, 
						new NoEditUniqueLabelHandler() {
							@Override
							public boolean isUnique(String label) {
								return !reporter.isLabelUsed(EventContentType.OTU, label) && 
										!reporter.isLabelUsed(EventContentType.SEQUENCE, label);
							}
						}, event);
				writeLineStart(writer, formatToken(label));
			}
			else {
				label = reporter.getEditedLabel(EventContentType.OTU, event.getLinkedID());
				if (label == null) {
					throw new InconsistentAdapterDataException("The sequence with the ID " + event.getID() + 
							" is referencing an OTU with the ID " + event.getLinkedID() + " which could not be found.");
				}
			}
			reporter.addEdit(event, label);
		}
		if (anyWritten) {
			writeCommandEnd();
			writeLineStart(writer, "" + COMMENT_START);
			writer.write("These additional taxon definitions were automatically added by JPhyloIO, because sequences without linked taxa had to be written or more than one sequence was linked to the same taxon (which is both invalid in Nexus).");
			writer.write(COMMENT_END);
			writeLineBreak(writer, parameters);
			decreaseIndention();
			decreaseIndention();
		}
	}
	
	
	private void writeCharStateLabelsCommand(MatrixDataAdapter matrix) throws IOException {
		final ObjectListDataAdapter<CharacterDefinitionEvent> definitions = matrix.getCharacterDefinitions();
		
		Iterator<String> iterator = definitions.getIDIterator();
		if (iterator.hasNext()) {
			writeLineStart(writer, COMMAND_NAME_CHAR_STATE_LABELS);
			writeLineBreak(writer, parameters);
			increaseIndention();
			increaseIndention();
			
			while (iterator.hasNext()) {  //TODO Should only definitions with labels be written?
				CharacterDefinitionEvent event = definitions.getObjectStartEvent(iterator.next());
				String label = createUniqueLabel(parameters, event);
				parameters.getLabelEditingReporter().addEdit(event, label);
				writeLineStart(writer, event.getIndex() + " " + label);
				if (iterator.hasNext()) {
					writer.write(ELEMENT_SEPARATOR);
					writeLineBreak(writer, parameters);
				}
			}
			
			decreaseIndention();
			decreaseIndention();
			writeCommandEnd();
		}
	}

	
	private void writeMatrixCommand(DocumentDataAdapter document, MatrixDataAdapter matrix, long alignmentLength, 
			String extensionToken)	throws IOException {
		
		LabelEditingReporter reporter = parameters.getLabelEditingReporter();
		writeLineStart(writer, COMMAND_NAME_MATRIX);
		writeLineBreak(writer, parameters);
		
		increaseIndention();
		increaseIndention();
		Iterator<String> iterator = matrix.getSequenceIDIterator();
		while (iterator.hasNext()) {
			String id = iterator.next();
			String sequenceName = reporter.getEditedLabel(EventContentType.SEQUENCE, id);
			if (sequenceName == null) {
				throw new InternalError("Writing TAXLABELS and MATRIX command is not consistent.");
			}
			
			writeLineStart(writer, formatToken(sequenceName));
			writer.write(' ');
			
			TextSequenceContentReceiver receiver = new TextSequenceContentReceiver(
					writer, parameters, "" + COMMENT_START, "" + COMMENT_END, matrix.containsLongTokens());
			matrix.writeSequencePartContentData(receiver, id, 0, matrix.getSequenceLength(id));
			if (receiver.didIgnoreMetadata()) {
				logger.addWarning(receiver.getIgnoredMetadata() + " metadata events nested inside the sequence \"" + sequenceName + 
						"\" have been ignored, since the Nexus format does not supprt such data.");
			}
			if (extensionToken != null) {
				long additionalTokens = alignmentLength - matrix.getSequenceLength(id);
				for (long i = 0; i < additionalTokens; i++) {
					if (matrix.containsLongTokens()) {
						writer.write(' ');
					}
					writer.write(extensionToken);
				}
			}
			
			if (iterator.hasNext()) {
				if (alignmentLength == -1) {
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
	private NexusMatrixWriteResult writeCharactersUnalignedBlock(DocumentDataAdapter document, MatrixDataAdapter matrix, ReadWriteParameterMap parameters) throws IOException {
		logIgnoredMetadata(matrix, "A character matrix");
		if (matrix.getSequenceCount(getParameters()) > 0) {
			long columnCount = matrix.getColumnCount(getParameters());
			String extensionToken = parameters.getString(ReadWriteParameterMap.KEY_SEQUENCE_EXTENSION_TOKEN);
			if ((columnCount == -1) && (extensionToken != null)) {
				columnCount = determineMaxSequenceLength(matrix, parameters);  // -1 will not be returned, since it was already checked, that at least one sequence is contained.
			}
			
			NexusMatrixWriteResult result;
			if (columnCount == -1) {
				result = NexusMatrixWriteResult.UNALIGNED;
				writeBlockStart(BLOCK_NAME_UNALIGNED);
			}
			else {
				result = NexusMatrixWriteResult.CHARACTERS;
				writeBlockStart(BLOCK_NAME_CHARACTERS);
			}
			
			LinkedLabeledIDEvent startEvent = matrix.getStartEvent(parameters);
			writeTitleCommand(startEvent);
			writeLinkCommand(startEvent, BLOCK_NAME_TAXA, EventContentType.OTU_LIST);
			
			writeMatrixDimensionsCommand(matrix, columnCount);
			writeFormatCommand(matrix);
			writeMatrixTaxLabelsCommand(matrix);
			writeCharStateLabelsCommand(matrix);
			writeMatrixCommand(document, matrix, columnCount, extensionToken);
			
			writeBlockEnd();
			matrixIDToBlockTypeMap.put(matrix.getStartEvent(parameters).getID(), result);			
			return result;
		}
		else {
			logger.addWarning("The document contained an emtpty character matrix, which has been ignored.");
			return NexusMatrixWriteResult.NONE;
		}
	}
	
	
	private void writeCharactersUnalignedBlocks(DocumentDataAdapter document, ReadWriteParameterMap parameters) throws IOException {
		boolean charactersWritten = false;
		boolean unalignedWritten = false;
		
		Iterator<MatrixDataAdapter> matrixIterator = document.getMatrixIterator(getParameters());
		while (matrixIterator.hasNext()) {
			switch (writeCharactersUnalignedBlock(document, matrixIterator.next(), parameters)) {
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
	
	
	private String getOTUsIDForTreeGroup(LinkedLabeledIDEvent event) {
		if (event.hasLink()) {
			return event.getLinkedID();
		}
		else {
			return UNDEFINED_OTUS_ID;
		}
	}
	
	
	private String createUniqueTreeLabel(LabeledIDEvent event, final Set<String> usedLabels) {
		String result = createUniqueLabel(
				parameters, 
				new NoEditUniqueLabelHandler() {
					@Override
					public boolean isUnique(String label) {
						return !usedLabels.contains(label);
					}
				}, 
				event);
		usedLabels.add(result);
		return result;
	}
	
	
	private Map<String, Long> createOTUIndexMap(OTUListDataAdapter otus) {
		Map<String, Long> result = new HashMap<String, Long>();
		long index = 1;  // Nexus taxon indices start with 1.
		Iterator<String> iterator = otus.getIDIterator();
		while (iterator.hasNext()) {
			result.put(iterator.next(), index);
			index++;
		}
		return result;
	}
	
	
	private void writeTranslateCommand(Map<String, Long> indexMap) throws IOException {
		if (!indexMap.isEmpty()) {
			LabelEditingReporter reporter = parameters.getLabelEditingReporter();
			writeLineStart(writer, COMMAND_NAME_TRANSLATE);
			writeLineBreak(writer, parameters);
			increaseIndention();
			increaseIndention();
			Iterator<String> iterator = indexMap.keySet().iterator();
			while (iterator.hasNext()) {
				String id = iterator.next();
				writeLineStart(writer, indexMap.get(id).toString());
				writer.write(' ');
				String label = reporter.getEditedLabel(EventContentType.OTU, id);
				if (label == null) {
					throw new InternalError("No label definition found for OTU ID " + id + ".");  // Should not happen.
				}
				writer.write(formatToken(label));
				if (iterator.hasNext()) {
					writer.write(ELEMENT_SEPARATOR);
					writeLineBreak(writer, parameters);
				}
				else {
					writeCommandEnd();					
				}
			}
			decreaseIndention();
			decreaseIndention();
		}
	}
	
	
	private void writeTreesBlocks(DocumentDataAdapter document) throws IOException {
		long skippedNetworks = 0;
		Iterator<TreeNetworkGroupDataAdapter> groupIterator = document.getTreeNetworkGroupIterator(getParameters());
		while (groupIterator.hasNext()) {
			TreeNetworkGroupDataAdapter group = groupIterator.next();
			LinkedLabeledIDEvent groupStartEvent = group.getStartEvent(parameters);
			String currentOTUsID = getOTUsIDForTreeGroup(groupStartEvent);
			OTUListDataAdapter currentOTUList = null;
			if (!UNDEFINED_OTUS_ID.equals(currentOTUsID)) {
				currentOTUList = document.getOTUList(getParameters(), currentOTUsID);
			}
			else if (document.getOTUListCount(getParameters()) > 1) {
				parameters.getLogger().addWarning("One or more trees were written to the Nexus document, which do not reference "
						+ "any TAXA block. Since the created Nexus document contains more than one TAXA block, this file may not be "
						+ "readable by some applications.");
			}

			// Write block start:
			writeBlockStart(BLOCK_NAME_TREES);
			writeTitleCommand(groupStartEvent);
			writeLinkCommand(groupStartEvent, BLOCK_NAME_TAXA, EventContentType.OTU_LIST);  // Writes only if a block is linked.
			
			// Write trees:
			Set<String> usedLabels = new HashSet<String>();
			Iterator<TreeNetworkDataAdapter> treeIterator = group.getTreeNetworkIterator();
			while (treeIterator.hasNext()) {
				Map<String, Long> indexMap = null;
				if (currentOTUList != null) {
					boolean translate = parameters.getBoolean(ReadWriteParameterMap.KEY_GENERATE_NEXUS_TRANSLATION_TABLE, false);
					boolean alwaysUseLabels = parameters.getBoolean(ReadWriteParameterMap.KEY_ALWAYS_WRITE_NEXUS_NODE_LABELS, false);
					if (translate || !alwaysUseLabels) {
						indexMap = createOTUIndexMap(currentOTUList);
					}
					if (translate) {  //TODO If no user defined translation labels are possible, the TRANSLATE command is unnecessary, since using the indices of the TAXA block is anyway possible.
						writeTranslateCommand(indexMap);  // Always writes translation table for all taxa, event if they are not contained in the trees of this block.
					}
					if (alwaysUseLabels) {
						indexMap = null;  // Delete possibly generated index map again.
					}
				}
				
				TreeNetworkDataAdapter treeNetwork = treeIterator.next();
				if (treeNetwork.isTree()) {
					writeLineStart(writer, COMMAND_NAME_TREE);
					writer.write(' ');
					writer.write(formatToken(createUniqueTreeLabel(treeNetwork.getStartEvent(parameters), usedLabels)));  // createUniqueLabel() can't be used here, because equal labels in different TREES blocks shall be allowed.
					writer.write(' ');
					writer.write(KEY_VALUE_SEPARATOR);
					writer.write(' ');
					new NewickStringWriter(writer, treeNetwork, new NexusNewickWriterNodeLabelProcessor(
							currentOTUList, indexMap, parameters), parameters).write();  // Also writes line break. indexMap may be null.
				}
				else {
					skippedNetworks += 1;
				}
			}
			
			writeBlockEnd();
		}
		
		if (skippedNetworks > 0)  {
			parameters.getLogger().addWarning("The document data contained " + skippedNetworks + 
					" phylogenetic network definitions, which have not been written to the Nexus document, since it only supports trees.");
		}
	}
	
	
	private void writeSetsBlocks(DocumentDataAdapter document) throws IOException {
		// Write taxon sets:
		new AbstractNexusSetWriter(getStreamDataProvider(), COMMAND_NAME_TAXON_SET, EventContentType.OTU_LIST, document.getOTUListIterator(getParameters()), 
				new ReferenceOnlySetReceiver(getStreamDataProvider(), EnumSet.of(EventContentType.OTU, EventContentType.OTU_SET))) {
			
			@Override
			protected ObjectListDataAdapter<LinkedLabeledIDEvent> getSets(DataAdapter<? extends LabeledIDEvent> dataSource) {
				return ((OTUListDataAdapter)dataSource).getOTUSets();
			}
			
			@Override
			protected String getLinkedBlockName(DataAdapter<? extends LabeledIDEvent> dataSource) {
				return BLOCK_NAME_TAXA;
			}
		}.write();

		// Write character sets:
		new AbstractNexusSetWriter(getStreamDataProvider(), COMMAND_NAME_CHAR_SET, EventContentType.ALIGNMENT, document.getMatrixIterator(getParameters()), 
				new CharacterSetEventReceiver(getStreamDataProvider())) {
			
			@Override
			protected ObjectListDataAdapter<LinkedLabeledIDEvent> getSets(DataAdapter<? extends LabeledIDEvent> dataSource) {
				return ((MatrixDataAdapter)dataSource).getCharacterSets();
			}
			
			@Override
			protected String getLinkedBlockName(DataAdapter<? extends LabeledIDEvent> dataSource) {
				return matrixIDToBlockTypeMap.get(dataSource.getStartEvent(parameters).getID()).toBlockName();
			}
		}.write();

		// Write tree sets:
		new AbstractNexusSetWriter(getStreamDataProvider(), COMMAND_NAME_TREE_SET, EventContentType.TREE_NETWORK_GROUP, 
				document.getTreeNetworkGroupIterator(getParameters()),	
				new ReferenceOnlySetReceiver(getStreamDataProvider(), EnumSet.of(EventContentType.TREE, EventContentType.TREE_NETWORK_SET),
						EnumSet.of(EventContentType.NETWORK))) {
			
			@Override
			protected ObjectListDataAdapter<LinkedLabeledIDEvent> getSets(DataAdapter<? extends LabeledIDEvent> dataSource) {
				return ((TreeNetworkGroupDataAdapter)dataSource).getTreeSets();
			}
			
			@Override
			protected String getLinkedBlockName(DataAdapter<? extends LabeledIDEvent> dataSource) {
				return BLOCK_NAME_TREES;
			}
		}.write();
	}
	
	
	@Override
	public void writeDocument(DocumentDataAdapter document, Writer writer, ReadWriteParameterMap parameters) throws IOException {
		this.writer = writer;
		this.parameters = parameters;
		logger = parameters.getLogger();
		
		parameters.getLabelEditingReporter().clear();
		writeInitialLines();
		logIgnoredMetadata(document, "The document");
		writeTaxaBlocks(document);
		writeCharactersUnalignedBlocks(document, parameters);
		writeTreesBlocks(document);
		writeSetsBlocks(document);
	}
}
