package edu.ezip.ing1.pds.frontend;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ezip.ing1.pds.business.dto.Medicament;
import edu.ezip.ing1.pds.business.dto.Medicaments;
import edu.ezip.ing1.pds.business.dto.Ordonnance;
import edu.ezip.ing1.pds.business.dto.Ordonnances;
import edu.ezip.ing1.pds.client.commons.ConfigLoader;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.services.MedicamentService;
import edu.ezip.ing1.pds.services.OrdonnanceService;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class OrdonnanceFront extends Application implements Initializable {
    
    private final static String LoggingLabel = "FrontEnd - Ordonnance";
    private final static Logger logger = LoggerFactory.getLogger(LoggingLabel);
    private final static String networkConfigFile = "network.yaml";

    private OrdonnanceService ordonnanceService;
    private MedicamentService medicamentService;
    
    @FXML
    private TextField idPatientField, idConsultationField, idMedecinField, searchMedicamentField;
    
    @FXML
    private TextArea displayArea;
    
    @FXML
    private VBox medicamentCheckboxContainer;
    
    @FXML
    private ScrollPane medicamentScrollPane;
    
    @FXML
    private Button addButton, displayButton, deleteButton, modifyButton, confirmButton;
    
    
    private List<Medicament> medicamentsList;
    private List<CheckBox> medicamentCheckboxes;
    private boolean enModeModification = false;
    private int currentOrdonnanceId = -1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            NetworkConfig networkConfig = ConfigLoader.loadConfig(NetworkConfig.class, networkConfigFile);
            this.ordonnanceService = new OrdonnanceService(networkConfig);
            this.medicamentService = new MedicamentService(networkConfig);
            
            initializeMedicaments();
            setupEventHandlers();
            
        } catch (Exception e) {
            logger.error("Erreur lors de l'initialisation", e);
            showAlert(AlertType.ERROR, "Erreur d'initialisation", e.getMessage());
        }
    }
    
    private void initializeMedicaments() {
        try {
            Medicaments medicaments = medicamentService.selectMedicaments();
            medicamentsList = new ArrayList<>(medicaments.getMedicaments());
            medicamentCheckboxes = new ArrayList<>();
            
            // Vider le conteneur existant
            medicamentCheckboxContainer.getChildren().clear();
            
            // Ajouter les médicaments avec checkbox et champ de description
            for (Medicament medicament : medicamentsList) {
                // Créer la checkbox pour le médicament
                CheckBox checkBox = new CheckBox(medicament.getNomMedicament());
                medicamentCheckboxes.add(checkBox);
                
                // Créer le champ de posologie
                TextField descriptionField = new TextField();
                descriptionField.setPromptText("Posologie...");
                descriptionField.setPrefWidth(200);
                // MODIFICATION: Désactiver le champ initialement si la checkbox n'est pas sélectionnée
                descriptionField.setDisable(!checkBox.isSelected());
                
                // Ajouter un listener pour activer/désactiver le champ selon l'état de la checkbox
                checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                    // MODIFICATION: Activer/désactiver le champ selon l'état de la checkbox
                    descriptionField.setDisable(!newValue);
                    if (!newValue) {
                        descriptionField.clear();
                    }
                });
                
                // Créer un conteneur pour la checkbox et le champ
                HBox medicamentEntry = new HBox(10);
                medicamentEntry.setAlignment(Pos.CENTER_LEFT);
                medicamentEntry.getChildren().addAll(checkBox, descriptionField);
                
                // Ajouter au conteneur principal
                medicamentCheckboxContainer.getChildren().add(medicamentEntry);
            }
            
            if (medicamentsList.isEmpty()) {
                Label noMedicamentsLabel = new Label("Aucun médicament disponible");
                medicamentCheckboxContainer.getChildren().add(noMedicamentsLabel);
            }
            
        } catch (Exception e) {
            logger.error("Erreur lors du chargement des médicaments", e);
            showAlert(AlertType.ERROR, "Erreur", "Impossible de charger les médicaments: " + e.getMessage());
        }
    }

// Méthode pour obtenir les médicaments sélectionnés avec leur description
private Map<Medicament, String> getSelectedMedicamentsWithDescription() {
    Map<Medicament, String> selectedMedicaments = new HashMap<>();
    
    // Parcourir tous les éléments enfants du conteneur
    for (Node node : medicamentCheckboxContainer.getChildren()) {
        if (node instanceof HBox) {
            HBox hbox = (HBox) node;
            
            // Vérifier si le HBox contient une CheckBox et un TextField
            if (hbox.getChildren().size() >= 2 && 
                hbox.getChildren().get(0) instanceof CheckBox && 
                hbox.getChildren().get(1) instanceof TextField) {
                
                CheckBox checkBox = (CheckBox) hbox.getChildren().get(0);
                TextField descField = (TextField) hbox.getChildren().get(1);
                
                // Si la checkbox est sélectionnée, ajouter le médicament et sa description
                if (checkBox.isSelected()) {
                    String medicamentName = checkBox.getText();
                    
                    // Trouver le médicament correspondant
                    Medicament med = medicamentsList.stream()
                            .filter(m -> m.getNomMedicament().equals(medicamentName))
                            .findFirst()
                            .orElse(null);
                    
                    if (med != null) {
                        selectedMedicaments.put(med, descField.getText());
                    }
                }
            }
        }
    }
    
    return selectedMedicaments;
}
    

private void filterMedicaments(String searchText) {
    medicamentCheckboxContainer.getChildren().clear();
    boolean foundResults = false;
    
    if (searchText.isEmpty()) {
        // Afficher tous les médicaments
        for (int i = 0; i < medicamentCheckboxes.size(); i++) {
            CheckBox checkBox = medicamentCheckboxes.get(i);
            
            // Créer le TextField pour la posologie
            TextField descriptionField = new TextField();
            descriptionField.setPromptText("Posologie...");
            descriptionField.setPrefWidth(200);
            descriptionField.setDisable(!checkBox.isSelected());
            
            // Associer le comportement du TextField à la CheckBox
            int index = i; 
            checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                descriptionField.setDisable(!newValue);
                if (!newValue) {
                    descriptionField.clear();
                }
            });
            
            // Créer le HBox pour contenir la CheckBox et le TextField
            HBox medicamentEntry = new HBox(10);
            medicamentEntry.setAlignment(Pos.CENTER_LEFT);
            medicamentEntry.getChildren().addAll(checkBox, descriptionField);
            
            medicamentCheckboxContainer.getChildren().add(medicamentEntry);
            foundResults = true;
        }
    } else {
        // Filtrer les médicaments
        for (int i = 0; i < medicamentCheckboxes.size(); i++) {
            CheckBox checkBox = medicamentCheckboxes.get(i);
            String medicamentName = checkBox.getText().toLowerCase();
            
            if (medicamentName.contains(searchText)) {
                // Créer le TextField pour la posologie
                TextField descriptionField = new TextField();
                descriptionField.setPromptText("Posologie...");
                descriptionField.setPrefWidth(200);
                descriptionField.setDisable(!checkBox.isSelected());
                
                // Associer le comportement du TextField à la CheckBox
                checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                    descriptionField.setDisable(!newValue);
                    if (!newValue) {
                        descriptionField.clear();
                    }
                });
                
                // Créer le HBox pour contenir la CheckBox et le TextField
                HBox medicamentEntry = new HBox(10);
                medicamentEntry.setAlignment(Pos.CENTER_LEFT);
                medicamentEntry.getChildren().addAll(checkBox, descriptionField);
                
                medicamentCheckboxContainer.getChildren().add(medicamentEntry);
                foundResults = true;
            }
        }
    }
    
    // Si aucun médicament ne correspond à la recherche, afficher un message
    if (!foundResults) {
        Label noResultLabel = new Label("Aucun médicament correspondant à \"" + searchText + "\"");
        medicamentCheckboxContainer.getChildren().add(noResultLabel);
    }
    
    logger.debug("Recherche de médicaments pour '" + searchText + "' : " + 
            (foundResults ? medicamentCheckboxContainer.getChildren().size() : 0) + 
            " résultats trouvés");
}
    
private void handleSaveButton() {
    String idPatient = idPatientField.getText();
    String idConsultationStr = idConsultationField.getText();
    String idMedecinStr = idMedecinField.getText();

    // Récupérer les médicaments sélectionnés avec leur posologie
    Map<Medicament, String> selectedMedicamentsWithDescription = getSelectedMedicamentsWithDescription();
    
    // Créer une liste de chaînes qui incluent le nom du médicament et sa posologie
    List<String> medicamentsWithPosology = new ArrayList<>();
    for (Map.Entry<Medicament, String> entry : selectedMedicamentsWithDescription.entrySet()) {
        String medicamentName = entry.getKey().getNomMedicament();
        String posology = entry.getValue().trim();
        
    
        if (posology.isEmpty()) {
            medicamentsWithPosology.add(medicamentName);
        } else {
            medicamentsWithPosology.add(medicamentName + " (" + posology + ")");
        }
    }
    
    // Pour la vérification des principes actifs
    List<String> selectedMedicaments = selectedMedicamentsWithDescription.keySet()
        .stream()
        .map(Medicament::getNomMedicament)
        .collect(Collectors.toList());

    if (idPatient.isEmpty() || idConsultationStr.isEmpty() || idMedecinStr.isEmpty() || selectedMedicaments.isEmpty()) {
        showAlert(AlertType.WARNING, "Informations manquantes", 
                 "Tous les champs d'ID doivent être remplis et au moins un médicament doit être sélectionné");
        return;
    }

    // Vérifier les principes actifs dupliqués
    String duplicateError = checkDuplicatePrincipesActifs(selectedMedicaments);
    if (duplicateError != null) {
        showAlert(AlertType.WARNING, "Risque d'interaction médicamenteuse", 
                "Attention: Vous avez sélectionné des médicaments avec le même principe actif!\n\n" + duplicateError);
        return;
    }

    try {
        int idConsultation = Integer.parseInt(idConsultationStr);
        int idMedecinInt = Integer.parseInt(idMedecinStr);
        int idPatientInt = Integer.parseInt(idPatient);

        // Afficher les données saisies dans displayArea avant d'enregistrer
        clearDisplayArea(); // Effacer le contenu précédent
        displayArea.appendText("Données à enregistrer :\n");
        displayArea.appendText("ID Patient: " + idPatient + "\n");
        displayArea.appendText("ID Consultation: " + idConsultation + "\n");
        displayArea.appendText("ID Médecin: " + idMedecinInt + "\n");
        displayArea.appendText("Médicaments: \n");
        
        // Afficher les médicaments avec leur posologie
        for (String medWithPosology : medicamentsWithPosology) {
            displayArea.appendText("- " + medWithPosology + "\n");
        }
        displayArea.appendText("-----------------------------\n");

        Ordonnance newOrdonnance = new Ordonnance();
        newOrdonnance.setIdOrdonnance(Ordonnance.generateIdOrdonnance());
        newOrdonnance.setIdPatient(idPatientInt);
        newOrdonnance.setIdConsultation(idConsultation);
        newOrdonnance.setIdMedecin(idMedecinInt);

        // Modifier l'appel pour utiliser la liste avec posologie
        ordonnanceService.insertOrdonnance(newOrdonnance, medicamentsWithPosology);

        showAlert(AlertType.INFORMATION, "Succès", "Ordonnance enregistrée avec succès");
    }   catch (Exception ex) {
    // Vérifier si l'exception ou sa cause contient le message concernant le médecin
    String errorMessage = ex.getMessage();
    Throwable cause = ex.getCause();
    
    // Vérifier le message de l'exception et de ses causes
    while (errorMessage == null && cause != null) {
        errorMessage = cause.getMessage();
        cause = cause.getCause();
    }
    
    // Traiter le message d'erreur
    if (errorMessage != null) {
        if (errorMessage.contains("Le médecin avec l'ID") && errorMessage.contains("n'existe pas")) {
            showAlert(AlertType.ERROR, "Médecin inexistant", 
                     "Le médecin avec l'ID spécifié n'existe pas dans la base de données.");
        } else if (errorMessage.contains("Le patient avec l'ID") && errorMessage.contains("n'existe pas")) {
            showAlert(AlertType.ERROR, "Patient inexistant", 
                     "Le patient avec l'ID spécifié n'existe pas dans la base de données.");
        } else {
            // Essayer d'extraire les erreurs depuis les logs
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String stackTrace = sw.toString();
            
            if (stackTrace.contains("Le médecin avec l'ID")) {
                showAlert(AlertType.ERROR, "Médecin inexistant", 
                         "Le médecin avec l'ID spécifié n'existe pas dans la base de données.");
            } else {
                showAlert(AlertType.ERROR, "Erreur", "Une erreur est survenue: " + errorMessage);
            }
        }
    } else {
        showAlert(AlertType.ERROR, "Erreur", "Une erreur est survenue lors du traitement de la demande.");
    }
    
    logger.error("Erreur lors de l'insertion de l'ordonnance", ex);
}
}
    private void handleDeleteButton() {
        if (currentOrdonnanceId == -1) {
            showAlert(AlertType.ERROR, "Erreur", "Veuillez d'abord sélectionner une ordonnance à supprimer.");
            return;
        }

        try {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText("Suppression d'ordonnance");
            alert.setContentText("Êtes-vous sûr de vouloir supprimer cette ordonnance ?");
            
            alert.showAndWait().ifPresent(response -> {
                if (response == javafx.scene.control.ButtonType.OK) {
                    try {
                        Ordonnance ordonnanceToDelete = new Ordonnance();
                        ordonnanceToDelete.setIdOrdonnance(currentOrdonnanceId);

                        logger.debug("Tentative de suppression de l'ordonnance avec ID: " + currentOrdonnanceId);
                        ordonnanceService.deleteOrdonnance(ordonnanceToDelete);
                        logger.debug("Suppression de l'ordonnance réussie");

                        showAlert(AlertType.INFORMATION, "Succès", "Ordonnance supprimée avec succès.");
                        resetFields();
                    } catch (Exception ex) {
                        logger.error("Erreur lors de la suppression", ex);
                        showAlert(AlertType.ERROR, "Erreur", "Erreur lors de la suppression: " + ex.getMessage());
                    }
                }
            });
        } catch (Exception ex) {
            showAlert(AlertType.ERROR, "Erreur", "Erreur lors de la suppression de l'ordonnance: " + ex.getMessage());
            logger.error("Erreur lors de la suppression de l'ordonnance", ex);
        }
    }
    
    private void handleModifyButton() {
        try {
            if (currentOrdonnanceId == -1) {
                showAlert(AlertType.WARNING, "Sélection requise", "Veuillez sélectionner une ordonnance à modifier.");
                return;
            }
            
            
            enModeModification = true;
            
            
            confirmButton.setVisible(true);
            
            
            List<String> medicamentsOrdonnance = ordonnanceService.getMedicamentsByOrdonnance(currentOrdonnanceId);
            
            
            for (CheckBox checkBox : medicamentCheckboxes) {
                checkBox.setSelected(medicamentsOrdonnance.contains(checkBox.getText()));
            }
            
        } catch (Exception ex) {
            logger.error("Erreur lors de la préparation de la modification", ex);
            showAlert(AlertType.ERROR, "Erreur", "Erreur lors de la préparation de la modification: " + ex.getMessage());
        }
    }
    
    private void handleConfirmButton() {
        try {
            if (currentOrdonnanceId == -1) {
                showAlert(AlertType.ERROR, "Erreur", "ID d'ordonnance invalide.");
                return;
            }
    
           
            List<String> selectedMedicamentNames = medicamentCheckboxes.stream()
                .filter(CheckBox::isSelected)
                .map(CheckBox::getText)
                .collect(Collectors.toList());
    
            if (selectedMedicamentNames.isEmpty()) {
                showAlert(AlertType.WARNING, "Sélection requise", "Veuillez sélectionner au moins un médicament.");
                return;
            }
    
            String duplicateMessage = checkDuplicatePrincipesActifs(selectedMedicamentNames);
            if (duplicateMessage != null) {
                Alert alert = new Alert(AlertType.WARNING);
                alert.setTitle("Risque d'interaction médicamenteuse");
                alert.setHeaderText("Principes actifs dupliqués détectés");
                alert.setContentText("Attention: Vous avez sélectionné des médicaments avec le même principe actif!\n\n" + 
                                    duplicateMessage + "\n\nVoulez-vous continuer quand même?");
                
                alert.getButtonTypes().setAll(javafx.scene.control.ButtonType.YES, javafx.scene.control.ButtonType.NO);
                
                alert.showAndWait().ifPresent(response -> {
                    if (response != javafx.scene.control.ButtonType.YES) {
                        return;
                    }
                });
            }
    
            // Vérifier que tous les champs sont remplis
            if (idPatientField.getText().isEmpty() || idConsultationField.getText().isEmpty() || 
                idMedecinField.getText().isEmpty()) {
                showAlert(AlertType.WARNING, "Informations manquantes", "Tous les champs d'ID doivent être remplis");
                return;
            }
    
            // Créer l'objet Ordonnance pour la mise à jour
            Ordonnance ordonnanceToUpdate = new Ordonnance();
            ordonnanceToUpdate.setIdOrdonnance(currentOrdonnanceId);
            ordonnanceToUpdate.setIdPatient(Integer.parseInt(idPatientField.getText()));
            ordonnanceToUpdate.setIdConsultation(Integer.parseInt(idConsultationField.getText()));
            ordonnanceToUpdate.setIdMedecin(Integer.parseInt(idMedecinField.getText()));
    
            // Mettre à jour l'ordonnance avec les médicaments
            logger.debug("Tentative de mise à jour de l'ordonnance ID: " + currentOrdonnanceId);
            boolean success = ordonnanceService.updateOrdonnance(ordonnanceToUpdate, selectedMedicamentNames);
            
            if (success) {
                logger.debug("Mise à jour réussie");
                // Afficher les données modifiées dans le displayArea
                clearDisplayArea();
                displayArea.appendText("Ordonnance modifiée :\n");
                displayArea.appendText("ID Ordonnance: " + currentOrdonnanceId + "\n");
                displayArea.appendText("ID Patient: " + idPatientField.getText() + "\n");
                displayArea.appendText("ID Consultation: " + idConsultationField.getText() + "\n");
                displayArea.appendText("ID Médecin: " + idMedecinField.getText() + "\n");
                displayArea.appendText("Médicaments: " + String.join(", ", selectedMedicamentNames) + "\n");
                displayArea.appendText("-----------------------------\n");
                
                // Réinitialiser l'UI et les variables d'état
                actualiserOrdonnances();
                
                // Réinitialiser le mode de modification
                enModeModification = false;
                
                // Masquer le bouton confirmer
                confirmButton.setVisible(false);
                
                // Réinitialiser currentOrdonnanceId
                currentOrdonnanceId = -1;
                
                showAlert(AlertType.INFORMATION, "Succès", "Ordonnance modifiée avec succès");
            }
        } catch (NumberFormatException ex) {
            showAlert(AlertType.ERROR, "Erreur de format", "Veuillez entrer des valeurs valides pour les ID.");
            logger.error("Erreur de format de nombre", ex);
        } catch (Exception ex) {
            showAlert(AlertType.ERROR, "Erreur", "Erreur lors de la modification de l'ordonnance: " + ex.getMessage());
            logger.error("Erreur lors de la modification", ex);
        }
    }
private void actualiserOrdonnances() {
    try {
        logger.debug("Tentative de récupération des ordonnances...");
        Ordonnances ordonnances = ordonnanceService.selectOrdonnances();
        
        if (ordonnances != null) {
            Set<Ordonnance> ordonnancesSet = ordonnances.getOrdonnances();
            List<Ordonnance> ordonnancesList = new ArrayList<>(ordonnancesSet);

            ordonnancesList.sort(Comparator.comparing(Ordonnance::getIdOrdonnance));
            
            logger.debug("Nombre d'ordonnances récupérées : " + ordonnancesList.size());
            
            try {
                // Charger le FXML
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/OrdonnanceList.fxml"));
                Parent root = loader.load();
                
                // Récupérer le contrôleur
                OrdonnanceListController controller = loader.getController();
                controller.setMainController(this);
                controller.loadOrdonnances(ordonnancesList);
                
                // Créer et configurer la scène
                Scene scene = new Scene(root);
                Stage stage = new Stage();
                stage.setTitle("Liste des Ordonnances");
                stage.setScene(scene);
                stage.initModality(Modality.APPLICATION_MODAL);
                
                // Afficher la fenêtre
                stage.showAndWait();
                
            } catch (IOException e) {
                logger.error("Erreur lors du chargement du FXML de la liste des ordonnances", e);
                showAlert(AlertType.ERROR, "Erreur", "Erreur lors du chargement de la liste: " + e.getMessage());
            }
            
        } else {
            logger.debug("Aucune ordonnance trouvée");
            showAlert(AlertType.INFORMATION, "Information", "Aucune ordonnance trouvée.");
        }
    } catch (Exception ex) {
        logger.error("Erreur lors de l'affichage des ordonnances", ex);
        showAlert(AlertType.ERROR, "Erreur", "Erreur lors de l'affichage des ordonnances: " + ex.getMessage());
    }
}


public void loadOrdonnanceForEditing(Ordonnance ordonnance) {
    // Mettre à jour les champs du formulaire avec les valeurs de l'ordonnance sélectionnée
    currentOrdonnanceId = ordonnance.getIdOrdonnance();
    idPatientField.setText(String.valueOf(ordonnance.getIdPatient()));
    idConsultationField.setText(String.valueOf(ordonnance.getIdConsultation()));
    idMedecinField.setText(String.valueOf(ordonnance.getIdMedecin()));
    
    try {
        // Charger les médicaments associés à cette ordonnance
        List<String> medicamentsOrdonnance = ordonnanceService.getMedicamentsByOrdonnance(currentOrdonnanceId);
        
        // Mettre à jour les checkboxes 
        for (Node node : medicamentCheckboxContainer.getChildren()) {
            if (node instanceof HBox) {
                HBox hbox = (HBox) node;
                if (hbox.getChildren().size() >= 2 && hbox.getChildren().get(0) instanceof CheckBox) {
                    CheckBox checkBox = (CheckBox) hbox.getChildren().get(0);
                    checkBox.setSelected(medicamentsOrdonnance.contains(checkBox.getText()));
                    
                    // Si la checkbox a un champ de texte associé, mettre à jour son état enabled/disabled
                    if (hbox.getChildren().get(1) instanceof TextField) {
                        ((TextField) hbox.getChildren().get(1)).setDisable(!checkBox.isSelected());
                    }
                }
            }
        }
        
        // Afficher les informations dans la zone d'affichage
        clearDisplayArea();
        displayArea.appendText("Ordonnance chargée pour modification :\n");
        displayArea.appendText("ID Ordonnance: " + currentOrdonnanceId + "\n");
        displayArea.appendText("ID Patient: " + ordonnance.getIdPatient() + "\n");
        displayArea.appendText("ID Consultation: " + ordonnance.getIdConsultation() + "\n");
        displayArea.appendText("ID Médecin: " + ordonnance.getIdMedecin() + "\n");
        displayArea.appendText("Médicaments: " + String.join(", ", medicamentsOrdonnance) + "\n");
        displayArea.appendText("-----------------------------\n");
        
    } catch (Exception e) {
        logger.error("Erreur lors du chargement des médicaments pour l'ordonnance", e);
        showAlert(AlertType.ERROR, "Erreur", "Erreur lors du chargement des médicaments: " + e.getMessage());
    }
}
  
    private void resetFields() {
        idPatientField.setText("");
        idConsultationField.setText("");
        idMedecinField.setText("");
        searchMedicamentField.setText("");
        
        // Décocher toutes les cases
        for (CheckBox checkbox : medicamentCheckboxes) {
            checkbox.setSelected(false);
        }
        
        // Afficher tous les médicaments
        filterMedicaments("");
        
        // Réinitialiser les variables d'état
        currentOrdonnanceId = -1;
        enModeModification = false;
    }

    private void clearDisplayArea() {
        displayArea.setText("");
    }
    
    private String checkDuplicatePrincipesActifs(List<String> selectedMedicamentNames) {
        System.out.println("====== VÉRIFICATION DES PRINCIPES ACTIFS ======");
        System.out.println("Médicaments sélectionnés: " + selectedMedicamentNames);

        System.out.println("--- Principes actifs disponibles ---");
        for (Medicament med : medicamentsList) {
            System.out.println(med.getNomMedicament() + " => " + med.getPrincipeActif());
        }
        
        Map<String, List<String>> principeActifMap = new HashMap<>();

        for (String medicamentName : selectedMedicamentNames) {
            Medicament selectedMed = medicamentsList.stream()
                    .filter(med -> med.getNomMedicament().equals(medicamentName))
                    .findFirst()
                    .orElse(null);

            if (selectedMed != null && selectedMed.getPrincipeActif() != null
                    && !selectedMed.getPrincipeActif().trim().isEmpty()) {
                String principeActif = selectedMed.getPrincipeActif();
                if (!principeActifMap.containsKey(principeActif)) {
                    principeActifMap.put(principeActif, new ArrayList<>());
                }
                principeActifMap.get(principeActif).add(medicamentName);
            }
        }

        StringBuilder errorMessage = new StringBuilder();
        for (Map.Entry<String, List<String>> entry : principeActifMap.entrySet()) {
            if (entry.getValue().size() > 1) {
                errorMessage.append("Principe actif \"").append(entry.getKey())
                        .append("\" présent dans plusieurs médicaments: ")
                        .append(String.join(", ", entry.getValue())).append("\n");
            }
        }

        return errorMessage.length() > 0 ? errorMessage.toString() : null;
    }

    private void handleSearchMedicament(KeyEvent event) {
        String searchText = searchMedicamentField.getText().toLowerCase();
        filterMedicaments(searchText);
    }

    private void setupEventHandlers() {
        // Recherche de médicament
        searchMedicamentField.setOnKeyReleased(this::handleSearchMedicament);
        
        // Boutons
        addButton.setOnAction(event -> handleSaveButton());
        displayButton.setOnAction(event -> actualiserOrdonnances());
        deleteButton.setOnAction(event -> handleDeleteButton());
        modifyButton.setOnAction(event -> handleModifyButton());
        confirmButton.setOnAction(event -> handleConfirmButton());
        
        // Masquer le bouton de confirmation initialement
        confirmButton.setVisible(false);
    }
    
    
    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Ordonnance.fxml"));
            loader.setController(this);
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            primaryStage.setTitle("Gestion des Ordonnances");
            primaryStage.setScene(scene);
            primaryStage.show();
            
        } catch (IOException e) {
            logger.error("Erreur lors du chargement du FXML", e);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}