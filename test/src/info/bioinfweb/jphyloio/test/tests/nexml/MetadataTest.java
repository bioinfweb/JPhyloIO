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
package info.bioinfweb.jphyloio.test.tests.nexml;


import info.bioinfweb.jphyloio.events.type.EventContentType;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;

import org.nexml.model.Annotation;
import org.nexml.model.Document;
import org.nexml.model.DocumentFactory;
import org.nexml.model.OTU;
import org.nexml.model.OTUs;



public class MetadataTest {
	private static class MyClass {
		private int someInt;
		private String someString;
		private EventContentType someEnum;
		
		public MyClass(int someInt, String someString, EventContentType someEnum) {
			super();
			this.someInt = someInt;
			this.someString = someString;
			this.someEnum = someEnum;
		}

		public int getSomeInt() {
			return someInt;
		}
		
		public void setSomeInt(int someInt) {
			this.someInt = someInt;
		}
		
		public String getSomeString() {
			return someString;
		}
		
		public void setSomeString(String someString) {
			this.someString = someString;
		}
		
		public EventContentType getSomeEnum() {
			return someEnum;
		}
		
		public void setSomeEnum(EventContentType someEnum) {
			this.someEnum = someEnum;
		}
	}
	
	
	public static void main(String[] args) throws ParserConfigurationException, URISyntaxException, IOException {
		final URI nameSpaceURI = URI.create("http://example.org/knownTypes");
		
		Document document = DocumentFactory.createDocument();
		OTUs otus = document.createOTUs();
		otus.setLabel("some OTUs");
		OTU otu = otus.createOTU();
		otu.setLabel("some OTU");
		
		otu.addAnnotationValue("kt:hasString", nameSpaceURI, "top level literal");
		//Annotation annotation = otu.addAnnotationValue("kt:hasNumber", nameSpaceURI, 18.3);  // Value will be ignored as soon as nested elements are present.
		//Annotation annotation = otu.addAnnotationValue("kt:hasResource", nameSpaceURI, new URI("http://bioinfweb.info/SubjectConflict"));
		Annotation annotation = otu.addAnnotationValue("kt:hasResource", nameSpaceURI, "");
		annotation.setId("parentMeta");
		annotation.setAbout("#" + annotation.getId());
		annotation.addAnnotationValue("kt:hasString", nameSpaceURI, "nested meta 1");
		annotation.addAnnotationValue("kt:hasString", nameSpaceURI, "nested meta 2");
//		otu.addAnnotationValue("kt:hasResource", nameSpaceURI, new URL("http://bioinfweb.info/Test_URL"));  // Not written as resource meta.
//		otu.addAnnotationValue("kt:hasResource", nameSpaceURI, new URI("http://bioinfweb.info/Test_URI"));
//		otu.addAnnotationValue("kt:hasMyObject", nameSpaceURI, new MyClass(18, "Hello World!", EventContentType.ALIGNMENT));
//		otu.addAnnotationValue("kt:hasImage", nameSpaceURI, ImageIO.read(new File("data/other/Test.png")));  // Not correctly written.
		
		System.out.println(document.getXmlString());
	}
}
