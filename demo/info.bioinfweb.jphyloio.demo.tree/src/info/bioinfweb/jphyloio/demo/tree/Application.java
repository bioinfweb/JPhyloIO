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
package info.bioinfweb.jphyloio.demo.tree;


import info.bioinfweb.commons.io.ContentExtensionFileFilter;
import info.bioinfweb.commons.io.ExtensionFileFilter;
import info.bioinfweb.jphyloio.JPhyloIOEventReader;
import info.bioinfweb.jphyloio.JPhyloIOFormatSpecificObject;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.ReadWriteParameterNames;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.factory.JPhyloIOContentExtensionFileFilter;
import info.bioinfweb.jphyloio.factory.JPhyloIOReaderWriterFactory;
import info.bioinfweb.jphyloio.formats.JPhyloIOFormatInfo;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URI;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultTreeModel;

import org.apache.commons.collections4.set.ListOrderedSet;



public class Application {
	private JFrame frame;
	private JTree tree;
	private JFileChooser fileChooser;
	
	private JPhyloIOReaderWriterFactory factory = new JPhyloIOReaderWriterFactory();

	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Application window = new Application();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	
	/**
	 * Create the application.
	 */
	public Application() {
		initialize();
	}
	
	
	/**
	 * Returns a file chooser with file filters for all tree formats supported by <i>JPhyloIO</i> and a filter filter
	 * accepting valid extensions of all tree formats.
	 * <p>
	 * The goal of this dialog is on the one hand to filter all supported tree files using the "All supported formats" 
	 * filter. {@link JPhyloIOReaderWriterFactory#guessReader(File, ReadWriteParameterMap)} will be used to determine
	 * an appropriate reader for files selected this way later. On the other hand, single filters for all supported
	 * formats are offered, so the user can manually define the format of a file. In that case the format will not be
	 * guessed but directly determined using {@link JPhyloIOContentExtensionFileFilter#getFormatID()}.
	 * 
	 * @return the file chooser instance for opening files in this application
	 */
	private JFileChooser getFileChooser() {
		if (fileChooser == null) {  // if fileChooser was not initialized yet
			// Create file chooser:
			fileChooser = new JFileChooser();
			fileChooser.setMultiSelectionEnabled(false);  // Do not allow to select more than one file.
			fileChooser.setAcceptAllFileFilterUsed(false);  // Do not include predefined "All files (*.*)" filter, since we will create a special instance later.
			
			// Add file filters for supported formats and collect extensions for "All supported formats" filter:
			ListOrderedSet<String> validExtensions = new ListOrderedSet<String>();  // This set is used to collect valid extensions of all formats to create the "All supported formats" filter later.
			for (String formatID : factory.getFormatIDsSet()) {
				JPhyloIOFormatInfo info = factory.getFormatInfo(formatID);
				if (info.isElementModeled(EventContentType.TREE, true)) {  // Check if the current format can contain trees.
					ContentExtensionFileFilter filter = info.createFileFilter();  // Create a filter filter instance for the current format.
					validExtensions.addAll(filter.getExtensions());  // Add the file extensions of this filter to the set of all supported extensions.
					fileChooser.addChoosableFileFilter(info.createFileFilter());  // Add the current filter to the file chooser.
				}
			}
			
			// Add "All supported formats" filter:
			ExtensionFileFilter allFormatsFilter = new ExtensionFileFilter("All supported formats", false, validExtensions.asList());
					// Create a file filter accepting extensions of all supported formats at the same time. 
			fileChooser.addChoosableFileFilter(allFormatsFilter);  // Add the "All supported formats" filter to the list.
			fileChooser.setFileFilter(allFormatsFilter);  // Select this filter as the default.
		}
		return fileChooser;
	}
	
	
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		tree = new JTree(new DefaultTreeModel(null));
		frame.getContentPane().add(tree, BorderLayout.CENTER);
		
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem mntmOpen = new JMenuItem("Open...");
		mntmOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if (getFileChooser().showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
						String formatID;
						if (getFileChooser().getFileFilter() instanceof JPhyloIOFormatSpecificObject) {
							formatID = ((JPhyloIOFormatSpecificObject)getFileChooser().getFileFilter()).getFormatID();  // Use the user defined format.
						}
						else {  // In this case the "All supported formats" filter was used and the format needs to be guessed.
							formatID = factory.guessFormat(getFileChooser().getSelectedFile());  // Guess the format, since the user did not explicitly specify one.
						}
						
						if (formatID != null) {
							ReadWriteParameterMap parameters = new ReadWriteParameterMap();
							parameters.put(ReadWriteParameterNames.KEY_NEXML_USE_OTU_LABEL, true);  // Use OTU labels as node labels if no node label is present.
							
							JPhyloIOEventReader eventReader = factory.getReader(formatID, getFileChooser().getSelectedFile(), parameters);  // Create JPhyloIO reader instance for the determined format.
							try {
								new TreeReader().read(eventReader, getTreeModel());  // Read tree into the business model of this application.
							}
							finally {
								eventReader.close();
							}
						}
						else {  // If the format had to be guessed and none was found.
							JOptionPane.showMessageDialog(frame, "The format of the file \"" + getFileChooser().getSelectedFile() + "\" is not supported.", 
									"Unsupported format", JOptionPane.ERROR_MESSAGE);
						}
					}
				}
				catch (Exception ex) {  // If an error occurred while trying to load a tree.
					ex.printStackTrace();
					JOptionPane.showMessageDialog(frame, "The error \"" + ex.getLocalizedMessage() + "\" occurred.", 
							"Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		mnFile.add(mntmOpen);
		
		JMenuItem mntmSaveAs = new JMenuItem("Save as...");
		mnFile.add(mntmSaveAs);
		
		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.setVisible(false);
				System.exit(0);
			}
		});
		mnFile.add(mntmExit);
		
		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);
		
		JMenuItem mntmAbout = new JMenuItem("About");
		mntmAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					Desktop.getDesktop().browse(new URI("http://bioinfweb.info/JPhyloIO/Documentation/Demos/Tree"));  //TODO Replace by r.bioinfweb.info
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		mnHelp.add(mntmAbout);
	}
	
	
	private JTree getTree() {
		return tree;
	}
	
	
	private DefaultTreeModel getTreeModel() {
		return (DefaultTreeModel)getTree().getModel();
	}
}
