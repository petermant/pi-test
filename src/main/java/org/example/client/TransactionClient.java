package org.example.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.client.dtos.instruction.InstructionBody;
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

import java.util.HashMap;
import java.util.UUID;

@Component
public class TransactionClient extends AbstractOpayoClient {

    private static final TypeReference<HashMap<String, Object>> MAP_TYPE  = new TypeReference<HashMap<String, Object>>() {};

    @Autowired private RestTemplate restTemplate;
    @Autowired private ObjectMapper mapper;

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

    public HashMap<String, Object> releaseTransaction(Transaction tx) {

        InstructionBody instructionBody = new InstructionBody("release", tx.getAmount());
        final HttpEntity<InstructionBody> httpEntity = createRequest(instructionBody);

        try {
            ResponseEntity<String> response = restTemplate.exchange(transactionURI + "/" + tx.getOpayoTransactionId().toString().toUpperCase() + "/instructions", HttpMethod.POST, httpEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                logger.debug("Release instruction response: {} with {}", response.getStatusCode(), response.getBody());

                return mapper.readValue(response.getBody(), MAP_TYPE);
            } else {
                throw new RuntimeException("Transaction request was unsuccessful: " + response.getStatusCode());
            }
        } catch (HttpClientErrorException hce) {
            logger.error("{} error releasing transaction. Response body: {}", hce.getStatusCode(), hce.getResponseBodyAsString());
            throw new RuntimeException("Client error requesting transaction detail");
        } catch (JsonProcessingException e) {
            logger.error("Unable to parse instruction response", e);
            throw new RuntimeException(e);
        }
    }

    public HashMap<String, Object> refund(Transaction tx) {

        final TransactionRequestDTO requestDTO = new TransactionRequestDTO(tx);

        final HttpEntity<TransactionRequestDTO> httpEntity = createRequest(requestDTO);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(transactionURI, httpEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                logger.debug("Release instruction response: {} with {}", response.getStatusCode(), response.getBody());

                return mapper.readValue(response.getBody(), MAP_TYPE);
            } else {
                throw new RuntimeException("Transaction request was unsuccessful: " + response.getStatusCode());
            }
        } catch (HttpClientErrorException hce) {
            logger.error("{} error refunding transaction. Response body: {}", hce.getStatusCode(), hce.getResponseBodyAsString());
            if (hce.getResponseBodyAsString().contains("code") && hce.getResponseBodyAsString().contains("description")) {
                try {
                    return mapper.readValue(hce.getResponseBodyAsString(), MAP_TYPE);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            } else {
                throw new RuntimeException("Client error refunding transaction");
            }
        } catch (JsonProcessingException e) {
            logger.error("Unable to parse instruction response", e);
            throw new RuntimeException(e);
        }
    }
}
