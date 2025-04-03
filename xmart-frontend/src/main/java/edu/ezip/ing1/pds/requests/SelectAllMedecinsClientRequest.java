package edu.ezip.ing1.pds.requests;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.ezip.ing1.pds.business.dto.Medecins;
import edu.ezip.ing1.pds.client.commons.ClientRequest;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.commons.Request;

import java.io.IOException;

public class SelectAllMedecinsClientRequest extends ClientRequest<Object, Medecins> {

    public SelectAllMedecinsClientRequest(
            NetworkConfig networkConfig, int myBirthDate, Request request, Object info, byte[] bytes)
            throws IOException {
        super(networkConfig, myBirthDate, request, info, bytes);
    }

    @Override
    public Medecins readResult(String body) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        final Medecins medecins = mapper.readValue(body, Medecins.class);
        return medecins;
    }
}
