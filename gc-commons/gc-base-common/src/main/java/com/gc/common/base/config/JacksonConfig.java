package com.gc.common.base.config;

import com.gc.common.base.serialization.date.LocalDateTimeDeserializer;
import com.gc.common.base.serialization.date.LocalDateTimeSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author shizhongming
 * 2020/9/13 5:38 下午
 */
@Configuration
public class JacksonConfig {

    @Value("${spring.jackson.serialization.write-dates-as-timestamps:false}")
    private Boolean datesAsTimestamps;

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer localDateTimeCustomizer() {
        return builder -> {
            if (Objects.equals(Boolean.TRUE, datesAsTimestamps)) {
                builder.deserializerByType(LocalDateTime.class, new LocalDateTimeDeserializer());
                builder.serializerByType(LocalDateTime.class, new LocalDateTimeSerializer());
            }
        };
    }
}
