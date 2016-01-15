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
import info.bioinfweb.jphyloio.events.type.EventTopologyType;



/**
 * Instances of this or inherited classes model data that contain a labeling text and an OTU ID.
 * Note that in contrast to most other event classes, this class is used to model different types
 * of events (having a different content type).
 * <p>
 * The OTU ID returned by {@link #getOTUID()} can either be the ID of this object, if it is used 
 * with the type {@link EventContentType#OTU} and defined a new OTU or a link to an OTU defined 
 * in a previous event. Such links are carried by start events of the types 
 * {@link EventContentType#SEQUENCE} or {@link EventContentType#NODE}.  
 * 
 * @author Ben St&ouml;ver
 */
public class BasicOTUEvent extends ConcreteJPhyloIOEvent {
	private String label;
	private String otuID;

	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param contentType the content type of the modeled data element (e.g. 
	 *        {@link EventContentType#OTU} or {@link EventContentType#SEQUENCE})
	 * @param label the label of the modeled data element (Maybe {@code null}, if no label is present.)
	 * @param otuID the declared or linked OTU ID (Maybe {@code null}, if none is present.)
	 * @throws NullPointerException if {@code contentType} is {@code null}
	 */
	public BasicOTUEvent(EventContentType contentType, String label, String otuID) {
		super(contentType, EventTopologyType.START);
		this.label = label;
		this.otuID = otuID;
	}


	/**
	 * Returns the label attached to the object indicates by this event.
	 * <p>
	 * Note that additional annotations of that object may be specified by up-coming metaevents, even if 
	 * this event carries no label.
	 * 
	 * @return the label string or {@code null} if this event carries no label
	 */
	public String getLabel() {
		return label;
	}


	/**
	 * Returns the OTU ID stored in this event. Depending of the content type this may be the ID
	 * of the modeled object or the ID of a previously defined OTU object that is linked to
	 * object modeled here.
	 * 
	 * @return the declared or linked OTU ID or {@code null} if this object does not declare or 
	 *         link an OTU ID
	 */
	public String getOTUID() {
		return otuID;
	}
	
	
	/**
	 * Indicates whether this event declares or links an OTU ID.
	 * 
	 * @return {@code true} if an ID is present, {@code false} otherwise
	 */
	public boolean isOTULinked() {
		return getOTUID() != null;
	}
}
