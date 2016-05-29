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


import info.bioinfweb.commons.io.W3CXSConstants;
import info.bioinfweb.jphyloio.objecttranslation.implementations.IllegalArgumentExceptionSimpleValueTranslator;

import java.util.Calendar;

import javax.xml.bind.DatatypeConverter;
import javax.xml.namespace.QName;



public class DateTranslator extends IllegalArgumentExceptionSimpleValueTranslator<Calendar> {
	@Override
	public QName getDataType() {
		return W3CXSConstants.DATA_TYPE_DATE;
	}
	

	@Override
	public Class<Calendar> getObjectClass() {
		return Calendar.class;
	}
	

	@Override
	protected Calendar parseValue(String representation) throws IllegalArgumentException {
		return DatatypeConverter.parseDate(representation);
	}


	@Override
	public String javaToRepresentation(Calendar object)	throws UnsupportedOperationException, ClassCastException {
		return DatatypeConverter.printDate(object);
	}
}
