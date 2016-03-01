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
package info.bioinfweb.jphyloio.formats.phylip;


import java.io.Writer;
import java.util.Iterator;

import info.bioinfweb.jphyloio.AbstractSingleMatrixEventWriter;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.LabelEditingReporter;
import info.bioinfweb.jphyloio.dataadapters.DocumentDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.MatrixDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.OTUListDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.implementations.receivers.SequenceContentReceiver;
import info.bioinfweb.jphyloio.events.LinkedOTUOrOTUsEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.formats.JPhyloIOFormatIDs;



/**
 * Event based writer for the Phylip format.
 * <p>
 * This write is able to write sequence data to Phylip formatted streams. It will ignore any data for phylogenetic
 * trees and networks that are provided by {@link DocumentDataAdapter#getTreeNetworkIterator()}, because the Phylip 
 * format does not support such data.
 * <p>
 * Note that sequence names may have to be edited according to the (length) constrains the Phylip format imposes.
 * According edits can be obtained using the {@link LabelEditingReporter} which is returned via the parameters map
 * (using {@link ReadWriteParameterMap#KEY_LABEL_EDITING_REPORTER}).
 * <p>
 * Since Phylip does not support OTU or taxon lists as well, such a list (if provided by 
 * {@link DocumentDataAdapter#getOTUListIterator()}) will also not be written. OTU definitions (if present) will though 
 * be used, if a sequence with a linked OTU ID but without a label is specified. In such cases 
 * {@link OTUListDataAdapter#getOTUStartEvent(String)} will be used to determine the according OTU label. If that OTU
 * label is also {@code null}, the sequence ID will be used as the sequence name in Phylip.
 * <p>
 * Comments and metadata nested in any of the supported elements will be ignored. 
 * <p>
 * <b>Recognized parameters:</b>
 * <ul>
 *   <li>{@link ReadWriteParameterMap#KEY_SEQUENCE_EXTENSION_TOKEN}</li>
 *   <li>{@link ReadWriteParameterMap#KEY_MAXIMUM_NAME_LENGTH}</li>
 *   <li>{@link ReadWriteParameterMap#KEY_LABEL_EDITING_REPORTER}</li>
 *   <li>{@link ReadWriteParameterMap#KEY_LOGGER}</li>
 * </ul>
 * 
 * @author Ben St&ouml;ver
 * @since 0.0.0
 */
public class PhylipEventWriter extends AbstractSingleMatrixEventWriter implements PhylipConstants {
	//TODO Check if documentation is still valid, when implementation is finished (especially regarding label editing). (Do the same for the FASTA writer.)
	//TODO Add support for KEY_SEQUENCE_EXTENSION_TOKEN

	/**
	 * Creates a new instance of this class. (Instances may be reused for different documents in subsequent calls of
	 * the different {@code #writeDocument()} methods.)
	 */
	public PhylipEventWriter() {
		super(PHYLIP_FORMAT_NAME);
	}


	@Override
	public String getFormatID() {
		return JPhyloIOFormatIDs.PHYLIP_FORMAT_ID;
	}


	private String editSequenceLabel(LinkedOTUOrOTUsEvent event, final ReadWriteParameterMap parameters) {
  	//TODO Mask, replace or remove invalid in Phylip characters: ("(" and ")"), square brackets ("[" and "]"), colon (":"), semicolon (";") and comma (",") 
		return createUniqueLabel(parameters,  //TODO A method considering linked OTUs needs to be used here.
				new UniqueLabelTester() {
					@Override
					public boolean isUnique(String label) {
						return !parameters.getLabelEditingReporter().isLabelUsed(EventContentType.SEQUENCE, label);
					}
				}, 
				event);  // Already considers possible maximum length.
	}

	
	@Override
	protected void writeSingleMatrix(DocumentDataAdapter document, MatrixDataAdapter matrix, 
			Iterator<String> sequenceIDIterator, Writer writer, ReadWriteParameterMap parameters) throws Exception {

		int nameLength = parameters.getInteger(ReadWriteParameterMap.KEY_MAXIMUM_NAME_LENGTH, DEFAULT_NAME_LENGTH);
		String extensionToken = parameters.getString(ReadWriteParameterMap.KEY_SEQUENCE_EXTENSION_TOKEN);
		long maxSequenceLength = determineMaxSequenceLength(matrix);
		
		// Write heading:
    writer.write("\t" + matrix.getSequenceCount() + "\t" + maxSequenceLength);
    writeLineBreak(writer, parameters);
    if ((matrix.getColumnCount() == -1) && (extensionToken == null)) {
    	parameters.getLogger().addWarning("The provided sequences have inequal lengths and filling up sequences was not "
    			+ "specified. The column count written to the Phylip document is the length of the longest sequence. Some "
    			+ "programs may not be able to parse Phylip files with unequal sequence lengths.");
    }
    
    while (sequenceIDIterator.hasNext()) {
    	String id = sequenceIDIterator.next();
    	
    	// Write label:
    	String label = editSequenceLabel(matrix.getSequenceStartEvent(id), parameters);
    	writer.write(label);
    	for (int i = label.length(); i < nameLength; i++) {
				writer.write(' ');
			}
    	
    	// Write sequence:
    	SequenceContentReceiver receiver = new SequenceContentReceiver(writer, parameters, null, null, 
    			matrix.containsLongTokens());
    	matrix.writeSequencePartContentData(receiver, id, 0, matrix.getSequenceLength(id));
    	extendSequence(matrix, id, maxSequenceLength, extensionToken, receiver);
    	
    	writeLineBreak(writer, parameters);
    }
	}
}
