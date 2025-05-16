package edu.ezip.ing1.pds.requests;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ezip.ing1.pds.business.dto.RendezVousData;
import edu.ezip.ing1.pds.client.commons.ClientRequest;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.commons.Request;

// CRÉER UN NOUVEAU RENDEZ-VOUS MÉDICAL
public class InsertRendezVousClientRequest extends ClientRequest<RendezVousData, String> {

    private final RendezVousData rendezVousData;

    public InsertRendezVousClientRequest(
            NetworkConfig networkConfig,
            int myId,
            Request request,
            RendezVousData rendezVousData,
            byte[] requestBytes) throws IOException {
        super(networkConfig, myId, request, rendezVousData, requestBytes);
        this.rendezVousData = rendezVousData;
    }

    @Override
    public String readResult(final String body) throws IOException {
        if (body == null || body.trim().isEmpty()) {
            System.out.println("Le serveur n'a rien répondu pour le rendez-vous du patient " + rendezVousData.getIdPatient());
            return "Erreur : Le serveur n'a rien répondu.";
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(body);
            String message = jsonNode.get("message").asText();
            
            if (message == null || message.isEmpty()) {
                System.out.println("Le serveur a répondu, mais il n'y a pas de message clair : " + body);
                return "Erreur : Réponse du serveur sans message clair.";
            }
            return message; // RETOUR DU MESSAGE DE CONFIRMATION
        } catch (IOException e) {
            System.out.println("Problème pour lire la réponse du serveur : " + body + " - Erreur : " + e.getMessage());
            return "Erreur : Impossible de lire la réponse du serveur.";
        }
    }
}