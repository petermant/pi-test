package org.example.client;

import org.example.client.dtos.transaction.TransactionRequestDTO;
import org.example.client.dtos.transaction.TransactionResponseDTO;
import org.example.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class TransactionClient extends AbstractOpayoClient {

    @Autowired private RestTemplate restTemplate;

    @Value("${opayo.uri.transaction}") private String transactionURI;

    public TransactionResponseDTO requestTransaction(Transaction t) {
        final TransactionRequestDTO requestDTO = new TransactionRequestDTO(t);

        final HttpEntity<TransactionRequestDTO> httpEntity = createRequest(requestDTO);

        ResponseEntity<TransactionResponseDTO> response = restTemplate.postForEntity(transactionURI, httpEntity, TransactionResponseDTO.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            logger.debug("Transaction request response: {} with {}", response.getStatusCode());

            return response.getBody();
        } else {
            logger.error("{} error requesting payment transaction. Http response object: {}", response.getStatusCode(), response);
            // todo think about error pathway here - something more graceful...
            throw new RuntimeException("Error requesting transaction");
        }
    }
}
