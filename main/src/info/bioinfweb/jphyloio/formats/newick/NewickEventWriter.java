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


import java.io.Writer;
import java.util.Iterator;

import info.bioinfweb.commons.log.ApplicationLogger;
import info.bioinfweb.jphyloio.AbstractEventWriter;
import info.bioinfweb.jphyloio.EventWriterParameterMap;
import info.bioinfweb.jphyloio.dataadapters.DocumentDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.TreeNetworkDataAdapter;



public class NewickEventWriter extends AbstractEventWriter implements NewickConstants {
	@Override
	public void writeDocument(DocumentDataAdapter document, Writer writer, EventWriterParameterMap parameters) throws Exception {
		ApplicationLogger logger = getLogger(parameters);
		
		logIngnoredOTULists(document, logger, "Newick/NHX", "tree nodes"); 
		if (document.getMatrixIterator().hasNext()) {
			logger.addWarning(
					"The specified matrix (matrices) will not be written, since the Newick/NHX format does not support such data."); 
		}
		
		Iterator<TreeNetworkDataAdapter> treeNetworkIterator = document.getTreeNetworkIterator();
		if (treeNetworkIterator.hasNext()) {
			while (treeNetworkIterator.hasNext()) {
				TreeNetworkDataAdapter treeNetwork = treeNetworkIterator.next();
				new NewickStringWriter(writer, treeNetwork, getReferencedOTUList(document, treeNetwork), false, null, parameters).write();
			}
		}
		else {
			logger.addWarning(
					"An empty document was written, since no tree definitions were affered by the specified document adapter.");  //TODO Use message, that would be more understandable by application users (which does not use library-specific terms)?
		}
	}
}
