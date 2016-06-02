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
package info.bioinfweb.jphyloio.formats.xml;


import info.bioinfweb.jphyloio.JPhyloIOEventReader;

import javax.xml.namespace.NamespaceContext;



public interface JPhyloIOXMLEventReader extends JPhyloIOEventReader {
	/**
	 * Returns the namespace context that is valid at the current position of the document. The returned value therefore may change with 
	 * each call of {@link #next()}.
	 * <p>
	 * Applications can use this namespace context e.g. to handle custom XML in literal meta events. In such cases, the according context
	 * object should be requested (and saved if necessary) before the next call of {@link #next()}, because prefix to namespace mapping
	 * may already change by reading the data for the next event.
	 * 
	 * @return the current namespace context object or {@code null} if the reader did not encounter the first XML tag of the document yet
	 */
	public NamespaceContext getNamespaceContext();
	//TODO Add hint for possible problems with events created from buffered data? (See also #123.)
}
