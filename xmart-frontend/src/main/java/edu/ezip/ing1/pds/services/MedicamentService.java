package edu.ezip.ing1.pds.services;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import edu.ezip.commons.LoggingUtils;
import edu.ezip.ing1.pds.business.dto.Medicament;
import edu.ezip.ing1.pds.business.dto.Medicaments;
import edu.ezip.ing1.pds.business.dto.Ordonnance;
import edu.ezip.ing1.pds.business.dto.Prescription;
import edu.ezip.ing1.pds.client.commons.ClientRequest;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.commons.Request;
import edu.ezip.ing1.pds.requests.InsertMedicamentClientRequest;
import edu.ezip.ing1.pds.requests.SelectAllMedicamentsClientRequest;

public class MedicamentService {

    private final static String LoggingLabel = "FrontEnd - MedicamentService";
    private final static Logger logger = LoggerFactory.getLogger(LoggingLabel);

    final String insertRequestOrder = "INSERT_MEDICAMENT";
    final String selectRequestOrder = "SELECT_ALL_MEDICAMENTS";

    private final NetworkConfig networkConfig;

    public MedicamentService(NetworkConfig networkConfig) {
        this.networkConfig = networkConfig;
    }

    public void insertMedicament(Medicament medicament, Ordonnance ordonnance, String posologie) throws InterruptedException, IOException {
        processMedicament(medicament, ordonnance, posologie, insertRequestOrder);
    }

    private void processMedicament(Medicament medicament, Ordonnance ordonnance, String posologie, String requestOrder) throws InterruptedException, IOException {
        final Deque<ClientRequest> clientRequests = new ArrayDeque<>();
        final ObjectMapper objectMapper = new ObjectMapper();

        final String requestId = UUID.randomUUID().toString();
        final Request request = new Request();
        request.setRequestId(requestId);
        request.setRequestOrder(requestOrder);

        if (insertRequestOrder.equals(requestOrder)) {
            Prescription prescription = new Prescription(ordonnance, medicament, posologie);
            final String jsonifiedPrescription = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(prescription);
            request.setRequestContent(jsonifiedPrescription);
        } else {
            final String jsonifiedMedicament = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(medicament);
            request.setRequestContent(jsonifiedMedicament);
        }

        objectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        final byte[] requestBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(request);
        LoggingUtils.logDataMultiLine(logger, Level.TRACE, requestBytes);

        ClientRequest clientRequest = new InsertMedicamentClientRequest(networkConfig, 0, request, new Prescription(ordonnance, medicament, posologie), requestBytes);

        clientRequests.push(clientRequest);

        while (!clientRequests.isEmpty()) {
            final ClientRequest processedRequest = clientRequests.pop();
            processedRequest.join();
            logger.debug("Thread {} terminé : Medicament {} --> {}",
                    processedRequest.getThreadName(),
                    medicament.getNomMedicament(), processedRequest.getResult());
        }
    }

    
    public Medicaments selectMedicaments() throws InterruptedException, IOException {
    final Deque<ClientRequest> clientRequests = new ArrayDeque<>();
    final ObjectMapper objectMapper = new ObjectMapper();

    final String requestId = UUID.randomUUID().toString();
    final Request request = new Request();
    request.setRequestId(requestId);
    request.setRequestOrder(selectRequestOrder);

    // Ajout du log pour afficher le JSON envoyé
    System.out.println("Requête envoyée : " + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(request));

    objectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
    final byte[] requestBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(request);
    LoggingUtils.logDataMultiLine(logger, Level.TRACE, requestBytes);

    final SelectAllMedicamentsClientRequest clientRequest = new SelectAllMedicamentsClientRequest(
            networkConfig, 0, request, null, requestBytes);
    clientRequests.push(clientRequest);

    if (!clientRequests.isEmpty()) {
        final ClientRequest joinedClientRequest = clientRequests.pop();
        joinedClientRequest.join();
        logger.debug("Thread {} terminé.", joinedClientRequest.getThreadName());
        
        //  Vérifier ce que getResult() retourne
        Object result = joinedClientRequest.getResult();
        System.out.println("Résultat brut récupéré : " + result);
        
        if (result instanceof String) {
            System.out.println("JSON reçu : " + result);
        }

        // Récupération du résultat sous forme de Medicaments
        Medicaments medicaments = (Medicaments) joinedClientRequest.getResult();
        
        if (medicaments != null && !medicaments.getMedicaments().isEmpty()) {
            System.out.println("Nombre de médicaments: " + medicaments.getMedicaments().size());
        for (Medicament med : medicaments.getMedicaments()) {
        System.out.println("Médicament: " + med.getNomMedicament() + 
                           ", ID: " + med.getId() + 
                           ", Principe Actif: [" + med.getPrincipeActif() + "]");
            
        }
        return medicaments; // Retourne l'objet Medicaments
        } else {
            logger.error("Aucun médicament trouvé !");
            return new Medicaments(); // Retourne un objet Medicaments vide
        }
    } else {
        logger.error("Aucun médicament trouvé !");
        return new Medicaments(); // Retourne un objet Medicaments vide
    }
}



}
