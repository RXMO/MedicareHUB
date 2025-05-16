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
import edu.ezip.ing1.pds.business.dto.Medecin;
import edu.ezip.ing1.pds.business.dto.Medecins;
import edu.ezip.ing1.pds.client.commons.ClientRequest;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.commons.Request;
import edu.ezip.ing1.pds.requests.DeleteMedecinClientRequest;
import edu.ezip.ing1.pds.requests.InsertMedecinClientRequest;
import edu.ezip.ing1.pds.requests.SelectAllMedecinsClientRequest;
import edu.ezip.ing1.pds.requests.UpdateMedecinClientRequest;

public class MedecinService {

    private final static String LoggingLabel = "FrontEnd - MedecinService";
    private final static Logger logger = LoggerFactory.getLogger(LoggingLabel);

    final String insertRequestOrder = "INSERT_MEDECINS";
    final String selectRequestOrder = "SELECT_ALL_MEDECINS";
    final String deleteRequestOrder = "DELETE_MEDECIN";
    final String updateRequestOrder = "UPDATE_MEDECIN";

    private final NetworkConfig networkConfig;

    public MedecinService(NetworkConfig networkConfig) {
        this.networkConfig = networkConfig;
    }

    public void UpdateMedecin(Medecin medecin) throws InterruptedException, IOException {
        processMedecin(medecin, updateRequestOrder);
    }

    public void InsertMedecin(Medecin medecin) throws InterruptedException, IOException {
        processMedecin(medecin, insertRequestOrder);
    }

    public void DeleteMedecin(Medecin medecin) throws InterruptedException, IOException {
        processMedecin(medecin, deleteRequestOrder);
    }

    private void processMedecin(Medecin medecin, String requestOrder) throws InterruptedException, IOException {
        final Deque<ClientRequest> medecinRequests = new ArrayDeque<>();
        final ObjectMapper objectMapper = new ObjectMapper();
        final String jsonifiedMedecin = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(medecin);
        logger.trace("Medecin en JSON : {}", jsonifiedMedecin);

        final String requestId = UUID.randomUUID().toString();
        final Request request = new Request();
        request.setRequestId(requestId);
        request.setRequestOrder(requestOrder);
        request.setRequestContent(jsonifiedMedecin);

        logger.info("Requête envoyée : '{}'", requestOrder); // Vérification avant envoi

        objectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        final byte[] requestBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(request);

        ClientRequest medecinRequest = null;

        if (requestOrder.equals(insertRequestOrder)) {
            medecinRequest = new InsertMedecinClientRequest(networkConfig, 0, request, medecin, requestBytes);
        } else if (requestOrder.equals(deleteRequestOrder)) {
            medecinRequest = new DeleteMedecinClientRequest(networkConfig, 0, request, medecin, requestBytes);
        } else if (requestOrder.equals(updateRequestOrder)) {
            medecinRequest = new UpdateMedecinClientRequest(networkConfig, 0, request, medecin, requestBytes);
        } else {
            logger.error("Requête inconnue : '{}'", requestOrder); // Log en cas d'erreur
            throw new IllegalArgumentException("Requête inconnue : " + requestOrder);
        }

        medecinRequests.push(medecinRequest);

        while (!medecinRequests.isEmpty()) {
            final ClientRequest processedRequest = medecinRequests.pop();
            processedRequest.join();
            final Medecin processedMedecin = (Medecin) processedRequest.getInfo();

            if (processedMedecin != null) {
                logger.debug("Thread {} terminé : {} {} --> {}",
                        processedRequest.getThreadName(),
                        processedMedecin.getNomMedecin(),
                        processedMedecin.getPrenomMedecin(),
                        processedMedecin.getIdMedecin(),
                        processedRequest.getResult());
            } else {
                logger.debug("Thread {} terminé sans médecin.", processedRequest.getThreadName());
            }
        }
    }

    public Medecins selectMedecins() throws InterruptedException, IOException {
        final Deque<ClientRequest> medecinRequests = new ArrayDeque<>();
        final ObjectMapper objectMapper = new ObjectMapper();

        final String requestId = UUID.randomUUID().toString();
        final Request request = new Request();
        request.setRequestId(requestId);
        request.setRequestOrder(selectRequestOrder);

        objectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        final byte[] requestBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(request);
        LoggingUtils.logDataMultiLine(logger, Level.TRACE, requestBytes);

        final SelectAllMedecinsClientRequest medecinRequest = new SelectAllMedecinsClientRequest(
                networkConfig, 0, request, null, requestBytes);
        medecinRequests.push(medecinRequest);

        if (!medecinRequests.isEmpty()) {
            final ClientRequest joinedMedecinRequest = medecinRequests.pop();
            joinedMedecinRequest.join();
            logger.debug("Thread {} terminé.", joinedMedecinRequest.getThreadName());
            return (Medecins) joinedMedecinRequest.getResult();
        } else {
            logger.error("Aucun médecin trouvé");
            return null;
        }
    }
}