package org.example.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import javax.annotation.PostConstruct;
import java.util.Base64;

public abstract class AbstractOpayoClient {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${opayo.integration.key}") private String integrationKey;
    @Value("${opayo.integration.password}") private String integrationPassword;

    private String integrationCode;

    @PostConstruct
    private void combineIntegrationCodes() {
        byte [] combined = (integrationKey + ":" + integrationPassword).getBytes();
        integrationCode = Base64.getEncoder().encodeToString(combined);
        logger.debug("{}: Created base 64 combined key: {}", this.getClass().getSimpleName(), integrationCode);
    }

    <T> HttpEntity<T> createRequest(final T body) {
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.setBasicAuth(integrationCode);

        return body == null ? new HttpEntity<>(httpHeaders) : new HttpEntity<>(body, httpHeaders);
    }
}
