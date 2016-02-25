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

import info.bioinfweb.commons.SystemUtils;
import info.bioinfweb.commons.log.ApplicationLogger;
import info.bioinfweb.jphyloio.dataadapters.DocumentDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.LinkedOTUsDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.OTUListDataAdapter;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.LinkedOTUOrOTUsEvent;



/**
 * Implements shared functionality for <i>JPhyloIO</i> event writers.
 * 
 * @author Ben St&ouml;ver
 */
public abstract class AbstractEventWriter	implements JPhyloIOEventWriter {
	public static final String EDITED_LABEL_SEPARATOR = "_";

	
	protected static interface UniqueLabelTester {
		public boolean isUnique(String label);
	}
	
	
	private String indention = "";
	
	
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
	
	
	public static void logIngnoredOTULists(DocumentDataAdapter document, ApplicationLogger logger, String formatName,
			String labeledElements) {
		
		if (document.getOTUListIterator().hasNext()) {
			logger.addWarning("The specified OTU list(s) will not be written, since the " + formatName
					+	" format does not support this. Referenced lists will though be used to try to label " + labeledElements
					+ " if necessary."); 
		}
	}
	
	
	public static String getLabeledIDName(LabeledIDEvent event) {
		if (event.hasLabel()) {
			return event.getLabel();
		}
		else {
			return event.getID();
		}
	}
	
	
	/**
	 * Determines the name (label) to be used for the specified event when writing its data to a file. The name is
	 * tried to be set by the following properties in decreasing priority:
	 * <ol>
	 *   <li>the label of {@code linkedOTUEvent} if present</li>
	 *   <li>the label of the linked OTU event, if any is linked and carries a label</li>
	 *   <li>the ID of {@code linkedOTUEvent}</li>
	 * </ol>
	 * 
	 * @param linkedOTUEvent the event defining the name
	 * @param otuList the data adapter providing the OTU data
	 * @return a string representing the specified event as described
	 * @see #getLinkedOTUNameOTUFirst(LinkedOTUOrOTUsEvent, OTUListDataAdapter)
	 */
	public static String getLinkedOTUNameOwnFirst(LinkedOTUOrOTUsEvent linkedOTUEvent, OTUListDataAdapter otuList) {
		String result = linkedOTUEvent.getLabel();
		if (result == null) {
			if (linkedOTUEvent.isOTUOrOTUsLinked() && (otuList != null)) {
				result = otuList.getOTUStartEvent(linkedOTUEvent.getOTUOrOTUsID()).getLabel();
			}
			if (result == null) {
				result = linkedOTUEvent.getID();
			}
		}
		return result;
	}
	
	
	/**
	 * Determines the name (label) to be used for the specified event when writing its data to a file. The name is
	 * tried to be set by the following properties in decreasing priority:
	 * <ol>
	 *   <li>the label of the linked OTU event, if any is linked and carries a label</li>
	 *   <li>the ID of the linked OTU event, if any is linked</li>
	 *   <li>the label of {@code linkedOTUEvent} if present</li>
	 *   <li>the ID of {@code linkedOTUEvent}</li>
	 * </ol>
	 * 
	 * @param linkedOTUEvent the event defining the name
	 * @param otuList the data adapter providing the OTU data
	 * @return a string representing the specified event as described
	 * @see #getLinkedOTUNameOwnFirst(LinkedOTUOrOTUsEvent, OTUListDataAdapter)
	 */
	public static String getLinkedOTUNameOTUFirst(LinkedOTUOrOTUsEvent linkedOTUEvent, OTUListDataAdapter otuList) {
		if (linkedOTUEvent.isOTUOrOTUsLinked() && (otuList != null)) {
			return getLabeledIDName(otuList.getOTUStartEvent(linkedOTUEvent.getOTUOrOTUsID()));
		}
		else {
			return getLabeledIDName(linkedOTUEvent);
		}
	}
	
	
	public static OTUListDataAdapter getReferencedOTUList(DocumentDataAdapter document, LinkedOTUsDataAdapter source) {
		OTUListDataAdapter result = null;
		String otuListID = source.getStartEvent().getOTUOrOTUsID();
		if (otuListID != null) {
			result = document.getOTUList(otuListID);
		}
		return result;
	}
	
	
	public static ApplicationLogger getLogger(EventWriterParameterMap parameters) {
		return parameters.getApplicationLogger(EventWriterParameterMap.KEY_LOGGER);
	}
	
	
	protected String getIndention() {
		return indention;
	}
	
	
	protected void writeLineStart(Writer writer, String text) throws IOException {
		if (indention.length() > 0) {
			writer.write(indention);
		}
		writer.write(text);
	}
	
	
	protected void increaseIndention() {
		indention += "\t";
	}
	
	
	protected void decreaseIndention() {
		if (indention.length() > 0) {
			indention = indention.substring(1);
		}
	}
	
	
	protected String createUniqueLabel(EventWriterParameterMap parameters, UniqueLabelTester tester, LabeledIDEvent event) {
		String result = getLabeledIDName(event);
		if (!tester.isUnique(result)) {
			if (event.hasLabel()) {
				result = event.getID() + EDITED_LABEL_SEPARATOR + event.getLabel();
			}
			
			if (!tester.isUnique(result)) {
				long suffix = 2;
				String editedResult;
				do {
					editedResult = result + EDITED_LABEL_SEPARATOR + suffix;
					suffix++;
				}	while (!tester.isUnique(editedResult));
				result = editedResult;
			}
		}
		
		parameters.getLabelEditingReporter().addEdit(event, result);
		return result;
	}
	
	
	protected String createUniqueLabel(final EventWriterParameterMap parameters, final LabeledIDEvent event) {
		return createUniqueLabel(parameters, new UniqueLabelTester() {
					@Override
					public boolean isUnique(String label) {
						return !parameters.getLabelEditingReporter().isLabelUsed(event.getType().getContentType(), label);
					}
				}, 
				event);
	}
	
	
//	protected String createUniqueLabel(EventWriterParameterMap parameters, LabeledIDEvent event) {
//		LabelEditingReporter reporter = parameters.getLabelEditingReporter();
//		
//		String result = getLabeledIDName(event);
//		if (reporter.isLabelUsed(event.getType().getContentType(), result)) {
//			if (event.hasLabel()) {
//				result = event.getID() + EDITED_LABEL_SEPARATOR + event.getLabel();
//			}
//			
//			if (reporter.isLabelUsed(event.getType().getContentType(), result)) {
//				long suffix = 2;
//				String editedResult;
//				do {
//					editedResult = result + EDITED_LABEL_SEPARATOR + suffix;
//					suffix++;
//				}	while (reporter.isLabelUsed(event.getType().getContentType(), editedResult));
//				result = editedResult;
//			}
//		}
//		
//		reporter.addEdit(event, result);
//		return result;
//	}
	
	
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
