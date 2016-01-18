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


import info.bioinfweb.jphyloio.events.type.EventContentType;



/**
 * Instances of this or inherited classes model data elements with an ID and an optional label 
 * that link an OTU ID.
 * 
 * @author Ben St&ouml;ver
 */
public class LinkedOTUEvent extends LabeledIDEvent {
	private String linkedOTUID;

	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param contentType the content type of the modeled data element (e.g. 
	 *        {@link EventContentType#OTU} or {@link EventContentType#SEQUENCE})
	 * @param label the label of the modeled data element (Maybe {@code null}, if no label is present.)
	 * @param otuID the declared or linked OTU ID (Maybe {@code null}, if none is present.)
	 * @throws NullPointerException if {@code contentType} is {@code null}
	 */
	public LinkedOTUEvent(EventContentType contentType, String id, String label, String otuID) {
		super(contentType, id, label);
		this.linkedOTUID = otuID;
	}


	/**
	 * Returns the OTU ID linked to the data element, which is modeled by this event.
	 * 
	 * @return the linked OTU ID or {@code null} if this object does not have an associated OTU
	 */
	public String getOTUID() {
		return linkedOTUID;
	}
	
	
	/**
	 * Indicates whether this event links an OTU ID.
	 * 
	 * @return {@code true} if an ID is present, {@code false} otherwise
	 */
	public boolean isOTULinked() {
		return getOTUID() != null;
	}
}
