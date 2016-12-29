package org.kiirun.beverages.service;

import java.util.List;

import javax.inject.Inject;

import org.kiirun.beverages.domain.AccumulatedSales;
import org.kiirun.beverages.domain.BeverageSale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.base.Splitter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;

@Component
public class BeveragesService extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(BeveragesService.class);

    private final BeveragesRepository beveragesRepository;

    private final EventBus eventBus;

    @Inject
    public BeveragesService(final BeveragesRepository beveragesRepository, final EventBus eventBus) {
        super();
        this.beveragesRepository = beveragesRepository;
        this.eventBus = eventBus;
    }

    @Override
    public void start(final Future<Void> startFuture) throws Exception {
        LOGGER.info("Starting beverages service...");
        eventBus.consumer("beverages.core.sell", message -> {
            final String salesMessage = message.body().toString();
            LOGGER.info("Receiving sales message: " + salesMessage);
            final List<String> contents = Splitter.on(":").splitToList(salesMessage);
            sellBeverage(contents.get(0), Long.valueOf(contents.get(1)), contents.get(2));
        });
        startFuture.complete();
    }

    public Future<AccumulatedSales> accumulateSales(final BeverageSalesQuery query) {
        return beveragesRepository.findSoldBeverages(query).compose(foundSales -> {
            return Future.succeededFuture(AccumulatedSales.calculatedFrom(foundSales));
        });
    }

    public Future<BeverageSale> sellBeverage(final String beverageName, final Long amount, final String terminalId) {
        return beveragesRepository.findBeverageByName(beverageName).<BeverageSale>compose(toSell -> {
            if (toSell.isPresent()) {
                return beveragesRepository.sellBeverage(toSell.get(), 1L, terminalId);
            }
            return Future.<BeverageSale>failedFuture("Could not find beverage " + beverageName);
        });
    }
}
