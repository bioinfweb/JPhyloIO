/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015-2018  Ben Stöver, Sarah Wiechers
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
package info.bioinfweb.jphyloio.demo.xmlmetadata;


import javax.xml.namespace.QName;



/**
 * Provides constants necessary for reading and writing the metadata of this demo application.
 * 
 * @author Ben St&ouml;ver
 */
public interface IOConstants {
  public static final String PREDICATE_NAMESPACE_URI = "http://example.org/predicates/";
  public static final String PREDICATE_NAMESPACE_PREFIX = "p";
  public static final QName PREDICATE_RELATED_REFERENCE = new QName(PREDICATE_NAMESPACE_URI, "relatedReference", PREDICATE_NAMESPACE_PREFIX);
  
	public static final String CUSTOM_XML_NAMESPACE_URI = "http://example.org/customXML/";
	public static final String CUSTOM_XML_NAMESPACE_PREFIX = "c";
	
	public static final QName TAG_RELATED_RESOURCE = new QName(CUSTOM_XML_NAMESPACE_URI, "relatedReference", CUSTOM_XML_NAMESPACE_PREFIX);
	public static final QName TAG_TITLE = new QName(CUSTOM_XML_NAMESPACE_URI, "title", CUSTOM_XML_NAMESPACE_PREFIX);
	public static final QName TAG_URL = new QName(CUSTOM_XML_NAMESPACE_URI, "url", CUSTOM_XML_NAMESPACE_PREFIX);
	public static final QName ATTR_TYPE = new QName(CUSTOM_XML_NAMESPACE_URI, "type", CUSTOM_XML_NAMESPACE_PREFIX);
}
