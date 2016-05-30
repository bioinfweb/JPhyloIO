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


import info.bioinfweb.commons.bio.CharacterStateSetType;
import info.bioinfweb.jphyloio.events.type.EventContentType;



/**
 * Event that indicates that a (predefined) definition of a character state set was found in the parsed file.
 * <p>
 * This event is not used to indicate subsets of states of certain characters as allowed e.g. in the SETS block 
 * of a Nexus file, but it defines all possible states of a character (although the concrete symbols are not 
 * enumerated by this event object).
 * <p>
 * This event may or may not occur in combination with {@link SingleTokenDefinitionEvent}, which 
 * might e.g. specify the gap character to be used with the standard set, depending on the parsed file. 
 * <p>
 * JPhyloIO enumerates some standard token sets in {@link TokenSetType}. Some formats might define additional
 * sets which would be represented as {@link TokenSetType#UNKNOWN}. Application developers would have to rely on 
 * {@link #getParsedName()} in such cases, to determine the meaning.
 * <p>
 * This event will always be followed by one or more events of the type {@link CharacterSetIntervalEvent} 
 * that specify the columns of the alignment in which this token set is valid.
 * 
 * @author Ben St&ouml;ver
 */
public class TokenSetDefinitionEvent extends LabeledIDEvent {
	private CharacterStateSetType setType;
	private String characterSetID = null; //TODO remove when readers and writers have been adjusted
	
	
	/**
	 * Creates a new instance of this class.
	 * <p>
	 * If {@code null} is specified as {@code parseName}, {@link #getParsedName()} will return the empty
	 * string ("").
	 * 
	 * @param type the meaning of the token set as defined by {@link TokenSetType}
	 * @param id the document-wide unique ID associated with the represented token set (Must be a valid
	 *        <a href="https://www.w3.org/TR/1999/REC-xml-names-19990114/#NT-NCName">NCName</a>.)
	 * @param label a name describing this token set
	 * @throws NullPointerException if {@code null} is specified for {@code type} 
	 */
	public TokenSetDefinitionEvent(CharacterStateSetType type, String id, String label) {
		super(EventContentType.TOKEN_SET_DEFINITION, id, label);

		if (type == null) {
			throw new NullPointerException("The set type must not be null.");
		}
		else {
			this.setType = type;
		}
	}

	
	/**
	 * Creates a new instance of this class.
	 * <p>
	 * If {@code null} is specified as {@code parseName}, {@link #getParsedName()} will return the empty
	 * string ("").
	 * 
	 * @param type the meaning of the token set as defined by {@link TokenSetType}
	 * @param id a document-wide unique ID identifying this token set
	 * @param label a name describing this token set
	 * @param linkedCharacterSetID the ID of the character set (set of alignment columns) this token set shall be valid 
	 *        for. (Specify {@code null} here if this token set shall be valid for the whole alignment or the 
	 *        columns are unknown. The referenced character set should have been defined by a
	 *        previously fired events.)
	 * @throws NullPointerException if {@code null} is specified for {@code type} 
	 */
	@Deprecated
	public TokenSetDefinitionEvent(CharacterStateSetType type, String id, String label, String linkedCharacterSetID) {
		super(EventContentType.TOKEN_SET_DEFINITION, id, label);

		if (type == null) {
			throw new NullPointerException("The set type must not be null.");
		}
		else {
			this.setType = type;
			this.characterSetID = linkedCharacterSetID;
		}
	}

	

	/**
	 * Returns the meaning of the the new character state set.
	 * 
	 * @return the meaning of the token set as defined by {@link TokenSetType}
	 */
	public CharacterStateSetType getSetType() {
		return setType;
	}
	
	
	/**
	 * Returns the ID of character set (which was defined in a previously fired series of 
	 * character set events) for which this token set shall be valid.
	 * 
	 * @return the name of the according character set or {@code null} if no character set was 
	 *         referenced in the parsed file (e.g. because this character state set shall be 
	 *         valid for the whole alignment)
	 */
	@Deprecated
	public String getCharacterSetID() {
		return characterSetID;
	}
	
	
	/**
	 * Specifies if this token set shall only be valid for a certain character set defined by a 
	 * previous event.
	 * 
	 * @return {@code true} if this set is only valid for the character set defined by the return 
	 *         value of {@link #getCharacterSetID()} or {@code false} if no character set was 
	 *         referenced in the parsed file (e.g. because this character state set shall be 
	 *         valid for the whole alignment) 
	 */
	@Deprecated
	public boolean hasLinkedCharacterSet() {
		return characterSetID != null;
	}
}
