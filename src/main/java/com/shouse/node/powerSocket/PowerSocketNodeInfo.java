package com.shouse.node.powerSocket;

import shouse.core.node.NodeInfo;
import shouse.core.node.NodeLocation;

public class PowerSocketNodeInfo extends NodeInfo {
    private boolean isSwitched;

    public PowerSocketNodeInfo(int id, String nodeTypeId, NodeLocation nodeLocationId, String description, boolean isActive, boolean isSwitched) {
        super(id, nodeTypeId, nodeLocationId, description, isActive);
        this.isSwitched = isSwitched;
    }

    public boolean isSwitched() {
        return isSwitched;
    }

    public void setSwitched(boolean switched) {
        isSwitched = switched;
    }
}
