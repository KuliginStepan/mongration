package com.kuliginstepan.mongration.testconfiguration;

import com.mongodb.MongoClient;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongoCmdOptionsBuilder;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version.Main;
import de.flapdoodle.embed.process.runtime.Network;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class EmbeddedMongoReplicaSetConfiguration {

    private static final byte[] IP4_LOOPBACK_ADDRESS = { 127, 0, 0, 1 };

    private static final byte[] IP6_LOOPBACK_ADDRESS = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 1 };

    private final ApplicationContext context;

    @Bean
    public IMongodConfig embeddedMongoConfiguration() throws IOException {
        return new MongodConfigBuilder()
            .version(Main.V4_0)
            .withLaunchArgument("--replSet", "rs0")
            .stopTimeoutInMillis(6000)
            .cmdOptions(new MongoCmdOptionsBuilder().useNoJournal(false).build())
            .net(new Net(Network.getFreeServerPort(getHost()), Network.localhostIsIPv6()))
            .build();
    }

    @Bean
    public MongoClient mongo() {
        String port = context.getEnvironment().getRequiredProperty("local.mongo.port");
        return new MongoClient("localhost", Integer.parseInt(port));
    }

    private static InetAddress getHost() throws UnknownHostException {
            return InetAddress.getByAddress(Network.localhostIsIPv6()
                ? IP6_LOOPBACK_ADDRESS : IP4_LOOPBACK_ADDRESS);
    }
}
