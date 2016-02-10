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
package info.bioinfweb.jphyloio.formats.fasta;


import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

import info.bioinfweb.commons.SystemUtils;
import info.bioinfweb.commons.log.ApplicationLogger;
import info.bioinfweb.jphyloio.AbstractEventWriter;
import info.bioinfweb.jphyloio.EventWriterParameterMap;
import info.bioinfweb.jphyloio.dataadapters.DocumentDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.MatrixDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.OTUListDataAdapter;
import info.bioinfweb.jphyloio.events.LinkedOTUEvent;



/**
 * Event based writer for the FASTA format.
 * <p>
 * This write is able to write sequence data to FASTA formatted streams. It will ignore any data for phylogenetic
 * trees and networks that are provided by {@link DocumentDataAdapter#getTreeNetworkIterator()}, because the FASTA 
 * format does not support such data. 
 * <p>
 * Since FASTA does not support OTU or taxon lists as well, such a list (if provided by 
 * {@link DocumentDataAdapter#getOTUListIterator()}) will also not be written. OTU definitions of the first list
 * (if present) will though be used, if a sequence with a linked OTU ID but without a label is specified. In such cases 
 * {@link OTUListDataAdapter#getOTUStartEvent(String)} will be used to determine the according OTU label. If that OTU
 * label is also {@code null}, the sequence ID will be used as the sequence name in FASTA.
 * <p>
 * Comments and metadata nested in any of the supported elements will be ignored, with the only exception of comments
 * before the first token of a sequence. Such comments will be included in FASTA, since this is only valid position
 * for comments in the format. 
 * 
 * @author Ben St&ouml;ver
 */
public class FASTAEventWriter extends AbstractEventWriter implements FASTAConstants {
	private void writeSequenceName(String sequenceName, Writer writer, FASTASequenceEventReceiver receiver) throws IOException {
		if (receiver.getCharsPerLineWritten() > 0) {
			receiver.writeNewLine(writer);
		}
		writer.write(NAME_START_CHAR + sequenceName + SystemUtils.LINE_SEPARATOR);
	}
	
	
	@Override
	public void writeDocument(DocumentDataAdapter document, Writer writer,
			EventWriterParameterMap parameters) throws Exception {
		
		ApplicationLogger logger = parameters.getApplicationLogger(EventWriterParameterMap.KEY_LOGGER);
		OTUListDataAdapter firstOTUList = getFirstOTUList(document, logger, "FASTA", "sequences");
		Iterator<MatrixDataAdapter> matrixIterator = document.getMatrixIterator();
		if (matrixIterator.hasNext()) {
			MatrixDataAdapter matrixDataAdapter = matrixIterator.next();
			Iterator<String> sequenceIDIterator = matrixDataAdapter.getSequenceIDIterator();
			if (sequenceIDIterator.hasNext()) {
				FASTASequenceEventReceiver eventReceiver = new FASTASequenceEventReceiver(writer, parameters, matrixDataAdapter, 
						parameters.getLong(EventWriterParameterMap.KEY_LINE_LENGTH, DEFAULT_LINE_LENGTH));
				
				while (sequenceIDIterator.hasNext()) {
					String id = sequenceIDIterator.next();
					LinkedOTUEvent sequenceEvent = matrixDataAdapter.getSequenceStartEvent(id);
					writeSequenceName(getLinkedOTUName(sequenceEvent, firstOTUList), writer, eventReceiver);
					eventReceiver.setAllowCommentsBeforeTokens(true);  // Writing starts with 0 each time.
					matrixDataAdapter.writeSequencePartContentData(eventReceiver, id, 0, matrixDataAdapter.getSequenceLength(id));
				}
				
				if (eventReceiver.didIgnoreComments()) {
					logger.addWarning(eventReceiver.getIgnoredComments() + " comment events inside the matrix could not be written, "
							+ "because FASTA supports only comments at the beginning of sequences.");
				}
				if (eventReceiver.didIgnoreMetadata()) {
					logger.addWarning(eventReceiver.getIgnoredMetadata() + " metadata events inside the matrix could not be written, "
							+ "because FASTA does not support metadata.");
				}
			}
			else {
				logger.addWarning("An empty FASTA file was written since the first matrix model adapter did not provide any sequences.");
			}
			
			if (matrixIterator.hasNext()) {
				logger.addWarning("The specified document adapter contained more than one character matrix adapter. Since the FASTA "
						+ "format does not support multiple alignments in one file, only the first matrix was written.");
			}
		}
		else {
			logger.addWarning("An empty FASTA file was written since the specified document adapter contained contained no matrices.");
		}
		
		if (document.getTreeNetworkIterator().hasNext()) {
			logger.addWarning(
					"The specified tree or network definitions(s) will not be written, since the FASTA format does not support this."); 
		}
	}
}
