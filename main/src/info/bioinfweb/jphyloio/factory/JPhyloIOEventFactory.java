/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015-2016  Ben St�ver, Sarah Wiechers
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
package info.bioinfweb.jphyloio.factory;


import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.io.XMLUtils;

import info.bioinfweb.commons.LongIDManager;
import info.bioinfweb.jphyloio.events.EdgeEvent;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.LinkedLabeledIDEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;



/**
 * A factory for <i>JPhyloIO</i> events that manages the creation of unique IDs for each event. A new instance of this factory
 * should be used for each document that is written, because one instance does not allow to use any event ID twice.
 * 
 * @author Ben St&ouml;ver
 * @since 0.0.0
 */
public class JPhyloIOEventFactory {
	//TODO Are the event specific constructor methods necessary or is createProposedID() already sufficient?
	//TODO Should an exception be thrown on used IDs or shall they automatically be replaced as in createProposedID()? (This could also be determined by a parameter.)
	
	private String idPrefix;
	private boolean replaceUsedIDs = false;
	private LongIDManager idManager = new LongIDManager();
	private Set<String> usedIDs = new HashSet<String>();
	
	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param idPrefix the prefix to be used for automatically generated IDs.
	 */
	public JPhyloIOEventFactory(String idPrefix) {
		super();
		if (idPrefix == null) {
			idPrefix = "";
		}
		if ("".equals(idPrefix) || XMLUtils.isNCName(idPrefix)) {
			this.idPrefix = idPrefix;
		}
		else {
			throw new IllegalArgumentException("idPrefix (\"" + idPrefix + "\") is not a valid NCName.");
		}
	}
	
	
	public String getIDPrefix() {
		return idPrefix;
	}


	/**
	 * Determines whether ID passed to {@code create*Event()} methods that are already used shall be replaced by new IDs or not.
	 * If this property is set to {@code false}, {@code create*Event()} methods will throw an {@link IllegalArgumentException} if
	 * used IDs are specified.
	 * 
	 * @return {@code true} if used IDs shall automatically be replaced, or {@code false} if exceptions shall be thrown on used IDs
	 */
	public boolean isReplaceUsedIDs() {
		return replaceUsedIDs;
	}


	/**
	 * Allows to specify whether ID passed to {@code create*Event()} methods that are already used shall be replaced by new IDs or not.
	 * If this property is set to {@code false}, {@code create*Event()} methods will throw an {@link IllegalArgumentException} if
	 * used IDs are specified.
	 * 
	 * @param replaceUsedIDs Specify {@code true} here, if used IDs shall automatically be replaced, or {@code false} if exceptions 
	 *        shall be thrown on used IDs
	 */
	public void setReplaceUsedIDs(boolean replaceUsedIDs) {
		this.replaceUsedIDs = replaceUsedIDs;
	}


	private String createNewID() {
		String result;
		do {
			result = idPrefix + idManager.createNewID();
		} while (!usedIDs.contains(result));
		return result;
	}
	
	
	private String checkID(String id, boolean replace) {
		if (id == null) {
			return createNewID();
		}
		else if (usedIDs.contains(id)) {
			if (replace) {
				return createNewID();
			}
			else {
				throw new IllegalArgumentException("The ID \"" + id + "\" is already used in the current document.");
			}
		}
		else {
			return id;
		}
	}
	
	
	/**
	 * Checks if the proposed ID is already used in the current document and generated a new one, if necessary.
	 * <p>
	 * The returned ID will be added to the set of used IDs.
	 * 
	 * @param proposedID the ID that shall be used if possible
	 * @return {@code proposedID} or a newly generated one, if {@code proposedID} was already used
	 */
	public String createProposedID(String proposedID) {
		return checkID(proposedID, true);
	}
	
	
	/**
	 * Creates a new {@link LabeledIDEvent} with a valid ID.
	 * <p>
	 * This method will generate a new ID if {@code id} is already used, if {@link #isReplaceUsedIDs()} is set to {@code true}.
	 * Otherwise an {@link IllegalArgumentException} exception will be thrown if a used ID is specified.
	 * 
	 * @param contentType the content type of the event
	 * @param id the unique ID associated with the represented data element (Must be a valid
	 *        <a href="https://www.w3.org/TR/1999/REC-xml-names-19990114/#NT-NCName">NCName</a>.)
	 * @param label a label associated with the represented data element (Maybe {@code null}.)
	 * @throws NullPointerException if {@code contentType}, {@code topologyType} or {@code id} are {@code null}
	 * @throws IllegalArgumentException if the specified ID is already used and {@link #isReplaceUsedIDs()} is set to {@code true} 
	 */
	public LabeledIDEvent createLabeledIDEvent(EventContentType contentType, String id, String label) {
		id = checkID(id, replaceUsedIDs);
		LabeledIDEvent result = new LabeledIDEvent(contentType, id, label);
		usedIDs.add(id);  // ID must be added after the creation of the event, since the constructor may throw an exception, if the ID is invalid.
		return result;
	}
	
	
	/**
	 * Creates a new {@link LinkedLabeledIDEvent} with a valid ID.
	 * <p>
	 * This method will generate a new ID if {@code id} is already used, if {@link #isReplaceUsedIDs()} is set to {@code true}.
	 * Otherwise an {@link IllegalArgumentException} exception will be thrown if a used ID is specified.
	 * 
	 * @param contentType the content type of the modeled data element (e.g. 
	 *        {@link EventContentType#OTU} or {@link EventContentType#SEQUENCE})
	 * @param label the label of the modeled data element (Maybe {@code null}, if no label is present.)
	 * @param linkedID the ID of a linked element (Maybe {@code null}, if none is present.)
	 * @throws NullPointerException if {@code contentType} is {@code null}
	 * @throws IllegalArgumentException if {@code id} is already used and {@link #isReplaceUsedIDs()} is set to {@code true} or 
	 *         {@code otuID} is not a valid <a href="https://www.w3.org/TR/1999/REC-xml-names-19990114/#NT-NCName">NCNames</a>
	 */
	public LinkedLabeledIDEvent createLinkedLabeledIDEvent(EventContentType contentType, String id, String label, String linkedID) {
		id = checkID(id, replaceUsedIDs);
		LinkedLabeledIDEvent result = new LinkedLabeledIDEvent(contentType, id, label, linkedID);
		usedIDs.add(id);  // ID must be added after the creation of the event, since the constructor may throw an exception, if the ID is invalid.
		return result;
	}
	
	
	/**
	 * Creates a new {@link EdgeEvent} with a valid ID.
	 * <p>
	 * This method will generate a new ID if {@code id} is already used, if {@link #isReplaceUsedIDs()} is set to {@code true}.
	 * Otherwise an {@link IllegalArgumentException} exception will be thrown if a used ID is specified.
	 * 
	 * @param id the unique ID associated with the represented edge (Must be a valid
	 *        <a href="https://www.w3.org/TR/1999/REC-xml-names-19990114/#NT-NCName">NCName</a>.)
	 * @param label an optional label associated with this edge (Maybe {@code null}.)
	 * @param sourceID the ID of the source node of this edge (Maybe {@code null} if this a root edge.) 
	 * @param targetID the ID of the target node of this edge
	 * @param length an optional lengths of this edge (Maybe {@link Double#NaN} if no length is given.)
	 * @throws NullPointerException if {@code id} or {@code targetID} are {@code null}
	 * @throws IllegalArgumentException if {@code id} is already used and {@link #isReplaceUsedIDs()} is set to {@code true} or 
	 *         {@code sourceID} or {@code targetID} are not a valid 
	 *         <a href="https://www.w3.org/TR/1999/REC-xml-names-19990114/#NT-NCName">NCNames</a>
	 */
	public EdgeEvent createEdgeEvent(String id, String label,	String sourceID, String targetID, double length) {
		id = checkID(id, replaceUsedIDs);
		EdgeEvent result = new EdgeEvent(id, label, sourceID, targetID, length);
		usedIDs.add(id);  // ID must be added after the creation of the event, since the constructor may throw an exception, if the ID is invalid.
		return result;
	}
}
