package edu.ezip.ing1.pds;

import edu.ezip.ing1.pds.business.dto.Medecin;
import edu.ezip.ing1.pds.business.dto.Medecins;
import edu.ezip.ing1.pds.services.MedecinService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MedecinUIController {
    private final static Logger logger = LoggerFactory.getLogger("MedecinUIController");
    private MedecinService medecinService;

    @FXML
    private TableView<Medecin> medecinTable;
    @FXML
    private TableColumn<Medecin, Integer> idColumn;
    @FXML
    private TableColumn<Medecin, String> nomColumn;
    @FXML
    private TableColumn<Medecin, String> prenomColumn;
    @FXML
    private TableColumn<Medecin, String> specialiteColumn;
    @FXML
    private TableColumn<Medecin, String> telColumn;

    @FXML
    private TextField idField;
    @FXML
    private TextField nomField;
    @FXML
    private TextField prenomField;
    @FXML
    private TextField specialiteField;
    @FXML
    private TextField telField;

    @FXML
    private Button ajouterButton;
    @FXML
    private Button mettreAJourButton;
    @FXML
    private Button supprimerButton;
    @FXML
    private Button actualiserButton;

    private ObservableList<Medecin> medecinData = FXCollections.observableArrayList();

    public void initialize() {
        // Configuration des colonnes de la table
        idColumn.setCellValueFactory(new PropertyValueFactory<>("idMedecin"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nomMedecin"));
        prenomColumn.setCellValueFactory(new PropertyValueFactory<>("prenomMedecin"));
        specialiteColumn.setCellValueFactory(new PropertyValueFactory<>("specialite"));
        telColumn.setCellValueFactory(new PropertyValueFactory<>("numTel"));

        // Liaison des données avec la table
        medecinTable.setItems(medecinData);

        // Gestion de la sélection dans la table
        medecinTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showMedecinDetails(newValue));

        // Initialisation des boutons
        setupButtons();
    }

    public void setMedecinService(MedecinService medecinService) {
        this.medecinService = medecinService;
        refreshTable();
    }

    private void showMedecinDetails(Medecin medecin) {
        if (medecin != null) {
            idField.setText(String.valueOf(medecin.getIdMedecin()));
            nomField.setText(medecin.getNomMedecin());
            prenomField.setText(medecin.getPrenomMedecin());
            specialiteField.setText(medecin.getSpecialite());
            telField.setText(medecin.getNumTel());
        }
    }

    private void clearForm() {
        idField.clear();
        nomField.clear();
        prenomField.clear();
        specialiteField.clear();
        telField.clear();
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
            Medecin medecin = new Medecin(
                    id,
                    nomField.getText().trim(),
                    prenomField.getText().trim(),
                    specialiteField.getText().trim(),
                    telField.getText().trim());

            medecinService.InsertMedecin(medecin);
            refreshTable();
            clearForm();
        } catch (Exception ex) {
            showAlert("Erreur lors de l'ajout", ex.getMessage());
        }
    }

    private void handleMettreAJour() {
        Medecin selectedMedecin = medecinTable.getSelectionModel().getSelectedItem();
        if (selectedMedecin != null) {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                Medecin medecin = new Medecin(
                        id,
                        nomField.getText().trim(),
                        prenomField.getText().trim(),
                        specialiteField.getText().trim(),
                        telField.getText().trim());

                medecinService.UpdateMedecin(medecin);
                refreshTable();
                clearForm();
            } catch (Exception ex) {
                showAlert("Erreur lors de la mise à jour", ex.getMessage());
            }
        } else {
            showAlert("Erreur", "Veuillez sélectionner un médecin à mettre à jour");
        }
    }

    private void handleSupprimer() {
        Medecin selectedMedecin = medecinTable.getSelectionModel().getSelectedItem();
        if (selectedMedecin != null) {
            try {
                medecinService.DeleteMedecin(selectedMedecin);
                refreshTable();
                clearForm();
            } catch (Exception ex) {
                showAlert("Erreur lors de la suppression", ex.getMessage());
            }
        } else {
            showAlert("Erreur", "Veuillez sélectionner un médecin à supprimer");
        }
    }

    private void refreshTable() {
        try {
            Medecins medecins = medecinService.selectMedecins();
            medecinData.clear();
            if (medecins != null && medecins.getMedecins() != null) {
                medecinData.addAll(medecins.getMedecins());
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