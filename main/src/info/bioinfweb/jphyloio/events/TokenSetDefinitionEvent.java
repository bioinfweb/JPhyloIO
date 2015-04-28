/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015  Ben Stöver
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
 * JPhyloIO enumerates some standard token sets in {@link SetType}. Some formats might define additional
 * sets which would be represented as {@link SetType#UNKNOWN}. Application developers would have to rely on 
 * {@link #getParsedName()} in such cases, to determine the meaning.
 * <p>
 * If this token set should only be valid for some columns of the alignment, a character set (defined by a
 * {@link CharacterSetEvent}) will be referenced by the return value of {@link #getCharacterSetName()} (which
 * should not be mixed up with {@link #getParsedName()} which is just the string representation of this token
 * set as it was given in the parsed file.)
 * 
 * @author Ben St&ouml;ver
 */
public class TokenSetDefinitionEvent extends ConcreteJPhyloIOEvent {
	/**
	 * Enumerates defined token set definitions (e.g. for DNA or protein sequences).
	 * 
	 * @author Ben St&ouml;ver
	 */
	public static enum SetType {
		/** 
		 * Indicates that the current alignment contains nucleotide characters from DNA or RNA sequences. This may also include 
		 * characters like ambiguity codes, gap characters or characters for missing data. 
		 */
		NUCLEOTIDE,
		
		/** 
		 * Indicates that the current alignment contains nucleotide characters from DNA sequences. This may also include 
		 * characters like ambiguity codes, gap characters or characters for missing data. 
		 */
		DNA,

		/** 
		 * Indicates that the current alignment contains nucleotide characters from RNA sequences. This may also include 
		 * characters like ambiguity codes, gap characters or characters for missing data. 
		 */
		RNA,
		
		/** 
		 * Indicates that the current alignment contains nucleotide characters from amino acid sequences. This may also 
		 * include characters for gaps, missing data or similar meta information. 
		 */
		AMINO_ACID,
		
		/** 
		 * Indicates that the current alignment consists of discrete character states that do not represent nucleotide or
		 * amino acid data (e.g. morphological character data). This type does not allow any conclusion on the tokens used
		 * to describe the character states, but may be followed by {@link SingleTokenDefinitionEvent}s.
		 * <p>
		 * Note that this type will only be selected by the readers if they can determine (e.g. from format conventions) that
		 * the current character state set is discrete. If that is not possible {@link #OTHER} might be selected as the type
		 * although the character states are discrete.  
		 */
		DISCRETE,

		/** 
		 * Indicates that the current alignment contains characters with continuous numeric states that could be represented
		 * e.g. by {@link Double} objects. 
		 * <p>
		 * Note that this type will only be selected by the readers if they can determine (e.g. from format conventions) that
		 * the current character state set is continuous. If that is not possible {@link #OTHER} might be selected as the type
		 * although the character states are continuous.  
		 */
		CONTINUOUS,
		
		/** 
		 * Indicates the a token set definition not enumerated by this class was found in the parsed file or it is not know
		 * whether the set is discrete or continuous. The only information about its meaning would than be given by 
		 * {@link TokenSetDefinition#getParsedName()}.
		 */
		UNKNOWN;
	}
	
	
	private SetType setType;
	private String parsedName;
	private String characterSetName = null;
	
	
	/**
	 * Creates a new instance of this class.
	 * <p>
	 * If {@code null} is specified as {@code parseName}, {@link #getParsedName()} will return the empty
	 * string ("").
	 * 
	 * @param type the meaning of the token set as defined by {@link SetType}
	 * @param parsedName the format specific name of the new token set
	 * @throws NullPointerException if {@code null} is specified for {@code type} 
	 */
	public TokenSetDefinitionEvent(SetType type, String parsedName) {
		this (type, parsedName, null);
	}
	
	
	/**
	 * Creates a new instance of this class.
	 * <p>
	 * If {@code null} is specified as {@code parseName}, {@link #getParsedName()} will return the empty
	 * string ("").
	 * 
	 * @param type the meaning of the token set as defined by {@link SetType}
	 * @param parsedName the format specific name of the new token set
	 * @param characterSetName the set of the character set (set of alignment columns) this token (character state) 
	 *        set shall be valid for (Specify {@code null} here if this character state set shall be valid for the 
	 *        whole alignment or the columns are unknown. The references character set should have been defined by a
	 *        previously fired instance if {@link CharacterSetEvent}.)
	 * @throws NullPointerException if {@code null} is specified for {@code type} 
	 */
	public TokenSetDefinitionEvent(SetType type, String parsedName, String characterSetName) {
		super(EventType.TOKEN_SET_DEFINITION);

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
			this.characterSetName = characterSetName;
		}
	}


	/**
	 * Returns the meaning of the the new character state set.
	 * 
	 * @return the meaning of the token set as defined by {@link SetType}
	 */
	public SetType getSetType() {
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
	 * Returns the name of character set (which was defined in a previously fired instance of 
	 * {@link CharacterSetEvent}) for which this character state set shall be valid.
	 * <p>
	 * Note that the name of the character state set described by this event is different from 
	 * the referenced character set name returned here. This can be obtained by calling
	 * {@link #getParsedName()}. 
	 * 
	 * @return the name of the according character set or {@code null} if no character set was 
	 *         referenced in the parsed file (e.g. because this character state set shall be 
	 *         valid for the whole alignment)
	 */
	public String getCharacterSetName() {
		return characterSetName;
	}
	
	
	/**
	 * Specifies if this token set shall only be valid for a certain character set defined by a 
	 * previous event.
	 * 
	 * @return {@code true} if this set is only valid for the character set defined by the return 
	 *         value of {@link #getCharacterSetName()} or {@code false} if no character set was 
	 *         referenced in the parsed file (e.g. because this character state set shall be 
	 *         valid for the whole alignment) 
	 */
	public boolean hasLinkedCharacterSet() {
		return characterSetName != null;
	}
}
