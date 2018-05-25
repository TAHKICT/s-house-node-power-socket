package com.shouse.node.powerSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shouse.core.common.SystemConstants;
import shouse.core.api.Notifier;
import shouse.core.communication.Communicator;
import shouse.core.communication.Packet;
import shouse.core.node.Node;
import shouse.core.node.NodeInfo;
import shouse.core.node.NodeLocation;
import shouse.core.node.request.Request;
import shouse.core.node.request.RequestIdGenerator;
import shouse.core.node.response.ExecutionStatus;
import shouse.core.node.response.Response;
import shouse.core.node.response.ResponseStatus;

import java.util.List;

public class PowerSocketNode extends Node {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private Communicator communicator;
    private String description;
    private boolean isSwitched;
    private List<Notifier> notifiers;

    public PowerSocketNode(int id, NodeLocation nodeLocation, String description, Communicator communicator, List<Notifier> notifiers) {
        super(id, nodeLocation);
        setTypeName(this.getClass().getSimpleName());
        this.description = description;
        this.communicator = communicator;
        this.notifiers = notifiers;
        LOGGER.info("PowerSocketNode created");
    }

    public boolean isSwitched() {
        return isSwitched;
    }

    public void setSwitched(boolean switched) {
        isSwitched = switched;
    }

    @Override
    public NodeInfo getNodeInfo() {
        return new PowerSocketNodeInfo(getId(), getTypeName(), getNodeLocation(), description, isActive(), isSwitched);
    }

    @Override
    public Response process(Request request) {
        Response response = new Response();

        if (!isActive()) {
            LOGGER.error("process. Request processing fail. Node is not active.");
            response.setStatus(ResponseStatus.FAILURE);
            response.put(SystemConstants.failureMessage, "Node is not active.");
            return response;
        }

        if (request.getBody().getParameter("isSwitched").equals("true"))
            setSwitched(true);
        else if (request.getBody().getParameter("isSwitched").equals("false"))
            setSwitched(false);
        else {
            LOGGER.error("process. Request processing fail. Parameter value is wrong.");
            response.setStatus(ResponseStatus.FAILURE);
            response.put(SystemConstants.failureMessage, "Parameter value is wrong.");
            return response;
        }

        String requestId = String.valueOf(RequestIdGenerator.generateId());

        response.setStatus(ResponseStatus.SUCCESS);
        response.put(SystemConstants.executionStatus, ExecutionStatus.IN_PROGRESS);
        response.put(SystemConstants.requestId, requestId);

        Packet packet = new Packet(getId());
        packet.putData("switch", String.valueOf(isSwitched()));
        packet.putData(SystemConstants.requestId, requestId);

        LOGGER.info("process. send packet: ".concat(packet.toString()));
//        communicator.sendPacket(packet);

        LOGGER.info("Send response: ".concat(response.toString()));
        return response;
    }

    @Override
    public void processPacket(Packet packet) {
        LOGGER.info(Thread.currentThread().getStackTrace()[1].getMethodName() + ": " + packet);

        //Alive packet detection
        if (packet.getData().get(SystemConstants.requestId) != null
                && packet.getData().get(SystemConstants.nodeTaskStatus) != null
                && packet.getData().get("switched") != null
                && packet.getData().get(SystemConstants.requestId).equals("")
                && packet.getData().get(SystemConstants.nodeTaskStatus).equals("")
                && packet.getData().get("switched").equals("")) {
            LOGGER.info("Received alive packet from node.");
            setActive(true);
            return;
        }

        //Executed task detection
        if (packet.getData().get(SystemConstants.nodeTaskStatus) != null
                && packet.getData().get(SystemConstants.nodeTaskStatus).equals("executed")
                && packet.getData().get(SystemConstants.requestId) != null
                && !packet.getData().get(SystemConstants.requestId).equals("")) {

            String requestId = packet.getData().get(SystemConstants.requestId);

            if ((packet.getData().get("switched") != null && packet.getData().get("switched").equals("on") && isSwitched)
                    ||
                    (packet.getData().get("switched") != null && packet.getData().get("switched").equals("off") && !isSwitched)) {
                LOGGER.info(String.format("processPacket. Request with id: {} successfully executed by node", requestId));

                Response response = new Response(ResponseStatus.SUCCESS);
                response.put(SystemConstants.executionStatus, ExecutionStatus.READY);
                response.put(SystemConstants.requestId, requestId);
                notifiers.stream().filter(notifier -> notifier != null).forEach(notifier -> notifier.sendResponse(response));
                return;
            }
        }

        LOGGER.error("processPacket. Invalid packet from node. Packet: " + packet);
    }

    @Override
    public String toString() {
        return "PowerSocketNode{" +
                ", nodeLocation=" + getNodeLocation() +
                ", description='" + description + '\'' +
                ", isSwitched=" + isSwitched +
                ", notifiers=" + notifiers +
                ", typeName=" + getTypeName() +
                '}';
    }
}