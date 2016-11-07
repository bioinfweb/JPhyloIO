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
package info.bioinfweb.jphyloio.events.meta;


import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;

import java.net.URI;



/**
 * Indicates that metadata linking a <i>RDF</i> resource has been encountered at the current position of the document. The resource 
 * can either be named and referenced by {@link #getHRef()} or (if {@link #getHRef()} returns {@code null}) be an anonymous 
 * <i>RDF</i> which is formed by the set of upcoming nested metaevents.
 * <p>
 * This event has the topology type {@link EventTopologyType#START} and other resource and literal metadata event subsequences may
 * be nested before the according end event. The content type is {@link EventContentType#RESOURCE_META}.
 *
 * @author Ben St&ouml;ver
 * @since 0.0.0
 */
public class ResourceMetadataEvent extends LabeledIDEvent {
	private URIOrStringIdentifier rel;
	private URI hRef;
	private String about;


	/**
	 * Creates a new instance of this class.
	 *
	 * @param id the unique ID associated with the represented data element (Must be a valid
	 *        <a href="https://www.w3.org/TR/1999/REC-xml-names-19990114/#NT-NCName">NCName</a>.)
	 * @param label a label associated with the represented data element (Maybe {@code null}.)
	 * @param rel the <i>RDF</i> rel URI of this element
	 * @param hRef the <i>RDF</i> hRef URI of this element
	 * @param about the content of a specific about attribute to be written on the according <i>XML</i> representation of this element
	 *        (Maybe {@code null}.)
	 * @throws NullPointerException if {@code id} or {@code rel} are {@code null}
	 * @throws IllegalArgumentException if the specified ID is not a valid
	 *         <a href="https://www.w3.org/TR/1999/REC-xml-names-19990114/#NT-NCName">NCName</a>
	 */
	public ResourceMetadataEvent(String id, String label, URIOrStringIdentifier rel, URI hRef, String about) {
		super(EventContentType.RESOURCE_META, id, label);
		if (rel == null) {
			throw new NullPointerException("\"rel\" must not be null.");
		}
		else {
			this.rel = rel;
			this.hRef = hRef;
			this.about = about;
		}
	}


	public URIOrStringIdentifier getRel() {
		return rel;
	}


	public URI getHRef() {
		return hRef;
	}


	public String getAbout() {
		return about;
	}
}
