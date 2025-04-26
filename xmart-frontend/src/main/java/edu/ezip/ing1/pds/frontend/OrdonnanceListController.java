package edu.ezip.ing1.pds.frontend;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ezip.ing1.pds.business.dto.Ordonnance;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class OrdonnanceListController implements Initializable {
    
    private final static String LoggingLabel = "FrontEnd - Ordonnance List";
    private final static Logger logger = LoggerFactory.getLogger(LoggingLabel);
    
    @FXML
    private TableView<Ordonnance> ordonnanceTableView;
    
    @FXML
    private TableColumn<Ordonnance, Integer> idColumn;
    
    @FXML
    private TableColumn<Ordonnance, Integer> patientColumn;
    
    @FXML
    private TableColumn<Ordonnance, Integer> consultationColumn;
    
    @FXML
    private TableColumn<Ordonnance, Integer> medecinColumn;

    @FXML
    private TableColumn<Ordonnance, String> descriptionColumn;
    
    @FXML
    private Button selectButton;
    
    @FXML
    private Button closeButton;
    
    private OrdonnanceFront mainController;
    private List<Ordonnance> ordonnancesList;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configurer les colonnes
        idColumn.setCellValueFactory(new PropertyValueFactory<>("idOrdonnance"));
        patientColumn.setCellValueFactory(new PropertyValueFactory<>("idPatient"));
        consultationColumn.setCellValueFactory(new PropertyValueFactory<>("idConsultation"));
        medecinColumn.setCellValueFactory(new PropertyValueFactory<>("idMedecin"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        
        // Configurer les boutons
        selectButton.setOnAction(event -> handleSelectButton());
        closeButton.setOnAction(event -> handleCloseButton());
    }
    
    public void setMainController(OrdonnanceFront controller) {
        this.mainController = controller;
    }
    
    public void loadOrdonnances(List<Ordonnance> ordonnances) {
        this.ordonnancesList = ordonnances;
        ObservableList<Ordonnance> data = FXCollections.observableArrayList(ordonnances);
        ordonnanceTableView.setItems(data);
    }
    
    private void handleSelectButton() {
        Ordonnance selectedOrdonnance = ordonnanceTableView.getSelectionModel().getSelectedItem();
        if (selectedOrdonnance != null) {
            logger.debug("Ordonnance sélectionnée: ID = " + selectedOrdonnance.getIdOrdonnance());
            mainController.loadOrdonnanceForEditing(selectedOrdonnance);
            closeWindow();
        }
    }
    
    private void handleCloseButton() {
        closeWindow();
    }
    
    private void closeWindow() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}