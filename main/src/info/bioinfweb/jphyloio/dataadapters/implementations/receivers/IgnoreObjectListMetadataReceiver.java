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
package info.bioinfweb.jphyloio.dataadapters.implementations.receivers;


import java.io.IOException;

import info.bioinfweb.commons.log.ApplicationLogger;
import info.bioinfweb.jphyloio.dataadapters.JPhyloIOEventReceiver;
import info.bioinfweb.jphyloio.dataadapters.ObjectListDataAdapter;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;



/**
 * Event receiver that can be passed to {@link ObjectListDataAdapter#writeContentData(JPhyloIOEventReceiver, String)}.
 * It checks whether any metadata is written and writes an according warning, that this data is not supported.
 * 
 * @author Ben St&ouml;ver
 */
public class IgnoreObjectListMetadataReceiver implements JPhyloIOEventReceiver {
	private boolean ignoredMetadata = false;
	private ApplicationLogger logger;
	private String objectName;
	private String formatName;
	
	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param logger the application logger to write a warning to, if metadata is ignored (Maybe {@code null} if
	 *        no warning shall be logged.)
	 * @param objectName the name of the objects in this list to be used in the warning message (Must be specified
	 *        if {@code logger} is not {@code null}.)
	 * @param formatName the name of the format currently written (e.g. "FASTA") to be used in the warning message 
	 *        (Must be specified if {@code logger} is not {@code null}.)
	 * @throws NullPointerException if {@code objectName} or {@code formatName} are {@code null} although 
	 *         a logger instance was specified}
	 */
	public IgnoreObjectListMetadataReceiver(ApplicationLogger logger,	String objectName, String formatName) {
		super();
		this.logger = logger;
		
		if (logger != null) {
			if (objectName == null) {
				throw new NullPointerException("objectName must not be null if an application logger was specified.");
			}
			else {
				this.objectName = objectName;
			}
			
			if (formatName == null) {
				throw new NullPointerException("formatName must not be null if an application logger was specified.");
			}
			else {
				this.formatName = formatName;
			}
		}
	}


	/**
	 * Indicates whether any metadata was ignored.
	 * 
	 * @return {@code true} if at least one element with the content type {@link EventContentType#META_INFORMATION}
	 *         was specified to {@link #add(JPhyloIOEvent)}, since the construction of this instance or {@code false}
	 *         otherwise
	 */
	public boolean isIgnoredMetadata() {
		return ignoredMetadata;
	}
	
	
	/**
	 * Sets the {@code ignoredMetadata} property back to {@code false}.
	 */
	public void reset() {
		ignoredMetadata = false;
	}


	@Override
	public boolean add(JPhyloIOEvent event) throws IllegalArgumentException, ClassCastException, IOException {
		if (event.getType().getContentType().equals(EventContentType.META_INFORMATION)) {
			if (!ignoredMetadata && (logger != null)) {
				logger.addWarning("Metadata attached to " + objectName + " was ignored, since the " + formatName + 
						" format does not support this.");
			}
			ignoredMetadata = true;
		}
		return !ignoredMetadata;
	}
}
