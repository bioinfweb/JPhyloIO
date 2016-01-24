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
package info.bioinfweb.jphyloio.events;


import info.bioinfweb.commons.text.StringUtils;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;



/**
 * Event that indicate data objects that carry an unique ID and a label.
 * 
 * @author Ben St&ouml;ver
 */
public class LabeledIDEvent extends ConcreteJPhyloIOEvent {
	private String id;
	private String label;
	
	
	/**
	 * Creates a new instance of this class.
	 * <p>
	 * Instances are always start events.
	 * 
	 * @param contentType the content type of the event
	 * @param id the unique ID associated with the represented data element (Must not contain any whitespace.)
	 * @param label a label associated with the represented data element (Maybe {@code null}.)
	 * @throws NullPointerException if {@code contentType}, {@code topologyType} or {@code id} are {@code null}
	 * @throws IllegalArgumentException if the specified ID is an empty string or contains whitespace
	 */
	public LabeledIDEvent(EventContentType contentType,	String id, String label) {
		super(contentType, EventTopologyType.START);
		
		checkID(id, "ID");
		this.id = id;
		this.label = label;
	}
	
	
	protected void checkID(String id, String idName) {
		if (id == null) {
			throw new NullPointerException("The " + idName + " of this event must not be null.");
		}
		else if ("".equals(id)) {
			throw new IllegalArgumentException("The " + idName + " of this event must not be an empty string.");
		}
		else if (StringUtils.containsWhitespace(id)) {
			throw new IllegalArgumentException("The " + idName + " of this event must not contain any whitespace. (\"" + id + "\")");
		}
	}


	/**
	 * Returns the document-wide unique ID of the data element represented by this event. 
	 * 
	 * @return a string ID without whitespace and never {@code null}
	 */
	public String getID() {
		return id;
	}


	/**
	 * A text label associated with the data element represented by this event.
	 * <p>
	 * Note that additional annotations of that object may be specified by nested metaevents, even if 
	 * this event carries no label.
	 * 
	 * @return the labeling text or {@code null}, if no label was specified for the modeled object
	 */
	public String getLabel() {
		return label;
	}
	
	
	/**
	 * Indicates whether the modeled data element carries a label.
	 * 
	 * @return {@code true} if the data element carries a label, {@code false} otherwise.
	 */
	public boolean hasLabel() {
		return getLabel() != null;
	}
}
