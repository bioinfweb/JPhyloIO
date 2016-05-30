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
package info.bioinfweb.jphyloio.objecttranslation.implementations.xsd;


import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.namespace.QName;

import info.bioinfweb.commons.io.W3CXSConstants;
import info.bioinfweb.jphyloio.objecttranslation.InvalidObjectSourceDataException;
import info.bioinfweb.jphyloio.objecttranslation.implementations.SimpleValueTranslator;



public class AnyURITranslator extends SimpleValueTranslator<URI> {
	@Override
	public QName getDataType() {
		return W3CXSConstants.DATA_TYPE_ANY_URI;
	}

	
	@Override
	public Class<URI> getObjectClass() {
		return URI.class;
	}
	

	@Override
	public URI representationToJava(String representation) throws InvalidObjectSourceDataException, UnsupportedOperationException {
		try {
			return new URI(representation);
		} 
		catch (URISyntaxException e) {
			throw new InvalidObjectSourceDataException(e);
		}
	}
}
