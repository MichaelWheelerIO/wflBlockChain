package com.setl.node.network.p2p;

import java.util.Map;

public class Peer {
	@Override
	public String toString() {
		return "Peer [host=" + host + ", port=" + port + ", conAttempt=" + conAttempt + ", unused=" + unused
				+ ", nodeType=" + nodeType + ", details=" + details + ", name=" + name + "]";
	}

	public Peer(String host, int port, int conAttempt, int unused, int nodeType, Map<String, Object> details,
			String name) {
		this.host = host;
		this.port = port;
		this.conAttempt = conAttempt;
		this.unused = unused;
		this.nodeType = nodeType;
		this.details = details;
		this.name = name;
	}

	public Peer(Object[] array) {
		this.host = (String) array[0];
		this.port = ((Number) array[1]).intValue();
		this.conAttempt = ((Number) array[2]).intValue();
		this.unused = ((Number) array[3]).intValue();
		this.nodeType = ((Number) array[4]).intValue();
		this.details = (Map<String, Object>) array[5];
		this.name = (String) array[6];
	}

	final private String host;
	final private int port;
	private int conAttempt;
	private int unused;
	final private int nodeType;
	final private Map<String, Object> details;
	final private String name;

	public Object[] packed() {
		return new Object[] { host, port, conAttempt, unused, nodeType, details, name };
	}

	public int getConAttempt() {
		return conAttempt;
	}

	public void setConAttempt(int conAttempt) {
		this.conAttempt = conAttempt;
	}

	public int getUnused() {
		return unused;
	}

	public void setUnused(int unused) {
		this.unused = unused;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public int getNodeType() {
		return nodeType;
	}

	public Map<String, Object> getDetails() {
		return details;
	}

	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((host == null) ? 0 : host.hashCode());
		result = prime * result + port;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Peer other = (Peer) obj;
		if (host == null) {
			if (other.host != null)
				return false;
		} else if (!host.equals(other.host))
			return false;
		if (port != other.port)
			return false;
		return true;
	}

}
