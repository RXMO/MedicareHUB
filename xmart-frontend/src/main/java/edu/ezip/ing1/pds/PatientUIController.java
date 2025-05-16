package edu.ezip.ing1.pds;

import edu.ezip.ing1.pds.business.dto.Patient;
import edu.ezip.ing1.pds.business.dto.Patients;
import edu.ezip.ing1.pds.services.PatientService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PatientUIController {
    private final static Logger logger = LoggerFactory.getLogger(PatientUIController.class);
    private PatientService patientService;

    @FXML
    private TableView<Patient> patientTable;
    @FXML
    private TableColumn<Patient, Integer> idColumn;
    @FXML
    private TableColumn<Patient, String> nomColumn;
    @FXML
    private TableColumn<Patient, String> prenomColumn;
    @FXML
    private TableColumn<Patient, String> telColumn;
    @FXML
    private TableColumn<Patient, String> allergiesColumn;

    @FXML
    private TextField idField;
    @FXML
    private TextField nomField;
    @FXML
    private TextField prenomField;
    @FXML
    private TextField telField;
    @FXML
    private TextField allergiesField;

    @FXML
    private Button ajouterButton;
    @FXML
    private Button mettreAJourButton;
    @FXML
    private Button supprimerButton;
    @FXML
    private Button actualiserButton;

    private ObservableList<Patient> patientData = FXCollections.observableArrayList();

    public void initialize() {
        // Configuration des colonnes de la table
        idColumn.setCellValueFactory(new PropertyValueFactory<>("idPatient"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nomPatient"));
        prenomColumn.setCellValueFactory(new PropertyValueFactory<>("prenomPatient"));
        telColumn.setCellValueFactory(new PropertyValueFactory<>("numTel"));
        allergiesColumn.setCellValueFactory(new PropertyValueFactory<>("allergies"));

        // Liaison des données avec la table
        patientTable.setItems(patientData);

        // Gestion de la sélection dans la table
        patientTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showPatientDetails(newValue));

        // Initialisation des boutons
        setupButtons();
    }

    public void setPatientService(PatientService patientService) {
        this.patientService = patientService;
        refreshTable();
    }

    private void showPatientDetails(Patient patient) {
        if (patient != null) {
            idField.setText(String.valueOf(patient.getIdPatient()));
            nomField.setText(patient.getNomPatient());
            prenomField.setText(patient.getPrenomPatient());
            telField.setText(patient.getNumTel());
            allergiesField.setText(patient.getAllergies());
        }
    }

    private void clearForm() {
        idField.clear();
        nomField.clear();
        prenomField.clear();
        telField.clear();
        allergiesField.clear();
    }

    private void setupButtons() {
        ajouterButton.setOnAction(e -> handleAjouter());
        mettreAJourButton.setOnAction(e -> handleMettreAJour());
        supprimerButton.setOnAction(e -> handleSupprimer());
        actualiserButton.setOnAction(e -> refreshTable());
    }

    private void handleAjouter() {
        // Validation ID
        try {
            Integer.parseInt(idField.getText().trim());
        } catch (NumberFormatException e) {
            showAlert("Erreur ID", "L'ID doit être un nombre valide");
            return;
        }

        // Validation Nom
        String nom = nomField.getText().trim();
        if (nom.isEmpty()) {
            showAlert("Erreur Nom", "Le nom ne peut pas être vide");
            return;
        }
        if (!Character.isUpperCase(nom.charAt(0))) {
            showAlert("Erreur Nom", "Le nom doit commencer par une majuscule");
            return;
        }

        // Validation Prénom
        String prenom = prenomField.getText().trim();
        if (prenom.isEmpty()) {
            showAlert("Erreur Prénom", "Le prénom ne peut pas être vide");
            return;
        }
        if (!Character.isUpperCase(prenom.charAt(0))) {
            showAlert("Erreur Prénom", "Le prénom doit commencer par une majuscule");
            return;
        }

        // Validation Téléphone
        String tel = telField.getText().trim();
        if (!tel.matches("^\\+33[1-9]\\d{8}$")) {
            showAlert("Erreur Téléphone", 
                "Le numéro doit:\n" +
                "- Commencer par +33\n" +
                "- Contenir exactement 9 chiffres\n" +
                "- Le premier chiffre après +33 ne peut pas être 0\n" +
                "Exemple: +33123456789");
            return;
        }

        // Si toutes les validations sont OK
        try {
            int id = Integer.parseInt(idField.getText().trim());
            Patient patient = new Patient(
                    id,
                    nom,
                    prenom,
                    tel,
                    allergiesField.getText().trim());

            patientService.InsertPatient(patient);
            refreshTable();
            clearForm();
            showSuccessAlert("Patient ajouté avec succès");
        } catch (Exception ex) {
            showAlert("Erreur lors de l'ajout", ex.getMessage());
        }
    }

    private void handleMettreAJour() {
        Patient selectedPatient = patientTable.getSelectionModel().getSelectedItem();
        if (selectedPatient == null) {
            showAlert("Erreur", "Veuillez sélectionner un patient à mettre à jour");
            return;
        }

        // Mêmes validations que pour l'ajout
        try {
            Integer.parseInt(idField.getText().trim());
        } catch (NumberFormatException e) {
            showAlert("Erreur ID", "L'ID doit être un nombre valide");
            return;
        }

        String nom = nomField.getText().trim();
        if (nom.isEmpty() || !Character.isUpperCase(nom.charAt(0))) {
            showAlert("Erreur Nom", "Le nom doit commencer par une majuscule");
            return;
        }

        String prenom = prenomField.getText().trim();
        if (prenom.isEmpty() || !Character.isUpperCase(prenom.charAt(0))) {
            showAlert("Erreur Prénom", "Le prénom doit commencer par une majuscule");
            return;
        }

        String tel = telField.getText().trim();
        if (!tel.matches("^\\+33[1-9]\\d{8}$")) {
            showAlert("Erreur Téléphone", "Format de téléphone invalide");
            return;
        }

        try {
            Patient patient = new Patient(
                    selectedPatient.getIdPatient(),
                    nom,
                    prenom,
                    tel,
                    allergiesField.getText().trim());

            patientService.UpdatePatient(patient);
            refreshTable();
            clearForm();
            showSuccessAlert("Patient mis à jour avec succès");
        } catch (Exception ex) {
            showAlert("Erreur lors de la mise à jour", ex.getMessage());
        }
    }

    private void handleSupprimer() {
        Patient selectedPatient = patientTable.getSelectionModel().getSelectedItem();
        if (selectedPatient == null) {
            showAlert("Erreur", "Veuillez sélectionner un patient à supprimer");
            return;
        }

        // Confirmation avant suppression
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation de suppression");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer le patient " + 
                                 selectedPatient.getNomPatient() + " " + 
                                 selectedPatient.getPrenomPatient() + "?");

        if (confirmAlert.showAndWait().get() == ButtonType.OK) {
            try {
                patientService.DeletePatient(selectedPatient);
                refreshTable();
                clearForm();
                showSuccessAlert("Patient supprimé avec succès");
            } catch (Exception ex) {
                showAlert("Erreur lors de la suppression", ex.getMessage());
            }
        }
    }

    private void refreshTable() {
        try {
            Patients patients = patientService.selectPatients();
            patientData.clear();
            if (patients != null && patients.getPatients() != null) {
                patientData.addAll(patients.getPatients());
            }
        } catch (Exception ex) {
            showAlert("Erreur lors de l'actualisation", ex.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}