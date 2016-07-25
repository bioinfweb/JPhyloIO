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
package info.bioinfweb.jphyloio.formats.phyloxml;

import info.bioinfweb.jphyloio.ReadWriteConstants;

import javax.xml.namespace.QName;

/**
 * This interface contains constants used by internally in JPhyloIO by {@link PhyloXMLEventWriter} and PhyloXML data receivers. 
 * They are not supposed to be used by application developers. Constants that can be used are contained in {@link PhyloXMLConstants}.
 * 
 * @author Sarah Wiechers
 *
 */
public interface PhyloXMLPrivateConstants {
	public static final QName IDENTIFIER_PHYLOGENY = new QName("Phylogeny");	
	public static final QName IDENTIFIER_CLADE = new QName("Clade");
	
	public static final QName IDENTIFIER_ANY_PREDICATE = new QName("AnyPredicate");
	public static final QName IDENTIFIER_CUSTOM_XML = new QName("CustomXML");
}
