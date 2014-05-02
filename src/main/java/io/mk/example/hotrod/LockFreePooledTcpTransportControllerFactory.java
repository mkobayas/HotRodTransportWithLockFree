package io.mk.example.hotrod;

import io.mk.pool.KeyedObjectControllerFactory;
import io.mk.pool.ObjectController;

import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

import org.infinispan.client.hotrod.impl.protocol.Codec;
import org.infinispan.client.hotrod.impl.transport.tcp.TcpTransport;


public class LockFreePooledTcpTransportControllerFactory implements
		KeyedObjectControllerFactory<SocketAddress, TcpTransport> {

	private final LockFreePooledTcpTransportFactory tcpTransportFactory;
	private final AtomicInteger topologyId;
	private final boolean pingOnStartup;
	private final Codec codec;

	public LockFreePooledTcpTransportControllerFactory(Codec codec,
			LockFreePooledTcpTransportFactory tcpTransportFactory, AtomicInteger topologyId,
			boolean pingOnStartup) {
		this.tcpTransportFactory = tcpTransportFactory;
		this.topologyId = topologyId;
		this.pingOnStartup = pingOnStartup;
		this.codec = codec;
	}

	@Override
	public ObjectController<TcpTransport> createObjectController(
			SocketAddress key) {
		LockFreePooledTcpTransportController controller = new LockFreePooledTcpTransportController(
				key, codec, tcpTransportFactory, topologyId, pingOnStartup);
		return controller;
	}

}
