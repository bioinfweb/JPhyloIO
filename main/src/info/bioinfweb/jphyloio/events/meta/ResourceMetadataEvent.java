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
package info.bioinfweb.jphyloio.events.meta;


import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;

import java.net.URI;

import javax.xml.namespace.QName;



/**
 * Indicates that metadata linking a RDF resource has been encountered at the current position of the document. The resource can 
 * either be named and referenced by {@link #getHRef()} or (if {@link #getHRef()} returns {@code null}) be an anonymous RDF which 
 * is formed by the set of upcoming nested metaevents. 
 * 
 * @author Ben St&ouml;ver
 * @since 0.0.0
 */
public class ResourceMetadataEvent extends LabeledIDEvent {
	private QName rel;
	private URI hRef;
	private String about;
	
	
	public ResourceMetadataEvent(String id, String label, QName rel, URI hRef, String about) {
		super(EventContentType.META_RESOURCE, id, label);
		this.rel = rel;
		this.hRef = hRef;
		this.about = about;
	}


	public QName getRel() {
		return rel;
	}


	public URI getHRef() {
		return hRef;
	}


	public String getAbout() {
		return about;
	}
}
