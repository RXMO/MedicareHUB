package edu.ezip.ing1.pds.frontend;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

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

public class OrdonnanceFrontEnd extends JFrame {

    private final static String LoggingLabel = "FrontEnd - Ordonnance";
    private final static Logger logger = LoggerFactory.getLogger(LoggingLabel);
    private final static String networkConfigFile = "network.yaml";

    private OrdonnanceService ordonnanceService;

    private JTextField idPatientField, idConsultationField, descriptionField, idMedecinField, searchMedicamentField;
    private JPanel medicamentCheckboxPanel;
    private List<JCheckBox> medicamentCheckboxes;
    private JTextArea displayArea;
    private DefaultTableModel ordonnanceTableModel;
    private JTable ordonnanceTable;
    private List<Medicament> medicamentsList;
    private boolean enModeModification = false;
    private int currentOrdonnanceId = -1;
    private JButton saveButton, afficherButton, supprimerButton, modifierButton, confirmerButton;

    public OrdonnanceFrontEnd(OrdonnanceService ordonnanceService, MedicamentService medicamentService) throws InterruptedException, IOException {
        if (ordonnanceService == null || medicamentService == null) {
            throw new IllegalArgumentException("Les services ne peuvent pas être null !");
        }
        this.ordonnanceService = ordonnanceService;
    
        // Configuration de la fenêtre principale
        this.setTitle("Gestion des Ordonnances");
        this.setLayout(new BorderLayout());
        this.setSize(800, 700);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
        // Initialisation des médicaments
        Medicaments medicaments = medicamentService.selectMedicaments();
        medicamentsList = new ArrayList<>(medicaments.getMedicaments());
        medicamentCheckboxes = new ArrayList<>();
    
        setupUIComponents();
        setupEventHandlers();
        actualiserOrdonnances();
    
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private void setupUIComponents() {
        // Panel de saisie des données (partie supérieure)
        JPanel topPanel = new JPanel(new BorderLayout());
        
        // Sous-panel pour les champs de texte
        JPanel inputPanel = setupInputPanel();
        topPanel.add(inputPanel, BorderLayout.NORTH);
        
        // Panel de recherche de médicaments
        JPanel medicamentPanel = setupMedicamentPanel();
        topPanel.add(medicamentPanel, BorderLayout.CENTER);
        this.add(topPanel, BorderLayout.NORTH);
        
        // Table des ordonnances
        setupOrdonnanceTable();
        
        // Panel central
        JPanel centerPanel = setupCenterPanel();
        this.add(centerPanel, BorderLayout.CENTER);
        
        // Panel des boutons
        JPanel buttonPanel = setupButtonPanel();
        this.add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel setupInputPanel() {
        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 10, 5));
        idPatientField = new JTextField();
        idConsultationField = new JTextField();
        descriptionField = new JTextField();
        idMedecinField = new JTextField();
        
        inputPanel.add(new JLabel("ID Patient:"));
        inputPanel.add(idPatientField);
        inputPanel.add(new JLabel("ID Consultation:"));
        inputPanel.add(idConsultationField);
        inputPanel.add(new JLabel("ID Médecin:"));
        inputPanel.add(idMedecinField);
        inputPanel.add(new JLabel("Description:"));
        inputPanel.add(descriptionField);
        
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return inputPanel;
    }

    private JPanel setupMedicamentPanel() {
        // Panel de recherche
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Rechercher un médicament:"));
        searchMedicamentField = new JTextField(20);
        searchPanel.add(searchMedicamentField);
        
        // Panel des checkboxes
        medicamentCheckboxPanel = new JPanel();
        medicamentCheckboxPanel.setLayout(new BoxLayout(medicamentCheckboxPanel, BoxLayout.Y_AXIS));
        
        if (medicamentsList != null && !medicamentsList.isEmpty()) {
            for (Medicament medicament : medicamentsList) {
                JCheckBox checkBox = new JCheckBox(medicament.getNomMedicament());
                medicamentCheckboxes.add(checkBox);
                medicamentCheckboxPanel.add(checkBox);
            }
        } else {
            medicamentCheckboxPanel.add(new JLabel("Aucun médicament disponible"));
        }
        
        JScrollPane medicamentScrollPane = new JScrollPane(medicamentCheckboxPanel);
        medicamentScrollPane.setPreferredSize(new Dimension(200, 120));
        
        JPanel medicamentPanel = new JPanel(new BorderLayout());
        medicamentPanel.add(searchPanel, BorderLayout.NORTH);
        medicamentPanel.add(medicamentScrollPane, BorderLayout.CENTER);
        medicamentPanel.setBorder(new TitledBorder("Sélectionner les médicaments"));
        
        return medicamentPanel;
    }

    private void setupOrdonnanceTable() {
        ordonnanceTableModel = new DefaultTableModel();
        ordonnanceTableModel.addColumn("ID Ordonnance");
        ordonnanceTableModel.addColumn("ID Patient");
        ordonnanceTableModel.addColumn("ID Consultation");
        ordonnanceTableModel.addColumn("ID Médecin");
        ordonnanceTableModel.addColumn("Description");
        
        ordonnanceTable = new JTable(ordonnanceTableModel);
        ordonnanceTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    }

    private JPanel setupCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        
        // Zone d'affichage
        displayArea = new JTextArea(5, 40);
        displayArea.setEditable(false);
        JScrollPane displayScrollPane = new JScrollPane(displayArea);
        
        JPanel displayPanel = new JPanel(new BorderLayout());
        displayPanel.setBorder(BorderFactory.createTitledBorder("Informations saisies"));
        displayPanel.add(displayScrollPane, BorderLayout.CENTER);
        centerPanel.add(displayPanel, BorderLayout.NORTH);
        
        // Table des ordonnances
        JScrollPane tableScrollPane = new JScrollPane(ordonnanceTable);
        tableScrollPane.setPreferredSize(new Dimension(750, 200));
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Liste des Ordonnances"));
        centerPanel.add(tableScrollPane, BorderLayout.CENTER);
        
        return centerPanel;
    }

    private JPanel setupButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        
        saveButton = new JButton("Enregistrer");
        afficherButton = new JButton("Afficher toutes les ordonnances");
        supprimerButton = new JButton("Supprimer");
        modifierButton = new JButton("Modifier");
        confirmerButton = new JButton("Confirmer Modification");
        confirmerButton.setVisible(false);
        
        buttonPanel.add(saveButton);
        buttonPanel.add(afficherButton);
        buttonPanel.add(supprimerButton);
        buttonPanel.add(modifierButton);
        buttonPanel.add(confirmerButton);
        
        return buttonPanel;
    }

    private void setupEventHandlers() {
        // Écouteur pour la recherche de médicament
        searchMedicamentField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterMedicaments(searchMedicamentField.getText().toLowerCase());
            }
        });
        
        // Gestionnaire pour le bouton Enregistrer
        saveButton.addActionListener(e -> handleSaveButton());
        
        // Gestionnaire pour le bouton Afficher
        afficherButton.addActionListener(e -> actualiserOrdonnances());
        
        // Gestionnaire pour le bouton Supprimer
        supprimerButton.addActionListener(e -> handleDeleteButton());
        
        // Gestionnaire pour le bouton Modifier
        modifierButton.addActionListener(e -> handleModifyButton());
        
        // Gestionnaire pour le bouton Confirmer
        confirmerButton.addActionListener(e -> handleConfirmButton());
    }

    private void handleSaveButton() {
        String idPatient = idPatientField.getText();
        String idConsultationStr = idConsultationField.getText();
        String description = descriptionField.getText();
        String idMedecin = idMedecinField.getText();

        // Récupérer les médicaments sélectionnés
        List<String> selectedMedicaments = medicamentCheckboxes.stream()
            .filter(JCheckBox::isSelected)
            .map(JCheckBox::getText)
            .collect(Collectors.toList());

        if (idPatient.isEmpty() || idConsultationStr.isEmpty() || idMedecin.isEmpty() || selectedMedicaments.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Tous les champs d'ID doivent être remplis et au moins un médicament doit être sélectionné");
            return;
        }

        // Vérifier les principes actifs dupliqués
        String duplicateError = checkDuplicatePrincipesActifs(selectedMedicaments);
        if (duplicateError != null) {
            JOptionPane.showMessageDialog(null, 
                "Attention: Vous avez sélectionné des médicaments avec le même principe actif!\n\n" + duplicateError,
                "Risque d'interaction médicamenteuse", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int idConsultation = Integer.parseInt(idConsultationStr);
            int idMedecinInt = Integer.parseInt(idMedecin);

            Ordonnance newOrdonnance = new Ordonnance();
            newOrdonnance.setIdOrdonnance(Ordonnance.generateIdOrdonnance());
            newOrdonnance.setIdPatient(Integer.parseInt(idPatient));
            newOrdonnance.setIdConsultation(idConsultation);
            newOrdonnance.setIdMedecin(idMedecinInt);
            newOrdonnance.setDescription(description);

            ordonnanceService.insertOrdonnance(newOrdonnance, selectedMedicaments);

            displayArea.append("Ordonnance enregistrée :\n");
            displayArea.append("ID Patient: " + idPatient + "\n");
            displayArea.append("ID Consultation: " + idConsultation + "\n");
            displayArea.append("Description: " + description + "\n");
            displayArea.append("ID Médecin: " + idMedecin + "\n");
            displayArea.append("Médicaments: " + String.join(", ", selectedMedicaments) + "\n");
            displayArea.append("-----------------------------\n");

            // Rafraîchir et réinitialiser
            actualiserOrdonnances();
            resetFields();
            clearDisplayArea();

            JOptionPane.showMessageDialog(null, "Ordonnance enregistrée avec succès");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Veuillez entrer des valeurs valides pour les ID.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Erreur lors de l'enregistrement de l'ordonnance: " + ex.getMessage());
        }
    }

    private void handleDeleteButton() {
        int selectedRow = ordonnanceTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Veuillez sélectionner une ordonnance à supprimer.", "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Récupérer l'ID de l'ordonnance
        int idOrdonnance = (int) ordonnanceTableModel.getValueAt(selectedRow, 0);

        try {
            int confirm = JOptionPane.showConfirmDialog(
                    null,
                    "Êtes-vous sûr de vouloir supprimer cette ordonnance ?",
                    "Confirmation de suppression",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                Ordonnance ordonnanceToDelete = new Ordonnance();
                ordonnanceToDelete.setIdOrdonnance(idOrdonnance);

                logger.debug("Tentative de suppression de l'ordonnance avec ID: " + idOrdonnance);
                ordonnanceService.deleteOrdonnance(ordonnanceToDelete);
                logger.debug("Suppression de l'ordonnance réussie");

                ordonnanceTableModel.removeRow(selectedRow);
                JOptionPane.showMessageDialog(null, "Ordonnance supprimée avec succès.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Erreur lors de la suppression de l'ordonnance: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            logger.error("Erreur lors de la suppression de l'ordonnance", ex);
        }
    }

    private void handleModifyButton() {
        int selectedRow = ordonnanceTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Veuillez sélectionner une ordonnance à modifier.");
            return;
        }
    
        // Récupérer les données de la ligne sélectionnée
        int idOrdonnance = (int) ordonnanceTableModel.getValueAt(selectedRow, 0);
        int idPatient = (int) ordonnanceTableModel.getValueAt(selectedRow, 1);
        int idConsultation = (int) ordonnanceTableModel.getValueAt(selectedRow, 2);
        int idMedecin = (int) ordonnanceTableModel.getValueAt(selectedRow, 3);
        String description = (String) ordonnanceTableModel.getValueAt(selectedRow, 4);
    
        // Stocker l'ID et remplir les champs
        currentOrdonnanceId = idOrdonnance;
        idPatientField.setText(String.valueOf(idPatient));
        idConsultationField.setText(String.valueOf(idConsultation));
        idMedecinField.setText(String.valueOf(idMedecin));
        descriptionField.setText(description);
    
        // Décocher d'abord tous les médicaments
        for (JCheckBox checkBox : medicamentCheckboxes) {
            checkBox.setSelected(false);
        }
    
        // Récupérer et cocher les médicaments associés à cette ordonnance
        try {
            List<String> medicamentsOrdonnance = ordonnanceService.getMedicamentsByOrdonnance(idOrdonnance);
            for (String medicamentNom : medicamentsOrdonnance) {
                for (JCheckBox checkBox : medicamentCheckboxes) {
                    if (checkBox.getText().equals(medicamentNom)) {
                        checkBox.setSelected(true);
                        break;
                    }
                }
                
            }
        } catch (Exception ex) {
            logger.error("Erreur lors de la récupération des médicaments de l'ordonnance", ex);
        }
    
        // Activer le mode modification
        enModeModification = true;
        toggleEditMode(true);
    
        JOptionPane.showMessageDialog(null, "Vous pouvez maintenant modifier les informations de l'ordonnance.");
    }

    
private void handleConfirmButton() {
    try {
        if (currentOrdonnanceId == -1) {
            JOptionPane.showMessageDialog(null, "Erreur: ID d'ordonnance invalide.");
            return;
        }

        // Récupérer les médicaments sélectionnés
        List<String> selectedMedicamentNames = medicamentCheckboxes.stream()
            .filter(JCheckBox::isSelected)
            .map(JCheckBox::getText)
            .collect(Collectors.toList());

        if (selectedMedicamentNames.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Veuillez sélectionner au moins un médicament.");
            return;
        }

        String duplicateMessage = checkDuplicatePrincipesActifs(selectedMedicamentNames);
        if (duplicateMessage != null) {
            int response = JOptionPane.showConfirmDialog(null, 
                "Attention: Vous avez sélectionné des médicaments avec le même principe actif!\n\n" + 
                duplicateMessage + "\n\nVoulez-vous continuer quand même?",
                "Risque d'interaction médicamenteuse", 
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (response != JOptionPane.YES_OPTION) {
                return;
            }
        }

        // Vérifier que tous les champs sont remplis
        if (idPatientField.getText().isEmpty() || idConsultationField.getText().isEmpty() || 
            idMedecinField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Tous les champs d'ID doivent être remplis");
            return;
        }

        // Créer l'objet Ordonnance pour la mise à jour
        Ordonnance ordonnanceToUpdate = new Ordonnance();
        ordonnanceToUpdate.setIdOrdonnance(currentOrdonnanceId);
        ordonnanceToUpdate.setIdPatient(Integer.parseInt(idPatientField.getText()));
        ordonnanceToUpdate.setIdConsultation(Integer.parseInt(idConsultationField.getText()));
        ordonnanceToUpdate.setIdMedecin(Integer.parseInt(idMedecinField.getText()));
        ordonnanceToUpdate.setDescription(descriptionField.getText());

        // Mettre à jour l'ordonnance avec les médicaments
        logger.debug("Tentative de mise à jour de l'ordonnance ID: " + currentOrdonnanceId);
        boolean success = ordonnanceService.updateOrdonnance(ordonnanceToUpdate, selectedMedicamentNames);
        
        if (success) {
            logger.debug("Mise à jour réussie");
            // Réinitialiser l'UI
            actualiserOrdonnances();
            resetFields();
            clearDisplayArea();
            enModeModification = false;
            toggleEditMode(false); // Sortir du mode édition
            
            JOptionPane.showMessageDialog(null, "Ordonnance modifiée avec succès");
        } else {
            logger.error("Échec de la mise à jour - aucune erreur signalée");
            JOptionPane.showMessageDialog(null, "La modification de l'ordonnance a échoué.");
        }
    } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(null, "Veuillez entrer des valeurs valides pour les ID.");
        logger.error("Erreur de format de nombre", ex);
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(null, "Erreur lors de la modification de l'ordonnance: " + ex.getMessage());
        logger.error("Erreur lors de la modification", ex);
    }
}
    private void resetFields() {
        idPatientField.setText("");
        idConsultationField.setText("");
        descriptionField.setText("");
        idMedecinField.setText("");
        searchMedicamentField.setText("");
        
        // Décocher toutes les cases
        for (JCheckBox checkbox : medicamentCheckboxes) {
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


    private void toggleEditMode(boolean editMode) {
        // Rendre les champs modifiables ou non selon le mode
        idPatientField.setEditable(editMode);
        idConsultationField.setEditable(editMode);
        idMedecinField.setEditable(editMode);
        descriptionField.setEditable(editMode);
        
        // S'assurer que les champs sont activés
        idPatientField.setEnabled(true);
        idConsultationField.setEnabled(true);
        idMedecinField.setEnabled(true);
        descriptionField.setEnabled(true);
    
        // Gérer l'état des boutons
        confirmerButton.setVisible(editMode);
        
        // L'inverse de editMode pour les autres boutons
        saveButton.setEnabled(!editMode);
        supprimerButton.setEnabled(!editMode);
        afficherButton.setEnabled(!editMode);
        modifierButton.setEnabled(!editMode);
        
        // Rendre la table non sélectionnable pendant l'édition
        ordonnanceTable.setEnabled(!editMode);
    }

    private void filterMedicaments(String searchText) {
        medicamentCheckboxPanel.removeAll();

        if (searchText.isEmpty()) {
            // Afficher tous les médicaments
            for (JCheckBox checkBox : medicamentCheckboxes) {
                medicamentCheckboxPanel.add(checkBox);
            }
        } else {
            // Filtrer les médicaments
            for (JCheckBox checkBox : medicamentCheckboxes) {
                String medicamentName = checkBox.getText().toLowerCase();
                if (medicamentName.contains(searchText)) {
                    medicamentCheckboxPanel.add(checkBox);
                }
            }
        }

        // Rafraîchir l'affichage du panel
        medicamentCheckboxPanel.revalidate();
        medicamentCheckboxPanel.repaint();

        // Si aucun médicament ne correspond à la recherche, afficher un message
        if (medicamentCheckboxPanel.getComponentCount() == 0) {
            JLabel noResultLabel = new JLabel("Aucun médicament correspondant à \"" + searchText + "\"");
            medicamentCheckboxPanel.add(noResultLabel);
        }

        logger.debug("Recherche de médicaments pour '" + searchText + "' : "
                + (medicamentCheckboxPanel.getComponentCount() == 1 && medicamentCheckboxPanel.getComponent(0) instanceof JLabel
                ? 0 : medicamentCheckboxPanel.getComponentCount())
                + " résultats trouvés");
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
    
    private void actualiserOrdonnances() {
        try {
            logger.debug("Tentative de récupération des ordonnances...");
            Ordonnances ordonnances = ordonnanceService.selectOrdonnances();
            if (ordonnances != null) {
                Set<Ordonnance> ordonnancesSet = ordonnances.getOrdonnances();
                List<Ordonnance> ordonnancesList = new ArrayList<>(ordonnancesSet);
                
                logger.debug("Nombre d'ordonnances récupérées : " + ordonnancesList.size());

                ordonnanceTableModel.setRowCount(0);
                for (Ordonnance ordonnance : ordonnancesList) {
                    ordonnanceTableModel.addRow(new Object[]{
                        ordonnance.getIdOrdonnance(),
                        ordonnance.getIdPatient(),
                        ordonnance.getIdConsultation(),
                        ordonnance.getIdMedecin(),
                        ordonnance.getDescription()
                    });
                }
                ordonnanceTableModel.fireTableDataChanged();

            } else {
                logger.debug("Aucune ordonnance trouvée");
                JOptionPane.showMessageDialog(null, "Aucune ordonnance trouvée.");
            }
        } catch (Exception ex) {
            logger.error("Erreur lors de l'affichage des ordonnances", ex);
            JOptionPane.showMessageDialog(null, "Erreur lors de l'affichage des ordonnances: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            NetworkConfig networkConfig = ConfigLoader.loadConfig(NetworkConfig.class, networkConfigFile);
            OrdonnanceService ordonnanceService = new OrdonnanceService(networkConfig);
            MedicamentService medicamentService = new MedicamentService(networkConfig);
    
            new OrdonnanceFrontEnd(ordonnanceService, medicamentService);
            System.out.println("Interface lancée avec succès !");
        } catch (Exception e) {
            System.err.println("Erreur lors du démarrage de l'application : " + e.getMessage());
            e.printStackTrace();
        }
    }
}