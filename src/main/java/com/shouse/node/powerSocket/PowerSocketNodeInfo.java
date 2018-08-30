package com.shouse.node.powerSocket;

import shouse.core.node.NodeInfo;
import shouse.core.node.NodeLocation;

public class PowerSocketNodeInfo extends NodeInfo {
    private boolean isSwitched;
    private boolean inProcess;

    public PowerSocketNodeInfo(PowerSocketNode powerSocketNode){
        super(powerSocketNode.getId(),
                powerSocketNode.getTypeName(),
                powerSocketNode.getNodeLocation(),
                powerSocketNode.getDescription(),
                powerSocketNode.isActive());
        this.isSwitched = powerSocketNode.isSwitched();
        this.inProcess = powerSocketNode.isInProcess();
    }

    public boolean isSwitched() {
        return isSwitched;
    }

    public boolean isInProcess() {
        return inProcess;
    }
}
