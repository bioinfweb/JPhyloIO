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
import info.bioinfweb.jphyloio.AbstractEventWriter;
import info.bioinfweb.jphyloio.EventWriterParameterMap;
import info.bioinfweb.jphyloio.dataadapters.DocumentDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.JPhyloIOEventReceiver;
import info.bioinfweb.jphyloio.dataadapters.MatrixDataAdapter;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.LinkedOTUEvent;



public class FASTAEventWriter extends AbstractEventWriter implements FASTAConstants {
	public void writeSequenceName(String sequenceName, Writer writer, FASTASequenceEventReceiver receiver) throws IOException {
		if (receiver.getCharsPerLineWritten() > 0) {
			receiver.writeNewLine(writer);
		}
		writer.write(NAME_START_CHAR + sequenceName + SystemUtils.LINE_SEPARATOR);
	}
	
	
	@Override
	public void writeDocument(DocumentDataAdapter document, Writer writer,
			EventWriterParameterMap parameters) throws Exception {
		
		//TODO Create possible OTU map
		
		Iterator<MatrixDataAdapter> matrixIterator = document.getMatrixIterator();
		if (matrixIterator.hasNext()) {
			MatrixDataAdapter matrixDataAdapter = matrixIterator.next();
			FASTASequenceEventReceiver eventReceiver = new FASTASequenceEventReceiver(writer, matrixDataAdapter, 
					parameters.getLong(EventWriterParameterMap.KEY_LINE_LENGTH, DEFAULT_LINE_LENGTH));
			
			Iterator<String> sequenceIDIterator = matrixDataAdapter.getSequenceIDIterator();
			if (sequenceIDIterator.hasNext()) {
				while (sequenceIDIterator.hasNext()) {
					String id = sequenceIDIterator.next();
					LinkedOTUEvent sequenceEvent = matrixDataAdapter.getSequenceStartEvent(id);
					writeSequenceName(sequenceEvent.getLabel(), writer, eventReceiver);  //TODO Use OTU label or ID, if label is null.
					//TODO Possibly write sequence comments
					matrixDataAdapter.writeSequencePartContentData(eventReceiver, id, 0, matrixDataAdapter.getSequenceLength(id));
				}
			}
			else {
				//TODO Log warning that there was no sequence to be written.
			}
		}
		else {
			//TODO Log warning that there was no matrix to be written.
		}
	}
}
