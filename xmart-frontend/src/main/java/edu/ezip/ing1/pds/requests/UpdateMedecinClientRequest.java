package edu.ezip.ing1.pds.requests;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.ezip.ing1.pds.business.dto.Medecin;
import edu.ezip.ing1.pds.client.commons.ClientRequest;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.commons.Request;

import java.io.IOException;
import java.util.Map;

public class UpdateMedecinClientRequest extends ClientRequest<Medecin, String> {

    public UpdateMedecinClientRequest(
            NetworkConfig networkConfig, int myBirthDate, Request request, Medecin medecin, byte[] bytes)
            throws IOException {
        super(networkConfig, myBirthDate, request, medecin, bytes);
    }

    @Override
    public String readResult(String body) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        final Map<String, String> responseMap = mapper.readValue(body, Map.class);
        return responseMap.get("message"); // Assurez-vous que le serveur renvoie un message dans la réponse
    }
}