package edu.ezip.ing1.pds.requests;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ezip.ing1.pds.client.commons.ClientRequest;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.commons.Request;

public class RechercherMaladiesParSymptomeClientRequest extends ClientRequest<String, List<String>> {

    private List<String> maladies;

    public RechercherMaladiesParSymptomeClientRequest(NetworkConfig networkConfig, int priority,
                                                      Request request, String symptomeNom, byte[] requestBytes) throws IOException {
        super(networkConfig, priority, request, symptomeNom, requestBytes);
    }

    @Override
    public List<String> readResult(String body) throws IOException {
        if (body.startsWith("Aucune")) {
            return List.of();
        } else {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(body, new TypeReference<List<String>>() {});
        }
    }
}
