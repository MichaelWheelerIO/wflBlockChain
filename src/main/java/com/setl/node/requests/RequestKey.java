package com.setl.node.requests;

import java.util.Arrays;

public class RequestKey {
	public RequestKey(RequestType type, Object... args) {
		super();
		this.type = type;
		this.args = args;
	}

	private final RequestType type;
	private final Object[] args;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(args);
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		RequestKey other = (RequestKey) obj;
		if (!Arrays.equals(args, other.args))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return type + "::" + Arrays.toString(args) + "";
	}
}
