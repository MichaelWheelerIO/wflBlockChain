package com.setl.node.state;

import java.util.Map;

import com.setl.node.wallet.Wallet;

public interface State {
	public static enum ApplicationState {
		unknown, stopped, starting, started, stopping
	}

	default ApplicationState getAplicatonState() {
		return ApplicationState.unknown;
	}

	default boolean updateState(ApplicationState newValue) {
		return false;
	}

	default boolean areLevelsEqual() {
		return getNetworkHeight() == getBlockHeight();
	}

	Map<Object, Object> getProposedTransactions();

	int getBlockHeight();

	boolean setBlockHeight(int newValue);

	int getNetworkHeight();

	boolean setNetworkHeight(int newValue);

	String getPreviousBlockHash();

	void setPreviousBlockHash(String newValue);

	Wallet getWallet();

}