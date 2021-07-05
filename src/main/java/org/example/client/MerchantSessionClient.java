package org.example.client;

import org.example.client.dtos.SessionKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;

@Component
public class MerchantSessionClient extends AbstractOpayoClient {

    @Autowired private RestTemplate restTemplate;

    @Value("${opayo.uri.session-key}") private String sessionKeyURI;

    @Value("${opayo.integration.vendor-name}") private String vendorName;

    public SessionKey getSessionKey() {
        final Map<String, String> body = Collections.singletonMap("vendorName", vendorName);
        final HttpEntity<Map<String, String>> httpEntity = createRequest(body);

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
}
