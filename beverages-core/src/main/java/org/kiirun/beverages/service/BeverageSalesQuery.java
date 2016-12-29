package org.kiirun.beverages.service;

import java.util.Optional;

import org.kiirun.beverages.domain.Beverage;
import org.kiirun.beverages.domain.BeverageSale;

import io.vertx.core.json.JsonObject;

public class BeverageSalesQuery {
    private final Optional<String> beverageName;

    private Optional<String> terminalId = Optional.empty();

    private BeverageSalesQuery(final String beverageName) {
        this.beverageName = Optional.ofNullable(beverageName);
    }

    public static BeverageSalesQuery searchFor(final String beverageName) {
        return new BeverageSalesQuery(beverageName);
    }

    public BeverageSalesQuery withTerminal(final String terminalId) {
        this.terminalId = Optional.ofNullable(terminalId);
        return this;
    }

    public JsonObject toQuery() {
        final JsonObject query = new JsonObject();
        beverageName.ifPresent(name -> query.put(BeverageSale._beverage + "." + Beverage._name, name));
        terminalId.ifPresent(id -> query.put(BeverageSale._terminalId, id));
        return query;
    }
}
