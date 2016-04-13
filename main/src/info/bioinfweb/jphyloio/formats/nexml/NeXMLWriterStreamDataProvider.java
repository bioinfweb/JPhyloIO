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


import info.bioinfweb.commons.bio.CharacterStateSetType;
import info.bioinfweb.jphyloio.dataadapters.implementations.UndefinedOTUListDataAdapter;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.LinkedLabeledIDEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataEvent;
import info.bioinfweb.jphyloio.exception.InconsistentAdapterDataException;
import info.bioinfweb.jphyloio.exception.JPhyloIOWriterException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.time.Duration;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;



public class NeXMLWriterStreamDataProvider implements NeXMLConstants {
	private NeXMLEventWriter eventWriter;
	
	private Set<String> documentIDs = new HashSet<String>();
	private int idIndex = 0;
	
	private Set<String> nameSpaces = new TreeSet<String>();
	private Set<String> namespacePrefixes = new HashSet<String>();
	
	private LiteralMetadataEvent literalWithoutXMLContent;
	private StringBuffer commentContent = new StringBuffer();
	private boolean literalContentIsContinued = false;
	
	private boolean hasOTUList = true; //is true if the document contains at least one OTU list	
	private boolean writeUndefinedOTU = false;
	private boolean writeUndefinedOtuList = false;
	
	private boolean writeCellsTags;
	private CharacterStateSetType alignmentType;
	
	private boolean hasTokenDefinitionSet = true;
	private boolean isNucleotideType = false;
	private Map<String, String> tokenTranslationMap = new HashMap<String, String>();
	private Set<String> tokenDefinitions = new HashSet<String>();
	
	private Map<String, String> charSetToTokenSetMap = new HashMap<String, String>();
	private Map<String, Set<Long>> charSets = new HashMap<String, Set<Long>>();
	private Map<Long, String> columnIndexToStatesMap = new HashMap<Long, String>();
	
	private String singleToken = null;
	
	private Set<String> phylogenyLinkedOtusIDs = new HashSet<String>();
	
	
	@SuppressWarnings("serial")
	private static Map<QName,Class<?>> classForXsdType = new HashMap<QName, Class<?>>() {{
		put(new QName(NAMESPACE_XS, "decimal", XSD_PRE), BigDecimal.class);
		put(new QName(NAMESPACE_XS, "integer", XSD_PRE), BigInteger.class);
		put(new QName(NAMESPACE_XS, "boolean", XSD_PRE), Boolean.class);
		put(new QName(NAMESPACE_XS, "byte", XSD_PRE), Byte.class);
		put(new QName(NAMESPACE_XS, "QName", XSD_PRE), QName.class);		
		put(new QName(NAMESPACE_XS, "double", XSD_PRE), Double.class);
		put(new QName(NAMESPACE_XS, "float", XSD_PRE), Float.class);
		put(new QName(NAMESPACE_XS, "long", XSD_PRE), Long.class);
		put(new QName(NAMESPACE_XS, "short", XSD_PRE), Short.class);		
		put(new QName(NAMESPACE_XS, "string",XSD_PRE), String.class);
		put(new QName(NAMESPACE_XS, "char", XSD_PRE), Character.class);
		put(new QName(NAMESPACE_XS, "dateTime", XSD_PRE), Date.class);
		put(new QName(NAMESPACE_XS, "duration", XSD_PRE), Duration.class);		
	}};
	
	
	@SuppressWarnings("serial")
	private static Map<Class<?>,QName> xsdTypeForClass = new HashMap<Class<?>,QName>() {{
		for ( QName xsdType : classForXsdType.keySet() ) {
			put(classForXsdType.get(xsdType), xsdType);
		}	
		put(Integer.class,new QName(NAMESPACE_XS, "integer", XSD_PRE));
		put(Date.class, new QName(NAMESPACE_XS, "dateTime", XSD_PRE));
		put(Calendar.class, new QName(NAMESPACE_XS, "dateTime", XSD_PRE));
		put(UUID.class, new QName(NAMESPACE_XS, "string", XSD_PRE));
		put(java.awt.Image.class, new QName(NAMESPACE_XS, "base64Binary", XSD_PRE));
		put(Duration.class, new QName(NAMESPACE_XS, "duration", XSD_PRE));
		put(java.lang.Character.class, new QName(NAMESPACE_XS, "char", XSD_PRE));
		put(Source.class, new QName(NAMESPACE_XS, "base64Binary", XSD_PRE));
	}};
	
	
	public NeXMLWriterStreamDataProvider(NeXMLEventWriter eventWriter) {
		super();
		this.eventWriter = eventWriter;
	}


	public XMLStreamWriter getXMLStreamWriter() {
		return eventWriter.getWriter();
	}


	public NeXMLEventWriter getEventWriter() {
		return eventWriter;
	}
	
	
	public Set<String> getDocumentIDs() {
		return documentIDs;
	}
	
	
	public int getIdIndex() {
		return idIndex;
	}


	public void setIdIndex(int idIndex) {
		this.idIndex = idIndex;
	}


	public void addToDocumentIDs(String id) throws JPhyloIOWriterException {
		if (!getDocumentIDs().add(id)) {
			throw new JPhyloIOWriterException("The encountered ID " + id + " already exists in the document. IDs have to be unique."); //TODO give different type of exception?
		}
	}


	public static Map<Class<?>, QName> getXsdTypeForClass() {
		return xsdTypeForClass;
	}


	public boolean hasOTUList() {
		return hasOTUList;
	}


	public void setHasOTUList(boolean hasOTUList) {
		this.hasOTUList = hasOTUList;
	}


	public LiteralMetadataEvent getLiteralWithoutXMLContent() {
		return literalWithoutXMLContent;
	}


	public void setLiteralWithoutXMLContent(LiteralMetadataEvent literalWithoutXMLContent) {
		this.literalWithoutXMLContent = literalWithoutXMLContent;
	}


	public Set<String> getNameSpaces() {
		return nameSpaces;
	}


	public Set<String> getNamespacePrefixes() {
		return namespacePrefixes;
	}


	public StringBuffer getCommentContent() {
		return commentContent;
	}


	public boolean isLiteralContentIsContinued() {
		return literalContentIsContinued;
	}


	public void setLiteralContentIsContinued(boolean literalContentIsContinued) {
		this.literalContentIsContinued = literalContentIsContinued;
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


	public boolean hasTokenDefinitionSet() {
		return hasTokenDefinitionSet;
	}


	public void setHasTokenDefinitionSet(boolean hasTokenDefinitionSet) {
		this.hasTokenDefinitionSet = hasTokenDefinitionSet;
	}


	public boolean isNucleotideType() {
		return isNucleotideType;
	}


	public void setNucleotideType(boolean isNucleotideType) {
		this.isNucleotideType = isNucleotideType;
	}


	public Map<String, String> getTokenTranslationMap() {
		return tokenTranslationMap;
	}


	public Set<String> getTokenDefinitions() {
		return tokenDefinitions;
	}


	public Map<String, String> getCharSetToTokenSetMap() {
		return charSetToTokenSetMap;
	}
	

	public Map<String, Set<Long>> getCharSets() {
		return charSets;
	}


	public Map<Long, String> getColumnIndexToStatesMap() {
		return columnIndexToStatesMap;
	}


	public String getSingleToken() {
		return singleToken;
	}


	public void setSingleToken(String singleToken) {
		this.singleToken = singleToken;
	}


	public boolean isWriteUndefinedOTU() {
		return writeUndefinedOTU;
	}


	public void setWriteUndefinedOTU(boolean writeUndefinedOTU) {
		this.writeUndefinedOTU = writeUndefinedOTU;
	}


	public boolean isWriteUndefinedOtuList() {
		return writeUndefinedOtuList;
	}


	public void setWriteUndefinedOtuList(boolean writeUndefinedOtuList) {
		this.writeUndefinedOtuList = writeUndefinedOtuList;
	}


	public Set<String> getPhylogenyLinkedOtusIDs() {
		return phylogenyLinkedOtusIDs;
	}
	
	
	public String createNewID(String prefix) {
		String id;
		
		do {
			id = prefix + getIdIndex();
			setIdIndex(getIdIndex() + 1);
		} while (getDocumentIDs().contains(id));
		
		return id;
	}
	
	
	public void writeLabeledIDAttributes(LabeledIDEvent event) throws XMLStreamException, JPhyloIOWriterException {
		writeLabeledIDAttributes(event, event.getID());
	}


	public void writeLabeledIDAttributes(LabeledIDEvent event, String about) throws XMLStreamException, JPhyloIOWriterException {
		getEventWriter().getWriter().writeAttribute(ATTR_ID.getLocalPart(), event.getID());
		
		if (about != null) {
			getEventWriter().getWriter().writeAttribute(ATTR_ABOUT.getLocalPart(), "#" + about);
		}
		
		if (event.hasLabel()) {
			getEventWriter().getWriter().writeAttribute(ATTR_LABEL.getLocalPart(), event.getLabel());
		}
	}
	
	
	public void writeLinkedLabeledIDAttributes(LinkedLabeledIDEvent event, QName linkAttribute, boolean forceOTULink) throws XMLStreamException, JPhyloIOWriterException {		
		writeLabeledIDAttributes(event);
		if (event.hasLink()) {
			if (hasOTUList()) {
				if (!getDocumentIDs().contains(event.getLinkedID())) {
					throw new InconsistentAdapterDataException("An element links to a non-existent OTU list or OTU.");
				}
				getEventWriter().getWriter().writeAttribute(linkAttribute.getLocalPart(), event.getLinkedID());
			}
			else {
				throw new InconsistentAdapterDataException("An element links to an OTU list or OTU though no OTU list exists in the document.");
			}
		}
		else if (forceOTULink) {
			if (linkAttribute.equals(TAG_OTUS)) {				
				getEventWriter().getWriter().writeAttribute(linkAttribute.getLocalPart(), UndefinedOTUListDataAdapter.UNDEFINED_OTUS_ID);			
			}
			else if (linkAttribute.equals(TAG_OTU)) {			
				getEventWriter().getWriter().writeAttribute(linkAttribute.getLocalPart(), UndefinedOTUListDataAdapter.UNDEFINED_OTU_ID);
			}
		}
	}
}
