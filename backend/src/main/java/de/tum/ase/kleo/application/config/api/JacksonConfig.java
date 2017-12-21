package de.tum.ase.kleo.application.config.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static com.fasterxml.jackson.annotation.PropertyAccessor.ALL;
import static com.fasterxml.jackson.annotation.PropertyAccessor.FIELD;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;

@Configuration
public class JacksonConfig {

    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    public void configureJacksonObjectMapper() {
        objectMapper.setVisibility(ALL, NONE);
        objectMapper.setVisibility(FIELD, ANY);
        objectMapper.setSerializationInclusion(NON_EMPTY);
        objectMapper.disable(WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.findAndRegisterModules();
    }
}
