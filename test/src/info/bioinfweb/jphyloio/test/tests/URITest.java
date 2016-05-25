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
package info.bioinfweb.jphyloio.test.tests;


import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;



public class URITest {
	public static void main(String[] args) {
		try {
			URI relativeURI = new URI("docs/guide/collections/designfaq.html#28");
			URI absoluteURI = new URI("http://java.sun.com/j2se/1.3/");
			URI resolvedURI = absoluteURI.resolve(relativeURI);			
			
			System.out.println(relativeURI.toString());
			System.out.println(absoluteURI.toString());			
			System.out.println(resolvedURI.toString());			
			
			new URI("http://java.sun.com/j2se:a/1.3/");
			
			URI URIFromFilePath = new URI("file:///Users/sarah/FileTest.java");
			System.out.println(URIFromFilePath.toString());
			
			System.out.println();
			
			File file = new File("D:\\LocalUserData\\example.txt");
			System.out.println(file.getPath() + " " + file.toURI() + " " + file.isAbsolute());
			file = new File((String)null, "Ordner\\example.txt");
			System.out.println(file.getPath() + " " + file.toURI() + " " + file.isAbsolute());
			file = new File("/Users/sarah/FileTest.java");
			System.out.println(file.getPath() + " " + file.toURI() + " " + file.isAbsolute());
			
			System.out.println();
			
			System.out.println(FileSystems.getDefault().getPath("D:\\LocalUserData\\example.txt")); //also: Paths.get(URI uri) or Paths.get(String first, String... more) for all segments of the path
			System.out.println(FileSystems.getDefault().getPath("Ordner\\example.txt"));
			System.out.println(FileSystems.getDefault().getPath("/Users/sarah/FileTest.java"));
			
			System.out.println();
			
			Path path = Paths.get("/Users/sarah/FileTest.java");
			System.out.println(path.toUri());
			System.out.println(path.toAbsolutePath());
			System.out.println(path.getFileSystem());			
			
			System.out.println();
			
			URIFromFilePath = URI.create("/Users/sarah/FileTest.java");
			System.out.println(URIFromFilePath.toString());
			
		}
		catch (URISyntaxException e) {
			System.out.println("Syntax exception");
			e.printStackTrace();
		}
	}
}
