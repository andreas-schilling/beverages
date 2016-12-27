package org.kiirun.beverages.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.vertx.core.json.JsonObject;

@JsonInclude(Include.NON_NULL)
public interface JsonSerializableObject {
    JsonObject toJson();
}
