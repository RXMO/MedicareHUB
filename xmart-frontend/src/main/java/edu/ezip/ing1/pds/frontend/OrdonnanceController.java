package edu.ezip.ing1.pds.frontend;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;
import org.json.JSONObject;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import edu.ezip.ing1.pds.business.dto.Medicament;
import edu.ezip.ing1.pds.business.dto.Medicaments;
import edu.ezip.ing1.pds.business.dto.Ordonnance;
import edu.ezip.ing1.pds.business.dto.Ordonnances;
import edu.ezip.ing1.pds.services.InteractionService;
import edu.ezip.ing1.pds.services.MedicamentService;
import edu.ezip.ing1.pds.services.OrdonnanceService;
import javafx.application.Platform;
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

public class OrdonnanceController implements Initializable {
    
    private final static String LoggingLabel = "FrontEnd - Ordonnance";
    private final static Logger logger = LoggerFactory.getLogger(LoggingLabel);

    private OrdonnanceService ordonnanceService;
    private MedicamentService medicamentService;
    private InteractionService interactionService;
    
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

    // Constructeur pour injection des services
    public OrdonnanceController(OrdonnanceService ordonnanceService, MedicamentService medicamentService, 
                           InteractionService interactionService) {
        this.ordonnanceService = ordonnanceService;
        this.medicamentService = medicamentService;
        this.interactionService = interactionService;
    }
    
    // Constructeur par défaut pour FXML
    public OrdonnanceController() {
       
    }

    // Méthode pour définir les services après l'initialisation FXML
    public void setServices(OrdonnanceService ordonnanceService, MedicamentService medicamentService, 
                         InteractionService interactionService) {
        this.ordonnanceService = ordonnanceService;
        this.medicamentService = medicamentService;
        this.interactionService = interactionService;
        
        if (medicamentCheckboxContainer != null) {
            initializeMedicaments();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Si les services sont déjà injectés, initialiser les médicaments
        if (ordonnanceService != null && medicamentService != null && interactionService != null) {
            initializeMedicaments();
        }
        setupEventHandlers();
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

                descriptionField.setDisable(!checkBox.isSelected());
                
                checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
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
        
        for (Node node : medicamentCheckboxContainer.getChildren()) {
            if (node instanceof HBox) {
                HBox hbox = (HBox) node;
                
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
                
                int index = i; 
                checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                    descriptionField.setDisable(!newValue);
                    if (!newValue) {
                        descriptionField.clear();
                    }
                });
                
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
        
        // Si aucun médicament ne correspond à la recherche on affiche un message
        if (!foundResults) {
            Label noResultLabel = new Label("Aucun médicament correspondant à \"" + searchText + "\"");
            medicamentCheckboxContainer.getChildren().add(noResultLabel);
        }
        
        logger.debug("Recherche de médicaments pour '" + searchText + "' : " + 
                (foundResults ? medicamentCheckboxContainer.getChildren().size() : 0) + 
                " résultats trouvés");
    }
    
@FXML
private void handleSaveButton() {
    try {
        // Récupérer les valeurs des champs
        String idPatient = idPatientField.getText();
        String idConsultationStr = idConsultationField.getText();
        String idMedecinStr = idMedecinField.getText();

        // Récupérer les médicaments sélectionnés avec leur posologie
        Map<Medicament, String> selectedMedicamentsWithDescription = getSelectedMedicamentsWithDescription();

        if (idPatient.isEmpty() || idConsultationStr.isEmpty() || idMedecinStr.isEmpty() || 
            selectedMedicamentsWithDescription.isEmpty()) {
            logger.debug("Champs obligatoires manquants");
            showAlert(AlertType.WARNING, "Informations manquantes", 
                     "Tous les champs d'ID doivent être remplis et au moins un médicament doit être sélectionné");
            return;
        }

        // Conversion des ID en entiers
        int idPatientInt, idConsultation, idMedecinInt;
        try {
            idPatientInt = Integer.parseInt(idPatient);
            idConsultation = Integer.parseInt(idConsultationStr);
            idMedecinInt = Integer.parseInt(idMedecinStr);
        } catch (NumberFormatException e) {
            logger.debug("Erreur de format numérique: {}", e.getMessage());
            showAlert(AlertType.ERROR, "Format invalide", 
                     "Les identifiants doivent être des nombres entiers valides.");
            return;
        }

        // Création des listes nécessaires
        List<String> medicamentsWithPosology = new ArrayList<>();
        List<String> selectedMedicaments = new ArrayList<>();

        for (Map.Entry<Medicament, String> entry : selectedMedicamentsWithDescription.entrySet()) {
            String medicamentName = entry.getKey().getNomMedicament();
            String posology = entry.getValue().trim();
            selectedMedicaments.add(medicamentName);
            if (posology.isEmpty()) {
                medicamentsWithPosology.add(medicamentName);
            } else {
                medicamentsWithPosology.add(medicamentName + " (" + posology + ")");
            }
        }

        // Vérification des principes actifs dupliqués
        String duplicateError = checkDuplicatePrincipesActifs(selectedMedicaments);
        if (duplicateError != null) {
            logger.debug("Principes actifs dupliqués détectés: {}", duplicateError);
            showAlert(AlertType.WARNING, "Risque d'interaction médicamenteuse", 
                      "Attention: Vous avez sélectionné des médicaments avec le même principe actif!\n\n" + duplicateError);
            return;
        }

        // Vérification des interactions médicamenteuses
        logger.debug("Vérification des interactions pour les médicaments: {}", selectedMedicaments);
        try {
            List<String> interactions = interactionService.checkInteractions(selectedMedicaments);
            if (!interactions.isEmpty()) {
                logger.debug("Interactions détectées: {}", interactions);
                StringBuilder messageBuilder = new StringBuilder();
                messageBuilder.append("Interactions médicamenteuses détectées:\n\n");
                for (String interaction : interactions) {
                    String cleanedMessage = cleanErrorMessage(interaction.trim().replace("\"", "").replace("\\", ""));
                    messageBuilder.append("- ").append(cleanedMessage).append("\n");
                }
                showAlert(AlertType.WARNING, "Interactions médicamenteuses détectées", messageBuilder.toString());
                return;
            }
        } catch (Exception e) {
            String errorMessage = cleanErrorMessage(e.getMessage() != null ? e.getMessage() : "Erreur inconnue");
            logger.debug("Erreur lors de la vérification des interactions: {}", errorMessage);
            if (errorMessage.toLowerCase().contains("deux médicaments") || 
                errorMessage.toLowerCase().contains("au moins deux")) {
                logger.debug("Erreur: Au moins deux médicaments requis - poursuite");
            } else {
                showAlert(AlertType.ERROR, "Erreur de vérification", 
                          "Impossible de vérifier les interactions médicamenteuses: " + errorMessage);
                return;
            }
        }

        // Afficher les données saisies dans displayArea
        displayAreaInfo(idPatientInt, idConsultation, idMedecinInt, medicamentsWithPosology);

        // Création de l'ordonnance
        Ordonnance newOrdonnance = new Ordonnance();
        newOrdonnance.setIdOrdonnance(Ordonnance.generateIdOrdonnance());
        newOrdonnance.setIdPatient(idPatientInt);
        newOrdonnance.setIdConsultation(idConsultation);
        newOrdonnance.setIdMedecin(idMedecinInt);

        // Tentative d'insertion de l'ordonnance
        try {
            logger.debug("Tentative d'insertion de l'ordonnance, ID: {}", newOrdonnance.getIdOrdonnance());
            ordonnanceService.insertOrdonnance(newOrdonnance, medicamentsWithPosology);
            logger.debug("Insertion de l'ordonnance confirmée comme réussie");
            showAlert(AlertType.INFORMATION, "Succès", "Ordonnance enregistrée avec succès");
        } catch (Exception e) {
            String responseMessage = e.getMessage() != null ? e.getMessage() : "Erreur inconnue";
            logger.debug("Erreur reçue: {}", responseMessage);

            try {
                JSONObject json = new JSONObject(responseMessage);
                if (json.has("type") && json.has("message")) {
                    String type = json.getString("type");
                    String message = json.getString("message");

                    switch (type) {
                        case "medecin_inexistant":
                            showAlert(AlertType.ERROR, "Médecin inexistant", message);
                            break;
                        case "patient_inexistant":
                            showAlert(AlertType.ERROR, "Patient inexistant", message);
                            break;
                        case "consultation_inexistante": showAlert(AlertType.ERROR, "Consultation inexistante", message);
                break;
                        default:
                            showAlert(AlertType.ERROR, "Erreur d'insertion", message);
                            break;
                    }
                } else {
                    showAlert(AlertType.ERROR, "Erreur d'insertion", responseMessage);
                }
            } catch (Exception jsonEx) {
                logger.error("Erreur JSON: {}", jsonEx.getMessage());
                showAlert(AlertType.ERROR, "Erreur d'insertion", cleanErrorMessage(responseMessage));
            }
        }
    } catch (Exception e) {
        logger.error("Erreur inattendue: {}", e.getMessage());
        showAlert(AlertType.ERROR, "Erreur", "Une erreur inattendue est survenue : " + e.getMessage());
    }
}

    // Méthode auxiliaire pour afficher les informations dans displayArea
       private void displayAreaInfo(int idPatient, int idConsultation, int idMedecin, List<String> medicaments) {
        clearDisplayArea();
        displayArea.appendText("Données à enregistrer :\n");
        displayArea.appendText("ID Patient: " + idPatient + "\n");
        displayArea.appendText("ID Consultation: " + idConsultation + "\n");
        displayArea.appendText("ID Médecin: " + idMedecin + "\n");
        displayArea.appendText("Médicaments: \n");
        
        for (String med : medicaments) {
            displayArea.appendText("- " + med + "\n");
        }
        displayArea.appendText("-----------------------------\n");
    }


    private String cleanErrorMessage(String errorMessage) {
        if (errorMessage == null) return "Erreur inconnue";
        
        // Nettoyer les caractères problématiques
        return errorMessage.replaceAll("[\\p{Cc}\\p{Cf}\\p{Co}\\p{Cn}]", "") // Supprime caractères de contrôle
                          .replace("Ôÿà", "") // Supprime les caractères spécifiques qui posent problème
                          .replace("ÔÇö", "--") // Remplace par des tirets standards
                          .replace("Ú", "é") // Corrige les caractères accentués
                          .replace("Þ", "è") // Corrige les caractères accentués
                          .replace("terminÚ", "terminé") // Corrige des mots spécifiques
                          .replace("MÚdicaments", "Médicaments") // Corrige des mots spécifiques
                          .trim(); // Supprime les espaces inutiles
    }

    @FXML
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

                        
                        resetFields();
                    } catch (Exception ex) {
                        logger.error("Erreur lors de la suppression", ex);
                    }
                }
            });
        } catch (Exception ex) {
            logger.error("Erreur lors de la suppression de l'ordonnance", ex);
        }
    }
    
    @FXML
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
    
    @FXML
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
                // Réinitialiser le mode de modification
                enModeModification = false;
                
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
        displayArea.appendText("Médicaments:\n");
        for (String med : medicamentsOrdonnance) {
            displayArea.appendText("- " + med + "\n");
        }
        System.out.println("Médicaments extraits : " + medicamentsOrdonnance);

        
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

// Dans un controller, nous utilisons généralement @FXML pour les gestionnaires d'événements
@FXML
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

// Cette méthode reste inchangée, elle est utile dans un controller
private void showAlert(AlertType alertType, String title, String message) {
    // Utiliser Platform.runLater pour s'assurer que l'alerte s'exécute sur le thread JavaFX
    Platform.runLater(() -> {
        try {
            // Créer l'alerte
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            
            // Journaliser l'affichage de l'alerte pour le débogage
            logger.debug("Tentative d'affichage d'une alerte : Type=" + alertType + ", Titre=" + title);
            
            // S'assurer que l'alerte est au premier plan
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            
            // Ajouter un handler pour détecter si l'alerte a été fermée
            alert.setOnHidden(e -> {
                logger.debug("Alerte fermée : " + title);
            });
            
            // Afficher l'alerte et attendre
            alert.showAndWait();
        } catch (Exception e) {
            // En cas d'erreur lors de l'affichage de l'alerte, journaliser l'erreur
            logger.error("Erreur lors de l'affichage de l'alerte : " + e.getMessage(), e);
            
            // Fallback : afficher un message dans la console
            System.err.println("ALERTE (" + alertType + ") : " + title + " - " + message);
        }
    });
}
}