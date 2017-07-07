package com.setl.node;

import io.setl.transport.poc.netty.BlockChainConfig;

public interface NodeConfig extends BlockChainConfig {

	/// getListeningPort
	// public static final int MY_PORT = 13450;

	@Override
	default int getListeningPort() {
		return 13430;
	}

	default String getUuid() {
		return java.util.UUID.randomUUID().toString();
	}

	default String getNodeAddress() {
		return "127.0.0.1";
	}

	// public static final String UUID = java.util.UUID.randomUUID().toString();
	// public static final String MY_ADDRESS = "127.0.0.1";

	default int getChainId() {
		return 2300;
	}
	// public static final Integer CHAIN_ID = 20;

	default int getNodeType() {
		return 0;
	}

	default String getBlocksPath(){
		return "./wallet/";
	}

	String getWalletDbUrl();

	String getWalletDbUser();
	String getWalletDbPassword();

}
