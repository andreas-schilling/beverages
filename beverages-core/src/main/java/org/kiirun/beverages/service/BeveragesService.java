package org.kiirun.beverages.service;

import javax.inject.Inject;

import org.kiirun.beverages.domain.AccumulatedSales;
import org.kiirun.beverages.domain.BeverageSale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

@Component
public class BeveragesService extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(BeveragesService.class);

    private final BeveragesRepository beveragesRepository;

    @Inject
    public BeveragesService(final BeveragesRepository beveragesRepository) {
        super();
        this.beveragesRepository = beveragesRepository;
    }

    @Override
    public void start(final Future<Void> startFuture) throws Exception {
        LOGGER.info("Starting beverages service...");
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
