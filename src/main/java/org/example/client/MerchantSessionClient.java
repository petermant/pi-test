package org.example.client;

import org.example.client.dtos.SessionKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;

@Component
public class MerchantSessionClient {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired private RestTemplate restTemplate;

    @Value("${opayo.uri.session-key}") private String sessionKeyURI;

    @Value("${opayo.integration.vendor-name}") private String vendorName;
    @Value("${opayo.integration.key}") private String integrationKey;
    @Value("${opayo.integration.password}") private String integrationPassword;

    private String integrationCode;

    @PostConstruct
    private void combineIntegrationCodes() {
        byte [] combined = (integrationKey + ":" + integrationPassword).getBytes();
        integrationCode = Base64.getEncoder().encodeToString(combined);
        logger.debug("Created base 64 combined key: {}", integrationCode);
    }

    public SessionKey getSessionKey() {
        final HttpEntity<Map<String, String>> httpEntity = createRequest();

        final ResponseEntity<SessionKey> response = restTemplate.postForEntity(sessionKeyURI, httpEntity, SessionKey.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            // assume keys themselves are semi-secrets so shouldn't be printed in logs
            logger.debug("Session key http response: {} with expiry {}", response.getStatusCode(), response.getBody().getExpiry());

            return response.getBody();
        } else {
            logger.error("{} error retrieving session key. Http response object: {}", response.getStatusCode(), response);
            throw new RuntimeException("Error retrieving session key");
        }
    }

    private HttpEntity<Map<String, String>> createRequest() {
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.setBasicAuth(integrationCode);

        final Map<String, String> body = Collections.singletonMap("vendorName", vendorName);
        return new HttpEntity<>(body, httpHeaders);
    }
}
