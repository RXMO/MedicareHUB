package edu.ezip.ing1.pds.requests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ezip.ing1.pds.business.dto.Symptomes;
import edu.ezip.ing1.pds.client.commons.ClientRequest;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.commons.Request;

// RÉCUPÉRER LES SYMPTÔMES D'UN PATIENT SPÉCIFIQUE
public class SelectPatientSymptomesClientRequest extends ClientRequest<Integer, List<Symptomes>> {

    public SelectPatientSymptomesClientRequest(
            NetworkConfig networkConfig,
            int myId,
            Request request,
            Integer idPatient,
            byte[] requestBytes) throws IOException {
        super(networkConfig, myId, request, idPatient, requestBytes);
    }

    @Override
    public List<Symptomes> readResult(final String body) throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode node = objectMapper.readTree(body);
            
            // VÉRIFICATION DU TYPE DE RÉPONSE
            if (node.isArray()) {
                // CONVERSION DE LA LISTE DE SYMPTÔMES DU PATIENT
                return objectMapper.readValue(body, objectMapper.getTypeFactory().constructCollectionType(List.class, Symptomes.class));
            } else if (node.has("message")) {
                // AUCUN SYMPTÔME TROUVÉ POUR CE PATIENT
                System.out.println("Aucun symptôme trouvé: " + node.get("message").asText());
                return new ArrayList<>();
            } else {
                throw new IOException("Format de réponse inattendu: " + body);
            }
        } catch (IOException e) {
            System.out.println("Erreur lors du parsing de la réponse: " + body + " - " + e.getMessage());
            throw e; 
        }
    }
}