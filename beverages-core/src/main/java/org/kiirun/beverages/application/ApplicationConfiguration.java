package org.kiirun.beverages.application;

import javax.inject.Inject;

import org.kiirun.beverages.service.BeveragesRepository;
import org.kiirun.beverages.web.BeveragesResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.ImmutableMap;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

@Configuration
@EnableConfigurationProperties({ BeveragesProperties.class, MongoDbProperties.class })
public class ApplicationConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationConfiguration.class);

    @Bean
    public Vertx vertx() {
        return Vertx.vertx();
    }

    @Inject
    @Bean
    public BeveragesResource beveragesResource(final BeveragesProperties properties,
            final BeveragesRepository beveragesRepository) {
        return new BeveragesResource(properties, beveragesRepository);
    }

    @Inject
    @Bean
    public BeveragesRepository beveragesRepository(final MongoClient mongoClient) {
        return new BeveragesRepository(mongoClient);
    }

    @Inject
    @Bean
    public MongoClient mongoClient(final MongoDbProperties commonProperties, final BeveragesProperties properties) {
        final JsonObject mongoConfig = new JsonObject(ImmutableMap.<String, Object>builder()
                .put("port", commonProperties.getPort()).put("db_name", properties.getDbName()).build());
        LOGGER.info(mongoConfig.toString());
        return MongoClient.createShared(vertx(), mongoConfig);
    }
}
