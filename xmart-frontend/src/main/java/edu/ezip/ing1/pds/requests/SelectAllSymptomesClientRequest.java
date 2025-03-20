package edu.ezip.ing1.pds.requests;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ezip.ing1.pds.business.dto.Symptomes;
import edu.ezip.ing1.pds.client.commons.ClientRequest;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.commons.Request;

public class SelectAllSymptomesClientRequest extends ClientRequest<Object, List<Symptomes>> {

    public SelectAllSymptomesClientRequest(NetworkConfig networkConfig, int myBirthDate, Request request, Object info, byte[] bytes) throws IOException {
        super(networkConfig, myBirthDate, request, info, bytes);
    }

    @Override
    public List<Symptomes> readResult(String body) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(body, new TypeReference<List<Symptomes>>() {});
    }
}
