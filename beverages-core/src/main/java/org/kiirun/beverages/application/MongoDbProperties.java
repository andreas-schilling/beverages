package org.kiirun.beverages.application;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix="spring.data.mongodb")
public class MongoDbProperties {
	private int port;

    public int getPort() {
        return port;
    }

    public void setPort(int springDataMongodbPort) {
        this.port = springDataMongodbPort;
    }
}
