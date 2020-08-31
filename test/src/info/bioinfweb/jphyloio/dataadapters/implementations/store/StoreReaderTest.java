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
package info.bioinfweb.jphyloio.dataadapters.implementations.store;


import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;

import org.junit.Test;

import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.LinkedLabeledIDEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.formats.newick.NewickEventReader;
import info.bioinfweb.jphyloio.formats.newick.NewickEventWriter;



public class StoreReaderTest {
	@Test
	public void test_readTreeNetwork() throws IOException {
		NewickEventReader reader = new NewickEventReader(new File("data/Newick/Metadata.nwk"),
				new ReadWriteParameterMap());
		try {
			while (!reader.peek().getType().equals(EventContentType.TREE, EventTopologyType.START)) {
				reader.next();
			}
			StoreTreeNetworkDataAdapter tree = StoreReader.readTreeNetwork(reader);

			StoreDocumentDataAdapter document = new StoreDocumentDataAdapter();
			StoreTreeNetworkGroupDataAdapter treeGroup = new StoreTreeNetworkGroupDataAdapter(new LinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, "treeGroup", null, null), 
					Collections.<JPhyloIOEvent>emptyList());
			treeGroup.getTreesAndNetworks().add(tree);
			document.getTreesNetworks().add(treeGroup);
			
			StringWriter writer = new StringWriter();
			new NewickEventWriter().writeDocument(document, writer, new ReadWriteParameterMap());
			
			assertEquals("[&U] (A[&prob=1.000000000000000e+000, prob_stddev=0, prob_range='{1.000000000000000e+000,1}', prob(percent)='100', prob+-sd='100+-0']:0.3682008685714568[&length_mean=3.744759260623280e-001, length_median=3.682008685714568e-001, length_95%HPD='{2.494893056441154e-001, \"A, =B\"}'], (B[&prob='A, =B', prob_stddev=0.000000000000000e+000, prob_range='{\"AB \"\"C\", ABC}', prob(percent)='100', prob+-sd='100+-0']:0.6244293083853111[&length_mean=6.345415111023917e-001, length_median=6.244293083853111e-001, length_95%HPD='{4.360295861156825e-001,8.441623753050405e-001}'], C[&prob=6.364056912805381e-001, prob_stddev=7.249475639180907e-004, prob_range='{6.358930759420870e-001,6.369183066189893e-001}', prob(percent)='64', prob+-sd='64+-0']:0.07039004236028111[&length_mean=7.419012044002400e-002, length_median=7.039004236028111e-002, length_95%HPD='{9.114712766459516e-003,1.418351647155842e-001}'])n37[&prob=1.000000000000000e+000, prob_stddev=0.000000000000000e+000, prob_range='{1.000000000000000e+000,1.000000000000000e+000}', prob(percent)='100', prob+-sd='100+-0'][&length_mean=0.000000000000000e+000, length_median=0.000000000000000e+000, length_95%HPD='{}'][comment 1])n40[18]:0.0[20];", 
					writer.getBuffer().toString().trim());
		}
		finally {
			reader.close();
		}
		
	}
}
