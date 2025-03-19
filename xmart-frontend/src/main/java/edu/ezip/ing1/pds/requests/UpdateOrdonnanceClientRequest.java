package edu.ezip.ing1.pds.requests;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ezip.ing1.pds.business.dto.Ordonnance;
import edu.ezip.ing1.pds.client.commons.ClientRequest;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.commons.Request;

// Définir les paramètres génériques <Ordonnance, String>
public class UpdateOrdonnanceClientRequest extends ClientRequest<Ordonnance, String> {

    private final static String LoggingLabel = "Client - UpdateOrdonnanceRequest";
    private final static Logger logger = LoggerFactory.getLogger(LoggingLabel);

    public UpdateOrdonnanceClientRequest(NetworkConfig networkConfig, int sleepDuration,
            Request request, Ordonnance info, byte[] requestBytes) throws IOException {
        super(networkConfig, sleepDuration, request, info, requestBytes);
    }

    @Override
    public void run() {
        try {
            // Utilisez getInfo() au lieu d'accéder directement à info
            Ordonnance ordonnance = getInfo();
            logger.info("Mise à jour de l'ordonnance avec id {}", ordonnance.getIdOrdonnance());
            super.run();
            logger.info("Ordonnance mise à jour avec succès");
        } catch (Exception e) {
            logger.error("Erreur lors de la mise à jour de l'ordonnance: {}", e.getMessage());
        }
    }
    
    @Override
    public String readResult(final String body) throws IOException {
        // Traiter le résultat si nécessaire
        logger.info("Résultat de la mise à jour: {}", body);
        return body; // Retourne le résultat qui sera stocké dans result
    }
}