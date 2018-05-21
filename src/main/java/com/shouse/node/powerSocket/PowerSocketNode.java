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
    private final Logger log = LoggerFactory.getLogger(this.getClass());

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
        log.info("PowerSocketNode created");
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

        if(!isActive()){
            log.error("process. Request processing fail. Parameter value is wrong.");
            response.setStatus(ResponseStatus.FAILURE);
            response.put(SystemConstants.failureMessage, "Node is not active.");
            return response;
        }

        if(request.getBody().getParameter("switch").equals("checked"))
            setSwitched(true);
        else if(request.getBody().getParameter("switch").equals("unchecked"))
            setSwitched(false);
        else {
            log.error("process. Request processing fail. Parameter value is wrong.");
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
        communicator.sendPacket(packet);

        return response;
    }

    @Override
    public void processPacket(Packet packet) {

        if(packet.getData().get(SystemConstants.nodeTaskStatus).equals("executed")){
            String requestId = packet.getData().get(SystemConstants.requestId);
            notifiers.stream().forEach(notifier -> {
            if(notifier != null) {
                log.info(String.format("processPacket. Request with id: {} successfully executed by node", requestId));

                Response response = new Response(ResponseStatus.SUCCESS);
                response.put(SystemConstants.executionStatus, ExecutionStatus.READY);
                response.put(SystemConstants.requestId, requestId);
                notifier.sendResponse(response);
            }});
        }

        if(packet.getData().get(SystemConstants.nodeTaskStatus) == null && packet.getData().get(SystemConstants.nodeTaskStatus) == null){
            boolean isSwitchedReal;
            if(packet.getData().get("switched").equals("on"))
                isSwitchedReal = true;
            else if(packet.getData().get("switched").equals("off"))
                isSwitchedReal = true;
            else {
                log.error("processPacket. Invalid packet from node. Packet: " + packet);
                return;
            }

        }

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