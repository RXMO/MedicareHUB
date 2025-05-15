package edu.ezip.ing1.pds.frontend;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import edu.ezip.ing1.pds.business.dto.DiagnosticResult;
import edu.ezip.ing1.pds.business.dto.Symptomes;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.services.ServiceSymptome;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class DiagnosticController implements Initializable {

    // Champs JavaFX liés au FXML pour l'interface
    @FXML private TextField champPatientId;
    @FXML private TextField champSymptome;
    @FXML private TextField champModification;
    @FXML private TextField champDateRendezVous;
    @FXML private ListView<String> listeSymptomes;
    @FXML private TextArea resultatDiagnostic;
    @FXML private VBox creneauxPanel;
    @FXML private Button boutonAjouter;
    @FXML private Button boutonAfficher;
    @FXML private Button boutonModifier;
    @FXML private Button boutonSupprimer;
    @FXML private Button boutonDiagnostiquer;
    @FXML private Button boutonChargerPatient;
    @FXML private Button boutonPrendreRendezVous;

    private ServiceSymptome serviceSymptome; 
    private List<Symptomes> symptomesAjoutes = new ArrayList<>(); // Liste des symptômes du patient
    private List<DiagnosticResult> derniersResultats; // Résultats du dernier diagnostic
    private int idPatientActuel; 
    private Map<Integer, ComboBox<String>> creneauxComboBoxes = new HashMap<>(); // ComboBox pour les créneaux
    private Map<Integer, Map<String, Map<String, Object>>> creneauxMap = new HashMap<>(); // Détails des créneaux

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        NetworkConfig networkConfig = new NetworkConfig();
        networkConfig.setTcpport(45065);
        networkConfig.setIpaddress("172.31.252.216");
        serviceSymptome = new ServiceSymptome(networkConfig);

        champPatientId.setText(""); 
    }

    public void chargerPatient() {
        // Charge un patient à partir de l'ID saisi
        try {
            String idText = champPatientId.getText().trim();
            if (idText.isEmpty()) {
                showAlert("Erreur", "Veuillez saisir un ID patient valide");
                return;
            }
            int newPatientId = Integer.parseInt(idText);
            if (newPatientId <= 0) {
                showAlert("Erreur", "L'ID patient doit être un entier positif");
                return;
            }
            idPatientActuel = newPatientId;
            chargerSymptomesPatient();
            showAlert("Succès", "Patient " + idPatientActuel + " chargé avec succès");
        } catch (NumberFormatException e) {
            showAlert("Erreur", "L'ID patient doit être un nombre entier");
        } catch (Exception ex) {
            showAlert("Erreur", "Erreur lors du chargement: " + ex.getMessage());
        }
    }

    public void chargerSymptomesPatient() {
        // Charge les symptômes du patient actuel depuis le serveur
        try {
            symptomesAjoutes.clear();
            List<Symptomes> symptomesPatient = serviceSymptome.getSymptomesPatient(idPatientActuel);
            symptomesAjoutes.addAll(symptomesPatient);
            afficherSymptomes();
        } catch (Exception ex) {
            showAlert("Erreur", "Erreur lors du chargement des symptômes: " + ex.getMessage());
        }
    }

    public void ajouterSymptome() {
        // Ajoute un symptôme au patient via le champ texte
        String symptomText = champSymptome.getText().trim();
        if (symptomText.isEmpty()) {
            showAlert("Erreur", "Le champ est vide !");
            return;
        }
        try {
            Symptomes symptom = new Symptomes(0, symptomText);
            Symptomes symptomWithId = serviceSymptome.associerSymptomePatient(idPatientActuel, symptom);
            champSymptome.setText("");
            if (symptomWithId == null) {
                showAlert("Erreur", "Erreur : Impossible d'associer le symptôme.");
                return;
            }
            boolean exists = symptomesAjoutes.stream().anyMatch(s -> s.getId() == symptomWithId.getId());
            if (!exists) symptomesAjoutes.add(symptomWithId);
            showAlert("Succès", "Symptôme associé au patient avec succès ! ID: " + symptomWithId.getId());
        } catch (Exception ex) {
            showAlert("Erreur", "Erreur lors de l'ajout du symptôme: " + ex.getMessage());
        }
    }

    public void afficherSymptomes() {
        // Met à jour la liste des symptômes affichés
        listeSymptomes.getItems().clear();
        for (Symptomes s : symptomesAjoutes) {
            listeSymptomes.getItems().add(s.getDescription());
        }
    }

    public void modifierSymptome() {
        // Modifie un symptôme sélectionné dans la liste
        String selectedSymptom = listeSymptomes.getSelectionModel().getSelectedItem();
        String newName = champModification.getText().trim();
        if (selectedSymptom == null || newName.isEmpty()) {
            showAlert("Erreur", "Le champ est vide ou aucun symptôme n'est sélectionné !");
            return;
        }
        try {
            int symptomId = 0, index = -1;
            for (int i = 0; i < symptomesAjoutes.size(); i++) {
                if (symptomesAjoutes.get(i).getDescription().equals(selectedSymptom)) {
                    symptomId = symptomesAjoutes.get(i).getId();
                    index = i;
                    break;
                }
            }
            if (symptomId == 0) {
                showAlert("Erreur", "Impossible de trouver l'ID du symptôme sélectionné.");
                return;
            }
            List<Symptomes> allSymptoms = serviceSymptome.selectSymptomes();
            boolean symptomExists = allSymptoms.stream().anyMatch(s -> s.getDescription().equals(newName));
            if (!symptomExists) {
                showAlert("Avertissement", "Le symptôme '" + newName + "' n'existe pas. Ajoutez-le d'abord.");
                return;
            }
            Symptomes newSymptom = serviceSymptome.modifierSymptomePatient(idPatientActuel, symptomId, newName);
            champModification.setText("");
            if (index >= 0 && newSymptom != null) {
                symptomesAjoutes.remove(index);
                symptomesAjoutes.add(newSymptom);
            } else {
                chargerSymptomesPatient();
            }
            afficherSymptomes();
            showAlert("Succès", "Symptôme modifié avec succès !");
        } catch (Exception ex) {
            showAlert("Erreur", "Erreur: " + ex.getMessage());
        }
    }

    public void supprimerSymptome() {
        // Supprime un symptôme sélectionné après confirmation
        String selectedSymptom = listeSymptomes.getSelectionModel().getSelectedItem();
        if (selectedSymptom == null) {
            showAlert("Avertissement", "Veuillez sélectionner un symptôme à supprimer.");
            return;
        }
        try {
            int symptomId = symptomesAjoutes.stream()
                    .filter(s -> s.getDescription().equals(selectedSymptom))
                    .findFirst()
                    .map(Symptomes::getId)
                    .orElse(0);
            if (symptomId == 0) {
                showAlert("Erreur", "Impossible de trouver l'ID du symptôme.");
                return;
            }
            if (showConfirmation("Supprimer l'association avec \"" + selectedSymptom + "\" ?")) {
                String message = serviceSymptome.supprimerSymptomePatient(idPatientActuel, symptomId);
                symptomesAjoutes.removeIf(s -> s.getId() == symptomId);
                afficherSymptomes();
                showAlert("Succès", message);
            }
        } catch (Exception ex) {
            showAlert("Erreur", "Erreur lors de la suppression: " + ex.getMessage());
        }
    }

    public void diagnostiquer() {
        // Lance un diagnostic et affiche jusqu'à 5 maladies possibles(cest mon choix)
        long debut = System.currentTimeMillis();
        try {
            derniersResultats = serviceSymptome.diagnostiquer(idPatientActuel);
            StringBuilder sb = new StringBuilder("Maladies possibles:\n");
            creneauxComboBoxes.clear();
            creneauxMap.clear();
            creneauxPanel.getChildren().clear();

            if (derniersResultats.isEmpty()) {
                sb.append("Aucune maladie trouvée pour ces symptômes.\n");
            } else {
                int maxDisplay = Math.min(5, derniersResultats.size());
                for (int i = 0; i < maxDisplay; i++) {
                    DiagnosticResult result = derniersResultats.get(i);
                    sb.append("- ").append(result.toString()).append("\n");
                    List<Map<String, Object>> creneaux = result.getCreneauxDisponibles();
                    if (creneaux != null && !creneaux.isEmpty()) {
                        Map<String, Map<String, Object>> creneauDetails = new HashMap<>();
                        List<String> creneauOptions = new ArrayList<>();
                        for (Map<String, Object> creneau : creneaux) {
                            String creneauStr = String.format("%s de %s à %s (Médecin: %d)",
                                    creneau.get("jour"), creneau.get("heure_debut"),
                                    creneau.get("heure_fin"), creneau.get("id_medecin"));
                            creneauOptions.add(creneauStr);
                            creneauDetails.put(creneauStr, creneau);
                        }
                        creneauxMap.put(i, creneauDetails);
                        ComboBox<String> creneauComboBox = new ComboBox<>();
                        creneauComboBox.getItems().addAll(creneauOptions);
                        creneauComboBox.getSelectionModel().selectFirst();
                        creneauxComboBoxes.put(i, creneauComboBox);
                        HBox creneauEntry = new HBox(10);
                        creneauEntry.getChildren().addAll(
                                new Label("Créneaux pour diagnostic " + (i + 1) + ":"),
                                creneauComboBox
                        );
                        creneauxPanel.getChildren().add(creneauEntry);
                    } else {
                        sb.append("  Aucun créneau disponible pour cette spécialité.\n");
                    }
                }
            }
            resultatDiagnostic.setText(sb.toString());
        } catch (Exception ex) {
            resultatDiagnostic.setText("Erreur lors du diagnostic: " + ex.getMessage());
            showAlert("Erreur de diagnostic", "Erreur: " + ex.getMessage());
        }
        long fin = System.currentTimeMillis();
        System.out.println("Temps diagnostic : " + (fin - debut) + " ms");
    }

    public void prendreRendezVous() {
        // Prend un rendez-vous après avoir choisi un diagnostic et un créneau
        try {
            String appointmentDate = champDateRendezVous.getText().trim();
            if (appointmentDate.isEmpty()) {
                showAlert("Erreur", "Veuillez saisir une date pour le rendez-vous (format AAAA-MM-JJ).");
                return;
            }
            if (derniersResultats == null || derniersResultats.isEmpty()) {
                showAlert("Erreur", "Effectuez un diagnostic avant de prendre un rendez-vous.");
                return;
            }
            String[] diagnosticOptions = new String[Math.min(5, derniersResultats.size())];
            for (int i = 0; i < diagnosticOptions.length; i++) {
                diagnosticOptions[i] = "Diagnostic " + (i + 1) + ": " + derniersResultats.get(i).toString();
            }
            String selectedDiagnosticStr = showChoiceDialog("Sélectionnez un diagnostic pour prendre rendez-vous:",
                    "Choix du diagnostic", diagnosticOptions, diagnosticOptions[0]);
            if (selectedDiagnosticStr == null) return;
            int selectedIndex = Integer.parseInt(selectedDiagnosticStr.split(":")[0].replace("Diagnostic ", "").trim()) - 1;
            DiagnosticResult selectedDiagnostic = derniersResultats.get(selectedIndex);
            int specialtyId = selectedDiagnostic.getIdSpecialite() == 0 ? 1 : selectedDiagnostic.getIdSpecialite();
            if (!creneauxComboBoxes.containsKey(selectedIndex)) {
                showAlert("Erreur", "Aucun créneau disponible pour ce diagnostic.");
                return;
            }
            ComboBox<String> selectedComboBox = creneauxComboBoxes.get(selectedIndex);
            String selectedCreneauStr = selectedComboBox.getValue();
            Map<String, Map<String, Object>> creneauDetails = creneauxMap.get(selectedIndex);
            Map<String, Object> selectedCreneau = creneauDetails.get(selectedCreneauStr);
            if (selectedCreneau == null) {
                showAlert("Erreur", "Erreur lors de la récupération du créneau sélectionné.");
                return;
            }
            int idDisponibilite = (int) selectedCreneau.get("id_disponibilite");
            int idMedecin = (int) selectedCreneau.get("id_medecin");
            String message = serviceSymptome.creerRendezVous(idPatientActuel, appointmentDate, specialtyId, idDisponibilite, idMedecin);
            if (message.toLowerCase().contains("erreur") || message.toLowerCase().contains("non disponible") || message.toLowerCase().contains("invalide")) {
                showAlert("Erreur lors de la prise de rendez-vous", message);
            } else {
                showAlert("Succès", message);
                diagnostiquer(); // Rafraîchit les créneaux après la prise de rendez-vous
            }
        } catch (Exception ex) {
            showAlert("Erreur", "Erreur lors de la prise de rendez-vous: " + ex.getMessage());
        }
    }

    public void showAlert(String title, String message) {
        // Affiche une alerte pour informer l'utilisateur
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public boolean showConfirmation(String message) {
        // Affiche une boîte de confirmation (Oui/Non)
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    public String showChoiceDialog(String message, String title, String[] options, String defaultOption) {
        // Affiche une boîte de dialogue pour choisir une option
        ChoiceDialog<String> dialog = new ChoiceDialog<>(defaultOption, options);
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(message);
        return dialog.showAndWait().orElse(null);
    }
}