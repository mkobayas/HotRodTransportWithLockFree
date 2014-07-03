package org.mk300.example.hotrod;

import org.mk300.pool.ObjectController;

import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

import org.infinispan.client.hotrod.impl.operations.PingOperation;
import org.infinispan.client.hotrod.impl.protocol.Codec;
import org.infinispan.client.hotrod.impl.transport.tcp.TcpTransport;
import org.infinispan.client.hotrod.logging.Log;
import org.infinispan.client.hotrod.logging.LogFactory;

public class LockFreePooledTcpTransportController implements ObjectController<TcpTransport> {

	private static final Log log = LogFactory.getLog(LockFreePooledTcpTransportController.class);

	private final SocketAddress address;

	private final LockFreePooledTcpTransportFactory tcpTransportFactory;
	private final AtomicInteger topologyId;
	private final boolean pingOnStartup;
	private volatile boolean firstPingExecuted = false;
	private final Codec codec;

	public LockFreePooledTcpTransportController(SocketAddress address, Codec codec,
			LockFreePooledTcpTransportFactory tcpTransportFactory, AtomicInteger topologyId,
			boolean pingOnStartup) {
		this.address = address;

		this.tcpTransportFactory = tcpTransportFactory;
		this.topologyId = topologyId;
		this.pingOnStartup = pingOnStartup;
		this.codec = codec;
	}

	@Override
	public TcpTransport create() {
		TcpTransport tcpTransport = new TcpTransport(address,
				tcpTransportFactory);
		if (log.isTraceEnabled()) {
			log.tracef("Created tcp transport: %s", tcpTransport);
		}

		if (pingOnStartup && !firstPingExecuted) {
			log.trace("Executing first ping!");
			firstPingExecuted = true;

			// Don't ignore exceptions from ping() command, since
			// they indicate that the transport instance is invalid.
			ping(tcpTransport, topologyId);
		}
		return tcpTransport;
	}

	@Override
	public void destory(TcpTransport obj) {

		obj.destroy();
	}

	private PingOperation.PingResult ping(TcpTransport tcpTransport,
			AtomicInteger topologyId) {
		PingOperation po = new PingOperation(codec, topologyId, tcpTransport);
		return po.execute();
	}
}
