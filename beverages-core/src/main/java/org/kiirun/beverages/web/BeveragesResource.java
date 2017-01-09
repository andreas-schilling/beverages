package org.kiirun.beverages.web;

import org.kiirun.beverages.infrastructure.Addresses;
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
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.ErrorHandler;

@Component
public class BeveragesResource extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(BeveragesResource.class);

    private final BeveragesRepository beveragesRepository;

    private final BeveragesService beveragesService;

    private final Router mainRouter;

    private final EventBus eventBus;

    public BeveragesResource(final BeveragesRepository beveragesRepository, final BeveragesService beveragesService,
            final Router mainRouter, final EventBus eventBus) {
        super();
        this.beveragesRepository = beveragesRepository;
        this.beveragesService = beveragesService;
        this.mainRouter = mainRouter;
        this.eventBus = eventBus;
    }

    @Override
    public void start(final Future<Void> startFuture) throws Exception {
        LOGGER.info("Starting beverages server...");
        mainRouter.mountSubRouter("/api", createRouter());
        startFuture.complete();
    }

    private Router createRouter() {
        final Router router = Router.router(vertx);
        router.get("/beverages").handler(this::getAllBeverages);
        router.post("/beverages/:name/sales/:terminal").handler(this::sellBeverage);
        router.get("/beverages/:name/sales").handler(this::getSoldBeverages);
        router.get("/beverages/:name/sales/:terminal").handler(this::getSoldBeverages);
        router.get("/sales").handler(this::getAccumulatedBeverageSales);
        router.get("/sales/:terminal").handler(this::getAccumulatedBeverageSales);
        router.put("/simulator").handler(this::triggerSimulator);
        router.route("/*").failureHandler(ErrorHandler.create());

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

    private void triggerSimulator(final RoutingContext routingContext) {
        eventBus.send(Addresses.INFRASTRUCTURE.address(), "TRIGGER-SIMULATOR");
        routingContext.response().setStatusCode(200).end();
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
