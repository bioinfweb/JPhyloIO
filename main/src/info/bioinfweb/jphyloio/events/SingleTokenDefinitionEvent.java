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


import info.bioinfweb.commons.bio.CharacterStateMeaning;
import info.bioinfweb.commons.bio.CharacterStateType;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;



/**
 * Event that indicates the definition of a single new valid token that could be contained in a sequence 
 * of the current alignment. One or more events defining a token may occur any time between 
 * the {@link EventContentType#ALIGNMENT} and the first {@link SequenceTokensEvent}. 
 * <p>
 * Not all formats support or require a token definition, therefore sequences might contain tokens that 
 * were not previously defined by {@link SingleTokenDefinitionEvent}.   
 * 
 * @author Ben St&ouml;ver
 */
public class SingleTokenDefinitionEvent extends ConcreteJPhyloIOEvent {  //TODO Inherit from LabeledIDEvent?
	private String tokenName;
	private CharacterStateMeaning meaning;

	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param tokenName the string representation of the new token
	 * @param meaning the meaning of the new token
	 * @throws NullPointerException if {@code null} is specified for any of the arguments
	 */
	public SingleTokenDefinitionEvent(String tokenName, CharacterStateMeaning meaning) {
		super(EventContentType.SINGLE_TOKEN_DEFINITION, EventTopologyType.START);

		if (tokenName == null) {
			throw new NullPointerException("The token name must not be null.");
		}
		else if (meaning == null) {
			throw new NullPointerException("The token meaning must not be null.");
		}
		else {
			this.tokenName = tokenName;
			this.meaning = meaning;
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
	public CharacterStateMeaning getMeaning() {
		return meaning;
	}
}
