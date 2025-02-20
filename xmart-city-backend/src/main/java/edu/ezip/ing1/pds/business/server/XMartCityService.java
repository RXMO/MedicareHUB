package edu.ezip.ing1.pds.business.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.ezip.ing1.pds.business.dto.Patient;
import edu.ezip.ing1.pds.business.dto.Patients;
import edu.ezip.ing1.pds.commons.Request;
import edu.ezip.ing1.pds.commons.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.*;

public class XMartCityService {

    private final static String LoggingLabel = "B u s i n e s s - S e r v e r";
    private final Logger logger = LoggerFactory.getLogger(LoggingLabel);

    private enum Queries {
        SELECT_ALL_PATIENTS("SELECT nom, prenom, age FROM patients "),
        INSERT_PATIENT("INSERT INTO patients (nom, prenom, age) VALUES (?, ?, ?)"),
        DELETE_PATIENT("DELETE FROM patients WHERE nom = ? AND prenom = ?");

        private final String query;

        private Queries(final String query) {
            this.query = query;
        }
    }

    public static XMartCityService inst = null;

    public static final XMartCityService getInstance() {
        if (inst == null) {
            inst = new XMartCityService();
        }
        return inst;
    }

    private XMartCityService() {
    }

    public final Response dispatch(final Request request, final Connection connection)
            throws SQLException, IOException {
        Response response = null;
        final Queries queryEnum = Enum.valueOf(Queries.class, request.getRequestOrder());
        switch (queryEnum) {
            case SELECT_ALL_PATIENTS:
                response = SelectAllPatients(request, connection);
                break;

            case INSERT_PATIENT:
                response = InsertPatient(request, connection);
                break;

            case DELETE_PATIENT:
                response = DeletePatient(request, connection);
                break;

            default:
                break;
        }
        return response;
    }

    private Response InsertPatient(final Request request, final Connection connection)
            throws SQLException, IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        Patient requestData = objectMapper.readValue(request.getRequestBody(),
                Patient.class);

        String nom = requestData.getNom();
        String prenom = requestData.getPrenom();
        int age = requestData.getAge();

        try (PreparedStatement pstmt = connection.prepareStatement(Queries.INSERT_PATIENT.query)) {
            pstmt.setString(1, nom);
            pstmt.setString(2, prenom);
            pstmt.setInt(3, age);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                return new Response(request.getRequestId(), "{\"message\": \"Patient ajouté avec succès\"}");
            } else {
                return new Response(request.getRequestId(), "{\"message\": \"Échec de l'ajout du patient\"}");
            }
        }
    }

    private Response SelectAllPatients(final Request request, final Connection connection)
            throws SQLException, JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        try (Statement stmt = connection.createStatement();
                ResultSet res = stmt.executeQuery(Queries.SELECT_ALL_PATIENTS.query)) {

            Patients patients = new Patients();
            while (res.next()) {
                Patient patient = new Patient();
                patient.setNom(res.getString(1));
                patient.setPrenom(res.getString(2));
                patient.setAge(res.getInt(3));
                patients.add(patient);
            }

            if (patients.getPatients().isEmpty()) {
                return new Response(request.getRequestId(), "Aucun patient trouvé");
            }

            return new Response(request.getRequestId(), objectMapper.writeValueAsString(patients));
        }
    }

    private Response DeletePatient(final Request request, final Connection connection)
            throws SQLException, IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        Patient requestData = objectMapper.readValue(request.getRequestBody(), Patient.class);

        String nom = requestData.getNom();
        String prenom = requestData.getPrenom();

        try (PreparedStatement pstmt = connection.prepareStatement(Queries.DELETE_PATIENT.query)) {
            pstmt.setString(1, nom);
            pstmt.setString(2, prenom);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                return new Response(request.getRequestId(), "{\"message\": \"Patient supprimé avec succès\"}");
            } else {
                return new Response(request.getRequestId(), "{\"message\": \"Aucun patient trouvé à supprimer\"}");
            }
        }
    }

}
