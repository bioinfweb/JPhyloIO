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
 * Event that indicates the definition of a single new valid token that could be contained in a sequence 
 * of the current alignment. One or more events defining a token may occur any time between 
 * the {@link EventType#ALIGNMENT_START} and the first {@link SequenceTokensEvent}. 
 * <p>
 * Not all formats support or require a token definition, therefore sequences might contain tokens that 
 * were not previously defined by {@link SingleTokenDefinitionEvent}.   
 * 
 * @author Ben St&ouml;ver
 */
public class SingleTokenDefinitionEvent extends ConcreteJPhyloIOEvent {
	/**
	 * Enumerates defined meanings of single token definitions.
	 * 
	 * @author Ben St&ouml;ver
	 */
	public static enum Meaning {
		/** Indicates that the string representation of a token is the gap character or token (e.g. '-'). */
		GAP,
		
		/** Indicates that the string representation of a token is the missing data character or token (e.g. '?'). */
		MISSING,
		
		/** Indicates that the string representation of a token is the match character or token (e.g. '.'). */
		MATCH,

		/** 
		 * Indicates that a token is a representation of a character state (e.g. a nucleotide, an amino acid, 
		 * an ambiguity code, ...). 
		 */
		CHARACTER_STATE,
		
		/** Indicates that a token has some other meaning which is not enumerated by this class. */
		OTHER;
	}
	
	
	private String tokenName;
	private Meaning meaning;
	private TokenSetDefinitionEvent.SetType tokenSetType;
	private String tokenSetName;

	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param tokenName the string representation of the new token
	 * @param meaning the meaning of the new token
	 * @throws NullPointerException if {@code null} is specified for any of the arguments
	 */
	public SingleTokenDefinitionEvent(String tokenName, Meaning meaning) {
		this(tokenName, meaning, null, null);
	}
	
	
	public SingleTokenDefinitionEvent(String tokenName, Meaning meaning, TokenSetDefinitionEvent.SetType tokenSetType,
			String tokenSetName) {
		
		super(EventType.SINGLE_TOKEN_DEFINITION);

		if (tokenName == null) {
			throw new NullPointerException("The token name must not be null.");
		}
		else if (meaning == null) {
			throw new NullPointerException("The token meaning must not be null.");
		}
		else {
			this.tokenName = tokenName;
			this.meaning = meaning;
			
			this.tokenSetType = tokenSetType;
			if ((tokenSetName == null) && (tokenSetType != null)) {
				this.tokenSetName = tokenSetType.toString();
			}
			else {
				this.tokenSetName = tokenSetName;
			}
		}
	}


	/**
	 * Returns the string representation of the new token.
	 * 
	 * @return the string representation of one or more characters in length 
	 */
	public String getTokenName() {
		return tokenName;
	}


	/**
	 * Returns the meaning of the new token.
	 * 
	 * @return the meaning
	 */
	public Meaning getMeaning() {
		return meaning;
	}


	/**
	 * Returns the type of the character state set this token belongs to.
	 * 
	 * @return the token set type or {@code null} if set was specified in the parsed file for this token
	 *         (This usually means that this token is valid for all token sets or there is only one token
	 *         set in the file.)
	 */
	public TokenSetDefinitionEvent.SetType getTokenSetType() {
		return tokenSetType;
	}


	/**
	 * Returns the string representation of the name of the token set, this token belongs to.
	 * <p>
	 * If no such name was read from the file, but a token set type could anyway be determined, the string 
	 * representation of {@link #getTokenSetType()} will be returned here.
	 * 
	 * @return the name or {@code null} if no name is available
	 * @see #getTokenSetType()
	 */
	public String getTokenSetName() {
		return tokenSetName;
	}
}