/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015-2019  Ben Stöver, Sarah Wiechers
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
package info.bioinfweb.jphyloio.objecttranslation;


import info.bioinfweb.commons.io.W3CXSConstants;
import info.bioinfweb.jphyloio.objecttranslation.implementations.BooleanTranslator;


import org.junit.* ;

import static org.junit.Assert.* ;



public class ObjectTranslatorFactoryTest {
	@Test
	public void test_getDefaultTranslator() {
		ObjectTranslatorFactory factory = new ObjectTranslatorFactory();
		factory.addXSDTranslators(false);
		assertEquals(new BooleanTranslator(), factory.getDefaultTranslator(W3CXSConstants.DATA_TYPE_BOOLEAN));
	}
	
	
	@Test
	public void test_getTranslator() {
		ObjectTranslatorFactory factory = new ObjectTranslatorFactory();
		factory.addXSDTranslators(false);
		assertEquals(new BooleanTranslator(), factory.getTranslator(W3CXSConstants.DATA_TYPE_BOOLEAN, Boolean.class));
		assertNull(factory.getTranslator(W3CXSConstants.DATA_TYPE_BOOLEAN, Integer.class));
	}
}
