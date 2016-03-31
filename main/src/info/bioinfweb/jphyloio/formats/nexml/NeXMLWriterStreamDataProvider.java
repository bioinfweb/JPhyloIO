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
package info.bioinfweb.jphyloio.formats.nexml;


import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.time.Duration;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import info.bioinfweb.commons.bio.CharacterStateSetType;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.LinkedOTUOrOTUsEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataEvent;
import info.bioinfweb.jphyloio.exception.JPhyloIOWriterException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;



public class NeXMLWriterStreamDataProvider implements NeXMLConstants {
	private NeXMLEventWriter eventWriter;
	private LiteralMetadataEvent literalWithoutXMLContent;
	
	private boolean emptyDocument = false; //is true if the document contains nothing or only document meta data
	private boolean hasOTUList = true; //is true if the document contains at least one OTU list
	private Set<String> metaDataNameSpaces = new TreeSet<String>();
	
	private boolean writeCellsTags;
	private CharacterStateSetType alignmentType = CharacterStateSetType.UNKNOWN;
	
	private Map<String, String> charSetToTokenSetMap = new HashMap<String, String>();
	private Map<String, Set<Long>> charSets = new HashMap<String, Set<Long>>();
	private Map<Long, String> charIndexToIDMap = new HashMap<Long, String>();
	private Map<String, String> charIDToStatesMap = new HashMap<String, String>();
	
	private boolean writeUndefinedOTU = false;
	
	@SuppressWarnings("serial")
	private static Map<QName,Class<?>> classForXsdType = new HashMap<QName, Class<?>>() {{
		put(new QName(XS_URI,"decimal",XSD_PRE), BigDecimal.class);
		put(new QName(XS_URI,"integer",XSD_PRE), BigInteger.class);
		put(new QName(XS_URI,"boolean",XSD_PRE), Boolean.class);
		put(new QName(XS_URI,"byte",XSD_PRE), Byte.class);
		put(new QName(XS_URI,"QName",XSD_PRE), QName.class);		
		put(new QName(XS_URI,"double",XSD_PRE), Double.class);
		put(new QName(XS_URI,"float",XSD_PRE), Float.class);
		put(new QName(XS_URI,"long",XSD_PRE), Long.class);
		put(new QName(XS_URI,"short",XSD_PRE), Short.class);		
		put(new QName(XS_URI,"string",XSD_PRE), String.class);
		put(new QName(XS_URI,"char",XSD_PRE), Character.class);
		put(new QName(XS_URI,"dateTime",XSD_PRE), Date.class);
//		put(new QName(XS_URI,"base64Binary",XSD_PRE), Base64BinaryWrapper.class); //TODO do we really need this?
		put(new QName(XS_URI,"duration",XSD_PRE), Duration.class);		
	}};
	
	@SuppressWarnings("serial")
	private static Map<Class<?>,QName> xsdTypeForClass = new HashMap<Class<?>,QName>() {{
		for ( QName xsdType : classForXsdType.keySet() ) {
			put(classForXsdType.get(xsdType), xsdType);
		}	
		put(Integer.class,new QName(XS_URI,"integer",XSD_PRE));
		put(Date.class, new QName(XS_URI,"dateTime",XSD_PRE));
		put(Calendar.class, new QName(XS_URI,"dateTime",XSD_PRE));
		put(UUID.class, new QName(XS_URI,"string",XSD_PRE));
		put(java.awt.Image.class, new QName(XS_URI,"base64Binary",XSD_PRE));
		put(Duration.class, new QName(XS_URI,"duration",XSD_PRE));
		put(java.lang.Character.class, new QName(XS_URI,"char",XSD_PRE));
		put(Source.class, new QName(XS_URI,"base64Binary",XSD_PRE));
	}};
	
	public NeXMLWriterStreamDataProvider(NeXMLEventWriter eventWriter) {
		super();
		this.eventWriter = eventWriter;
	}


	public NeXMLEventWriter getEventWriter() {
		return eventWriter;
	}
	
	
	public LiteralMetadataEvent getLiteralWithoutXMLContent() {
		return literalWithoutXMLContent;
	}


	public void setLiteralWithoutXMLContent(LiteralMetadataEvent literalWithoutXMLContent) {
		this.literalWithoutXMLContent = literalWithoutXMLContent;
	}


	public static Map<Class<?>, QName> getXsdTypeForClass() {
		return xsdTypeForClass;
	}
	
	
	public boolean isEmptyDocument() {
		return emptyDocument;
	}
	

	public void setEmptyDocument(boolean empty) {
		this.emptyDocument = empty;
	}


	public boolean hasOTUList() {
		return hasOTUList;
	}


	public void setHasOTUList(boolean hasOTUList) {
		this.hasOTUList = hasOTUList;
	}


	public Set<String> getMetaDataNameSpaces() {
		return metaDataNameSpaces;
	}


	public boolean isWriteCellsTags() {
		return writeCellsTags;
	}


	public void setWriteCellsTags(boolean writeCellsTags) {
		this.writeCellsTags = writeCellsTags;
	}


	public CharacterStateSetType getAlignmentType() {
		return alignmentType;
	}


	public void setAlignmentType(CharacterStateSetType alignmentType) throws JPhyloIOWriterException {
		this.alignmentType = alignmentType;	
	}


	public Map<String, String> getCharSetToTokenSetMap() {
		return charSetToTokenSetMap;
	}
	

	public Map<String, Set<Long>> getCharSets() {
		return charSets;
	}


	public Map<Long, String> getCharIndexToIDMap() {
		return charIndexToIDMap;
	}


	public Map<String, String> getCharIDToStatesMap() {
		return charIDToStatesMap;
	}


	public boolean isWriteUndefinedOTU() {
		return writeUndefinedOTU;
	}


	public void setWriteUndefinedOTU(boolean writeUndefinedOTU) {
		this.writeUndefinedOTU = writeUndefinedOTU;
	}


	public void writeLabeledIDAttributes(LabeledIDEvent event) throws XMLStreamException {
		getEventWriter().getWriter().writeAttribute(ATTR_ID.getLocalPart(), event.getID());  //TODO Add ID to set to ensure all IDs are unique. (Probably a task that should use resources to be added to the super class.)
		getEventWriter().getWriter().writeAttribute(ATTR_ABOUT.getLocalPart(), "#" + event.getID()); //TODO maybe only write about attribute if meta data follows?
		if (event.hasLabel()) {
			getEventWriter().getWriter().writeAttribute(ATTR_LABEL.getLocalPart(), event.getLabel());
		}
	}
	
	
	public void writeLinkedOTUOrOTUsAttributes(LinkedOTUOrOTUsEvent event, QName linkAttribute, boolean forceOTULink) throws XMLStreamException {
		writeLabeledIDAttributes(event);
		if (event.isOTUOrOTUsLinked()) {
			getEventWriter().getWriter().writeAttribute(linkAttribute.getLocalPart(), event.getOTUOrOTUsID());
		}
		else if (forceOTULink) {
			//TODO Link UNDEFINED taxon, if an OTU shall be linked.
		}
		//TODO Linking OTUs is never optional, therefore one OTU (usually the one containing the UDEFINED taxon) should be linked.
	}
}
