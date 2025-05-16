package edu.ezip.ing1.pds.requests;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.ezip.ing1.pds.client.commons.ClientRequest;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.commons.Request;

import java.io.IOException;
import java.util.List;

public class CheckInteractionClientRequest extends ClientRequest<List<String>, List<String>> {

    public CheckInteractionClientRequest(NetworkConfig networkConfig, int priority, Request request,
                                         List<String> medicaments, byte[] requestBytes) throws IOException {
        super(networkConfig, priority, request, medicaments, requestBytes);
    }

    @Override
    public List<String> readResult(String body) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(body, new TypeReference<List<String>>() {});
    }
}
