package org.example.client;

import org.example.client.dtos.session.SessionKey;
import org.example.client.dtos.transaction.ThreeDSecure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Component
public class ThreeDSecureClient extends AbstractOpayoClient {

    @Autowired private RestTemplate restTemplate;

    @Value("${opayo.uri.three-d-secure.fallback-complete}") private String threeDSecureFallbackCompleteURI;

    public String fallbackComplete(UUID transactionId, String paRes) {
        final Map<String, String> body = Collections.singletonMap("paRes", paRes);
        final Map<String, String> uriParams = Collections.singletonMap("transactionId", transactionId.toString());
        final HttpEntity<Map<String, String>> httpEntity = createRequest(body);

        ResponseEntity<ThreeDSecure> response;

        try {
            response = restTemplate.postForEntity(threeDSecureFallbackCompleteURI, httpEntity, ThreeDSecure.class, uriParams);
        } catch (HttpClientErrorException hce) {
            throw new RuntimeException("Error completing 3D Secure fallback", hce);
        }

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            logger.debug("Fallback complete http response: {} with expiry {}", response.getStatusCode(), response.getBody().getStatus());

            return response.getBody().getStatus();
        } else {
            logger.error("{} error retrieving 3DSecure fallback status. Http response object: {}", response.getStatusCode(), response);
            throw new RuntimeException("Error retrieving 3DSecure fallback status");
        }
    }

}
