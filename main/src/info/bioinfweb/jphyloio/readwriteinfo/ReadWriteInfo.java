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
package info.bioinfweb.jphyloio.readwriteinfo;

import info.bioinfweb.jphyloio.ReadWriteParameterNames;
import info.bioinfweb.jphyloio.events.type.EventContentType;



public interface ReadWriteInfo {
	/**
	 * Determines whether the represented format allows to model the specified data element (content type). Readers will not 
	 * produce events of an unsupported type, while writers will accept but ignore events of this type.
	 * <p>
	 * Note that the values returned here indicate the capabilities of the provided readers and writers and not the 
	 * capabilities of the respective format. In may happen that elements of a format are not supported by its reader and 
	 * writer. In such cases {@code false} would be returned here for the according content type.
	 * <p>
	 * Note this method will return {@code true} for content types that may occur at multiple positions in the event stream 
	 * (e.g. {@link EventContentType#META_LITERAL} or {@link EventContentType#SET_ELEMENT}), if they are supported at least
	 * at one position. Use {@link #isMetadataModeled(EventContentType)} to check at which position metadata elements are
	 * supported.
	 * 
	 * @param contentType the content type to be tested
	 * @return {@code true} if the specified content type is supported, {@code false} otherwise
	 * @see #isMetadataModeled(EventContentType)
	 */
	public boolean isElementModeled(EventContentType contentType);
	
	/**
	 * Determines whether the represented format allows to model any kind of metadata nested under the specified data element 
	 * Readers will not produce metaevents under an unsupported type, while writers will accept but ignore metaevents under 
	 * this type.
	 * <p>
	 * Note that the values returned here indicate the capabilities of the provided readers and writers and not the 
	 * capabilities of the respective format.
	 * <p>
	 * Note this method will return {@code true} if any literal of resource metadata is supported. This does not necessary
	 * mean that any type of metadata is supported. In some cases only a single level of literal metadata or even only certain
	 * predicates may be supported, while in other cases a whole <i>RDFa</i> metadata tree maybe legal. The way metadata are
	 * represented may even depend of specified reader or writer parameters. Refer to the documentations of the reader or
	 * writer in question for further details on supported metadata. 
	 * 
	 * @param contentType the content type of the parent element of possibly supported metadata
	 * @return {@code true} if metadata may be nested under the specified content type, {@code false} otherwise
	 * @see #isElementModeled(EventContentType)
	 */
	public boolean isMetadataModeled(EventContentType parentContentType);
	
	/**
	 * Determines whether the associated reader or writer supports the specified parameter. Parameter names used by readers
	 * and writers included in <i>JPhyloIO</i> are defined as string constants in {@link ReadWriteParameterNames}. Readers
	 * and writers from third party developers may introduce additional parameter names defined elsewhere.
	 * 
	 * @param parameterName the name of the parameter to be tested
	 * @return {@code true} of the associated reader or writer makes use of the parameter with the specified name, or
	 *         {@code false} otherwise
	 */
	public boolean isParameterSupported(String parameterName);
}
