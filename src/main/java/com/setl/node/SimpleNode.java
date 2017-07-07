package com.setl.node;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.setl.node.messages.MessageFactory;
import com.setl.node.messages.MessageType;
import com.setl.node.messages.actions.MessageAction;
import com.setl.node.network.p2p.Peer;
import com.setl.node.network.p2p.PeerManager;
import com.setl.node.requests.RequestRegister;

import io.setl.transport.poc.messages.Message;
import io.setl.transport.poc.netty.NettyMessageService;
import io.setl.transport.poc.netty.NettyTransportService;

public class SimpleNode {

	private static Logger logger = LogManager.getLogger(SimpleNode.class);
	private final PeerManager peerManager;
	private final MessageFactory messageFactory;
	private final RequestRegister requestRegister;
	private final NettyMessageService messageService;
	private final Map<MessageType, MessageAction> handlers;
	private final NodeConfig config;

	private final NettyTransportService transportService;

	@Inject
	public SimpleNode(final NettyMessageService messageService, PeerManager peerManager, MessageFactory messageFactory,
			final RequestRegister requestRegister, Map<MessageType, MessageAction> handlers,
			NettyTransportService transportService,NodeConfig config) {
		this.transportService = transportService;
		this.peerManager = peerManager;
		this.messageFactory = messageFactory;
		this.handlers = handlers;
		this.messageService = messageService;
		this.requestRegister = requestRegister;
		this.config=config;
	}

	private void handle(Message msg) {
		int type = ((Number) msg.getContent().get(1)).intValue();
		MessageType messageType = MessageType.byCode(type);
		MessageAction handler = handlers.get(messageType);
		logger.info("handle message {} :: {}, handler present == {}", messageType, type, handler != null);
		if (handler != null) {
			handler.handleMessage(msg.getContent(), msg.getAddress());

		}
	}

	public void start(String remoteHost, int remotePort, int localPort)
			throws UnknownHostException, IOException, InterruptedException {
		peerManager.addPeer(new Peer(config.getNodeAddress(), localPort, 0, 0, 0, new HashMap<>(), "Wojciechs-iMac.local"));

		transportService.setHadler(this::handle);
		transportService.start();
		Thread.sleep(15000);
		transportService.connect(remoteHost, remotePort);
		Thread.sleep(2000);
		Message message = messageFactory.uniqueId(config.getUuid());
		messageService.broadcastMessage( message);
		messageService.broadcastMessage(messageFactory.peerList());
		Message msg = messageFactory.stateReq();
		messageService.sendMessage(msg);





	}

	public void stop() {
		requestRegister.stop();
		transportService.stop();
	}

 

}
