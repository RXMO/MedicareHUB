package edu.ezip.ing1.pds.requests;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ezip.ing1.pds.business.dto.Symptomes;
import edu.ezip.ing1.pds.client.commons.ClientRequest;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.commons.Request;

// METTRE À JOUR UN SYMPTÔME EXISTANT
public class UpdateSymptomeClientRequest extends ClientRequest<Symptomes, String> {

    public UpdateSymptomeClientRequest(
            NetworkConfig networkConfig, int myBirthDate, Request request, Symptomes symptome, byte[] bytes)
            throws IOException {
        super(networkConfig, myBirthDate, request, symptome, bytes);
    }

    @Override
    public String readResult(String body) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        // CONVERSION DE LA RÉPONSE JSON EN MAP
        final Map<String, String> responseMap = mapper.readValue(body, Map.class);
        // EXTRACTION DU MESSAGE DE CONFIRMATION
        return responseMap.get("message");
    }
}