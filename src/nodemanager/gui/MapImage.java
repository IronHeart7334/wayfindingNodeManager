package nodemanager.gui;

import java.awt.*;
import java.io.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.event.*;
import static java.lang.System.out;
import java.util.*;
import nodemanager.node.Node;
import nodemanager.*;

/*
MapImage is used to render an image,
as well as proved a scale to resize points drawn onto it.

Upon creating a MapImage, you need to call setImage on a file,
then scaleTo a set of coordinates
*/

public class MapImage extends JLabel implements MouseListener, MouseMotionListener, MouseWheelListener{
    private BufferedImage buff;
    private final Scale scaler;
    private final HashMap<Integer, NodeIcon> nodeIcons;
    
    private double zoom;
    
    private int clipX;
    private int clipY;
    private int clipW;
    private int clipH;
    
    private double aspectRatio;
    
    public MapImage(){
        super();
        setVisible(true);
        scaler = new Scale();
        nodeIcons = new HashMap<>();
        
        zoom = 1.0;
        
        clipX = 0;
        clipY = 0;
        clipW = 0;
        clipH = 0;
        
        setBackground(Color.BLACK);
        
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        addComponentListener(new ComponentListener(){
            @Override
            public void componentResized(ComponentEvent ce) {
                resize();
            }
            @Override
            public void componentMoved(ComponentEvent ce) {}
            @Override
            public void componentShown(ComponentEvent ce) {}
            @Override
            public void componentHidden(ComponentEvent ce) {}
        });
    }
    
    public void addNode(Node n){
        NodeIcon ni = n.getIcon();
        ni.scaleTo(scaler);
        nodeIcons.put(n.id, ni);
        
        add(ni);
        revalidate();
        repaint();
    }
    public void removeAllNodes(){
        nodeIcons.clear();
        ArrayList<Component> newComp = new ArrayList<>();
        for(Component c : getComponents()){
            if(!(c instanceof NodeIcon)){
                newComp.add(c);
            }
        }
        removeAll();
        newComp.stream().forEach(c -> add(c));
    }
    
    private void resizeNodeIcons(){
        nodeIcons.values().forEach(n -> n.scaleTo(scaler));
    }
    
    private void pan(int x, int y){
        boolean panX = true;
        boolean panY = true;
        
        if(clipX <= 0 || clipX + clipW >= buff.getWidth()){
            panX = false;
        }
        if(clipY <= 0 || clipY + clipH >= buff.getHeight()){
            panY = false;
        }
        
        if(panX){
            Arrays.asList(this.getComponents()).stream().forEach(c -> c.setLocation(c.getX() - x, c.getY()));
        }
        if(panY){
            Arrays.asList(this.getComponents()).stream().forEach(c -> c.setLocation(c.getX(), c.getY() - y));
        }
        
        clipX += x;
        clipY += y;
        
        if(clipX < 0){
            clipX = 0;
        } else if(clipX + clipW > buff.getWidth()){
            clipX = buff.getWidth() - clipW;
        }
        
        if(clipY < 0){
            clipY = 0;
        } else if(clipY + clipH > buff.getHeight()){
            clipY = buff.getHeight() - clipH;
        }
    }
    
    private void resize(){
        clipW = (getWidth() < buff.getWidth()) ? getWidth() : buff.getWidth();
        clipH = (getHeight() < buff.getHeight()) ? getHeight() : buff.getHeight();
        scaler.setSize(buff.getWidth(), buff.getHeight());
        resizeNodeIcons();
    }
    
    public void setImage(BufferedImage bi){
        buff = bi;
        aspectRatio = 1.0 * buff.getWidth() / buff.getHeight();
        clipX = 0;
        clipY = 0;
        resize();
        revalidate();
        repaint();
    }
    
    public void scaleTo(double x1, double y1, double x2, double y2){
        scaler.rescale(x1, y1, x2, y2);
    }
    
    public void saveImage(){
        JFileChooser cd = new JFileChooser();
        cd.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        try {
            if(cd.showDialog(cd, "Select a location to place the new map file") == JFileChooser.APPROVE_OPTION){
                File f = new File(cd.getSelectedFile().getPath() + File.separator + "mapImage" + System.currentTimeMillis() + ".png");
                ImageIO.write(buff, "png", f);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    @Override
    public void mouseClicked(MouseEvent me) {
        switch(Session.mode){
            case ADD:
                Node n = new Node(scaler.inverseX(me.getX()), scaler.inverseY(me.getY()));
                n.init();
                addNode(n);
                Session.mode = Mode.NONE;
                break;
            case MOVE:
                Session.selectedNode.repos(scaler.inverseX(me.getX()), scaler.inverseY(me.getY()));
                Session.mode = Mode.NONE;
                break;
            case RESCALE_UL:
                Session.mode = Mode.RESCALE_LR;
                Session.newMapX = Node.get(-1).getIcon().getX();
                Session.newMapY = Node.get(-1).getIcon().getY();
                JOptionPane.showMessageDialog(null, "Position the upper left corner of node -2 at the lower right corner of where you want to crop");
                break;
            case RESCALE_LR:
                Session.mode = Mode.NONE;
                Session.newMapWidth = Node.get(-2).getIcon().getX() - Session.newMapX;
                Session.newMapHeight = Node.get(-2).getIcon().getY() - Session.newMapY;
                
                int[] clip = new int[]{Session.newMapX, Session.newMapY, Session.newMapWidth, Session.newMapHeight};
                
                out.print("Clip: ");
                for(int i : clip){
                    out.print(i + " ");
                }
                
                if(clip[0] < 0){
                    clip[0] = 0;
                }
                if(clip[1] < 0){
                    clip[1] = 0;
                }
                if(clip[2] > buff.getWidth() - clip[0]){
                    clip[2] = buff.getWidth() - clip[0];
                }
                if(clip[3] > buff.getHeight() - clip[1]){
                    clip[3] = buff.getHeight() - clip[1];
                }
                
                out.println();
                for(int i : clip){
                    out.print(i + " ");
                }
                
                setImage(buff.getSubimage(clip[0], clip[1], clip[2], clip[3]));
                break;
        }
    }
    
    @Override
    public void mousePressed(MouseEvent me) {}

    @Override
    public void mouseReleased(MouseEvent me) {}

    @Override
    public void mouseEntered(MouseEvent me) {
        if(Session.mode == Mode.ADD){
            //TODO: add node icon that follows mouse?
        }
    }

    @Override
    public void mouseExited(MouseEvent me) {}

    @Override
    public void mouseDragged(MouseEvent me) {}

    @Override
    public void mouseMoved(MouseEvent me) {
        switch(Session.mode){
            case MOVE:
                Session.selectedNode.getIcon().setLocation(me.getX(), me.getY() + 5);
                Session.selectedNode.getIcon().drawAllLinks();
                revalidate();
                repaint();
                break;
            case RESCALE_UL:
                double shiftX = me.getX();
                double shiftY = me.getY();
                double baseX;
                double baseY;
                for(NodeIcon ni : nodeIcons.values()){
                    baseX = scaler.x(ni.node.rawX);
                    baseY = scaler.y(ni.node.rawY);
                    ni.setLocation((int)(baseX + shiftX - ni.getWidth() - 5), (int)(baseY + shiftY - ni.getHeight() - 5));
                }
                revalidate();
                repaint();
                break;
            case RESCALE_LR:
                scaler.setSize(me.getX() - Session.newMapX, me.getY() - Session.newMapY);
                for(NodeIcon ni : nodeIcons.values()){
                    ni.repos();
                    ni.setLocation(ni.getX() + Session.newMapX + 5, ni.getY() + Session.newMapY + 5);
                }
                break;
        }//end switch
        
        if(me.getY() < getHeight() * 0.1){
            pan(0, -5);
        } else if(me.getY() > getHeight() * 0.9){
            pan(0, 5);
        }
        repaint();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent mwe) {
        zoom -= mwe.getPreciseWheelRotation() / 100;
        
        clipW = (int)(buff.getWidth() * zoom);
        clipH = (int)(buff.getHeight() * zoom);
        
        int newWidth = (int)(buff.getWidth() * zoom);
        int newHeight = (int)(buff.getHeight() * zoom);
        
        BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.OPAQUE);
        Graphics2D g = resized.createGraphics();
        g.drawImage(buff, 0, 0, newWidth, newHeight, null);
        g.dispose();
        //setIcon(new ImageIcon(resized));
        scaler.setSource(this);
        resizeNodeIcons();
        
    }
    
    public static File createNewImageFile(BufferedImage image){
        File f = null;
        JFileChooser cd = new JFileChooser();
        cd.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        try {
            if(cd.showDialog(cd, "Select a location to place the new map file") == JFileChooser.APPROVE_OPTION){
                f = new File(cd.getSelectedFile().getPath() + File.separator + "mapImage" + System.currentTimeMillis() + ".png");
                ImageIO.write(image, "png", f);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        return f;
    }
    
    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        /*
        Drawing a clip of the image means we don't need to move the map, but how to deal with the node icons?
        */
        //System.out.println(clipX + ", " + clipY + ", " + clipW + ", " + clipH);
        g.drawImage(buff.getSubimage(clipX, clipY, clipW, clipH), 0, 0, (int)(getWidth() * aspectRatio), getHeight(), this);
        
    }
}