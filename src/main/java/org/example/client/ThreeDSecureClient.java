package org.example.client;

import org.example.client.dtos.session.SessionKey;
import org.example.client.dtos.transaction.ThreeDSecure;
import org.example.client.dtos.transaction.TransactionResponseDTO;
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
    @Autowired private TransactionClient transactionClient;

    @Value("${opayo.server-uri}${opayo.uri.three-d-secure.challenge-complete}") private String threeDSecureChallengeCompleteURI;
    @Value("${opayo.server-uri}${opayo.uri.three-d-secure.fallback-complete}") private String threeDSecureFallbackCompleteURI;

    public TransactionResponseDTO challengeComplete(UUID transactionId, String cres) {
        final Map<String, String> body = Collections.singletonMap("cRes", cres);
        final Map<String, String> uriParams = Collections.singletonMap("transactionId", transactionId.toString().toUpperCase());
        final HttpEntity<Map<String, String>> httpEntity = createRequest(body);

        try {
            ResponseEntity<TransactionResponseDTO> response = restTemplate.postForEntity(threeDSecureChallengeCompleteURI, httpEntity, TransactionResponseDTO.class, uriParams);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                logger.debug("Challenge complete http response: {} with status {}", response.getStatusCode(), response.getBody().getStatus());

                return response.getBody();
            } else {
                logger.error("{} error retrieving 3DSecure challenge status. Http response object: {}", response.getStatusCode(), response);
                throw new RuntimeException("Error retrieving 3DSecure challenge status");
            }
        } catch (HttpClientErrorException hce) {
            // this is (probably?) not possible as get API doesn't allow access to in-flight transactions
//            try {
//                logger.debug("Tried to get transaction, response was {}", transactionClient.getTransaction(transactionId));
//            } catch (Exception e) {
//                logger.error("Couldn't get details with Opayo TX ID", e);
//            }

            throw new RuntimeException("Error completing 3D Secure challenge", hce);
        }
    }

    public TransactionResponseDTO fallbackComplete(UUID transactionId, String paRes) {
        final Map<String, String> body = Collections.singletonMap("paRes", paRes);
        final Map<String, String> uriParams = Collections.singletonMap("transactionId", transactionId.toString().toUpperCase());
        final HttpEntity<Map<String, String>> httpEntity = createRequest(body);

        ResponseEntity<ThreeDSecure> response;

        try {
            response = restTemplate.postForEntity(threeDSecureFallbackCompleteURI, httpEntity, ThreeDSecure.class, uriParams);
        } catch (HttpClientErrorException hce) {
            // this is (probably?) not possible as get API doesn't allow access to in-flight transactions
//            try {
//                logger.debug("Tried to get transaction, response was {}", transactionClient.getTransaction(transactionId));
//            } catch (Exception e) {
//                logger.error("Couldn't get details with Opayo TX ID", e);
//            }

            throw new RuntimeException("Error completing 3D Secure fallback", hce);
        }

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            logger.debug("Fallback complete http response: {} with status {}", response.getStatusCode(), response.getBody().getStatus());

            return transactionClient.getTransaction(transactionId);
        } else {
            logger.error("{} error retrieving 3DSecure fallback status. Http response object: {}", response.getStatusCode(), response);
            throw new RuntimeException("Error retrieving 3DSecure fallback status");
        }
    }

}
