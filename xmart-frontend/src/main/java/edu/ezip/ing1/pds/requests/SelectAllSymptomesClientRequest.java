package edu.ezip.ing1.pds.requests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ezip.ing1.pds.business.dto.Symptomes;
import edu.ezip.ing1.pds.client.commons.ClientRequest;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.commons.Request;

// RÉCUPÉRER TOUS LES SYMPTÔMES DISPONIBLES DANS LE SYSTÈME
public class SelectAllSymptomesClientRequest extends ClientRequest<Object, List<Symptomes>> {

    public SelectAllSymptomesClientRequest(NetworkConfig networkConfig, int myBirthDate, Request request, Object info, byte[] bytes) throws IOException {
        super(networkConfig, myBirthDate, request, info, bytes);
    }

    @Override
    public List<Symptomes> readResult(String body) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode node = mapper.readTree(body);
            
            // VÉRIFICATION DU TYPE DE RÉPONSE
            if (node.isArray()) {
                // CONVERSION DE LA LISTE DE SYMPTÔMES
                return mapper.readValue(body, new TypeReference<List<Symptomes>>() {});
            } else if (node.has("message")) {
                // AUCUN SYMPTÔME TROUVÉ
                System.out.println("Aucun symptôme trouvé: " + node.get("message").asText());
                return new ArrayList<>();
            } else {
                // FORMAT INCONNU
                throw new IOException("Format de réponse inattendu: " + body);
            }
        } catch (IOException e) {
            System.out.println("Erreur lors du parsing de la réponse: " + body + " - " + e.getMessage());
            throw e; 
        }
    }
}