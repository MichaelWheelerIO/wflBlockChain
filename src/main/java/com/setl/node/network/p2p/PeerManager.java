package com.setl.node.network.p2p;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.setl.transport.poc.netty.NettyTransportService;

/* not thread safe */
public class PeerManager {

	private static Logger logger = LogManager.getLogger(PeerManager.class);
	private final NettyTransportService nettyTransportService;

	@Inject
	public PeerManager(NettyTransportService nettyTransportService) {
		this.nettyTransportService = nettyTransportService;
	}

	private List<Peer> peers = new ArrayList<>();

	public boolean addPeer(Peer peer) {
		if (nettyTransportService == null) {
			logger.fatal("nettyTransportService is not set");
			return false;
		}

		if (peers.contains(peer)) {
			logger.trace("peer {} already on list", peer);
			return false;
		}

		if (!peers.isEmpty()) {
			logger.trace("add peer {}", peer);
			if (!peer.getName().isEmpty()) {
				nettyTransportService.connect(peer.getHost(), peer.getPort());
				logger.trace("connect {}:{}", peer.getHost(), peer.getPort());
			} else {
				logger.trace("not connect {}:{}", peer.getHost(), peer.getPort());
				
			}

		}
		peers.add(peer);
		return true;
	}

	public List<Peer> getPeers() {
		return peers;

	}

}
