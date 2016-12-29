package org.kiirun.beverages.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.kiirun.beverages.domain.Beverage;
import org.kiirun.beverages.domain.BeverageSale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableList;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

@Component
public class BeveragesRepository extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(BeveragesRepository.class);

    private static final String BEVERAGES_COLLECTION = "beverages";

    private static final String BEVERAGE_SALES_COLLECTION = "beverageSales";

    private final MongoClient mongoClient;

    @Inject
    public BeveragesRepository(final MongoClient mongoClient) {
        super();
        this.mongoClient = mongoClient;
    }

    @Override
    public void start(final Future<Void> startFuture) throws Exception {
        LOGGER.info("Starting beverages repository...");
        setupBeverages(startFuture);
    }

    public Future<List<Beverage>> getAllBeverages() {
        final Future<List<JsonObject>> queryResult = Future.future();
        mongoClient.find(BEVERAGES_COLLECTION, new JsonObject(), queryResult.completer());
        return queryResult.compose(result -> {
            return Future.succeededFuture(result.stream().map(Beverage::new).collect(Collectors.toList()));
        });
    }

    public Future<Optional<Beverage>> findBeverageByName(final String beverageName) {
        final Future<List<JsonObject>> queryResult = Future.future();
        mongoClient.find(BEVERAGES_COLLECTION, new JsonObject().put(Beverage._name, beverageName),
                queryResult.completer());
        return queryResult.compose(result -> {
            return Future.succeededFuture(result.stream().map(Beverage::new).findFirst());
        });
    }

    public Future<BeverageSale> sellBeverage(final Beverage beverage, final Long amount, final String terminalId) {
        final BeverageSale beverageSale = beverage.sell(amount, terminalId);
        final Future<String> saveResult = Future.future();
        mongoClient.save(BEVERAGE_SALES_COLLECTION, beverageSale.toJson(), saveResult.completer());
        return saveResult.compose(id -> {
            beverageSale.setId(id);
            return Future.succeededFuture(beverageSale);
        });
    }

    public Future<List<BeverageSale>> findSoldBeverages(final BeverageSalesQuery query) {
        final Future<List<JsonObject>> queryResult = Future.future();
        mongoClient.find(BEVERAGE_SALES_COLLECTION, query.toQuery(), queryResult.completer());
        return queryResult.compose(result -> {
            return Future.succeededFuture(result.stream().map(BeverageSale::new).collect(Collectors.toList()));
        });
    }

    private void setupBeverages(final Future<Void> startFuture) {
        final List<Beverage> beverages = ImmutableList.of(new Beverage("Cola", new BigDecimal("1.50")),
                new Beverage("Beer", new BigDecimal("2.50")), new Beverage("Sparkling Water", new BigDecimal("1.00")));

        CompositeFuture.all(beverages.stream().map(b -> {
            final Future<String> saveResult = Future.future();
            mongoClient.insert(BEVERAGES_COLLECTION, b.toJson(), saveResult.completer());
            return saveResult;
        }).collect(Collectors.toList())).setHandler(result -> {
            if (result.succeeded()) {
                LOGGER.info("Successfully initialized beverages base data.");
                startFuture.complete();
            } else {
                LOGGER.error(result.cause().getMessage());
                startFuture.fail(result.cause());
            }
        });
    }
}
