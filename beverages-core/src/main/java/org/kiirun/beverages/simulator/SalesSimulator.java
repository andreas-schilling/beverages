package org.kiirun.beverages.simulator;

import java.util.List;
import java.util.Random;

import javax.inject.Inject;

import org.kiirun.beverages.infrastructure.Addresses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;

@Component
public class SalesSimulator extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(SalesSimulator.class);

    private static final List<String> BEVERAGES = ImmutableList.of("Cola", "Beer", "Sparkling Water");

    private static final List<String> TERMINALS = ImmutableList.of("TERM-1", "TERM-2");

    private final EventBus eventBus;

    private final Random random = new Random(System.currentTimeMillis());

    private boolean active = false;

    @Inject
    public SalesSimulator(final EventBus eventBus) {
        super();
        this.eventBus = eventBus;
    }

    @Override
    public void start(final Future<Void> startFuture) throws Exception {
        LOGGER.info("Starting beverages sales simulator...");
        eventBus.consumer(Addresses.INFRASTRUCTURE.address(), message -> {
            final String triggerMessage = message.body().toString();
            if (triggerMessage.equals("TRIGGER-SIMULATOR")) {
                active = active ? false : true;
                LOGGER.info("Triggering simulator to: " + (active ? "ON" : "OFF"));
            }
        });
        vertx.setPeriodic(1000, timerId -> {
            if (active) {
                final String beverage = BEVERAGES.get(random.nextInt(BEVERAGES.size()));
                final String amount = String.valueOf(random.nextInt(5));
                final String terminal = TERMINALS.get(random.nextInt(TERMINALS.size()));
                final String salesMessage = Joiner.on(":").join(beverage, amount, terminal);
                eventBus.publish(Addresses.BEVERAGE_SALE.address(), salesMessage);
                LOGGER.info("Sending sales message: " + salesMessage);
            }
        });
        startFuture.complete();
    }
}
