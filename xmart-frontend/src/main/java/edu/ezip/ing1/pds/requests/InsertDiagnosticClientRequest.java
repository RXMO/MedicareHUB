package edu.ezip.ing1.pds.requests;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ezip.ing1.pds.business.dto.DiagnosticResult;
import edu.ezip.ing1.pds.client.commons.ClientRequest;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.commons.Request;

//insère un diagnostic dans la table patients_diagnostics
public class InsertDiagnosticClientRequest extends ClientRequest<DiagnosticResult, String> {

    public InsertDiagnosticClientRequest(
            NetworkConfig networkConfig, int myBirthDate, Request request, DiagnosticResult diagnosis, byte[] bytes)
            throws IOException {
        super(networkConfig, myBirthDate, request, diagnosis, bytes);
    }

    @Override
    public String readResult(String body) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        final Map<String, String> responseMap = mapper.readValue(body, new TypeReference<Map<String, String>>() {});
        return responseMap.get("message");
    }
}