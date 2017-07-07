package com.setl.node.state;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import com.setl.node.repository.SettingsRepository;
import com.setl.node.wallet.Wallet;

public class NodeState implements State {

	@Inject
	public NodeState(Wallet wallet, SettingsRepository settingsRepository) {
		this.wallet = wallet;
		this.settingsRepository = settingsRepository;
	}

	private final Wallet wallet;

	private final SettingsRepository settingsRepository;

	private Map<Object, Object> proposedTransactions = new HashMap<>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.setl.node.state.Node#blockHeight()
	 */
	@Override
	public int getBlockHeight() {
		Object value = settingsRepository.get(SettingsKeys.BlockHeight);
		if (value == null) {
			return 0;
		}
		return ((Number) value).intValue();

	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.setl.node.state.Node#updateHeight(int)
	 */
	@Override
	public boolean setBlockHeight(int newValue) {
		Object value = settingsRepository.get(SettingsKeys.BlockHeight);
		if (value != null && ((Number) value).intValue() >= newValue) {
			return false;
		}
		settingsRepository.put(SettingsKeys.BlockHeight, newValue);
		return true;
	}

	public Map<Object, Object> getProposedTransactions() {
		return proposedTransactions;
	}

	@Override
	public int getNetworkHeight() {
		Object value = settingsRepository.get(SettingsKeys.NetworkHeight);
		if (value == null) {
			return 0;
		}
		return ((Number) value).intValue()
			;
	}

	@Override
	public boolean setNetworkHeight(int newValue) {
		Object value = settingsRepository.get(SettingsKeys.NetworkHeight);
		if (value != null && ((Number) value).intValue() >= newValue) {
			return false;
		}
		settingsRepository.put(SettingsKeys.NetworkHeight, newValue);
		return true;
	}

	@Override
	public String getPreviousBlockHash() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPreviousBlockHash(String newValue) {
		// TODO Auto-generated method stub

	}

	@Override
	public Wallet getWallet() {
		return wallet;
	}

}
