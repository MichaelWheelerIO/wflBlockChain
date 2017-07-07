package com.setl.node.requests;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.setl.utils.Triple;

import io.setl.transport.poc.netty.NettyMessageService;

public class RequestRegister {
	private static Logger logger = LogManager.getLogger(RequestRegister.class);

	private class Command implements Runnable {

		public Command(Request request) {
			super();
			this.request = request;
		}

		final Request request;

		@Override
		public void run() {

			boolean canceled = requests.get(request.getKey()).middle.cancel(true);

			logger.error("Request {} failed after {} {} {}", request.getKey(), request.getTimeout()	, executor.getTaskCount(),
					canceled);

			Optional<String> connection = messageService.getConnections().stream()
					.filter(connectionId -> !request.getConnections().contains(connectionId)).findFirst();

			if (!connection.isPresent()) {
				String cid = messageService.sendMessage(request.getOriginalMsg());
				if (cid != null) {
					request.getConnections().clear();
					request.getConnections().add(cid);
					register(request);
				}
				;
			}

			connection.ifPresent(connectionId -> {
				logger.error("resent msg {}  to {}", request.getKey(), connectionId);
				request.getOriginalMsg().setAddress(connectionId);
				messageService.sendMessage(request.getOriginalMsg());
				request.getConnections().add(connectionId);
				register(request);
			});

		}

	}

	private final NettyMessageService messageService;

	public RequestRegister(NettyMessageService messageService) {
		this.messageService = messageService;
		executor.setRemoveOnCancelPolicy(true);
	}

 
	private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

	private final Map<RequestKey, Triple<Request, ScheduledFuture<?>, Command>> requests = new ConcurrentHashMap<>();

	public void register(Request request) {

		Triple<Request, ScheduledFuture<?>, Command> req = requests.get(request.getKey());
		// check is previous request exist
		if (req != null) {
			req.middle.cancel(true);
		}

		Command command = new Command(request);
	 	ScheduledFuture<?> scheduledFuture = executor.schedule(new Command(request), request.getTimeout(), TimeUnit.SECONDS);
		requests.put(request.getKey(), new Triple<>(request, scheduledFuture, command));

	}

	public boolean check(RequestKey requestKey) {

		Triple<Request, ScheduledFuture<?>, Command> request = requests.remove(requestKey);
		boolean removed = false;
		if (request != null) {
			removed = request.middle.cancel(true);
		}
		return removed;
	}

	public void stop() {
		executor.shutdown();
		
	}

}
