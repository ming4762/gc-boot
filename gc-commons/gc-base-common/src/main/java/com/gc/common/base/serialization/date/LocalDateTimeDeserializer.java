package com.gc.common.base.serialization.date;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

/**
 * @author shizhongming
 * 2020/9/13 5:37 下午
 */
public class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        long timestamp = p.getLongValue();
        return LocalDateTime.ofEpochSecond(timestamp / 1000, 0, OffsetDateTime.now().getOffset());
    }
}
