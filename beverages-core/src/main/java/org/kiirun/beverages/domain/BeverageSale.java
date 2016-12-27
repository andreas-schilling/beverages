package org.kiirun.beverages.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

public class BeverageSale implements JsonSerializableObject {
    public static final String _beverage = "beverage";
    public static final String _amount = "amount";
    public static final String _total = "total";
    public static final String _soldOn = "soldOn";
    public static final String _terminalId = "terminalId";

    private String id;

    private final Beverage beverage;

    private final Long amount;

    private final BigDecimal total;

    private final LocalDateTime soldOn;

    private final String terminalId;

    public BeverageSale(final Beverage beverage, final Long amount, final BigDecimal total, final LocalDateTime soldOn,
            final String terminalId) {
        super();
        this.beverage = beverage;
        this.amount = amount;
        this.total = total;
        this.soldOn = soldOn;
        this.terminalId = terminalId;
    }

    public BeverageSale(final JsonObject json) {
        this.id = json.getString("_id");
        this.beverage = new Beverage(json.getJsonObject(_beverage));
        this.amount = json.getLong(_amount);
        this.total = new BigDecimal(json.getString(_total));
        this.soldOn = LocalDateTime.ofInstant(Instant.ofEpochMilli(json.getLong(_soldOn)), ZoneId.of("Z"));
        this.terminalId = json.getString(_terminalId);
    }

    public static BeverageSale create(final Beverage beverage, final Long amount, final String terminalId) {
        Preconditions.checkArgument(amount != 0);
        return new BeverageSale(beverage, amount, beverage.getPrice().multiply(BigDecimal.valueOf(amount)).setScale(2),
                LocalDateTime.now(), terminalId);
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public Beverage getBeverage() {
        return beverage;
    }

    public Long getAmount() {
        return amount;
    }

    @JsonSerialize(using = BigDecimalSerializer.class)
    public BigDecimal getTotal() {
        return total;
    }

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    public LocalDateTime getSoldOn() {
        return soldOn;
    }

    public String getTerminalId() {
        return terminalId;
    }

    @Override
    public JsonObject toJson() {
        return new JsonObject(Json.encode(this));
    }
}
