package com.shouse.node.powerSocket.config;

import com.shouse.node.powerSocket.PowerSocketNode;
import shouse.core.api.DefaultRequestProcessor;
import shouse.core.api.Notifier;
import shouse.core.api.RequestProcessor;
import shouse.core.communication.Communicator;
import shouse.core.communication.DefaultPacketProcessor;
import shouse.core.communication.PacketProcessor;
import shouse.core.controller.NodeContainer;
import shouse.core.loader.NodeConfig;
import shouse.core.loader.NodeFactory;
import shouse.core.node.Node;
import shouse.core.node.NodeLocation;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

/**
 * Created by Maks on 01.07.2018.
 */
@NodeConfig(name = "powerSocket")
public class PowerSocketfactory implements NodeFactory {

    private Set<Communicator> communicators;
    private Set<Notifier> notifiers;
    private RequestProcessor requestProcessor;
    private PacketProcessor packetProcessor;

    @Override
    public Node createNode(Map<String, Object> details) {
        NodeLocation kitchen = new NodeLocation(0, "Kitchen");
        return new PowerSocketNode(1, kitchen,"стиральная машина", communicators.stream().findFirst().get(), new ArrayList<>(notifiers));
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
    public void setCommunicators(Set<Communicator> communicators) {
        this.communicators = communicators;
    }

    @Override
    public void setNotifiers(Set<Notifier> notifiers) {
        this.notifiers = notifiers;
    }
}
