package edu.ezip.ing1.pds.frontend;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ezip.ing1.pds.business.dto.Medicament;
import edu.ezip.ing1.pds.business.dto.Ordonnance;
import edu.ezip.ing1.pds.client.commons.ConfigLoader;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.services.MedicamentService;
import edu.ezip.ing1.pds.services.OrdonnanceService;

public class OrdonnanceFrontEnd extends JFrame {

    private final static String LoggingLabel = "FrontEnd - Ordonnance";
    private final static Logger logger = LoggerFactory.getLogger(LoggingLabel);
    private final static String networkConfigFile = "network.yaml";

    private JTextField idPatientField; 
    private JTextField idConsultationField;
    private JTextField descriptionField;
    private JTextField idMedecinField;
    private JComboBox<String> medicamentComboBox;
    private JTextArea displayArea;

    public OrdonnanceFrontEnd() {
        NetworkConfig networkConfig = null;
        try {
            networkConfig = ConfigLoader.loadConfig(NetworkConfig.class, networkConfigFile);
            logger.debug("Load Network config file : {}", networkConfig);
        } catch (Exception e) {
            logger.error("Erreur lors du chargement du fichier de configuration", e);
            JOptionPane.showMessageDialog(null, "Erreur lors du chargement de la configuration réseau.");
            return;
        }

        final OrdonnanceService ordonnanceService = new OrdonnanceService(networkConfig);
        final MedicamentService medicamentService = new MedicamentService(networkConfig);

        List<Medicament> medicamentsList = medicamentService.selectMedicaments();
        medicamentComboBox = new JComboBox<>();
        if (medicamentsList != null) {
            for (Medicament medicament : medicamentsList) {
                medicamentComboBox.addItem(medicament.getNomMedicament());
            }
        } else {
            medicamentComboBox.addItem("Aucun médicament disponible");
        }

        JPanel inputPanel = new JPanel(new GridLayout(5, 2));
        idPatientField = new JTextField();
        idConsultationField = new JTextField();
        descriptionField = new JTextField();
        idMedecinField = new JTextField();

        inputPanel.add(new JLabel("ID Patient:"));
        inputPanel.add(idPatientField);
        inputPanel.add(new JLabel("ID Consultation:"));
        inputPanel.add(idConsultationField);
        inputPanel.add(new JLabel("Description:"));
        inputPanel.add(descriptionField);
        inputPanel.add(new JLabel("ID Médecin:"));
        inputPanel.add(idMedecinField);
        inputPanel.add(new JLabel("Choisir Médicament:"));
        inputPanel.add(medicamentComboBox);

        displayArea = new JTextArea(5, 30);
        displayArea.setEditable(false);
        JPanel displayPanel = new JPanel();
        displayPanel.add(new JLabel("Informations saisies :"));
        displayPanel.add(displayArea);

        JButton saveButton = new JButton("Enregistrer");
        saveButton.addActionListener(e -> {
            String idPatient = idPatientField.getText();
            String idConsultationStr = idConsultationField.getText();
            String description = descriptionField.getText();
            String idMedecin = idMedecinField.getText();
            String selectedMedicament = (String) medicamentComboBox.getSelectedItem();

            if (idPatient.isEmpty() || idConsultationStr.isEmpty() || description.isEmpty() || idMedecin.isEmpty() || selectedMedicament == null) {
                JOptionPane.showMessageDialog(null, "Tous les champs doivent être remplis");
            } else {
                try {
                    int idConsultation = Integer.parseInt(idConsultationStr);
                    int idMedecinInt = Integer.parseInt(idMedecin);

                    Ordonnance newOrdonnance = new Ordonnance();
                    newOrdonnance.setIdOrdonnance(Ordonnance.generateIdOrdonnance());
                    newOrdonnance.setIdPatient(Integer.parseInt(idPatient));
                    newOrdonnance.setIdConsultation(idConsultation);
                    newOrdonnance.setDescription(description);
                    newOrdonnance.setIdMedecin(idMedecinInt);

                    ordonnanceService.insertOrdonnance(newOrdonnance, List.of(selectedMedicament));

                    displayArea.append("Ordonnance enregistrée :\n");
                    displayArea.append("ID Patient: " + idPatient + "\n");
                    displayArea.append("ID Consultation: " + idConsultation + "\n");
                    displayArea.append("Description: " + description + "\n");
                    displayArea.append("ID Médecin: " + idMedecin + "\n");
                    displayArea.append("Médicament: " + selectedMedicament + "\n");
                    displayArea.append("-----------------------------\n");


                    JOptionPane.showMessageDialog(null, "Ordonnance enregistrée avec succès");
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Veuillez entrer des valeurs valides pour les ID.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Erreur lors de l'enregistrement de l'ordonnance: " + ex.getMessage());
                }
            }
        });

        /*JButton showOrdonnancesButton = new JButton("Afficher les ordonnances");
        showOrdonnancesButton.addActionListener(e -> {
            try {
                Ordonnances ordonnances = ordonnanceService.selectOrdonnances();
                if (ordonnances == null || ordonnances.getOrdonnances().isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Aucune ordonnance trouvée.");
                } else {
                    new OrdonnanceTableView(ordonnances);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Erreur lors de la récupération des ordonnances : " + ex.getMessage());
            }
        });*/
        /*JButton deleteButton = new JButton("Supprimer Ordonnance");
        deleteButton.addActionListener(e -> {
            String idOrdonnanceStr = JOptionPane.showInputDialog("Entrez l'ID de l'ordonnance à supprimer :");
            try {
                int idOrdonnance = Integer.parseInt(idOrdonnanceStr);
                ordonnanceService.deleteOrdonnanceById(idOrdonnance);
                JOptionPane.showMessageDialog(null, "Ordonnance supprimée avec succès !");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Veuillez entrer un ID valide !");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Erreur lors de la suppression de l'ordonnance : " + ex.getMessage());
            }
        });*/

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        //buttonPanel.add(showOrdonnancesButton);
        //buttonPanel.add(deleteButton);

        this.setLayout(new BorderLayout());
        this.add(inputPanel, BorderLayout.NORTH);
        this.add(displayPanel, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH);
        this.setSize(500, 300);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    /*private static class OrdonnanceTableView extends JFrame {
        public OrdonnanceTableView(Ordonnances ordonnances) {
            setTitle("Liste des Ordonnances");
            setSize(600, 400);
            setLocationRelativeTo(null);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            // Affichage dans la console pour vérification
            System.out.println("Affichage des ordonnances dans la console :");
            for (Ordonnance ordonnance : ordonnances.getOrdonnances()) {
                System.out.println("Ordonnance: ID=" + ordonnance.getIdOrdonnance() +
                                   ", Patient ID=" + ordonnance.getIdPatient() +
                                   ", Consultation ID=" + ordonnance.getIdConsultation() +
                                   ", Description=" + ordonnance.getDescription() +
                                   ", Medecin ID=" + ordonnance.getIdMedecin());
            }

            List<Ordonnance> ordonnancesList = ordonnances.getOrdonnances();
            JTable table = new JTable(new OrdonnanceTableModel(ordonnancesList));
            add(new JScrollPane(table), BorderLayout.CENTER);
            setVisible(true);
        }
        
    }*/

    public static void main(String[] args) {
        new OrdonnanceFrontEnd();
    }
}
