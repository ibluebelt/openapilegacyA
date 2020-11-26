package com.openapilegacy.server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * The type Netty server.
 */
@Component
@PropertySource(value = "classpath:/application.properties")
@Slf4j
public class NettyServer {
    /**
     * The Tcp port.
     */
    @Value("${tcp.port}")
    private int tcpPort;

    /**
     * The Boss count.
     */
    @Value("${boss.thread.count}")
    private int bossCount;

    /**
     * The Worker count.
     */
    @Value("${worker.thread.count}")
    private int workerCount;

    /**
     * The constant SERVICE_HANDLER.
     */
    private static final ServiceHandler SERVICE_HANDLER = new ServiceHandler();

    /**
     * Start.
     */
    public void start() {
        /**
         * 클라이언트 연결을 수락하는 부모 스레드 그룹
         */
        EventLoopGroup bossGroup = new NioEventLoopGroup(bossCount);
        
        /**
         * 연결된 클라이언트의 소켓으로 부터 데이터 입출력 및 이벤트를 담당하는 자식 스레드
         */
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class) // 서버 소켓 입출력 모드를 NIO로 설정
                    .handler(new LoggingHandler(LogLevel.INFO)) // 서버 소켓 채널 핸들러 등록
                    .childHandler(new ChannelInitializer() { // 송수신 되는 데이터 가공 핸들러
                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            SocketChannel socketChannel = (SocketChannel) ch;
                            ChannelPipeline channelPipeline = socketChannel.pipeline();
                            channelPipeline.addLast(new LoggingHandler(LogLevel.INFO));
                            channelPipeline.addLast(SERVICE_HANDLER);
                        }
                    });

            ChannelFuture channelFuture = serverBootstrap.bind(tcpPort).sync();
            channelFuture.channel().closeFuture().sync();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}