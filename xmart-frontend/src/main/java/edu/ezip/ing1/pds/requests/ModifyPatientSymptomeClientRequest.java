package edu.ezip.ing1.pds.requests;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ezip.ing1.pds.business.dto.Symptomes;
import edu.ezip.ing1.pds.client.commons.ClientRequest;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.commons.Request;

// MODIFIER L'ASSOCIATION ENTRE UN PATIENT ET UN SYMPTÔME
public class ModifyPatientSymptomeClientRequest extends ClientRequest<Map<String, Integer>, Symptomes> {

    // STOCKAGE DES DONNÉES DE MODIFICATION
    private final Map<String, Integer> data;

    public ModifyPatientSymptomeClientRequest(
            NetworkConfig networkConfig,
            int myId,
            Request request,
            Map<String, Integer> data,
            byte[] requestBytes) throws IOException {
        super(networkConfig, myId, request, data, requestBytes);
        this.data = data;
    }

    @Override
    public Symptomes readResult(final String body) throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(body, Symptomes.class);
        } catch (IOException e) {
            System.out.println("Erreur lors du parsing de la réponse: " + body + " - " + e.getMessage());
            throw e; 
        }
    }
}