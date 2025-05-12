package edu.ezip.ing1.pds.services;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.ezip.ing1.pds.business.dto.DiagnosticResult;
import edu.ezip.ing1.pds.business.dto.RendezVousData;
import edu.ezip.ing1.pds.business.dto.Symptomes;
import edu.ezip.ing1.pds.client.commons.ClientRequest;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.commons.Request;
import edu.ezip.ing1.pds.requests.DeletePatientSymptomeClientRequest;
import edu.ezip.ing1.pds.requests.DeleteSymptomeClientRequest;
import edu.ezip.ing1.pds.requests.DiagnostiquerClientRequest;
import edu.ezip.ing1.pds.requests.InsertDiagnosticClientRequest;
import edu.ezip.ing1.pds.requests.InsertPatientSymptomeClientRequest;
import edu.ezip.ing1.pds.requests.InsertRendezVousClientRequest;
import edu.ezip.ing1.pds.requests.InsertSymptomeClientRequest;
import edu.ezip.ing1.pds.requests.ModifyPatientSymptomeClientRequest;
import edu.ezip.ing1.pds.requests.SelectAllSymptomesClientRequest;
import edu.ezip.ing1.pds.requests.SelectPatientDiagnosticsClientRequest;
import edu.ezip.ing1.pds.requests.SelectPatientSymptomesClientRequest;
import edu.ezip.ing1.pds.requests.UpdateSymptomeClientRequest;


public class ServiceSymptome {

    private final static String LoggingLabel = "FrontEnd - ServiceSymptome";
    private final static Logger logger = LoggerFactory.getLogger(LoggingLabel);

    private final NetworkConfig networkConfig;

    public ServiceSymptome(NetworkConfig networkConfig) {
        this.networkConfig = networkConfig;
    }

    // ajoute un nouveau symptôme dans la base de données
    public Symptomes insertSymptome(Symptomes symptome) throws InterruptedException, IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final String requestId = UUID.randomUUID().toString();
        final Request request = new Request();
        request.setRequestId(requestId);
        request.setRequestOrder("INSERT_SYMPTOME");
        request.setRequestContent(objectMapper.writeValueAsString(symptome));
        objectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        final byte[] requestBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(request);

        final InsertSymptomeClientRequest symptomeRequest = new InsertSymptomeClientRequest(
                networkConfig, 0, request, symptome, requestBytes);
        final Deque<ClientRequest> symptomeRequests = new ArrayDeque<>();
        symptomeRequests.push(symptomeRequest);

        if (!symptomeRequests.isEmpty()) {
            final ClientRequest joinedSymptomeRequest = symptomeRequests.pop();
            joinedSymptomeRequest.join();
            logger.debug("Thread {} terminé.", joinedSymptomeRequest.getThreadName());
            return (Symptomes) joinedSymptomeRequest.getResult();
        }
        return null;
    }

    // mettre à jour un symptôme existant dans la base
    public void updateSymptome(Symptomes symptome, String nouveauNom) throws InterruptedException, IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final String requestId = UUID.randomUUID().toString();
        final Request request = new Request();
        request.setRequestId(requestId);
        request.setRequestOrder("UPDATE_SYMPTOME");
        symptome.setDescription(nouveauNom);
        request.setRequestContent(objectMapper.writeValueAsString(symptome));
        objectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        final byte[] requestBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(request);

        final UpdateSymptomeClientRequest symptomeRequest = new UpdateSymptomeClientRequest(
                networkConfig, 0, request, symptome, requestBytes);
        final Deque<ClientRequest> symptomeRequests = new ArrayDeque<>();
        symptomeRequests.push(symptomeRequest);

        if (!symptomeRequests.isEmpty()) {
            final ClientRequest joinedSymptomeRequest = symptomeRequests.pop();
            joinedSymptomeRequest.join();
            logger.debug("Thread {} terminé.", joinedSymptomeRequest.getThreadName());
        } else {
            throw new IOException("Échec de la mise à jour du symptôme : aucune requête envoyée.");
        }
    }

    // supprime un symptôme de la base de données
    public void deleteSymptome(Symptomes symptome) throws InterruptedException, IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final String requestId = UUID.randomUUID().toString();
        final Request request = new Request();
        request.setRequestId(requestId);
        request.setRequestOrder("DELETE_SYMPTOME");
        request.setRequestContent(objectMapper.writeValueAsString(symptome));
        objectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        final byte[] requestBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(request);

        final DeleteSymptomeClientRequest symptomeRequest = new DeleteSymptomeClientRequest(
                networkConfig, 0, request, symptome, requestBytes);
        final Deque<ClientRequest> symptomeRequests = new ArrayDeque<>();
        symptomeRequests.push(symptomeRequest);

        if (!symptomeRequests.isEmpty()) {
            final ClientRequest joinedSymptomeRequest = symptomeRequests.pop();
            joinedSymptomeRequest.join();
            logger.debug("Thread {} terminé.", joinedSymptomeRequest.getThreadName());
        }
    }

    //  récupère tous les symptômes disponibles dans la base
    public List<Symptomes> selectSymptomes() throws InterruptedException, IOException {
        //  prépare la requête pour récupérer tous les symptômes
        final ObjectMapper objectMapper = new ObjectMapper();
        final String requestId = UUID.randomUUID().toString();
        final Request request = new Request();
        request.setRequestId(requestId);
        request.setRequestOrder("SELECT_ALL_SYMPTOMES");
        objectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        final byte[] requestBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(request);

        final SelectAllSymptomesClientRequest symptomeRequest = new SelectAllSymptomesClientRequest(
                networkConfig, 0, request, null, requestBytes);
        final Deque<ClientRequest> symptomeRequests = new ArrayDeque<>();
        symptomeRequests.push(symptomeRequest);

        if (!symptomeRequests.isEmpty()) {
            final ClientRequest joinedSymptomeRequest = symptomeRequests.pop();
            joinedSymptomeRequest.join();
            logger.debug("Thread {} terminé.", joinedSymptomeRequest.getThreadName());
            List<Symptomes> result = (List<Symptomes>) joinedSymptomeRequest.getResult();
            return result != null ? result : new ArrayList<>();
        }
        return new ArrayList<>();
    }

    //  diagnostic pour un patient en fonction de ses symptômes
    public List<DiagnosticResult> diagnostiquer(int idPatient) throws InterruptedException, IOException {
    final ObjectMapper objectMapper = new ObjectMapper();
    final String requestId = UUID.randomUUID().toString();
    final Request request = new Request();
    request.setRequestId(requestId);
    request.setRequestOrder("DIAGNOSTIC_PATIENT");
    request.setRequestContent(objectMapper.writeValueAsString(idPatient));
    objectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
    final byte[] requestBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(request);

    final DiagnostiquerClientRequest diagnostiqueRequest = new DiagnostiquerClientRequest(
            networkConfig, 0, request, idPatient, requestBytes);
    final Deque<ClientRequest> diagnostiqueRequests = new ArrayDeque<>();
    diagnostiqueRequests.push(diagnostiqueRequest);

    List<DiagnosticResult> resultats = new ArrayList<>();
    if (!diagnostiqueRequests.isEmpty()) {
        final ClientRequest joinedRequest = diagnostiqueRequests.pop();
        joinedRequest.join();
        logger.debug("Thread {} terminé.", joinedRequest.getThreadName());
        resultats = (List<DiagnosticResult>) joinedRequest.getResult();
        resultats = resultats != null ? resultats : new ArrayList<>();
    }

    // Trie les résultats par score décroissant
    resultats.sort((r1, r2) -> Double.compare(r2.getScore(), r1.getScore()));

    // Limite les insertions aux 3 meilleurs diagnostics
    int maxDiagnosticsToInsert = 3;
    if (!resultats.isEmpty()) {
        List<DiagnosticResult> topDiagnostics = resultats.subList(0, Math.min(maxDiagnosticsToInsert, resultats.size()));
        for (DiagnosticResult result : topDiagnostics) {
            final String insertRequestId = UUID.randomUUID().toString();
            final Request insertRequest = new Request();
            insertRequest.setRequestId(insertRequestId);
            insertRequest.setRequestOrder("INSERT_DIAGNOSIS");
            ObjectNode dataNode = objectMapper.createObjectNode();
            dataNode.put("id_patient", idPatient);
            dataNode.put("id_maladie", result.getId_maladie());
            dataNode.put("score", result.getScore());
            insertRequest.setRequestContent(objectMapper.writeValueAsString(dataNode));
            objectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
            final byte[] insertRequestBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(insertRequest);

            final InsertDiagnosticClientRequest insertDiagnosisRequest = new InsertDiagnosticClientRequest(
                    networkConfig, 0, insertRequest, result, insertRequestBytes);
            final Deque<ClientRequest> insertRequests = new ArrayDeque<>();
            insertRequests.push(insertDiagnosisRequest);

            if (!insertRequests.isEmpty()) {
                final ClientRequest joinedInsertRequest = insertRequests.pop();
                joinedInsertRequest.join();
                logger.debug("Thread {} terminé : Diagnostic inséré pour id_maladie {}", 
                        joinedInsertRequest.getThreadName(), result.getId_maladie());
            }
        }
    }

    // Retourne tous les résultats pour l'affichage dans l'interface
    return resultats;
}

    // crée un rendez-vous pour un patient
    public String creerRendezVous(int idPatient, String dateRendezVous, int idSpecialite, int idDisponibilite, int idMedecin) throws InterruptedException, IOException {
        //  préparer la requête pour créer un rendez-vous
        final ObjectMapper objectMapper = new ObjectMapper();
        final String requestId = UUID.randomUUID().toString();
        final Request request = new Request();
        request.setRequestId(requestId);
        request.setRequestOrder("INSERT_RENDEZ_VOUS");

        // mettre les données du rendez-vous en format JSON
        ObjectNode dataNode = objectMapper.createObjectNode();
        dataNode.put("id_patient", idPatient);
        dataNode.put("date_rendez_vous", dateRendezVous);
        dataNode.put("id_specialite", idSpecialite);
        dataNode.put("id_disponibilite", idDisponibilite);
        dataNode.put("id_medecin", idMedecin);
        request.setRequestContent(objectMapper.writeValueAsString(dataNode));

        objectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        final byte[] requestBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(request);

        // afficher un message pour dire : envoie la requête
        logger.info("Envoi d'une demande de rendez-vous pour le patient " + idPatient + 
                    " le " + dateRendezVous + " avec le médecin " + idMedecin);

        final InsertRendezVousClientRequest rendezVousRequest = new InsertRendezVousClientRequest(
                networkConfig, 0, request, new RendezVousData(idPatient, dateRendezVous, idSpecialite, idDisponibilite, idMedecin), requestBytes);
        final Deque<ClientRequest> rendezVousRequests = new ArrayDeque<>();
        rendezVousRequests.push(rendezVousRequest);

        // attend la réponse et  renvoie le message du serveur
        if (!rendezVousRequests.isEmpty()) {
            final ClientRequest joinedRequest = rendezVousRequests.pop();
            joinedRequest.join();
            logger.debug("Thread {} terminé.", joinedRequest.getThreadName());
            String result = (String) joinedRequest.getResult();

            if (result == null) {
                logger.error("Le serveur n'a rien répondu pour le rendez-vous du créneau " + idDisponibilite);
                return "Erreur : Le serveur n'a rien répondu.";
            }

            logger.info("Réponse du serveur : " + result);
            return result;
        }

        logger.error("Problème : la demande de rendez-vous n'a pas pu être envoyée.");
        return "Erreur : Impossible d'envoyer la demande de rendez-vous.";
    }

    // associe un symptôme à un patient
    public Symptomes associerSymptomePatient(int idPatient, Symptomes symptome) throws InterruptedException, IOException {
        //  prépare la requête pour associer un symptôme à un patient
        final ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode dataNode = objectMapper.createObjectNode();
        dataNode.put("id_patient", idPatient);
        if (symptome.getId() > 0) {
            dataNode.put("id_symptome", symptome.getId());
        } else {
            dataNode.put("nom_symptome", symptome.getDescription());
        }
        final String requestId = UUID.randomUUID().toString();
        final Request request = new Request();
        request.setRequestId(requestId);
        request.setRequestOrder("INSERT_PATIENT_SYMPTOME");
        request.setRequestContent(objectMapper.writeValueAsString(dataNode));
        objectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        final byte[] requestBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(request);

        // envoie la requête au serveur
        final InsertPatientSymptomeClientRequest clientRequest = new InsertPatientSymptomeClientRequest(
                networkConfig, 0, request, symptome, requestBytes);
        final Deque<ClientRequest> symptomeRequests = new ArrayDeque<>();
        symptomeRequests.push(clientRequest);

        // attend la réponse et  renvoie le symptôme associé
        if (!symptomeRequests.isEmpty()) {
            final ClientRequest joinedRequest = symptomeRequests.pop();
            joinedRequest.join();
            logger.debug("Thread {} terminé : Symptôme associé au patient {}", 
                    joinedRequest.getThreadName(), idPatient);
            Symptomes result = (Symptomes) joinedRequest.getResult();
            return result != null ? result : null;
        }
        return null;
    }

    //  supprime l'association entre un symptôme et un patient
    public String supprimerSymptomePatient(int idPatient, int idSymptome) throws InterruptedException, IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode dataNode = objectMapper.createObjectNode();
        dataNode.put("id_patient", idPatient);
        dataNode.put("id_symptome", idSymptome);
        final String requestId = UUID.randomUUID().toString();
        final Request request = new Request();
        request.setRequestId(requestId);
        request.setRequestOrder("DELETE_PATIENT_SYMPTOME");
        request.setRequestContent(objectMapper.writeValueAsString(dataNode));
        objectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        final byte[] requestBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(request);
        final Map<String, Integer> data = Map.of("id_patient", idPatient, "id_symptome", idSymptome);
        final DeletePatientSymptomeClientRequest clientRequest = new DeletePatientSymptomeClientRequest(
                networkConfig, 0, request, data, requestBytes);
        final Deque<ClientRequest> symptomeRequests = new ArrayDeque<>();
        symptomeRequests.push(clientRequest);

        if (!symptomeRequests.isEmpty()) {
            final ClientRequest joinedRequest = symptomeRequests.pop();
            joinedRequest.join();
            String result = (String) joinedRequest.getResult();
            logger.debug("Thread {} terminé : Association supprimée pour patient {} et symptôme {} - Résultat: {}", 
                    joinedRequest.getThreadName(), idPatient, idSymptome, result);
            return result != null ? result : "Erreur lors de la suppression de l'association";
        }
        return "Erreur lors de la suppression de l'association";
    }

    //  récupère tous les symptômes associés à un patient
    public List<Symptomes> getSymptomesPatient(int idPatient) throws InterruptedException, IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final String requestId = UUID.randomUUID().toString();
        final Request request = new Request();
        request.setRequestId(requestId);
        request.setRequestOrder("SELECT_PATIENT_SYMPTOMES");
        String jsonifiedId = objectMapper.writeValueAsString(idPatient);
        request.setRequestContent(jsonifiedId);
        objectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        final byte[] requestBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(request);
        final SelectPatientSymptomesClientRequest clientRequest = new SelectPatientSymptomesClientRequest(
                networkConfig, 0, request, idPatient, requestBytes);
        final Deque<ClientRequest> symptomeRequests = new ArrayDeque<>();
        symptomeRequests.push(clientRequest);

        // attend la réponse et  renvoie la liste des symptômes
        if (!symptomeRequests.isEmpty()) {
            final ClientRequest joinedRequest = symptomeRequests.pop();
            joinedRequest.join();
            List<Symptomes> symptomes = (List<Symptomes>) joinedRequest.getResult();
            logger.debug("Thread {} terminé : {} symptômes récupérés pour le patient {}", 
                    joinedRequest.getThreadName(), symptomes != null ? symptomes.size() : 0, idPatient);
            return symptomes != null ? symptomes : new ArrayList<>();
        }
        return new ArrayList<>();
    }

    //  modifie un symptôme associé à un patient
    public Symptomes modifierSymptomePatient(int idPatient, int idAncienSymptome, String nouveauNomSymptome) 
            throws InterruptedException, IOException {
        //  vérifie si le nouveau symptôme existe dans la base
        final ObjectMapper objectMapper = new ObjectMapper();
        List<Symptomes> tousLesSymptomes = selectSymptomes();
        Symptomes symptomeExistant = null;
        for (Symptomes s : tousLesSymptomes) {
            if (s.getDescription().equals(nouveauNomSymptome)) {
                symptomeExistant = s;
                break;
            }
        }
        if (symptomeExistant == null) {
            throw new IOException("Le symptôme '" + nouveauNomSymptome + "' n'existe pas dans la base de données.");
        }

        //  prépare la requête pour modifier l'association
        ObjectNode dataNode = objectMapper.createObjectNode();
        dataNode.put("id_patient", idPatient);
        dataNode.put("id_ancien_symptome", idAncienSymptome);
        dataNode.put("id_nouveau_symptome", symptomeExistant.getId());
        final String requestId = UUID.randomUUID().toString();
        final Request request = new Request();
        request.setRequestId(requestId);
        request.setRequestOrder("MODIFY_PATIENT_SYMPTOME");
        request.setRequestContent(objectMapper.writeValueAsString(dataNode));
        objectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        final byte[] requestBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(request);
        final Map<String, Integer> data = Map.of(
            "id_patient", idPatient,
            "id_ancien_symptome", idAncienSymptome,
            "id_nouveau_symptome", symptomeExistant.getId()
        );
        final ModifyPatientSymptomeClientRequest clientRequest = new ModifyPatientSymptomeClientRequest(
                networkConfig, 0, request, data, requestBytes);
        final Deque<ClientRequest> symptomeRequests = new ArrayDeque<>();
        symptomeRequests.push(clientRequest);

        // attend la réponse et renvoie le nouveau symptôme
        if (!symptomeRequests.isEmpty()) {
            final ClientRequest joinedRequest = symptomeRequests.pop();
            joinedRequest.join();
            Symptomes result = (Symptomes) joinedRequest.getResult();
            logger.debug("Thread {} terminé : Symptôme {} modifié en {} pour le patient {}", 
                    joinedRequest.getThreadName(), idAncienSymptome, symptomeExistant.getDescription(), idPatient);
            return result != null ? result : null;
        }
        return null;
    }


    public List<DiagnosticResult> getPatientDiagnostics(int idPatient) throws InterruptedException, IOException {
    final ObjectMapper objectMapper = new ObjectMapper();
    final String requestId = UUID.randomUUID().toString();
    final Request request = new Request();
    request.setRequestId(requestId);
    request.setRequestOrder("SELECT_PATIENT_DIAGNOSTICS"); 
    request.setRequestContent(objectMapper.writeValueAsString(idPatient));
    objectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
    final byte[] requestBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(request);

    final SelectPatientDiagnosticsClientRequest clientRequest = new SelectPatientDiagnosticsClientRequest(
            networkConfig, 0, request, idPatient, requestBytes); 
    final Deque<ClientRequest> diagnosticRequests = new ArrayDeque<>();
    diagnosticRequests.push(clientRequest);

    if (!diagnosticRequests.isEmpty()) {
        final ClientRequest joinedRequest = diagnosticRequests.pop();
        joinedRequest.join();
        List<DiagnosticResult> diagnostics = (List<DiagnosticResult>) joinedRequest.getResult();
        logger.debug("Thread {} terminé : {} diagnostics récupérés pour le patient {}", 
                joinedRequest.getThreadName(), diagnostics != null ? diagnostics.size() : 0, idPatient);
        return diagnostics != null ? diagnostics : new ArrayList<>();
    }
    return new ArrayList<>();
}
}