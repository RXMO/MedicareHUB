package edu.ezip.ing1.pds.requests;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ezip.ing1.pds.client.commons.ClientRequest;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.commons.Request;


public class CheckSymptomeUtiliseClientRequest extends ClientRequest<Integer, Boolean> {

    public CheckSymptomeUtiliseClientRequest(
            NetworkConfig networkConfig, int myBirthDate, Request request, Integer idSymptome, byte[] bytes)
            throws IOException {
        super(networkConfig, myBirthDate, request, idSymptome, bytes);
    }

    @Override
    public Boolean readResult(String body) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(body, Boolean.class);
        } catch (Exception ex) {
            System.err.println("Impossible de parser la réponse: " + body);
            return true;
        }
    }
}