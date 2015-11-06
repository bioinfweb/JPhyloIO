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
package info.bioinfweb.jphyloio.model.implementations;

import info.bioinfweb.jphyloio.model.CharacterData;
import info.bioinfweb.jphyloio.model.ElementCollection;



public class EmptyCharacterData implements CharacterData {
	private ElementCollection<String> sequenceNames = new EmptyElementCollection<String>();
	
	
	@Override
	public ElementCollection<String> getSequenceNames() {
		return sequenceNames;
	}


	@Override
	public ElementCollection<String> getTokens(long sequenceIndex) {
		throw new IndexOutOfBoundsException("There are no sequences in this collection.");
	}
}
