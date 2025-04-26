package edu.ezip.ing1.pds.requests;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ezip.ing1.pds.business.dto.Symptomes;
import edu.ezip.ing1.pds.client.commons.ClientRequest;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.commons.Request;

public class DeleteSymptomeClientRequest extends ClientRequest<Symptomes, String> {

    public DeleteSymptomeClientRequest(
            NetworkConfig networkConfig, int myBirthDate, Request request, Symptomes symptome, byte[] bytes)
            throws IOException {
        super(networkConfig, myBirthDate, request, symptome, bytes);
    }

    @Override
    public String readResult(String body) throws IOException {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> responseMap = mapper.readValue(body, Map.class);
            
            if (responseMap.containsKey("message")) {
                return responseMap.get("message").toString();
            } else {
                return body;
            }
        } catch (Exception e) {
            return body;
        }
    }
}