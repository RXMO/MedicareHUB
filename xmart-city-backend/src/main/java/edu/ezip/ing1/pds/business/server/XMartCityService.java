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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ezip.ing1.pds.business.dto.Medecin;
import edu.ezip.ing1.pds.business.dto.Medecins;
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
        SELECT_ALL_MEDECINS("SELECT * FROM medecins "),
        INSERT_MEDECINS(
                "INSERT INTO `medecins`(`id_medecin`, `nom_medecin`, `prenom_medecin`, `num_tel`, `id_specialite`) VALUES (?,?, ?, ?,?)"),
        UPDATE_MEDECIN(
                "UPDATE medecins SET nom_medecin = ?, prenom_medecin = ?,num_tel =?, id_specialite = ? WHERE id_medecin = ?"),
        DELETE_MEDECIN("DELETE FROM medecins WHERE id_medecin = ?"),
        SELECT_ALL_PATIENTS("SELECT id_patient, nom_patient, prenom_patient, num_tel, allergies FROM Patients "),
        INSERT_PATIENT(
                "INSERT INTO Patients (id_patient,nom_patient, prenom_patient, num_tel, allergies) VALUES (?,?, ?, ?, ?)"),
        UPDATE_PATIENT(
                "UPDATE Patients SET nom_patient = ?, prenom_patient = ?, num_tel = ?, allergies = ? WHERE id_patient = ?"),
        DELETE_PATIENT("DELETE FROM Patients WHERE id_patient = ?"),
        
        SELECT_ALL_ORDONNANCES("SELECT * FROM ordonnance ORDER BY id_ordonnance DESC"),
        INSERT_ORDONNANCE(
                "INSERT INTO ordonnance (description, id_patient, id_medecin, id_consultation) VALUES (?, ?, ?, ?)"),
        UPDATE_ORDONNANCE(
                "UPDATE ordonnance SET description = ?, id_patient = ?, id_medecin = ?, id_consultation = ? WHERE id_ordonnance = ?"),
        DELETE_ORDONNANCE("DELETE FROM ordonnance WHERE id_ordonnance = ?"),
        INSERT_PRESCRIPTION("INSERT INTO Prescription (id_ordonnance, id_medicament, posologie) VALUES (?, ?, ?)"),
        SELECT_ALL_MEDICAMENTS("SELECT id_medicament, nom_medicament, principe_actif FROM medicament"),
        SELECT_PRESCRIPTION_PAR_ORDONNANCE("SELECT m.id_medicament, m.nom_medicament, p.posologie FROM Prescription p " +
                        "JOIN medicament m ON p.id_medicament = m.id_medicament " +
                        "WHERE p.id_ordonnance = ?"),
        SELECT_MEDICAMENTS_PAR_ORDONNANCE(
                            "SELECT m.nom_medicament FROM medicament m " +
                            "JOIN Prescription p ON m.id_medicament = p.id_medicament " +
                            "WHERE p.id_ordonnance = ?"),
        VERIFIER_INTERACTIONS_MEDICAMENTEUSES(
                            "SELECT description FROM interaction_medicamenteuse " +
                            "WHERE (id_medicament1 = ? AND id_medicament2 = ?) " +
                            "OR (id_medicament2 = ? AND id_medicament1 = ?)"),
  
        VERIFIER_MEDECIN_CONNEXION("SELECT * FROM medecins WHERE id_medecin = ? AND nom_medecin = ?"),


        // Requêtes pour les symptômes et diagnostic
        SELECT_ALL_SYMPTOMES("SELECT id_symptome, description FROM symptomes"),
        INSERT_SYMPTOME("INSERT INTO symptomes (description) VALUES (?)"),
        DELETE_SYMPTOME("DELETE FROM symptomes WHERE id_symptome = ?"),
        UPDATE_SYMPTOME("UPDATE symptomes SET description = ? WHERE id_symptome = ?"),
        RECHERCHER_MALADIES_PAR_SYMPTOME(
                "SELECT DISTINCT maladies.nom_maladie FROM maladies " +
                        "JOIN symptomes_maladies ON maladies.id_maladie = symptomes_maladies.id_maladie " +
                        "JOIN symptomes ON symptomes_maladies.id_symptome = symptomes.id_symptome " +
                        "WHERE symptomes.description = ?"),
        DIAGNOSTIC_PATIENT(
                "SELECT DISTINCT maladies.nom_maladie FROM maladies " +
                        "JOIN symptomes_maladies ON maladies.id_maladie = symptomes_maladies.id_maladie "
                        + "JOIN symptomes ON symptomes_maladies.id_symptome = symptomes.id_symptome " +
                        "JOIN patients_symptomes ON symptomes.id_symptome = patients_symptomes.id_symptome " +
                        "WHERE patients_symptomes.id_patient = ?");

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
            case SELECT_ALL_MEDECINS:
                response = SelectAllMedecins(request, connection);
                break;
            case INSERT_MEDECINS:
                response = InsertMedecin(request, connection);
                break;
            case UPDATE_MEDECIN:
                response = UpdateMedecin(request, connection);
                break;
            case DELETE_MEDECIN:
                response = DeleteMedecin(request, connection);
                break;
            case SELECT_ALL_PATIENTS:
                response = SelectAllPatients(request, connection);
                break;
            case INSERT_PATIENT:
                response = InsertPatient(request, connection);
                break;
            case UPDATE_PATIENT:
                response = UpdatePatient(request, connection);
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
            case UPDATE_ORDONNANCE:
                response = UpdateOrdonnance(request, connection);
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
            case VERIFIER_INTERACTIONS_MEDICAMENTEUSES:
                response = verifierInteractionsManuelle(request, connection);
                break;
            /*case VERIFIER_MEDECIN_CONNEXION:
                response = verifierConnexionMedecin(request, connection);
                break;*/
    
            
            default:
                break;
        }
        return response;
    }

    private Response SelectAllMedecins(final Request request, final Connection connection)
            throws SQLException, JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        try (Statement stmt = connection.createStatement();
                ResultSet res = stmt.executeQuery(Queries.SELECT_ALL_MEDECINS.query)) {
            Medecins medecins = new Medecins();
            while (res.next()) {
                Medecin medecin = new Medecin();
                medecin.setIdMedecin(res.getInt("id_medecin"));
                medecin.setNomMedecin(res.getString("nom_medecin"));
                medecin.setPrenomMedecin(res.getString("prenom_medecin"));
                medecin.setSpecialite(res.getString("id_specialite"));
                medecin.setNumTel(res.getString("num_tel"));
                medecins.add(medecin);
            }
            return new Response(request.getRequestId(), objectMapper.writeValueAsString(medecins));
        }
    }

    private Response InsertMedecin(final Request request, final Connection connection)
            throws SQLException, IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        Medecin requestData = objectMapper.readValue(request.getRequestBody(), Medecin.class);
        try (PreparedStatement pstmt = connection.prepareStatement(Queries.INSERT_MEDECINS.query)) {
            pstmt.setInt(1, requestData.getIdMedecin());
            pstmt.setString(2, requestData.getNomMedecin());
            pstmt.setString(3, requestData.getPrenomMedecin());
            pstmt.setString(4, requestData.getSpecialite());
            pstmt.setString(5, requestData.getNumTel());
            int rowsAffected = pstmt.executeUpdate();
            return new Response(request.getRequestId(),
                    rowsAffected > 0 ? "Médecin ajouté avec succès" : "Échec de l'ajout du médecin");
        }
    }

    private Response UpdateMedecin(final Request request, final Connection connection)
            throws SQLException, IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        Medecin requestData = objectMapper.readValue(request.getRequestBody(), Medecin.class);
        try (PreparedStatement pstmt = connection.prepareStatement(Queries.UPDATE_MEDECIN.query)) {
            pstmt.setString(1, requestData.getNomMedecin());
            pstmt.setString(2, requestData.getPrenomMedecin());
            pstmt.setString(3, requestData.getSpecialite());
            pstmt.setString(4, requestData.getNumTel());
            pstmt.setInt(5, requestData.getIdMedecin());
            int rowsAffected = pstmt.executeUpdate();
            return new Response(request.getRequestId(),
                    rowsAffected > 0 ? "Médecin mis à jour avec succès" : "Aucun médecin trouvé pour mise à jour");
        }
    }

    private Response DeleteMedecin(final Request request, final Connection connection)
            throws SQLException, IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        Medecin requestData = objectMapper.readValue(request.getRequestBody(), Medecin.class);
        try (PreparedStatement pstmt = connection.prepareStatement(Queries.DELETE_MEDECIN.query)) {
            pstmt.setInt(1, requestData.getIdMedecin());
            int rowsAffected = pstmt.executeUpdate();
            return new Response(request.getRequestId(),
                    rowsAffected > 0 ? "Médecin supprimé avec succès" : "Aucun médecin trouvé à supprimer");
        }
    }

    private Response UpdatePatient(final Request request, final Connection connection)
            throws SQLException, IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        Patient requestData = objectMapper.readValue(request.getRequestBody(), Patient.class);

        String nomPatient = requestData.getNomPatient();
        String prenomPatient = requestData.getPrenomPatient();
        String numTel = requestData.getNumTel();
        String allergies = requestData.getAllergies();
        int idPatient = requestData.getIdPatient();

        try (PreparedStatement pstmt = connection.prepareStatement(Queries.UPDATE_PATIENT.query)) {
            pstmt.setString(1, nomPatient);
            pstmt.setString(2, prenomPatient);
            pstmt.setString(3, numTel);
            pstmt.setString(4, allergies);
            pstmt.setInt(5, idPatient);
            int rowsAffected = pstmt.executeUpdate();
            return new Response(request.getRequestId(),
                    rowsAffected > 0 ? "Patient mis à jour avec succès" : "Échec de la mise à jour du patient");
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
            return new Response(request.getRequestId(), objectMapper.writeValueAsString(patients));
        }
    }

    private Response InsertPatient(final Request request, final Connection connection)
            throws SQLException, IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        Patient requestData = objectMapper.readValue(request.getRequestBody(), Patient.class);
        try (PreparedStatement pstmt = connection.prepareStatement(Queries.INSERT_PATIENT.query)) {
            pstmt.setInt(1, requestData.getIdPatient());
            pstmt.setString(2, requestData.getNomPatient());
            pstmt.setString(3, requestData.getPrenomPatient());
            pstmt.setString(4, requestData.getNumTel());
            pstmt.setString(5, requestData.getAllergies());
            int rowsAffected = pstmt.executeUpdate();
            return new Response(request.getRequestId(),
                    rowsAffected > 0 ? "Patient ajouté avec succès" : "Échec de l'ajout du patient");
        }
    }

    

     private Response DeletePatient(final Request request, final Connection connection)
            throws SQLException, IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        Patient requestData = objectMapper.readValue(request.getRequestBody(), Patient.class);
        try (PreparedStatement pstmt = connection.prepareStatement(Queries.DELETE_PATIENT.query)) {
            pstmt.setInt(1, requestData.getIdPatient());
            int rowsAffected = pstmt.executeUpdate();
            return new Response(request.getRequestId(),
                    rowsAffected > 0 ? "Patient supprimé avec succès" : "Échec de la suppression du patient");
        }
    }


    private Response SelectAllOrdonnances(final Request request, final Connection connection)
            throws SQLException, JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();

        System.out.println(" Exécution de la requête : " + Queries.SELECT_ALL_ORDONNANCES.query);

        try (Statement stmt = connection.createStatement();
                ResultSet res = stmt.executeQuery(Queries.SELECT_ALL_ORDONNANCES.query)) {

            Ordonnances ordonnances = new Ordonnances();

            System.out.println(" Résultat de la requête reçu, traitement du ResultSet...");

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

            System.out.println(" Nombre d'ordonnances récupérées : " + ordonnancesCount);

            if (ordonnancesCount == 0) {
                System.out.println(" Aucune ordonnance trouvée dans le ResultSet.");
            }

            System.out.println(" Ordonnances récupérées : " + objectMapper.writeValueAsString(ordonnances));

            return new Response(request.getRequestId(),
                    ordonnances.getOrdonnances().isEmpty() ? "Aucune ordonnance trouvée"
                            : objectMapper.writeValueAsString(ordonnances));
        }
    }

    
    private Response DeleteOrdonnance(final Request request, final Connection connection)
        throws SQLException, IOException {
            final ObjectMapper objectMapper = new ObjectMapper();
            Ordonnance requestData = objectMapper.readValue(request.getRequestBody(), Ordonnance.class);
    
            try (PreparedStatement pstmt = connection.prepareStatement(Queries.DELETE_ORDONNANCE.query)) {
                pstmt.setInt(1, requestData.getIdOrdonnance());
                int rowsAffected = pstmt.executeUpdate();
    
                String messageJson;
                if (rowsAffected > 0) {
                     messageJson = "{\"message\":\"Ordonnance supprimée avec succès\"}";
                } else {
                    messageJson = "{\"message\":\"Aucune ordonnance trouvée pour suppression\"}";
                }
    
                return new Response(request.getRequestId(), messageJson);
            }

    }

    private Response verifierInteractionsManuelle(final Request request, final Connection connection)
        throws SQLException, IOException {
    final ObjectMapper objectMapper = new ObjectMapper();

    List<String> medicaments = objectMapper.readValue(request.getRequestBody(), new TypeReference<List<String>>() {});

    if (medicaments.size() < 2) {
        return new Response(request.getRequestId(), "{\"error\":\"Au moins deux médicaments sont nécessaires pour vérifier les interactions.\"}");
    }

    List<String> interactions = verifierInteractionsMedicamenteuses(medicaments, connection);

    if (interactions.isEmpty()) {
        return new Response(request.getRequestId(), "{\"message\":\"Aucune interaction détectée.\"}");
    } else {
        // Retourner une liste JSON des interactions 
        String jsonResponse = objectMapper.writeValueAsString(interactions);
        return new Response(request.getRequestId(), jsonResponse);
    }
}


private Response InsertOrdonnance(final Request request, final Connection connection)
        throws SQLException, IOException {
    final ObjectMapper objectMapper = new ObjectMapper();
    Ordonnance ordonnance = objectMapper.readValue(request.getRequestBody(), Ordonnance.class);

    // Vérifier que le médecin existe
    try (PreparedStatement checkMedecinStmt = connection.prepareStatement(
            "SELECT 1 FROM medecins WHERE id_medecin = ?")) {
        checkMedecinStmt.setInt(1, ordonnance.getIdMedecin());
        try (ResultSet rsCheck = checkMedecinStmt.executeQuery()) {
            if (!rsCheck.next()) {
                return new Response(request.getRequestId(), 
                    "{\"error\":\"Le médecin avec l'ID " + ordonnance.getIdMedecin() + " n'existe pas.\"}");
            }
        }
    }

    // Vérifier que le patient existe
    try (PreparedStatement checkPatientStmt = connection.prepareStatement(
            "SELECT 1 FROM Patients WHERE id_patient = ?")) {
        checkPatientStmt.setInt(1, ordonnance.getIdPatient());
        try (ResultSet rsCheckPatient = checkPatientStmt.executeQuery()) {
            if (!rsCheckPatient.next()) {
                return new Response(request.getRequestId(),
                    "{\"error\":\"Le patient avec l'ID " + ordonnance.getIdPatient() + " n'existe pas.\"}");
            }
        }
    }
    //vérifier l'id_consultation est enregistrée dans la bdd 
    try (PreparedStatement checkConsultStmt = connection.prepareStatement(
            "SELECT 1 FROM consultation WHERE id_consultation = ?")) {
        checkConsultStmt.setInt(1, ordonnance.getIdConsultation());
        try (ResultSet rsCheckConsult = checkConsultStmt.executeQuery()) {
            if (!rsCheckConsult.next()) {
                return new Response(request.getRequestId(),
                    "{\"error\":\"La consultation avec l'ID " + ordonnance.getIdConsultation() + " n'existe pas.\"}");
            }
        }
    }

    // Extraire les noms des médicaments
    List<String> medicamentNames = extractMedicamentsFromDescription(ordonnance.getDescription());
    System.out.println("Médicaments extraits de la description: " + String.join(", ", medicamentNames));

    // Vérifier les interactions médicamenteuses
    List<String> interactions = verifierInteractionsMedicamenteuses(medicamentNames, connection);

    if (!interactions.isEmpty()) {
        System.out.println("Interactions détectées: " + String.join(", ", interactions));
        String errorMsg = "{\"error\":\"Impossible d'insérer l'ordonnance en raison des interactions suivantes: " 
                          + String.join(", ", interactions).replace("\"", "\\\"") + "\"}";
        return new Response(request.getRequestId(), errorMsg);
    }

    // Insérer ordonnance + prescriptions
    try (PreparedStatement pstmt = connection.prepareStatement(
            Queries.INSERT_ORDONNANCE.query, Statement.RETURN_GENERATED_KEYS)) {
        pstmt.setString(1, ordonnance.getDescription());
        pstmt.setInt(2, ordonnance.getIdPatient());
        pstmt.setInt(3, ordonnance.getIdMedecin());
        pstmt.setInt(4, ordonnance.getIdConsultation());
        pstmt.executeUpdate();

        try (ResultSet rs = pstmt.getGeneratedKeys()) {
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
        }
    }
    // supprimer a consultation une fois l'insertion est effectuée
    String deleteConsultationSQL = "DELETE FROM consultation WHERE id_consultation = ?";
    try (PreparedStatement deleteStmt = connection.prepareStatement(deleteConsultationSQL)) {
        deleteStmt.setInt(1, ordonnance.getIdConsultation());
        int rowsAffected = deleteStmt.executeUpdate();

       if (rowsAffected == 0) {
        logger.warn("Aucune ligne supprimée : consultation avec ID {} non trouvée", ordonnance.getIdConsultation());
       } else {
        logger.debug("Consultation ID {} supprimée avec succès", ordonnance.getIdConsultation());
       }
    }
    return new Response(request.getRequestId(), "{\"message\":\"Ordonnance et prescriptions ajoutées avec succès\"}");
}

private List<String> extractMedicamentsFromDescription(String description) {
    List<String> medicamentNames = new ArrayList<>();
    if (description != null) {
        int index = description.indexOf("Medicaments:");
        if (index != -1 && index + "Medicaments:".length() < description.length()) {
            String medicamentsStr = description.substring(index + "Medicaments:".length()).trim();
            if (!medicamentsStr.isEmpty()) {
                String[] meds = medicamentsStr.split(",");
                for (String med : meds) {
                    String trimmed = med.trim();
                    if (!trimmed.isEmpty()) {
                        medicamentNames.add(trimmed);
                    }
                }
            }
        }
    }
    return medicamentNames;
}

// Vérifie toutes les interactions entre les médicaments 
private List<String> verifierInteractionsMedicamenteuses(List<String> medicamentNames, Connection connection) 
        throws SQLException {
    List<String> interactions = new ArrayList<>();
    
    if (medicamentNames.size() < 2) {
        return interactions;
    }
    
    for (int i = 0; i < medicamentNames.size(); i++) {
        for (int j = i + 1; j < medicamentNames.size(); j++) {
            String med1 = medicamentNames.get(i);
            String med2 = medicamentNames.get(j);
            
            Integer idMed1 = getMedicamentIdByName(med1, connection);
            Integer idMed2 = getMedicamentIdByName(med2, connection);
            
            System.out.println("Medicament: " + med1 + " -> ID: " + idMed1);
            System.out.println("Medicament: " + med2 + " -> ID: " + idMed2);
            
            if (idMed1 != null && idMed2 != null) {
                System.out.println("Verification interaction entre " + med1 + " (ID: " + idMed1 + ") et " 
                                  + med2 + " (ID: " + idMed2 + ")");
                
                String interaction = checkInteractionInDatabase(idMed1, idMed2, connection);
                if (interaction != null) {
                    interactions.add(med1 + " et " + med2 + ": " + interaction);
                }
            }
        }
    }
    
    return interactions;
}

private Integer getMedicamentIdByName(String nomMedicament, Connection connection) throws SQLException {
    try (PreparedStatement pstmt = connection.prepareStatement(
            "SELECT id_medicament FROM medicament WHERE LOWER(nom_medicament) = LOWER(?)")) {
        pstmt.setString(1, nomMedicament);
        try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("id_medicament");
            }
        }
    }
    return null;
}

private String checkInteractionInDatabase(int idMed1, int idMed2, Connection connection) throws SQLException {
    String query = "SELECT description FROM interaction_medicamenteuse WHERE " +
                  "(id_medicament1 = ? AND id_medicament2 = ?) OR " +
                  "(id_medicament1 = ? AND id_medicament2 = ?)";
    System.out.println("Exécution de la requête: " + query);
    
    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
        pstmt.setInt(1, idMed1);
        pstmt.setInt(2, idMed2);
        pstmt.setInt(3, idMed2);
        pstmt.setInt(4, idMed1);
        
        try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                String description = rs.getString("description");
                System.out.println("Interaction trouvée: " + description);
                return description;
            } else {
                System.out.println("Aucune interaction trouvée entre les médicaments " + idMed1 + " et " + idMed2);
            }
        }
    }
    return null;
}

    private Response UpdateOrdonnance(final Request request, final Connection connection)
            throws SQLException, IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        Ordonnance ordonnance = objectMapper.readValue(request.getRequestBody(), Ordonnance.class);

        try (PreparedStatement pstmt = connection.prepareStatement(Queries.UPDATE_ORDONNANCE.query)) {
            pstmt.setString(1, ordonnance.getDescription());
            pstmt.setInt(2, ordonnance.getIdPatient());
            pstmt.setInt(3, ordonnance.getIdMedecin());
            pstmt.setInt(4, ordonnance.getIdConsultation());
            pstmt.setInt(5, ordonnance.getIdOrdonnance());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                return new Response(request.getRequestId(), "Ordonnance mise a jour avec succes");
            } else {
                return new Response(request.getRequestId(), "Aucune ordonnance trouvee pour mise a jour");
            }
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
                return new Response(request.getRequestId(), "Aucun medicament trouve");
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
                symptome.setId(res.getInt("id_symptome"));
                symptome.setNom(res.getString("description")); 
                symptomes.add(symptome);
            }

            System.out.println("Symptomes récupérés depuis la BD : " + symptomes);

            return new Response(request.getRequestId(),
                    symptomes.isEmpty() ? "Aucun symptome trouve"
                            : objectMapper.writeValueAsString(symptomes));
        }
    }

    private Response InsertSymptome(final Request request, final Connection connection)
        throws SQLException, IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        Symptomes symptome = objectMapper.readValue(request.getRequestBody(), Symptomes.class);

        System.out.println("Tentative d'insertion du symptôme: " + symptome.getNom());

        try (PreparedStatement pstmt = connection.prepareStatement(Queries.INSERT_SYMPTOME.query, 
                Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, symptome.getNom());
            int rowsAffected = pstmt.executeUpdate();

            // Récupérer l'ID généré
            int newId = 0;
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    newId = generatedKeys.getInt(1);
                    symptome.setId(newId);
                    System.out.println("Nouveau symptôme créé avec ID: " + newId);
                }
            }

            System.out.println("Résultat de l'insertion: " + rowsAffected + " ligne(s) affectée(s)");

            // Retourne l'objet symptôme avec l'ID
            return new Response(request.getRequestId(), objectMapper.writeValueAsString(symptome));
        }
    }

    private Response UpdateSymptome(final Request request, final Connection connection)
            throws SQLException, IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        Symptomes symptome = objectMapper.readValue(request.getRequestBody(), Symptomes.class);
        
        System.out.println("Tentative de modification du symptôme avec ID: " + symptome.getId() + " en: " + symptome.getNom());

        try (PreparedStatement pstmt = connection.prepareStatement(Queries.UPDATE_SYMPTOME.query)) {
            pstmt.setString(1, symptome.getNom()); 
            pstmt.setInt(2, symptome.getId());      
            
            int rowsAffected = pstmt.executeUpdate();

            System.out.println("Résultat de la modification: " + rowsAffected + " ligne(s) affectée(s)");

            return new Response(request.getRequestId(), 
                    rowsAffected > 0 
                    ? "{\"message\": \"Symptome mis à jour avec succes\"}"
                    : "{\"message\": \"Aucun symptome trouvé pour mise a jour\"}");
        }
    }

    private Response DeleteSymptome(final Request request, final Connection connection)
            throws SQLException, IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        Symptomes symptome = objectMapper.readValue(request.getRequestBody(), Symptomes.class);

        System.out.println("Tentative de suppression du symptôme avec ID: " + symptome.getId());

        try (PreparedStatement pstmt = connection.prepareStatement(Queries.DELETE_SYMPTOME.query)) {
           
           // Utiliser l'ID pour la suppression
            pstmt.setInt(1, symptome.getId());  
            int rowsAffected = pstmt.executeUpdate();
            
            System.out.println("Résultat de la suppression: " + rowsAffected + " ligne(s) affectée(s)");
            
            return new Response(request.getRequestId(), 
                    rowsAffected > 0 
                    ? "{\"message\": \"Symptôme supprimé avec succès\"}"
                    : "{\"message\": \"Aucun symptôme trouvé à supprimer\"}");
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
                maladies.add(res.getString("nom_maladie"));  
            }
        }

        if (maladies.isEmpty()) {
            return new Response(request.getRequestId(), "{\"message\": \"Aucune maladie trouvée\"}");
        } else {
            return new Response(request.getRequestId(), objectMapper.writeValueAsString(maladies));
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
                maladies.add(res.getString("nom_maladie"));
            }

            return new Response(request.getRequestId(),
                    maladies.isEmpty() ? "Aucune maladie trouvée" : objectMapper.writeValueAsString(maladies));
        }
    }
}