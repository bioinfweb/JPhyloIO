/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015-2016  Ben Stöver, Sarah Wiechers
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
package info.bioinfweb.jphyloio.demo.metadata;


import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import info.bioinfweb.commons.appversion.ApplicationType;
import info.bioinfweb.commons.appversion.ApplicationVersion;
import info.bioinfweb.jphyloio.JPhyloIOEventReader;
import info.bioinfweb.jphyloio.JPhyloIOEventWriter;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.ReadWriteParameterNames;
import info.bioinfweb.jphyloio.dataadapters.implementations.ListBasedDocumentDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.implementations.store.StoreTreeNetworkGroupDataAdapter;
import info.bioinfweb.jphyloio.demo.metadata.adapters.MetadataTreeNetworkDataAdapterImpl;
import info.bioinfweb.jphyloio.events.LinkedLabeledIDEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;



public class MetadataApplication extends info.bioinfweb.jphyloio.demo.tree.Application {
	private JFormattedTextField supportTextField;
	private JTextField genusTextField;
	private JTextField speciesTextField;
	
	private NodeData selection = null;

	
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
					MetadataApplication window = new MetadataApplication();
					window.mainFrame.setVisible(true);
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	
	@Override
	public String getName() {
		return "JPhyloIO metadata demo application";
	}


	@Override
	public ApplicationVersion getVersion() {
		return new ApplicationVersion(1, 0, 0, 1368, ApplicationType.BETA);
	}
	

	@Override
	public String getApplicationURL() {
		return "http://r.bioinfweb.info/JPhyloIODemoMetadata";
	}


	/**
	 * This method is overwritten in order to use the tree reader implemented in this project.
	 */
	@Override
	protected void readTree(String formatID, File file) throws Exception {
		ReadWriteParameterMap parameters = new ReadWriteParameterMap();
		parameters.put(ReadWriteParameterNames.KEY_USE_OTU_LABEL, true);  // Use OTU labels as node labels if no node label is present.
		
		JPhyloIOEventReader eventReader = factory.getReader(formatID, file, parameters);  // Create JPhyloIO reader instance for the determined format.
		try {
			new MetadataTreeReader().read(eventReader, getTreeModel());  // Read tree into the data model of this application.
		}
		finally {
			eventReader.close();
		}
	}


	/**
	 * This method is overwritten in order to use the tree reader implemented in this project.
	 */
	@Override
	protected void writeTree(String formatID, File file) {
		// Create data adapters:
		ListBasedDocumentDataAdapter document = new ListBasedDocumentDataAdapter();
		StoreTreeNetworkGroupDataAdapter treeGroup = new StoreTreeNetworkGroupDataAdapter(
				new LinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, "treeGroup", null, null), null);
		document.getTreeNetworkGroups().add(treeGroup);
		treeGroup.getTreesAndNetworks().add(new MetadataTreeNetworkDataAdapterImpl(getTreeModel()));
		
		// Define writer parameters:
		ReadWriteParameterMap parameters = new ReadWriteParameterMap();
		parameters.put(ReadWriteParameterNames.KEY_APPLICATION_NAME, getName());
		parameters.put(ReadWriteParameterNames.KEY_APPLICATION_VERSION, getVersion());
		parameters.put(ReadWriteParameterNames.KEY_APPLICATION_URL, getApplicationURL());
		
		// Write document:
		JPhyloIOEventWriter writer = factory.getWriter(formatID);
		try {
			writer.writeDocument(document, file, parameters);
		}
		catch (IOException ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(mainFrame, "The error \"" + ex.getLocalizedMessage() + "\" occurred.", 
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	
	private void saveMetadataPanel() {
		if (selection != null) {
			if (!supportTextField.getText().isEmpty()) {
				selection.setSupport(Double.parseDouble(supportTextField.getText()));
			}
			selection.getTaxonomy().setGenus(genusTextField.getText());
			selection.getTaxonomy().setSpecies(speciesTextField.getText());
		}
	}
	
	
	@Override
	protected void initialize() {
		super.initialize();
		
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				saveMetadataPanel();

				boolean nodeSelected = (e.getNewLeadSelectionPath() != null);
				supportTextField.setEnabled(nodeSelected);
				genusTextField.setEnabled(nodeSelected);
				speciesTextField.setEnabled(nodeSelected);
				
				if (nodeSelected) {
					selection = (NodeData)((DefaultMutableTreeNode)e.getNewLeadSelectionPath().getLastPathComponent()).getUserObject();
					if (Double.isNaN(selection.getSupport())) {
						supportTextField.setText("");
					}
					else {
						supportTextField.setText(Double.toString(selection.getSupport()));
					}
					genusTextField.setText(selection.getTaxonomy().getGenus());
					speciesTextField.setText(selection.getTaxonomy().getSpecies());
				}
				else {
					selection = null;
					supportTextField.setText("");
					genusTextField.setText("");
					speciesTextField.setText("");
				}
			}
		});
		mainFrame.getContentPane().add(createMetadataPanel(), BorderLayout.EAST);
	}


	private JPanel createMetadataPanel() {
		JPanel metadataPanel = new JPanel();
		GridBagLayout gbl_metadataPanel = new GridBagLayout();
		gbl_metadataPanel.columnWidths = new int[]{0, 0, 0};
		gbl_metadataPanel.rowHeights = new int[]{0, 0, 0, 0};
		gbl_metadataPanel.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gbl_metadataPanel.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		metadataPanel.setLayout(gbl_metadataPanel);
		
		JLabel supportLabel = new JLabel("Support:");
		GridBagConstraints gbc_supportLabel = new GridBagConstraints();
		gbc_supportLabel.anchor = GridBagConstraints.WEST;
		gbc_supportLabel.insets = new Insets(2, 3, 5, 5);
		gbc_supportLabel.gridx = 0;
		gbc_supportLabel.gridy = 0;
		metadataPanel.add(supportLabel, gbc_supportLabel);
		
		supportTextField = new JFormattedTextField(NumberFormat.getNumberInstance(Locale.US));
		GridBagConstraints gbc_suuportTextField = new GridBagConstraints();
		gbc_suuportTextField.insets = new Insets(2, 3, 5, 3);
		gbc_suuportTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_suuportTextField.gridx = 1;
		gbc_suuportTextField.gridy = 0;
		metadataPanel.add(supportTextField, gbc_suuportTextField);
		supportTextField.setColumns(10);
		
		JLabel genusLabel = new JLabel("Genus:");
		GridBagConstraints gbc_genusLabel = new GridBagConstraints();
		gbc_genusLabel.anchor = GridBagConstraints.WEST;
		gbc_genusLabel.insets = new Insets(2, 3, 5, 5);
		gbc_genusLabel.gridx = 0;
		gbc_genusLabel.gridy = 1;
		metadataPanel.add(genusLabel, gbc_genusLabel);
		
		genusTextField = new JTextField();
		GridBagConstraints gbc_genusTextField = new GridBagConstraints();
		gbc_genusTextField.insets = new Insets(2, 3, 5, 3);
		gbc_genusTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_genusTextField.gridx = 1;
		gbc_genusTextField.gridy = 1;
		metadataPanel.add(genusTextField, gbc_genusTextField);
		genusTextField.setColumns(10);
		
		JLabel speciesLabel = new JLabel("Species:");
		GridBagConstraints gbc_speciesLabel = new GridBagConstraints();
		gbc_speciesLabel.anchor = GridBagConstraints.EAST;
		gbc_speciesLabel.insets = new Insets(2, 3, 2, 5);
		gbc_speciesLabel.gridx = 0;
		gbc_speciesLabel.gridy = 2;
		metadataPanel.add(speciesLabel, gbc_speciesLabel);
		
		speciesTextField = new JTextField();
		GridBagConstraints gbc_speciesTextField = new GridBagConstraints();
		gbc_speciesTextField.insets = new Insets(2, 3, 2, 3);
		gbc_speciesTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_speciesTextField.gridx = 1;
		gbc_speciesTextField.gridy = 2;
		metadataPanel.add(speciesTextField, gbc_speciesTextField);
		speciesTextField.setColumns(10);
		return metadataPanel;
	}
}
