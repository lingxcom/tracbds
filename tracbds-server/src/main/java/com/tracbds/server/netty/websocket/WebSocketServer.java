package com.tracbds.server.netty.websocket;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tracbds.server.netty.JT808Server;
import com.tracbds.server.service.JT808ServerConfigService;
import com.lingx.web.ILingxThread;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
@Component
public class WebSocketServer implements ILingxThread, Runnable{
    private final static Logger log = LoggerFactory.getLogger(WebSocketServer.class);
	private ChannelFuture future = null;
	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;
    private Channel channel;

	@Autowired
	private JT808ServerConfigService configService;
	@Autowired
	private WebSocketServerInitializer wsServerInitializer;

	public void startServer() throws Exception {
		int port=Integer.parseInt(this.configService.getWebsocketPort());
		bossGroup = new NioEventLoopGroup(0,new DefaultThreadFactory("JT808ServerWebSocketBossGroup"));
		workerGroup = new NioEventLoopGroup(0,new DefaultThreadFactory("JT808ServerWebSocketWorkerGroup"));
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup);
			b.channel(NioServerSocketChannel.class);
			b.childHandler(wsServerInitializer);

			// 服务器绑定端口监听
			future = b.bind(port).sync();
			this.channel=future.channel();
			future.channel().closeFuture().sync();
			// 可以简写为
			/* b.bind(portNumber).sync().channel().closeFuture().sync(); */
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}

	@PreDestroy
	public void stop() {
		try {
			// 监听服务器关闭监听
			if (future != null)
				future.channel().closeFuture().sync();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}

	}

	@Override
	public void run() {
		try {
			this.startServer();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	


	@Override
	public String getName() {
		
		return "Tracbds-Server -> Websocket服务:"+this.configService.getWebsocketPort();
	}

	@Override
	public void startup() {
		new Thread(this,this.getName()).start();
	}

	@Override
	public void shutdown() {
		if(channel!=null)
		channel.close();
	}
}
