package edu.ezip.ing1.pds.requests;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ezip.ing1.pds.business.dto.Symptomes;
import edu.ezip.ing1.pds.client.commons.ClientRequest;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.commons.Request;

// AJOUTER UN NOUVEAU SYMPTÔME DANS LA BASE DE DONNÉES
public class InsertSymptomeClientRequest extends ClientRequest<Symptomes, Symptomes> {

    public InsertSymptomeClientRequest(
            NetworkConfig networkConfig, int myBirthDate, Request request, Symptomes symptome, byte[] bytes)
            throws IOException {
        super(networkConfig, myBirthDate, request, symptome, bytes);
    }

    @Override
    public Symptomes readResult(String body) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        try {
            // CONVERSION DE LA RÉPONSE JSON EN OBJET SYMPTÔME
            return mapper.readValue(body, Symptomes.class);
        } catch (Exception e) {
            // GESTION DES ERREURS DE PARSING
            System.err.println("Erreur lors du parsing de la réponse: " + body);
            return new Symptomes(); // RETOUR D'UN OBJET VIDE EN CAS D'ERREUR
        }
    }
}