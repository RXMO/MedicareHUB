package edu.ezip.ing1.pds.requests;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ezip.ing1.pds.client.commons.ClientRequest;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.commons.Request;

// SUPPRIMER L'ASSOCIATION ENTRE UN PATIENT ET UN SYMPTÔME
public class DeletePatientSymptomeClientRequest extends ClientRequest<Map<String, Integer>, String> {

    private final Map<String, Integer> data;

    public DeletePatientSymptomeClientRequest(
            NetworkConfig networkConfig,
            int myId,
            Request request,
            Map<String, Integer> data,
            byte[] requestBytes) throws IOException {
        super(networkConfig, myId, request, data, requestBytes);
        this.data = data;
    }

    @Override
    public String readResult(final String body) throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        try {
            final Map<String, String> responseMap = objectMapper.readValue(body, Map.class);
            return responseMap.get("message"); // EXTRACTION DU MESSAGE DE CONFIRMATION
        } catch (IOException e) {
            System.out.println("Erreur lors du parsing de la réponse: " + body + " - " + e.getMessage());
            throw e; // PROPAGATION DE L'ERREUR
        }
    }
}