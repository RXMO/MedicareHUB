package edu.ezip.ing1.pds.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import edu.ezip.commons.LoggingUtils;
import edu.ezip.commons.connectionpool.config.DatabaseConnectionBasicConfiguration;
import edu.ezip.commons.connectionpool.config.impl.ConnectionPoolImpl;
import edu.ezip.ing1.pds.business.dto.Medicament;
import edu.ezip.ing1.pds.business.dto.Ordonnance;
import edu.ezip.ing1.pds.business.dto.Prescription; // Importer la classe Prescription
import edu.ezip.ing1.pds.client.commons.ClientRequest;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.commons.Request;
import edu.ezip.ing1.pds.requests.InsertMedicamentClientRequest; // Assurez-vous d'importer la nouvelle classe
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.UUID;

public class MedicamentService {
    private final static String LoggingLabel = "FrontEnd - MedicamentService";
    private final static Logger logger = LoggerFactory.getLogger(LoggingLabel);
    private final String insertRequestOrder = "INSERT_MEDICAMENT"; 
    private final NetworkConfig networkConfig;

    public MedicamentService(NetworkConfig networkConfig) {
        this.networkConfig = networkConfig;
    }
    public List<Medicament> selectMedicaments() {
    List<Medicament> medicaments = new ArrayList<>();
    ConnectionPoolImpl pool = null;
    Connection connection = null;
    
    try {
        
        pool = ConnectionPoolImpl.getInstance("mysql");
        connection = pool.get();

        if (connection == null) {
            logger.error("Aucune connexion disponible !");
            return medicaments;
        }

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM medicament")) {

            while (resultSet.next()) {
                Medicament medicament = new Medicament();
                medicament.setIdMedicament(resultSet.getInt("id_medicament"));
                medicament.setNomMedicament(resultSet.getString("nom_medicament"));
                medicaments.add(medicament);
            }
            logger.info("Médicaments récupérés : " + medicaments.size());
        }
    } catch (SQLException | UnsupportedEncodingException e) {
        logger.error("Erreur lors de la récupération des médicaments", e);
    } finally {
        
        if (pool != null && connection != null) {
            try {
                pool.release(connection);
            } catch (InterruptedException e) {
                logger.error("Erreur lors de la libération de la connexion", e);
            }
        }
    }
    return medicaments;
}



    public void insertMedicament(Medicament medicament, Ordonnance ordonnance, String posologie) throws InterruptedException, IOException {
        final Deque<ClientRequest> clientRequests = new ArrayDeque<ClientRequest>();
        final ObjectMapper objectMapper = new ObjectMapper();
        final String requestId = UUID.randomUUID().toString();
        final Request request = new Request();
        request.setRequestId(requestId);
        request.setRequestOrder(insertRequestOrder.trim());
        objectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        
        
        Prescription prescription = new Prescription(ordonnance, medicament, posologie);

        final byte[] requestBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(request);
        LoggingUtils.logDataMultiLine(logger, Level.TRACE, requestBytes);
        
        final int myBirthDate = 12345;
        final InsertMedicamentClientRequest clientRequest = new InsertMedicamentClientRequest(
                networkConfig, myBirthDate, request, prescription, requestBytes);

        clientRequests.push(clientRequest);

        if (!clientRequests.isEmpty()) {
            final ClientRequest joinedClientRequest = clientRequests.pop();
            joinedClientRequest.join();
            logger.debug("Thread {} complete: Medicament inserted.", joinedClientRequest.getThreadName());
        } else {
            logger.error("Erreur lors de l'insertion du médicament");
        }
    }
}
