package edu.ezip.ing1.pds.frontend;

import java.net.URL;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ezip.ing1.pds.business.dto.Medecin;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.services.MedecinService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ConnexionMedecinController implements Initializable {
    private final static String LoggingLabel = "ConnexionMedecinController";
    private final static Logger logger = LoggerFactory.getLogger(LoggingLabel);
    
    @FXML
    private TextField idField;
    
    @FXML
    private TextField nomField;
    
    @FXML
    private Button validerButton;
    
    @FXML
    private Button annulerButton;
    
    private MedecinService medecinService;
    private NetworkConfig networkConfig;
    
    public ConnexionMedecinController(NetworkConfig networkConfig, MedecinService medecinService) {
        this.networkConfig = networkConfig;
        this.medecinService = medecinService;
    }

    /*public ConnexionMedecinController(NetworkConfig networkConfig, MedecinService medecinService) {
        throw new UnsupportedOperationException("Not supported yet.");
    }*/
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        validerButton.setOnAction(event -> handleConnexion());
        annulerButton.setOnAction(event -> handleAnnuler());
    }
    
    @FXML
    private void handleConnexion() {
        try {
            // Récupérer les valeurs saisies
            String idMedecinStr = idField.getText().trim();
            String nomMedecin = nomField.getText().trim();
            
            // Vérifier que les champs ne sont pas vides
            if (idMedecinStr.isEmpty() || nomMedecin.isEmpty()) {
                showAlert(AlertType.WARNING, "Champs manquants", "Veuillez saisir l'ID et le nom du médecin.");
                return;
            }
            
            // Convertir l'ID en entier
            int idMedecin;
            try {
                idMedecin = Integer.parseInt(idMedecinStr);
            } catch (NumberFormatException e) {
                showAlert(AlertType.ERROR, "Format incorrect", "L'ID du médecin doit être un nombre entier.");
                return;
            }
            
            // Créer un objet Medecin pour l'authentification
            Medecin medecin = new Medecin();
            medecin.setIdMedecin(idMedecin);
            medecin.setNomMedecin(nomMedecin);
            
            // Vérifier les informations d'authentification
            boolean authentificationReussie = verifierAuthentification(medecin);
            
            if (authentificationReussie) {
                logger.info("Authentification réussie pour le médecin: ID={}, Nom={}", idMedecin, nomMedecin);
                
                // Fermer la fenêtre de connexion
                Stage stage = (Stage) validerButton.getScene().getWindow();
                stage.close();
                
                // Ouvrir l'interface d'ordonnances
                //ouvrirInterfaceOrdonnances(medecin);
            } else {
                showAlert(AlertType.ERROR, "Authentification échouée", 
                          "Les informations d'identification sont incorrectes. Veuillez réessayer.");
            }
        } catch (Exception e) {
            logger.error("Erreur lors de l'authentification : {}", e.getMessage());
            showAlert(AlertType.ERROR, "Erreur d'authentification", 
                      "Une erreur est survenue lors de l'authentification : " + e.getMessage());
        }
    }
    
    @FXML
    private void handleAnnuler() {
        // Fermer la fenêtre sans continuer
        Stage stage = (Stage) annulerButton.getScene().getWindow();
        stage.close();
    }
    
    private boolean verifierAuthentification(Medecin medecin) {
        try {
            // Tentative d'authentification réelle via le service
            // Nous pourrions utiliser VerifierConnexionMedecinClientRequest ici
            // Pour l'instant, nous considérons l'authentification comme réussie si les données sont fournies
            
            // TODO: Ajouter la vérification réelle avec le backend
            // Exemple d'implémentation future:
            /*
            String requestId = UUID.randomUUID().toString();
            Request request = new Request();
            request.setRequestId(requestId);
            request.setRequestOrder("VERIFIER_CONNEXION_MEDECIN");
            
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonifiedMedecin = objectMapper.writeValueAsString(medecin);
            request.setRequestContent(jsonifiedMedecin);
            
            objectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
            byte[] requestBytes = objectMapper.writeValueAsBytes(request);
            
            VerifierConnexionMedecinClientRequest connexionRequest = 
                new VerifierConnexionMedecinClientRequest(networkConfig, 0, request, medecin, requestBytes);
            
            connexionRequest.join();
            return (Boolean) connexionRequest.getResult();
            */
            
            // Pour l'instant, on simule une authentification réussie
            logger.info("Simulation d'authentification pour le médecin ID={}, Nom={}", 
                       medecin.getIdMedecin(), medecin.getNomMedecin());
            return true;
        } catch (Exception e) {
            logger.error("Erreur lors de la vérification d'authentification : {}", e.getMessage());
            return false;
        }
    }
    
    /*private void ouvrirInterfaceOrdonnances(Medecin medecin) {
        try {
            // Initialiser les services nécessaires
            OrdonnanceService ordonnanceService = new OrdonnanceService(networkConfig);
            MedicamentService medicamentService = new MedicamentService(networkConfig);
            InteractionService interactionService = new InteractionService(networkConfig);
            
            // Charger l'interface d'ordonnances
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Ordonnance.fxml"));
            
            // Créer et configurer le contrôleur
            OrdonnanceController controller = new OrdonnanceController(ordonnanceService, medicamentService, interactionService);
            controller.setMedecinConnecte(medecin); // Transmettre l'information du médecin connecté
            loader.setController(controller);
            
            // Charger la vue
            Parent root = loader.load();
            
            // Créer et configurer la scène
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("Gestion des Ordonnances - Médecin: " + medecin.getNomMedecin());
            stage.setScene(scene);
            
            // Afficher la fenêtre
            stage.show();
            
        } catch (IOException e) {
            logger.error("Erreur lors de l'ouverture de l'interface d'ordonnances : {}", e.getMessage());
            showAlert(AlertType.ERROR, "Erreur", 
                     "Impossible d'ouvrir l'interface d'ordonnances : " + e.getMessage());
        }
    }*/
    
    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}