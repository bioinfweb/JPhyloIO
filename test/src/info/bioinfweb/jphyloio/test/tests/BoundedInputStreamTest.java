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


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.io.input.BoundedInputStream;



public class BoundedInputStreamTest {
	public static void main(String[] args) throws IOException {
		FileInputStream fileStream = new FileInputStream(new File("data/NeXML/treebase-record.xml")); 
		BoundedInputStream stream = new BoundedInputStream(fileStream, 20);
		//BoundedInputStream stream = new BoundedInputStream(new BufferedInputStream(fileStream), 20);

		System.out.println(stream.available() + " " + fileStream.available());
		for (int i = 0; i < 10; i++) {
			stream.read();
		}
		System.out.println(stream.available() + " " + fileStream.available());
		for (int i = 0; i < 10; i++) {
			stream.read();
		}
		System.out.println(stream.available() + " " + fileStream.available());
		System.out.println(stream.read());
		
		stream.close();
	}
}
