package com.shouse.node.powerSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shouse.core.communication.Packet;
import shouse.core.communication.PacketProcessor;
import shouse.core.controller.NodeContainer;

public class PowerSocketPacketProcessor implements PacketProcessor {
    private final Logger log = LoggerFactory.getLogger(this.getClass());


    private NodeContainer nodeContainer;

    public PowerSocketPacketProcessor(NodeContainer nodeContainer) {
        this.nodeContainer = nodeContainer;
    }

    @Override
    public void processPacket(Packet packet) {
        PowerSocketNode node = (PowerSocketNode) nodeContainer.getNode(packet.getNodeId()).get();
        node.processPacket(packet);
    }

    @Override
    public boolean isApplicable(Packet packet) {

        if(packet.getData().get("nodeTypeName").equals(PowerSocketNode.class.getSimpleName())) {
            log.info("isApplicable. true. packet: " + packet);
            return true;
        }

        log.info("isApplicable. false. packet: " + packet);
        return false;
    }
}
