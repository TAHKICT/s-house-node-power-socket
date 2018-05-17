package com.shouse.node.PowerSocket;

import shouse.core.node.NodeInfo;

public class PowerSocketNodeInfo extends NodeInfo {
    private boolean isSwitched;

    public PowerSocketNodeInfo(int id, int nodeTypeId, int nodeLocationId, String description, boolean isActive, boolean isSwitched) {
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
