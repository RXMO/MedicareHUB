package edu.ezip.ing1.pds;

import edu.ezip.ing1.pds.business.dto.Patient;
import edu.ezip.ing1.pds.business.dto.Patients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import edu.ezip.ing1.pds.services.PatientService;

public class PatientUI {
    private final static Logger logger = LoggerFactory.getLogger("PatientUI");

    private PatientService patientService; // Service pour gérer les patients

    public PatientUI(Patients patients, PatientService patientService) {
        this.patientService = patientService; // Injection du service
        SwingUtilities.invokeLater(() -> createAndShowGUI(patients));
    }

    private void createAndShowGUI(Patients patients) {
        // Créer le modèle de la table
        DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.addColumn("ID");
        tableModel.addColumn("Nom");
        tableModel.addColumn("Prénom");
        tableModel.addColumn("Téléphone");
        tableModel.addColumn("Allergies");

        // Ajouter les patients existants au modèle de table
        if (patients != null && patients.getPatients() != null && !patients.getPatients().isEmpty()) {
            for (Patient patient : patients.getPatients()) {
                tableModel.addRow(new Object[] {
                        patient.getIdPatient(),
                        patient.getNomPatient(),
                        patient.getPrenomPatient(),
                        patient.getNumTel(),
                        patient.getAllergies()
                });
            }
        } else {
            logger.debug("Aucun patient trouvé.");
        }

        // Créer une JTable pour afficher les patients
        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        table.setFillsViewportHeight(true);

        // Créer les champs de saisie pour le formulaire
        JTextField idField = new JTextField(10);
        JTextField nomField = new JTextField(10);
        JTextField prenomField = new JTextField(10);
        JTextField telField = new JTextField(10);
        JTextField allergiesField = new JTextField(15);
        JButton ajouterButton = new JButton("Ajouter");
        JButton afficherButton = new JButton("Afficher la liste");
        JButton supprimerButton = new JButton("Supprimer");

        // Panel pour le formulaire d'ajout
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        formPanel.add(new JLabel("Id:"));
        formPanel.add(idField);
        formPanel.add(new JLabel("Nom:"));
        formPanel.add(nomField);
        formPanel.add(new JLabel("Prénom:"));
        formPanel.add(prenomField);
        formPanel.add(new JLabel("Téléphone:"));
        formPanel.add(telField);
        formPanel.add(new JLabel("Allergies:"));
        formPanel.add(allergiesField);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(ajouterButton);
        buttonPanel.add(afficherButton);
        buttonPanel.add(supprimerButton);

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(formPanel, BorderLayout.CENTER);
        northPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Action du bouton Ajouter
        ajouterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String idText = idField.getText().trim();
                String nom = nomField.getText().trim();
                String prenom = prenomField.getText().trim();
                String tel = telField.getText().trim();
                String allergies = allergiesField.getText().trim();
                int id = Integer.parseInt(idText);
                // Vérification que les champs obligatoires sont remplis
                if (!nom.isEmpty() && !prenom.isEmpty()) {
                    try {
                        // Créer un objet Patient
                        Patient patient = new Patient(id, nom, prenom, tel, allergies);

                        // Insérer le patient dans la base de données via le service
                        patientService.InsertPatient(patient);

                        // Actualiser la liste pour obtenir l'ID généré
                        try {
                            Patients refreshedPatients = patientService.selectPatients();
                            refreshTable(tableModel, refreshedPatients);
                        } catch (Exception ex) {
                            // Si l'actualisation échoue, ajouter le patient sans ID à la table
                            tableModel.addRow(new Object[] { null, nom, prenom, tel, allergies });
                        }

                        // Réinitialiser les champs de texte
                        idField.setText("");
                        nomField.setText("");
                        prenomField.setText("");
                        telField.setText("");
                        allergiesField.setText("");
                    } catch (IOException | InterruptedException ex) {
                        // Capturer les exceptions IOException et InterruptedException
                        JOptionPane.showMessageDialog(null, "Une erreur est survenue lors de l'insertion du patient.",
                                "Erreur", JOptionPane.ERROR_MESSAGE);
                        logger.error("Erreur lors de l'insertion du patient", ex);
                    }
                } else {
                    // Afficher une erreur si un champ est vide
                    JOptionPane.showMessageDialog(null, "Veuillez remplir au moins le nom et le prénom.", "Erreur",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Action du bouton Afficher
        afficherButton.addActionListener(e -> {
            try {
                Patients refreshedPatients = patientService.selectPatients();
                refreshTable(tableModel, refreshedPatients);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Erreur lors de l'actualisation de la liste des patients.",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                logger.error("Erreur lors de l'actualisation", ex);
            }
        });

        // Action du bouton Supprimer
        supprimerButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                // Récupérer l'ID du patient sélectionné
                Integer id = (Integer) tableModel.getValueAt(selectedRow, 0);

                if (id == null) {
                    JOptionPane.showMessageDialog(null, "Impossible de supprimer un patient sans ID.",
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Créer un objet Patient à supprimer avec seulement l'ID
                Patient patientToDelete = new Patient();
                patientToDelete.setIdPatient(id);

                try {
                    // Appeler le service pour supprimer le patient
                    patientService.DeletePatient(patientToDelete);

                    // Supprimer la ligne de la table
                    tableModel.removeRow(selectedRow);
                } catch (IOException | InterruptedException ex) {
                    // Capturer les exceptions IOException et InterruptedException
                    JOptionPane.showMessageDialog(null, "Une erreur est survenue lors de la suppression du patient.",
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                    logger.error("Erreur lors de la suppression du patient", ex);
                }
            } else {
                // Afficher une erreur si aucune ligne n'est sélectionnée
                JOptionPane.showMessageDialog(null, "Veuillez sélectionner un patient à supprimer.", "Erreur",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        // Créer une fenêtre JFrame pour afficher la table et le formulaire
        JFrame frame = new JFrame("Gestion des Patients");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 500);
        frame.setLocationRelativeTo(null); // Centrer la fenêtre
        frame.setLayout(new BorderLayout());
        frame.add(northPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private void refreshTable(DefaultTableModel model, Patients patients) {
        // Effacer toutes les lignes du modèle
        model.setRowCount(0);

        // Ajouter les patients actualisés
        if (patients != null && patients.getPatients() != null) {
            for (Patient patient : patients.getPatients()) {
                model.addRow(new Object[] {
                        patient.getIdPatient(),
                        patient.getNomPatient(),
                        patient.getPrenomPatient(),
                        patient.getNumTel(),
                        patient.getAllergies()
                });
            }
        }
    }
}