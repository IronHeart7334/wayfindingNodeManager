package nodemanager.gui;

import nodemanager.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import static java.lang.System.out;
import javax.imageio.ImageIO;
import nodemanager.node.*;

/*
EditCanvas is the main JPanel used by the program
*/

public class EditCanvas extends JPanel{
    private final MenuBar menu;
    private final Pane body;
    private final Sidebar sideBar;
    private final JButton chooseNodeSourceButton;
    private final JButton chooseNodeConnButton;
    private final JButton chooseMapButton;
    private final JButton addNodeButton;
    private final JButton exportNodeData;
    private final JButton saveMap;
    
    private final NodeDataPane selectedNode;
    private MapImage map;
    
    public EditCanvas(){
        super();
        
        GridBagLayout lo = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(lo);
        
        menu = new MenuBar();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1;
        c.fill = GridBagConstraints.BOTH;
        add(menu, c);
        
        body = new Pane();
        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 9;
        c.weighty = 9;
        c.fill = GridBagConstraints.BOTH;
        add(body, c);
        
        sideBar = new Sidebar();
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.weightx = 1;
        c.weighty = 9;
        c.fill = GridBagConstraints.BOTH;
        add(sideBar, c);
        
        selectedNode = new NodeDataPane();
        sideBar.add(selectedNode);
        Session.dataPane = selectedNode;
        
        //don't set a layout for the body, causes issues
        body.setLayout(new GridLayout(1, 1));
        map = new MapImage();
        body.add(map);
        
        chooseNodeSourceButton = createSelector(
                "node file", 
                new String[]{"Comma Separated Values", "csv"},
                new FileSelectedListener(){
                    @Override
                    public void run(File f){
                        try {
                            loadNodesFromFile(new FileInputStream(f));
                        } catch (FileNotFoundException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
        );
        menu.add(chooseNodeSourceButton);
        
        chooseNodeConnButton = createSelector(
                "connection file",
                new String[]{"Comma Separated Values", "csv"},
                new FileSelectedListener(){
                    @Override
                    public void run(File f){
                        try {
                            loadConn(new FileInputStream(f));
                        } catch (FileNotFoundException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
        );
        menu.add(chooseNodeConnButton);
        
        chooseMapButton = createSelector(
                "map Image", 
                new String[]{"Image file", "JPEG file", "jpg", "jpeg", "png"},
                new FileSelectedListener(){
                    @Override
                    public void run(File f){
                        try {
                            map.setImage(ImageIO.read(f));
                            JOptionPane.showMessageDialog(null, "Click on a point on the new map to set the new upper-left corner");
                            Session.mode = Mode.RESCALE_UL;
                            repaint();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
        );
        menu.add(chooseMapButton);
        
        addNodeButton = new JButton("Add a new Node");
        addNodeButton.addActionListener(new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e){
                Session.mode = Mode.ADD;
                JOptionPane.showMessageDialog(null, "Click on any location on the map to add a new node");
            }
        });
        menu.add(addNodeButton);
        
        exportNodeData = createExportButton();
        
        saveMap = new JButton("Export map");
        saveMap.addActionListener(new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e){
                map.saveImage();
            }
        });
        menu.add(saveMap);
        
        setBackground(Color.blue);
        
        
        
        //set defaults
        try{
            map.setImage(ImageIO.read(getClass().getResourceAsStream("/map.png")));
        } catch(Exception e){
            e.printStackTrace();
        }
        
        try{
            loadNodesFromFile(getClass().getResourceAsStream("/nodeData.csv"));
        } catch(Exception e){
            e.printStackTrace();
        }
        
        try{
           loadConn(getClass().getResourceAsStream("/nodeConnections.csv"));
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    private JButton createSelector(String type, String[] types, FileSelectedListener l){
        JButton ret = new JButton("Select " + type);
        
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setFileFilter(new FileNameExtensionFilter(type, types));
        
        ret.addActionListener(new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e){
                int response = chooser.showOpenDialog(chooser);
                if(response == JFileChooser.APPROVE_OPTION){
                    l.run(chooser.getSelectedFile());
                }
            }
        });
        
        return ret;
    }
    
    private JButton createExportButton(){
        JButton j = new JButton("Export data");
        j.addActionListener(new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent ae) {
                JFileChooser destination = new JFileChooser();
                destination.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int response = destination.showOpenDialog(destination);
                if(response == JFileChooser.APPROVE_OPTION){
                    File f = destination.getSelectedFile();
                    Node.generateDataAt(f.getAbsolutePath());
                }
            }
        });
        menu.add(j);
        
        return j;
    }
    
    private void loadNodesFromFile(InputStream i){
        map.removeAllNodes();
        Node.removeAll();
        
        NodeParser.parseNodeFile(i);
        map.scaleTo(Node.get(-1).rawX, Node.get(-1).rawY, Node.get(-2).rawX, Node.get(-2).rawY);
        
        for(Node n : Node.getAll()){
            map.addNode(n);
        }
        revalidate();
        repaint();
    }
    
    private void loadConn(InputStream i){
        NodeParser.parseConnFile(i);
        Node.initAll();
    }
    
    private abstract class FileSelectedListener{
        public abstract void run(File f);
    }
}