package nodemanager.events;

import nodemanager.node.Node;

/**
 * Created when a connection between nodes is added
 * @author Matt Crow
 */
public class ConnectionAddedEvent extends EditEvent{
    private final int id1;
    private final int id2;
    
    public ConnectionAddedEvent(int from, int to){
        id1 = from;
        id2 = to;
    }
    
    @Override
    public void undo() {
        Node.get(id1).removeAdj(id2);
    }

    @Override
    public void redo() {
        Node.get(id1).addAdjId(id2);
    }
    
}
