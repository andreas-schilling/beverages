package org.kiirun.beverages.domain;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

public class Beverage implements JsonSerializableObject {
    public static final String _name = "name";
    public static final String _price = "price";

    private String id;

    private final String name;

    private final BigDecimal price;

    public Beverage(final String name, final BigDecimal price) {
        super();
        this.name = name;
        this.price = price;
    }

    public Beverage(final JsonObject json) {
        this.id = json.getString("_id");
        this.name = json.getString(_name);
        this.price = new BigDecimal(json.getString(_price));
    }

    @JsonProperty("_id")
    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    @JsonSerialize(using = BigDecimalSerializer.class)
    public BigDecimal getPrice() {
        return price;
    }

    public BeverageSale sell(final Long amount, final String terminalId) {
        return BeverageSale.create(this, amount, terminalId);
    }

    @Override
    public JsonObject toJson() {
        return new JsonObject(Json.encode(this));
    }
}
