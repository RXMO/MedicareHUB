package edu.ezip.ing1.pds;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ezip.ing1.pds.business.dto.Medecins;
import edu.ezip.ing1.pds.business.dto.Patients;
import edu.ezip.ing1.pds.client.commons.ConfigLoader;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.frontend.OrdonnanceController;
import edu.ezip.ing1.pds.services.InteractionService;
import edu.ezip.ing1.pds.services.MedecinService;
import edu.ezip.ing1.pds.services.MedicamentService;
import edu.ezip.ing1.pds.services.OrdonnanceService;
import edu.ezip.ing1.pds.services.PatientService;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class MainFrontEndFX extends Application implements Initializable {
    private final static String LoggingLabel = "FrontEnd";
    private final static Logger logger = LoggerFactory.getLogger(LoggingLabel);
    private final static String networkConfigFile = "network.yaml";
    
    @FXML
    private Button btnMedecins;
    
    @FXML
    private Button btnPatients;
    
    @FXML
    private Button btnDiagnostic;
    
    @FXML
    private Button btnOrdonnances;
    
    private PatientService patientService;
    private Patients patients;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
public void start(Stage primaryStage) {
    try {
        // Charger le fichier FXML avec this comme contrôleur
        URL fxmlUrl = getClass().getResource("/main_view.fxml");
        if (fxmlUrl == null) {
            logger.error("FXML file not found: /main_view.fxml");
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("FXML File Not Found");
            alert.setContentText("Could not find the main_view.fxml file. The application cannot start.");
            alert.showAndWait();
            return;
        }
        
        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        loader.setController(this);
        Parent root = loader.load();
        
        // Configurer la scène
        Scene scene = new Scene(root, 1000, 700);

        
        // Configurer la fenêtre principale
        primaryStage.setTitle("Système de Gestion Médicale");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.centerOnScreen();
        
        // Afficher la fenêtre
        primaryStage.show();
        
        logger.info("Application démarrée avec succès");
    } catch (IOException e) {
        logger.error("Erreur lors du chargement de l'interface : {}", e.getMessage());
        e.printStackTrace();
        
        // Afficher une alerte en cas d'erreur
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Application Error");
        alert.setContentText("Failed to load the application: " + e.getMessage());
        alert.showAndWait();
    }
}
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Charger la configuration du réseau
            final NetworkConfig networkConfig = ConfigLoader.loadConfig(NetworkConfig.class, networkConfigFile);
            logger.debug("Load Network config file : {}", networkConfig.toString());

            // Récupération des patients
            patientService = new PatientService(networkConfig);
            patients = patientService.selectPatients();
        } catch (Exception e) {
            logger.error("Erreur lors de l'initialisation : {}", e.getMessage());
            showErrorAlert("Initialisation", e);
        }
    }
    
    @FXML
    private void handleMedecinsAction() {
        try {
            // Charger la configuration réseau
            NetworkConfig networkConfig = ConfigLoader.loadConfig(NetworkConfig.class, networkConfigFile);

            // Initialiser le service médecin
            MedecinService medecinService = new MedecinService(networkConfig);

            // Récupérer la liste des médecins
            Medecins medecins = medecinService.selectMedecins();

            // Lancer l'interface du médecin avec les bons arguments
            // Note: MedecinUI devrait être converti en JavaFX aussi
            new MedecinUI(medecins, medecinService);
        } catch (Exception ex) {
            showErrorAlert("Médecins", ex);
        }
    }
    
    @FXML
    private void handlePatientsAction() {
        try {
            // Note: PatientUI devrait être converti en JavaFX aussi
            new PatientUI(patients, patientService);
        } catch (Exception ex) {
            showErrorAlert("Patients", ex);
        }
    }
    
    @FXML
    private void handleDiagnosticAction() {
        try {
            // Note: FenetreDiagnostic devrait être converti en JavaFX aussi
            //FenetreDiagnostic fenetreDiagnostic = new FenetreDiagnostic();
             //fenetreDiagnostic.show();
        } catch (Exception ex) {
            showErrorAlert("Diagnostic", ex);
        }
    }
    
    @FXML   
    private void handleOrdonnancesAction() {
    try {
        // Charger la configuration réseau à partir du fichier "network.yaml"
        NetworkConfig networkConfig = ConfigLoader.loadConfig(NetworkConfig.class, networkConfigFile);
        
        // Initialiser les services avec la configuration réseau
        OrdonnanceService ordonnanceService = new OrdonnanceService(networkConfig);
        MedicamentService medicamentService = new MedicamentService(networkConfig);
        InteractionService interactionService = new InteractionService(networkConfig);
        
        // Charger le fichier FXML de l'interface Ordonnance
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Ordonnance.fxml"));
        
        // Créer une instance du contrôleur et injecter les services
        OrdonnanceController controller = new OrdonnanceController(ordonnanceService, medicamentService, interactionService);
        loader.setController(controller);
        
        // Charger la vue
        Parent root = loader.load();
        
        // Créer et configurer la scène
        Scene scene = new Scene(root);
        Stage stage = new Stage();
        stage.setScene(scene);
        
        // Afficher la fenêtre
        stage.show();
        
    } catch (IOException ex) {
        showErrorAlert("Ordonnances", ex);
    }
}
    
   /*  @FXML
    private void handleOrdonnancesAction() {
        try {
            // Charger la configuration réseau
            NetworkConfig networkConfig = ConfigLoader.loadConfig(NetworkConfig.class, networkConfigFile);
            
            // Initialiser le service médecin pour l'authentification
            MedecinService medecinService = new MedecinService(networkConfig);
            
            // Charger et afficher la fenêtre d'authentification
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cnx_medecin.fxml"));
            
            // Créer le contrôleur et l'associer au fichier FXML
            ConnexionMedecinController controller = new ConnexionMedecinController(networkConfig, medecinService);
            loader.setController(controller);
            
            // Créer la scène
            Parent root = loader.load();
            Scene scene = new Scene(root);
            
            // Configurer la fenêtre
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Authentification Médecin");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setResizable(false);
            dialogStage.setScene(scene);
            
            // Afficher la fenêtre et attendre qu'elle soit fermée
            dialogStage.showAndWait();
            
        } catch (Exception ex) {
            showErrorAlert("Ordonnances", ex);
        }
    }*/

    // Méthode pour afficher une alerte d'erreur
    private void showErrorAlert(String module, Exception ex) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText("Erreur lors de l'ouverture de l'interface " + module.toLowerCase());
        alert.setContentText(ex.getMessage());
        
        logger.error("Erreur lors de l'ouverture de l'interface {} : {}", module.toLowerCase(), ex.getMessage());
        ex.printStackTrace();
        
        alert.showAndWait();
    }
}