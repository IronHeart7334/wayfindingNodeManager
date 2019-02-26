package nodemanager.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import nodemanager.Mode;
import nodemanager.Session;
import nodemanager.events.*;
import nodemanager.node.Node;

/**
 * @author Matt Crow (greengrappler12@gmail.com)
 */

 /** 
 * NodeDataPane is used to display information on the currently selected Node 
 */
public class NodeDataPane extends JComponent{
    private Node selectedNode;
    private boolean hasNodeSelected;
    private final JTextArea nodeInfo;
    private GridBagConstraints gbc;
    
    public NodeDataPane(){
        setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        selectedNode = null;
        hasNodeSelected = false;
        
        nodeInfo = new JTextArea("No node selected");
        nodeInfo.setBackground(Color.red);
        nodeInfo.setEditable(false);
        add(nodeInfo, gbc);
        
        addOption("Delete this node", () ->{
            if(selectedNode.id < 0){
                //prevent user from deleting corner nodes
                JOptionPane.showMessageDialog(null, "Cannot delete node with id of " + selectedNode.id);
            } else if(JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this node? This will not alter the original spreadsheet.") == JOptionPane.YES_OPTION){
                Session.logAction(new NodeDeleteEvent(selectedNode, selectedNode.getIcon().getHost()));
                selectedNode.getIcon().getHost().removeNode(selectedNode);
                Node.removeNode(selectedNode.id);
                Session.selectNode(Node.get(-1));
            }
        });
        
        addOption("Move this node", () -> {
            if(selectedNode.id < 0){
                JOptionPane.showMessageDialog(null, "Cannot move node with id of " + selectedNode.id);
            } else {
                Session.setMode(Mode.MOVE);
                Session.logAction(new NodeMovedEvent(selectedNode, selectedNode.getIcon().getX(), selectedNode.getIcon().getY()));
            }
        });
        
        //resets the NodeIcon to its position when it was initially imported
        addOption("Reset position", () -> {
            Session.logAction(new NodeMovedEvent(selectedNode, selectedNode.getIcon().getX(), selectedNode.getIcon().getY()));
            selectedNode.getIcon().resetPos();
        });
        
        addOption("Add a connection", () -> {
            JOptionPane.showMessageDialog(null, "Click on a node to connect it to node " + selectedNode.id);
            Session.setMode(Mode.ADD_CONNECTION);
        });
        
        addOption("Remove a connection", () -> {
            JOptionPane.showMessageDialog(null, "Click on a node to disconnect it from node " + selectedNode.id);
            Session.setMode(Mode.REMOVE_CONNECTION);
        });
        
        addOption("Add a label", () -> {
            String ip = JOptionPane.showInputDialog("Enter the label to add to this node: ");
            if(selectedNode.addLabel(ip)){
                Session.logAction(new LabelAddedEvent(selectedNode, ip));
                selectNode(selectedNode); //reload node description
            } else {
                JOptionPane.showMessageDialog(null, "Label '" + ip + "' is already in use.");
            }
        });
        
        addOption("Remove a label", () -> {
            String ip = JOptionPane.showInputDialog("Enter the label to remove from this node: ");
            if(selectedNode.removeLabel(ip)){
                Session.logAction(new LabelRemovedEvent(selectedNode, ip));
                selectNode(selectedNode); //reload node description
            }
        });
        
        setVisible(true);
    }
    
    /**
     * Adds a button to the data pane
     * @param title what the button will display
     * @param action the runnable to run when the button is clicked, if a node is selected, 
     * otherwise it does nothing
     */
    private void addOption(String title, Runnable action){
        JButton j = new JButton(title);
        j.addActionListener((ActionEvent e) -> {
            if(hasNodeSelected){
                action.run();
            }
        });
        add(j, gbc);
    }
    
    
    /**
     * Creates a NodeDataPane with a specific node selected
     * @param n the Node to select
     */
    public NodeDataPane(Node n){
        this();
        selectNode(n);
    }
    
    /**
     * Changes the information displayed to that of a given node
     * @param n the Node this should display the data for
     */
    public void selectNode(Node n){
        if(hasNodeSelected){
            selectedNode.getIcon().setDrawLinks(false);
        }
        hasNodeSelected = true;
        selectedNode = n;
        nodeInfo.setText(n.getDesc());
        n.getIcon().setDrawLinks(true);
        n.getIcon().getHost().repaint();
    }
    
    /**
     * returns the Node this is displaying the data for
     * @return the selected Node
     */
    public Node getSelected(){
        return selectedNode;
    }
}
