package edu.ezip.ing1.pds.requests;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ezip.ing1.pds.business.dto.Symptomes;
import edu.ezip.ing1.pds.client.commons.ClientRequest;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.commons.Request;

//  ASSOCIER UN SYMPTÔME À UN PATIENT
public class InsertPatientSymptomeClientRequest extends ClientRequest<Symptomes, Symptomes> {

    public InsertPatientSymptomeClientRequest(
            NetworkConfig networkConfig,
            int myId,
            Request request,
            Symptomes symptome,
            byte[] requestBytes) throws IOException {
        super(networkConfig, myId, request, symptome, requestBytes);
    }

    @Override
    public Symptomes readResult(final String body) throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        try {
            // CONVERSION DE LA RÉPONSE EN OBJET SYMPTÔME
            return objectMapper.readValue(body, Symptomes.class);
        } catch (IOException e) {
            System.out.println("Erreur lors du parsing de la réponse: " + body + " - " + e.getMessage());
            throw e; //
        }
    }
}