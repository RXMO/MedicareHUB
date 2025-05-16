package edu.ezip.ing1.pds.frontend;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import edu.ezip.ing1.pds.business.dto.DiagnosticResult;
import edu.ezip.ing1.pds.business.dto.Symptomes;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.services.ServiceSymptome;

public class FenetreDiagnostic extends JFrame {

    // CHAMPS DE TEXTE POUR LA SAISIE DES INFORMATIONS
    private JTextField champSymptome;
    private JTextField champModification;
    private JTextField champPatientId;
    private JTextField champDateRendezVous;

    // AFFICHAGE DES DONNÉES
    private DefaultListModel<String> modelList;
    private JList<String> listeSymptomes;
    private JTextArea resultatDiagnostic;

    // BOUTONS DE L'INTERFACE
    private JButton boutonAjouter;
    private JButton boutonAfficher;
    private JButton boutonModifier;
    private JButton boutonSupprimer;
    private JButton boutonDiagnostiquer;
    private JButton boutonPrendreRendezVous;

    private ServiceSymptome serviceSymptome;

    private List<Symptomes> symptomesAjoutes = new ArrayList<>();
    private List<DiagnosticResult> derniersResultats;

    private int idPatientActuel = 1;
    private Map<Integer, JComboBox<String>> creneauxComboBoxes = new HashMap<>();
    private Map<Integer, Map<String, Map<String, Object>>> creneauxMap = new HashMap<>();

    // Nouveau panneau pour les créneaux interactifs
    private JPanel creneauxPanel;

    // Constructeur
    public FenetreDiagnostic() {
        NetworkConfig networkConfig = new NetworkConfig();
        networkConfig.setTcpport(45065);
        networkConfig.setIpaddress("172.31.252.216");

        serviceSymptome = new ServiceSymptome(networkConfig);

        setTitle("Diagnostic Médical");
        setSize(800, 500);
        setMinimumSize(new Dimension(600, 500));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel panelPatient = new JPanel(new FlowLayout(FlowLayout.LEFT));
        champPatientId = new JTextField(5);
        champPatientId.setText(String.valueOf(idPatientActuel));
        JButton boutonChargerPatient = new JButton("Charger Patient");
        boutonChargerPatient.setPreferredSize(new Dimension(130, 30));

        panelPatient.add(new JLabel("ID Patient:"));
        panelPatient.add(champPatientId);
        panelPatient.add(boutonChargerPatient);
        add(panelPatient, BorderLayout.NORTH);

        // AJOUT DE SYMPTÔMES
        JPanel panelHaut = new JPanel();
        panelHaut.setLayout(new BoxLayout(panelHaut, BoxLayout.X_AXIS));
        champSymptome = new JTextField(8);
        boutonAjouter = new JButton("Ajouter");
        boutonAfficher = new JButton("Afficher");

        champSymptome.setMaximumSize(new Dimension(150, 30));
        boutonAjouter.setPreferredSize(new Dimension(120, 40));
        boutonAfficher.setPreferredSize(new Dimension(120, 40));

        panelHaut.add(new JLabel("Symptôme:"));
        panelHaut.add(champSymptome);
        panelHaut.add(boutonAjouter);
        panelHaut.add(boutonAfficher);

        JPanel panelCentre = new JPanel(new BorderLayout());
        panelCentre.add(panelHaut, BorderLayout.NORTH);

        modelList = new DefaultListModel<>();
        listeSymptomes = new JList<>(modelList);
        JScrollPane scrollPane = new JScrollPane(listeSymptomes);
        panelCentre.add(scrollPane, BorderLayout.CENTER);

        add(panelCentre, BorderLayout.CENTER);

        // MODIFICATION ET SUPPRESSION DE SYMPTÔMES
        JPanel panelBas = new JPanel();
        champModification = new JTextField(15);
        boutonModifier = new JButton("Modifier");
        boutonSupprimer = new JButton("Supprimer");

        boutonModifier.setPreferredSize(new Dimension(100, 30));
        boutonSupprimer.setPreferredSize(new Dimension(100, 30));

        panelBas.add(new JLabel("Modifier le symptôme sélectionné:"));
        panelBas.add(champModification);
        panelBas.add(boutonModifier);
        panelBas.add(boutonSupprimer);
        add(panelBas, BorderLayout.SOUTH);

        // DIAGNOSTIC ET RENDEZ-VOUS
        JPanel panelDroite = new JPanel(new BorderLayout());
        boutonDiagnostiquer = new JButton("Diagnostiquer");
        resultatDiagnostic = new JTextArea(10, 20);
        resultatDiagnostic.setEditable(false);

        boutonDiagnostiquer.setPreferredSize(new Dimension(150, 30));

        // Panneau pour les créneaux
        creneauxPanel = new JPanel();
        creneauxPanel.setLayout(new BoxLayout(creneauxPanel, BoxLayout.Y_AXIS));
        JScrollPane creneauxScrollPane = new JScrollPane(creneauxPanel);

        JPanel panelRendezVous = new JPanel(new FlowLayout());
        champDateRendezVous = new JTextField(10);
        boutonPrendreRendezVous = new JButton("Prendre Rendez-vous");
        boutonPrendreRendezVous.setPreferredSize(new Dimension(150, 30));

        panelRendezVous.add(new JLabel("Date (AAAA-MM-JJ):"));
        panelRendezVous.add(champDateRendezVous);
        panelRendezVous.add(boutonPrendreRendezVous);

        panelDroite.add(boutonDiagnostiquer, BorderLayout.NORTH);
        panelDroite.add(new JScrollPane(resultatDiagnostic), BorderLayout.CENTER);
        panelDroite.add(creneauxScrollPane, BorderLayout.SOUTH);
        panelDroite.add(panelRendezVous, BorderLayout.PAGE_END);
        add(panelDroite, BorderLayout.EAST);

        // ÉCOUTEURS D'ÉVÉNEMENTS
        boutonAjouter.addActionListener(e -> ajouterSymptome());
        boutonAfficher.addActionListener(e -> afficherSymptomes());
        boutonModifier.addActionListener(e -> modifierSymptome());
        boutonSupprimer.addActionListener(e -> supprimerSymptome());
        boutonDiagnostiquer.addActionListener(e -> diagnostiquer());
        boutonChargerPatient.addActionListener(e -> chargerPatient());
        boutonPrendreRendezVous.addActionListener(e -> prendreRendezVous());

        chargerSymptomesPatient();
    }

    // (Méthodes inchangées : chargerPatient, chargerSymptomesPatient, ajouterSymptome, afficherSymptomes, modifierSymptome, supprimerSymptome)

    private void chargerPatient() {
        try {
            String idText = champPatientId.getText().trim();
            if (idText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Veuillez saisir un ID patient valide", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            int newPatientId = Integer.parseInt(idText);
            if (newPatientId <= 0) {
                JOptionPane.showMessageDialog(this, "L'ID patient doit être un entier positif", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            idPatientActuel = newPatientId;
            
            chargerSymptomesPatient();
            
            JOptionPane.showMessageDialog(this, "Patient " + idPatientActuel + " chargé avec succès", "Succès", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "L'ID patient doit être un nombre entier", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void chargerSymptomesPatient() {
        try {
            symptomesAjoutes.clear();
            List<Symptomes> symptomesPatient = serviceSymptome.getSymptomesPatient(idPatientActuel);
            symptomesAjoutes.addAll(symptomesPatient);
            afficherSymptomes();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Erreur lors du chargement des symptômes: " + ex.getMessage(), 
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void ajouterSymptome() {
        String symptomText = champSymptome.getText().trim();
        
        if (symptomText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Le champ est vide !", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            Symptomes symptom = new Symptomes(0, symptomText);
            Symptomes symptomWithId = serviceSymptome.associerSymptomePatient(idPatientActuel, symptom);
            champSymptome.setText("");
            
            if (symptomWithId == null) {
                JOptionPane.showMessageDialog(this, 
                    "Erreur : Impossible d'associer le symptôme au patient. Réponse du serveur non valide.", 
                    "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            boolean exists = false;
            for (Symptomes s : symptomesAjoutes) {
                if (s.getId() == symptomWithId.getId()) {
                    exists = true;
                    break;
                }
            }
            
            if (!exists) {
                symptomesAjoutes.add(symptomWithId);
            }
            
            JOptionPane.showMessageDialog(this, 
                "Symptôme associé au patient avec succès ! ID: " + symptomWithId.getId(), 
                "Succès", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Erreur lors de l'ajout du symptôme: " + ex.getMessage(), 
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void afficherSymptomes() {
        modelList.clear();
        for (Symptomes s : symptomesAjoutes) {
            modelList.addElement(s.getDescription());
        }
    }

    private void modifierSymptome() {
        String selectedSymptom = listeSymptomes.getSelectedValue();
        String newName = champModification.getText().trim();

        if (selectedSymptom == null || newName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Le champ est vide ou aucun symptôme n'est sélectionné !", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int symptomId = 0;
            int index = -1;
            for (int i = 0; i < symptomesAjoutes.size(); i++) {
                Symptomes s = symptomesAjoutes.get(i);
                if (s.getDescription().equals(selectedSymptom)) {
                    symptomId = s.getId();
                    index = i;
                    break;
                }
            }
            
            if (symptomId == 0) {
                JOptionPane.showMessageDialog(this, "Impossible de trouver l'ID du symptôme sélectionné.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            List<Symptomes> allSymptoms = serviceSymptome.selectSymptomes();
            boolean symptomExists = false;
            
            for (Symptomes s : allSymptoms) {
                if (s.getDescription().equals(newName)) {
                    symptomExists = true;
                    break;
                }
            }
            
            if (!symptomExists) {
                JOptionPane.showMessageDialog(this, 
                    "Le symptôme '" + newName + "' n'existe pas dans la base de données.\n" +
                    "Veuillez l'ajouter comme nouveau symptôme avant de l'utiliser pour la modification.",
                    "Symptôme non trouvé", JOptionPane.WARNING_MESSAGE);
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

            JOptionPane.showMessageDialog(this, "Symptôme modifié avec succès !", "Succès", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void supprimerSymptome() {
        String selectedSymptom = listeSymptomes.getSelectedValue();
        if (selectedSymptom == null) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un symptôme à supprimer.", "Avertissement", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            int symptomId = 0;
            for (Symptomes s : symptomesAjoutes) {
                if (s.getDescription().equals(selectedSymptom)) {
                    symptomId = s.getId();
                    break;
                }
            }
            
            if (symptomId == 0) {
                JOptionPane.showMessageDialog(this, 
                    "Impossible de trouver l'ID du symptôme " + selectedSymptom, 
                    "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Êtes-vous sûr de vouloir supprimer l'association avec le symptôme \"" + selectedSymptom + "\" ?",
                "Confirmation de suppression",
                JOptionPane.YES_NO_OPTION);
            
            if (confirmation != JOptionPane.YES_OPTION) {
                return;
            }
            
            String message = serviceSymptome.supprimerSymptomePatient(idPatientActuel, symptomId);
            
            boolean deleted = false;
            for (int i = 0; i < symptomesAjoutes.size(); i++) {
                if (symptomesAjoutes.get(i).getId() == symptomId) {
                    symptomesAjoutes.remove(i);
                    deleted = true;
                    break;
                }
            }
            
            if (!deleted) {
                chargerSymptomesPatient();
            } else {
                afficherSymptomes();
            }
            
            JOptionPane.showMessageDialog(this, message, "Succès", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Erreur lors de la suppression: " + ex.getMessage(), 
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ANALYSE DES SYMPTÔMES POUR OBTENIR UN DIAGNOSTIC
    
  private void diagnostiquer() {
    long debut = System.currentTimeMillis();
    try {
        derniersResultats = serviceSymptome.diagnostiquer(idPatientActuel);
        StringBuilder sb = new StringBuilder("Maladies possibles:\n");
        creneauxComboBoxes.clear();
        creneauxMap.clear();
        creneauxPanel.removeAll(); // Nettoie le panneau des créneaux

        if (derniersResultats.isEmpty()) {
            sb.append("Aucune maladie trouvée pour ces symptômes.\n");
        } else {
            // Limiter à 5 diagnostics pour l'affichage
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

                    // Créer un JComboBox pour ce diagnostic
                    JComboBox<String> creneauComboBox = new JComboBox<>(creneauOptions.toArray(new String[0]));
                    creneauxComboBoxes.put(i, creneauComboBox);

                    // Ajouter un label et le JComboBox au panneau des créneaux
                    JPanel creneauEntry = new JPanel(new FlowLayout(FlowLayout.LEFT));
                    creneauEntry.add(new JLabel("Créneaux pour diagnostic " + (i + 1) + ":"));
                    creneauEntry.add(creneauComboBox);
                    creneauxPanel.add(creneauEntry);
                } else {
                    sb.append("  Aucun créneau disponible pour cette spécialité.\n");
                }
            }
        }
        resultatDiagnostic.setText(sb.toString());
        creneauxPanel.revalidate();
        creneauxPanel.repaint();
    } catch (Exception ex) {
        resultatDiagnostic.setText("Erreur lors du diagnostic: " + ex.getMessage());
        JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage(), "Erreur de diagnostic", JOptionPane.ERROR_MESSAGE);
    }
    long fin = System.currentTimeMillis();
    System.out.println("Temps diagnostic : " + (fin - debut) + " ms");
}


    // ENREGISTREMENT D'UN RENDEZ-VOUS
    private void prendreRendezVous() {
        try {
            String appointmentDate = champDateRendezVous.getText().trim();
            if (appointmentDate.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Veuillez saisir une date pour le rendez-vous (format AAAA-MM-JJ).", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (derniersResultats == null || derniersResultats.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Effectuez un diagnostic avant de prendre un rendez-vous.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Demander à l'utilisateur de choisir un diagnostic
            String[] diagnosticOptions = new String[derniersResultats.size()];
            for (int i = 0; i < derniersResultats.size(); i++) {
                diagnosticOptions[i] = "Diagnostic " + (i + 1) + ": " + derniersResultats.get(i).toString();
            }
            String selectedDiagnosticStr = (String) JOptionPane.showInputDialog(
                this,
                "Sélectionnez un diagnostic pour prendre rendez-vous:",
                "Choix du diagnostic",
                JOptionPane.PLAIN_MESSAGE,
                null,
                diagnosticOptions,
                diagnosticOptions[0]
            );

            if (selectedDiagnosticStr == null) {
                return; // L'utilisateur a annulé
            }

            // Extraire l'index du diagnostic sélectionné
            int selectedIndex = Integer.parseInt(selectedDiagnosticStr.split(":")[0].replace("Diagnostic ", "").trim()) - 1;
            DiagnosticResult selectedDiagnostic = derniersResultats.get(selectedIndex);

            int specialtyId = selectedDiagnostic.getIdSpecialite();
            if (specialtyId == 0) {
                specialtyId = 1;
            }

            // Vérifier si des créneaux sont disponibles
            if (!creneauxComboBoxes.containsKey(selectedIndex)) {
                JOptionPane.showMessageDialog(this, "Aucun créneau disponible pour ce diagnostic.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Récupérer le créneau sélectionné dans le JComboBox
            JComboBox<String> selectedComboBox = creneauxComboBoxes.get(selectedIndex);
            String selectedCreneauStr = (String) selectedComboBox.getSelectedItem();
            Map<String, Map<String, Object>> creneauDetails = creneauxMap.get(selectedIndex);
            Map<String, Object> selectedCreneau = creneauDetails.get(selectedCreneauStr);

            if (selectedCreneau == null) {
                JOptionPane.showMessageDialog(this, "Erreur lors de la récupération du créneau sélectionné.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int idDisponibilite = (int) selectedCreneau.get("id_disponibilite");
            int idMedecin = (int) selectedCreneau.get("id_medecin");

            // ENVOI DE LA DEMANDE DE RENDEZ-VOUS AU SERVEUR
            String message = serviceSymptome.creerRendezVous(idPatientActuel, appointmentDate, specialtyId, idDisponibilite, idMedecin);

            // VÉRIFICATION DU RÉSULTAT ET AFFICHAGE AU UTILISATEUR
            if (message.toLowerCase().contains("erreur") || message.toLowerCase().contains("non disponible") || message.toLowerCase().contains("invalide")) {
                JOptionPane.showMessageDialog(this, message, "Erreur lors de la prise de rendez-vous", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, message, "Succès", JOptionPane.INFORMATION_MESSAGE);
            }

            // MISE À JOUR DES CRÉNEAUX DISPONIBLES APRÈS PRISE DU RENDEZ-VOUS
            diagnostiquer();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur lors de la prise de rendez-vous: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FenetreDiagnostic().setVisible(true));
    }
}