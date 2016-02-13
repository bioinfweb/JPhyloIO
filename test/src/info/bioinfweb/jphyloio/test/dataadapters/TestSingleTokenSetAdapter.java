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

import info.bioinfweb.commons.bio.CharacterStateMeaning;
import info.bioinfweb.commons.bio.CharacterStateType;
import info.bioinfweb.jphyloio.dataadapters.JPhyloIOEventReceiver;
import info.bioinfweb.jphyloio.dataadapters.ObjectListDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.implementations.EmptyAnnotatedDataAdapter;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.MetaInformationEvent;
import info.bioinfweb.jphyloio.events.SingleTokenDefinitionEvent;
import info.bioinfweb.jphyloio.events.TokenSetDefinitionEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;



public class TestSingleTokenSetAdapter extends EmptyAnnotatedDataAdapter implements ObjectListDataAdapter {
	@Override
	public long getCount() {
		return 1;
	}

	
	@Override
	public Iterator<String> getIDIterator() {
		return Arrays.asList(new String[]{"tokenSet0"}).iterator();
	}

	
	@Override
	public void writeData(JPhyloIOEventReceiver receiver, String id) throws IllegalArgumentException, IOException {
		if (id.equals("tokenSet0")) {
			receiver.add(new TokenSetDefinitionEvent(CharacterStateType.DNA, id, "Some token set"));
			
			receiver.add(new SingleTokenDefinitionEvent("-", CharacterStateMeaning.GAP));
			receiver.add(new SingleTokenDefinitionEvent("?", CharacterStateMeaning.MISSING));
			receiver.add(new SingleTokenDefinitionEvent(".", CharacterStateMeaning.MATCH));
			receiver.add(new SingleTokenDefinitionEvent("A", CharacterStateMeaning.CHARACTER_STATE));
			receiver.add(new SingleTokenDefinitionEvent("T", CharacterStateMeaning.CHARACTER_STATE));
			receiver.add(new SingleTokenDefinitionEvent("C", CharacterStateMeaning.CHARACTER_STATE));
			receiver.add(new SingleTokenDefinitionEvent("G", CharacterStateMeaning.CHARACTER_STATE));
			
			receiver.add(new MetaInformationEvent("someKey", "someType", "someValue"));
			receiver.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_INFORMATION));
			
			receiver.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.TOKEN_SET_DEFINITION));
		}
		else {
			throw new IllegalArgumentException("No token set with the ID " + id + " could be found.");
		}
	}
}
