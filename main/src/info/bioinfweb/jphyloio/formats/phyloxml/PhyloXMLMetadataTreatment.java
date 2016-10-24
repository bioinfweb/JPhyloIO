/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015-2016  Ben St�ver, Sarah Wiechers
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
package info.bioinfweb.jphyloio.formats.phyloxml;


import info.bioinfweb.jphyloio.ReadWriteParameterNames;



/**
 * Instances of this enum can be used as parameter values for {@link ReadWriteParameterNames#KEY_PHYLOXML_METADATA_TREATMENT}.
 * It enumerates ways how metadata from hierarchical metadata structures shall be written to a <i>PhyloXML</i> document. 
 * This is necessary, since it is not possible to write nested annotations in a <i>PhyloXML</i> document.
 * 
 * @author Sarah Wiechers
 * @see ReadWriteParameterNames#KEY_PHYLOXML_METADATA_TREATMENT
 * @see PhyloXMLEventWriter
 * @since 0.0.0
 */
public enum PhyloXMLMetadataTreatment {
	/**
	 * The contents of all hierarchically structured meta events are written to the file in a sequential order. 
	 * Information about the structure gets lost, while all contents are written to the file.
	 */
	SEQUENTIAL,
	
	/**
	 * Only the contents of meta events on the top level of a hierarchical structure are written to the file, if they 
	 * had further events nested under them. The contents of the nested events are not written to the file.
	 */
	TOP_LEVEL_WITH_CHILDREN,
	
	/**
	 * Only the contents of meta events on the top level of a hierarchical structure are written to the file, if they 
	 * do not have further events nested under them.
	 */
	TOP_LEVEL_WITHOUT_CHILDREN,
	
	/**
	 * Only the contents of meta events without any nested events are written to teh file.
	 */
	ONLY_LEAFS,
	
	/**
	 * The content of meta events is not written to the file.
	 */
	NONE;
}