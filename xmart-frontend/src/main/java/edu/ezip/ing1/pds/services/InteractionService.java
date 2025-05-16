package edu.ezip.ing1.pds.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import edu.ezip.commons.LoggingUtils;
import edu.ezip.ing1.pds.client.commons.ClientRequest;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.requests.CheckInteractionClientRequest;

public class InteractionService {
    
    private static final String LoggingLabel = "FrontEnd - InteractionService";
    private static final Logger logger = LoggerFactory.getLogger(LoggingLabel);
    
    private static final String checkInteractionsOrder = "VERIFIER_INTERACTIONS_MEDICAMENTEUSES";
    private final NetworkConfig networkConfig;
    
    public InteractionService(NetworkConfig networkConfig) {
        this.networkConfig = networkConfig;
    }
    
    public List<String> checkInteractions(List<String> medicaments) throws InterruptedException, IOException {
        logger.debug("Vérification des interactions pour les médicaments : {}", medicaments);
        
        // Nettoyage de la liste
        List<String> cleanedMedicaments = medicaments.stream()
                .filter(med -> med != null && !med.trim().isEmpty())
                .map(String::trim)
                .toList();
        
        logger.debug("Médicaments nettoyés : {}", cleanedMedicaments);
        
        // Préparation de la requête
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(SerializationFeature.WRAP_ROOT_VALUE);
        
        final String requestId = UUID.randomUUID().toString();
        
        // Création d'un objet Map pour respecter la structure attendue par le serveur
        Map<String, Object> rootMap = new HashMap<>();
        Map<String, Object> requestMap = new HashMap<>();
        
        requestMap.put("request_id", requestId);
        requestMap.put("request_order", checkInteractionsOrder);
        requestMap.put("request_body", cleanedMedicaments);
        
        // Ajout de la structure attendue avec l'élément racine "request"
        rootMap.put("request", requestMap);
        
        // Sérialisation de la structure complète
        final byte[] requestBytes = objectMapper.writeValueAsBytes(rootMap);
        LoggingUtils.logDataMultiLine(logger, Level.TRACE, requestBytes);
        
        // Création de la requête
        CheckInteractionClientRequest clientRequest = null;
        
        try {
            // Création et exécution de la requête
            clientRequest = new CheckInteractionClientRequest(
                    networkConfig, 0, null, cleanedMedicaments, requestBytes
            );
            
            logger.debug("Thread {} lancé, en attente de la réponse...", clientRequest.getThreadName());
            clientRequest.join();
            logger.debug("Thread {} terminé : {}", clientRequest.getThreadName(), 
                        " - Médicaments: " + String.join(", ", cleanedMedicaments));
            
            // Récupération du résultat
            List<String> result = clientRequest.getResult();
            return processResult(result, cleanedMedicaments);
            
        } catch (JsonParseException e) {
            // Cas spécifique où le parser JSON échoue (format incorrect)
            logger.warn("Erreur de parsing JSON: {}", e.getMessage());
            
            // Si nous avons une référence à la requête client, essayons de récupérer la réponse brute
            if (clientRequest != null) {
                try {
                    // On accède au champ privé responseBody via la réflexion
                    java.lang.reflect.Field responseBodyField = ClientRequest.class.getDeclaredField("responseBody");
                    responseBodyField.setAccessible(true);
                    String responseBody = (String) responseBodyField.get(clientRequest);
                    
                    if (responseBody != null) {
                        // Traitement du message d'erreur brut
                        List<String> errorMessages = extractErrorMessages(responseBody);
                        if (!errorMessages.isEmpty()) {
                            return errorMessages;
                        }
                    }
                } catch (Exception reflectionEx) {
                    logger.error("Impossible d'accéder à la réponse brute: {}", reflectionEx.getMessage());
                }
            }
            
            // Fallback: création d'un message d'erreur générique
            List<String> errorList = new ArrayList<>();
            errorList.add("Erreur lors du traitement de la réponse: " + e.getMessage());
            return errorList;
        } catch (Exception e) {
            // Gestion des autres exceptions
            logger.error("Erreur lors de la vérification des interactions: {}", e.getMessage(), e);
            List<String> errorList = new ArrayList<>();
            errorList.add("Erreur de communication avec le serveur: " + e.getMessage());
            return errorList;
        }
    }
    
    /**
     * Traite le résultat retourné par la requête client
     */
    private List<String> processResult(List<String> result, List<String> medicaments) {
        if (result != null && !result.isEmpty()) {
            // Log les interactions détectées
            logger.info("Interactions détectées : {}", result);
            
            // Vérifier si l'une des interactions contient un message d'erreur d'insertion
            for (String interaction : result) {
                if (interaction.contains("Impossible d'insérer") || interaction.contains("en raison des interactions")) {
                    logger.warn("Détection d'un message d'erreur d'insertion: {}", interaction);
                    // Nettoyer le message pour afficher uniquement les interactions
                    String cleanedMessage = interaction;
                    if (interaction.contains("suivantes:")) {
                        cleanedMessage = interaction.substring(interaction.indexOf("suivantes:") + "suivantes:".length()).trim();
                    }
                    List<String> cleanedResult = new ArrayList<>();
                    cleanedResult.add(cleanedMessage);
                    return cleanedResult;
                }
            }
            
            return result;
        } else {
            logger.warn("Aucune interaction détectée ou réponse vide.");
            logger.info("Médicaments envoyés au backend : {}", medicaments);
            return new ArrayList<>();
        }
    }
    
    /**
     * Extrait les messages d'erreur d'une réponse brute JSON malformée
     */
    private List<String> extractErrorMessages(String responseBody) {
        List<String> messages = new ArrayList<>();
        
        if (responseBody == null || responseBody.isEmpty()) {
            return messages;
        }
        
        // Recherche de motifs connus d'erreur dans la réponse
        if (responseBody.contains("Impossible d'insérer")) {
            int startIndex = responseBody.indexOf("Impossible d'insérer");
            // Recherche de la fin du message
            int endIndex = responseBody.indexOf("}", startIndex);
            if (endIndex == -1) {
                // Si pas d'accolade, chercher la fin de ligne
                endIndex = responseBody.indexOf("\n", startIndex);
            }
            if (endIndex == -1) {
                // Si pas de fin de ligne, prendre tout le reste
                endIndex = responseBody.length();
            }
            
            if (endIndex > startIndex) {
                String errorMessage = responseBody.substring(startIndex, endIndex).trim();
                // Extraction des interactions spécifiques si elles sont présentes
                if (errorMessage.contains("interactions suivantes:")) {
                    String interactionsText = errorMessage.substring(
                        errorMessage.indexOf("interactions suivantes:") + "interactions suivantes:".length()
                    ).trim();
                    messages.add(interactionsText);
                    return messages;
                }
                messages.add(errorMessage);
            }
        } else {
            // Tentative d'extraction d'un message d'erreur générique
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                // Essai de corriger le JSON malformé
                String cleanedResponse = responseBody;
                if (!responseBody.trim().startsWith("{")) {
                    cleanedResponse = "{" + responseBody;
                }
                // Si l'accolade de fin manque
                if (!cleanedResponse.trim().endsWith("}")) {
                    cleanedResponse = cleanedResponse + "}";
                }
                
                Map<String, Object> responseMap = objectMapper.readValue(cleanedResponse, Map.class);
                if (responseMap.containsKey("response_body")) {
                    Object responseBodyObj = responseMap.get("response_body");
                    if (responseBodyObj != null) {
                        messages.add(responseBodyObj.toString());
                    }
                }
            } catch (Exception e) {
                logger.debug("Échec de l'extraction du message d'erreur JSON: {}", e.getMessage());
                // En dernier recours, ajouter un extrait de la réponse brute
                String shortResponse = responseBody.length() > 100 ? 
                    responseBody.substring(0, 100) + "..." : responseBody;
                messages.add("Réponse non traitée: " + shortResponse);
            }
        }
        
        return messages;
    }
}