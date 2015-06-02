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
 * Enumerates defined token set definitions (e.g. for DNA or protein sequences).
 * 
 * @author Ben St&ouml;ver
 */
public enum TokenSetType {
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
	 * Indicates that a token set definition not enumerated by this class was found in the parsed file or it is not know
	 * whether the set is discrete or continuous. The only information about its meaning would than be given by 
	 * {@link TokenSetDefinition#getParsedName()}.
	 */
	UNKNOWN;
	
	
	/**
	 * Tests whether this token set type describes a set of nucleotide tokens.
	 * 
	 * @return {@code true} if this instance is either {@link #NUCLEOTIDE}, {@link #DNA} or {@link #RNA}, {@code false}
	 *         otherwise
	 */
	public boolean isNucleotide() {
		return this.equals(NUCLEOTIDE) || this.equals(DNA) || this.equals(RNA);
	}
	
	
	/**
	 * Tests whether this token set type describes a set of discrete tokens.
	 * 
	 * @return {@code false} if this instance is either {@link #CONTINUOUS} or {@link #UNKNOWN}, {@code false} otherwise
	 */
	public boolean isDiscrete() {
		return !this.equals(CONTINUOUS) && !this.equals(UNKNOWN);
	}
}