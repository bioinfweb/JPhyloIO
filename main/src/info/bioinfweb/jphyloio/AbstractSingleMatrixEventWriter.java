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
package info.bioinfweb.jphyloio;


import info.bioinfweb.commons.log.ApplicationLogger;
import info.bioinfweb.jphyloio.dataadapters.DocumentDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.MatrixDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.OTUListDataAdapter;
import info.bioinfweb.jphyloio.events.LinkedOTUOrOTUsEvent;

import java.io.Writer;
import java.util.Iterator;



/**
 * Default implementation for writers that can only write a single matrix (multiple sequence alignment) to their
 * target format, because that format does not support additional data.
 * <p>
 * This abstract implementation will log according warnings for the other data that has not been written and than
 * calls {@link #writeSingleMatrix(DocumentDataAdapter, MatrixDataAdapter, Iterator, Writer, ReadWriteParameterMap)}
 * which must be implemented by inherited classes accordingly.
 * 
 * @author Ben St&ouml;ver
 * @since 0.0.0
 */
public abstract class AbstractSingleMatrixEventWriter extends AbstractEventWriter {
	private String formatName;
	

	/**
	 * Creates a new instance of this class.
	 * 
	 * @param formatName the name of the format the inherited implementation writes to, which will be used for the log
	 *        messages generated by this abstract implementation.
	 */
	public AbstractSingleMatrixEventWriter(String formatName) {
		super();
		this.formatName = formatName;
	}
	
	
	/**
	 * Implementations of this method should write a single matrix to their target format.
	 * 
	 * @param document the document containing the matrix
	 * @param matrix the (non-empty) matrix to be written
	 * @param sequenceIDIterator an iterator oder all sequences in {@code matrix} positioned before the first sequence
	 * @param writer the writer to write the data to
	 * @param parameters the parameter map for the writer implementation
	 * @throws Exception if the implementing writer throws an exception
	 */
	protected abstract void writeSingleMatrix(DocumentDataAdapter document, MatrixDataAdapter matrix, 
			Iterator<String> sequenceIDIterator, Writer writer,	ReadWriteParameterMap parameters) throws Exception;


	protected String editSequenceOrNodeLabel(final LinkedOTUOrOTUsEvent event, final ReadWriteParameterMap parameters, 
			OTUListDataAdapter otuList) {
		
		return createUniqueLinkedOTULabel(parameters,
				new UniqueLabelHandler() {
					@Override
					public boolean isUnique(String label) {
						return !parameters.getLabelEditingReporter().isLabelUsed(event.getType().getContentType(), label);
					}

					@Override
					public String editLabel(String label) {
						return maskReservedLabelCharacters(label);
					}
				}, 
				event, otuList, false);  // Already considers possible maximum length.
	}
	
	
	protected abstract String maskReservedLabelCharacters(String label);

	
	@Override
	public void writeDocument(DocumentDataAdapter document, Writer writer,
			ReadWriteParameterMap parameters) throws Exception {
		
		ApplicationLogger logger = parameters.getLogger();
		logIngnoredOTULists(document, logger, formatName, "sequences");
		Iterator<MatrixDataAdapter> matrixIterator = document.getMatrixIterator();
		if (matrixIterator.hasNext()) {
			MatrixDataAdapter matrixDataAdapter = matrixIterator.next();
			Iterator<String> sequenceIDIterator = matrixDataAdapter.getSequenceIDIterator();
			if (sequenceIDIterator.hasNext()) {
				writeSingleMatrix(document, matrixDataAdapter, sequenceIDIterator, writer, parameters);
			}
			else {
				logger.addWarning("An empty " + formatName + 
						" file was written since the first matrix model adapter did not provide any sequences.");
			}
			
			if (matrixIterator.hasNext()) {
				logger.addWarning("The specified document adapter contained more than one character matrix adapter. Since the "
						 + formatName	+ " format does not support multiple alignments in one file, only the first matrix was written.");
			}
		}
		else {
			logger.addWarning("An empty " + formatName + 
					" file was written since the specified document adapter contained contained no matrices.");
		}
		
		if (document.getTreeNetworkIterator().hasNext()) {
			logger.addWarning("The specified tree or network definitions(s) will not be written, since the " + formatName + 
					" format does not support this."); 
		}
	}	
}
