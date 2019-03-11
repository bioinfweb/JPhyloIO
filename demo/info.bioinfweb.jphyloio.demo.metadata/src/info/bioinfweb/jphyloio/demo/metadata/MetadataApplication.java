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
package info.bioinfweb.jphyloio.demo.metadata;


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
import info.bioinfweb.jphyloio.formats.phyloxml.PhyloXMLMetadataTreatment;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Locale;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;



public class MetadataApplication extends info.bioinfweb.jphyloio.demo.tree.Application {
	private JFormattedTextField supportTextField;
	private JTextField genusTextField;
	private JTextField speciesTextField;
	
	private NodeData selection = null;
	private JPanel listPanel;
	private JFormattedTextField sizeTextField;
	private JList<Double> sizeList;

	
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
		parameters.put(ReadWriteParameterNames.KEY_USE_OTU_LABEL, true);
				// Use OTU labels as node labels if no node label is present. (This is only relevant for NeXML and Nexus.)
		parameters.put(ReadWriteParameterNames.KEY_PHYLOXML_CONSIDER_PHYLOGENY_AS_TREE, true);
				// This parameter defines if cross links between nodes (defined by the clade_relation tag of PhyloXML) should be
				// modeled as metadata attached to a node or if the whole phylogeny shall be interpreted as a phylogenetic network.
				// Since the network interpretation is the default, we need to set this parameter in order to receive tree events
				// and not network events.
		
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
		saveMetadataPanel();  // The last edit might not have been saved, if the selected node did not change since then.
		
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
		parameters.put(ReadWriteParameterNames.KEY_PHYLOXML_METADATA_TREATMENT, PhyloXMLMetadataTreatment.LEAVES_ONLY);
		
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
			selection.getTaxonomy().setScientificName(genusTextField.getText());
			selection.getTaxonomy().setNCBIID(speciesTextField.getText());
			selection.setSizeMeasurements(Collections.list(getSizeListModel().elements()));
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
					genusTextField.setText(selection.getTaxonomy().getScientificName());
					speciesTextField.setText(selection.getTaxonomy().getNCBIID());
					
					getSizeListModel().clear();
					for (Double size : selection.getSizeMeasurements()) {
						getSizeListModel().addElement(size);
					}
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


	/**
	 * @wbp.parser.entryPoint
	 */
	private JPanel createMetadataPanel() {
		JPanel metadataPanel = new JPanel();
		GridBagLayout gbl_metadataPanel = new GridBagLayout();
		gbl_metadataPanel.columnWidths = new int[] {0, 0};
		gbl_metadataPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
		gbl_metadataPanel.columnWeights = new double[]{0.0, 1.0};
		gbl_metadataPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		metadataPanel.setLayout(gbl_metadataPanel);
		
		JLabel lblEdgeAnnotations = new JLabel("Edge annotations:");
		lblEdgeAnnotations.setFont(new Font("Tahoma", Font.BOLD, 13));
		GridBagConstraints gbc_lblEdgeAnnotations = new GridBagConstraints();
		gbc_lblEdgeAnnotations.anchor = GridBagConstraints.WEST;
		gbc_lblEdgeAnnotations.insets = new Insets(0, 2, 5, 5);
		gbc_lblEdgeAnnotations.gridx = 0;
		gbc_lblEdgeAnnotations.gridy = 0;
		metadataPanel.add(lblEdgeAnnotations, gbc_lblEdgeAnnotations);
		
		JLabel supportLabel = new JLabel("Support:");
		GridBagConstraints gbc_supportLabel = new GridBagConstraints();
		gbc_supportLabel.anchor = GridBagConstraints.WEST;
		gbc_supportLabel.insets = new Insets(2, 3, 5, 5);
		gbc_supportLabel.gridx = 0;
		gbc_supportLabel.gridy = 1;
		metadataPanel.add(supportLabel, gbc_supportLabel);
		
		supportTextField = new JFormattedTextField(NumberFormat.getNumberInstance(Locale.US));
		GridBagConstraints gbc_suuportTextField = new GridBagConstraints();
		gbc_suuportTextField.weightx = 1.0;
		gbc_suuportTextField.insets = new Insets(2, 3, 5, 5);
		gbc_suuportTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_suuportTextField.gridx = 1;
		gbc_suuportTextField.gridy = 1;
		metadataPanel.add(supportTextField, gbc_suuportTextField);
		supportTextField.setColumns(10);
		
		JLabel lblNodeAnnotations = new JLabel("Node annotations:");
		lblNodeAnnotations.setFont(new Font("Tahoma", Font.BOLD, 13));
		GridBagConstraints gbc_lblNodeAnnotations = new GridBagConstraints();
		gbc_lblNodeAnnotations.anchor = GridBagConstraints.WEST;
		gbc_lblNodeAnnotations.insets = new Insets(20, 2, 5, 5);
		gbc_lblNodeAnnotations.gridx = 0;
		gbc_lblNodeAnnotations.gridy = 2;
		metadataPanel.add(lblNodeAnnotations, gbc_lblNodeAnnotations);
		
		JLabel genusLabel = new JLabel("Scientific name:");
		GridBagConstraints gbc_genusLabel = new GridBagConstraints();
		gbc_genusLabel.anchor = GridBagConstraints.WEST;
		gbc_genusLabel.insets = new Insets(2, 3, 5, 5);
		gbc_genusLabel.gridx = 0;
		gbc_genusLabel.gridy = 3;
		metadataPanel.add(genusLabel, gbc_genusLabel);
		
		genusTextField = new JTextField();
		GridBagConstraints gbc_genusTextField = new GridBagConstraints();
		gbc_genusTextField.weightx = 1.0;
		gbc_genusTextField.insets = new Insets(2, 3, 5, 5);
		gbc_genusTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_genusTextField.gridx = 1;
		gbc_genusTextField.gridy = 3;
		metadataPanel.add(genusTextField, gbc_genusTextField);
		genusTextField.setColumns(10);
		
		JLabel speciesLabel = new JLabel("NCBI taxonomy ID:");
		GridBagConstraints gbc_speciesLabel = new GridBagConstraints();
		gbc_speciesLabel.anchor = GridBagConstraints.WEST;
		gbc_speciesLabel.insets = new Insets(2, 3, 5, 5);
		gbc_speciesLabel.gridx = 0;
		gbc_speciesLabel.gridy = 4;
		metadataPanel.add(speciesLabel, gbc_speciesLabel);
		
		speciesTextField = new JTextField();
		GridBagConstraints gbc_speciesTextField = new GridBagConstraints();
		gbc_speciesTextField.weightx = 1.0;
		gbc_speciesTextField.insets = new Insets(2, 3, 5, 5);
		gbc_speciesTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_speciesTextField.gridx = 1;
		gbc_speciesTextField.gridy = 4;
		metadataPanel.add(speciesTextField, gbc_speciesTextField);
		speciesTextField.setColumns(10);
		
		GridBagConstraints gbc_listPanel = new GridBagConstraints();
		gbc_listPanel.gridwidth = 2;
		gbc_listPanel.insets = new Insets(10, 0, 0, 5);
		gbc_listPanel.fill = GridBagConstraints.BOTH;
		gbc_listPanel.gridx = 0;
		gbc_listPanel.gridy = 5;
		metadataPanel.add(getListPanel(), gbc_listPanel);
		
		return metadataPanel;
	}
	
	
	/**
	 * This methods creates the GUI element to edit a list of body size measures. Such a list is attached to tree nodes as one
	 * type of example metadata. The GUI implementations in this method are not relevant for this example application to 
	 * understand how metadata is processed in <i>JPhyloIO</i>. 
	 * 
	 * @return a panel with an editable list of body size measures
	 */
	protected JPanel getListPanel() {
		if (listPanel == null) {
			listPanel = new JPanel();
			GridBagLayout gbl_listPanel = new GridBagLayout();
			gbl_listPanel.columnWidths = new int[]{0, 0};
			gbl_listPanel.rowHeights = new int[]{0, 0, 0, 0};
			gbl_listPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
			gbl_listPanel.rowWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
			listPanel.setLayout(gbl_listPanel);
			
			JLabel lblBodySizeMeasurements = new JLabel("Body size measurements [m]:");
			GridBagConstraints gbc_lblBodySizeMeasurements = new GridBagConstraints();
			gbc_lblBodySizeMeasurements.weightx = 1.0;
			gbc_lblBodySizeMeasurements.anchor = GridBagConstraints.WEST;
			gbc_lblBodySizeMeasurements.insets = new Insets(2, 3, 5, 5);
			gbc_lblBodySizeMeasurements.gridx = 0;
			gbc_lblBodySizeMeasurements.gridy = 0;
			listPanel.add(lblBodySizeMeasurements, gbc_lblBodySizeMeasurements);
			
			NumberFormat sizeFormat = NumberFormat.getNumberInstance(Locale.US);
			sizeFormat.setMaximumFractionDigits(10);
			sizeTextField = new JFormattedTextField(sizeFormat);
			GridBagConstraints gbc_sizeTextField = new GridBagConstraints();
			gbc_sizeTextField.weightx = 1.0;
			gbc_sizeTextField.insets = new Insets(0, 2, 5, 5);
			gbc_sizeTextField.fill = GridBagConstraints.HORIZONTAL;
			gbc_sizeTextField.gridx = 0;
			gbc_sizeTextField.gridy = 1;
			listPanel.add(sizeTextField, gbc_sizeTextField);
			
			JButton addButton = new JButton("Add");
			addButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (!sizeTextField.getText().isEmpty()) {
						getSizeListModel().addElement(Double.parseDouble(sizeTextField.getText()));  // Add a new element to the list.
					}
				}
			});
			GridBagConstraints gbc_addButton = new GridBagConstraints();
			gbc_addButton.fill = GridBagConstraints.HORIZONTAL;
			gbc_addButton.insets = new Insets(0, 0, 5, 0);
			gbc_addButton.gridx = 1;
			gbc_addButton.gridy = 1;
			listPanel.add(addButton, gbc_addButton);
			
			JButton removeButton = new JButton("Remove");
			removeButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (getSizeList().getSelectedIndex() >= 0) {
						getSizeListModel().remove(getSizeList().getSelectedIndex());  // Remove the currently selected element from the list.
					}
				}
			});
			
			JScrollPane sizeListScrollPane = new JScrollPane(getSizeList());
			GridBagConstraints gbc_sizeListScrollPane = new GridBagConstraints();
			gbc_sizeListScrollPane.insets = new Insets(3, 2, 3, 5);
			gbc_sizeListScrollPane.fill = GridBagConstraints.BOTH;
			gbc_sizeListScrollPane.gridx = 0;
			gbc_sizeListScrollPane.gridy = 2;
			listPanel.add(sizeListScrollPane, gbc_sizeListScrollPane);
			GridBagConstraints gbc_removeButton = new GridBagConstraints();
			gbc_removeButton.fill = GridBagConstraints.HORIZONTAL;
			gbc_removeButton.anchor = GridBagConstraints.NORTH;
			gbc_removeButton.gridx = 1;
			gbc_removeButton.gridy = 2;
			listPanel.add(removeButton, gbc_removeButton);
		}
		return listPanel;
	}
	
	
	protected JList<Double> getSizeList() {
		if (sizeList == null) {
			sizeList = new JList<Double>(new DefaultListModel<Double>());
			sizeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		}
		return sizeList;
	}
	
	
	protected DefaultListModel<Double> getSizeListModel() {
		return (DefaultListModel<Double>)getSizeList().getModel();
	}
}
