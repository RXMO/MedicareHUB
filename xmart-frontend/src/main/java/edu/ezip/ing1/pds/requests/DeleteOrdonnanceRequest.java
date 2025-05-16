package edu.ezip.ing1.pds.requests;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ezip.ing1.pds.business.dto.Ordonnance;
import edu.ezip.ing1.pds.client.commons.ClientRequest;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.commons.Request;

public class DeleteOrdonnanceRequest extends ClientRequest<Ordonnance, String> {
    
    public DeleteOrdonnanceRequest(
            NetworkConfig networkConfig, int myBirthDate, Request request, Ordonnance ordonnance, byte[] bytes)
            throws IOException {
        super(networkConfig, myBirthDate, request, ordonnance, bytes);
    }
    
    @Override
public String readResult(String body) throws IOException {
    try {
        final ObjectMapper mapper = new ObjectMapper();
        final Map<String, String> responseMap = mapper.readValue(body, Map.class);
        return responseMap.get("response_body");
    } catch (IOException e) {
        // En cas d'erreur de parsing JSON, essayons d'extraire manuellement la réponse
        System.out.println("Erreur de parsing JSON, tentative d'extraction manuelle: " + body);
        
        // Méthode simple pour extraire le message de réponse
        int start = body.indexOf("response_body") + "response_body".length();
        int end = body.lastIndexOf("}");
        
        if (start > 0 && end > start) {
            String extracted = body.substring(start, end).trim();
            // Enlever les premiers caractères comme ":" et les espaces
            extracted = extracted.replaceAll("^[:\\s]+", "");
            return extracted;
        }
        
        // Si l'extraction manuelle échoue aussi, retournons le corps complet
        return "Erreur de parsing avec le message: " + body;
    }
}
}