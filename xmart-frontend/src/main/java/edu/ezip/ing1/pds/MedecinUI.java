package edu.ezip.ing1.pds;

import edu.ezip.ing1.pds.business.dto.Medecin;
import edu.ezip.ing1.pds.business.dto.Medecins;
import edu.ezip.ing1.pds.services.MedecinService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class MedecinUI {
    private final static Logger logger = LoggerFactory.getLogger("MedecinUI");
    private MedecinService medecinService;

    public MedecinUI(Medecins medecins, MedecinService medecinService) {
        this.medecinService = medecinService;
        SwingUtilities.invokeLater(() -> createAndShowGUI(medecins));
    }

    private void clearForm(JTextField idField, JTextField nomField, JTextField prenomField, JTextField specialiteField,
            JTextField telField) {
        idField.setText("");
        nomField.setText("");
        prenomField.setText("");
        specialiteField.setText("");
        telField.setText("");
    }

    private void createAndShowGUI(Medecins medecins) {
        DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.addColumn("ID");
        tableModel.addColumn("Nom");
        tableModel.addColumn("Prénom");
        tableModel.addColumn("Spécialité");
        tableModel.addColumn("Téléphone");

        if (medecins != null && medecins.getMedecins() != null) {
            for (Medecin medecin : medecins.getMedecins()) {
                tableModel.addRow(new Object[] {
                        medecin.getIdMedecin(),
                        medecin.getNomMedecin(),
                        medecin.getPrenomMedecin(),
                        medecin.getSpecialite(),
                        medecin.getNumTel()
                });
            }
        }

        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        JTextField idField = new JTextField(10);
        JTextField nomField = new JTextField(10);
        JTextField prenomField = new JTextField(10);
        JTextField specialiteField = new JTextField(15);
        JTextField telField = new JTextField(10);
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
        formPanel.add(new JLabel("Spécialité:"));
        formPanel.add(specialiteField);
        formPanel.add(new JLabel("Téléphone:"));
        formPanel.add(telField);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(ajouterButton);
        buttonPanel.add(mettreAJourButton);
        buttonPanel.add(supprimerButton);
        buttonPanel.add(afficherButton);

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(formPanel, BorderLayout.CENTER);
        northPanel.add(buttonPanel, BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    idField.setText(getCellValue(tableModel, selectedRow, 0));
                    nomField.setText(getCellValue(tableModel, selectedRow, 1));
                    prenomField.setText(getCellValue(tableModel, selectedRow, 2));
                    specialiteField.setText(getCellValue(tableModel, selectedRow, 3));
                    telField.setText(getCellValue(tableModel, selectedRow, 4));
                }
            }
        });

        ajouterButton.addActionListener(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                Medecin medecin = new Medecin(id, nomField.getText().trim(), prenomField.getText().trim(),
                        specialiteField.getText().trim(), telField.getText().trim());
                medecinService.InsertMedecin(medecin);
                refreshTable(tableModel, medecinService.selectMedecins());
                clearForm(idField, nomField, prenomField, specialiteField, telField);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Erreur lors de l'ajout.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        mettreAJourButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                try {
                    int id = Integer.parseInt(idField.getText().trim());
                    Medecin medecin = new Medecin(id, nomField.getText().trim(), prenomField.getText().trim(),
                            specialiteField.getText().trim(), telField.getText().trim());
                    medecinService.UpdateMedecin(medecin);
                    refreshTable(tableModel, medecinService.selectMedecins());
                    clearForm(idField, nomField, prenomField, specialiteField, telField);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Erreur lors de la mise à jour.", "Erreur",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        supprimerButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                try {
                    int id = Integer.parseInt(getCellValue(tableModel, selectedRow, 0));
                    medecinService.DeleteMedecin(new Medecin(id, "", "", "", ""));
                    refreshTable(tableModel, medecinService.selectMedecins());
                    clearForm(idField, nomField, prenomField, specialiteField, telField);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Erreur lors de la suppression.", "Erreur",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JFrame frame = new JFrame("Gestion des Médecins");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 500);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());
        frame.add(northPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private void refreshTable(DefaultTableModel model, Medecins medecins) {
        model.setRowCount(0);
        if (medecins != null && medecins.getMedecins() != null) {
            for (Medecin medecin : medecins.getMedecins()) {
                model.addRow(new Object[] {
                        medecin.getIdMedecin(),
                        medecin.getNomMedecin(),
                        medecin.getPrenomMedecin(),
                        medecin.getSpecialite(),
                        medecin.getNumTel()
                });
            }
        }
    }

    private String getCellValue(DefaultTableModel model, int row, int col) {
        Object value = model.getValueAt(row, col);
        return value != null ? value.toString() : "";
    }
}
