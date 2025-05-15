package edu.ezip.ing1.pds.requests;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ezip.ing1.pds.business.dto.Patient;
import edu.ezip.ing1.pds.client.commons.ClientRequest;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.commons.Request;

// CRÉER UN NOUVEAU PATIENT DANS LE SYSTÈME
public class InsertPatientClientRequest extends ClientRequest<Patient, String> {

    public InsertPatientClientRequest(
            NetworkConfig networkConfig, int myBirthDate, Request request, Patient patient, byte[] bytes)
            throws IOException {
        super(networkConfig, myBirthDate, request, patient, bytes);
    }

    @Override
    public String readResult(String body) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        final Map<String, String> responseMap = mapper.readValue(body, Map.class);
        return responseMap.get("message"); // EXTRACTION DU MESSAGE DE CONFIRMATION
    }
}