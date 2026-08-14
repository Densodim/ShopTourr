package com.shoptourr.api.config;

import com.shoptourr.domain.MoneyMath;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

import java.math.BigDecimal;

public final class BigDecimalAsStringDeserializer extends ValueDeserializer<BigDecimal> {

    @Override
    public BigDecimal deserialize(JsonParser parser, DeserializationContext context) {
        JsonToken token = parser.currentToken();
        if (token == JsonToken.VALUE_NULL) {
            return null;
        }
        if (token == JsonToken.VALUE_NUMBER_INT || token == JsonToken.VALUE_NUMBER_FLOAT) {
            return MoneyMath.scale(parser.getDecimalValue());
        }
        String raw = parser.getValueAsString();
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return MoneyMath.parse(raw);
    }
}
