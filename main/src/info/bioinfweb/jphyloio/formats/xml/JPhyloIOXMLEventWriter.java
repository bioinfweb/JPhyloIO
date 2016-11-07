/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015-2016  Ben St�ver, Sarah Wiechers
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
package info.bioinfweb.jphyloio.formats.xml;


import javax.xml.namespace.NamespaceContext;

import info.bioinfweb.jphyloio.JPhyloIOEventWriter;



/**
 * Interface providing basic functionality for all JPhyloIO writers of XML formats.
 * 
 * Note, that these writers are able to change the prefix a namespace is bound to. Therefore
 * it is important that applications writing prefixes in attributes or character data always use the provided 
 * namespace context object to obtain the prefix a namespace is currently bound to.
 //TODO point to this information at a central place (e.g. JPhyloIOEventWriter)
 * 
 * @author Sarah Wiechers
 *
 */
public interface JPhyloIOXMLEventWriter extends JPhyloIOEventWriter {
	/**
	 * Returns the currently valid namespace context of the writer.
	 * 
	 * @return the currently valid {@link NamespaceContext} object
	 */
	public NamespaceContext getNamespaceContext();
}
