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
 * Instances of this or inherited classes model data elements with an ID and an optional label, 
 * that link a single OTU or an OTU list by their ID.
 * <p>
 * If a single OTU or an whole OTU list is linked, depends on the modeled object (the content type of this 
 * instance). Matrices, trees or networks links OTU lists, while sequences or tree/network nodes link single
 * OTUs.
 * 
 * @author Ben St&ouml;ver
 */
public class LinkedOTUOrOTUsEvent extends LabeledIDEvent {
	private String linkedOTUOrOTUsID;

	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param contentType the content type of the modeled data element (e.g. 
	 *        {@link EventContentType#OTU} or {@link EventContentType#SEQUENCE})
	 * @param id the unique ID associated with the represented data element (Must be a valid
	 *        <a href="https://www.w3.org/TR/1999/REC-xml-names-19990114/#NT-NCName">NCName</a>.)
	 * @param label the label of the modeled data element (Maybe {@code null}, if no label is present.)
	 * @param otuID the declared or linked OTU ID (Maybe {@code null}, if none is present.)
	 * @throws NullPointerException if {@code contentType} or {@code id} are {@code null}
	 * @throws IllegalArgumentException if {@code id} or {@code otuID} are not valid 
	 *         <a href="https://www.w3.org/TR/1999/REC-xml-names-19990114/#NT-NCName">NCNames</a>
	 */
	public LinkedOTUOrOTUsEvent(EventContentType contentType, String id, String label, String otuID) {
		super(contentType, id, label);
		
		if (otuID != null) {
			checkID(otuID, "linked OTU ID");
		}
		this.linkedOTUOrOTUsID = otuID;
	}


	/**
	 * Returns the ID of a single OTU or an OTU list linked to the data element, which is modeled by this event.
	 * <p>
	 * If a single OTU or an whole OTU list is linked, depends on the modeled object (the content type of this 
	 * instance). Matrices, trees or networks links OTU lists, while sequences or tree/network nodes link single
	 * OTUs.
	 * 
	 * @return the linked OTU or OTU list ID or {@code null} if this object does not have an associated OTU or
	 *         OTU list
	 */
	public String getOTUOrOTUsID() {
		return linkedOTUOrOTUsID;
	}
	
	
	/**
	 * Indicates whether this event links an OTU or OTU list ID.
	 * 
	 * @return {@code true} if an ID is present, {@code false} otherwise
	 */
	public boolean isOTUOrOTUsLinked() {
		return getOTUOrOTUsID() != null;
	}
}
