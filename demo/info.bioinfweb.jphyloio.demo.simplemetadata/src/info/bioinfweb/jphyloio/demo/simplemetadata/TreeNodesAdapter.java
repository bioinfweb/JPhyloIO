/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015-2019  Ben Stöver, Sarah Wiechers
 * <http://bioinfweb.info/JPhyloIO>
 * 
 * This file is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This file is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package info.bioinfweb.jphyloio.demo.simplemetadata;


import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.dataadapters.JPhyloIOEventReceiver;
import info.bioinfweb.jphyloio.dataadapters.ObjectListDataAdapter;
import info.bioinfweb.jphyloio.events.NodeEvent;



public class TreeNodesAdapter implements ObjectListDataAdapter<NodeEvent> {
	@Override
	public long getCount(ReadWriteParameterMap parameters) {
		return 1;
	}

	
	@Override
	public Iterator<String> getIDIterator(ReadWriteParameterMap parameters) {
		return Arrays.asList(new String[]{"node1"}).iterator();
	}

	
	@Override
	public NodeEvent getObjectStartEvent(ReadWriteParameterMap parameters, String id) throws IllegalArgumentException {
		return new NodeEvent("node1", "someNode", null, true);  // Will only be called with the one ID specified in the iterator above.
	}


	@Override
	public void writeContentData(ReadWriteParameterMap parameters, JPhyloIOEventReceiver receiver, String id) throws IOException, IllegalArgumentException {}
}
