package edu.ezip.ing1.pds.business.server;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ezip.ing1.pds.business.dto.Medicament;
import edu.ezip.ing1.pds.business.dto.Medicaments;
import edu.ezip.ing1.pds.business.dto.Ordonnance;
import edu.ezip.ing1.pds.business.dto.Ordonnances;
import edu.ezip.ing1.pds.business.dto.Patient;
import edu.ezip.ing1.pds.business.dto.Patients;
import edu.ezip.ing1.pds.business.dto.Prescription;
import edu.ezip.ing1.pds.business.dto.Symptomes;
import edu.ezip.ing1.pds.commons.Request;
import edu.ezip.ing1.pds.commons.Response;

public class XMartCityService {

    private final static String LoggingLabel = "B u s i n e s s - S e r v e r";
    private final Logger logger = LoggerFactory.getLogger(LoggingLabel);

    private enum Queries {
        SELECT_ALL_PATIENTS("SELECT id_patient, nom_patient, prenom_patient, num_tel, allergies FROM Patients "),
        INSERT_PATIENT(
                "INSERT INTO Patients (id_patient,nom_patient, prenom_patient, num_tel, allergies) VALUES (?,?, ?, ?, ?)"),
        DELETE_PATIENT("DELETE FROM Patients WHERE id_patient = ?"),
        SELECT_ALL_ORDONNANCES("SELECT * FROM ordonnance ORDER BY id_ordonnance DESC"),
        INSERT_ORDONNANCE(
                "INSERT INTO ordonnance (description, id_patient, id_medecin, id_consultation) VALUES (?, ?, ?, ?)"),
        DELETE_ORDONNANCE("DELETE FROM ordonnance WHERE id_ordonnance = ?"),
        INSERT_PRESCRIPTION("INSERT INTO Prescription (id_ordonnance, id_medicament, posologie) VALUES (?, ?, ?)"),
        SELECT_ALL_MEDICAMENTS("SELECT id_medicament, nom_medicament, principe_actif FROM medicament"),
        SELECT_PRESCRIPTION_PAR_ORDONNANCE(
                "SELECT m.id_medicament, m.nom_medicament, p.posologie FROM Prescription p " +
                        "JOIN medicament m ON p.id_medicament = m.id_medicament " +
                        "WHERE p.id_ordonnance = ?"),
                        SELECT_ALL_SYMPTOMES("SELECT id_symptome, description FROM symptomes"),
INSERT_SYMPTOME("INSERT INTO symptomes (description) VALUES (?)"),
DELETE_SYMPTOME("DELETE FROM symptomes WHERE description = ?"),
UPDATE_SYMPTOME("UPDATE symptomes SET description = ? WHERE description = ?"),
RECHERCHER_MALADIES_PAR_SYMPTOME(
        "SELECT DISTINCT maladies.nom FROM maladies " +
                "JOIN symptomes_maladies ON maladies.id = symptomes_maladies.id_maladie " +
                "JOIN symptomes ON symptomes_maladies.id_symptome = symptomes.id_symptome " +
                "WHERE symptomes.description = ?"
),
DIAGNOSTIC_PATIENT(
"SELECT DISTINCT maladies.nom FROM maladies " +
"JOIN symptomes_maladies ON maladies.id = symptomes_maladies.id_maladie " +
"JOIN symptomes ON symptomes_maladies.id_symptome = symptomes.id_symptome " +
"JOIN patients_symptomes ON symptomes.id_symptome = patients_symptomes.id_symptome " +
"WHERE patients_symptomes.id_patient = ?"
    );


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
            case SELECT_ALL_ORDONNANCES:
                response = SelectAllOrdonnances(request, connection);
                break;
            case INSERT_ORDONNANCE:
                response = InsertOrdonnance(request, connection);
                break;
            case DELETE_ORDONNANCE:
                response = DeleteOrdonnance(request, connection);
                break;
            case SELECT_ALL_MEDICAMENTS:
                response = SelectAllMedicaments(request, connection);
                break;
                case SELECT_ALL_SYMPTOMES:
                response = SelectAllSymptomes(request, connection);
                break;
            case INSERT_SYMPTOME:
                response = InsertSymptome(request, connection);
                break;
            case DELETE_SYMPTOME:
                response = DeleteSymptome(request, connection);
                break;
            case UPDATE_SYMPTOME:
                response = UpdateSymptome(request, connection);
                break;
            case DIAGNOSTIC_PATIENT:
                response = DiagnostiquerPatient(request, connection);
                break;
            case RECHERCHER_MALADIES_PAR_SYMPTOME:
                response = rechercherMaladiesParSymptome(request, connection);
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

        String nomPatient = requestData.getNomPatient();
        String prenomPatient = requestData.getPrenomPatient();
        String numTel = requestData.getNumTel();
        String allergies = requestData.getAllergies();
        int idPatient = requestData.getIdPatient();

        try (PreparedStatement pstmt = connection.prepareStatement(Queries.INSERT_PATIENT.query)) {
            pstmt.setInt(1, idPatient);
            pstmt.setString(2, nomPatient);
            pstmt.setString(3, prenomPatient);
            pstmt.setString(4, numTel);
            pstmt.setString(5, allergies);

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
                patient.setIdPatient(res.getInt("id_patient"));
                patient.setNomPatient(res.getString("nom_patient"));
                patient.setPrenomPatient(res.getString("prenom_patient"));
                patient.setNumTel(res.getString("num_tel"));
                patient.setAllergies(res.getString("allergies"));
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

        int idPatient = requestData.getIdPatient();

        try (PreparedStatement pstmt = connection.prepareStatement(Queries.DELETE_PATIENT.query)) {
            pstmt.setInt(1, idPatient);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                return new Response(request.getRequestId(), "{\"message\": \"Patient supprimé avec succès\"}");
            } else {
                return new Response(request.getRequestId(), "{\"message\": \"Aucun patient trouvé à supprimer\"}");
            }
        }
    }

    private Response SelectAllOrdonnances(final Request request, final Connection connection)
            throws SQLException, JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();

        // Log pour vérifier la requête SQL
        System.out.println(" Exécution de la requête : " + Queries.SELECT_ALL_ORDONNANCES.query);

        try (Statement stmt = connection.createStatement();
                ResultSet res = stmt.executeQuery(Queries.SELECT_ALL_ORDONNANCES.query)) {

            Ordonnances ordonnances = new Ordonnances();

            // Log pour vérifier le résultat de la requête
            System.out.println(" Résultat de la requête reçu, traitement du ResultSet...");

            // Vérifie si des ordonnances sont récupérées
            int ordonnancesCount = 0;
            while (res.next()) {
                Ordonnance ordonnance = new Ordonnance();
                ordonnance.setIdOrdonnance(res.getInt("id_ordonnance"));
                ordonnance.setDescription(res.getString("description"));
                ordonnance.setIdPatient(res.getInt("id_patient"));
                ordonnance.setIdMedecin(res.getInt("id_medecin"));
                ordonnance.setIdConsultation(res.getInt("id_consultation"));

                ordonnances.add(ordonnance);
                ordonnancesCount++;
            }

            // Log pour vérifier combien d'ordonnances ont été récupérées
            System.out.println(" Nombre d'ordonnances récupérées : " + ordonnancesCount);

            // Si aucune ordonnance n'est trouvée
            if (ordonnancesCount == 0) {
                System.out.println(" Aucune ordonnance trouvée dans le ResultSet.");
            }

            // Log pour afficher les ordonnances récupérées
            System.out.println(" Ordonnances récupérées : " + objectMapper.writeValueAsString(ordonnances));

            // Retourne la réponse avec un message en fonction de l'existence d'ordonnances
            return new Response(request.getRequestId(),
                    ordonnances.getOrdonnances().isEmpty() ? "Aucune ordonnance trouvée"
                            : objectMapper.writeValueAsString(ordonnances));
        }
    }

    private Response DeleteOrdonnance(final Request request, final Connection connection)
            throws SQLException, JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        Ordonnance ordonnance = null;
        try {
            ordonnance = objectMapper.readValue(request.getRequestBody(), Ordonnance.class);
        } catch (IOException e) {
            return new Response(request.getRequestId(), "Erreur lors de la lecture de l'ordonnance");
        }

        try (PreparedStatement pstmt = connection.prepareStatement(Queries.DELETE_ORDONNANCE.query)) {
            pstmt.setInt(1, ordonnance.getIdOrdonnance());
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                return new Response(request.getRequestId(), "Ordonnance supprimée avec succès");
            } else {
                return new Response(request.getRequestId(), "Aucune ordonnance trouvée pour suppression");
            }
        }
    }

    private Response InsertOrdonnance(final Request request, final Connection connection)
            throws SQLException, IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        Ordonnance ordonnance = objectMapper.readValue(request.getRequestBody(), Ordonnance.class);

        try (PreparedStatement pstmt = connection.prepareStatement(Queries.INSERT_ORDONNANCE.query,
                Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, ordonnance.getDescription());
            pstmt.setInt(2, ordonnance.getIdPatient());
            pstmt.setInt(3, ordonnance.getIdMedecin());
            pstmt.setInt(4, ordonnance.getIdConsultation());
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                int idOrdonnance = rs.getInt(1);
                for (Prescription prescription : ordonnance.getPrescriptions()) {
                    try (PreparedStatement pstmtPres = connection.prepareStatement(Queries.INSERT_PRESCRIPTION.query)) {
                        pstmtPres.setInt(1, idOrdonnance);
                        pstmtPres.setInt(2, prescription.getIdMedicament());
                        pstmtPres.setString(3, prescription.getPosologie());
                        pstmtPres.executeUpdate();
                    }
                }
            }

            return new Response(request.getRequestId(), "Ordonnance et prescriptions ajoutées avec succès");
        }
    }

    private Response SelectAllMedicaments(final Request request, final Connection connection)
            throws SQLException, JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        try (Statement stmt = connection.createStatement();
                ResultSet res = stmt.executeQuery(Queries.SELECT_ALL_MEDICAMENTS.query)) {

            Medicaments medicaments = new Medicaments();
            while (res.next()) {
                Medicament medicament = new Medicament();
                medicament.setIdMedicament(res.getInt(1)); // Ajout de l'ID
                medicament.setNomMedicament(res.getString(2)); // Le nom est maintenant à l'index 2
                medicament.setPrincipeActif(res.getString(3)); // Ajout du principe actif
                medicaments.add(medicament);
            }

            if (medicaments.getMedicaments().isEmpty()) {
                return new Response(request.getRequestId(), "Aucun médicament trouvé");
            }

            return new Response(request.getRequestId(), objectMapper.writeValueAsString(medicaments));
        }
    }
    private Response SelectAllSymptomes(final Request request, final Connection connection)
        throws SQLException, JsonProcessingException {
    final ObjectMapper objectMapper = new ObjectMapper();
    try (Statement stmt = connection.createStatement();
         ResultSet res = stmt.executeQuery(Queries.SELECT_ALL_SYMPTOMES.query)) {

        List<Symptomes> symptomes = new ArrayList<>();
        while (res.next()) {
            Symptomes symptome = new Symptomes();
            symptome.setId(res.getInt("id"));
            symptome.setNom(res.getString("nom"));
            symptomes.add(symptome);
        }

        System.out.println("Symptômes récupérés depuis la BD : " + symptomes); 

        return new Response(request.getRequestId(),
                symptomes.isEmpty() ? "Aucun symptôme trouvé"
                        : objectMapper.writeValueAsString(symptomes));
    }
}


private Response InsertSymptome(final Request request, final Connection connection)
        throws SQLException, IOException {
    final ObjectMapper objectMapper = new ObjectMapper();
    Symptomes symptome = objectMapper.readValue(request.getRequestBody(), Symptomes.class);

    try (PreparedStatement pstmt = connection.prepareStatement(Queries.INSERT_SYMPTOME.query)) {
        pstmt.setString(1, symptome.getNom());
        int rowsAffected = pstmt.executeUpdate();

        return rowsAffected > 0
                ? new Response(request.getRequestId(), "{\"message\": \"Symptôme ajouté avec succès\"}")
                : new Response(request.getRequestId(), "{\"message\": \"Échec de l'ajout du symptôme\"}");
    }
}

    private Response DeleteSymptome(final Request request, final Connection connection)
            throws SQLException, IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        Symptomes symptome = objectMapper.readValue(request.getRequestBody(), Symptomes.class);

        try (PreparedStatement pstmt = connection.prepareStatement(Queries.DELETE_SYMPTOME.query)) {
            pstmt.setString(1, symptome.getNom()); 
            int rowsAffected = pstmt.executeUpdate();

            return rowsAffected > 0
                    ? new Response(request.getRequestId(), "{\"message\": \"Symptôme supprimé avec succès\"}")
                    : new Response(request.getRequestId(), "{\"message\": \"Aucun symptôme trouvé à supprimer\"}");
        }
    }


    private Response UpdateSymptome(final Request request, final Connection connection)
            throws SQLException, IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        String[] updateData = objectMapper.readValue(request.getRequestBody(), String[].class); // ✅

        String ancienNom = updateData[0];
        String nouveauNom = updateData[1];

        try (PreparedStatement pstmt = connection.prepareStatement(Queries.UPDATE_SYMPTOME.query)) {
            pstmt.setString(1, nouveauNom);
            pstmt.setString(2, ancienNom); 
            int rowsAffected = pstmt.executeUpdate();

            return rowsAffected > 0
                    ? new Response(request.getRequestId(), "{\"message\": \"Symptôme mis à jour avec succès\"}")
                    : new Response(request.getRequestId(), "{\"message\": \"Aucun symptôme trouvé pour mise à jour\"}");
        }
    }


private Response DiagnostiquerPatient(final Request request, final Connection connection)
        throws SQLException, JsonProcessingException, IOException { 
    final ObjectMapper objectMapper = new ObjectMapper();
    int idPatient = objectMapper.readValue(request.getRequestBody(), Integer.class); 

    try (PreparedStatement pstmt = connection.prepareStatement(Queries.DIAGNOSTIC_PATIENT.query)) {
        pstmt.setInt(1, idPatient);
        ResultSet res = pstmt.executeQuery();

        List<String> maladies = new ArrayList<>();
        while (res.next()) {
            maladies.add(res.getString("nom"));
        }

        return new Response(request.getRequestId(),
                maladies.isEmpty() ? "Aucune maladie trouvée" : objectMapper.writeValueAsString(maladies));
    }
}

    private Response rechercherMaladiesParSymptome(final Request request, final Connection connection)
            throws SQLException, IOException {

        final ObjectMapper objectMapper = new ObjectMapper();
        String symptomeNom = objectMapper.readValue(request.getRequestBody(), String.class);

        List<String> maladies = new ArrayList<>();

        try (PreparedStatement pstmt = connection.prepareStatement(Queries.RECHERCHER_MALADIES_PAR_SYMPTOME.query)) {
            pstmt.setString(1, symptomeNom);
            ResultSet res = pstmt.executeQuery();

            while (res.next()) {
                maladies.add(res.getString("nom"));
            }
        }

        return new Response(request.getRequestId(),
                maladies.isEmpty() ? "Aucune maladie trouvée" : objectMapper.writeValueAsString(maladies));
    }


}
