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
package info.bioinfweb.jphyloio.test.tests.boundedstream;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.CharBuffer;

import org.apache.commons.io.input.BoundedInputStream;



public class BoundedInputStreamTest {
	private static class DelegatingInputStream extends InputStream {
		private InputStream underlyingStream;

		
		public DelegatingInputStream(InputStream underlyingStream) {
			super();
			this.underlyingStream = underlyingStream;
		}


		public int available() throws IOException {
			return underlyingStream.available();
		}


		public void close() throws IOException {
			underlyingStream.close();
		}


		public void mark(int readlimit) {
			underlyingStream.mark(readlimit);
		}


		public boolean markSupported() {
			return underlyingStream.markSupported();
		}


		public int read() throws IOException {
			return underlyingStream.read();
		}


		public int read(byte[] b, int off, int len) throws IOException {
			System.out.println("read 1");
			return underlyingStream.read(b, off, len);
		}


		public int read(byte[] b) throws IOException {
			System.out.println("read 2");
			return underlyingStream.read(b);
		}


		public void reset() throws IOException {
			underlyingStream.reset();
		}


		public long skip(long n) throws IOException {
			System.out.println("skip");
			return read(new byte[(int)n]);  // Does not work for large values.
		}
	}
	
	
	private static void testAvailable() throws IOException {
		FileInputStream fileStream = new FileInputStream(new File("data/NeXML/treebase-record.xml"));
		try {
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
		}
		finally {
			fileStream.close();
		}
	}
	
	
	private static void testDoubleCounting() throws IOException {
		BoundedInputStream stream = new BoundedInputStream(new BufferedInputStream(new FileInputStream(
				new File("data/NeXML/treebase-record.xml"))));
		try {
			System.out.println("Available: " + stream.available());
			System.out.println("Skipped: " + stream.skip(5));  // What if this would call read() internally?
			System.out.println("Available: " + stream.available());
			System.out.println("read: " + stream.read(new byte[5]));
			System.out.println("Available: " + stream.available());
			System.out.println("read: " + stream.read(new byte[5], 0, 5));
			System.out.println("Available: " + stream.available());
		}
		finally {
			stream.close();
		}
	}
	
	
	private static void testDoubleCountingDelegating() throws IOException {
		BoundedInputStream stream = new BoundedInputStream(new DelegatingInputStream(new FileInputStream(
				new File("data/NeXML/treebase-record.xml"))));
		try {
			System.out.println("Available: " + stream.available());
			System.out.println("Skipped: " + stream.skip(5));  // What if this would call read() internally?
			System.out.println("Available: " + stream.available());
			System.out.println("read: " + stream.read(new byte[5]));
			System.out.println("Available: " + stream.available());
			System.out.println("read: " + stream.read(new byte[5], 0, 5));
			System.out.println("Available: " + stream.available());
		}
		finally {
			stream.close();
		}
	}
	
	
	private static void testDoubleCountingDelegatingLocal() throws IOException {
		info.bioinfweb.jphyloio.test.tests.boundedstream.BoundedInputStream stream = 
				new info.bioinfweb.jphyloio.test.tests.boundedstream.BoundedInputStream(new DelegatingInputStream(new FileInputStream(
						new File("data/NeXML/treebase-record.xml"))));
		try {
			System.out.println("Available: " + stream.available());
			System.out.println("Skipped: " + stream.skip(5));  // What if this would call read() internally?
			System.out.println("Available: " + stream.available());
			System.out.println("read: " + stream.read(new byte[5]));
			System.out.println("Available: " + stream.available());
			System.out.println("read: " + stream.read(new byte[5], 0, 5));
			System.out.println("Available: " + stream.available());
			
			// => These are decorators, not subclasses. Therefore delegation problems cannot occur! (error in reasoning)
		}
		finally {
			stream.close();
		}
	}
	
	
	public static void main(String[] args) throws IOException {
		testDoubleCountingDelegatingLocal();
	}
}
