package ca.discotek.deepdive.grep.gui.tree;

import ca.discotek.deepdive.grep.Location;

public class LocationNode extends AbstractNode {

    public final Location location;
    
    public LocationNode(Location location) {
        this.location = location;
        setUserObject(location.getName());
    }
    
    public String getName() {
        return location.getName();
    }
}
