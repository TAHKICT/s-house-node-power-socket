package com.shouse.node.powerSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shouse.core.common.SystemConstants;
import shouse.core.api.Notifier;
import shouse.core.communication.NodeCommunicator;
import shouse.core.communication.Packet;
import shouse.core.node.Node;
import shouse.core.node.NodeInfo;
import shouse.core.node.NodeLocation;
import shouse.core.node.request.Request;
import shouse.core.node.response.ExecutionStatus;
import shouse.core.node.response.Response;
import shouse.core.node.response.ResponseStatus;

import java.time.LocalDateTime;
import java.util.List;

public class PowerSocketNode extends Node {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private NodeCommunicator nodeCommunicator;
    private String description;
    private boolean isSwitched;
    private Boolean requestedSwitchState = null;
    private List<Notifier> notifiers;

    public PowerSocketNode(int id, NodeLocation nodeLocation, String description, NodeCommunicator nodeCommunicator, List<Notifier> notifiers) {
        super(id, nodeLocation);
        setTypeName(this.getClass().getSimpleName());
        this.description = description;
        this.nodeCommunicator = nodeCommunicator;
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
        return new PowerSocketNodeInfo(this);
//        return new PowerSocketNodeInfo(getId(), getTypeName(), getNodeLocation(), description, isActive(), isSwitched);
    }

    @Override
    public Response process(Request request) {
        LOGGER.info(Thread.currentThread().getStackTrace()[1].getMethodName().concat(": ").concat(request.toString()));
        Response response = new Response();

        if (!isActive()) {
            LOGGER.error("Request processing fail. Node is not active.");
            response.setStatus(ResponseStatus.FAILURE);
            response.put(SystemConstants.failureMessage, "Node is not active.");
            return response;
        }

        if (request.getBody().getParameter("isSwitched").equals("true")) {
            requestedSwitchState = true;
            LOGGER.info("requestedSwitchState:"+requestedSwitchState);
        }
        else if (request.getBody().getParameter("isSwitched").equals("false")) {
            requestedSwitchState = false;
            LOGGER.info("requestedSwitchState:"+requestedSwitchState);
        }
        else {
            LOGGER.error("Request processing fail. Parameter value is wrong.");
            response.setStatus(ResponseStatus.FAILURE);
            response.put(SystemConstants.failureMessage, "Parameter value is wrong.");
            return response;
        }

        response.setStatus(ResponseStatus.SUCCESS);
        response.put(SystemConstants.nodeId, getId());
        response.put(SystemConstants.executionStatus, ExecutionStatus.IN_PROGRESS);
        response.put(SystemConstants.requestId, request.getBody().getParameter(SystemConstants.requestId));

        Packet packet = new Packet(getId());
        packet.putData("switch", String.valueOf(requestedSwitchState));
        packet.putData("requestId", request.getBody().getParameter(SystemConstants.requestId));

        LOGGER.info("Control packet sending: ".concat(packet.toString()));
        nodeCommunicator.sendPacket(packet);

        LOGGER.info("Return temporary: ".concat(response.toString()));
        return response;
    }

    @Override
    public void processPacket(Packet packet) {
        LOGGER.info(Thread.currentThread().getStackTrace()[1].getMethodName() + ": " + packet);

        //Alive packet detection
        if (packet.getData().get(SystemConstants.requestId) == null
                && packet.getData().get(SystemConstants.nodeTaskStatus) == null) {
            LOGGER.info("Received alive packet from Power Socket Node.");

            setLastAliveDate(LocalDateTime.now());

            if(isActive()) {
                LOGGER.info("Node already active. Last alive date updated.");
                return;
            }

            setActive(true);
            Response response = new Response(ResponseStatus.SUCCESS);
            response.put(SystemConstants.topic, SystemConstants.nodeAliveTopic);
            response.put(SystemConstants.nodeAliveState, true);
            response.put(SystemConstants.nodeId, getId());
            notifiers.stream().filter(notifier -> notifier != null).forEach(notifier -> notifier.sendResponse(response));

            return;
        }

        //Executed task detection
        if (packet.getData().get(SystemConstants.nodeTaskStatus) != null
                && packet.getData().get(SystemConstants.nodeTaskStatus).equals("executed")
                && packet.getData().get(SystemConstants.requestId) != null
                && !packet.getData().get(SystemConstants.requestId).equals("")) {

            LOGGER.info("Executed task detected. Requested switch state:"+requestedSwitchState);

            String requestId = packet.getData().get(SystemConstants.requestId);

            if ((packet.getData().get("switched") != null) &&
                    ((packet.getData().get("switched").equals("true") && requestedSwitchState == true)
                    ||
                    (packet.getData().get("switched").equals("false") &&  requestedSwitchState == false)
                    )
                ){
                isSwitched = requestedSwitchState;
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

    public String getDescription() {
        return description;
    }

    public Boolean getRequestedSwitchState() {
        return requestedSwitchState;
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