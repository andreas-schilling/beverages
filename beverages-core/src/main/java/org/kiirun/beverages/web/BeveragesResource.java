package org.kiirun.beverages.web;

import java.util.stream.Collectors;

import javax.inject.Inject;

import org.kiirun.beverages.application.BeveragesProperties;
import org.kiirun.beverages.domain.Beverage;
import org.kiirun.beverages.domain.BeverageException;
import org.kiirun.beverages.service.BeveragesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.ErrorHandler;

@Component
public class BeveragesResource extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(BeveragesResource.class);

    private final BeveragesProperties properties;

    private final BeveragesRepository beveragesRepository;

    @Inject
    public BeveragesResource(final BeveragesProperties properties, final BeveragesRepository beveragesRepository) {
        super();
        this.properties = properties;
        this.beveragesRepository = beveragesRepository;
    }

    @Override
    public void start(final Future<Void> startFuture) throws Exception {
        LOGGER.info("Starting beverages server...");
        final Router router = createRouter();
        vertx.createHttpServer().requestHandler(router::accept).listen(properties.getHttpPort(), result -> {
            if (result.succeeded()) {
                startFuture.complete();
            } else {
                startFuture.fail(result.cause());
            }
        });
    }

    private Router createRouter() {
        final Router router = Router.router(vertx);

        router.route("/").handler(routingContext -> {
            LOGGER.info("Serving request");
            final HttpServerResponse response = routingContext.response();
            beveragesRepository.getAllBeverages().setHandler(allBeverages -> {
                response.putHeader("content-type", "text/html").end("<h1>Beverages</h1><ul><li>"
                        + allBeverages.result().stream().map(Beverage::getName).collect(Collectors.joining("<li>"))
                        + "</ul>");
            });
        });

        router.post("/api/beverages/:name/sales/:terminal").handler(this::sellBeverage);
        router.get("/api/beverages/:name/sales").handler(this::getSoldBeverages);
        router.get("/api/beverages/:name/sales/:terminal").handler(this::getSoldBeverages);
        router.route("/api/*").failureHandler(ErrorHandler.create());

        return router;
    }

    private void sellBeverage(final RoutingContext routingContext) {
        final String name = routingContext.request().getParam("name");
        if (name == null) {
            routingContext.response().setStatusCode(400).end();
        }
        final String terminal = routingContext.request().getParam("terminal");
        if (terminal == null) {
            routingContext.response().setStatusCode(400).end();
        }
        beveragesRepository.findBeverageByName(name).setHandler(beverage -> {
            final Beverage toSell = beverage.result()
                    .orElseThrow(() -> new BeverageException("Beverage " + name + " not found!"));
            beveragesRepository.sellBeverage(toSell, 1L, terminal).setHandler(beverageSale -> {
                if (beverageSale.succeeded()) {
                    routingContext.response().setStatusCode(201)
                            .putHeader("content-type", "application/json; charset=utf-8")
                            .end(Json.encodePrettily(beverageSale.result()));
                }
            });
        });
    }

    private void getSoldBeverages(final RoutingContext routingContext) {
        final String name = routingContext.request().getParam("name");
        if (name == null) {
            routingContext.response().setStatusCode(400).end();
        }
        final String terminal = routingContext.request().getParam("terminal");
        beveragesRepository.findSoldBeverages(name, terminal).setHandler(foundSales -> {
            if (foundSales.succeeded()) {
                routingContext.response().setStatusCode(200)
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end(Json.encodePrettily(foundSales.result()));
            }
        });
    }
}
