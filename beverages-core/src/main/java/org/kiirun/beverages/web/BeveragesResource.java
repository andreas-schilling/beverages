package org.kiirun.beverages.web;

import java.util.stream.Collectors;

import javax.inject.Inject;

import org.kiirun.beverages.application.BeveragesProperties;
import org.kiirun.beverages.domain.Beverage;
import org.kiirun.beverages.service.BeverageSalesQuery;
import org.kiirun.beverages.service.BeveragesRepository;
import org.kiirun.beverages.service.BeveragesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
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

    private final BeveragesService beveragesService;

    @Inject
    public BeveragesResource(final BeveragesProperties properties, final BeveragesRepository beveragesRepository,
            final BeveragesService beveragesService) {
        super();
        this.properties = properties;
        this.beveragesRepository = beveragesRepository;
        this.beveragesService = beveragesService;
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
            LOGGER.info("Beverages overview");
            final HttpServerResponse response = routingContext.response();
            beveragesRepository.getAllBeverages().setHandler(allBeverages -> {
                response.putHeader("content-type", "text/html").end("<h1>Beverages</h1><ul><li>"
                        + allBeverages.result().stream().map(Beverage::getName).collect(Collectors.joining("<li>"))
                        + "</ul>");
            });
        });

        router.get("/api/beverages").handler(this::getAllBeverages);
        router.post("/api/beverages/:name/sales/:terminal").handler(this::sellBeverage);
        router.get("/api/beverages/:name/sales").handler(this::getSoldBeverages);
        router.get("/api/beverages/:name/sales/:terminal").handler(this::getSoldBeverages);
        router.get("/api/sales").handler(this::getAccumulatedBeverageSales);
        router.get("/api/sales/:terminal").handler(this::getAccumulatedBeverageSales);
        router.route("/api/*").failureHandler(ErrorHandler.create());

        return router;
    }

    private void getAllBeverages(final RoutingContext routingContext) {
        beveragesRepository.getAllBeverages().setHandler(defaultToJsonHandler(200, routingContext));
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
        beveragesService.sellBeverage(name, 1L, terminal).setHandler(defaultToJsonHandler(201, routingContext));
    }

    private void getSoldBeverages(final RoutingContext routingContext) {
        final String name = routingContext.request().getParam("name");
        if (name == null) {
            routingContext.response().setStatusCode(400).end();
        }
        final String terminal = routingContext.request().getParam("terminal");
        final String mapping = Strings.nullToEmpty(routingContext.request().getParam("mapping"));
        if (mapping.equals("accumulated")) {
            getAccumulatedBeverageSales(routingContext);
        } else {
            beveragesRepository.findSoldBeverages(BeverageSalesQuery.searchFor(name).withTerminal(terminal))
                    .setHandler(defaultToJsonHandler(200, routingContext));
        }
    }

    private void getAccumulatedBeverageSales(final RoutingContext routingContext) {
        final String name = routingContext.request().getParam("name");
        final String terminal = routingContext.request().getParam("terminal");
        beveragesService.accumulateSales(BeverageSalesQuery.searchFor(name).withTerminal(terminal))
                .setHandler(defaultToJsonHandler(200, routingContext));
    }

    private <T> Handler<AsyncResult<T>> defaultToJsonHandler(final int statusCode,
            final RoutingContext routingContext) {
        return asyncResult -> {
            if (asyncResult.succeeded()) {
                routingContext.response().setStatusCode(statusCode)
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end(Json.encodePrettily(asyncResult.result()));
            }
        };
    }
}
