package com.setl.node.requests;

import java.util.List;

import io.setl.transport.poc.messages.Message;

public class Request {

	public Request(RequestKey key, Message originalMsg, List<String> connections) {
		this.key = key;
		this.originalMsg = originalMsg;
		this.connections = connections;
		this.timeout = 10;

	}

	public Request(RequestKey key, Message originalMsg, int timeout, List<String> connections) {
		this.key = key;
		this.originalMsg = originalMsg;
		this.connections = connections;
		this.timeout = timeout;

	}

	private final RequestKey key;
	private final Message originalMsg;
	private final List<String> connections;
	private final int timeout;

	public Message getOriginalMsg() {
		return originalMsg;
	}

	public List<String> getConnections() {
		return connections;
	}

	public RequestKey getKey() {
		return key;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
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
		Request other = (Request) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}

	public int getTimeout() {
		return timeout;
	}

}
