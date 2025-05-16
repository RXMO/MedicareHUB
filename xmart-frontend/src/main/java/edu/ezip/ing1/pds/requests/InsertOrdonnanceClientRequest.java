package edu.ezip.ing1.pds.requests;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ezip.ing1.pds.business.dto.Ordonnance;
import edu.ezip.ing1.pds.client.commons.ClientRequest;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.commons.Request;

public class InsertOrdonnanceClientRequest extends ClientRequest<Ordonnance, String> {

    public InsertOrdonnanceClientRequest(
            NetworkConfig networkConfig, int myBirthDate, Request request, Ordonnance ordonnance, byte[] bytes)
            throws IOException {
        super(networkConfig, myBirthDate, request, ordonnance, bytes);
    }

    @Override
    public String readResult(String body) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    Map<String, Object> responseMap = mapper.readValue(body, Map.class);

    if ("error".equals(responseMap.get("status"))) {
        // En cas d'erreur, on peut retourner le message d'erreur (ou un autre champ)
        return (String) responseMap.get("message");
    }

    // Sinon, supposons que tu récupères l'ID ordonnance dans la clé "ordonnance_id"
    if (responseMap.containsKey("ordonnance_id")) {
        return responseMap.get("ordonnance_id").toString();
    }

    // Par défaut, retourne le body brut si aucun cas ne correspond
    return body;
}


}
