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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import edu.ezip.ing1.pds.services.PatientService;

public class PatientUI {
    private final static Logger logger = LoggerFactory.getLogger("PatientUI");
    private PatientService patientService;

    public PatientUI(Patients patients, PatientService patientService) {
        this.patientService = patientService;
        SwingUtilities.invokeLater(() -> createAndShowGUI(patients));
    }

    // Méthode pour vider le formulaire
    private void clearForm(JTextField idField, JTextField nomField, JTextField prenomField, JTextField telField,
            JTextField allergiesField) {
        idField.setText("");
        nomField.setText("");
        prenomField.setText("");
        telField.setText("");
        allergiesField.setText("");
    }

    private void createAndShowGUI(Patients patients) {
        DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.addColumn("ID");
        tableModel.addColumn("Nom");
        tableModel.addColumn("Prénom");
        tableModel.addColumn("Téléphone");
        tableModel.addColumn("Allergies");

        if (patients != null && patients.getPatients() != null) {
            for (Patient patient : patients.getPatients()) {
                tableModel.addRow(new Object[] {
                        patient.getIdPatient(),
                        patient.getNomPatient(),
                        patient.getPrenomPatient(),
                        patient.getNumTel(),
                        patient.getAllergies()
                });
            }
        }

        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        JTextField idField = new JTextField(10);
        JTextField nomField = new JTextField(10);
        JTextField prenomField = new JTextField(10);
        JTextField telField = new JTextField(10);
        JTextField allergiesField = new JTextField(15);
        JButton ajouterButton = new JButton("Ajouter");
        JButton mettreAJourButton = new JButton("Mettre à jour");
        JButton supprimerButton = new JButton("Supprimer");
        JButton afficherButton = new JButton("Actualiser");

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
        buttonPanel.add(mettreAJourButton);
        buttonPanel.add(supprimerButton);
        buttonPanel.add(afficherButton);

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(formPanel, BorderLayout.CENTER);
        northPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Ajouter un écouteur de sélection à la table
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) { // Pour éviter les événements multiples
                    int selectedRow = table.getSelectedRow();
                    if (selectedRow != -1) {
                        // Remplir les champs de saisie avec les données de la ligne sélectionnée
                        idField.setText(tableModel.getValueAt(selectedRow, 0).toString());
                        nomField.setText(tableModel.getValueAt(selectedRow, 1).toString());
                        prenomField.setText(tableModel.getValueAt(selectedRow, 2).toString());
                        telField.setText(tableModel.getValueAt(selectedRow, 3).toString());
                        allergiesField.setText(tableModel.getValueAt(selectedRow, 4).toString());
                    }
                }
            }
        });

        ajouterButton.addActionListener(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                Patient patient = new Patient(id, nomField.getText().trim(), prenomField.getText().trim(),
                        telField.getText().trim(), allergiesField.getText().trim());
                patientService.InsertPatient(patient);
                refreshTable(tableModel, patientService.selectPatients());
                clearForm(idField, nomField, prenomField, telField, allergiesField); // Vider le formulaire après
                                                                                     // l'ajout
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Erreur lors de l'ajout.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        mettreAJourButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                try {
                    int id = Integer.parseInt(idField.getText().trim());
                    Patient patient = new Patient(id, nomField.getText().trim(), prenomField.getText().trim(),
                            telField.getText().trim(), allergiesField.getText().trim());
                    patientService.UpdatePatient(patient);
                    refreshTable(tableModel, patientService.selectPatients());
                    clearForm(idField, nomField, prenomField, telField, allergiesField); // Vider le formulaire après la
                                                                                         // mise à jour
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Erreur lors de la mise à jour.", "Erreur",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Veuillez sélectionner un patient à mettre à jour.", "Erreur",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        supprimerButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                try {
                    int id = (int) tableModel.getValueAt(selectedRow, 0);
                    patientService.DeletePatient(new Patient(id, "", "", "", ""));
                    refreshTable(tableModel, patientService.selectPatients());
                    clearForm(idField, nomField, prenomField, telField, allergiesField); // Vider le formulaire après la
                                                                                         // suppression
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Erreur lors de la suppression.", "Erreur",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        afficherButton.addActionListener(e -> {
            try {
                refreshTable(tableModel, patientService.selectPatients());
                clearForm(idField, nomField, prenomField, telField, allergiesField); // Vider le formulaire après
                                                                                     // l'actualisation
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Erreur lors de l'affichage.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        JFrame frame = new JFrame("Gestion des Patients");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 500);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());
        frame.add(northPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private void refreshTable(DefaultTableModel model, Patients patients) {
        model.setRowCount(0);
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
    };
}