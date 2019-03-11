/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nodemanager.gui;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.*;
import nodemanager.io.VersionLog;
import nodemanager.io.WayfindingManifest;

/**
 * Acts as the body of the import dialog whenever the user clicks the import from drive button.
 * Allows the user to specify which version of wayfinding they are importing from, as well as what to name the export.
 * 
 * TODO: combine this and ExportBody
 * @author Matt Crow
 */
public class ImportBody extends Container{
    private JComboBox<String> version;
    private JComboBox<String> exportSelector;
    private String[] exportIds;
    private String[] exportNames;
    
    private VersionLog v;
    
    private final JButton importButton;
    private final JTextArea msg;
    
    public ImportBody(){
        super();
        
        v = new VersionLog();
        v.download();
        
        
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        version = new JComboBox<>(v.getTypes());
        version.addItemListener((e)->{
            updateExportSelector();
        });
        add(version, gbc);
        
        gbc.gridx = 1;
        exportSelector = new JComboBox<>();
        
        
        add(exportSelector, gbc);
        
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        msg = new JTextArea();
        msg.setEditable(false);
        add(msg, gbc);
        
        gbc.gridx = 1;
        importButton = new JButton("Import");
        importButton.addActionListener((e)->{
            try{
                Class.forName("com.google.api.client.http.HttpTransport"); 
                //will throw an error if don't have google drive API
            } catch(ClassNotFoundException ex){
                ex.printStackTrace();
                msg.setText(
                        "Looks like you forgot to include the lib folder \n"
                      + "1. Download lib.zip from the node manager Google drive \n"
                      + "2. Extract the contents so that the lib folder is in the same folder as NodeManager.jar \n"
                      + "3. Click export again"
                );
                return;
            }
            
            msg.setText("Beginning download...");
            WayfindingManifest.importManifest(exportIds[exportSelector.getSelectedIndex()]);
            msg.setText("Done!");
        });
        add(importButton, gbc);
        updateExportSelector();
    }
    
    private void updateExportSelector(){
        exportIds = v.getExportIdsFor(version.getSelectedItem().toString());
        exportNames = v.getExportNamesFor(version.getSelectedItem().toString());
        exportSelector.removeAllItems();
        for(int i = exportNames.length -1; i >= 0; i--){
            exportSelector.addItem(exportNames[i]);
        }
    }
}
