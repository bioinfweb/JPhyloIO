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


import java.util.HashSet;
import java.util.Set;

import info.bioinfweb.jphyloio.formats.WriterStreamDataProvider;

import javax.xml.stream.XMLStreamWriter;



public class XMLWriterStreamDataProvider<R extends AbstractXMLEventWriter> extends WriterStreamDataProvider<R> {
	private Set<String> namespacePrefixes = new HashSet<String>();	
	private StringBuffer commentContent = new StringBuffer();
	
	
	public XMLWriterStreamDataProvider(R eventWriter) {
		super(eventWriter);
	}
	

	public XMLStreamWriter getWriter() {
		return getEventWriter().getXMLWriter();
	}
	
	
	public Set<String> getNamespacePrefixes() {
		return namespacePrefixes;
	}


	public StringBuffer getCommentContent() {
		return commentContent;
	}
}
