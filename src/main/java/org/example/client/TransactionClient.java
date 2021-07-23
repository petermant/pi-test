package org.example.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.client.dtos.transaction.CredentialType;
import org.example.client.dtos.transaction.TransactionRequestDTO;
import org.example.client.dtos.transaction.TransactionResponseDTO;
import org.example.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
public class TransactionClient extends AbstractOpayoClient {

    @Autowired private RestTemplate restTemplate;

    @Value("${opayo.server-uri}${opayo.uri.transaction}") private String transactionURI;
    @Value("${org.example.3DSecureACSRedirectV2}") private String threeDSecureV2ResponseEndpoint;

    public TransactionResponseDTO requestTransaction(final String transactionType, Transaction t, CredentialType credentialType, final Boolean save, final Boolean reusable) {
        // always using V2 endpoint here, because it's ignored in V1 requests anyway
        final TransactionRequestDTO requestDTO = new TransactionRequestDTO(transactionType, t, threeDSecureV2ResponseEndpoint, credentialType, save, reusable);

        final HttpEntity<TransactionRequestDTO> httpEntity = createRequest(requestDTO);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(transactionURI, httpEntity, String.class);
            TransactionResponseDTO responseDTO;
            try {
                responseDTO = new ObjectMapper().readValue(response.getBody(), TransactionResponseDTO.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                logger.debug("Transaction request response: {} with {}", response.getStatusCode(), response.getBody());

                return responseDTO;
            } else {
                throw new RuntimeException("Transaction request was unsuccessful: " + response.getStatusCode());
            }
        } catch (HttpClientErrorException hce) {
            logger.error("{} error requesting payment transaction. Response body: {}", hce.getStatusCode(), hce.getResponseBodyAsString());
            // todo think about error pathway here - something more graceful...
            throw new RuntimeException("Client error requesting transaction");
        }
    }

    public TransactionResponseDTO getTransaction(UUID transactionId) {
        final HttpEntity<TransactionRequestDTO> httpEntity = createRequest(null);

        try {
            ResponseEntity<TransactionResponseDTO> response = restTemplate.exchange(transactionURI + "/" + transactionId.toString().toUpperCase(), HttpMethod.GET, httpEntity, TransactionResponseDTO.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                logger.debug("Transaction detail request response: {} with {}", response.getStatusCode(), response.getBody());

                return response.getBody();
            } else {
                throw new RuntimeException("Transaction request was unsuccessful: " + response.getStatusCode());
            }
        }  catch (HttpClientErrorException hce) {
            logger.error("{} error GETting transaction details. Response body: {}", hce.getStatusCode(), hce.getResponseBodyAsString());
            throw new RuntimeException("Client error requesting transaction detail");
        }
    }
}
