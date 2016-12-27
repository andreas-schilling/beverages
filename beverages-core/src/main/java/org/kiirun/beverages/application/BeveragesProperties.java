package org.kiirun.beverages.application;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "beverages")
public class BeveragesProperties {
	private int httpPort;
	
	private String dbName;

	public int getHttpPort() {
		return httpPort;
	}

	public void setHttpPort(final int httpPort) {
		this.httpPort = httpPort;
	}

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }
}
