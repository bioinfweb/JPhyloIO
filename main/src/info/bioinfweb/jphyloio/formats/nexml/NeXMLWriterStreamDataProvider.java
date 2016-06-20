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


import info.bioinfweb.jphyloio.dataadapters.implementations.UndefinedOTUListDataAdapter;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.LinkedLabeledIDEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataEvent;
import info.bioinfweb.jphyloio.exception.InconsistentAdapterDataException;
import info.bioinfweb.jphyloio.exception.JPhyloIOWriterException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;



public class NeXMLWriterStreamDataProvider implements NeXMLConstants {
	private NeXMLEventWriter eventWriter;
	private XMLStreamWriter writer;
	
	private Set<String> documentIDs = new HashSet<String>();
	private int idIndex = 0;
	
	private Set<String> nameSpaces = new TreeSet<String>();
	private Set<String> namespacePrefixes = new HashSet<String>();
	
	private LiteralMetadataEvent literalWithoutXMLContent;
	private StringBuffer commentContent = new StringBuffer();
	private boolean literalContentIsContinued = false;
	
	private StringBuffer currentSetElements = new StringBuffer();
	
	private boolean hasOTUList = true;
	private boolean writeUndefinedOTU = false;
	private boolean writeUndefinedOtuList = false;
	
	private NeXMLWriterAlignmentInformation currentAlignmentInfo;
	private Map<String, NeXMLWriterAlignmentInformation> idToAlignmentInfo = new HashMap<String, NeXMLWriterAlignmentInformation>();
	
	private NeXMLWriterTokenSetInformation currentTokenSetInfo;
	
	private String singleToken = null;
	
	
	public NeXMLWriterStreamDataProvider(NeXMLEventWriter eventWriter, XMLStreamWriter writer) {
		super();
		this.eventWriter = eventWriter;
		this.writer = writer;
	}


	public XMLStreamWriter getXMLStreamWriter() {
		return writer;
	}


	public NeXMLEventWriter getEventWriter() {
		return eventWriter;
	}
	
	
	public String getNexPrefix() throws XMLStreamException {
		String prefix = getXMLStreamWriter().getPrefix(NEXML_NAMESPACE);
		if (prefix == null) { //TODO should not happen, leave out this part?
			prefix = NEXML_DEFAULT_PRE;
		}
		return prefix;
	}

	
	public Set<String> getDocumentIDs() {
		return documentIDs;
	}
	
	
	public int getIDIndex() {
		return idIndex;
	}


	public void setIDIndex(int idIndex) {
		this.idIndex = idIndex;
	}


	public void addToDocumentIDs(String id) throws JPhyloIOWriterException {
		if (!getDocumentIDs().add(id)) {
			throw new InconsistentAdapterDataException("The encountered ID " + id + " already exists in the document. IDs have to be unique.");
		}
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


	public StringBuffer getCurrentSetElements() {
		return currentSetElements;
	}


	public NeXMLWriterAlignmentInformation getCurrentAlignmentInfo() {
		return currentAlignmentInfo;
	}


	public void setCurrentAlignmentInfo(NeXMLWriterAlignmentInformation currentAlignmentInfo) {
		this.currentAlignmentInfo = currentAlignmentInfo;
	}


	public Map<String, NeXMLWriterAlignmentInformation> getIdToAlignmentInfo() {
		return idToAlignmentInfo;
	}


	public NeXMLWriterTokenSetInformation getCurrentTokenSetInfo() {
		return currentTokenSetInfo;
	}


	public void setCurrentTokenSetInfo(NeXMLWriterTokenSetInformation currentTokenSetInfo) {
		this.currentTokenSetInfo = currentTokenSetInfo;
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
	
	
	public String createNewID(String prefix) {
		String id;
		
		do {
			id = prefix + getIDIndex();
			setIDIndex(getIDIndex() + 1);
		} while (getDocumentIDs().contains(id));
		
		return id;
	}
	
	
	public void writeLabeledIDAttributes(LabeledIDEvent event) throws XMLStreamException, JPhyloIOWriterException {
		writeLabeledIDAttributes(event, event.getID());
	}


	public void writeLabeledIDAttributes(LabeledIDEvent event, String about) throws XMLStreamException, JPhyloIOWriterException {
		getXMLStreamWriter().writeAttribute(ATTR_ID.getLocalPart(), event.getID());
		
		if (about != null) {
			getXMLStreamWriter().writeAttribute(ATTR_ABOUT.getLocalPart(), "#" + about);
		}
		
		if (event.hasLabel()) {
			getXMLStreamWriter().writeAttribute(ATTR_LABEL.getLocalPart(), event.getLabel());
		}
	}
	
	
	public void writeLinkedLabeledIDAttributes(LinkedLabeledIDEvent event, QName linkAttribute, boolean forceOTULink) throws XMLStreamException, JPhyloIOWriterException {		
		writeLabeledIDAttributes(event);
		if (event.hasLink()) {
			if (hasOTUList()) {
				if (!getDocumentIDs().contains(event.getLinkedID())) {
					throw new InconsistentAdapterDataException("An element links to a non-existent OTU list or OTU.");
				}
				getXMLStreamWriter().writeAttribute(linkAttribute.getLocalPart(), event.getLinkedID());
			}
			else {
				throw new InconsistentAdapterDataException("An element links to an OTU list or OTU though no OTU list exists in the document.");
			}
		}
		else if (forceOTULink) {
			if (linkAttribute.equals(TAG_OTUS)) {				
				getXMLStreamWriter().writeAttribute(linkAttribute.getLocalPart(), UndefinedOTUListDataAdapter.UNDEFINED_OTUS_ID);			
			}
			else if (linkAttribute.equals(TAG_OTU)) {			
				getXMLStreamWriter().writeAttribute(linkAttribute.getLocalPart(), UndefinedOTUListDataAdapter.UNDEFINED_OTU_ID);
			}
		}
	}
	
	
	public void setNamespacePrefix(String prefix, String namespace) throws XMLStreamException {
		setNamespacePrefix(prefix, "defaultNamespacePrefix", namespace);
	}
	
	
	public void setNamespacePrefix(String prefix, String defaultPrefix, String namespace) throws XMLStreamException {				
		if (namespace != null) {
			if (prefix == null) {
				prefix = defaultPrefix;
			}
		
			if (getXMLStreamWriter().getPrefix(namespace) == null) {
				String nameSpacePrefix = prefix;
				int index = 0;
				
				if (!getNamespacePrefixes().add(nameSpacePrefix)) {
					do {
						nameSpacePrefix = prefix + index;
						index++;
					} while (!getNamespacePrefixes().add(nameSpacePrefix));
				}
				
				getXMLStreamWriter().setPrefix(nameSpacePrefix, namespace);
				getNameSpaces().add(namespace);
			}
		}
	}
}