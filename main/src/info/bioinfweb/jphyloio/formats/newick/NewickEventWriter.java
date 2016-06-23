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
package info.bioinfweb.jphyloio.formats.newick;


import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

import info.bioinfweb.commons.log.ApplicationLogger;
import info.bioinfweb.jphyloio.AbstractEventWriter;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.dataadapters.DocumentDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.OTUListDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.TreeNetworkDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.TreeNetworkGroupDataAdapter;
import info.bioinfweb.jphyloio.formats.JPhyloIOFormatIDs;



/**
 * Event writer for the Newick format.
 * 
 * @author Ben St&ouml;ver
 * @see 0.0.0
 */
public class NewickEventWriter extends AbstractEventWriter implements NewickConstants {
	@Override
	public String getFormatID() {
		return JPhyloIOFormatIDs.NEWICK_FORMAT_ID;
	}
	
	
	@Override
	public void writeDocument(DocumentDataAdapter document, Writer writer, ReadWriteParameterMap parameters) throws IOException {
		ApplicationLogger logger = parameters.getLogger();
		int treeCount = 0;
		
		logIngnoredOTULists(document, logger, "Newick/NHX", "tree nodes"); 
		if (document.getMatrixIterator().hasNext()) {
			logger.addWarning(
					"The specified matrix (matrices) will not be written, since the Newick/NHX format does not support such data."); 
		}
		
		Iterator<TreeNetworkGroupDataAdapter> treeNetworkGroupIterator = document.getTreeNetworkGroupIterator();
		while (treeNetworkGroupIterator.hasNext()) {
			TreeNetworkGroupDataAdapter treeNetworkGroup = treeNetworkGroupIterator.next();
			OTUListDataAdapter otuList = getReferencedOTUList(document, treeNetworkGroup, parameters);
			
			Iterator<TreeNetworkDataAdapter> treeNetworkIterator = treeNetworkGroup.getTreeNetworkIterator();
			while (treeNetworkIterator.hasNext()) {
				treeCount++;
				TreeNetworkDataAdapter treeNetwork = treeNetworkIterator.next();
				new NewickStringWriter(writer, treeNetwork, new DefaultNewickWriterNodeLabelProcessor(otuList, parameters), parameters).write();
			}			
		}
		
		if (treeCount == 0) {
			logger.addWarning(
					"An empty document was written, since no tree definitions were offered by the specified document adapter.");  //TODO Use message, that would be more understandable by application users (which does not use library-specific terms)?
		}
	}
}
