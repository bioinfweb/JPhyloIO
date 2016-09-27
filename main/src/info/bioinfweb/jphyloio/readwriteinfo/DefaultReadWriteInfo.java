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


import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.TreeSet;

import info.bioinfweb.jphyloio.events.type.EventContentType;



/**
 * Default implementation of {@link ReadWriteInfo}.
 * 
 * @author Ben St&ouml;ver
 * @since 0.1.0
 */
public class DefaultReadWriteInfo implements ReadWriteInfo {
	private EnumSet<EventContentType> supportedContentTypes;
	private EnumSet<EventContentType> supportedMetadata;
	private Set<String> supportedParameters;
	
	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param supportedContentTypes the set of supported content types (If {@code null} is specified, an empty set is assumed.)
	 * @param supportedMetadata the set of content types under which any kind of metadata is supported (If {@code null} is 
	 *                          specified, an empty set is assumed.)
	 * @param supportedParameters the set of parameters that are supported by the associated reader or writer (If {@code null}  
	 *                            is specified, an empty set is assumed.)
	 */
	public DefaultReadWriteInfo(EnumSet<EventContentType> supportedContentTypes, EnumSet<EventContentType> supportedMetadata, 
			Set<String> supportedParameters) {
		
		super();
		
		if (supportedMetadata == null) {
			this.supportedContentTypes = EnumSet.noneOf(EventContentType.class);
		}
		else {
			this.supportedContentTypes = supportedContentTypes;
		}
		
		if (supportedMetadata == null) {
			this.supportedMetadata = EnumSet.noneOf(EventContentType.class);
		}
		else {
			this.supportedMetadata = supportedMetadata;
		}
		
		if (supportedParameters == null) {
			this.supportedParameters = Collections.emptySet();
		}
		else {
			this.supportedParameters = supportedParameters;
		}
	}


	/**
	 * Creates a new instance of this class.
	 * 
	 * @param supportedContentTypes the set of supported content types (If {@code null} is specified, an empty set is assumed.)
	 * @param supportedMetadata the set of content types under which any kind of metadata is supported (If {@code null} is 
	 *                          specified, an empty set is assumed.)
	 * @param supportedParameters the set of parameters that are supported by the associated reader or writer (If {@code null}  
	 *                            is specified, an empty set is assumed.)
	 */
	public DefaultReadWriteInfo(EnumSet<EventContentType> supportedContentTypes, EnumSet<EventContentType> supportedMetadata,
			String... supportedParameters) {
		
		this(supportedContentTypes, supportedMetadata, new TreeSet<String>(Arrays.asList(supportedParameters)));
	}


	@Override
	public boolean isElementModeled(EventContentType contentType) {
		return supportedContentTypes.contains(contentType);
	}

	
	@Override
	public boolean isMetadataModeled(EventContentType parentContentType) {
		return supportedMetadata.contains(parentContentType);
	}

	
	@Override
	public boolean isParameterSupported(String parameterName) {
		return supportedParameters.contains(parameterName);
	}
}
