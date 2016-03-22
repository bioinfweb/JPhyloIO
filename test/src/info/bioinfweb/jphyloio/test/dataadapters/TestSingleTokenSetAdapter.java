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
package info.bioinfweb.jphyloio.test.dataadapters;


import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import info.bioinfweb.commons.IntegerIDManager;
import info.bioinfweb.commons.bio.CharacterSymbolMeaning;
import info.bioinfweb.commons.bio.CharacterStateSetType;
import info.bioinfweb.commons.bio.CharacterSymbolType;
import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.dataadapters.JPhyloIOEventReceiver;
import info.bioinfweb.jphyloio.dataadapters.ObjectListDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.implementations.EmptyAnnotatedDataAdapter;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.MetaInformationEvent;
import info.bioinfweb.jphyloio.events.SingleTokenDefinitionEvent;
import info.bioinfweb.jphyloio.events.TokenSetDefinitionEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;



public class TestSingleTokenSetAdapter extends EmptyAnnotatedDataAdapter implements ObjectListDataAdapter, ReadWriteConstants {	
	@Override
	public long getCount() {
		return 1;
	}


	@Override
	public Iterator<String> getIDIterator() {
		return Arrays.asList(new String[]{"tokenSet0"}).iterator();
	}

	
	@Override
	public LabeledIDEvent getObjectStartEvent(String id) throws IllegalArgumentException {
		if (id.equals("tokenSet0")) {
			return new TokenSetDefinitionEvent(CharacterStateSetType.DNA, "tokenSet0", "Some token set");
		}
		else {
			throw new IllegalArgumentException("No token set with the ID " + id + " could be found.");
		}
	}


	@Override
	public void writeContentData(JPhyloIOEventReceiver receiver, String id) throws IOException {
		if (id.equals("tokenSet0")) {			
			IntegerIDManager idManager = new IntegerIDManager();
			receiver.add(new SingleTokenDefinitionEvent(DEFAULT_TOKEN_DEFINITION_ID_PREFIX + idManager.createNewID(), null, "-", 
					CharacterSymbolMeaning.GAP, CharacterSymbolType.ATOMIC_STATE));
			receiver.add(new SingleTokenDefinitionEvent(DEFAULT_TOKEN_DEFINITION_ID_PREFIX + idManager.createNewID(), null, "?", 
					CharacterSymbolMeaning.MISSING, CharacterSymbolType.ATOMIC_STATE));
			receiver.add(new SingleTokenDefinitionEvent(DEFAULT_TOKEN_DEFINITION_ID_PREFIX + idManager.createNewID(), null, ".", 
					CharacterSymbolMeaning.MATCH, CharacterSymbolType.ATOMIC_STATE));
			receiver.add(new SingleTokenDefinitionEvent(DEFAULT_TOKEN_DEFINITION_ID_PREFIX + idManager.createNewID(), null, "A", 
					CharacterSymbolMeaning.CHARACTER_STATE, CharacterSymbolType.ATOMIC_STATE));
			receiver.add(new SingleTokenDefinitionEvent(DEFAULT_TOKEN_DEFINITION_ID_PREFIX + idManager.createNewID(), null, "T", 
					CharacterSymbolMeaning.CHARACTER_STATE, CharacterSymbolType.ATOMIC_STATE));
			receiver.add(new SingleTokenDefinitionEvent(DEFAULT_TOKEN_DEFINITION_ID_PREFIX + idManager.createNewID(), null, "C", 
					CharacterSymbolMeaning.CHARACTER_STATE, CharacterSymbolType.ATOMIC_STATE));
			receiver.add(new SingleTokenDefinitionEvent(DEFAULT_TOKEN_DEFINITION_ID_PREFIX + idManager.createNewID(), null, "G", 
					CharacterSymbolMeaning.CHARACTER_STATE, CharacterSymbolType.ATOMIC_STATE));
			
			receiver.add(new MetaInformationEvent("someKey", "someType", "someValue"));
			receiver.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_INFORMATION));
			
			receiver.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.TOKEN_SET_DEFINITION));
		}
		else {
			throw new IllegalArgumentException("No token set with the ID " + id + " could be found.");
		}
	}
}
