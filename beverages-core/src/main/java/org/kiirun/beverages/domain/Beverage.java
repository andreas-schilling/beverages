package org.kiirun.beverages.domain;

import java.math.BigDecimal;
import java.util.Objects;

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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (id == null ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Beverage other = (Beverage) obj;
        return Objects.equals(id, other.id);
    }
}
