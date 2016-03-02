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
package info.bioinfweb.jphyloio.formats.nexus;


import java.util.Map;

import info.bioinfweb.jphyloio.AbstractEventWriter;
import info.bioinfweb.jphyloio.InconsistentAdapterDataException;
import info.bioinfweb.jphyloio.LabelEditingReporter;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.dataadapters.OTUListDataAdapter;
import info.bioinfweb.jphyloio.events.LinkedOTUOrOTUsEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.formats.newick.DefaultNewickWriterNodeLabelProcessor;



public class NexusNewickWriterNodeLabelProcessor extends DefaultNewickWriterNodeLabelProcessor {
	private Map<String, Long> indexMap;
	
	
	public NexusNewickWriterNodeLabelProcessor(OTUListDataAdapter otuList, Map<String, Long> indexMap, 
			ReadWriteParameterMap parameters) {
		
		super(otuList, parameters);
		this.indexMap = indexMap;
	}


	@Override
	public String createNodeName(LinkedOTUOrOTUsEvent nodeEvent) {
		LabelEditingReporter reporter = getParameters().getLabelEditingReporter();
		String result;
		if ((indexMap != null) && nodeEvent.isOTUOrOTUsLinked()) {
			Long index = indexMap.get(nodeEvent.getOTUOrOTUsID());
			if (index == null) {
				throw new InconsistentAdapterDataException("Error when writing tree: The node with the ID " + nodeEvent.getID() + 
						" references an OTU with the ID " + nodeEvent.getOTUOrOTUsID() + 
						", which could not be found in the OTU list associated with this tree.");
			}
			else {
				result = index.toString();
				reporter.addEdit(nodeEvent, reporter.getEditedLabel(EventContentType.OTU, nodeEvent.getOTUOrOTUsID()));
			}
		}
		else {
			result = AbstractEventWriter.getLinkedOTUNameOTUFirst(nodeEvent, getOTUList());
			reporter.addEdit(nodeEvent, result);  // Collisions between labels of nodes that do not reference an OTU are legal. (Otherwise it would not be possible to write support values as internal node names.)
		}
		return result;
	}
}
