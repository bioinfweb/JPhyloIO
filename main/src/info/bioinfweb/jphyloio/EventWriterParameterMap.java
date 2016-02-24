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


import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;

import info.bioinfweb.commons.collections.ParameterMap;
import info.bioinfweb.commons.log.ApplicationLogger;
import info.bioinfweb.jphyloio.events.type.EventContentType;



/**
 * Parameter map that allows to specify (optional) parameters to implementations of {@link JPhyloIOEventWriter}.
 * 
 * @author Ben St&ouml;ver
 */
public class EventWriterParameterMap extends ParameterMap {
	/** 
	 * Identifies an application logger to write log messages to.
	 * <p>
	 * The value should have the type {@link ApplicationLogger}. 
	 */
	public static final String KEY_LOGGER = "logger";
	
	/** 
	 * Identifies a comment describing the application generating the output, which shall be added at the beginning
	 * of formats that support this.
	 * <p>
	 * The value should be a {@link String}.
	 */
	public static final String KEY_APPLICATION_COMMENT = "applicationComment";
	
	/** 
	 * If a line separator different from that of the current operating system shall be used by a writer, it can be 
	 * specified using this key. (Note that writers for XML formats will not necessarily make use of this parameter.)
	 * <p>
	 * The value should be a {@link String}.
	 */
	public static final String KEY_LINE_SEPARATOR = "lineSeparator";
	
	/** 
	 * Specifies the preferred line length for writers that support this.
	 * <p> 
	 * The value should have an integer type (e.g. {@link Integer}.
	 */
	public static final String KEY_LINE_LENGTH = "lineLength";
	
	/** 
	 * This parameter can be used to specify sequences with unequal lengths (in character matrix data) shall be filled 
	 * up with gap tokens, until all have an equal length.  
	 * <p>
	 * The value should have the type {@link Boolean}. If {@code true} is specified, gaps will be added to the end of 
	 * sequences, of necessary. If {@code false} is specified or this parameter is omitted, sequences with different 
	 * lengths will be written (if the according format allows that).
	 */
	public static final String KEY_EXTEND_SEQUENCE_WITH_GAPS = "extendSequenceWithGaps";
	
	/**
	 * This parameter can be used to specify whether comment events should be written to the output, at positions where 
	 * the format (or reader) supports it.
	 * <p>
	 * The value should have the type {@link Boolean}. If {@code true} is specified, comment events will be ignored by 
	 * writers supporting this parameter. If {@code false} is specified or this parameter is omitted, comments will be 
	 * written to the output at all supported positions.
	 */
	public static final String KEY_IGNORE_COMMENTS = "ignoreComments";
	
	/**
	 * This parameter can be used to specify whether a {@code TRANSLATE} command should be included in the {@code TREES}
	 * block of a Nexus document.
	 * <p>
	 * The value should have the type {@link Boolean}. If {@code true} or no value is specified, a {@code TRANSLATE} 
	 * command will be written and references to will be used as node labels in the following tree(s). If {@code false} is 
	 * specified, the full node labels will included in the tree(s).
	 */
	public static final String KEY_GENERATE_TRANSLATION_TABLE = "generateTranslationTable";
	
	/**
	 * Writers may have to edit names of OTUs, sequences or tree/network nodes according to the limitations of a specific
	 * format. In such cases, the generated names used in the output will be registered in an information object, which 
	 * will be stored in the parameter map under this key.
	 * <p>
	 * The value will have the type {@link LabelEditingReporter}. If no instance is specified, writers supporting this
	 * key will create a new one and put it in their parameter map. If a reporter is specified, its previous contents 
	 * will be cleared by the writer. If an object of another type is specified using this key, it will be replaced.
	 * 
	 * @see #getGeneratedLabelsMap()
	 */
	public static final String KEY_LABEL_EDITING_REPORTER = "labelEditingReporter";
	
	
	/**
	 * Returns the label editing reporter of this map stored under {@link #KEY_LABEL_EDITING_REPORTER}. If no object for 
	 * this key is present in this instance, a new one is created, added to this instance and returned. The same is done, 
	 * if an object which is not an instance of {@link LabelEditingReporter} is found for this key. 
	 * 
	 * @return the map instance
	 */
	public LabelEditingReporter getGeneratedLabelsMap() {
		Object result = get(KEY_LABEL_EDITING_REPORTER);
		if (!(result instanceof LabelEditingReporter)) {  // Also checks for null.
			result = new LabelEditingReporter();
			put(KEY_LABEL_EDITING_REPORTER, result);
		}
		return (LabelEditingReporter)result;
	}
}
