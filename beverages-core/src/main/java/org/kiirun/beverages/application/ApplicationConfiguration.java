package org.kiirun.beverages.application;

import javax.inject.Inject;

import org.kiirun.beverages.service.BeveragesRepository;
import org.kiirun.beverages.service.BeveragesService;
import org.kiirun.beverages.simulator.SalesSimulator;
import org.kiirun.beverages.web.BeveragesResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import com.google.common.collect.ImmutableMap;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
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

    @Bean
    public EventBus eventBus() {
        return vertx().eventBus();
    }

    @Inject
    @Bean
    public BeveragesResource beveragesResource(final BeveragesProperties properties,
            final BeveragesRepository beveragesRepository, final BeveragesService beveragesService) {
        return new BeveragesResource(properties, beveragesRepository, beveragesService);
    }

    @Inject
    @Bean
    public BeveragesRepository beveragesRepository(final MongoClient mongoClient) {
        return new BeveragesRepository(mongoClient);
    }

    @Inject
    @Bean
    public BeveragesService beveragesService(final BeveragesRepository beveragesRepository) {
        return new BeveragesService(beveragesRepository, eventBus());
    }

    @Bean
    @DependsOn("mongo")
    public SalesSimulator salesSimulator() {
        return new SalesSimulator(eventBus());
    }

    @Inject
    @Bean
    @DependsOn("mongo")
    public MongoClient mongoClient(final MongoDbProperties commonProperties, final BeveragesProperties properties) {
        final JsonObject mongoConfig = new JsonObject(ImmutableMap.<String, Object>builder()
                .put("port", commonProperties.getPort()).put("db_name", properties.getDbName()).build());
        LOGGER.info(mongoConfig.toString());
        return MongoClient.createShared(vertx(), mongoConfig);
    }
}
