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
package info.bioinfweb.jphyloio.objecttranslation.implementations;


import info.bioinfweb.commons.io.XMLUtils;
import info.bioinfweb.jphyloio.objecttranslation.InvalidObjectSourceDataException;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;



/**
 * An object translator from and to {@link QName}. 
 * 
 * @author Ben St&ouml;ver
 * @since 0.0.0
 */
public class QNameTranslator extends SimpleValueTranslator<QName> {
	@Override
	public Class<QName> getObjectClass() {
		return QName.class;
	}

	
	@Override
	public QName representationToJava(String representation) throws InvalidObjectSourceDataException, UnsupportedOperationException {
		//TODO Also use DatatypeConverter here instead.
		int splitPos = representation.indexOf(XMLUtils.QNAME_SEPARATOR);
		if (splitPos == -1) {
			return new QName(representation);
		}
		else {
			return new QName(XMLConstants.NULL_NS_URI, representation.substring(splitPos + 1), representation.substring(0, splitPos));
					//TODO Should/can a prefix dependent namespace be resolved somehow?
		}
		//TODO Should any additional validation (e.g. of NCNames) be done in here?
		//TODO Should parsing QNames including "{namespaceURI} also be supported?
	}


	@Override
	public String javaToRepresentation(QName object) throws UnsupportedOperationException, ClassCastException {
		//TODO Also use DatatypeConverter here instead.
		if ("".equals(object.getPrefix())) {  // Constructing instances with null is not possible.
			return object.getLocalPart();
		}
		else {
			return object.getPrefix() + XMLUtils.QNAME_SEPARATOR + object.getLocalPart();
		}
		//TODO Should output of QNames including "{namespaceURI} also or in another translator be supported?
	}
}
