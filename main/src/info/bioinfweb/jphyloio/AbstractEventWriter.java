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


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;

import info.bioinfweb.commons.SystemUtils;
import info.bioinfweb.commons.log.ApplicationLogger;
import info.bioinfweb.jphyloio.dataadapters.DocumentDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.OTUListDataAdapter;
import info.bioinfweb.jphyloio.events.LinkedOTUEvent;



/**
 * Implements shared functionality for <i>JPhyloIO</i> event writers.
 * 
 * @author Ben St&ouml;ver
 */
public abstract class AbstractEventWriter	implements JPhyloIOEventWriter {
	/**
	 * Writes the line separator, as it is specified in the parameter map or the line separator
	 * of the current operating system, if the map contains no according entry. 
	 * 
	 * @param writer the writer to write the line break to
	 * @param parameters the parameter map possibly containing a line separator definition
	 * @throws IOException if an I/O error occurs while writing to the specified writer
	 */
	public static void writeLineBreak(Writer writer, EventWriterParameterMap parameters) throws IOException {
		writer.write(parameters.getString(EventWriterParameterMap.KEY_LINE_SEPARATOR, SystemUtils.LINE_SEPARATOR));
	}
	
	
	protected OTUListDataAdapter getFirstOTUList(DocumentDataAdapter document, ApplicationLogger logger, String formatName,
			String labeledElements) {
		OTUListDataAdapter result = null;
		Iterator<OTUListDataAdapter> otuListIterator = document.getOTUListIterator();
		if (otuListIterator.hasNext()) {
			result = otuListIterator.next();
		}
		
		if (result != null) {
			logger.addWarning("The specified OTU list(s) will not be written, since the " + formatName
					+	" format does not support this. The first list will though be used to try to label " + labeledElements
					+ " that do not carry a label themselves."); 
		}
		return result;
	}
	
	
	public static String getLinkedOTUName(LinkedOTUEvent linkedOTUEvent, OTUListDataAdapter otuList) {
		String result = linkedOTUEvent.getLabel();
		if (result == null) {
			if (linkedOTUEvent.isOTULinked() && (otuList != null)) {
				result = otuList.getOTUStartEvent(linkedOTUEvent.getOTUID()).getLabel();
			}
			if (result == null) {
				result = linkedOTUEvent.getID();
			}
		}
		return result;
	}
	
	
	@Override
	public void writeDocument(DocumentDataAdapter document, File file, EventWriterParameterMap parameters) throws Exception {
		Writer writer = new BufferedWriter(new FileWriter(file));
		try {
			writeDocument(document, writer, parameters);	
		}
		finally {
			writer.close();
		}
	}
	

	@Override
	public void writeDocument(DocumentDataAdapter document, OutputStream stream, EventWriterParameterMap parameters) throws Exception {		
		writeDocument(document, new BufferedWriter(new OutputStreamWriter(stream)), parameters);		
	}
}
