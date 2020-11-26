package com.openapilegacy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.openapilegacy.server.NettyServer;

@SpringBootApplication
public class OpenapilegacyApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(OpenapilegacyApplication.class, args);
        NettyServer nettyServer = context.getBean(NettyServer.class);
        nettyServer.start();
    }

}
