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


import info.bioinfweb.commons.bio.CharacterStateType;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;



/**
 * Event that indicates that a (predefined) definition of a character state set was found in the parsed file.
 * <p>
 * This event is not used to indicate subsets of states of certain characters as allowed e.g. in the SETS block 
 * of a Nexus file, but it defines all possible states of a character (although the concrete symbols are not 
 * enumerated by this event object).
 * <p>
 * This event may or may not occur in combination with {@link SingleTokenDefinitionEvent}, which 
 * might e.g. specify the gap character to used with the standard set, depending on the parsed file. 
 * <p>
 * JPhyloIO enumerates some standard token sets in {@link TokenSetType}. Some formats might define additional
 * sets which would be represented as {@link TokenSetType#UNKNOWN}. Application developers would have to rely on 
 * {@link #getParsedName()} in such cases, to determine the meaning.
 * <p>
 * If this token set should only be valid for some columns of the alignment, a character set (defined by a
 * {@link CharacterSetIntervalEvent}) will be referenced by the return value of {@link #getCharacterSetID()} (which
 * should not be mixed up with {@link #getParsedName()} which is just the string representation of this token
 * set as it was given in the parsed file.)
 * 
 * @author Ben St&ouml;ver
 */
public class TokenSetDefinitionEvent extends ConcreteJPhyloIOEvent {
	private CharacterStateType setType;
	private String parsedName;
	private String characterSetID = null;
	
	
	/**
	 * Creates a new instance of this class.
	 * <p>
	 * If {@code null} is specified as {@code parseName}, {@link #getParsedName()} will return the empty
	 * string ("").
	 * 
	 * @param type the meaning of the token set as defined by {@link TokenSetType}
	 * @param parsedName the format specific name of the new token set
	 * @throws NullPointerException if {@code null} is specified for {@code type} 
	 */
	public TokenSetDefinitionEvent(CharacterStateType type, String parsedName) {
		this (type, parsedName, null);
	}
	
	
	/**
	 * Creates a new instance of this class.
	 * <p>
	 * If {@code null} is specified as {@code parseName}, {@link #getParsedName()} will return the empty
	 * string ("").
	 * 
	 * @param type the meaning of the token set as defined by {@link TokenSetType}
	 * @param parsedName the format specific name of the new token set
	 * @param characterSetID the ID of the character set (set of alignment columns) this token set shall be valid 
	 *        for. (Specify {@code null} here if this token set shall be valid for the whole alignment or the 
	 *        columns are unknown. The referenced character set should have been defined by a
	 *        previously fired events.)
	 * @throws NullPointerException if {@code null} is specified for {@code type} 
	 */
	public TokenSetDefinitionEvent(CharacterStateType type, String parsedName, String characterSetID) {
		super(EventContentType.TOKEN_SET_DEFINITION, EventTopologyType.START);

		if (type == null) {
			throw new NullPointerException("The set type must not be null.");
		}
		else {
			this.setType = type;
			if (parsedName == null) {
				this.parsedName = "";
			}
			else {
				this.parsedName = parsedName;
			}
			this.characterSetID = characterSetID;
		}
	}


	/**
	 * Returns the meaning of the the new character state set.
	 * 
	 * @return the meaning of the token set as defined by {@link TokenSetType}
	 */
	public CharacterStateType getSetType() {
		return setType;
	}
	
	
	/**
	 * The name of the new character state/token set as it was found in the file.
	 * 
	 * @return a format specific name of the new token set (never {@code null})
	 */
	public String getParsedName() {
		return parsedName;
	}


	/**
	 * Returns the ID of character set (which was defined in a previously fired series of 
	 * character set events) for which this token set shall be valid.
	 * 
	 * @return the name of the according character set or {@code null} if no character set was 
	 *         referenced in the parsed file (e.g. because this character state set shall be 
	 *         valid for the whole alignment)
	 */
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
	public boolean hasLinkedCharacterSet() {
		return characterSetID != null;
	}
}
