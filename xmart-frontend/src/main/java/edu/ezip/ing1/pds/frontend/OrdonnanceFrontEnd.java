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

    private JTextField idPatientField; 
    private JTextField idConsultationField;
    private JTextField descriptionField;
    private JTextField idMedecinField;
    private JTextField searchMedicamentField;
    private JPanel medicamentCheckboxPanel;
    private List<JCheckBox> medicamentCheckboxes;
    private JTextArea displayArea;

    private DefaultTableModel ordonnanceTableModel;
    private JTable ordonnanceTable;
    private List<Medicament> medicamentsList;

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
        medicamentsList = new ArrayList<>(medicaments.getMedicaments()); // Convertir Set en List
        medicamentCheckboxes = new ArrayList<>();
        
        // Panel de saisie des données (partie supérieure)
        JPanel topPanel = new JPanel(new BorderLayout());
        
        // Sous-panel pour les champs de texte
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
        
        
        inputPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        topPanel.add(inputPanel, BorderLayout.NORTH);
        
        // Panel de recherche de médicaments
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Rechercher un médicament:"));
        searchMedicamentField = new JTextField(20);
        searchPanel.add(searchMedicamentField);
        
        // Ajouter un écouteur d'événements pour la recherche
        searchMedicamentField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String searchText = searchMedicamentField.getText().toLowerCase();
                filterMedicaments(searchText);
            }
        });
        
        // Création du panel des checkboxes pour les médicaments
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
        
        // Créer un panel pour contenir la recherche et la liste des médicaments
        JPanel medicamentPanel = new JPanel(new BorderLayout());
        medicamentPanel.add(searchPanel, BorderLayout.NORTH);
        medicamentPanel.add(medicamentScrollPane, BorderLayout.CENTER);
        medicamentPanel.setBorder(new TitledBorder("Sélectionner les médicaments"));
        
        topPanel.add(medicamentPanel, BorderLayout.CENTER);
        this.add(topPanel, BorderLayout.NORTH);
        
        // Création du modèle de table pour afficher les ordonnances
        ordonnanceTableModel = new DefaultTableModel();
        ordonnanceTableModel.addColumn("ID Ordonnance");
        ordonnanceTableModel.addColumn("ID Patient");
        ordonnanceTableModel.addColumn("ID Consultation");
        ordonnanceTableModel.addColumn("ID Médecin");
        ordonnanceTableModel.addColumn("Description");
        

        ordonnanceTable = new JTable(ordonnanceTableModel);
        ordonnanceTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        // Création du panel central avec la zone de texte et la table (ordre inversé)
        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 10, 10, 10));
        
        // Configuration de la zone d'affichage 
        displayArea = new JTextArea(5, 40);
        displayArea.setEditable(false);
        JScrollPane displayScrollPane = new JScrollPane(displayArea);
        
        JPanel displayPanel = new JPanel(new BorderLayout());
        displayPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Informations saisies"));
        displayPanel.add(displayScrollPane, BorderLayout.CENTER);
        centerPanel.add(displayPanel, BorderLayout.NORTH);
        
        // Configuration du JScrollPane pour la table 
        JScrollPane tableScrollPane = new JScrollPane(ordonnanceTable);
        tableScrollPane.setPreferredSize(new Dimension(750, 200));
        tableScrollPane.setBorder(javax.swing.BorderFactory.createTitledBorder("Liste des Ordonnances"));
        centerPanel.add(tableScrollPane, BorderLayout.CENTER);
        
        this.add(centerPanel, BorderLayout.CENTER);

        // Panel des boutons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 10, 10, 10));

        JButton saveButton = new JButton("Enregistrer");
        saveButton.addActionListener(e -> {
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
            } else {
                // Vérifier les principes actifs dupliqués
                String duplicateError = checkDuplicatePrincipesActifs(selectedMedicaments);
                if (duplicateError != null) {
                    JOptionPane.showMessageDialog(null, 
                        "Attention: Vous avez sélectionné des médicaments avec le même principe actif!\n\n" + duplicateError,
                        "Risque d'interaction médicamenteuse", 
                        JOptionPane.WARNING_MESSAGE);
                    return; // Arrêter l'enregistrement
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
        
                    // Rafraîchir la liste des ordonnances après ajout
                    actualiserOrdonnances();
                    
                    // Vider les champs après l'enregistrement
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
                    
                    JOptionPane.showMessageDialog(null, "Ordonnance enregistrée avec succès");
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Veuillez entrer des valeurs valides pour les ID.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Erreur lors de l'enregistrement de l'ordonnance: " + ex.getMessage());
                }
            }
        });
        // Afficher toutes les ordonnances
        JButton afficherButton = new JButton("Afficher toutes les ordonnances");
        afficherButton.addActionListener(e -> {
            actualiserOrdonnances();
        });

        // Action du bouton Supprimer
        JButton supprimerButton = new JButton("Supprimer");
        supprimerButton.addActionListener(e -> {
            int selectedRow = ordonnanceTable.getSelectedRow(); // Récupérer la ligne sélectionnée dans la table
            if (selectedRow != -1) {
                // Récupérer l'ID de l'ordonnance sélectionnée
                int idOrdonnance = (int) ordonnanceTableModel.getValueAt(selectedRow, 0);

                try {
                    // Confirmer la suppression
                    int confirm = JOptionPane.showConfirmDialog(
                        null, 
                        "Êtes-vous sûr de vouloir supprimer cette ordonnance ?", 
                        "Confirmation de suppression", 
                        JOptionPane.YES_NO_OPTION
                    );
                    
                    if (confirm == JOptionPane.YES_OPTION) {
                        // Créer un objet Ordonnance à supprimer en utilisant l'ID
                        Ordonnance ordonnanceToDelete = new Ordonnance();
                        ordonnanceToDelete.setIdOrdonnance(idOrdonnance);

                        // Appeler le service pour supprimer l'ordonnance
                        logger.debug("Tentative de suppression de l'ordonnance avec ID: " + idOrdonnance);
                        ordonnanceService.deleteOrdonnance(ordonnanceToDelete);
                        logger.debug("Suppression de l'ordonnance réussie");

                        // Supprimer la ligne de la table
                        ordonnanceTableModel.removeRow(selectedRow);

                        // Afficher un message de succès
                        JOptionPane.showMessageDialog(null, "Ordonnance supprimée avec succès.");
                    }
                } catch (Exception ex) {
                    // Capturer les exceptions si une erreur se produit
                    JOptionPane.showMessageDialog(null, "Erreur lors de la suppression de l'ordonnance: " + ex.getMessage(),
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                    logger.error("Erreur lors de la suppression de l'ordonnance", ex);
                }
            } else {
                // Afficher une erreur si aucune ligne n'est sélectionnée
                JOptionPane.showMessageDialog(null, "Veuillez sélectionner une ordonnance à supprimer.", "Erreur",
                        JOptionPane.ERROR_MESSAGE);
            }
        });


        buttonPanel.add(saveButton);
        buttonPanel.add(afficherButton);
        buttonPanel.add(supprimerButton);
        this.add(buttonPanel, BorderLayout.SOUTH);

        // Chargement initial des données
        actualiserOrdonnances();
        
        // Afficher la fenêtre
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    /**
 * Filtre les médicaments affichés en fonction du texte de recherche
 * @param searchText Le texte de recherche
 */
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
    
    // Afficher le nombre de résultats dans les logs
    logger.debug("Recherche de médicaments pour '" + searchText + "' : " 
               + (medicamentCheckboxPanel.getComponentCount() == 1 && medicamentCheckboxPanel.getComponent(0) instanceof JLabel 
                  ? 0 : medicamentCheckboxPanel.getComponentCount()) 
               + " résultats trouvés");
}

    /**
 * Vérifie si les médicaments sélectionnés contiennent des principes actifs dupliqués
 * @param selectedMedicamentNames Liste des noms de médicaments sélectionnés
 * @return Un message d'erreur s'il y a des doublons, sinon null
 */
private String checkDuplicatePrincipesActifs(List<String> selectedMedicamentNames) {

    System.out.println("====== VÉRIFICATION DES PRINCIPES ACTIFS ======");
    System.out.println("Médicaments sélectionnés: " + selectedMedicamentNames);
    
    // Affichez les principes actifs disponibles
    System.out.println("--- Principes actifs disponibles ---");
    for (Medicament med : medicamentsList) {
        System.out.println(med.getNomMedicament() + " => " + med.getPrincipeActif());
    }
    // Créer une map pour stocker les principes actifs et leurs médicaments associés
    Map<String, List<String>> principeActifMap = new HashMap<>();
    
    // Pour chaque médicament sélectionné
    for (String medicamentName : selectedMedicamentNames) {
        // Trouver le médicament correspondant dans la liste complète
        Medicament selectedMed = medicamentsList.stream()
                .filter(med -> med.getNomMedicament().equals(medicamentName))
                .findFirst()
                .orElse(null);
        
        if (selectedMed != null && selectedMed.getPrincipeActif() != null 
                && !selectedMed.getPrincipeActif().trim().isEmpty()) {
            String principeActif = selectedMed.getPrincipeActif();
            // Ajouter à la map
            if (!principeActifMap.containsKey(principeActif)) {
                principeActifMap.put(principeActif, new ArrayList<>());
            }
            principeActifMap.get(principeActif).add(medicamentName);
        }
    }
    
    // Vérifier s'il y a des principes actifs qui ont plus d'un médicament
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
            Ordonnances ordonnances = ordonnanceService.selectOrdonnances(); // Retourne un objet Ordonnances
            if (ordonnances != null) {
                Set<Ordonnance> ordonnancesSet = ordonnances.getOrdonnances();  // Appelle getOrdonnances() sur l'objet ordonnances
                List<Ordonnance> ordonnancesList = new ArrayList<>(ordonnancesSet);
                
                logger.debug("Nombre d'ordonnances récupérées : " + ordonnancesList.size()); // Debug pour vérifier le nombre d'ordonnances

                ordonnanceTableModel.setRowCount(0); // Réinitialiser la table
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