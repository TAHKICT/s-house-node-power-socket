package com.shouse.node.powerSocket.config;

import com.shouse.node.powerSocket.PowerSocketNode;
import shouse.core.api.DefaultRequestProcessor;
import shouse.core.api.Notifier;
import shouse.core.api.RequestProcessor;
import shouse.core.communication.NodeCommunicator;
import shouse.core.communication.DefaultPacketProcessor;
import shouse.core.communication.PacketProcessor;
import shouse.core.controller.NodeContainer;
import shouse.core.loader.NodeConfig;
import shouse.core.loader.NodeFactory;
import shouse.core.node.Node;
import shouse.core.node.NodeLocation;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by Maks on 01.07.2018.
 */
@NodeConfig(name = "powerSocket")
public class PowerSocketfactory implements NodeFactory<PowerSocketModel> {

    private Set<NodeCommunicator> nodeCommunicators;
    private Set<Notifier> notifiers;
    private RequestProcessor requestProcessor;
    private PacketProcessor packetProcessor;

    @Override
    public Node createNode(PowerSocketModel details) {
        NodeLocation kitchen = new NodeLocation(0, "Kitchen");
        return new PowerSocketNode(1, kitchen,"стиральная машина", nodeCommunicators.stream().findFirst().get(), new ArrayList<>(notifiers));
    }

    //TODO: created only to run application. Should be removed.
    @Override
    public Node createNode() {
        NodeLocation kitchen = new NodeLocation(0, "Kitchen");
        return new PowerSocketNode(1, kitchen,"стиральная машина", nodeCommunicators.stream().findFirst().get(), new ArrayList<>(notifiers));
    }

    @Override
    public RequestProcessor getRequestProcessor(NodeContainer nodeContainer) {
        if (this.requestProcessor != null) return requestProcessor;

        this.requestProcessor = new DefaultRequestProcessor(PowerSocketNode.class.getSimpleName(), nodeContainer);
        return requestProcessor;
    }

    @Override
    public PacketProcessor getPacketProcessor(NodeContainer nodeContainer) {
        if (this.packetProcessor != null) return packetProcessor;

        this.packetProcessor = new DefaultPacketProcessor(PowerSocketNode.class.getSimpleName(), nodeContainer);
        return packetProcessor;
    }

    @Override
    public String getTypeName() {
        return "powerSocket";
    }

    @Override
    public void setNodeCommunicators(Set<NodeCommunicator> nodeCommunicators) {
        this.nodeCommunicators = nodeCommunicators;
    }

    @Override
    public void setNotifiers(Set<Notifier> notifiers) {
        this.notifiers = notifiers;
    }
}
