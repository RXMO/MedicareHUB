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
    private final static Logger logger = LoggerFactory.getLogger("PatientUIController");
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
        try {
            int id = Integer.parseInt(idField.getText().trim());
            Patient patient = new Patient(
                    id,
                    nomField.getText().trim(),
                    prenomField.getText().trim(),
                    telField.getText().trim(),
                    allergiesField.getText().trim());

            patientService.InsertPatient(patient);
            refreshTable();
            clearForm();
        } catch (Exception ex) {
            showAlert("Erreur lors de l'ajout", ex.getMessage());
        }
    }

    private void handleMettreAJour() {
        Patient selectedPatient = patientTable.getSelectionModel().getSelectedItem();
        if (selectedPatient != null) {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                Patient patient = new Patient(
                        id,
                        nomField.getText().trim(),
                        prenomField.getText().trim(),
                        telField.getText().trim(),
                        allergiesField.getText().trim());

                patientService.UpdatePatient(patient);
                refreshTable();
                clearForm();
            } catch (Exception ex) {
                showAlert("Erreur lors de la mise à jour", ex.getMessage());
            }
        } else {
            showAlert("Erreur", "Veuillez sélectionner un patient à mettre à jour");
        }
    }

    private void handleSupprimer() {
        Patient selectedPatient = patientTable.getSelectionModel().getSelectedItem();
        if (selectedPatient != null) {
            try {
                patientService.DeletePatient(selectedPatient);
                refreshTable();
                clearForm();
            } catch (Exception ex) {
                showAlert("Erreur lors de la suppression", ex.getMessage());
            }
        } else {
            showAlert("Erreur", "Veuillez sélectionner un patient à supprimer");
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
}