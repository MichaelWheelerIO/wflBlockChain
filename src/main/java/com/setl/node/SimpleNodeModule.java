package com.setl.node;

import com.setl.node.dao.TransactionDao;
import com.setl.node.dao.TransactionDaoImpl;
import java.util.Map;

import javax.inject.Singleton;

import com.google.common.eventbus.EventBus;
import com.setl.ed25519.Ed25519DonnaJNI;
import com.setl.node.messages.MessageFactory;
import com.setl.node.messages.MessageType;
import com.setl.node.messages.actions.MessageAction;
import com.setl.node.messages.actions.impl.BlockCommittedAction;
import com.setl.node.messages.actions.impl.BlockFinalizedMessage;
import com.setl.node.messages.actions.impl.BlockRequestAction;
import com.setl.node.messages.actions.impl.PeerRecordAction;
import com.setl.node.messages.actions.impl.StateRequestAction;
import com.setl.node.messages.actions.impl.StateResponseAction;
import com.setl.node.network.p2p.PeerManager;
import com.setl.node.repository.BlockManagerRepository;
import com.setl.node.repository.SettingsRepository;
import com.setl.node.repository.impl.ChronicleMapBlockRepository;
import com.setl.node.requests.RequestRegister;
import com.setl.node.state.NodeState;
import com.setl.node.state.State;
import com.setl.node.wallet.Wallet;
import com.setl.node.wallet.WalletService;

import dagger.MapKey;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;
import io.setl.transport.poc.netty.BlockChainConfig;
import io.setl.transport.poc.netty.NettyMessageService;
import io.setl.transport.poc.netty.NettyTransportService;
import io.setl.transport.poc.netty.PipelineFactoryImpl;

@Module()
public class SimpleNodeModule {

	@Provides
	@Singleton
	Ed25519DonnaJNI provideEd25519DonnaJNI() {
		return new Ed25519DonnaJNI();
	}

	@Provides
	@Singleton
	PeerManager providePeerManager(NettyTransportService nettyTransportService) {
		return new PeerManager(nettyTransportService);
	}

	@Provides
	@Singleton
	EventBus provideEventBus() {
		return new EventBus();
	}

	@Provides
	@Singleton
	NettyTransportService provideNettyTransportService(NettyMessageService nettyTransportService) {
		return new NettyTransportService(new PipelineFactoryImpl(), nettyTransportService, new BlockChainConfig() {
		});
	}

	@Provides
	@Singleton
	NodeConfig provideNodeConfig() {
		return new NodeConfig() {

			@Override
			public String getWalletDbUrl() { 
				return null;
			}

			@Override
			public String getWalletDbUser() { 
				return null;
			}

			@Override
			public String getWalletDbPassword() { 
				return null;
			}
		};
	}

	@Provides
	@Singleton
	TransactionDao provideTransactionDao() {
		return new TransactionDaoImpl();
	}

	@Provides
	@Singleton
	BlockManagerRepository provideBlockManagerRepository() {
		return new ChronicleMapBlockRepository();
	}

	@Provides
	@Singleton
	MessageFactory provideMessageFactory(PeerManager peerManager, State state, NodeConfig nodeConfig) {
		return new MessageFactory(peerManager, state, nodeConfig);
	}

	@Provides
	@Singleton
	SimpleNode provideSimpleNode(PeerManager peerManager, MessageFactory messageFactory, NettyMessageService nettyMessageService,
			Map<MessageType, MessageAction> handlers, RequestRegister requestRegister,
			NettyTransportService transportService, NodeConfig nodeConfig) {
		return new SimpleNode(nettyMessageService, peerManager, messageFactory, requestRegister, handlers, transportService,
				nodeConfig);
	}

	@Provides
	@Singleton
	NettyMessageService provideNettyMessageService() {
		return new NettyMessageService();
	}

	@Provides
	@Singleton
	RequestRegister provideRequestRegister(NettyMessageService messageService) {
		return new RequestRegister(messageService);
	}

	@Provides
	@Singleton
	State provideState(WalletService walletService, SettingsRepository settingsRepository) {

		Wallet wallet = walletService.create();
		return new NodeState(wallet, settingsRepository);

	}

	@MapKey
	@interface MessageTypeKey {
		MessageType value();
	}

	@Provides
	@IntoMap
	@MessageTypeKey(MessageType.BlockCommitted)
	MessageAction BlockCommittedActionProvider(MessageFactory messageFactory, NettyMessageService messageService,
			RequestRegister requestRegister) {
		return new BlockCommittedAction(messageFactory, messageService, requestRegister);
	}

	@Provides
	@IntoMap
	@MessageTypeKey(MessageType.StateResponse)
	MessageAction StateResponseActionProvider(State state, MessageFactory messageFactory, NettyMessageService messageService,
			RequestRegister requestRegister, NodeConfig nodeConfig) {

		return new StateResponseAction(state, messageFactory, messageService, nodeConfig);

	}

	@Provides
	@IntoMap
	@MessageTypeKey(MessageType.StateRequest)
	MessageAction StateRequestActionProvider(MessageFactory messageFactory, NettyMessageService messageService) {

		return new StateRequestAction(messageService, messageFactory);

	}

	@Provides
	@IntoMap
	@MessageTypeKey(MessageType.BlockReq)
	MessageAction BlockRequestActionProvider(State state, BlockManagerRepository blockManagerRepository,
			MessageFactory messageFactory, NettyMessageService messageService) {

		return new BlockRequestAction(state, blockManagerRepository, messageFactory, messageService);

	}

	@Provides
	@IntoMap
	@MessageTypeKey(MessageType.BlockFinalized)
	MessageAction BlockFinalizedActionProvider(BlockManagerRepository blockManagerRepository,
			State state, EventBus bus,
			RequestRegister requestRegister, MessageFactory messageFactory,
			NettyMessageService messageService, TransactionDao transactionDao) {

		return new BlockFinalizedMessage(requestRegister,transactionDao);

	}

	@Provides
	@IntoMap
	@MessageTypeKey(MessageType.PeerRecord)
	MessageAction PeerRecordActionProvider(PeerManager peerManager) {
		return new PeerRecordAction(peerManager);
	}

}
