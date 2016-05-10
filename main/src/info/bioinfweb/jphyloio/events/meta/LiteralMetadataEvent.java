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
package info.bioinfweb.jphyloio.events.meta;


import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;



/**
 * Indicates that literal metadata has been encountered at the current position of the document. The actual literal value will be 
 * represented by one or more nested {@link LiteralMetadataContentEvent}s.
 * 
 * @author Ben St&ouml;ver
 * @since 0.0.0
 */
public class LiteralMetadataEvent extends LabeledIDEvent {
	//TODO This class shall replace the current MetaInformationEvent implementation as soon as the metadata concept is finally approved.

	private UriOrStringIdentifier predicate;
	private String key;  //TODO remove when Nexus and other text formats are adjusted to new predicate object
	private LiteralContentSequenceType sequenceType;
	
	
	public LiteralMetadataEvent(String id, String label, UriOrStringIdentifier predicate, String key, LiteralContentSequenceType sequenceType) {
		super(EventContentType.META_LITERAL, id, label);
		this.predicate = predicate;
		this.key = key;
		this.sequenceType = sequenceType;
	}


	public UriOrStringIdentifier getPredicate() {
		return predicate;
	}


	public String getKey() {
		return key;
	}


	/**
	 * Determines which type of sequence of {@link LiteralMetadataContentEvent}s will be nested within this
	 * event. 
	 * 
	 * @return the content sequence type
	 */
	public LiteralContentSequenceType getSequenceType() {
		return sequenceType;
	}
}
