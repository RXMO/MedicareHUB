package edu.ezip.ing1.pds.business.server;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ezip.ing1.pds.business.dto.DiagnosticResult;
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
        SELECT_PRESCRIPTION_PAR_ORDONNANCE(
                "SELECT m.id_medicament, m.nom_medicament, p.posologie FROM Prescription p " +
                        "JOIN medicament m ON p.id_medicament = m.id_medicament " +
                        "WHERE p.id_ordonnance = ?"),
                        
                        
        SELECT_ALL_SYMPTOMES("SELECT id_symptome, description FROM symptomes"),
        INSERT_SYMPTOME("INSERT INTO symptomes (description) VALUES (?)"),
        DELETE_SYMPTOME("DELETE FROM symptomes WHERE id_symptome = ?"),
        UPDATE_SYMPTOME("UPDATE symptomes SET description = ? WHERE id_symptome = ?"),
        RECHERCHER_MALADIES_PAR_SYMPTOME(
                            "SELECT DISTINCT maladies.nom_maladie FROM maladies " +
                            "JOIN symptomes_maladies ON maladies.id_maladie = symptomes_maladies.id_maladie " +
                            "JOIN symptomes ON symptomes_maladies.id_symptome = symptomes.id_symptome " +
                            "WHERE symptomes.description = ?"),
        SELECT_PATIENT_SYMPTOMES("SELECT s.id_symptome, s.description FROM symptomes s " +
                            "JOIN patients_symptomes ps ON s.id_symptome = ps.id_symptome " +
                            "WHERE ps.id_patient = ?"),
        CHECK_PATIENT_SYMPTOME_EXISTS("SELECT COUNT(*) FROM patients_symptomes WHERE id_patient = ? AND id_symptome = ?"),
        SELECT_SYMPTOME_BY_NAME("SELECT id_symptome FROM symptomes WHERE description = ?"),
        INSERT_PATIENT_SYMPTOME("INSERT INTO patients_symptomes (id_patient, id_symptome) VALUES (?, ?)"),
        MODIFY_PATIENT_SYMPTOME("UPDATE patients_symptomes SET id_symptome = ? WHERE id_patient = ? AND id_symptome = ?"),
        INSERT_RENDEZ_VOUS("INSERT INTO consultation (id_patient, id_medecin, id_disponibilite, heure_consultation) VALUES (?, ?, ?, ?)"),                        
        SELECT_CRENEAU_DISPONIBLE("SELECT c.id_disponibilite, c.id_medecin, c.jour, c.heure_debut, c.heure_fin " +
                            "FROM creneaux_disponibles c " +
                            "JOIN medecins m ON c.id_medecin = m.id_medecin " +
                            "WHERE m.id_specialite = ? AND c.disponible = true LIMIT 1"),
        UPDATE_CRENEAU_DISPONIBLE("UPDATE creneaux_disponibles SET disponible = false WHERE id_disponibilite = ?"),
        DIAGNOSTIC_PATIENT("SELECT s.id_symptome, s.description FROM symptomes s " +
                            "JOIN patients_symptomes ps ON s.id_symptome = ps.id_symptome " +
                            "WHERE ps.id_patient = ?"),
        DELETE_PATIENT_SYMPTOME("DELETE FROM patients_symptomes WHERE id_patient = ? AND id_symptome = ?"),
        SELECT_ALL_CRENEAUX_DISPONIBLES("SELECT id_disponibilite, jour, heure_debut, heure_fin, id_medecin, disponible FROM creneaux_disponibles WHERE disponible = true"),
        SELECT_RENDEZ_VOUS_BY_PATIENT("SELECT c.id_consultation, c.id_patient, c.id_medecin, c.id_disponibilite, c.heure_consultation FROM consultation c WHERE c.id_patient = ?");
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
            case INSERT_PATIENT_SYMPTOME:
                response = InsertPatientSymptome(request, connection);
                break;
            case DELETE_PATIENT_SYMPTOME:
                response = DeletePatientSymptome(request, connection);
                break;
            case SELECT_PATIENT_SYMPTOMES:
                response = SelectPatientSymptomes(request, connection);
                break;
            case MODIFY_PATIENT_SYMPTOME:
                response = ModifyPatientSymptome(request, connection);
                break;
            case INSERT_RENDEZ_VOUS:
                response = InsertRendezVous(request, connection);
                break;
                case SELECT_ALL_CRENEAUX_DISPONIBLES:
                response = SelectAllCreneauxDisponibles(request, connection);
                break;
            case SELECT_RENDEZ_VOUS_BY_PATIENT:
                response = SelectRendezVousByPatient(request, connection);
                break;
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
        try (PreparedStatement pstmt = connection.prepareStatement(Queries.UPDATE_PATIENT.query)) {
            pstmt.setString(1, requestData.getNomPatient());
            pstmt.setString(2, requestData.getPrenomPatient());
            pstmt.setString(3, requestData.getNumTel());
            pstmt.setString(4, requestData.getAllergies());
            pstmt.setInt(5, requestData.getIdPatient());
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
        try (Statement stmt = connection.createStatement();
             ResultSet res = stmt.executeQuery(Queries.SELECT_ALL_ORDONNANCES.query)) {
            Ordonnances ordonnances = new Ordonnances();
            while (res.next()) {
                Ordonnance ordonnance = new Ordonnance();
                ordonnance.setIdOrdonnance(res.getInt("id_ordonnance"));
                ordonnance.setDescription(res.getString("description"));
                ordonnance.setIdPatient(res.getInt("id_patient"));
                ordonnance.setIdMedecin(res.getInt("id_medecin"));
                ordonnance.setIdConsultation(res.getInt("id_consultation"));
                ordonnances.add(ordonnance);
            }
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
            return new Response(request.getRequestId(),
                    rowsAffected > 0 ? "Ordonnance supprimée avec succès" : "Aucune ordonnance trouvée pour suppression");
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
            return new Response(request.getRequestId(),
                    rowsAffected > 0 ? "Ordonnance mise à jour avec succès" : "Aucune ordonnance trouvée pour mise à jour");
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
                medicament.setIdMedicament(res.getInt(1));
                medicament.setNomMedicament(res.getString(2));
                medicament.setPrincipeActif(res.getString(3));
                medicaments.add(medicament);
            }
            return new Response(request.getRequestId(),
                    medicaments.getMedicaments().isEmpty() ? "Aucun médicament trouvé"
                            : objectMapper.writeValueAsString(medicaments));
        }
    }
//récupérer tous les symptômes de la base
    private Response SelectAllSymptomes(final Request request, final Connection connection)
            throws SQLException, JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        try (Statement stmt = connection.createStatement();
             ResultSet res = stmt.executeQuery(Queries.SELECT_ALL_SYMPTOMES.query)) {
            List<Symptomes> symptomes = new ArrayList<>();
            while (res.next()) {
                Symptomes symptome = new Symptomes();
                symptome.setId(res.getInt("id_symptome"));
                symptome.setDescription(res.getString("description"));
                symptomes.add(symptome);
            }
            return new Response(request.getRequestId(),
                    symptomes.isEmpty() ? "Aucun symptôme trouvé"
                            : objectMapper.writeValueAsString(symptomes));
        }
    }
// ajouter un nouveau symptôme 

    private Response InsertSymptome(final Request request, final Connection connection)
            throws SQLException, IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        Symptomes symptome = objectMapper.readValue(request.getRequestBody(), Symptomes.class);
        try (PreparedStatement pstmt = connection.prepareStatement(Queries.INSERT_SYMPTOME.query,
                Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, symptome.getDescription());
            int rowsAffected = pstmt.executeUpdate();
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    symptome.setId(generatedKeys.getInt(1));
                }
            }
            return new Response(request.getRequestId(), objectMapper.writeValueAsString(symptome));
        }
    }
//modifier un symptome 
    private Response UpdateSymptome(final Request request, final Connection connection)
            throws SQLException, IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        Symptomes symptome = objectMapper.readValue(request.getRequestBody(), Symptomes.class);
        try (PreparedStatement pstmt = connection.prepareStatement(Queries.UPDATE_SYMPTOME.query)) {
            pstmt.setString(1, symptome.getDescription());
            pstmt.setInt(2, symptome.getId());
            int rowsAffected = pstmt.executeUpdate();
            return new Response(request.getRequestId(),
                    rowsAffected > 0 ? "{\"message\": \"Symptôme mis à jour avec succès\"}"
                            : "{\"message\": \"Aucun symptôme trouvé pour mise à jour\"}");
        }
    }
//supprimer un symptome
    private Response DeleteSymptome(final Request request, final Connection connection)
            throws SQLException, IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        Symptomes symptome = objectMapper.readValue(request.getRequestBody(), Symptomes.class);
        try (PreparedStatement pstmt = connection.prepareStatement(Queries.DELETE_SYMPTOME.query)) {
            pstmt.setInt(1, symptome.getId());
            int rowsAffected = pstmt.executeUpdate();
            return new Response(request.getRequestId(),
                    rowsAffected > 0 ? "{\"message\": \"Symptôme supprimé avec succès\"}"
                            : "{\"message\": \"Aucun symptôme trouvé à supprimer\"}");
        }
    }
// chercher les maladies liées a un symptome
    private Response rechercherMaladiesParSymptome(final Request request, final Connection connection)
            throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        String symptomeNom = objectMapper.readValue(request.getRequestBody(), String.class);
        List<String> maladies = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(Queries.RECHERCHER_MALADIES_PAR_SYMPTOME.query)) {
            pstmt.setString(1, symptomeNom);
            ResultSet res = pstmt.executeQuery();
            while (res.next()) {
                maladies.add(res.getString("nom_maladie"));
            }
        } catch (SQLException e) {
            return new Response(request.getRequestId(), "{\"message\": \"Erreur lors de la recherche des maladies: " + e.getMessage() + "\"}");
        }
        return new Response(request.getRequestId(),
                maladies.isEmpty() ? "{\"message\": \"Aucune maladie trouvée\"}"
                        : objectMapper.writeValueAsString(maladies));
    }
//diagnostiquer en f des symptomes 
    private Response DiagnostiquerPatient(final Request request, final Connection connection)
        throws SQLException, JsonProcessingException, IOException {
    final ObjectMapper objectMapper = new ObjectMapper();
    int idPatient = objectMapper.readValue(request.getRequestBody(), Integer.class);
    List<Integer> symptomesPatient = new ArrayList<>();

    // Récupérer les symptômes du patient
    try (PreparedStatement pstmt = connection.prepareStatement(Queries.SELECT_PATIENT_SYMPTOMES.query)) {
        pstmt.setInt(1, idPatient);
        ResultSet rs = pstmt.executeQuery();
        while (rs.next()) {
            symptomesPatient.add(rs.getInt("id_symptome"));
        }
    }
    if (symptomesPatient.isEmpty()) {
        return new Response(request.getRequestId(), "{\"message\": \"Aucun symptôme trouvé pour ce patient\"}");
    }

    // Récupérer les maladies et leurs symptômes associés et les spécialités
    Map<Integer, String> maladiesMap = new HashMap<>();
    Map<Integer, List<Integer>> symptomesMaladies = new HashMap<>();
    Map<Integer, String> specialitesMap = new HashMap<>();
    Map<Integer, Integer> idSpecialitesMap = new HashMap<>();
    try (PreparedStatement pstmt = connection.prepareStatement(
            "SELECT m.id_maladie, m.nom_maladie, sm.id_symptome, s.nom_specialite, s.id_specialite " +
            "FROM maladies m " +
            "JOIN symptomes_maladies sm ON m.id_maladie = sm.id_maladie " +
            "LEFT JOIN specialites s ON m.id_specialite = s.id_specialite")) {
        ResultSet rs = pstmt.executeQuery();
        while (rs.next()) {
            int idMaladie = rs.getInt("id_maladie");
            maladiesMap.put(idMaladie, rs.getString("nom_maladie"));
            symptomesMaladies.computeIfAbsent(idMaladie, k -> new ArrayList<>()).add(rs.getInt("id_symptome"));
            specialitesMap.put(idMaladie, rs.getString("nom_specialite") != null ? rs.getString("nom_specialite") : "Généraliste");
            int idSpecialite = rs.getInt("id_specialite");
            if (!rs.wasNull()) {
                idSpecialitesMap.put(idMaladie, idSpecialite);
            } else {
                idSpecialitesMap.put(idMaladie, 0); 
            }
        }
    }

    // Calculer les diagnostics avec score
    List<DiagnosticResult> resultats = new ArrayList<>();
    for (Map.Entry<Integer, List<Integer>> entry : symptomesMaladies.entrySet()) {
        int idMaladie = entry.getKey();
        List<Integer> symptomesMaladie = entry.getValue();
        int correspondances = (int) symptomesMaladie.stream().filter(symptomesPatient::contains).count();
        double score = (correspondances * 100.0) / symptomesMaladie.size();
        if (score >= 50.0) {
            DiagnosticResult result = new DiagnosticResult(
                maladiesMap.get(idMaladie),
                score,
                correspondances,
                symptomesMaladie.size(),
                symptomesPatient.size(),
                specialitesMap.get(idMaladie),
                idSpecialitesMap.getOrDefault(idMaladie, 0),
                score,
                (correspondances * 100.0) / symptomesPatient.size()
            );
            resultats.add(result);
        }
    }
    resultats.sort((r1, r2) -> Double.compare(r2.getScore(), r1.getScore()));

    // Récupérer les créneaux disponibles pour les spécialités associées
    if (!resultats.isEmpty()) {
        for (DiagnosticResult result : resultats) {
            int idSpecialite = result.getIdSpecialite();
            try (PreparedStatement pstmt = connection.prepareStatement(Queries.SELECT_ALL_CRENEAUX_DISPONIBLES.query)) {
                // Filtrer les créneaux par spécialité via une jointure avec medecins
                ResultSet creneauxRs = pstmt.executeQuery();
                List<Map<String, Object>> creneaux = new ArrayList<>();
                while (creneauxRs.next()) {
                    int idMedecin = creneauxRs.getInt("id_medecin");
                    try (PreparedStatement medecinStmt = connection.prepareStatement(
                            "SELECT id_specialite FROM medecins WHERE id_medecin = ?")) {
                        medecinStmt.setInt(1, idMedecin);
                        ResultSet medecinRs = medecinStmt.executeQuery();
                        if (medecinRs.next() && medecinRs.getInt("id_specialite") == idSpecialite) {
                            Map<String, Object> creneau = new HashMap<>();
                            creneau.put("id_disponibilite", creneauxRs.getInt("id_disponibilite"));
                            creneau.put("jour", creneauxRs.getString("jour"));
                            creneau.put("heure_debut", creneauxRs.getString("heure_debut"));
                            creneau.put("heure_fin", creneauxRs.getString("heure_fin"));
                            creneau.put("id_medecin", idMedecin);
                            creneaux.add(creneau);
                        }
                    }
                }
                result.setCreneauxDisponibles(creneaux); // Ajouter les créneaux au résultat
            }
        }
    }

    return new Response(request.getRequestId(),
            resultats.isEmpty() ? "{\"message\": \"Aucune maladie correspondante trouvée\"}"
                    : objectMapper.writeValueAsString(resultats));
}

private Response InsertRendezVous(final Request request, final Connection connection)
        throws SQLException, IOException {
    final ObjectMapper objectMapper = new ObjectMapper();
    JsonNode requestData = objectMapper.readTree(request.getRequestBody());
    int idSpecialite = requestData.get("id_specialite").asInt();
    String dateRendezVous = requestData.get("date_rendez_vous").asText();
    int idPatient = requestData.get("id_patient").asInt();
    int idDisponibilite = requestData.get("id_disponibilite").asInt();
    int idMedecin = requestData.get("id_medecin").asInt();

    // Activer une transaction pour éviter les conditions de concurrence
    connection.setAutoCommit(false);
    try {
        // Vérifier que le créneau est disponible avec un verrouillage
        String heureConsultation;
        try (PreparedStatement creneauStmt = connection.prepareStatement(
                "SELECT heure_debut FROM creneaux_disponibles WHERE id_disponibilite = ? AND id_medecin = ? AND disponible = TRUE FOR UPDATE")) {
            creneauStmt.setInt(1, idDisponibilite);
            creneauStmt.setInt(2, idMedecin);
            ResultSet creneauRs = creneauStmt.executeQuery();

            if (!creneauRs.next()) {
                connection.rollback();
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Créneau non disponible ou invalide (ID: " + idDisponibilite + ", Médecin: " + idMedecin + ").");
                return new Response(request.getRequestId(), objectMapper.writeValueAsString(errorResponse));
            }
            heureConsultation = creneauRs.getString("heure_debut");
        }

        // Marquer le créneau comme non disponible
        try (PreparedStatement updateStmt = connection.prepareStatement(Queries.UPDATE_CRENEAU_DISPONIBLE.query)) {
            updateStmt.setInt(1, idDisponibilite);
            int rowsAffected = updateStmt.executeUpdate();
            if (rowsAffected == 0) {
                connection.rollback();
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Échec de la mise à jour de la disponibilité du créneau (ID: " + idDisponibilite + ").");
                return new Response(request.getRequestId(), objectMapper.writeValueAsString(errorResponse));
            }
        }

        // Insérer la consultation
        try (PreparedStatement insertStmt = connection.prepareStatement(Queries.INSERT_RENDEZ_VOUS.query)) {
            insertStmt.setInt(1, idPatient);
            insertStmt.setInt(2, idMedecin);
            insertStmt.setInt(3, idDisponibilite);
            insertStmt.setString(4, heureConsultation);
            int rowsAffected = insertStmt.executeUpdate();
            if (rowsAffected == 0) {
                connection.rollback();
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Échec de l'insertion de la consultation.");
                return new Response(request.getRequestId(), objectMapper.writeValueAsString(errorResponse));
            }
        }

        // Valider la transaction
        connection.commit();
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", "Rendez-vous créé avec succès pour le patient " + idPatient +
                " avec le médecin " + idMedecin + " le " + dateRendezVous + " à " + heureConsultation);
        return new Response(request.getRequestId(), objectMapper.writeValueAsString(responseBody));
    } catch (SQLException e) {
        connection.rollback();
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", "Erreur lors de la création du rendez-vous: " + e.getMessage());
        return new Response(request.getRequestId(), objectMapper.writeValueAsString(errorResponse));
    } finally {
        connection.setAutoCommit(true);
    }
}
//afficher  les creneaux dispo
private Response SelectAllCreneauxDisponibles(final Request request, final Connection connection)
throws SQLException, JsonProcessingException {
final ObjectMapper objectMapper = new ObjectMapper();
try (Statement stmt = connection.createStatement();
     ResultSet res = stmt.executeQuery(Queries.SELECT_ALL_CRENEAUX_DISPONIBLES.query)) {
    List<Map<String, Object>> creneaux = new ArrayList<>();
    while (res.next()) {
        Map<String, Object> creneau = new HashMap<>();
        creneau.put("id_disponibilite", res.getInt("id_disponibilite"));
        creneau.put("jour", res.getString("jour"));
        creneau.put("heure_debut", res.getString("heure_debut"));
        creneau.put("heure_fin", res.getString("heure_fin"));
        creneau.put("id_medecin", res.getInt("id_medecin"));
        creneau.put("disponible", res.getBoolean("disponible"));
        creneaux.add(creneau);
    }
    return new Response(request.getRequestId(),
            creneaux.isEmpty() ? "{\"message\": \"Aucun créneau disponible trouvé\"}"
                    : objectMapper.writeValueAsString(creneaux));
}
}

//recup rdv par patient
private Response SelectRendezVousByPatient(final Request request, final Connection connection)
throws SQLException, JsonProcessingException, IOException {
final ObjectMapper objectMapper = new ObjectMapper();
int idPatient = objectMapper.readValue(request.getRequestBody(), Integer.class);
try (PreparedStatement pstmt = connection.prepareStatement(Queries.SELECT_RENDEZ_VOUS_BY_PATIENT.query)) {
    pstmt.setInt(1, idPatient);
    ResultSet res = pstmt.executeQuery();
    List<Map<String, Object>> rendezVous = new ArrayList<>();
    while (res.next()) {
        Map<String, Object> rdv = new HashMap<>();
        rdv.put("id_consultation", res.getInt("id_consultation"));
        rdv.put("id_patient", res.getInt("id_patient"));
        rdv.put("id_medecin", res.getInt("id_medecin"));
        rdv.put("id_disponibilite", res.getInt("id_disponibilite"));
        rdv.put("heure_consultation", res.getString("heure_consultation"));
        rendezVous.add(rdv);
    }
    return new Response(request.getRequestId(),
            rendezVous.isEmpty() ? "{\"message\": \"Aucun rendez-vous trouvé pour ce patient\"}"
                    : objectMapper.writeValueAsString(rendezVous));
}
}
     // associe un symptôme à un patient

    private Response InsertPatientSymptome(final Request request, final Connection connection)
            throws SQLException, IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        JsonNode data = objectMapper.readTree(request.getRequestBody());
        int idPatient = data.get("id_patient").asInt();
        int idSymptome = data.has("id_symptome") ? data.get("id_symptome").asInt() : -1;
        String nomSymptome = data.has("nom_symptome") ? data.get("nom_symptome").asText() : null;

        connection.setAutoCommit(false);
        try {
            if (nomSymptome != null && idSymptome == -1) {
                // Vérifier si le symptôme existe déjà
                try (PreparedStatement pstmt = connection.prepareStatement(Queries.SELECT_SYMPTOME_BY_NAME.query)) {
                    pstmt.setString(1, nomSymptome);
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) {
                        idSymptome = rs.getInt("id_symptome");
                    } else {
                        // Insérer un nouveau symptôme sinon
                        try (PreparedStatement insertStmt = connection.prepareStatement(Queries.INSERT_SYMPTOME.query,
                                Statement.RETURN_GENERATED_KEYS)) {
                            insertStmt.setString(1, nomSymptome);
                            insertStmt.executeUpdate();
                            try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                                if (generatedKeys.next()) {
                                    idSymptome = generatedKeys.getInt(1);
                                } else {
                                    throw new SQLException("Échec de la récupération de l'ID du symptôme inséré");
                                }
                            }
                        }
                    }
                }
            }

            if (idSymptome == -1) {
                connection.rollback();
                Symptomes errorSymptome = new Symptomes();
                errorSymptome.setId(-1);
                errorSymptome.setDescription("Erreur: ID du symptôme non fourni et non résolu");
                return new Response(request.getRequestId(), objectMapper.writeValueAsString(errorSymptome));
            }

            // Vérifier si l'association existe déjà 
            boolean associationExiste = false;
            try (PreparedStatement checkStmt = connection.prepareStatement(Queries.CHECK_PATIENT_SYMPTOME_EXISTS.query)) {
                checkStmt.setInt(1, idPatient);
                checkStmt.setInt(2, idSymptome);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    associationExiste = true;
                }
            }

            // Insérer l'association si elle n'existe pas
            if (!associationExiste) {
                try (PreparedStatement insertStmt = connection.prepareStatement(Queries.INSERT_PATIENT_SYMPTOME.query)) {
                    insertStmt.setInt(1, idPatient);
                    insertStmt.setInt(2, idSymptome);
                    insertStmt.executeUpdate();
                }
            }

            // Récupérer les détails du symptôme pour le retourner
            Symptomes symptome = new Symptomes();
            try (PreparedStatement pstmt = connection.prepareStatement("SELECT id_symptome, description FROM symptomes WHERE id_symptome = ?")) {
                pstmt.setInt(1, idSymptome);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    symptome.setId(rs.getInt("id_symptome"));
                    symptome.setDescription(rs.getString("description"));
                } else {
                    throw new SQLException("Symptôme introuvable après insertion");
                }
            }

            connection.commit();
            return new Response(request.getRequestId(), objectMapper.writeValueAsString(symptome));
        } catch (SQLException e) {
            connection.rollback();
            Symptomes errorSymptome = new Symptomes();
            errorSymptome.setId(-1);
            errorSymptome.setDescription("Erreur lors de l'insertion: " + e.getMessage());
            return new Response(request.getRequestId(), objectMapper.writeValueAsString(errorSymptome));
        } finally {
            connection.setAutoCommit(true);
        }
    }

    private Response DeletePatientSymptome(final Request request, final Connection connection)
            throws SQLException, IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        JsonNode data = objectMapper.readTree(request.getRequestBody());
        int idPatient = data.get("id_patient").asInt();
        int idSymptome = data.get("id_symptome").asInt();
        connection.setAutoCommit(false);
        try {
            try (PreparedStatement pstmt = connection.prepareStatement(Queries.DELETE_PATIENT_SYMPTOME.query)) {
                pstmt.setInt(1, idPatient);
                pstmt.setInt(2, idSymptome);
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected == 0) {
                    connection.rollback();
                    return new Response(request.getRequestId(), "{\"message\": \"Aucune association trouvée\"}");
                }
            }
            connection.commit();
            return new Response(request.getRequestId(), "{\"message\": \"Association supprimée avec succès\"}");
        } catch (SQLException e) {
            connection.rollback();
            return new Response(request.getRequestId(), "{\"message\": \"Erreur lors de la suppression: " + e.getMessage() + "\"}");
        } finally {
            connection.setAutoCommit(true);
        }
    }

    private Response SelectPatientSymptomes(final Request request, final Connection connection)
            throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        int idPatient = objectMapper.readValue(request.getRequestBody(), Integer.class);
        try (PreparedStatement pstmt = connection.prepareStatement(Queries.SELECT_PATIENT_SYMPTOMES.query)) {
            pstmt.setInt(1, idPatient);
            ResultSet res = pstmt.executeQuery();
            List<Symptomes> symptomes = new ArrayList<>();
            while (res.next()) {
                Symptomes symptome = new Symptomes();
                symptome.setId(res.getInt("id_symptome"));
                symptome.setDescription(res.getString("description"));
                symptomes.add(symptome);
            }
            return new Response(request.getRequestId(), objectMapper.writeValueAsString(symptomes));
        } catch (SQLException e) {
            List<Symptomes> errorList = new ArrayList<>();
            Symptomes errorSymptome = new Symptomes();
            errorSymptome.setId(-1);
            errorSymptome.setDescription("Erreur: " + e.getMessage());
            errorList.add(errorSymptome);
            return new Response(request.getRequestId(), objectMapper.writeValueAsString(errorList));
        }
    }

    private Response ModifyPatientSymptome(final Request request, final Connection connection)
            throws SQLException, IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        JsonNode data = objectMapper.readTree(request.getRequestBody());
        int idPatient = data.get("id_patient").asInt();
        int idAncienSymptome = data.get("id_ancien_symptome").asInt();
        int idNouveauSymptome = data.get("id_nouveau_symptome").asInt();
        connection.setAutoCommit(false);
        try {
            try (PreparedStatement pstmt = connection.prepareStatement(Queries.MODIFY_PATIENT_SYMPTOME.query)) {
                pstmt.setInt(1, idNouveauSymptome);
                pstmt.setInt(2, idPatient);
                pstmt.setInt(3, idAncienSymptome);
                int rowsUpdated = pstmt.executeUpdate();
                if (rowsUpdated == 0) {
                    try (PreparedStatement insertStmt = connection.prepareStatement(Queries.INSERT_PATIENT_SYMPTOME.query)) {
                        insertStmt.setInt(1, idPatient);
                        insertStmt.setInt(2, idNouveauSymptome);
                        insertStmt.executeUpdate();
                    }
                }
            }
            connection.commit();
            Symptomes nouveauSymptome = new Symptomes();
            try (PreparedStatement pstmt = connection.prepareStatement("SELECT id_symptome, description FROM symptomes WHERE id_symptome = ?")) {
                pstmt.setInt(1, idNouveauSymptome);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    nouveauSymptome.setId(rs.getInt("id_symptome"));
                    nouveauSymptome.setDescription(rs.getString("description"));
                } else {
                    throw new SQLException("Nouveau symptôme introuvable");
                }
            }
            return new Response(request.getRequestId(), objectMapper.writeValueAsString(nouveauSymptome));
        } catch (SQLException e) {
            connection.rollback();
            Symptomes errorSymptome = new Symptomes();
            errorSymptome.setId(-1);
            errorSymptome.setDescription("Erreur lors de la modification: " + e.getMessage());
            return new Response(request.getRequestId(), objectMapper.writeValueAsString(errorSymptome));
        } finally {
            connection.setAutoCommit(true);
        }
    }
}